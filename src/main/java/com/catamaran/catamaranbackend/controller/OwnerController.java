package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.catamaran.catamaranbackend.domain.*;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.MaintananceRepository;
import com.catamaran.catamaranbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final UserRepositoryJpa userRepository;
    private final BoatRepository boatRepository;
    private final MaintananceRepository maintananceRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<Map<String, Object>> getOwnerDashboard(@PathVariable Long userId) {
        Map<String, Object> dashboard = new HashMap<>();

        // Get user info
        var userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var user = userOptional.get();

        // Get owner's boats
        List<BoatEntity> ownerBoats = boatRepository.findAll().stream()
                .filter(boat -> boat.getOwner() != null && boat.getOwner().getId().equals(userId))
                .collect(Collectors.toList());

        // Get maintenances for owner's boats
        List<MaintananceEntity> ownerMaintenances = ownerBoats.stream()
                .flatMap(boat -> maintananceRepository.findByBoatId(boat.getId()).stream())
                .collect(Collectors.toList());

        // Get payments by owner
        List<PaymentEntity> ownerPayments = paymentRepository.findByUserId(userId);

        // Calculate metrics
        int totalBoats = ownerBoats.size();
        int pendingMaintenances = (int) ownerMaintenances.stream()
                .filter(m -> m.getStatus() == MaintananceStatus.PROGRAMADO || m.getStatus() == MaintananceStatus.EN_PROCESO)
                .count();
        int completedMaintenances = (int) ownerMaintenances.stream()
                .filter(m -> m.getStatus() == MaintananceStatus.COMPLETADO)
                .count();
        int totalDocuments = ownerBoats.stream()
                .mapToInt(boat -> boat.getDocuments() != null ? boat.getDocuments().size() : 0)
                .sum();

        // Current month payments
        YearMonth currentMonth = YearMonth.now();
        double monthlyPayments = ownerPayments.stream()
                .filter(p -> YearMonth.from(p.getDate()).equals(currentMonth))
                .mapToDouble(p -> p.getMount() != null ? p.getMount() : 0.0)
                .sum();

        // Build response
        dashboard.put("user", Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail()
        ));

        dashboard.put("metrics", Map.of(
                "totalBoats", totalBoats,
                "pendingMaintenances", pendingMaintenances,
                "completedMaintenances", completedMaintenances,
                "totalDocuments", totalDocuments,
                "monthlyPayments", monthlyPayments
        ));

        dashboard.put("boats", ownerBoats.stream().<Map<String, Object>>map(boat -> Map.of(
                "id", boat.getId(),
                "name", boat.getName(),
                "type", boat.getType(),
                "model", boat.getModel(),
                "location", boat.getLocation(),
                "price", boat.getPrice(),
                "balance", boat.getBalance(),
                "documentsCount", boat.getDocuments() != null ? boat.getDocuments().size() : 0
        )).collect(Collectors.toList()));

        // Upcoming maintenances (next 30 days)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysFromNow = now.plusDays(30);

        List<Map<String, Object>> upcomingMaintenances = ownerMaintenances.stream()
                .filter(m -> m.getDateScheduled() != null &&
                           m.getDateScheduled().isAfter(now) &&
                           m.getDateScheduled().isBefore(thirtyDaysFromNow))
                .sorted((a, b) -> a.getDateScheduled().compareTo(b.getDateScheduled()))
                .limit(5)
                .<Map<String, Object>>map(m -> Map.of(
                        "id", m.getId(),
                        "boatName", m.getBoat().getName(),
                        "type", m.getType(),
                        "status", m.getStatus(),
                        "priority", m.getPriority(),
                        "scheduledDate", m.getDateScheduled(),
                        "description", m.getDescription()
                ))
                .collect(Collectors.toList());

        dashboard.put("upcomingMaintenances", upcomingMaintenances);

        // Recent maintenances (last 10)
        List<Map<String, Object>> recentMaintenances = ownerMaintenances.stream()
                .sorted((a, b) -> {
                    LocalDateTime aDate = a.getDatePerformed() != null ? a.getDatePerformed() : a.getDateScheduled();
                    LocalDateTime bDate = b.getDatePerformed() != null ? b.getDatePerformed() : b.getDateScheduled();
                    return bDate.compareTo(aDate);
                })
                .limit(10)
                .<Map<String, Object>>map(m -> Map.of(
                        "id", m.getId(),
                        "boatName", m.getBoat().getName(),
                        "type", m.getType(),
                        "status", m.getStatus(),
                        "priority", m.getPriority(),
                        "scheduledDate", m.getDateScheduled(),
                        "performedDate", m.getDatePerformed(),
                        "description", m.getDescription(),
                        "cost", m.getCost()
                ))
                .collect(Collectors.toList());

        dashboard.put("recentMaintenances", recentMaintenances);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/payments/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getOwnerPayments(@PathVariable Long userId) {
        List<PaymentEntity> payments = paymentRepository.findByUserId(userId);

        List<Map<String, Object>> paymentData = payments.stream()
                .<Map<String, Object>>map(p -> Map.of(
                        "id", p.getId(),
                        "amount", p.getMount(),
                        "date", p.getDate(),
                        "reason", p.getReason(),
                        "invoiceUrl", p.getInvoice_url()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(paymentData);
    }

    @GetMapping("/maintenances/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getOwnerMaintenances(@PathVariable Long userId) {
        // Get owner's boats first
        List<BoatEntity> ownerBoats = boatRepository.findAll().stream()
                .filter(boat -> boat.getOwner() != null && boat.getOwner().getId().equals(userId))
                .collect(Collectors.toList());

        // Get maintenances for all owner's boats
        List<MaintananceEntity> maintenances = ownerBoats.stream()
                .flatMap(boat -> maintananceRepository.findByBoatId(boat.getId()).stream())
                .collect(Collectors.toList());

        List<Map<String, Object>> maintenanceData = maintenances.stream()
                .<Map<String, Object>>map(m -> Map.of(
                        "id", m.getId(),
                        "boatName", m.getBoat().getName(),
                        "boatId", m.getBoat().getId(),
                        "type", m.getType(),
                        "status", m.getStatus(),
                        "priority", m.getPriority(),
                        "scheduledDate", m.getDateScheduled(),
                        "performedDate", m.getDatePerformed(),
                        "description", m.getDescription(),
                        "cost", m.getCost()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(maintenanceData);
    }
}