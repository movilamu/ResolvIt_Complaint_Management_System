package com.resolvit.core.enums;

/**
 * Notification types for categorizing user notifications.
 */
public enum NotificationType {
    STATUS_UPDATE("Status Update", "🔄", "Complaint status has changed"),
    ASSIGNMENT("Assignment", "📋", "Complaint has been assigned"),
    REMINDER("Reminder", "⏰", "Reminder notification"),
    SYSTEM("System", "⚙️", "System notification");

    private final String displayName;
    private final String icon;
    private final String description;

    NotificationType(String displayName, String icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get display text with icon.
     */
    public String getDisplayWithIcon() {
        return icon + " " + displayName;
    }

    /**
     * Get notification type from string value (case-insensitive).
     */
    public static NotificationType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("NotificationType value cannot be null or empty");
        }
        for (NotificationType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type: " + value);
    }
}
