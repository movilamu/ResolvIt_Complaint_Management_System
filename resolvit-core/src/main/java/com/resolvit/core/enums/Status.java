package com.resolvit.core.enums;

/**
 * Complaint status values with display properties.
 * Supports state machine transitions.
 */
public enum Status {
    PENDING("Pending", "#FFA000", "Awaiting review"),
    IN_PROGRESS("In Progress", "#1565C0", "Being worked on"),
    RESOLVED("Resolved", "#2E7D32", "Issue has been fixed"),
    REJECTED("Rejected", "#B71C1C", "Complaint was declined"),
    REOPENED("Reopened", "#6A1B9A", "Issue resurfaced");

    private final String displayName;
    private final String hexColor;
    private final String description;

    Status(String displayName, String hexColor, String description) {
        this.displayName = displayName;
        this.hexColor = hexColor;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getHexColor() {
        return hexColor;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get CSS class name for styling status badges.
     */
    public String getCssClass() {
        return "status-badge-" + this.name().toLowerCase().replace("_", "-");
    }

    /**
     * Check if this is a terminal status (no more transitions possible by default).
     */
    public boolean isTerminal() {
        return this == RESOLVED || this == REJECTED;
    }

    /**
     * Check if this status indicates the complaint is active/open.
     */
    public boolean isActive() {
        return this == PENDING || this == IN_PROGRESS || this == REOPENED;
    }

    /**
     * Get status from string value (case-insensitive).
     */
    public static Status fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Status value cannot be null or empty");
        }
        for (Status status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
