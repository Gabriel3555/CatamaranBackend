package com.catamaran.catamaranbackend.auth.web;

import com.catamaran.catamaranbackend.auth.application.dto.AuthRequest;
import com.catamaran.catamaranbackend.auth.application.dto.AuthResponse;
import com.catamaran.catamaranbackend.auth.application.dto.ForgotPasswordRequest;
import com.catamaran.catamaranbackend.auth.application.dto.PasswordResetResponse;
import com.catamaran.catamaranbackend.auth.application.dto.ResetPasswordRequest;
import com.catamaran.catamaranbackend.auth.application.port.LoginUseCase;
import com.catamaran.catamaranbackend.auth.application.service.PasswordRecoveryService;
import com.catamaran.catamaranbackend.auth.application.service.UserDetailsServiceImp;
import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.catamaran.catamaranbackend.auth.security.EmailAlreadyExistsException;
import com.catamaran.catamaranbackend.auth.security.UsernameAlreadyExistsException;
import com.catamaran.catamaranbackend.domain.Role;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final UserRepositoryJpa userRepository;
    private final BoatRepository boatRepository;
    private final LoginUseCase loginUseCase;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImp userDetailsService;
    private final PasswordRecoveryService passwordRecoveryService;

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
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserEntity> allUsers = userRepository.findAll(pageable);
        List<UserEntity> propietarios = allUsers.getContent().stream()
                .filter(user -> user.getRole() == Role.PROPIETARIO)
                .collect(Collectors.toList());
        long total = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.PROPIETARIO)
                .count();
        return new PageImpl<>(propietarios, pageable, total);
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserEntity updatedUser
    ) {
        return userRepository.findById(id)
                .map(existing -> {
                    try {
                        // Only update fields that are provided
                        if (updatedUser.getEmail() != null) {
                            existing.setEmail(updatedUser.getEmail());
                        }
                        if (updatedUser.getUsername() != null) {
                            existing.setUsername(updatedUser.getUsername());
                        }
                        if (updatedUser.getFullName() != null) {
                            existing.setFullName(updatedUser.getFullName());
                        }
                        if (updatedUser.getPhoneNumber() != null) {
                            existing.setPhoneNumber(updatedUser.getPhoneNumber());
                        }
                        if (updatedUser.getStatus() != null) {
                            existing.setStatus(updatedUser.getStatus());
                        }

                        return ResponseEntity.ok(userRepository.save(existing));
                    } catch (DataIntegrityViolationException e) {
                        String errorMessage = "El email o username ya existe";
                        if (e.getMessage().contains("users_email_key")) {
                            errorMessage = "El email ya está registrado";
                        } else if (e.getMessage().contains("users_username_key")) {
                            errorMessage = "El username ya está registrado";
                        }
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of("message", errorMessage));
                    }
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
    public ResponseEntity<?> createOwner(@RequestBody UserEntity request) {
        try {
            // Validar formato de email
            String email = request.getEmail();
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "El email es requerido"));
            }

            // Validar formato de email usando expresión regular más estricta
            // Patrón: debe tener al menos un carácter antes del @, un @, y un dominio válido
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            if (!email.trim().matches(emailRegex)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "El formato del email no es válido. Debe tener el formato: ejemplo@dominio.com"));
            }

            // Validar formato de username
            String username = request.getUsername();
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "El username es requerido"));
            }

            // Validar formato de username (solo letras, números, guiones y guiones bajos, entre 3 y 20 caracteres)
            String usernameRegex = "^[a-zA-Z0-9_-]{3,20}$";
            if (!username.trim().matches(usernameRegex)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "El formato del username no es válido. Debe tener entre 3 y 20 caracteres y solo puede contener letras, números, guiones y guiones bajos."));
            }

            // Verificar si el email ya existe
            if (userRepository.findByEmail(email.trim().toLowerCase()).isPresent()) {
                throw new EmailAlreadyExistsException("El email '" + email + "' ya está registrado. Por favor, use otro email.");
            }

            // Verificar si el username ya existe
            if (userRepository.findByUsername(username.trim()).isPresent()) {
                throw new UsernameAlreadyExistsException("El username '" + username + "' ya está registrado. Por favor, use otro username.");
            }

            // Set default encrypted password
            String encryptedPassword = passwordEncoder.encode("owner123");

            UserEntity owner = UserEntity.builder()
                    .email(email.trim().toLowerCase())
                    .username(username.trim())
                    .uniqueId(UUID.randomUUID())
                    .fullName(request.getFullName())
                    .phoneNumber(request.getPhoneNumber())
                    .status(true)
                    .role(Role.PROPIETARIO)
                    .password(encryptedPassword)
                    .build();

            UserEntity savedOwner = userRepository.save(owner);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedOwner);
        } catch (EmailAlreadyExistsException | UsernameAlreadyExistsException e) {
            // Estas excepciones serán manejadas por el GlobalExceptionHandler
            throw e;
        } catch (DataIntegrityViolationException e) {
            String errorMessage = "El email o username ya existe";
            if (e.getMessage().contains("users_email_key")) {
                errorMessage = "El email ya está registrado";
            } else if (e.getMessage().contains("users_username_key")) {
                errorMessage = "El username ya está registrado";
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", errorMessage));
        }
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

    @Operation(
            summary = "Solicitar recuperación de contraseña",
            description = "Envía un correo electrónico con un enlace para restablecer la contraseña"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Solicitud procesada exitosamente",
            content = @Content(schema = @Schema(implementation = PasswordResetResponse.class))
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request
    ) {
        PasswordResetResponse response = passwordRecoveryService.requestPasswordReset(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Restablecer contraseña",
            description = "Restablece la contraseña usando el token recibido por correo electrónico"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Contraseña restablecida exitosamente",
            content = @Content(schema = @Schema(implementation = PasswordResetResponse.class))
    )
    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request
    ) {
        PasswordResetResponse response = passwordRecoveryService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Validar token de recuperación",
            description = "Verifica si un token de recuperación es válido y no ha expirado"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Validación del token",
            content = @Content(schema = @Schema(implementation = PasswordResetResponse.class))
    )
    @GetMapping("/validate-reset-token")
    public ResponseEntity<PasswordResetResponse> validateResetToken(
            @RequestParam String token
    ) {
        PasswordResetResponse response = passwordRecoveryService.validateResetToken(token);
        return ResponseEntity.ok(response);
    }
}