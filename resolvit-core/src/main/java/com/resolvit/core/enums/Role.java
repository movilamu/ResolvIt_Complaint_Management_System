package com.resolvit.core.enums;

/**
 * User roles in the ResolvIt system.
 * Determines permissions and accessible features.
 */
public enum Role {
    STUDENT("Student", "Standard user who can submit and track complaints"),
    ADMIN("Administrator", "Department admin who can manage complaints"),
    SUPERADMIN("Super Administrator", "System admin with full access");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this role has admin privileges.
     */
    public boolean isAdmin() {
        return this == ADMIN || this == SUPERADMIN;
    }

    /**
     * Check if this role is the super admin.
     */
    public boolean isSuperAdmin() {
        return this == SUPERADMIN;
    }

    /**
     * Get role from string value (case-insensitive).
     */
    public static Role fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role value cannot be null or empty");
        }
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
