package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.catamaran.catamaranbackend.domain.*;
import com.catamaran.catamaranbackend.repository.BoatDocumentRepository;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.MaintananceRepository;
import com.catamaran.catamaranbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final UserRepositoryJpa userRepository;
    private final BoatRepository boatRepository;
    private final MaintananceRepository maintananceRepository;
    private final PaymentRepository paymentRepository;
    private final BoatDocumentRepository boatDocumentRepository;

    @Value("${app.upload.dir:src/main/resources/static/documents/}")
    private String uploadDir;

    /**
     * Valida que el usuario autenticado tenga permisos de propietario
     * @return Usuario autenticado si tiene permisos de propietario
     * @throws SecurityException si el usuario no tiene permisos
     */
    private UserEntity validateOwnerAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Usuario no autenticado");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof com.catamaran.catamaranbackend.auth.application.dto.UserPrincipal)) {
            throw new SecurityException("Tipo de autenticación inválido");
        }

        com.catamaran.catamaranbackend.auth.application.dto.UserPrincipal userPrincipal =
            (com.catamaran.catamaranbackend.auth.application.dto.UserPrincipal) principal;

        UserEntity user = userRepository.findById(userPrincipal.id()).orElse(null);
        if (user == null) {
            throw new SecurityException("Usuario no encontrado");
        }

        if (user.getRole() != Role.PROPIETARIO) {
            throw new SecurityException("Acceso denegado. Se requieren permisos de propietario.");
        }

        if (!user.getStatus()) {
            throw new SecurityException("Usuario inactivo. Contacta al administrador.");
        }

        return user;
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<Map<String, Object>> getOwnerDashboard(@PathVariable Long userId) {
        // Validate that the authenticated user is the owner and matches the requested userId
        UserEntity authenticatedUser;
        try {
            authenticatedUser = validateOwnerAccess();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }

        // Ensure the authenticated user can only access their own data
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "No tienes permisos para acceder a estos datos"));
        }

        UserEntity user = authenticatedUser;

        Map<String, Object> response = new HashMap<>();

        // User info
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("fullName", user.getFullName() != null ? user.getFullName() : user.getUsername());
        response.put("user", userInfo);

        // Get owner's boats
        List<BoatEntity> ownerBoats = boatRepository.findByOwner(user);

        // Metrics
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalBoats", ownerBoats.size());

        // Count documents across all boats
        long totalDocuments = ownerBoats.stream()
                .mapToLong(boat -> boat.getDocuments() != null ? boat.getDocuments().size() : 0)
                .sum();
        metrics.put("totalDocuments", totalDocuments);

        // Get all maintenances for owner's boats
        List<MaintananceEntity> allMaintenances = ownerBoats.stream()
                .flatMap(boat -> boat.getMaintanances().stream())
                .collect(Collectors.toList());

        // Pending maintenances (PROGRAMADO or EN_PROCESO)
        long pendingMaintenances = allMaintenances.stream()
                .filter(m -> m.getStatus() == MaintananceStatus.PROGRAMADO ||
                           m.getStatus() == MaintananceStatus.EN_PROCESO)
                .count();
        metrics.put("pendingMaintenances", pendingMaintenances);

        // Completed maintenances
        long completedMaintenances = allMaintenances.stream()
                .filter(m -> m.getStatus() == MaintananceStatus.COMPLETADO)
                .count();
        metrics.put("completedMaintenances", completedMaintenances);

        // Get all payments for owner's boats
        List<PaymentEntity> allPayments = ownerBoats.stream()
                .flatMap(boat -> boat.getPayments().stream())
                .collect(Collectors.toList());

        // Pending payments (POR_PAGAR)
        long pendingPaymentsCount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.POR_PAGAR)
                .count();
        metrics.put("pendingPayments", pendingPaymentsCount);

        // Total pending payments amount
        double totalPendingAmount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.POR_PAGAR)
                .mapToDouble(PaymentEntity::getMount)
                .sum();
        metrics.put("totalPendingAmount", totalPendingAmount);

        response.put("metrics", metrics);

        // Boats data
        List<Map<String, Object>> boatsData = ownerBoats.stream()
                .map(boat -> {
                    Map<String, Object> boatData = new HashMap<>();
                    boatData.put("id", boat.getId());
                    boatData.put("name", boat.getName());
                    boatData.put("model", boat.getModel());
                    boatData.put("type", boat.getType() != null ? boat.getType().name() : null);
                    boatData.put("location", boat.getLocation());
                    boatData.put("price", boat.getPrice());
                    boatData.put("balance", boat.getBalance());

                    // Calculate maintenance debt
                    double maintenanceDebt = boat.getMaintanances().stream()
                            .filter(m -> m.getCost() != null)
                            .filter(m -> m.getPayment() == null || m.getPayment().getStatus() != PaymentStatus.PAGADO)
                            .mapToDouble(MaintananceEntity::getCost)
                            .sum();
                    boatData.put("maintenanceDebt", maintenanceDebt);

                    // Calculate boat debt (unpaid boat payments)
                    double boatDebt = boat.getPayments().stream()
                            .filter(p -> p.getReason() == ReasonPayment.PAGO)
                            .filter(p -> p.getStatus() == PaymentStatus.POR_PAGAR)
                            .mapToDouble(PaymentEntity::getMount)
                            .sum();
                    boatData.put("boatDebt", boatDebt);

                    return boatData;
                })
                .collect(Collectors.toList());
        response.put("boats", boatsData);

        // Upcoming maintenances (next 30 days)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysFromNow = now.plusDays(30);

        List<Map<String, Object>> upcomingMaintenances = allMaintenances.stream()
                .filter(m -> m.getDateScheduled() != null &&
                           m.getDateScheduled().isAfter(now) &&
                           m.getDateScheduled().isBefore(thirtyDaysFromNow) &&
                           (m.getStatus() == MaintananceStatus.PROGRAMADO || m.getStatus() == MaintananceStatus.EN_PROCESO))
                .sorted((m1, m2) -> m1.getDateScheduled().compareTo(m2.getDateScheduled()))
                .limit(5)
                .map(m -> {
                    Map<String, Object> maintData = new HashMap<>();
                    maintData.put("id", m.getId());
                    maintData.put("boatName", m.getBoat().getName());
                    maintData.put("description", m.getDescription());
                    maintData.put("scheduledDate", m.getDateScheduled().toString());
                    maintData.put("priority", m.getPriority() != null ? m.getPriority().name() : null);
                    maintData.put("type", m.getType() != null ? m.getType().name() : null);
                    maintData.put("status", m.getStatus() != null ? m.getStatus().name() : null);
                    return maintData;
                })
                .collect(Collectors.toList());
        response.put("upcomingMaintenances", upcomingMaintenances);

        // All maintenances for owner's boats
        List<Map<String, Object>> allMaintenancesData = allMaintenances.stream()
                .sorted((m1, m2) -> {
                    LocalDateTime date1 = m1.getDatePerformed() != null ? m1.getDatePerformed() : m1.getDateScheduled();
                    LocalDateTime date2 = m2.getDatePerformed() != null ? m2.getDatePerformed() : m2.getDateScheduled();
                    if (date1 == null && date2 == null) return 0;
                    if (date1 == null) return 1;
                    if (date2 == null) return -1;
                    return date2.compareTo(date1); // Most recent first
                })
                .map(m -> {
                    Map<String, Object> maintData = new HashMap<>();
                    maintData.put("id", m.getId());
                    maintData.put("boatName", m.getBoat().getName());
                    maintData.put("description", m.getDescription());
                    maintData.put("scheduledDate", m.getDateScheduled() != null ? m.getDateScheduled().toString() : null);
                    maintData.put("performedDate", m.getDatePerformed() != null ? m.getDatePerformed().toString() : null);
                    maintData.put("priority", m.getPriority() != null ? m.getPriority().name() : null);
                    maintData.put("type", m.getType() != null ? m.getType().name() : null);
                    maintData.put("status", m.getStatus() != null ? m.getStatus().name() : null);
                    maintData.put("cost", m.getCost());
                    return maintData;
                })
                .collect(Collectors.toList());
        response.put("allMaintenances", allMaintenancesData);

        // Pending payments for owner's boats
        List<Map<String, Object>> pendingPaymentsData = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.POR_PAGAR)
                .sorted((p1, p2) -> p2.getDate().compareTo(p1.getDate())) // Most recent first
                .map(payment -> {
                    Map<String, Object> paymentData = new HashMap<>();
                    paymentData.put("id", payment.getId());
                    paymentData.put("boatName", payment.getBoat().getName());
                    paymentData.put("amount", payment.getMount());
                    paymentData.put("date", payment.getDate().toString());
                    paymentData.put("reason", payment.getReason() != null ? payment.getReason().name() : null);
                    paymentData.put("status", payment.getStatus() != null ? payment.getStatus().name() : null);
                    paymentData.put("invoiceUrl", payment.getInvoice_url());
                    return paymentData;
                })
                .collect(Collectors.toList());
        response.put("pendingPayments", pendingPaymentsData);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/boats/{userId}")
    public ResponseEntity<Map<String, Object>> getOwnerBoats(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Validate that the authenticated user is the owner and matches the requested userId
        UserEntity authenticatedUser;
        try {
            authenticatedUser = validateOwnerAccess();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }

        // Ensure the authenticated user can only access their own data
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "No tienes permisos para acceder a estos datos"));
        }

        UserEntity user = authenticatedUser;

        // Create pageable
        Pageable pageable = PageRequest.of(page, size);

        // Get owner's boats with pagination
        Page<BoatEntity> boatsPage = boatRepository.findByOwner(user, pageable);

        // Convert to response format
        Map<String, Object> response = new HashMap<>();
        response.put("content", boatsPage.getContent().stream().map(boat -> {
            Map<String, Object> boatData = new HashMap<>();
            boatData.put("id", boat.getId());
            boatData.put("name", boat.getName());
            boatData.put("model", boat.getModel());
            boatData.put("type", boat.getType() != null ? boat.getType().name() : null);
            boatData.put("location", boat.getLocation());
            boatData.put("price", boat.getPrice());
            boatData.put("balance", boat.getBalance());

            // Calculate maintenance debt
            double maintenanceDebt = boat.getMaintanances().stream()
                    .filter(m -> m.getCost() != null)
                    .filter(m -> m.getPayment() == null || m.getPayment().getStatus() != PaymentStatus.PAGADO)
                    .mapToDouble(MaintananceEntity::getCost)
                    .sum();
            boatData.put("maintenanceDebt", maintenanceDebt);

            // Calculate boat debt (unpaid boat payments)
            double boatDebt = boat.getPayments().stream()
                    .filter(p -> p.getReason() == ReasonPayment.PAGO)
                    .filter(p -> p.getStatus() == PaymentStatus.POR_PAGAR)
                    .mapToDouble(PaymentEntity::getMount)
                    .sum();
            boatData.put("boatDebt", boatDebt);

            return boatData;
        }).collect(Collectors.toList()));

        response.put("totalPages", boatsPage.getTotalPages());
        response.put("totalElements", boatsPage.getTotalElements());
        response.put("size", boatsPage.getSize());
        response.put("number", boatsPage.getNumber());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getOwnerPayments(@PathVariable Long userId) {
        // Validate that the authenticated user is the owner and matches the requested userId
        UserEntity authenticatedUser;
        try {
            authenticatedUser = validateOwnerAccess();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(List.of(Map.of("message", e.getMessage())));
        }

        // Ensure the authenticated user can only access their own data
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(List.of(Map.of("message", "No tienes permisos para acceder a estos datos")));
        }

        UserEntity user = authenticatedUser;

        // Get owner's boats
        List<BoatEntity> ownerBoats = boatRepository.findByOwner(user);

        // Get all payments for owner's boats
        List<PaymentEntity> allPayments = ownerBoats.stream()
                .flatMap(boat -> boat.getPayments().stream())
                .sorted((p1, p2) -> p2.getDate().compareTo(p1.getDate())) // Most recent first
                .collect(Collectors.toList());

        // Convert to response format
        List<Map<String, Object>> paymentsData = allPayments.stream()
                .map(payment -> {
                    Map<String, Object> paymentData = new HashMap<>();
                    paymentData.put("id", payment.getId());
                    paymentData.put("boatName", payment.getBoat().getName());
                    paymentData.put("amount", payment.getMount());
                    paymentData.put("date", payment.getDate().toString());
                    paymentData.put("reason", payment.getReason() != null ? payment.getReason().name() : null);
                    paymentData.put("status", payment.getStatus() != null ? payment.getStatus().name() : null);
                    paymentData.put("invoiceUrl", payment.getInvoice_url());
                    return paymentData;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(paymentsData);
    }

    @GetMapping("/boats/{boatId}/documents")
    public ResponseEntity<List<BoatDocumentEntity>> getBoatDocuments(@PathVariable Long boatId) {
        // Validate that the authenticated user is the owner
        UserEntity authenticatedUser;
        try {
            authenticatedUser = validateOwnerAccess();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
        if (boatOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BoatEntity boat = boatOpt.get();

        // Validate that the boat belongs to the authenticated owner
        if (!boat.getOwner().getId().equals(authenticatedUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ArrayList<>());
        }

        List<BoatDocumentEntity> documents = boat.getDocuments();
        return ResponseEntity.ok(documents != null ? documents : new ArrayList<>());
    }

    @PostMapping("/boats/{boatId}/documents")
    public ResponseEntity<BoatDocumentEntity> addDocumentToBoat(
            @PathVariable Long boatId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String documentName) {

        // Validate that the authenticated user is the owner
        UserEntity authenticatedUser;
        try {
            authenticatedUser = validateOwnerAccess();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
        if (boatOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BoatEntity boat = boatOpt.get();

        // Validate that the boat belongs to the authenticated owner
        if (!boat.getOwner().getId().equals(authenticatedUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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

            // Crear el documento con URL para descarga
            String webUrl = "/documents/" + fileName;
            BoatDocumentEntity document = BoatDocumentEntity.builder()
                    .name(documentName)
                    .url(webUrl)
                    .build();

            BoatDocumentEntity savedDocument = boatDocumentRepository.save(document);

            // Agregar el documento al bote
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

    @PutMapping("/boats/{boatId}/documents/{documentId}")
    public ResponseEntity<BoatDocumentEntity> updateDocument(
            @PathVariable Long boatId,
            @PathVariable Long documentId,
            @RequestParam("name") String documentName) {

        // Validate that the authenticated user is the owner
        UserEntity authenticatedUser;
        try {
            authenticatedUser = validateOwnerAccess();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
        if (boatOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BoatEntity boat = boatOpt.get();

        // Validate that the boat belongs to the authenticated owner
        if (!boat.getOwner().getId().equals(authenticatedUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<BoatDocumentEntity> documentOpt = boatDocumentRepository.findById(documentId);
        if (documentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verificar que el documento pertenece al bote
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

    @DeleteMapping("/boats/{boatId}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long boatId,
            @PathVariable Long documentId) {

        // Validate that the authenticated user is the owner
        UserEntity authenticatedUser;
        try {
            authenticatedUser = validateOwnerAccess();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
        if (boatOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BoatEntity boat = boatOpt.get();

        // Validate that the boat belongs to the authenticated owner
        if (!boat.getOwner().getId().equals(authenticatedUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<BoatDocumentEntity> documentOpt = boatDocumentRepository.findById(documentId);
        if (documentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

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

}