package com.resolvit.core.enums;

/**
 * Complaint priority levels with visual indicators.
 */
public enum Priority {
    LOW("Low", 1, "#4CAF50", "Minor issue, can wait"),
    MEDIUM("Medium", 2, "#FF9800", "Standard priority"),
    HIGH("High", 3, "#F44336", "Urgent, needs attention"),
    CRITICAL("Critical", 4, "#880E4F", "Emergency, immediate action required");

    private final String displayName;
    private final int level;
    private final String hexColor;
    private final String description;

    Priority(String displayName, int level, String hexColor, String description) {
        this.displayName = displayName;
        this.level = level;
        this.hexColor = hexColor;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getHexColor() {
        return hexColor;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get CSS class name for styling priority badges.
     */
    public String getCssClass() {
        return "priority-badge-" + this.name().toLowerCase();
    }

    /**
     * Check if this is a high-priority item (HIGH or CRITICAL).
     */
    public boolean isUrgent() {
        return this == HIGH || this == CRITICAL;
    }

    /**
     * Compare priority levels.
     */
    public boolean isHigherThan(Priority other) {
        return this.level > other.level;
    }

    /**
     * Get priority from string value (case-insensitive).
     */
    public static Priority fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Priority value cannot be null or empty");
        }
        for (Priority priority : values()) {
            if (priority.name().equalsIgnoreCase(value.trim())) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown priority: " + value);
    }

    /**
     * Get priority from level number.
     */
    public static Priority fromLevel(int level) {
        for (Priority priority : values()) {
            if (priority.level == level) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown priority level: " + level);
    }
}
