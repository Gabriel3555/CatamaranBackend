package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.domain.PaymentEntity;
import com.catamaran.catamaranbackend.domain.PaymentStatus;
import com.catamaran.catamaranbackend.domain.ReasonPayment;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;

import com.catamaran.catamaranbackend.domain.BoatEntity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final BoatRepository boatRepository;

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

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending());

        Page<PaymentEntity> payments;

        // Apply filters if provided
        if (search != null && !search.trim().isEmpty()) {
            // Search in owner name, email, or invoice_url
            payments = paymentRepository.findBySearchTerm(search.trim(), pageable);
        } else if (reason != null && !reason.equals("all")) {
            ReasonPayment reasonEnum = ReasonPayment.valueOf(reason.toUpperCase());
            payments = paymentRepository.findByReason(reasonEnum, pageable);
        } else if (status != null && !status.equals("all")) {
            PaymentStatus statusEnum = PaymentStatus.valueOf(status.toUpperCase());
            payments = paymentRepository.findByStatus(statusEnum, pageable);
        } else if (month != null && !month.equals("all")) {
            // Filter by month
            LocalDateTime startDate;
            LocalDateTime endDate = LocalDateTime.now();

            switch (month) {
                case "current":
                    startDate = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                    break;
                case "last3":
                    startDate = LocalDateTime.now().minusMonths(3);
                    break;
                case "last6":
                    startDate = LocalDateTime.now().minusMonths(6);
                    break;
                default:
                    startDate = LocalDateTime.now().minusYears(1); // fallback
            }
            payments = paymentRepository.findByDateBetween(startDate, endDate, pageable);
        } else {
            payments = paymentRepository.findAll(pageable);
        }

        return ResponseEntity.ok(payments);
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
    public ResponseEntity<PaymentEntity> addReceipt(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        Optional<PaymentEntity> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            String fileName = "receipt_" + id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = "receipts/" + fileName;

            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent()); // Ensure receipts directory exists
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            PaymentEntity payment = paymentOpt.get();
            payment.setInvoice_url(filePath);
            payment.setStatus(PaymentStatus.PAGADO);

            if (payment.getReason() == ReasonPayment.PAGO) {
                BoatEntity boat = payment.getBoat();
                boat.setBalance(boat.getBalance() + payment.getMount());
                boatRepository.save(boat);
            }

            PaymentEntity updatedPayment = paymentRepository.save(payment);
            return ResponseEntity.ok(updatedPayment);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/download-receipt")
    public ResponseEntity<Resource> downloadReceipt(@PathVariable Long id) {
        Optional<PaymentEntity> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isEmpty() || paymentOpt.get().getInvoice_url() == null) {
            return ResponseEntity.notFound().build();
        }

        PaymentEntity payment = paymentOpt.get();
        Path filePath = Paths.get(payment.getInvoice_url());
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Determine content type
        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(filePath);
        } catch (IOException e) {
            // Use default
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Content-Disposition", "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
                .body(resource);
    }
}