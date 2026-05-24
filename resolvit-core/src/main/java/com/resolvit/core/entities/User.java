package com.resolvit.core.entities;

import com.resolvit.core.enums.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for all user types in the ResolvIt system.
 * Implements common user properties and defines abstract methods for polymorphism.
 */
public abstract class User {

    private String userId;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean isActive;
    private String phone;
    private String profilePic;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor.
     */
    protected User() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Full-argument constructor.
     */
    protected User(String userId, String name, String email, String passwordHash,
                   Role role, boolean isActive, String phone, String profilePic,
                   LocalDateTime lastLogin, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive;
        this.phone = phone;
        this.profilePic = profilePic;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ═══════════════════════════════════════════════════════════════
    // Abstract Methods - Must be implemented by subclasses
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get the title to display on the user's dashboard.
     */
    public abstract String getDashboardTitle();

    /**
     * Get a personalized welcome message for the user.
     */
    public abstract String getWelcomeMessage();

    /**
     * Get the list of permissions available to this user type.
     */
    public abstract List<String> getPermissions();

    /**
     * Get the FXML file name for the user's home screen.
     */
    public abstract String getHomeScreen();

    // ═══════════════════════════════════════════════════════════════
    // Common Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Check if the user has a specific permission.
     */
    public boolean hasPermission(String permission) {
        return getPermissions().contains(permission);
    }

    /**
     * Get the user's first name.
     */
    public String getFirstName() {
        if (name == null || name.isBlank()) {
            return "";
        }
        String[] parts = name.trim().split("\\s+");
        return parts[0];
    }

    /**
     * Get initials for avatar display.
     */
    public String getInitials() {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }

    // ═══════════════════════════════════════════════════════════════
    // Getters and Setters
    // ═══════════════════════════════════════════════════════════════

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        this.updatedAt = LocalDateTime.now();
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ═══════════════════════════════════════════════════════════════
    // Object Methods
    // ═══════════════════════════════════════════════════════════════

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                '}';
    }
}
