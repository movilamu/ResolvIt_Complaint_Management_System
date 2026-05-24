package com.resolvit.core.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Department entity representing an organizational department
 * that can be assigned to handle complaints.
 */
public class Department {

    private int deptId;
    private String deptName;
    private String deptHead;
    private String contactEmail;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor.
     */
    public Department() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor with basic fields.
     */
    public Department(int deptId, String deptName) {
        this();
        this.deptId = deptId;
        this.deptName = deptName;
    }

    /**
     * Full constructor.
     */
    public Department(int deptId, String deptName, String deptHead, String contactEmail,
                      boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.deptId = deptId;
        this.deptName = deptName;
        this.deptHead = deptHead;
        this.contactEmail = contactEmail;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ═══════════════════════════════════════════════════════════════
    // Utility Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get a formatted display string for the department.
     */
    public String getDisplayInfo() {
        StringBuilder sb = new StringBuilder(deptName);
        if (deptHead != null && !deptHead.isBlank()) {
            sb.append(" (Head: ").append(deptHead).append(")");
        }
        return sb.toString();
    }

    /**
     * Get short display name.
     */
    public String getShortName() {
        if (deptName == null) return "";
        // Return abbreviation for long names
        if (deptName.length() > 20) {
            return deptName.substring(0, 17) + "...";
        }
        return deptName;
    }

    // ═══════════════════════════════════════════════════════════════
    // Getters and Setters
    // ═══════════════════════════════════════════════════════════════

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDeptHead() {
        return deptHead;
    }

    public void setDeptHead(String deptHead) {
        this.deptHead = deptHead;
        this.updatedAt = LocalDateTime.now();
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        this.updatedAt = LocalDateTime.now();
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
        Department that = (Department) o;
        return deptId == that.deptId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deptId);
    }

    @Override
    public String toString() {
        return "Department{" +
                "deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", deptHead='" + deptHead + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
