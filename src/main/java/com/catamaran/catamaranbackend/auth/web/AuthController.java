package com.catamaran.catamaranbackend.auth.web;

import com.catamaran.catamaranbackend.auth.application.dto.AuthRequest;
import com.catamaran.catamaranbackend.auth.application.dto.AuthResponse;
import com.catamaran.catamaranbackend.auth.application.port.LoginUseCase;
import com.catamaran.catamaranbackend.auth.application.service.UserDetailsServiceImp;
import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.catamaran.catamaranbackend.domain.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final UserRepositoryJpa userRepository;
    private final LoginUseCase loginUseCase;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImp  userDetailsService;

    @Operation(
            summary = "Sign in",
            description = "Validates credentials and returns an access token and a refresh token"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successful login",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
    )
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    AuthResponse authenticate(@RequestBody @Valid AuthRequest authRequest) {
        return loginUseCase.login(authRequest);
    }

    @GetMapping
    public Page<UserEntity> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size,  Sort.by("id").descending());
        return userRepository.findAllByRole(Role.PROPIETARIO, pageable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateUser(
            @PathVariable Long id,
            @RequestBody UserEntity updatedUser
    ) {
        return userRepository.findById(id)
                .map(existing -> {
                    existing.setEmail(updatedUser.getEmail());
                    existing.setUsername(updatedUser.getUsername());
                    existing.setFullName(updatedUser.getFullName());
                    existing.setPhoneNumber(updatedUser.getPhoneNumber());
                    existing.setRole(updatedUser.getRole());
                    existing.setStatus(updatedUser.getStatus());
                    return ResponseEntity.ok(userRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/create-owner")
    public ResponseEntity<Void> createOwner(@RequestBody UserEntity request) {
        // Set default encrypted password
        String encryptedPassword = passwordEncoder.encode("owner123");

        UserEntity owner = UserEntity.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .uniqueId(UUID.randomUUID())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .status(true)
                .role(Role.PROPIETARIO)
                .password(encryptedPassword)
                .build();

        userRepository.save(owner);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody String newPassword
    ) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}