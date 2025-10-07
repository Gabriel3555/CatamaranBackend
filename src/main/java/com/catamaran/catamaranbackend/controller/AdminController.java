package com.catamaran.catamaranbackend.controller;

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
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total boats
        long totalBoats = boatRepository.count();
        stats.put("totalBoats", totalBoats);

        // Active owners (users with role PROPIETARIO and status true)
        long activeOwners = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.PROPIETARIO && Boolean.TRUE.equals(user.getStatus()))
                .count();
        stats.put("activeOwners", activeOwners);

        // Pending maintenances (PROGRAMADO or EN_PROCESO)
        long pendingMaintenances = maintananceRepository.findAll().stream()
                .filter(m -> m.getStatus() == MaintananceStatus.PROGRAMADO || m.getStatus() == MaintananceStatus.EN_PROCESO)
                .count();
        stats.put("pendingMaintenances", pendingMaintenances);

        // Monthly payments (current month)
        YearMonth currentMonth = YearMonth.now();
        double monthlyPayments = paymentRepository.findAll().stream()
                .filter(p -> {
                    YearMonth paymentMonth = YearMonth.from(p.getDate());
                    return paymentMonth.equals(currentMonth);
                })
                .mapToDouble(p -> p.getMount() != null ? p.getMount() : 0.0)
                .sum();
        stats.put("monthlyPayments", monthlyPayments);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/charts/boats-by-type")
    public ResponseEntity<Map<String, Long>> getBoatsByType() {
        Map<BoatType, Long> typeCounts = boatRepository.findAll().stream()
                .filter(boat -> boat.getType() != null)
                .collect(Collectors.groupingBy(BoatEntity::getType, Collectors.counting()));

        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<BoatType, Long> entry : typeCounts.entrySet()) {
            result.put(entry.getKey().name(), entry.getValue());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/charts/maintenances-by-status")
    public ResponseEntity<Map<String, Long>> getMaintenancesByStatus() {
        Map<MaintananceStatus, Long> statusCounts = maintananceRepository.findAll().stream()
                .filter(m -> m.getStatus() != null)
                .collect(Collectors.groupingBy(MaintananceEntity::getStatus, Collectors.counting()));

        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<MaintananceStatus, Long> entry : statusCounts.entrySet()) {
            result.put(entry.getKey().name(), entry.getValue());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/charts/payments-by-month")
    public ResponseEntity<Map<String, Double>> getPaymentsByMonth() {
        // Get last 6 months
        Map<String, Double> monthlyPayments = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.from(now.minusMonths(i));
            String monthKey = month.getMonth().name().substring(0, 3) + " " + month.getYear();

            double sum = paymentRepository.findAll().stream()
                    .filter(p -> {
                        YearMonth paymentMonth = YearMonth.from(p.getDate());
                        return paymentMonth.equals(month);
                    })
                    .mapToDouble(p -> p.getMount() != null ? p.getMount() : 0.0)
                    .sum();

            monthlyPayments.put(monthKey, sum);
        }

        return ResponseEntity.ok(monthlyPayments);
    }

    @GetMapping("/maintenances")
    public ResponseEntity<List<MaintananceEntity>> getAllMaintenances() {
        return ResponseEntity.ok(maintananceRepository.findAll());
    }
}