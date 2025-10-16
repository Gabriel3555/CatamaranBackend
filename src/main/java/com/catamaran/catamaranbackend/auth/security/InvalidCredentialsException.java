package com.catamaran.catamaranbackend.auth.security;

/**
 * Excepción personalizada para credenciales inválidas
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}