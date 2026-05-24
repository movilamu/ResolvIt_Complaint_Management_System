package com.resolvit.core.exceptions;

/**
 * Thrown when a user's session has expired or is invalid.
 */
public class SessionExpiredException extends RuntimeException {

    private final String sessionToken;

    public SessionExpiredException(String message) {
        super(message);
        this.sessionToken = null;
    }

    public SessionExpiredException(String message, String sessionToken) {
        super(message);
        this.sessionToken = sessionToken;
    }

    public SessionExpiredException(String message, Throwable cause) {
        super(message, cause);
        this.sessionToken = null;
    }

    public String getSessionToken() {
        return sessionToken;
    }
}
