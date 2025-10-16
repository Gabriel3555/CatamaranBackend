package com.catamaran.catamaranbackend.auth.security;

/**
 * Excepción personalizada para usuario no encontrado
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}