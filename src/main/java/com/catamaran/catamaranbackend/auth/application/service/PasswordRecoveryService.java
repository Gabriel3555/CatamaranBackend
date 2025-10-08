package com.catamaran.catamaranbackend.auth.application.service;

import com.catamaran.catamaranbackend.auth.application.dto.ForgotPasswordRequest;
import com.catamaran.catamaranbackend.auth.application.dto.PasswordResetResponse;
import com.catamaran.catamaranbackend.auth.application.dto.ResetPasswordRequest;
import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordRecoveryService {

    private final UserRepositoryJpa userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int TOKEN_EXPIRY_HOURS = 1;

    @Transactional
    public PasswordResetResponse requestPasswordReset(ForgotPasswordRequest request) {
        try {
            UserEntity user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el email: " + request.getEmail()));

            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            
            // Set token and expiry
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
            
            userRepository.save(user);

            // Send email to admin with user information
            String userName = user.getFullName() != null ? user.getFullName() : user.getUsername();
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken, userName);

            log.info("Solicitud de recuperación de contraseña procesada para: {}", request.getEmail());
            
            return new PasswordResetResponse(
                "Se ha enviado un correo electrónico con las instrucciones para restablecer tu contraseña",
                true
            );
        } catch (RuntimeException e) {
            log.error("Error al procesar solicitud de recuperación de contraseña", e);
            // Por seguridad, no revelamos si el email existe o no
            return new PasswordResetResponse(
                "Si el correo electrónico existe en nuestro sistema, recibirás las instrucciones para restablecer tu contraseña",
                true
            );
        }
    }

    @Transactional
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        try {
            UserEntity user = userRepository.findByResetToken(request.getToken())
                    .orElseThrow(() -> new RuntimeException("Token inválido o expirado"));

            // Verify token expiry
            if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("El token ha expirado");
            }

            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            
            // Clear reset token
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            
            userRepository.save(user);

            log.info("Contraseña restablecida exitosamente para el usuario: {}", user.getEmail());
            
            return new PasswordResetResponse(
                "Tu contraseña ha sido restablecida exitosamente. Ya puedes iniciar sesión con tu nueva contraseña",
                true
            );
        } catch (RuntimeException e) {
            log.error("Error al restablecer contraseña", e);
            return new PasswordResetResponse(
                "Error al restablecer la contraseña. El token puede ser inválido o haber expirado",
                false
            );
        }
    }

    public PasswordResetResponse validateResetToken(String token) {
        try {
            UserEntity user = userRepository.findByResetToken(token)
                    .orElseThrow(() -> new RuntimeException("Token inválido"));

            if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Token expirado");
            }

            return new PasswordResetResponse("Token válido", true);
        } catch (RuntimeException e) {
            log.error("Token de recuperación inválido o expirado", e);
            return new PasswordResetResponse("Token inválido o expirado", false);
        }
    }
}