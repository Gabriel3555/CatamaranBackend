package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.catamaran.catamaranbackend.domain.*;
import com.catamaran.catamaranbackend.repository.BoatDocumentRepository;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boat")
public class BoatController {

    private final BoatRepository boatRepository;
    private final UserRepositoryJpa userRepository;
    private final BoatDocumentRepository boatDocumentRepository;
    private final PaymentRepository paymentRepository;

    @Value("${app.upload.dir:src/main/resources/static/documents/}")
    private String uploadDir;

    @GetMapping("/{id}")
    public ResponseEntity<BoatEntity> getById(@PathVariable Long id) {
        return boatRepository.findById(id)
                .map(boat -> ResponseEntity.ok(boat))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<BoatEntity>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "all") String status) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            // Convert string type to enum, handling "all" case
            BoatType boatType = null;
            if (!"all".equals(type)) {
                try {
                    boatType = BoatType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().build();
                }
            }

            // Use the proper filtering method from the repository
              Page<BoatEntity> boats = boatRepository.findWithFilters(search, boatType, status, pageable);

            return ResponseEntity.ok(boats);
        } catch (Exception e) {
            // Log the actual error for debugging
            System.err.println("Error in BoatController.getAll: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be handled by GlobalExceptionHandler
        }
    }

    @PostMapping
    public ResponseEntity<BoatEntity> createBoat(@RequestBody BoatEntity boat) {
        BoatEntity savedBoat = boatRepository.save(boat);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBoat);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoatEntity> updateBoat(@PathVariable Long id, @RequestBody BoatEntity boat) {
        if (!boatRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        boat.setId(id);
        BoatEntity updatedBoat = boatRepository.save(boat);
        return ResponseEntity.ok(updatedBoat);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoatById(@PathVariable Long id) {
        if (!boatRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        boatRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/{boatId}/documents")
    public ResponseEntity<List<BoatDocumentEntity>> getBoatDocuments(@PathVariable Long boatId) {
        Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
        if (boatOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<BoatDocumentEntity> documents = boatOpt.get().getDocuments();
        return ResponseEntity.ok(documents != null ? documents : new ArrayList<>());
    }

    @PostMapping("/{boatId}/documents")
    public ResponseEntity<BoatDocumentEntity> addDocumentToBoat(
            @PathVariable Long boatId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String documentName) {

        Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
        if (boatOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Generar nombre único para el archivo
            String fileName = "boat_" + boatId + "_doc_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = uploadDir + fileName;

            // Crear directorio si no existe
            Path directoryPath = Paths.get(uploadDir);
            Files.createDirectories(directoryPath);

            // Guardar archivo en el directorio
            Path path = Paths.get(filePath);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Verificar que el archivo se guardó correctamente
            if (Files.size(path) == 0) {
                Files.deleteIfExists(path);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // Crear el documento con URL para descarga
            String webUrl = "/api/v1/boat/documents/" + fileName;
            BoatDocumentEntity document = BoatDocumentEntity.builder()
                    .name(documentName)
                    .url(webUrl)
                    .boat(boatOpt.get())
                    .build();

            BoatDocumentEntity savedDocument = boatDocumentRepository.save(document);

            // Agregar el documento al bote
            BoatEntity boat = boatOpt.get();
            if (boat.getDocuments() == null) {
                boat.setDocuments(new ArrayList<>());
            }
            boat.getDocuments().add(savedDocument);
            boatRepository.save(boat);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{boatId}/documents/{documentId}")
    public ResponseEntity<BoatDocumentEntity> updateDocument(
            @PathVariable Long boatId,
            @PathVariable Long documentId,
            @RequestParam("name") String documentName) {

        Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
        if (boatOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<BoatDocumentEntity> documentOpt = boatDocumentRepository.findById(documentId);
        if (documentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verificar que el documento pertenece al bote
        BoatEntity boat = boatOpt.get();
        boolean documentBelongsToBoat = boat.getDocuments() != null &&
                boat.getDocuments().stream().anyMatch(doc -> doc.getId().equals(documentId));

        if (!documentBelongsToBoat) {
            return ResponseEntity.badRequest().build();
        }

        BoatDocumentEntity document = documentOpt.get();
        document.setName(documentName);
        BoatDocumentEntity updatedDocument = boatDocumentRepository.save(document);

        return ResponseEntity.ok(updatedDocument);
    }

    @DeleteMapping("/{boatId}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long boatId,
            @PathVariable Long documentId) {

        Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
        if (boatOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<BoatDocumentEntity> documentOpt = boatDocumentRepository.findById(documentId);
        if (documentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BoatEntity boat = boatOpt.get();
        BoatDocumentEntity document = documentOpt.get();

        // Verificar que el documento pertenece al bote
        boolean documentBelongsToBoat = boat.getDocuments() != null &&
                boat.getDocuments().stream().anyMatch(doc -> doc.getId().equals(documentId));

        if (!documentBelongsToBoat) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Convertir URL a ruta del sistema de archivos
            String webUrl = document.getUrl();
            String fileName = webUrl.replace("/api/v1/boat/documents/", "");
            String filePath = uploadDir + fileName;

            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);

            boat.getDocuments().removeIf(doc -> doc.getId().equals(documentId));
            boatRepository.save(boat);

            boatDocumentRepository.deleteById(documentId);

            return ResponseEntity.noContent().build();

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/documents/{filename:.+}")
    public ResponseEntity<Resource> getDocument(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();

            if (Files.exists(filePath) && Files.isReadable(filePath)) {
                // Determine content type
                String contentType = "application/octet-stream";
                try {
                    contentType = Files.probeContentType(filePath);
                } catch (IOException e) {
                    // Use default content type
                }

                Resource resource = new InputStreamResource(Files.newInputStream(filePath));

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .contentLength(Files.size(filePath))
                        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{boatId}/owner/{ownerId}")
    @Transactional
    public ResponseEntity<BoatEntity> assignOwner(
            @PathVariable Long boatId,
            @PathVariable Long ownerId,
            @RequestParam int numberOfInstallments) {

        try {
            // Validate parameters first
            int[] validInstallments = {1, 6, 12, 24, 48, 72};
            boolean isValid = false;
            for (int valid : validInstallments) {
                if (numberOfInstallments == valid) {
                    isValid = true;
                    break;
                }
            }

            if (!isValid) {
                System.err.println("Invalid number of installments: " + numberOfInstallments);
                return ResponseEntity.badRequest().build();
            }

            // Find boat
            Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
            if (boatOpt.isEmpty()) {
                System.err.println("Boat not found: " + boatId);
                return ResponseEntity.notFound().build();
            }

            // Find owner
            Optional<UserEntity> ownerOpt = userRepository.findById(ownerId);
            if (ownerOpt.isEmpty()) {
                System.err.println("Owner not found: " + ownerId);
                return ResponseEntity.notFound().build();
            }

            BoatEntity boat = boatOpt.get();
            UserEntity owner = ownerOpt.get();

            // Check if boat already has an owner
            if (boat.getOwner() != null) {
                System.err.println("Boat already has an owner: " + boatId);
                return ResponseEntity.badRequest().build();
            }

            // Validate boat price
            double boatPrice = boat.getPrice() != null ? boat.getPrice() : 0.0;
            if (boatPrice <= 0) {
                System.err.println("Invalid boat price: " + boatPrice);
                return ResponseEntity.badRequest().build();
            }

            System.out.println("Assigning owner to boat:");
            System.out.println("  Boat ID: " + boatId);
            System.out.println("  Owner ID: " + ownerId);
            System.out.println("  Boat Price: " + boatPrice);
            System.out.println("  Number of Installments: " + numberOfInstallments);

            // Assign owner to boat
            boat.setOwner(owner);

            // Ensure balance is set
            if (boat.getBalance() == null) {
                boat.setBalance(0.0);
            }

            // Save boat first
            BoatEntity savedBoat = boatRepository.save(boat);
            System.out.println("Boat saved with owner");

            // Create payment records
            LocalDateTime currentDate = LocalDateTime.now();
            List<PaymentEntity> payments = new ArrayList<>();

            // Calculate amount per installment
            double amountPerInstallment = boatPrice / numberOfInstallments;

            for (int i = 0; i < numberOfInstallments; i++) {
                double currentInstallmentAmount = amountPerInstallment;

                // Last installment: adjust to the remaining amount to avoid rounding errors
                if (i == numberOfInstallments - 1) {
                    double totalPaid = (numberOfInstallments - 1) * amountPerInstallment;
                    currentInstallmentAmount = boatPrice - totalPaid;
                }

                // Calculate payment date based on number of installments
                LocalDateTime paymentDate;
                if (numberOfInstallments == 1) {
                    // Contado: pago inmediato
                    paymentDate = currentDate;
                } else if (numberOfInstallments <= 12) {
                    // 6 o 12 cuotas: pagos mensuales
                    // Para 6 cuotas: cada 2 meses, para 12 cuotas: cada mes
                    int monthsBetween = 12 / numberOfInstallments;
                    paymentDate = currentDate.plusMonths((long) i * monthsBetween);
                } else {
                    // 24, 48 o 72 cuotas: pagos en días
                    // Distribuir en aproximadamente 12 meses (365 días)
                    int daysBetween = 365 / numberOfInstallments;
                    paymentDate = currentDate.plusDays((long) i * daysBetween);
                }

                PaymentEntity payment = PaymentEntity.builder()
                        .mount(currentInstallmentAmount)
                        .date(paymentDate)
                        .reason(ReasonPayment.PAGO)
                        .status(PaymentStatus.POR_PAGAR)
                        .boat(savedBoat)
                        .build();

                payments.add(payment);

                System.out.println("  Payment " + (i + 1) + ": " + currentInstallmentAmount + " on " + paymentDate);
            }

            // Save all payments using the inherited saveAll method
            paymentRepository.saveAll(payments);
            System.out.println("All payments saved successfully");

            return ResponseEntity.ok(savedBoat);

        } catch (Exception e) {
            // Log the detailed error
            System.err.println("Error in assignOwner: " + e.getMessage());
            e.printStackTrace();

            // Return internal server error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{boatId}/owner/{ownerId}/manual")
    @Transactional
    public ResponseEntity<BoatEntity> assignOwnerManual(
            @PathVariable Long boatId,
            @PathVariable Long ownerId) {

        try {
            // Find boat
            Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
            if (boatOpt.isEmpty()) {
                System.err.println("Boat not found: " + boatId);
                return ResponseEntity.notFound().build();
            }

            // Find owner
            Optional<UserEntity> ownerOpt = userRepository.findById(ownerId);
            if (ownerOpt.isEmpty()) {
                System.err.println("Owner not found: " + ownerId);
                return ResponseEntity.notFound().build();
            }

            BoatEntity boat = boatOpt.get();
            UserEntity owner = ownerOpt.get();

            // Check if boat already has an owner
            if (boat.getOwner() != null) {
                System.err.println("Boat already has an owner: " + boatId);
                return ResponseEntity.badRequest().build();
            }

            System.out.println("Manually assigning owner to boat:");
            System.out.println("  Boat ID: " + boatId);
            System.out.println("  Owner ID: " + ownerId);

            // Assign owner to boat (without creating payments)
            boat.setOwner(owner);

            // Ensure balance is set
            if (boat.getBalance() == null) {
                boat.setBalance(0.0);
            }

            // Save boat
            BoatEntity savedBoat = boatRepository.save(boat);
            System.out.println("Boat saved with owner (manual assignment - no payments created)");

            return ResponseEntity.ok(savedBoat);

        } catch (Exception e) {
            // Log the detailed error
            System.err.println("Error in assignOwnerManual: " + e.getMessage());
            e.printStackTrace();

            // Return internal server error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
