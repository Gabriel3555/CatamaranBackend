# Configuración de Recuperación de Contraseña por Email

Este documento explica cómo configurar y usar el sistema de recuperación de contraseña mediante correo electrónico en la aplicación Catamaran Backend.

## 📋 Características Implementadas

- ✅ Solicitud de recuperación de contraseña por email
- ✅ Generación de tokens seguros con expiración (1 hora)
- ✅ Envío de emails HTML profesionales
- ✅ Validación de tokens
- ✅ Restablecimiento seguro de contraseña
- ✅ Interfaz web completa (forgot-password.html y reset-password.html)
- ✅ Integración con el login existente

## 🏗️ Arquitectura

### Componentes Creados

1. **Entidad de Usuario Actualizada** (`UserEntity.java`)
   - Campo `resetToken`: Token único para recuperación
   - Campo `resetTokenExpiry`: Fecha de expiración del token

2. **DTOs**
   - `ForgotPasswordRequest`: Solicitud de recuperación (email)
   - `ResetPasswordRequest`: Restablecimiento (token + nueva contraseña)
   - `PasswordResetResponse`: Respuesta estándar

3. **Servicios**
   - `EmailService`: Envío de correos electrónicos
   - `PasswordRecoveryService`: Lógica de negocio de recuperación

4. **Endpoints REST** (en `AuthController`)
   - `POST /api/v1/auth/forgot-password`: Solicitar recuperación
   - `POST /api/v1/auth/reset-password`: Restablecer contraseña
   - `GET /api/v1/auth/validate-reset-token`: Validar token

5. **Páginas Web**
   - `/forgot-password.html`: Formulario de solicitud
   - `/reset-password.html`: Formulario de restablecimiento
   - `/login.html`: Actualizado con enlaces de recuperación

## ⚙️ Configuración

### 1. Configurar Gmail para Envío de Emails

#### Opción A: Usar Gmail con App Password (Recomendado)

1. Ve a tu cuenta de Google: https://myaccount.google.com/
2. Navega a **Seguridad** → **Verificación en dos pasos** (actívala si no está activa)
3. Busca **Contraseñas de aplicaciones**
4. Genera una nueva contraseña de aplicación para "Correo"
5. Copia la contraseña generada (16 caracteres)

#### Opción B: Usar otro proveedor SMTP

Puedes usar otros proveedores como:
- **Outlook/Hotmail**: smtp.office365.com:587
- **Yahoo**: smtp.mail.yahoo.com:587
- **SendGrid**: smtp.sendgrid.net:587
- **Mailgun**: smtp.mailgun.org:587

### 2. Actualizar application.properties

Edita `src/main/resources/application.properties`:

```properties
# Email configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-app-password-de-16-caracteres
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# Application URL (para enlaces en emails)
app.frontend.url=http://localhost:8080
```

