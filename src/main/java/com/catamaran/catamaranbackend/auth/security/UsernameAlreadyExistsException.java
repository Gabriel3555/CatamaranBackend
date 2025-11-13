package com.catamaran.catamaranbackend.auth.security;

/**
 * Excepción personalizada para cuando un username ya está registrado
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

    public UsernameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

