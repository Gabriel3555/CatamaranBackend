package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.domain.PaymentEntity;
import com.catamaran.catamaranbackend.domain.PaymentStatus;
import com.catamaran.catamaranbackend.domain.ReasonPayment;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.catamaran.catamaranbackend.domain.BoatEntity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import java.util.List;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final BoatRepository boatRepository;

    @PostMapping("/{boatId}")
    public ResponseEntity<PaymentEntity> createPayment(
            @PathVariable Long boatId,
            @RequestBody PaymentEntity request
    ) {
        Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
        if (boatOpt.isPresent()) {
            BoatEntity boat = boatOpt.get();
            request.setUser(boat.getOwner());
            request.setBoat(boat);
            request.setDate(LocalDateTime.now());
            request.setStatus(PaymentStatus.POR_PAGAR);

            PaymentEntity saved = paymentRepository.save(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } else {
            return ResponseEntity.<PaymentEntity>status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping(value = "/{id}/attach-receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PaymentEntity> attachReceipt(
            @PathVariable Long id,
            @RequestParam("receipt") MultipartFile receipt
    ) {
        Optional<PaymentEntity> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isPresent()) {
            PaymentEntity payment = paymentOpt.get();
            if (payment.getInvoice_url() != null) {
                return ResponseEntity.badRequest().build();
            }
            try {
                if (receipt != null && !receipt.isEmpty()) {
                    String originalFilename = receipt.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String filename = UUID.randomUUID().toString() + extension;
                    Path receiptsDir = Paths.get("receipts");
                    Files.createDirectories(receiptsDir);
                    Path filePath = receiptsDir.resolve(filename);
                    Files.write(filePath, receipt.getBytes());
                    payment.setInvoice_url("receipts/" + filename);
                } else {
                    return ResponseEntity.badRequest().build(); // No file provided
                }
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            payment.setStatus(PaymentStatus.PAGADO);

            // Deduct the amount now that receipt is attached
            if (payment.getReason() == ReasonPayment.PAGO) {
                BoatEntity boat = payment.getBoat();
                boat.setBalance(boat.getBalance() - payment.getMount());
                boatRepository.save(boat);
            }

            PaymentEntity saved = paymentRepository.save(payment);
            return ResponseEntity.ok(saved);
        } else {
            return ResponseEntity.<PaymentEntity>status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<PaymentEntity>> getPaymentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentRepository.findByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<List<PaymentEntity>> getAllPayments() {
        return ResponseEntity.ok(paymentRepository.findAll());
    }
}
