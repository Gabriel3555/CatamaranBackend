package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.catamaran.catamaranbackend.domain.*;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.MaintananceRepository;
import com.catamaran.catamaranbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/owners/stats")
    public ResponseEntity<Map<String, Object>> getOwnersStats() {
        Map<String, Object> stats = new HashMap<>();

        // Get all owners (only users with PROPIETARIO role)
        List<UserEntity> allOwners = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.PROPIETARIO)
                .collect(Collectors.toList());

        // Total owners
        stats.put("totalOwners", allOwners.size());

        // Active owners
        long activeOwners = allOwners.stream()
                .filter(user -> user.getStatus() != null && user.getStatus())
                .count();
        stats.put("activeOwners", activeOwners);

        // Inactive owners
        long inactiveOwners = allOwners.size() - activeOwners;
        stats.put("inactiveOwners", inactiveOwners);

        // Owners with boats
        long ownersWithBoats = allOwners.stream()
                .filter(owner -> {
                    List<BoatEntity> boats = boatRepository.findByOwner(owner);
                    return boats != null && !boats.isEmpty();
                })
                .count();
        stats.put("ownersWithBoats", ownersWithBoats);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/owners")
    public ResponseEntity<Page<Map<String, Object>>> getUsersWithBoatCounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        System.out.println("AdminController.getUsersWithBoatCounts called with filters:");
        System.out.println("  page: " + page + ", size: " + size);
        System.out.println("  search: " + search);

        // Get all owners first
        List<UserEntity> allOwners = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.PROPIETARIO)
                .collect(Collectors.toList());

        // Apply search filter if provided
        if (search != null && !search.trim().isEmpty()) {
            System.out.println("Applying search filter: " + search.trim());
            String searchTerm = search.trim().toLowerCase();
            allOwners = allOwners.stream()
                    .filter(user ->
                        (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchTerm)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchTerm)) ||
                        (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchTerm))
                    )
                    .collect(Collectors.toList());
        }

        // Sort owners by ID in descending order (highest to lowest)
        allOwners.sort((a, b) -> Long.compare(b.getId(), a.getId()));

        // Create pageable for pagination
        Pageable pageable = PageRequest.of(page, size);

        // Convert to Page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allOwners.size());

        List<Map<String, Object>> pageContent = allOwners.subList(start, end).stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("email", user.getEmail());
                    userMap.put("username", user.getUsername());
                    userMap.put("fullName", user.getFullName());
                    userMap.put("phoneNumber", user.getPhoneNumber());
                    userMap.put("role", user.getRole());
                    userMap.put("status", user.getStatus());
                    userMap.put("uniqueId", user.getUniqueId());
                    // Count boats for this user
                    long boatCount = boatRepository.findByOwner(user).size();
                    userMap.put("boatsCount", boatCount);
                    return userMap;
                })
                .collect(Collectors.toList());

        Page<Map<String, Object>> result = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, allOwners.size());

        System.out.println("Returning " + result.getTotalElements() + " owners");
        return ResponseEntity.ok(result);
    }
}