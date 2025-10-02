package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.catamaran.catamaranbackend.domain.PaymentEntity;
import com.catamaran.catamaranbackend.domain.ReasonPayment;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final BoatRepository boatRepository;
    private final UserRepositoryJpa userRepository;

    @PostMapping("/{boatId}/{userId}")
    public ResponseEntity<PaymentEntity> createPayment(
            @PathVariable Long boatId,
            @PathVariable Long userId,
            @RequestBody PaymentEntity request
    ) {
        return boatRepository.findById(boatId).flatMap(boat ->
                userRepository.findById(userId).map(user -> {
                    request.setUser(user);
                    request.setDate(LocalDateTime.now());

                    // Descontar del balance del barco
                    if (request.getReason() == ReasonPayment.COUTA || request.getReason() == ReasonPayment.MANTENIMIENTO) {
                        boat.setBalance(boat.getBalance() - request.getMount());
                        boatRepository.save(boat);
                    }

                    PaymentEntity saved = paymentRepository.save(request);
                    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
                })
        ).orElse(ResponseEntity.notFound().build());
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
