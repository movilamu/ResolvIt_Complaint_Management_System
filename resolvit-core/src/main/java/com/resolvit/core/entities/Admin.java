package com.resolvit.core.entities;

import com.resolvit.core.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin entity representing an administrator user in the system.
 * Admins can manage complaints, update statuses, and generate reports.
 */
public class Admin extends User {

    private Department department;
    private int totalResolved;

    /**
     * Default constructor.
     */
    public Admin() {
        super();
        setRole(Role.ADMIN);
        this.totalResolved = 0;
    }

    /**
     * Constructor with basic fields.
     */
    public Admin(String userId, String name, String email, String passwordHash) {
        super();
        setUserId(userId);
        setName(name);
        setEmail(email);
        setPasswordHash(passwordHash);
        setRole(Role.ADMIN);
        this.totalResolved = 0;
    }

    /**
     * Full constructor.
     */
    public Admin(String userId, String name, String email, String passwordHash,
                 boolean isActive, String phone, String profilePic,
                 LocalDateTime lastLogin, LocalDateTime createdAt, LocalDateTime updatedAt,
                 Department department, int totalResolved) {
        super(userId, name, email, passwordHash, Role.ADMIN, isActive, phone, profilePic,
              lastLogin, createdAt, updatedAt);
        this.department = department;
        this.totalResolved = totalResolved;
    }

    // ═══════════════════════════════════════════════════════════════
    // Abstract Method Implementations
    // ═══════════════════════════════════════════════════════════════

    @Override
    public String getDashboardTitle() {
        if (department != null) {
            return "Admin Panel — " + department.getDeptName();
        }
        return "Admin Panel";
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome back, Admin " + getFirstName() + "!";
    }

    @Override
    public List<String> getPermissions() {
        return List.of(
            "VIEW_ALL_COMPLAINTS",
            "UPDATE_STATUS",
            "ASSIGN_DEPARTMENT",
            "ADD_REMARKS",
            "VIEW_ANALYTICS",
            "GENERATE_REPORTS",
            "MANAGE_NOTIFICATIONS",
            "VIEW_STUDENT_INFO",
            "BULK_UPDATE",
            "EXPORT_DATA"
        );
    }

    @Override
    public String getHomeScreen() {
        return "admin_panel.fxml";
    }

    // ═══════════════════════════════════════════════════════════════
    // Admin-Specific Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Increment the total resolved count.
     */
    public void incrementResolved() {
        this.totalResolved++;
    }

    /**
     * Get admin info display string.
     */
    public String getAdminInfo() {
        StringBuilder sb = new StringBuilder("Administrator");
        if (department != null) {
            sb.append(" | ").append(department.getDeptName());
        }
        sb.append(" | Resolved: ").append(totalResolved);
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    // Getters and Setters
    // ═══════════════════════════════════════════════════════════════

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public int getTotalResolved() {
        return totalResolved;
    }

    public void setTotalResolved(int totalResolved) {
        this.totalResolved = totalResolved;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "userId='" + getUserId() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", department=" + (department != null ? department.getDeptName() : "null") +
                ", totalResolved=" + totalResolved +
                ", isActive=" + isActive() +
                '}';
    }
}
