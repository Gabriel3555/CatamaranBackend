package com.catamaran.catamaranbackend.auth.security;

/**
 * Excepción personalizada para cuando un email ya está registrado
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

