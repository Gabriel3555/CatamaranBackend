package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.domain.*;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.MaintananceRepository;
import com.catamaran.catamaranbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/maintenances")
@RequiredArgsConstructor
public class MaintananceController {

    private final MaintananceRepository maintananceRepository;
    private final BoatRepository boatRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping("/{id}")
    public ResponseEntity<MaintananceEntity> getById(@PathVariable Long id) {
        return maintananceRepository.findById(id)
                .map(maintenance -> ResponseEntity.ok(maintenance))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<MaintananceEntity>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<MaintananceEntity> maintenances = maintananceRepository.findAll(pageable);
        return ResponseEntity.ok(maintenances);
    }

    @GetMapping("/boat/{boatId}")
    public ResponseEntity<Page<MaintananceEntity>> getByBoatId(
            @PathVariable Long boatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<MaintananceEntity> maintenances = maintananceRepository.findByBoatId(boatId, pageable);
        return ResponseEntity.ok(maintenances);
    }

    @PostMapping("/boat/{boatId}")
    public ResponseEntity<MaintananceEntity> createMaintenanceForBoat(
            @PathVariable Long boatId,
            @RequestBody MaintananceEntity maintenance) {

        Optional<BoatEntity> boatOpt = boatRepository.findById(boatId);
        if (boatOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        maintenance.setBoat(boatOpt.get());
        MaintananceEntity savedMaintenance = maintananceRepository.save(maintenance);

        // Crear el pago automáticamente
        PaymentEntity payment = PaymentEntity.builder()
                .mount(maintenance.getCost())
                .date(LocalDateTime.now())
                .reason(ReasonPayment.MANTENIMIENTO)
                .status(PaymentStatus.POR_PAGAR)
                .maintanance(savedMaintenance)
                .boat(savedMaintenance.getBoat())
                .build();

        PaymentEntity savedPayment = paymentRepository.save(payment);
        savedMaintenance.setPayment(savedPayment);
        savedMaintenance = maintananceRepository.save(savedMaintenance);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedMaintenance);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintananceEntity> updateMaintenance(@PathVariable Long id, @RequestBody MaintananceEntity maintenance) {
        if (!maintananceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        maintenance.setId(id);
        MaintananceEntity updatedMaintenance = maintananceRepository.save(maintenance);
        return ResponseEntity.ok(updatedMaintenance);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaintenanceById(@PathVariable Long id) {
        if (!maintananceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        maintananceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}