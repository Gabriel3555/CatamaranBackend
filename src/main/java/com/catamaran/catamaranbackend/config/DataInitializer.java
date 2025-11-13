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
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() == 0) {
                // Create Admin Users
                List<UserEntity> admins = Arrays.asList(
                        UserEntity.builder()
                                .email("admin@alianzacarrocera.com")
                                .username("javier")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Javier Albarracin")
                                .phoneNumber("+57 300 123 4567")
                                .build(),
                        UserEntity.builder()
                                .email("admin2@alianzacarrocera.com")
                                .username("maria")
                                .uniqueId(UUID.randomUUID())
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .status(true)
                                .fullName("Maria Antonieta")
                                .phoneNumber("+57 301 234 5678")
                                .build()
                );

                userRepository.saveAll(admins);
            }
        };
    }
}