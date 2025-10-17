package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.domain.*;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.MaintananceRepository;
import com.catamaran.catamaranbackend.repository.MaintananceSpecifications;
import com.catamaran.catamaranbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) MaintananceStatus status,
            @RequestParam(required = false) MaintananceType type) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Build specification for filtering
        Specification<MaintananceEntity> spec = null;

        if (search != null && !search.trim().isEmpty()) {
            spec = MaintananceSpecifications.hasSearchTerm(search);
        }

        if (status != null) {
            if (spec == null) {
                spec = MaintananceSpecifications.hasStatus(status);
            } else {
                spec = spec.and(MaintananceSpecifications.hasStatus(status));
            }
        }

        if (type != null) {
            if (spec == null) {
                spec = MaintananceSpecifications.hasType(type);
            } else {
                spec = spec.and(MaintananceSpecifications.hasType(type));
            }
        }

        Page<MaintananceEntity> maintenances;
        if (spec == null) {
            // If no filters are applied, use the original method
            maintenances = maintananceRepository.findAll(pageable);
        } else {
            maintenances = maintananceRepository.findAll(spec, pageable);
        }

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

        // Set default scheduled date to current date if not provided
        if (maintenance.getDateScheduled() == null) {
            maintenance.setDateScheduled(LocalDateTime.now());
        }

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
        Optional<MaintananceEntity> existingMaintenanceOpt = maintananceRepository.findById(id);
        if (existingMaintenanceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        MaintananceEntity existingMaintenance = existingMaintenanceOpt.get();

        // Preserve the boat relationship if not provided in the update
        if (maintenance.getBoat() == null) {
            maintenance.setBoat(existingMaintenance.getBoat());
        }

        // Preserve other existing relationships
        if (maintenance.getPayment() == null) {
            maintenance.setPayment(existingMaintenance.getPayment());
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

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getMaintenanceStatistics() {
        try {
            long totalMaintenances = maintananceRepository.count();
            long pendingMaintenances = maintananceRepository.countByStatus(MaintananceStatus.PROGRAMADO);
            long completedMaintenances = maintananceRepository.countByStatus(MaintananceStatus.COMPLETADO);
            Double totalCost = maintananceRepository.sumTotalCost();

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalMaintenances", totalMaintenances);
            statistics.put("pendingMaintenances", pendingMaintenances);
            statistics.put("completedMaintenances", completedMaintenances);
            statistics.put("totalCost", totalCost != null ? totalCost : 0.0);

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            // Log the error in a real application
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("totalMaintenances", 0);
            errorStats.put("pendingMaintenances", 0);
            errorStats.put("completedMaintenances", 0);
            errorStats.put("totalCost", 0.0);
            return ResponseEntity.ok(errorStats);
        }
    }
}