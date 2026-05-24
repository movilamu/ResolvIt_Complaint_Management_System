package com.resolvit.core.exceptions;

import com.resolvit.core.enums.Status;

/**
 * Thrown when an invalid status transition is attempted.
 * For example, transitioning from RESOLVED directly to PENDING.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    private final Status fromStatus;
    private final Status toStatus;

    public InvalidStatusTransitionException(String message) {
        super(message);
        this.fromStatus = null;
        this.toStatus = null;
    }

    public InvalidStatusTransitionException(String message, Status fromStatus, Status toStatus) {
        super(message);
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    public InvalidStatusTransitionException(Status fromStatus, Status toStatus) {
        super(String.format("Invalid status transition from %s to %s", 
              fromStatus != null ? fromStatus.name() : "null", 
              toStatus != null ? toStatus.name() : "null"));
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    public InvalidStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
        this.fromStatus = null;
        this.toStatus = null;
    }

    public Status getFromStatus() {
        return fromStatus;
    }

    public Status getToStatus() {
        return toStatus;
    }
}
