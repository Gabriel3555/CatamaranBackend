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
                        .email("carlos.rodriguez@email.com")
                        .username("carlosr")
                        .uniqueId(UUID.randomUUID())
                        .password(passwordEncoder.encode("owner123"))
                        .role(Role.PROPIETARIO)
                        .status(true)
                        .fullName("Carlos Rodríguez")
                        .phoneNumber("+57 302 345 6789")
                        .build(),
                    UserEntity.builder()
                        .email("maria.gonzalez@email.com")
                        .username("mariag")
                        .uniqueId(UUID.randomUUID())
                        .password(passwordEncoder.encode("owner123"))
                        .role(Role.PROPIETARIO)
                        .status(true)
                        .fullName("María González")
                        .phoneNumber("+57 303 456 7890")
                        .build(),
                    UserEntity.builder()
                        .email("juan.martinez@email.com")
                        .username("juanm")
                        .uniqueId(UUID.randomUUID())
                        .password(passwordEncoder.encode("owner123"))
                        .role(Role.PROPIETARIO)
                        .status(true)
                        .fullName("Juan Martínez")
                        .phoneNumber("+57 304 567 8901")
                        .build(),
                    UserEntity.builder()
                        .email("ana.lopez@email.com")
                        .username("anal")
                        .uniqueId(UUID.randomUUID())
                        .password(passwordEncoder.encode("owner123"))
                        .role(Role.PROPIETARIO)
                        .status(true)
                        .fullName("Ana López")
                        .phoneNumber("+57 305 678 9012")
                        .build(),
                    UserEntity.builder()
                        .email("pedro.sanchez@email.com")
                        .username("pedros")
                        .uniqueId(UUID.randomUUID())
                        .password(passwordEncoder.encode("owner123"))
                        .role(Role.PROPIETARIO)
                        .status(true)
                        .fullName("Pedro Sánchez")
                        .phoneNumber("+57 306 789 0123")
                        .build(),
                    UserEntity.builder()
                        .email("laura.torres@email.com")
                        .username("laurat")
                        .uniqueId(UUID.randomUUID())
                        .password(passwordEncoder.encode("owner123"))
                        .role(Role.PROPIETARIO)
                        .status(true)
                        .fullName("Laura Torres")
                        .phoneNumber("+57 307 890 1234")
                        .build(),
                    UserEntity.builder()
                        .email("diego.ramirez@email.com")
                        .username("diegor")
                        .uniqueId(UUID.randomUUID())
                        .password(passwordEncoder.encode("owner123"))
                        .role(Role.PROPIETARIO)
                        .status(false)
                        .fullName("Diego Ramírez")
                        .phoneNumber("+57 308 901 2345")
                        .build(),
                    UserEntity.builder()
                        .email("sofia.herrera@email.com")
                        .username("sofiah")
                        .uniqueId(UUID.randomUUID())
                        .password(passwordEncoder.encode("owner123"))
                        .role(Role.PROPIETARIO)
                        .status(true)
                        .fullName("Sofía Herrera")
                        .phoneNumber("+57 309 012 3456")
                        .build()
                );

                userRepository.saveAll(owners);

                // Create Boats
                List<BoatEntity> boats = Arrays.asList(
                    BoatEntity.builder()
                        .name("Catamaran Explorer")
                        .model("CE-2000")
                        .type(BoatType.TURISMO)
                        .location("Lago de Tota")
                        .price(150000000.0)
                        .balance(2500000.0)
                        .build(),
                    BoatEntity.builder()
                        .name("Catamaran Paradise")
                        .model("CP-1500")
                        .type(BoatType.ALOJAMIENTO)
                        .location("Laguna de Fúquene")
                        .price(200000000.0)
                        .balance(1800000.0)
                        .build(),
                    BoatEntity.builder()
                        .name("Catamaran Eco")
                        .model("ECO-1000")
                        .type(BoatType.TURISMO)
                        .location("Embalse del Neusa")
                        .price(120000000.0)
                        .balance(3200000.0)
                        .build(),
                    BoatEntity.builder()
                        .name("Catamaran Luxury")
                        .model("LX-3000")
                        .type(BoatType.DISENO_EXCLUSIVO)
                        .location("Lago de Tota")
                        .price(350000000.0)
                        .balance(1500000.0)
                        .build(),
                    BoatEntity.builder()
                        .name("Catamaran Adventure")
                        .model("AD-2500")
                        .type(BoatType.TURISMO)
                        .location("Laguna de Fúquene")
                        .price(180000000.0)
                        .balance(2800000.0)
                        .build(),
                    BoatEntity.builder()
                        .name("Catamaran Resort")
                        .model("RS-1800")
                        .type(BoatType.ALOJAMIENTO)
                        .location("Embalse del Neusa")
                        .price(220000000.0)
                        .balance(1900000.0)
                        .build(),
                    BoatEntity.builder()
                        .name("Catamaran Green")
                        .model("GR-1200")
                        .type(BoatType.TURISMO)
                        .location("Lago de Tota")
                        .price(140000000.0)
                        .balance(3500000.0)
                        .build(),
                    BoatEntity.builder()
                        .name("Catamaran Premium")
                        .model("PR-4000")
                        .type(BoatType.DISENO_EXCLUSIVO)
                        .location("Laguna de Fúquene")
                        .price(400000000.0)
                        .balance(1200000.0)
                        .build(),
                    BoatEntity.builder()
                        .name("Catamaran Discovery")
                        .model("DC-2200")
                        .type(BoatType.TURISMO)
                        .location("Embalse del Neusa")
                        .price(160000000.0)
                        .balance(2700000.0)
                        .build(),
                    BoatEntity.builder()
                        .name("Catamaran Oasis")
                        .model("OA-1600")
                        .type(BoatType.ALOJAMIENTO)
                        .location("Lago de Tota")
                        .price(190000000.0)
                        .balance(2100000.0)
                        .build()
                );

                boatRepository.saveAll(boats);

                // Assign owners to boats (only some boats should be assigned, leaving others available)
                List<UserEntity> savedOwners = userRepository.findAll().stream()
                    .filter(owner -> owner.getRole() == Role.PROPIETARIO && Boolean.TRUE.equals(owner.getStatus()))
                    .toList();
                List<BoatEntity> savedBoats = boatRepository.findAll();

                // Only assign owners to the first half of boats, leaving the rest available
                int boatsToAssign = Math.min(savedBoats.size() / 2, savedOwners.size());

                for (int i = 0; i < boatsToAssign; i++) {
                    savedBoats.get(i).setOwner(savedOwners.get(i % savedOwners.size()));
                    savedBoats.get(i).setBalance(savedBoats.get(i).getBalance() + savedBoats.get(i).getPrice());
                }

                boatRepository.saveAll(savedBoats);

                // Create Maintenances
                List<MaintananceEntity> maintenances = Arrays.asList(
                    MaintananceEntity.builder()
                        .boat(savedBoats.get(0))
                        .type(MaintananceType.PREVENTIVO)
                        .status(MaintananceStatus.COMPLETADO)
                        .priority(MaintenancePriority.MEDIA)
                        .dateScheduled(LocalDateTime.now().minusDays(30))
                        .datePerformed(LocalDateTime.now().minusDays(28))
                        .description("Mantenimiento preventivo mensual - Revisión de motores y sistemas eléctricos")
                        .cost(450000.0)
                        .build(),
                    MaintananceEntity.builder()
                        .boat(savedBoats.get(1))
                        .type(MaintananceType.CORRECTIVO)
                        .status(MaintananceStatus.EN_PROCESO)
                        .priority(MaintenancePriority.ALTA)
                        .dateScheduled(LocalDateTime.now().minusDays(5))
                        .description("Reparación de sistema de navegación GPS")
                        .cost(750000.0)
                        .build(),
                    MaintananceEntity.builder()
                        .boat(savedBoats.get(2))
                        .type(MaintananceType.PREVENTIVO)
                        .status(MaintananceStatus.PROGRAMADO)
                        .priority(MaintenancePriority.BAJA)
                        .dateScheduled(LocalDateTime.now().plusDays(15))
                        .description("Limpieza y mantenimiento de paneles solares")
                        .cost(250000.0)
                        .build(),
                    MaintananceEntity.builder()
                        .boat(savedBoats.get(3))
                        .type(MaintananceType.CORRECTIVO)
                        .status(MaintananceStatus.COMPLETADO)
                        .priority(MaintenancePriority.ALTA)
                        .dateScheduled(LocalDateTime.now().minusDays(20))
                        .datePerformed(LocalDateTime.now().minusDays(18))
                        .description("Reparación de motores - Reemplazo de filtros")
                        .cost(380000.0)
                        .build(),
                    MaintananceEntity.builder()
                        .boat(savedBoats.get(4))
                        .type(MaintananceType.CORRECTIVO)
                        .status(MaintananceStatus.PROGRAMADO)
                        .priority(MaintenancePriority.ALTA)
                        .dateScheduled(LocalDateTime.now().plusDays(3))
                        .description("Reparación urgente de sistema de refrigeración")
                        .cost(920000.0)
                        .build(),
                    MaintananceEntity.builder()
                        .boat(savedBoats.get(5))
                        .type(MaintananceType.PREVENTIVO)
                        .status(MaintananceStatus.COMPLETADO)
                        .priority(MaintenancePriority.MEDIA)
                        .dateScheduled(LocalDateTime.now().minusDays(45))
                        .datePerformed(LocalDateTime.now().minusDays(43))
                        .description("Mantenimiento general de habitaciones y áreas comunes")
                        .cost(550000.0)
                        .build(),
                    MaintananceEntity.builder()
                        .boat(savedBoats.get(6))
                        .type(MaintananceType.PREVENTIVO)
                        .status(MaintananceStatus.EN_PROCESO)
                        .priority(MaintenancePriority.MEDIA)
                        .dateScheduled(LocalDateTime.now().minusDays(10))
                        .description("Mantenimiento de sistemas ecológicos y filtros de agua")
                        .cost(320000.0)
                        .build(),
                    MaintananceEntity.builder()
                        .boat(savedBoats.get(7))
                        .type(MaintananceType.CORRECTIVO)
                        .status(MaintananceStatus.COMPLETADO)
                        .priority(MaintenancePriority.ALTA)
                        .dateScheduled(LocalDateTime.now().minusDays(12))
                        .datePerformed(LocalDateTime.now().minusDays(10))
                        .description("Reparación de sistema de sonido premium")
                        .cost(680000.0)
                        .build(),
                    MaintananceEntity.builder()
                        .boat(savedBoats.get(8))
                        .type(MaintananceType.PREVENTIVO)
                        .status(MaintananceStatus.PROGRAMADO)
                        .priority(MaintenancePriority.BAJA)
                        .dateScheduled(LocalDateTime.now().plusDays(25))
                        .description("Revisión general de equipos de navegación y seguridad")
                        .cost(410000.0)
                        .build(),
                    MaintananceEntity.builder()
                        .boat(savedBoats.get(9))
                        .type(MaintananceType.PREVENTIVO)
                        .status(MaintananceStatus.EN_PROCESO)
                        .priority(MaintenancePriority.MEDIA)
                        .dateScheduled(LocalDateTime.now().minusDays(7))
                        .description("Mantenimiento preventivo de sistemas de climatización")
                        .cost(290000.0)
                        .build(),
                    MaintananceEntity.builder()
                        .boat(savedBoats.get(0))
                        .type(MaintananceType.CORRECTIVO)
                        .status(MaintananceStatus.COMPLETADO)
                        .priority(MaintenancePriority.ALTA)
                        .dateScheduled(LocalDateTime.now().minusDays(60))
                        .datePerformed(LocalDateTime.now().minusDays(58))
                        .description("Reparación de generador eléctrico auxiliar")
                        .cost(850000.0)
                        .build(),
                    MaintananceEntity.builder()
                        .boat(savedBoats.get(1))
                        .type(MaintananceType.PREVENTIVO)
                        .status(MaintananceStatus.PROGRAMADO)
                        .priority(MaintenancePriority.MEDIA)
                        .dateScheduled(LocalDateTime.now().plusDays(30))
                        .description("Mantenimiento preventivo trimestral completo")
                        .cost(620000.0)
                        .build()
                );

                maintananceRepository.saveAll(maintenances);

                // Create Payments
                List<PaymentEntity> payments = Arrays.asList(
                    PaymentEntity.builder()
                        .user(savedOwners.get(0))
                        .mount(500000.0)
                        .date(LocalDateTime.now().minusDays(30))
                        .reason(ReasonPayment.PAGO)
                        .invoice_url("INV-001-2024")
                        .build(),
                    PaymentEntity.builder()
                        .user(savedOwners.get(1))
                        .mount(750000.0)
                        .date(LocalDateTime.now().minusDays(25))
                        .reason(ReasonPayment.MANTENIMIENTO)
                        .invoice_url("INV-002-2024")
                        .build(),
                    PaymentEntity.builder()
                        .user(savedOwners.get(2))
                        .mount(600000.0)
                        .date(LocalDateTime.now().minusDays(20))
                        .reason(ReasonPayment.PAGO)
                        .invoice_url("INV-003-2024")
                        .build(),
                    PaymentEntity.builder()
                        .user(savedOwners.get(3))
                        .mount(450000.0)
                        .date(LocalDateTime.now().minusDays(15))
                        .reason(ReasonPayment.PAGO)
                        .invoice_url("INV-004-2024")
                        .build(),
                    PaymentEntity.builder()
                        .user(savedOwners.get(4))
                        .mount(800000.0)
                        .date(LocalDateTime.now().minusDays(10))
                        .reason(ReasonPayment.MANTENIMIENTO)
                        .invoice_url("INV-005-2024")
                        .build(),
                    PaymentEntity.builder()
                        .user(savedOwners.get(5))
                        .mount(550000.0)
                        .date(LocalDateTime.now().minusDays(5))
                        .reason(ReasonPayment.PAGO)
                        .invoice_url("INV-006-2024")
                        .build(),
                    // Current month payments
                    PaymentEntity.builder()
                        .user(savedOwners.get(0))
                        .mount(1200000.0)
                        .date(LocalDateTime.now().minusDays(2))
                        .reason(ReasonPayment.PAGO)
                        .invoice_url("INV-013-2024")
                        .build(),
                    PaymentEntity.builder()
                        .user(savedOwners.get(1))
                        .mount(950000.0)
                        .date(LocalDateTime.now().minusDays(1))
                        .reason(ReasonPayment.MANTENIMIENTO)
                        .invoice_url("INV-014-2024")
                        .build(),
                    PaymentEntity.builder()
                        .user(savedOwners.get(2))
                        .mount(780000.0)
                        .date(LocalDateTime.now())
                        .reason(ReasonPayment.PAGO)
                        .invoice_url("INV-015-2024")
                        .build(),
                    PaymentEntity.builder()
                        .user(savedOwners.get(0))
                        .mount(700000.0)
                        .date(LocalDateTime.now().minusDays(60))
                        .reason(ReasonPayment.MANTENIMIENTO)
                        .invoice_url("INV-007-2024")
                        .build(),
                    PaymentEntity.builder()
                        .user(savedOwners.get(1))
                        .mount(650000.0)
                        .date(LocalDateTime.now().minusDays(45))
                        .reason(ReasonPayment.PAGO)
                        .invoice_url("INV-008-2024")
                        .build(),
                    PaymentEntity.builder()
                        .user(savedOwners.get(2))
                        .mount(900000.0)
                        .date(LocalDateTime.now().minusDays(35))
                        .reason(ReasonPayment.MANTENIMIENTO)
                        .invoice_url("INV-009-2024")
                        .build(),
                    PaymentEntity.builder()
                        .user(savedOwners.get(3))
                        .mount(580000.0)
                        .date(LocalDateTime.now().minusDays(28))
                        .reason(ReasonPayment.PAGO)
                        .invoice_url("INV-010-2024")
                        .build(),
                    PaymentEntity.builder()
                        .user(savedOwners.get(4))
                        .mount(720000.0)
                        .date(LocalDateTime.now().minusDays(21))
                        .reason(ReasonPayment.PAGO)
                        .invoice_url("INV-011-2024")
                        .build(),
                    PaymentEntity.builder()
                        .user(savedOwners.get(5))
                        .mount(850000.0)
                        .date(LocalDateTime.now().minusDays(14))
                        .reason(ReasonPayment.MANTENIMIENTO)
                        .invoice_url("INV-012-2024")
                        .build()
                );

                paymentRepository.saveAll(payments);

                System.out.println("✅ Base de datos inicializada con datos de prueba:");
                System.out.println("   - " + admins.size() + " administradores");
                System.out.println("   - " + owners.size() + " propietarios");
                System.out.println("   - " + boats.size() + " embarcaciones");
                System.out.println("   - " + maintenances.size() + " mantenimientos");
                System.out.println("   - " + payments.size() + " pagos");
                System.out.println("💰 Pagos del mes actual: $" +
                    payments.stream()
                        .filter(p -> YearMonth.from(p.getDate()).equals(YearMonth.now()))
                        .mapToDouble(p -> p.getMount() != null ? p.getMount() : 0.0)
                        .sum());
                System.out.println("🚀 Aplicación lista para usar!");
            }
        };
    }
}