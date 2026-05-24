package com.resolvit.core.exceptions;

/**
 * Thrown when a user attempts to perform an action they are not authorized for.
 */
public class UnauthorizedAccessException extends RuntimeException {

    private final String userId;
    private final String requiredPermission;

    public UnauthorizedAccessException(String message) {
        super(message);
        this.userId = null;
        this.requiredPermission = null;
    }

    public UnauthorizedAccessException(String message, String userId) {
        super(message);
        this.userId = userId;
        this.requiredPermission = null;
    }

    public UnauthorizedAccessException(String message, String userId, String requiredPermission) {
        super(message);
        this.userId = userId;
        this.requiredPermission = requiredPermission;
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
        this.userId = null;
        this.requiredPermission = null;
    }

    public String getUserId() {
        return userId;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }
}
