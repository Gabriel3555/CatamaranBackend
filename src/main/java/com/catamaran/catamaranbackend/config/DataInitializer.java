package com.catamaran.catamaranbackend.config;

import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.catamaran.catamaranbackend.domain.Role;
import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

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
                UserEntity admin = UserEntity.builder()
                        .email("admin1@example.com")
                        .username("admin")
                        .uniqueId(UUID.randomUUID())
                        .password(passwordEncoder.encode("admin123")) // 👈 encriptada
                        .role(Role.ADMIN)
                        .status(true)
                        .fullName("Admin User")
                        .phoneNumber("123456789")
                        .build();

                userRepository.save(admin);

                UserEntity propietario = UserEntity.builder()
                        .email("owner1@example.com")
                        .username("owner")
                        .uniqueId(UUID.randomUUID())
                        .password(passwordEncoder.encode("owner123"))
                        .role(Role.PROPIETARIO)
                        .status(true)
                        .fullName("Owner User")
                        .phoneNumber("987654321")
                        .build();

                userRepository.save(propietario);

                System.out.println("✅ Usuarios iniciales creados: admin1@example.com / owner1@example.com");
            }
        };
    }
}