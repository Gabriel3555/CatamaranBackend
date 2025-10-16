package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.domain.*;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final BoatRepository boatRepository;

    @Value("${file.upload-dir:uploads/receipts}")
    private String uploadDir;

    @GetMapping("/{id}")
    public ResponseEntity<PaymentEntity> getById(@PathVariable Long id) {
        return paymentRepository.findById(id)
                .map(payment -> ResponseEntity.ok(payment))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<PaymentEntity>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String status) {

        System.out.println("PaymentController.getAll called with filters:");
        System.out.println("  page: " + page + ", size: " + size);
        System.out.println("  search: " + search);
        System.out.println("  reason: " + reason);
        System.out.println("  month: " + month);
        System.out.println("  status: " + status);

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending());

        // Build dynamic query with multiple filters using Specifications
        org.springframework.data.jpa.domain.Specification<PaymentEntity> spec = Specification.where(null);

        // Apply search filter if provided
        if (search != null && !search.trim().isEmpty()) {
            System.out.println("Applying search filter: " + search.trim());
            spec = spec.and(PaymentSpecification.hasSearchTerm(search.trim()));
        }

        // Apply reason filter if provided
        if (reason != null && !reason.equals("all")) {
            System.out.println("Applying reason filter: " + reason);
            ReasonPayment reasonEnum = ReasonPayment.valueOf(reason.toUpperCase());
            spec = spec.and(PaymentSpecification.hasReason(reasonEnum));
        }

        // Apply status filter if provided
        if (status != null && !status.equals("all")) {
            System.out.println("Applying status filter: " + status);
            PaymentStatus statusEnum = PaymentStatus.valueOf(status.toUpperCase());
            spec = spec.and(PaymentSpecification.hasStatus(statusEnum));
        }

        // Apply month filter if provided
        if (month != null && !month.equals("all")) {
            System.out.println("Applying month filter: " + month);

            // Month filter options:
            // - "current": Current month (from 1st day to last day of current month)
            // - "last3": Last 3 months (from 1st day of month, 3 months ago to now)
            // - "last6": Last 6 months (from 1st day of month, 6 months ago to now)
            LocalDateTime startDate;
            LocalDateTime endDate;

            switch (month) {
                case "current":
                    // Current month: from first day of current month to last day of current month
                    LocalDateTime now = LocalDateTime.now();
                    startDate = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                    endDate = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                                  .withHour(23).withMinute(59).withSecond(59);
                    System.out.println("Current month filter - startDate: " + startDate + ", endDate: " + endDate);
                    break;
                case "last3":
                    // Last 3 months: from 1st day of month, 3 months ago to now
                    LocalDateTime nowForLast3 = LocalDateTime.now();
                    startDate = nowForLast3.withDayOfMonth(1).minusMonths(3).withHour(0).withMinute(0).withSecond(0);
                    endDate = nowForLast3.withHour(23).withMinute(59).withSecond(59);
                    System.out.println("Last 3 months filter - startDate: " + startDate + ", endDate: " + endDate);
                    break;
                case "last6":
                    // Last 6 months: from 1st day of month, 6 months ago to now
                    LocalDateTime nowForLast6 = LocalDateTime.now();
                    startDate = nowForLast6.withDayOfMonth(1).minusMonths(6).withHour(0).withMinute(0).withSecond(0);
                    endDate = nowForLast6.withHour(23).withMinute(59).withSecond(59);
                    System.out.println("Last 6 months filter - startDate: " + startDate + ", endDate: " + endDate);
                    break;
                default:
                    // Default to last year
                    endDate = LocalDateTime.now();
                    startDate = endDate.minusYears(1);
            }
            spec = spec.and(PaymentSpecification.isBetweenDates(startDate, endDate));
            System.out.println("Date filter applied");
        }

        // Execute query with combined specifications
        Page<PaymentEntity> payments = paymentRepository.findAll(spec, pageable);
        System.out.println("Combined filters returned " + payments.getTotalElements() + " payments");

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics() {
        // Obtener estadísticas generales sin filtros ni paginación
        List<PaymentEntity> allPayments = paymentRepository.findAll();

        // Calcular estadísticas generales
        long totalPayments = allPayments.size();
        double totalAmount = allPayments.stream()
                .mapToDouble(payment -> payment.getMount() != null ? payment.getMount() : 0.0)
                .sum();

        // Calcular pagos del mes actual
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                                      .withHour(23).withMinute(59).withSecond(59);

        double monthlyAmount = allPayments.stream()
                .filter(payment -> {
                    LocalDateTime paymentDate = payment.getDate();
                    return paymentDate != null &&
                           (paymentDate.isEqual(startOfMonth) || paymentDate.isAfter(startOfMonth)) &&
                           (paymentDate.isEqual(endOfMonth) || paymentDate.isBefore(endOfMonth));
                })
                .mapToDouble(payment -> payment.getMount() != null ? payment.getMount() : 0.0)
                .sum();

        // Calcular pagadores activos (propietarios únicos con pagos)
        long activePayers = allPayments.stream()
                .filter(payment -> payment.getBoat() != null && payment.getBoat().getOwner() != null)
                .map(payment -> payment.getBoat().getOwner().getId())
                .distinct()
                .count();

        // Crear respuesta con estadísticas
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalPayments", totalPayments);
        statistics.put("totalAmount", totalAmount);
        statistics.put("monthlyAmount", monthlyAmount);
        statistics.put("activePayers", activePayers);

        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/boat/{boatId}")
    public ResponseEntity<Page<PaymentEntity>> getByBoatId(
            @PathVariable Long boatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending());
        Page<PaymentEntity> payments = paymentRepository.findByBoatId(boatId, pageable);
        return ResponseEntity.ok(payments);
    }

    @PostMapping
    public ResponseEntity<PaymentEntity> createPayment(@RequestBody PaymentEntity payment) {
        // If boatId is provided but boat is null, resolve the boat entity
        if (payment.getBoat() == null && payment.getBoatId() != null) {
            Optional<BoatEntity> boatOpt = boatRepository.findById(payment.getBoatId());
            if (boatOpt.isPresent()) {
                payment.setBoat(boatOpt.get());
            } else {
                return ResponseEntity.badRequest().build();
            }
        }

        PaymentEntity savedPayment = paymentRepository.save(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPayment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentEntity> updatePayment(@PathVariable Long id, @RequestBody PaymentEntity payment) {
        if (!paymentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        payment.setId(id);
        PaymentEntity updatedPayment = paymentRepository.save(payment);
        return ResponseEntity.ok(updatedPayment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentById(@PathVariable Long id) {
        if (!paymentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        paymentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/receipt")
    public ResponseEntity<?> addReceipt(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        Optional<PaymentEntity> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No se ha proporcionado ningún archivo");
        }

        try {
            // Crear el directorio si no existe
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Generar nombre único para el archivo
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = "receipt_" + id + "" + System.currentTimeMillis() + "" + originalFilename;

            // Ruta completa del archivo
            Path filePath = uploadPath.resolve(fileName);

            // Guardar el archivo
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Actualizar el pago
            PaymentEntity payment = paymentOpt.get();
            payment.setInvoice_url(fileName);
            payment.setStatus(PaymentStatus.PAGADO);

            if (payment.getReason() == ReasonPayment.PAGO) {
                BoatEntity boat = payment.getBoat();
                if (boat != null) {
                    boat.setBalance(boat.getBalance() + payment.getMount());
                    boatRepository.save(boat);
                }
            }

            PaymentEntity updatedPayment = paymentRepository.save(payment);
            return ResponseEntity.ok(updatedPayment);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar el archivo: " + e.getMessage());
        }
    }


    @GetMapping("/{id}/download-receipt")
    public ResponseEntity<Resource> downloadReceipt(@PathVariable Long id) {
        Optional<PaymentEntity> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isEmpty() || paymentOpt.get().getInvoice_url() == null) {
            return ResponseEntity.notFound().build();
        }

        PaymentEntity payment = paymentOpt.get();

        try {
            // Construir la ruta completa del archivo
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(payment.getInvoice_url()).normalize();

            // Verificar que el archivo está dentro del directorio permitido (seguridad)
            if (!filePath.startsWith(uploadPath)) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Determinar content type
            String contentType = "application/octet-stream";
            try {
                contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            } catch (IOException e) {
                // Usar default
            }

            // Obtener el nombre original del archivo desde la URL o usar el nombre del archivo
            String filename = payment.getInvoice_url();
            if (filename.contains("_")) {
                // Extraer el nombre original si está en el formato "receipt_ID_timestamp_originalname"
                String[] parts = filename.split("_", 4);
                if (parts.length >= 4) {
                    filename = parts[3];
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}