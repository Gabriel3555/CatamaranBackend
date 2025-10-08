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
        // Find the user
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

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

        return ResponseEntity.ok(response);
    }

}