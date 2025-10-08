package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.catamaran.catamaranbackend.domain.*;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.MaintananceRepository;
import com.catamaran.catamaranbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BoatRepository boatRepository;
    private final UserRepositoryJpa userRepository;
    private final MaintananceRepository maintananceRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total boats
        stats.put("totalBoats", boatRepository.count());

        // Active owners (only users with PROPIETARIO role)
        List<UserEntity> users = userRepository.findAll();
        long activeOwners = users.stream()
                .filter(user -> user.getRole() == Role.PROPIETARIO)
                .count();
        stats.put("activeOwners", activeOwners);

        // Pending maintenances (PROGRAMADO or EN_PROCESO)
        List<MaintananceEntity> maintenances = maintananceRepository.findAll();
        long pendingMaintenances = maintenances.stream()
                .filter(m -> m.getStatus() == MaintananceStatus.PROGRAMADO ||
                           m.getStatus() == MaintananceStatus.EN_PROCESO)
                .count();
        stats.put("pendingMaintenances", pendingMaintenances);

        // Monthly payments (current month, status PAGADO)
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        List<PaymentEntity> payments = paymentRepository.findAll();
        double monthlyPayments = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAGADO &&
                           p.getDate().isAfter(startOfMonth) &&
                           p.getDate().isBefore(endOfMonth))
                .mapToDouble(p -> p.getMount() != null ? p.getMount() : 0.0)
                .sum();
        stats.put("monthlyPayments", monthlyPayments);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/charts/boats-by-type")
    public ResponseEntity<Map<String, Long>> getBoatsByType() {
        List<BoatEntity> boats = boatRepository.findAll();

        Map<String, Long> boatsByType = boats.stream()
                .collect(Collectors.groupingBy(
                        boat -> boat.getType() != null ? boat.getType().name() : "UNKNOWN",
                        Collectors.counting()
                ));

        return ResponseEntity.ok(boatsByType);
    }

    @GetMapping("/charts/maintenances-by-status")
    public ResponseEntity<Map<String, Long>> getMaintenancesByStatus() {
        List<MaintananceEntity> maintenances = maintananceRepository.findAll();

        Map<String, Long> maintenancesByStatus = maintenances.stream()
                .collect(Collectors.groupingBy(
                        maintenance -> maintenance.getStatus() != null ? maintenance.getStatus().name() : "UNKNOWN",
                        Collectors.counting()
                ));

        return ResponseEntity.ok(maintenancesByStatus);
    }
}