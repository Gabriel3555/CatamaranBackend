package com.catamaran.catamaranbackend.config;

import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.catamaran.catamaranbackend.domain.*;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.MaintananceRepository;
import com.catamaran.catamaranbackend.repository.PaymentRepository;
import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepositoryJpa userRepository;
    private final BoatRepository boatRepository;
    private final MaintananceRepository maintananceRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() == 0) {
                // Create Admin Users
                List<UserEntity> admins = Arrays.asList(
                        UserEntity.builder()
                                .email("admin@alianzacarrocera.com")
                                .username("admin")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Administrador Principal")
                                .phoneNumber("+57 300 123 4567")
                                .build(),
                        UserEntity.builder()
                                .email("admin2@alianzacarrocera.com")
                                .username("admin2")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Administrador Secundario")
                                .phoneNumber("+57 301 234 5678")
                                .build()
                );

                userRepository.saveAll(admins);

                // Create Owner Users
                List<UserEntity> owners = Arrays.asList(
                        UserEntity.builder()
                                .email("owner1@alianzacarrocera.com")
                                .username("owner1")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Juan Pérez")
                                .phoneNumber("+57 302 345 6789")
                                .build(),
                        UserEntity.builder()
                                .email("owner2@alianzacarrocera.com")
                                .username("owner2")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("María González")
                                .phoneNumber("+57 303 456 7890")
                                .build()
                );

                userRepository.saveAll(owners);

                // Create Sample Boats with owners
                if (boatRepository.count() == 0) {
                    List<BoatEntity> boats = Arrays.asList(
                            BoatEntity.builder()
                                    .name("Catamaran A")
                                    .model("Model A")
                                    .type(BoatType.TURISMO)
                                    .location("Manta")
                                    .price(1000000.0)
                                    .balance(0.0)
                                    .owner(owners.get(0)) // Assign to first owner
                                    .build(),
                            BoatEntity.builder()
                                    .name("Catamaran B")
                                    .model("Model B")
                                    .type(BoatType.DISENO_EXCLUSIVO)
                                    .location("Manta")
                                    .price(1500000.0)
                                    .balance(0.0)
                                    .owner(owners.get(0)) // Assign to first owner
                                    .build(),
                            BoatEntity.builder()
                                    .name("Catamaran C")
                                    .model("Model C")
                                    .type(BoatType.ALOJAMIENTO)
                                    .location("Manta")
                                    .price(1200000.0)
                                    .balance(0.0)
                                    .owner(owners.get(1)) // Assign to second owner
                                    .build()
                    );
                    boatRepository.saveAll(boats);

                    // Create some sample maintenances
                    if (maintananceRepository.count() == 0) {
                        List<MaintananceEntity> maintenances = Arrays.asList(
                                MaintananceEntity.builder()
                                        .boat(boats.get(0))
                                        .type(MaintananceType.PREVENTIVO)
                                        .status(MaintananceStatus.COMPLETADO)
                                        .priority(MaintenancePriority.MEDIA)
                                        .description("Mantenimiento preventivo mensual")
                                        .dateScheduled(LocalDateTime.now().minusDays(10))
                                        .datePerformed(LocalDateTime.now().minusDays(8))
                                        .cost(150000.0)
                                        .build(),
                                MaintananceEntity.builder()
                                        .boat(boats.get(0))
                                        .type(MaintananceType.CORRECTIVO)
                                        .status(MaintananceStatus.PROGRAMADO)
                                        .priority(MaintenancePriority.ALTA)
                                        .description("Reparación de motor")
                                        .dateScheduled(LocalDateTime.now().plusDays(5))
                                        .cost(300000.0)
                                        .build(),
                                MaintananceEntity.builder()
                                        .boat(boats.get(1))
                                        .type(MaintananceType.CORRECTIVO)
                                        .status(MaintananceStatus.EN_PROCESO)
                                        .priority(MaintenancePriority.BAJA)
                                        .description("Inspección anual")
                                        .dateScheduled(LocalDateTime.now().plusDays(15))
                                        .build()
                        );
                        maintananceRepository.saveAll(maintenances);
                    }
                }
            }
        };
    }
}