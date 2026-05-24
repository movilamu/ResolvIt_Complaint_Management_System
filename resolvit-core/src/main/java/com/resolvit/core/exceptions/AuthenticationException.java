package com.resolvit.core.exceptions;

/**
 * Thrown when authentication fails (invalid credentials).
 */
public class AuthenticationException extends RuntimeException {

    private final String email;

    public AuthenticationException(String message) {
        super(message);
        this.email = null;
    }

    public AuthenticationException(String message, String email) {
        super(message);
        this.email = email;
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.email = null;
    }

    public String getEmail() {
        return email;
    }
}
