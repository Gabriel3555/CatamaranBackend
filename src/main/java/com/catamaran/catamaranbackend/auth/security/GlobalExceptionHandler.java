package com.catamaran.catamaranbackend.auth.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la aplicación
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de credenciales inválidas personalizadas
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentialsException(
            InvalidCredentialsException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }

    /**
     * Maneja excepciones de usuario no encontrado personalizadas
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }

    /**
     * Maneja excepciones de usuario inactivo personalizadas
     */
    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<Map<String, Object>> handleUserInactiveException(
            UserInactiveException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, "USER_INACTIVE");
    }

    /**
     * Maneja excepciones de email ya existente
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS");
    }

    /**
     * Maneja excepciones de username ya existente
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameAlreadyExistsException(
            UsernameAlreadyExistsException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, "USERNAME_ALREADY_EXISTS");
    }

    /**
     * Maneja excepciones de credenciales inválidas de Spring Security
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        return buildErrorResponse("Credenciales inválidas. Verifica tu usuario y contraseña.",
                HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }

    /**
     * Maneja excepciones de usuario no encontrado de Spring Security
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFoundException(
            UsernameNotFoundException ex, WebRequest request) {
        return buildErrorResponse("Usuario no encontrado.", HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }

    /**
     * Maneja excepciones generales
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        // Log the full stack trace
        ex.printStackTrace();

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Error interno del servidor: " + ex.getMessage());
        errorResponse.put("errorCode", "INTERNAL_ERROR");
        errorResponse.put("status", 500);
        errorResponse.put("timestamp", LocalDateTime.now());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Construye una respuesta de error consistente
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status, String errorCode) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("status", status.value());
        errorResponse.put("timestamp", LocalDateTime.now());

        return new ResponseEntity<>(errorResponse, status);
    }
}