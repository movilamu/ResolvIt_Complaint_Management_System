package com.resolvit.core.exceptions;

/**
 * Thrown when a requested complaint cannot be found in the system.
 */
public class ComplaintNotFoundException extends RuntimeException {

    private final String complaintId;

    public ComplaintNotFoundException(String message) {
        super(message);
        this.complaintId = null;
    }

    public ComplaintNotFoundException(String message, String complaintId) {
        super(message);
        this.complaintId = complaintId;
    }

    public ComplaintNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.complaintId = null;
    }

    public ComplaintNotFoundException(String message, String complaintId, Throwable cause) {
        super(message, cause);
        this.complaintId = complaintId;
    }

    public String getComplaintId() {
        return complaintId;
    }
}
