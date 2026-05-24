package com.resolvit.core.enums;

/**
 * Complaint categories for routing to appropriate departments.
 */
public enum Category {
    HOSTEL("Hostel", "🏠", "Hostel-related issues like rooms, facilities, WiFi"),
    ACADEMICS("Academics", "📚", "Academic issues like courses, exams, faculty"),
    IT("IT", "💻", "IT issues like network, software, hardware"),
    TRANSPORT("Transport", "🚌", "Transport issues like buses, routes, timing"),
    CANTEEN("Canteen", "🍽️", "Canteen issues like food quality, hygiene, pricing"),
    OTHER("Other", "📋", "General issues not fitting other categories");

    private final String displayName;
    private final String icon;
    private final String description;

    Category(String displayName, String icon, String description) {
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
     * Get CSS class name for styling category badges.
     */
    public String getCssClass() {
        return "category-badge-" + this.name().toLowerCase();
    }

    /**
     * Get category from string value (case-insensitive).
     */
    public static Category fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Category value cannot be null or empty");
        }
        for (Category category : values()) {
            if (category.name().equalsIgnoreCase(value.trim())) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + value);
    }

    /**
     * Get suggested department ID for this category.
     * Maps to default department assignments.
     */
    public int getSuggestedDepartmentId() {
        return switch (this) {
            case HOSTEL -> 1;     // Hostel Management
            case ACADEMICS -> 2;  // Academic Affairs
            case IT -> 3;         // IT Department
            case TRANSPORT -> 4;  // Transport
            case CANTEEN -> 5;    // Canteen & Facilities
            case OTHER -> 6;      // General Affairs
        };
    }
}
