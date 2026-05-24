package com.resolvit.core.exceptions;

/**
 * Thrown when attempting to create a duplicate entry (e.g., duplicate email).
 */
public class DuplicateEntryException extends RuntimeException {

    private final String field;
    private final String value;

    public DuplicateEntryException(String message) {
        super(message);
        this.field = null;
        this.value = null;
    }

    public DuplicateEntryException(String message, String field, String value) {
        super(message);
        this.field = field;
        this.value = value;
    }

    public DuplicateEntryException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.value = null;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
