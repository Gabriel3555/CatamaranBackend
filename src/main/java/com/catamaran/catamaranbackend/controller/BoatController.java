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

import java.io.IOException;
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

    @PutMapping("/{boatId}/owner/{ownerId}")
    public ResponseEntity<BoatEntity> assignOwner(
            @PathVariable Long boatId,
            @PathVariable Long ownerId,
            @RequestParam int months,
            @RequestParam Double monthlyPayment) {

        Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
        if (boatOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<UserEntity> ownerOpt = userRepository.findById(ownerId);
        if (ownerOpt.isEmpty()) {
            return ResponseEntity.badRequest().build(); // Owner not found
        }

        // Validar parámetros
        if (months < 1 || months > 12) {
            return ResponseEntity.badRequest().build(); // Months must be between 1 and 12
        }

        if (monthlyPayment <= 0) {
            return ResponseEntity.badRequest().build(); // Monthly payment must be positive
        }

        BoatEntity boat = boatOpt.get();
        boat.setOwner(ownerOpt.get());
        BoatEntity updatedBoat = boatRepository.save(boat);

        // Generar pagos mensuales
        LocalDateTime currentDate = LocalDateTime.now();

        for (int i = 0; i < months; i++) {
            PaymentEntity payment = PaymentEntity.builder()
                    .mount(monthlyPayment)
                    .date(currentDate.plusMonths(i))
                    .reason(ReasonPayment.PAGO)
                    .status(PaymentStatus.POR_PAGAR)
                    .boat(updatedBoat)
                    .build();

            paymentRepository.save(payment);
        }

        return ResponseEntity.ok(updatedBoat);
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
            String filePath = fileName;

            // Guardar archivo en la raíz del proyecto
            Path path = Paths.get(filePath);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Crear el documento
            BoatDocumentEntity document = BoatDocumentEntity.builder()
                    .name(documentName)
                    .url(filePath)
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
            Path filePath = Paths.get(document.getUrl());
            Files.deleteIfExists(filePath);

            boat.getDocuments().removeIf(doc -> doc.getId().equals(documentId));
            boatRepository.save(boat);

            boatDocumentRepository.deleteById(documentId);

            return ResponseEntity.noContent().build();

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
