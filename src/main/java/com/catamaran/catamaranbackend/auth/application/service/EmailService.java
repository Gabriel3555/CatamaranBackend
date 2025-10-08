package com.catamaran.catamaranbackend.auth.application.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Value("${app.admin.email:gabrielbarrantes45@gmail.com}")
    private String adminEmail;

    public void sendPasswordResetEmail(String userEmail, String resetToken, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail); // Send to admin instead of user
            helper.setSubject("Solicitud de Recuperación de Contraseña - Catamaran");

            String resetLink = frontendUrl + "/reset-password.html?token=" + resetToken;
            
            String htmlContent = buildPasswordResetEmailTemplate(userName, userEmail, resetLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de recuperación de contraseña enviado al administrador para el usuario: {}", userEmail);
        } catch (MessagingException e) {
            log.error("Error al enviar email de recuperación de contraseña para el usuario: {}", userEmail, e);
            throw new RuntimeException("Error al enviar el email de recuperación", e);
        }
    }

    private String buildPasswordResetEmailTemplate(String userName, String userEmail, String resetLink) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Recuperación de Contraseña</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td align="center" style="padding: 40px 0;">
                            <table role="presentation" style="width: 600px; border-collapse: collapse; background-color: #ffffff; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
                                <!-- Header -->
                                <tr>
                                    <td style="padding: 40px 30px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); text-align: center;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: bold;">Catamaran</h1>
                                        <p style="margin: 10px 0 0 0; color: #ffffff; font-size: 16px;">Sistema de Gestión</p>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="margin: 0 0 20px 0; color: #333333; font-size: 24px;">Solicitud de Recuperación de Contraseña</h2>
                                        <div style="margin: 0 0 20px 0; padding: 15px; background-color: #e3f2fd; border-left: 4px solid #2196f3; border-radius: 4px;">
                                            <p style="margin: 0 0 10px 0; color: #1565c0; font-size: 16px; font-weight: bold;">
                                                Información del Usuario:
                                            </p>
                                            <p style="margin: 0 0 5px 0; color: #424242; font-size: 15px;">
                                                <strong>Nombre:</strong> %s
                                            </p>
                                            <p style="margin: 0; color: #424242; font-size: 15px;">
                                                <strong>Email:</strong> %s
                                            </p>
                                        </div>
                                        <p style="margin: 0 0 20px 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                            Se ha recibido una solicitud para restablecer la contraseña de este usuario.
                                        </p>
                                        <p style="margin: 0 0 30px 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                            Para restablecer la contraseña, haz clic en el siguiente botón:
                                        </p>
                                        
                                        <!-- Button -->
                                        <table role="presentation" style="margin: 0 auto;">
                                            <tr>
                                                <td style="border-radius: 4px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);">
                                                    <a href="%s" target="_blank" style="display: inline-block; padding: 16px 36px; font-size: 16px; color: #ffffff; text-decoration: none; border-radius: 4px; font-weight: bold;">
                                                        Restablecer Contraseña
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <p style="margin: 30px 0 20px 0; color: #666666; font-size: 14px; line-height: 1.6;">
                                            O copia y pega el siguiente enlace en tu navegador:
                                        </p>
                                        <p style="margin: 0 0 20px 0; color: #667eea; font-size: 14px; word-break: break-all;">
                                            %s
                                        </p>
                                        
                                        <div style="margin: 30px 0; padding: 20px; background-color: #fff3cd; border-left: 4px solid #ffc107; border-radius: 4px;">
                                            <p style="margin: 0; color: #856404; font-size: 14px; line-height: 1.6;">
                                                <strong>⚠️ Importante:</strong> Este enlace expirará en 1 hora por razones de seguridad.
                                            </p>
                                        </div>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="padding: 30px; background-color: #f8f9fa; text-align: center; border-top: 1px solid #e9ecef;">
                                        <p style="margin: 0 0 10px 0; color: #6c757d; font-size: 14px;">
                                            Este es un correo automático, por favor no respondas a este mensaje.
                                        </p>
                                        <p style="margin: 0; color: #6c757d; font-size: 14px;">
                                            © 2025 Catamaran. Todos los derechos reservados.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(userName, userEmail, resetLink, resetLink);
    }
}