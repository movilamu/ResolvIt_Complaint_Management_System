package com.resolvit.core.entities;

import com.resolvit.core.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Student entity representing a student user in the system.
 * Students can submit, view, and track their own complaints.
 */
public class Student extends User {

    private String rollNumber;
    private String className;
    private Department department;

    /**
     * Default constructor.
     */
    public Student() {
        super();
        setRole(Role.STUDENT);
    }

    /**
     * Constructor with basic fields.
     */
    public Student(String userId, String name, String email, String passwordHash) {
        super();
        setUserId(userId);
        setName(name);
        setEmail(email);
        setPasswordHash(passwordHash);
        setRole(Role.STUDENT);
    }

    /**
     * Full constructor.
     */
    public Student(String userId, String name, String email, String passwordHash,
                   boolean isActive, String phone, String profilePic,
                   LocalDateTime lastLogin, LocalDateTime createdAt, LocalDateTime updatedAt,
                   String rollNumber, String className, Department department) {
        super(userId, name, email, passwordHash, Role.STUDENT, isActive, phone, profilePic,
              lastLogin, createdAt, updatedAt);
        this.rollNumber = rollNumber;
        this.className = className;
        this.department = department;
    }

    // ═══════════════════════════════════════════════════════════════
    // Abstract Method Implementations
    // ═══════════════════════════════════════════════════════════════

    @Override
    public String getDashboardTitle() {
        return "My Complaints — " + getName();
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome back, " + getFirstName() + "!";
    }

    @Override
    public List<String> getPermissions() {
        return List.of(
            "SUBMIT_COMPLAINT",
            "VIEW_OWN_COMPLAINTS",
            "TRACK_STATUS",
            "SUBMIT_FEEDBACK",
            "VIEW_NOTIFICATIONS",
            "UPDATE_PROFILE",
            "REOPEN_COMPLAINT"
        );
    }

    @Override
    public String getHomeScreen() {
        return "student_dashboard.fxml";
    }

    // ═══════════════════════════════════════════════════════════════
    // Student-Specific Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get formatted display string for student info.
     */
    public String getStudentInfo() {
        StringBuilder sb = new StringBuilder();
        if (rollNumber != null && !rollNumber.isBlank()) {
            sb.append(rollNumber);
        }
        if (className != null && !className.isBlank()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append(className);
        }
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    // Getters and Setters
    // ═══════════════════════════════════════════════════════════════

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Student{" +
                "userId='" + getUserId() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", rollNumber='" + rollNumber + '\'' +
                ", className='" + className + '\'' +
                ", isActive=" + isActive() +
                '}';
    }
}