**Importante**: 
- Reemplaza `tu-email@gmail.com` con tu email real
- Reemplaza `tu-app-password-de-16-caracteres` con la contraseña de aplicación generada
- En producción, cambia `app.frontend.url` a tu dominio real (ej: https://catamaran.com)

### 3. Variables de Entorno (Producción)

Para mayor seguridad en producción, usa variables de entorno:

```properties
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
app.frontend.url=${FRONTEND_URL}
```

Luego configura las variables en tu servidor:
```bash
export MAIL_USERNAME=tu-email@gmail.com
export MAIL_PASSWORD=tu-app-password
export FRONTEND_URL=https://tu-dominio.com
```

## 🚀 Uso del Sistema

### Flujo Completo

1. **Usuario olvida su contraseña**
   - Va a `/login.html`
   - Hace clic en "¿Olvidaste tu contraseña? Recuperar contraseña"
   - Es redirigido a `/forgot-password.html`

2. **Solicitud de recuperación**
   - Usuario ingresa su email
   - Sistema genera token único
   - Se envía email con enlace de recuperación
   - Enlace válido por 1 hora

3. **Restablecimiento de contraseña**
   - Usuario hace clic en el enlace del email
   - Es redirigido a `/reset-password.html?token=xxx`
   - Sistema valida el token
   - Usuario ingresa nueva contraseña
   - Contraseña es actualizada
   - Usuario es redirigido al login

### Endpoints API

#### 1. Solicitar Recuperación de Contraseña

```bash
POST /api/v1/auth/forgot-password
Content-Type: application/json

{
  "email": "usuario@ejemplo.com"
}
```

**Respuesta exitosa:**
```json
{
  "message": "Se ha enviado un correo electrónico con las instrucciones para restablecer tu contraseña",
  "success": true
}
```

#### 2. Validar Token

```bash
GET /api/v1/auth/validate-reset-token?token=abc123...
```

**Respuesta:**
```json
{
  "message": "Token válido",
  "success": true
}
```

#### 3. Restablecer Contraseña

```bash
POST /api/v1/auth/reset-password
Content-Type: application/json

{
  "token": "abc123...",
  "newPassword": "nuevaContraseña123"
}
```

**Respuesta exitosa:**
```json
{
  "message": "Tu contraseña ha sido restablecida exitosamente. Ya puedes iniciar sesión con tu nueva contraseña",
  "success": true
}
```

## 🔒 Seguridad

### Medidas Implementadas

1. **Tokens únicos**: Cada solicitud genera un UUID único
2. **Expiración**: Los tokens expiran en 1 hora
3. **Un solo uso**: El token se elimina después de usarse
4. **Contraseñas encriptadas**: Usando BCrypt
5. **Validación de email**: Solo emails válidos
6. **Respuestas genéricas**: No se revela si un email existe o no
7. **HTTPS recomendado**: Para producción

### Recomendaciones Adicionales

- Implementar rate limiting en los endpoints
- Agregar CAPTCHA en el formulario de recuperación
- Registrar intentos de recuperación en logs
- Notificar al usuario cuando se cambia su contraseña
- Considerar autenticación de dos factores (2FA)

## 🧪 Pruebas

### Prueba Local

1. Inicia la aplicación:
```bash
./mvnw spring-boot:run
```

2. Abre el navegador en `http://localhost:8080/login.html`

3. Haz clic en "Recuperar contraseña"

4. Ingresa un email registrado en el sistema

5. Revisa tu bandeja de entrada

6. Haz clic en el enlace del email

7. Ingresa una nueva contraseña

8. Inicia sesión con la nueva contraseña

### Verificar Configuración de Email

Puedes probar el envío de emails con este código de prueba:

```java
@Autowired
private EmailService emailService;

@GetMapping("/test-email")
public String testEmail() {
    emailService.sendPasswordResetEmail(
        "destinatario@ejemplo.com",
        "test-token-123",
        "Usuario de Prueba"
    );
    return "Email enviado";
}
```

## 🐛 Solución de Problemas

### Error: "Authentication failed"

**Causa**: Credenciales incorrectas o App Password no configurada

**Solución**:
1. Verifica que usas una App Password, no tu contraseña normal de Gmail
2. Asegúrate de que la verificación en dos pasos esté activa
3. Regenera la App Password si es necesario

### Error: "Connection timeout"

**Causa**: Firewall o puerto bloqueado

**Solución**:
1. Verifica que el puerto 587 esté abierto
2. Intenta con el puerto 465 (SSL) si 587 no funciona
3. Verifica la configuración de tu firewall

### Email no llega

**Posibles causas**:
1. Email en carpeta de spam
2. Email no existe en el sistema
3. Configuración SMTP incorrecta

**Solución**:
1. Revisa la carpeta de spam
2. Verifica los logs de la aplicación
3. Prueba con el endpoint de test

### Token expirado

**Causa**: Han pasado más de 1 hora desde la solicitud

**Solución**:
1. Solicita un nuevo enlace de recuperación
2. Ajusta el tiempo de expiración si es necesario (en `PasswordRecoveryService.java`)

## 📝 Personalización

### Cambiar Tiempo de Expiración

En `PasswordRecoveryService.java`:

```java
private static final int TOKEN_EXPIRY_HOURS = 2; // Cambiar a 2 horas
```

### Personalizar Email Template

Edita el método `buildPasswordResetEmailTemplate()` en `EmailService.java` para cambiar:
- Colores y estilos
- Texto del mensaje
- Logo de la empresa
- Información de contacto

### Cambiar URL del Frontend

En `application.properties`:

```properties
app.frontend.url=https://tu-dominio.com
```

## 📚 Documentación API (Swagger)

Los endpoints están documentados en Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

Busca la sección "Auth" para ver todos los endpoints de autenticación y recuperación de contraseña.

## 🎯 Próximos Pasos Recomendados

1. Implementar rate limiting
2. Agregar logs de auditoría
3. Implementar notificaciones de cambio de contraseña
4. Agregar CAPTCHA
5. Implementar 2FA
6. Crear tests unitarios e integración
7. Agregar métricas y monitoreo

## 📞 Soporte

Para problemas o preguntas, contacta al equipo de desarrollo.