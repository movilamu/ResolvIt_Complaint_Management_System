package com.resolvit.core.entities;

import com.resolvit.core.enums.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SuperAdmin entity representing a super administrator with full system access.
 * SuperAdmins can manage users, departments, audit logs, and all system settings.
 */
public class SuperAdmin extends Admin {

    /**
     * Default constructor.
     */
    public SuperAdmin() {
        super();
        setRole(Role.SUPERADMIN);
    }

    /**
     * Constructor with basic fields.
     */
    public SuperAdmin(String userId, String name, String email, String passwordHash) {
        super(userId, name, email, passwordHash);
        setRole(Role.SUPERADMIN);
    }

    /**
     * Full constructor.
     */
    public SuperAdmin(String userId, String name, String email, String passwordHash,
                      boolean isActive, String phone, String profilePic,
                      LocalDateTime lastLogin, LocalDateTime createdAt, LocalDateTime updatedAt,
                      Department department, int totalResolved) {
        super(userId, name, email, passwordHash, isActive, phone, profilePic,
              lastLogin, createdAt, updatedAt, department, totalResolved);
        setRole(Role.SUPERADMIN);
    }

    // ═══════════════════════════════════════════════════════════════
    // Override Methods
    // ═══════════════════════════════════════════════════════════════

    @Override
    public String getDashboardTitle() {
        return "System Administration";
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome back, Super Admin " + getFirstName() + "!";
    }

    @Override
    public List<String> getPermissions() {
        // SuperAdmin inherits all Admin permissions and adds more
        List<String> permissions = new ArrayList<>(super.getPermissions());
        permissions.addAll(List.of(
            "MANAGE_USERS",
            "CREATE_ADMIN",
            "DEACTIVATE_USER",
            "MANAGE_DEPARTMENTS",
            "CREATE_DEPARTMENT",
            "DELETE_DEPARTMENT",
            "VIEW_AUDIT_LOGS",
            "SYSTEM_SETTINGS",
            "DELETE_COMPLAINTS",
            "EXPORT_ALL_DATA",
            "VIEW_ALL_USERS",
            "RESET_PASSWORD",
            "MANAGE_CATEGORIES",
            "DATABASE_BACKUP"
        ));
        return permissions;
    }

    @Override
    public String getHomeScreen() {
        return "admin_panel.fxml"; // SuperAdmin uses same panel with more options
    }

    // ═══════════════════════════════════════════════════════════════
    // SuperAdmin-Specific Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get super admin info display string.
     */
    @Override
    public String getAdminInfo() {
        return "Super Administrator | Full System Access";
    }

    /**
     * Check if this user can perform dangerous operations.
     */
    public boolean canPerformDangerousOperations() {
        return true;
    }

    @Override
    public String toString() {
        return "SuperAdmin{" +
                "userId='" + getUserId() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", isActive=" + isActive() +
                '}';
    }
}
