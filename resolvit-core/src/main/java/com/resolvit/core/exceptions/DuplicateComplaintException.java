package com.resolvit.core.exceptions;

/**
 * Thrown when a user attempts to submit a duplicate complaint
 * (same title within a specified time window).
 */
public class DuplicateComplaintException extends RuntimeException {

    private final String existingComplaintId;

    public DuplicateComplaintException(String message) {
        super(message);
        this.existingComplaintId = null;
    }

    public DuplicateComplaintException(String message, String existingComplaintId) {
        super(message);
        this.existingComplaintId = existingComplaintId;
    }

    public DuplicateComplaintException(String message, Throwable cause) {
        super(message, cause);
        this.existingComplaintId = null;
    }

    public String getExistingComplaintId() {
        return existingComplaintId;
    }
}
