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
import java.util.ArrayList;
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
                                .build(),
                        UserEntity.builder()
                                .email("supervisor@alianzacarrocera.com")
                                .username("supervisor")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Supervisor de Operaciones")
                                .phoneNumber("+57 302 345 6789")
                                .build(),
                        UserEntity.builder()
                                .email("gerente@alianzacarrocera.com")
                                .username("gerente")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Gerente General")
                                .phoneNumber("+57 303 456 7890")
                                .build(),
                        UserEntity.builder()
                                .email("coordinador@alianzacarrocera.com")
                                .username("coordinador")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Coordinador de Mantenimiento")
                                .phoneNumber("+57 304 567 8901")
                                .build(),
                        UserEntity.builder()
                                .email("tecnico@alianzacarrocera.com")
                                .username("tecnico")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Técnico Especialista")
                                .phoneNumber("+57 305 678 9012")
                                .build(),
                        UserEntity.builder()
                                .email("soporte@alianzacarrocera.com")
                                .username("soporte")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Soporte Técnico")
                                .phoneNumber("+57 306 789 0123")
                                .build(),
                        UserEntity.builder()
                                .email("analista@alianzacarrocera.com")
                                .username("analista")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Analista de Sistemas")
                                .phoneNumber("+57 307 890 1234")
                                .build(),
                        UserEntity.builder()
                                .email("auditor@alianzacarrocera.com")
                                .username("auditor")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Auditor Interno")
                                .phoneNumber("+57 308 901 2345")
                                .build(),
                        UserEntity.builder()
                                .email("capitan@alianzacarrocera.com")
                                .username("capitan")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Capitán de Puerto")
                                .phoneNumber("+57 309 012 3456")
                                .build(),
                        UserEntity.builder()
                                .email("inspector@alianzacarrocera.com")
                                .username("inspector")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Inspector de Seguridad")
                                .phoneNumber("+57 310 123 4567")
                                .build(),
                        UserEntity.builder()
                                .email("entrenador@alianzacarrocera.com")
                                .username("entrenador")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Instructor de Seguridad")
                                .phoneNumber("+57 311 234 5678")
                                .build(),
                        UserEntity.builder()
                                .email("logistica@alianzacarrocera.com")
                                .username("logistica")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Coordinador de Logística")
                                .phoneNumber("+57 312 345 6789")
                                .build(),
                        UserEntity.builder()
                                .email("rrhh@alianzacarrocera.com")
                                .username("rrhh")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Recursos Humanos")
                                .phoneNumber("+57 313 456 7890")
                                .build(),
                        UserEntity.builder()
                                .email("finanzas@alianzacarrocera.com")
                                .username("finanzas")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Director Financiero")
                                .phoneNumber("+57 314 567 8901")
                                .build()
                );

                // Create diverse Boat Owners
                List<UserEntity> owners = Arrays.asList(
                        UserEntity.builder()
                                .email("carlos.martinez@email.com")
                                .username("cmartinez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Carlos Martínez López")
                                .phoneNumber("+57 310 111 2222")
                                .build(),
                        UserEntity.builder()
                                .email("maria.garcia@email.com")
                                .username("mgarcia")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("María García Rodríguez")
                                .phoneNumber("+57 311 333 4444")
                                .build(),
                        UserEntity.builder()
                                .email("juan.perez@email.com")
                                .username("jperez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Juan Pérez Sánchez")
                                .phoneNumber("+57 312 555 6666")
                                .build(),
                        UserEntity.builder()
                                .email("ana.lopez@email.com")
                                .username("alopez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Ana López Fernández")
                                .phoneNumber("+57 313 777 8888")
                                .build(),
                        UserEntity.builder()
                                .email("luis.gonzalez@email.com")
                                .username("lgonzalez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Luis González Martín")
                                .phoneNumber("+57 314 999 0000")
                                .build(),
                        UserEntity.builder()
                                .email("carmen.ruiz@email.com")
                                .username("cruiz")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Carmen Ruiz Jiménez")
                                .phoneNumber("+57 315 123 4567")
                                .build(),
                        UserEntity.builder()
                                .email("antonio.diaz@email.com")
                                .username("adiaz")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Antonio Díaz Herrera")
                                .phoneNumber("+57 316 234 5678")
                                .build(),
                        UserEntity.builder()
                                .email("isabel.moreno@email.com")
                                .username("imoreno")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Isabel Moreno Álvarez")
                                .phoneNumber("+57 317 345 6789")
                                .build(),
                        UserEntity.builder()
                                .email("francisco.munoz@email.com")
                                .username("fmunoz")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Francisco Muñoz Romero")
                                .phoneNumber("+57 318 456 7890")
                                .build(),
                        UserEntity.builder()
                                .email("dolores.castro@email.com")
                                .username("dcastro")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Dolores Castro Ortega")
                                .phoneNumber("+57 319 567 8901")
                                .build(),
                        UserEntity.builder()
                                .email("roberto.silva@email.com")
                                .username("rsilva")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Roberto Silva Mendoza")
                                .phoneNumber("+57 320 678 9012")
                                .build(),
                        UserEntity.builder()
                                .email("patricia.navarro@email.com")
                                .username("pnavarro")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Patricia Navarro Herrera")
                                .phoneNumber("+57 321 789 0123")
                                .build(),
                        UserEntity.builder()
                                .email("alejandro.ramos@email.com")
                                .username("aramos")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Alejandro Ramos Cortés")
                                .phoneNumber("+57 322 890 1234")
                                .build(),
                        UserEntity.builder()
                                .email("silvia.guerrero@email.com")
                                .username("sguerrero")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Silvia Guerrero Medina")
                                .phoneNumber("+57 323 901 2345")
                                .build(),
                        UserEntity.builder()
                                .email("diego.pena@email.com")
                                .username("dpena")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Diego Peña Aguilar")
                                .phoneNumber("+57 324 012 3456")
                                .build(),
                        UserEntity.builder()
                                .email("natalia.vargas@email.com")
                                .username("nvargas")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Natalia Vargas Cabrera")
                                .phoneNumber("+57 325 123 4567")
                                .build(),
                        UserEntity.builder()
                                .email("sergio.morales@email.com")
                                .username("smorales")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Sergio Morales Delgado")
                                .phoneNumber("+57 326 234 5678")
                                .build(),
                        UserEntity.builder()
                                .email("valentina.rios@email.com")
                                .username("vrios")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Valentina Ríos Estrada")
                                .phoneNumber("+57 327 345 6789")
                                .build(),
                        UserEntity.builder()
                                .email("fernando.castillo@email.com")
                                .username("fcastillo")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Fernando Castillo Guerrero")
                                .phoneNumber("+57 328 456 7890")
                                .build(),
                        UserEntity.builder()
                                .email("camila.santos@email.com")
                                .username("csantos")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Camila Santos Mendoza")
                                .phoneNumber("+57 329 567 8901")
                                .build(),
                        UserEntity.builder()
                                .email("eduardo.fernandez@email.com")
                                .username("efernandez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Eduardo Fernández Pineda")
                                .phoneNumber("+57 330 678 9012")
                                .build(),
                        UserEntity.builder()
                                .email("paula.torres@email.com")
                                .username("ptorres")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Paula Torres Guzmán")
                                .phoneNumber("+57 331 789 0123")
                                .build(),
                        UserEntity.builder()
                                .email("ricardo.sanchez@email.com")
                                .username("rsanchez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Ricardo Sánchez Morales")
                                .phoneNumber("+57 332 890 1234")
                                .build(),
                        UserEntity.builder()
                                .email("sofia.ortega@email.com")
                                .username("sortega")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Sofía Ortega Delgado")
                                .phoneNumber("+57 333 901 2345")
                                .build(),
                        UserEntity.builder()
                                .email("gabriel.ruiz@email.com")
                                .username("gruiz")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Gabriel Ruiz Herrera")
                                .phoneNumber("+57 334 012 3456")
                                .build(),
                        UserEntity.builder()
                                .email("carolina.mendoza@email.com")
                                .username("cmendoza")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Carolina Mendoza Castro")
                                .phoneNumber("+57 335 123 4567")
                                .build(),
                        UserEntity.builder()
                                .email("matias.silva@email.com")
                                .username("msilva")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Matías Silva Romero")
                                .phoneNumber("+57 336 234 5678")
                                .build(),
                        UserEntity.builder()
                                .email("antonia.navarro@email.com")
                                .username("anavarro")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Antonia Navarro León")
                                .phoneNumber("+57 337 345 6789")
                                .build(),
                        UserEntity.builder()
                                .email("emiliano.garcia@email.com")
                                .username("egarcia")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Emiliano García Medina")
                                .phoneNumber("+57 338 456 7890")
                                .build(),
                        UserEntity.builder()
                                .email("renata.lopez@email.com")
                                .username("rlopez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Renata López Aguilar")
                                .phoneNumber("+57 339 567 8901")
                                .build(),
                        UserEntity.builder()
                                .email("thiago.martinez@email.com")
                                .username("tmartinez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Thiago Martínez Cabrera")
                                .phoneNumber("+57 340 678 9012")
                                .build(),
                        UserEntity.builder()
                                .email("constanza.rodriguez@email.com")
                                .username("crodriguez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Constanza Rodríguez Peña")
                                .phoneNumber("+57 341 789 0123")
                                .build(),
                        UserEntity.builder()
                                .email("nicolas.gonzalez@email.com")
                                .username("ngonzalez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Nicolás González Vargas")
                                .phoneNumber("+57 342 890 1234")
                                .build(),
                        UserEntity.builder()
                                .email("florencia.hernandez@email.com")
                                .username("fhernandez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Florencia Hernández Ramos")
                                .phoneNumber("+57 343 901 2345")
                                .build(),
                        UserEntity.builder()
                                .email("agustin.perez@email.com")
                                .username("aperez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Agustín Pérez Guerrero")
                                .phoneNumber("+57 344 012 3456")
                                .build(),
                        UserEntity.builder()
                                .email("celeste.diaz@email.com")
                                .username("cdiaz")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Celeste Díaz Mendoza")
                                .phoneNumber("+57 345 123 4567")
                                .build(),
                        UserEntity.builder()
                                .email("benjamin.morales@email.com")
                                .username("bmorales")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Benjamín Morales Ortega")
                                .phoneNumber("+57 346 234 5678")
                                .build(),
                        UserEntity.builder()
                                .email("delfina.gutierrez@email.com")
                                .username("dgutierrez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Delfina Gutiérrez Silva")
                                .phoneNumber("+57 347 345 6789")
                                .build(),
                        UserEntity.builder()
                                .email("maximiliano.fernandez@email.com")
                                .username("mfernandez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Maximiliano Fernández Torres")
                                .phoneNumber("+57 348 456 7890")
                                .build(),
                        UserEntity.builder()
                                .email("paloma.sanchez@email.com")
                                .username("psanchez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Paloma Sánchez Ruiz")
                                .phoneNumber("+57 349 567 8901")
                                .build(),
                        UserEntity.builder()
                                .email("valentin.lopez@email.com")
                                .username("vlopez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Valentín López Navarro")
                                .phoneNumber("+57 350 678 9012")
                                .build(),
                        UserEntity.builder()
                                .email("azul.garcia@email.com")
                                .username("agarcia")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Azul García Mendoza")
                                .phoneNumber("+57 351 789 0123")
                                .build(),
                        UserEntity.builder()
                                .email("santino.rodriguez@email.com")
                                .username("srodriguez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Santino Rodríguez Silva")
                                .phoneNumber("+57 352 890 1234")
                                .build(),
                        UserEntity.builder()
                                .email("quintana.gonzalez@email.com")
                                .username("qgonzalez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Quintana González Torres")
                                .phoneNumber("+57 353 901 2345")
                                .build(),
                        UserEntity.builder()
                                .email("remedios.martinez@email.com")
                                .username("rmartinez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Remedios Martínez Sánchez")
                                .phoneNumber("+57 354 012 3456")
                                .build(),
                        UserEntity.builder()
                                .email("teodoro.perez@email.com")
                                .username("tperez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Teodoro Pérez López")
                                .phoneNumber("+57 355 123 4567")
                                .build(),
                        UserEntity.builder()
                                .email("urbana.garcia@email.com")
                                .username("ugarcia")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Urbana García Rodríguez")
                                .phoneNumber("+57 356 234 5678")
                                .build(),
                        UserEntity.builder()
                                .email("victoriano.lopez@email.com")
                                .username("vlopez2")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Victoriano López González")
                                .phoneNumber("+57 357 345 6789")
                                .build(),
                        UserEntity.builder()
                                .email("wanda.martinez@email.com")
                                .username("wmartinez")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Wanda Martínez Hernández")
                                .phoneNumber("+57 358 456 7890")
                                .build()
                );

                List<UserEntity> allUsers = new ArrayList<>();
                allUsers.addAll(admins);
                allUsers.addAll(owners);
                userRepository.saveAll(allUsers);
                userRepository.flush();
            }

            if (boatRepository.count() == 0) {
                // Get some owners for boat assignment
                // First, ensure users are properly saved by clearing the persistence context
                userRepository.flush();

                // Query all users and filter by role to ensure we get the right users
                List<UserEntity> allUsersInDb = userRepository.findAll();
                List<UserEntity> owners = allUsersInDb.stream()
                        .filter(user -> user.getRole() == Role.PROPIETARIO)
                        .toList();

                // Log the actual counts for debugging
                System.out.println("Total users in DB: " + allUsersInDb.size());
                System.out.println("Owners found: " + owners.size());

                // If we don't have enough owners, create additional ones
                if (owners.size() < 10) {
                    System.out.println("Creating additional owners for boat initialization...");
                    List<UserEntity> additionalOwners = new ArrayList<>();

                    for (int i = owners.size(); i < 10; i++) {
                        UserEntity additionalOwner = UserEntity.builder()
                                .email("owner" + (i + 1) + "@example.com")
                                .username("owner" + (i + 1))
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("owner123"))
                                .role(Role.PROPIETARIO)
                                .status(true)
                                .fullName("Additional Owner " + (i + 1))
                                .phoneNumber("+57 300 000 000" + i)
                                .build();
                        additionalOwners.add(additionalOwner);
                    }

                    userRepository.saveAll(additionalOwners);
                    userRepository.flush();

                    // Re-query to get all owners including the new ones
                    allUsersInDb = userRepository.findAll();
                    owners = allUsersInDb.stream()
                            .filter(user -> user.getRole() == Role.PROPIETARIO)
                            .toList();

                    System.out.println("After creating additional owners - Total owners: " + owners.size());
                }

                // Create diverse Boats (only as many as we have owners)
                List<BoatEntity> boats = new ArrayList<>();

                // Boat data for dynamic creation
                List<String[]> boatData = Arrays.asList(
                        new String[]{"El Viento del Mar", "Catamaran Cruiser 45", "Cartagena, Colombia", "250000.0", "15000.0"},
                        new String[]{"Estrella Marina", "Luxury Yacht 60", "San Andrés, Colombia", "450000.0", "25000.0"},
                        new String[]{"Ola Perfecta", "Event Catamaran 50", "Santa Marta, Colombia", "320000.0", "18000.0"},
                        new String[]{"Delfín Dorado", "Premium Catamaran 55", "Barranquilla, Colombia", "500000.0", "35000.0"},
                        new String[]{"Brisa del Pacífico", "Touring Cat 40", "Buenaventura, Colombia", "180000.0", "12000.0"},
                        new String[]{"Luna de Cristal", "Floating Hotel 70", "Isla Gorgona, Colombia", "600000.0", "45000.0"},
                        new String[]{"Corona del Mar", "Business Events 55", "Cali, Colombia", "380000.0", "22000.0"},
                        new String[]{"Águila Real", "Elite Catamaran 65", "Medellín, Colombia", "750000.0", "55000.0"},
                        new String[]{"Sirena Cantora", "Eco Tour 35", "Providencia, Colombia", "150000.0", "8000.0"},
                        new String[]{"Palacio Flotante", "Mega Yacht 80", "Bogotá, Colombia", "850000.0", "65000.0"},
                        new String[]{"Tridente Azul", "Sport Fishing 42", "Tolú, Colombia", "220000.0", "14000.0"},
                        new String[]{"Perla Negra", "Racing Catamaran 38", "Puerto Colombia, Colombia", "280000.0", "19000.0"},
                        new String[]{"Reina del Caribe", "Luxury Cruiser 52", "Isla de San Bernardo, Colombia", "420000.0", "28000.0"},
                        new String[]{"Tiburón Blanco", "Diving Support 48", "Capurganá, Colombia", "350000.0", "22000.0"},
                        new String[]{"Gaviota Voladora", "Bird Watching 36", "Ciénaga Grande, Colombia", "160000.0", "9500.0"},
                        new String[]{"Poseidón", "Research Vessel 55", "Gulf of Urabá, Colombia", "480000.0", "32000.0"},
                        new String[]{"Calipso", "Exploration Cat 44", "Archipiélago de San Andrés, Colombia", "290000.0", "17500.0"},
                        new String[]{"Neptuno", "Deep Sea 62", "Tumaco, Colombia", "520000.0", "38000.0"},
                        new String[]{"Ondina", "Eco Adventure 40", "Parque Tayrona, Colombia", "195000.0", "11500.0"},
                        new String[]{"Leviatán", "Expedition 58", "Bahía Solano, Colombia", "460000.0", "29000.0"},
                        new String[]{"María del Mar", "Coastal Cruiser 42", "Puerto Escondido, Colombia", "210000.0", "13500.0"},
                        new String[]{"Espíritu del Viento", "Wind Surf 38", "Rincón del Mar, Colombia", "175000.0", "11000.0"},
                        new String[]{"Costa Azul", "Beach Hopper 35", "Arboletes, Colombia", "145000.0", "8500.0"},
                        new String[]{"Reina del Pacífico", "Ocean Voyager 55", "Turbo, Colombia", "380000.0", "24000.0"},
                        new String[]{"Delfín Plateado", "River Explorer 40", "Río Magdalena, Colombia", "195000.0", "12500.0"},
                        new String[]{"Halcones del Mar", "Speed Cat 45", "Cartagena Bay, Colombia", "320000.0", "20000.0"},
                        new String[]{"Isla Paraíso", "Island Hopper 48", "Isla Fuerte, Colombia", "275000.0", "17500.0"},
                        new String[]{"Tortuga Marina", "Eco Tour 36", "Parque Nacional Tayrona, Colombia", "165000.0", "9500.0"},
                        new String[]{"Cóndor Andino", "Mountain Lake 52", "Embalse del Peñol, Colombia", "340000.0", "22000.0"},
                        new String[]{"Jade Imperial", "Luxury Liner 65", "Bahía de Santa Marta, Colombia", "580000.0", "38000.0"},
                        new String[]{"Cristal del Mar", "Glass Bottom 44", "Islas del Rosario, Colombia", "260000.0", "16500.0"},
                        new String[]{"Furia del Océano", "Storm Chaser 50", "Punta Gallinas, Colombia", "410000.0", "26000.0"},
                        new String[]{"Luna Plateada", "Night Cruiser 46", "Ciénaga de Zapatosa, Colombia", "285000.0", "18000.0"},
                        new String[]{"Rayo del Mar", "Speed Demon 42", "Golfo de Morrosquillo, Colombia", "310000.0", "19500.0"},
                        new String[]{"Perla del Caribe", "Pearl Diver 38", "Archipiélago de San Bernardo, Colombia", "185000.0", "11500.0"},
                        new String[]{"Titán del Abismo", "Deep Explorer 60", "Fosa de Cariaco, Colombia", "520000.0", "34000.0"},
                        new String[]{"Esmeralda Costera", "Coastal Gem 41", "Necoclí, Colombia", "225000.0", "14500.0"},
                        new String[]{"Vikingo Moderno", "Viking Tour 47", "Puerto López, Colombia", "295000.0", "19000.0"},
                        new String[]{"Diamante Negro", "Black Diamond 54", "Isla de Gorgona, Colombia", "450000.0", "29000.0"},
                        new String[]{"Águila Pescadora", "Fishing Eagle 43", "Río Sinú, Colombia", "240000.0", "15500.0"},
                        new String[]{"Zafiro Real", "Royal Sapphire 58", "Cartagena Walled City, Colombia", "480000.0", "31000.0"},
                        new String[]{"Ola Eterna", "Eternal Wave 39", "Playa Blanca, Colombia", "190000.0", "12000.0"},
                        new String[]{"Centurión Marino", "Sea Centurion 51", "Base Naval ARC, Colombia", "360000.0", "23000.0"},
                        new String[]{"Orquídea Marina", "Sea Orchid 37", "Manglares de la Boquilla, Colombia", "155000.0", "9000.0"},
                        new String[]{"Leopardo de Mar", "Sea Leopard 49", "PNN Los Katíos, Colombia", "330000.0", "21000.0"},
                        new String[]{"Rubí Estelar", "Star Ruby 56", "Observatorio Astronómico, Colombia", "420000.0", "27000.0"},
                        new String[]{"Pantera Acuática", "Water Panther 44", "Río Cauca, Colombia", "250000.0", "16000.0"},
                        new String[]{"Diamante Azul", "Blue Diamond 53", "Laguna de Guatavita, Colombia", "390000.0", "25000.0"},
                        new String[]{"Fénix Renacido", "Phoenix Rising 48", "Volcán del Totumo, Colombia", "315000.0", "20000.0"},
                        new String[]{"Tigre Blanco", "White Tiger 46", "Santuario de Fauna, Colombia", "280000.0", "18000.0"}
                );

                // Create boats dynamically based on available owners
                int numBoats = Math.min(owners.size(), boatData.size());
                for (int i = 0; i < numBoats; i++) {
                    String[] data = boatData.get(i);
                    BoatEntity boat = BoatEntity.builder()
                            .type(i % 4 == 0 ? BoatType.TURISMO : i % 4 == 1 ? BoatType.ALOJAMIENTO : i % 4 == 2 ? BoatType.EVENTOS_NEGOCIOS : BoatType.DISENO_EXCLUSIVO)
                            .name(data[0])
                            .model(data[1])
                            .location(data[2])
                            .price(Double.parseDouble(data[3]))
                            .balance(Double.parseDouble(data[4]))
                            .owner(owners.get(i))
                            .build();
                    boats.add(boat);
                }

                System.out.println("Created " + boats.size() + " boats for " + owners.size() + " owners");

                boatRepository.saveAll(boats);
            }

            if (maintananceRepository.count() == 0) {
                // Get boats for maintenance assignment
                List<BoatEntity> boats = boatRepository.findAll();

                // Create diverse Maintenance Records (only as many as we have boats)
                List<MaintananceEntity> maintenances = new ArrayList<>();

                // Maintenance data for dynamic creation
                List<Object[]> maintenanceData = Arrays.asList(
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.MEDIA, "Mantenimiento preventivo general: limpieza de casco, revisión de motores, cambio de filtros", 2500.0, -2, 1},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.ALTA, "Reparación de sistema eléctrico: reemplazo de baterías y cableado dañado", 4500.0, -15, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.BAJA, "Mantenimiento preventivo de rutina: inspección de velas y aparejos", 1200.0, 7, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.CRITICA, "Reparación crítica de motor: reconstrucción completa del motor principal", 15000.0, -1, 3},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.MEDIA, "Mantenimiento preventivo de casco: limpieza y aplicación de antifouling", 3200.0, -10, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.ALTA, "Reparación de sistema de navegación: actualización de GPS y radares", 6800.0, 3, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.BAJA, "Mantenimiento preventivo de interiores: limpieza y restauración de muebles", 1800.0, -3, 2},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.CRITICA, "Reparación crítica de estructura: refuerzo de quilla y reparación de grietas", 25000.0, -5, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.MEDIA, "Mantenimiento preventivo de sistemas: revisión de plomería y electricidad", 2100.0, 14, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.ALTA, "Reparación de sistema de aire acondicionado: reemplazo de compresor y ductos", 5200.0, -3, 5},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.ALTA, "Inspección anual de seguridad: verificación de equipos de emergencia y salvavidas", 3800.0, -7, 1},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.MEDIA, "Reparación de sistema hidráulico: reemplazo de bombas y válvulas", 4200.0, 5, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.BAJA, "Mantenimiento de cubiertas: limpieza y tratamiento de superficies de madera", 1600.0, -12, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.CRITICA, "Reparación de emergencia de timón: reemplazo completo del sistema de dirección", 18500.0, -2, 7},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.MEDIA, "Mantenimiento de equipos electrónicos: actualización de software y calibración", 2900.0, 10, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.ALTA, "Reparación de sistema de combustible: limpieza de tanques y reemplazo de filtros", 5600.0, -8, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.BAJA, "Mantenimiento estacional: preparación para temporada de lluvias", 2100.0, -4, 3},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.CRITICA, "Reparación estructural mayor: refuerzo de casco y reparación de daños por impacto", 32000.0, 2, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.MEDIA, "Mantenimiento de sistemas de seguridad: inspección y prueba de alarmas", 3400.0, -6, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.ALTA, "Reparación de sistema de propulsión: service completo de hélices y ejes", 7800.0, -1, 4},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.MEDIA, "Inspección técnica anual: verificación completa de todos los sistemas", 4200.0, 21, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.BAJA, "Reparación menor de pintura: retoque de rayones en casco", 1800.0, -18, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.ALTA, "Mantenimiento de seguridad: revisión de chalecos salvavidas y extintores", 3100.0, -5, 2},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.CRITICA, "Reparación de vía de agua: sellado de filtración en compartimento", 12500.0, 1, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.MEDIA, "Limpieza profunda de tanques de combustible y agua", 2800.0, -12, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.ALTA, "Reemplazo de sistema de iluminación LED completo", 6500.0, -3, 6},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.BAJA, "Mantenimiento de cubiertas de teca: lijado y barnizado", 3900.0, 28, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.CRITICA, "Reparación de motor fuera de borda: reconstrucción total", 18500.0, -7, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.MEDIA, "Actualización de cartas náuticas y software de navegación", 2200.0, -8, 3},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.ALTA, "Reparación de sistema de gobierno: reemplazo de cilindros hidráulicos", 8900.0, 4, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.BAJA, "Limpieza y mantenimiento de sistemas de ventilación", 1600.0, -14, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.CRITICA, "Reparación estructural por colisión: refuerzo de zona de impacto", 28500.0, -2, 8},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.MEDIA, "Mantenimiento preventivo de generador eléctrico", 3400.0, 18, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.ALTA, "Reparación de sistema de combustible: limpieza de inyectores", 5200.0, -9, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.BAJA, "Mantenimiento de equipos de buceo y snorkel", 1900.0, -6, 4},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.CRITICA, "Reparación mayor de transmisión: reemplazo completo", 22500.0, 2, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.MEDIA, "Inspección y mantenimiento de sistemas contra incendios", 3600.0, -11, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.ALTA, "Reparación de sistema de refrigeración de motores", 7100.0, -4, 7},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.BAJA, "Limpieza y protección de superficies metálicas", 2400.0, 25, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.CRITICA, "Reparación de emergencia de quilla: enderezado y refuerzo", 35200.0, -1, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.MEDIA, "Mantenimiento de sistemas de entretenimiento y audio", 2800.0, -9, 5},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.ALTA, "Reparación de sistema de anclas y cadenas", 4600.0, 6, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.BAJA, "Mantenimiento de cabinas y áreas interiores", 3200.0, -16, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.CRITICA, "Reparación crítica de tanques de combustible: reemplazo completo", 19800.0, -3, 9},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.MEDIA, "Inspección técnica de casco y estructura", 4100.0, 22, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.ALTA, "Reparación de sistema eléctrico auxiliar", 5800.0, -10, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.BAJA, "Mantenimiento de equipos de pesca deportiva", 2100.0, -7, 6},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.PROGRAMADO, MaintenancePriority.CRITICA, "Reparación mayor de sala de máquinas: renovación completa", 42500.0, 3, null},
                        new Object[]{MaintananceType.PREVENTIVO, MaintananceStatus.EN_PROCESO, MaintenancePriority.MEDIA, "Mantenimiento preventivo de sistemas de comunicación", 2900.0, -13, null},
                        new Object[]{MaintananceType.CORRECTIVO, MaintananceStatus.COMPLETADO, MaintenancePriority.ALTA, "Reparación de sistema de dirección asistida", 8600.0, -5, 8}
                );

                // Create maintenance records dynamically based on available boats
                int numMaintenances = Math.min(boats.size(), maintenanceData.size());
                for (int i = 0; i < numMaintenances; i++) {
                    Object[] data = maintenanceData.get(i);
                    MaintananceEntity.MaintananceEntityBuilder builder = MaintananceEntity.builder()
                            .boat(boats.get(i))
                            .type((MaintananceType) data[0])
                            .status((MaintananceStatus) data[1])
                            .priority((MaintenancePriority) data[2])
                            .description((String) data[3])
                            .cost((Double) data[4]);

                    Integer scheduledDays = (Integer) data[5];
                    Integer performedDays = (Integer) data[6];

                    if (scheduledDays != null) {
                        builder.dateScheduled(LocalDateTime.now().plusDays(scheduledDays));
                    }

                    if (performedDays != null) {
                        builder.datePerformed(LocalDateTime.now().plusDays(performedDays));
                    }

                    maintenances.add(builder.build());
                }

                System.out.println("Created " + maintenances.size() + " maintenance records for " + boats.size() + " boats");

                maintananceRepository.saveAll(maintenances);
            }

            if (paymentRepository.count() == 0) {
                // Get boats and maintenances for payment assignment
                List<BoatEntity> boats = boatRepository.findAll();
                List<MaintananceEntity> maintenances = maintananceRepository.findAll();

                // Create diverse Payment Records
                List<PaymentEntity> payments = Arrays.asList(
                        PaymentEntity.builder()
                                .mount(2500.0)
                                .date(LocalDateTime.now().minusMonths(2).plusDays(2))
                                .invoice_url("/uploads/receipts/receipt_001.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(0))
                                .maintanance(maintenances.get(0))
                                .build(),
                        PaymentEntity.builder()
                                .mount(4500.0)
                                .date(LocalDateTime.now().minusDays(10))
                                .invoice_url("/uploads/receipts/receipt_002.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(1))
                                .maintanance(maintenances.get(1))
                                .build(),
                        PaymentEntity.builder()
                                .mount(1200.0)
                                .date(LocalDateTime.now().plusDays(10))
                                .invoice_url("/uploads/receipts/receipt_003.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(2))
                                .maintanance(maintenances.get(2))
                                .build(),
                        PaymentEntity.builder()
                                .mount(15000.0)
                                .date(LocalDateTime.now().minusMonths(1).plusDays(5))
                                .invoice_url("/uploads/receipts/receipt_004.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(3))
                                .maintanance(maintenances.get(3))
                                .build(),
                        PaymentEntity.builder()
                                .mount(3200.0)
                                .date(LocalDateTime.now().minusDays(5))
                                .invoice_url("/uploads/receipts/receipt_005.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(4))
                                .maintanance(maintenances.get(4))
                                .build(),
                        PaymentEntity.builder()
                                .mount(1500.0)
                                .date(LocalDateTime.now().minusDays(20))
                                .invoice_url("/uploads/receipts/receipt_006.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(0))
                                .build(),
                        PaymentEntity.builder()
                                .mount(2500.0)
                                .date(LocalDateTime.now().minusDays(15))
                                .invoice_url("/uploads/receipts/receipt_007.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(1))
                                .build(),
                        PaymentEntity.builder()
                                .mount(1800.0)
                                .date(LocalDateTime.now().minusDays(25))
                                .invoice_url("/uploads/receipts/receipt_008.pdf")
                                .reason(ReasonPayment.PAGO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(2))
                                .build(),
                        PaymentEntity.builder()
                                .mount(3500.0)
                                .date(LocalDateTime.now().minusDays(12))
                                .invoice_url("/uploads/receipts/receipt_009.pdf")
                                .reason(ReasonPayment.PAGO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(3))
                                .build(),
                        PaymentEntity.builder()
                                .mount(1200.0)
                                .date(LocalDateTime.now().minusDays(30))
                                .invoice_url("/uploads/receipts/receipt_010.pdf")
                                .reason(ReasonPayment.PAGO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(4))
                                .build(),
                        PaymentEntity.builder()
                                .mount(3800.0)
                                .date(LocalDateTime.now().minusDays(18))
                                .invoice_url("/uploads/receipts/receipt_011.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(5))
                                .maintanance(maintenances.get(5))
                                .build(),
                        PaymentEntity.builder()
                                .mount(4200.0)
                                .date(LocalDateTime.now().plusDays(8))
                                .invoice_url("/uploads/receipts/receipt_012.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(6))
                                .maintanance(maintenances.get(6))
                                .build(),
                        PaymentEntity.builder()
                                .mount(1600.0)
                                .date(LocalDateTime.now().minusDays(35))
                                .invoice_url("/uploads/receipts/receipt_013.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(7))
                                .maintanance(maintenances.get(7))
                                .build(),
                        PaymentEntity.builder()
                                .mount(18500.0)
                                .date(LocalDateTime.now().minusDays(8))
                                .invoice_url("/uploads/receipts/receipt_014.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(8))
                                .maintanance(maintenances.get(8))
                                .build(),
                        PaymentEntity.builder()
                                .mount(2900.0)
                                .date(LocalDateTime.now().plusDays(15))
                                .invoice_url("/uploads/receipts/receipt_015.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(9))
                                .maintanance(maintenances.get(9))
                                .build(),
                        PaymentEntity.builder()
                                .mount(5600.0)
                                .date(LocalDateTime.now().minusDays(14))
                                .invoice_url("/uploads/receipts/receipt_016.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(0))
                                .maintanance(maintenances.get(10))
                                .build(),
                        PaymentEntity.builder()
                                .mount(2100.0)
                                .date(LocalDateTime.now().minusDays(22))
                                .invoice_url("/uploads/receipts/receipt_017.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(1))
                                .maintanance(maintenances.get(11))
                                .build(),
                        PaymentEntity.builder()
                                .mount(32000.0)
                                .date(LocalDateTime.now().plusDays(5))
                                .invoice_url("/uploads/receipts/receipt_018.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(2))
                                .maintanance(maintenances.get(12))
                                .build(),
                        PaymentEntity.builder()
                                .mount(3400.0)
                                .date(LocalDateTime.now().minusDays(28))
                                .invoice_url("/uploads/receipts/receipt_019.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(3))
                                .maintanance(maintenances.get(13))
                                .build(),
                        PaymentEntity.builder()
                                .mount(7800.0)
                                .date(LocalDateTime.now().minusDays(6))
                                .invoice_url("/uploads/receipts/receipt_020.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(4))
                                .maintanance(maintenances.get(14))
                                .build(),
                        PaymentEntity.builder()
                                .mount(4200.0)
                                .date(LocalDateTime.now().plusDays(25))
                                .invoice_url("/uploads/receipts/receipt_021.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(5))
                                .maintanance(maintenances.get(15))
                                .build(),
                        PaymentEntity.builder()
                                .mount(1800.0)
                                .date(LocalDateTime.now().minusDays(40))
                                .invoice_url("/uploads/receipts/receipt_022.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(6))
                                .maintanance(maintenances.get(16))
                                .build(),
                        PaymentEntity.builder()
                                .mount(3100.0)
                                .date(LocalDateTime.now().minusDays(16))
                                .invoice_url("/uploads/receipts/receipt_023.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(7))
                                .maintanance(maintenances.get(17))
                                .build(),
                        PaymentEntity.builder()
                                .mount(12500.0)
                                .date(LocalDateTime.now().plusDays(3))
                                .invoice_url("/uploads/receipts/receipt_024.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(8))
                                .maintanance(maintenances.get(18))
                                .build(),
                        PaymentEntity.builder()
                                .mount(2800.0)
                                .date(LocalDateTime.now().minusDays(32))
                                .invoice_url("/uploads/receipts/receipt_025.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(9))
                                .maintanance(maintenances.get(19))
                                .build(),
                        PaymentEntity.builder()
                                .mount(6500.0)
                                .date(LocalDateTime.now().minusDays(11))
                                .invoice_url("/uploads/receipts/receipt_026.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(0))
                                .maintanance(maintenances.get(20))
                                .build(),
                        PaymentEntity.builder()
                                .mount(3900.0)
                                .date(LocalDateTime.now().plusDays(32))
                                .invoice_url("/uploads/receipts/receipt_027.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(1))
                                .maintanance(maintenances.get(21))
                                .build(),
                        PaymentEntity.builder()
                                .mount(18500.0)
                                .date(LocalDateTime.now().minusDays(19))
                                .invoice_url("/uploads/receipts/receipt_028.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(2))
                                .maintanance(maintenances.get(22))
                                .build(),
                        PaymentEntity.builder()
                                .mount(2200.0)
                                .date(LocalDateTime.now().minusDays(24))
                                .invoice_url("/uploads/receipts/receipt_029.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(3))
                                .maintanance(maintenances.get(23))
                                .build(),
                        PaymentEntity.builder()
                                .mount(8900.0)
                                .date(LocalDateTime.now().plusDays(12))
                                .invoice_url("/uploads/receipts/receipt_030.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(4))
                                .maintanance(maintenances.get(24))
                                .build(),
                        PaymentEntity.builder()
                                .mount(1600.0)
                                .date(LocalDateTime.now().minusDays(38))
                                .invoice_url("/uploads/receipts/receipt_031.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(5))
                                .maintanance(maintenances.get(25))
                                .build(),
                        PaymentEntity.builder()
                                .mount(28500.0)
                                .date(LocalDateTime.now().minusDays(8))
                                .invoice_url("/uploads/receipts/receipt_032.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(6))
                                .maintanance(maintenances.get(26))
                                .build(),
                        PaymentEntity.builder()
                                .mount(3400.0)
                                .date(LocalDateTime.now().plusDays(22))
                                .invoice_url("/uploads/receipts/receipt_033.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(7))
                                .maintanance(maintenances.get(27))
                                .build(),
                        PaymentEntity.builder()
                                .mount(5200.0)
                                .date(LocalDateTime.now().minusDays(26))
                                .invoice_url("/uploads/receipts/receipt_034.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(8))
                                .maintanance(maintenances.get(28))
                                .build(),
                        PaymentEntity.builder()
                                .mount(1900.0)
                                .date(LocalDateTime.now().minusDays(20))
                                .invoice_url("/uploads/receipts/receipt_035.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(9))
                                .maintanance(maintenances.get(29))
                                .build(),
                        PaymentEntity.builder()
                                .mount(22500.0)
                                .date(LocalDateTime.now().plusDays(8))
                                .invoice_url("/uploads/receipts/receipt_036.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(0))
                                .maintanance(maintenances.get(30))
                                .build(),
                        PaymentEntity.builder()
                                .mount(3600.0)
                                .date(LocalDateTime.now().minusDays(29))
                                .invoice_url("/uploads/receipts/receipt_037.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(1))
                                .maintanance(maintenances.get(31))
                                .build(),
                        PaymentEntity.builder()
                                .mount(7100.0)
                                .date(LocalDateTime.now().minusDays(14))
                                .invoice_url("/uploads/receipts/receipt_038.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(2))
                                .maintanance(maintenances.get(32))
                                .build(),
                        PaymentEntity.builder()
                                .mount(2400.0)
                                .date(LocalDateTime.now().plusDays(29))
                                .invoice_url("/uploads/receipts/receipt_039.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(3))
                                .maintanance(maintenances.get(33))
                                .build(),
                        PaymentEntity.builder()
                                .mount(35200.0)
                                .date(LocalDateTime.now().minusDays(4))
                                .invoice_url("/uploads/receipts/receipt_040.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(4))
                                .maintanance(maintenances.get(34))
                                .build(),
                        PaymentEntity.builder()
                                .mount(2800.0)
                                .date(LocalDateTime.now().minusDays(25))
                                .invoice_url("/uploads/receipts/receipt_041.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(5))
                                .maintanance(maintenances.get(35))
                                .build(),
                        PaymentEntity.builder()
                                .mount(4600.0)
                                .date(LocalDateTime.now().plusDays(14))
                                .invoice_url("/uploads/receipts/receipt_042.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(6))
                                .maintanance(maintenances.get(36))
                                .build(),
                        PaymentEntity.builder()
                                .mount(3200.0)
                                .date(LocalDateTime.now().minusDays(42))
                                .invoice_url("/uploads/receipts/receipt_043.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(7))
                                .maintanance(maintenances.get(37))
                                .build(),
                        PaymentEntity.builder()
                                .mount(19800.0)
                                .date(LocalDateTime.now().minusDays(10))
                                .invoice_url("/uploads/receipts/receipt_044.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(8))
                                .maintanance(maintenances.get(38))
                                .build(),
                        PaymentEntity.builder()
                                .mount(4100.0)
                                .date(LocalDateTime.now().plusDays(26))
                                .invoice_url("/uploads/receipts/receipt_045.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(9))
                                .maintanance(maintenances.get(39))
                                .build(),
                        PaymentEntity.builder()
                                .mount(5800.0)
                                .date(LocalDateTime.now().minusDays(28))
                                .invoice_url("/uploads/receipts/receipt_046.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(0))
                                .maintanance(maintenances.get(40))
                                .build(),
                        PaymentEntity.builder()
                                .mount(2100.0)
                                .date(LocalDateTime.now().minusDays(21))
                                .invoice_url("/uploads/receipts/receipt_047.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(1))
                                .maintanance(maintenances.get(41))
                                .build(),
                        PaymentEntity.builder()
                                .mount(42500.0)
                                .date(LocalDateTime.now().plusDays(9))
                                .invoice_url("/uploads/receipts/receipt_048.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.POR_PAGAR)
                                .boat(boats.get(2))
                                .maintanance(maintenances.get(42))
                                .build(),
                        PaymentEntity.builder()
                                .mount(2900.0)
                                .date(LocalDateTime.now().minusDays(35))
                                .invoice_url("/uploads/receipts/receipt_049.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(3))
                                .maintanance(maintenances.get(43))
                                .build(),
                        PaymentEntity.builder()
                                .mount(8600.0)
                                .date(LocalDateTime.now().minusDays(17))
                                .invoice_url("/uploads/receipts/receipt_050.pdf")
                                .reason(ReasonPayment.MANTENIMIENTO)
                                .status(PaymentStatus.PAGADO)
                                .boat(boats.get(4))
                                .maintanance(maintenances.get(44))
                                .build()
                );

                paymentRepository.saveAll(payments);
            }
        };
    }
}