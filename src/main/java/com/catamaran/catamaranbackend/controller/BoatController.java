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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;

import java.io.IOException;
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

    @GetMapping("/{id}")
    public ResponseEntity<BoatEntity> getById(@PathVariable Long id) {
        return boatRepository.findById(id)
                .map(boat -> ResponseEntity.ok(boat))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<BoatEntity>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<BoatEntity> boats = boatRepository.findAll(pageable);
        return ResponseEntity.ok(boats);
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

        try {
            // Generar nombre único para el archivo
            String fileName = "boat_" + boatId + "_doc_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String staticDir = "src/main/resources/static/documents/";
            String filePath = staticDir + fileName;

            // Crear directorio si no existe
            Path directoryPath = Paths.get(staticDir);
            Files.createDirectories(directoryPath);

            // Guardar archivo en el directorio static
            Path path = Paths.get(filePath);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Crear el documento con URL relativa para acceso web
            String webUrl = "/documents/" + fileName;
            BoatDocumentEntity document = BoatDocumentEntity.builder()
                    .name(documentName)
                    .url(webUrl)
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
            // Convertir URL web a ruta del sistema de archivos
            String webUrl = document.getUrl();
            String fileName = webUrl.replace("/documents/", "");
            String filePath = "src/main/resources/static/documents/" + fileName;

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
            Path filePath = Paths.get("src/main/resources/static/documents/").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = "application/octet-stream";
                try {
                    contentType = Files.probeContentType(filePath);
                } catch (IOException e) {
                    // Use default content type
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{boatId}/owner/{ownerId}")
    public ResponseEntity<BoatEntity> assignOwner(
            @PathVariable Long boatId,
            @PathVariable Long ownerId,
            @RequestParam double installmentAmount,
            @RequestParam int frequency) {

        Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
        if (boatOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<UserEntity> ownerOpt = userRepository.findById(ownerId);
        if (ownerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BoatEntity boat = boatOpt.get();
        UserEntity owner = ownerOpt.get();

        // Check if boat already has an owner
        if (boat.getOwner() != null) {
            return ResponseEntity.badRequest().build();
        }

        // Validate parameters
        if (installmentAmount <= 0 || frequency < 1 || frequency > 11) {
            return ResponseEntity.badRequest().build();
        }

        // Calculate number of installments
        double boatPrice = boat.getPrice() != null ? boat.getPrice() : 0.0;
        int numberOfInstallments = (int) Math.ceil(boatPrice / installmentAmount);

        // Assign owner to boat
        boat.setOwner(owner);
        BoatEntity savedBoat = boatRepository.save(boat);

        // Create payment records
        LocalDateTime currentDate = LocalDateTime.now();
        for (int i = 0; i < numberOfInstallments; i++) {
            PaymentEntity payment = PaymentEntity.builder()
                    .mount(installmentAmount)
                    .date(currentDate.plusMonths(i * frequency))
                    .reason(ReasonPayment.PAGO)
                    .status(PaymentStatus.POR_PAGAR)
                    .boat(savedBoat)
                    .build();
            paymentRepository.save(payment);
        }

        return ResponseEntity.ok(savedBoat);
    }
}
