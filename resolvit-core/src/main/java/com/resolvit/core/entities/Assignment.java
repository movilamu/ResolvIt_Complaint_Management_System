package com.resolvit.core.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Assignment entity representing the assignment of a complaint to a department.
 */
public class Assignment {

    private String assignmentId;
    private String complaintId;
    private Complaint complaint;
    private int deptId;
    private Department department;
    private String assignedBy;
    private User assignedByUser;
    private LocalDateTime assignedAt;
    private String remarks;

    /**
     * Default constructor.
     */
    public Assignment() {
        this.assignmentId = UUID.randomUUID().toString();
        this.assignedAt = LocalDateTime.now();
    }

    /**
     * Constructor with required fields.
     */
    public Assignment(String complaintId, int deptId, String assignedBy, String remarks) {
        this();
        this.complaintId = complaintId;
        this.deptId = deptId;
        this.assignedBy = assignedBy;
        this.remarks = remarks;
    }

    /**
     * Full constructor.
     */
    public Assignment(String assignmentId, String complaintId, int deptId,
                      String assignedBy, LocalDateTime assignedAt, String remarks) {
        this.assignmentId = assignmentId;
        this.complaintId = complaintId;
        this.deptId = deptId;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
        this.remarks = remarks;
    }

    // ═══════════════════════════════════════════════════════════════
    // Utility Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get formatted assignment info.
     */
    public String getAssignmentInfo() {
        StringBuilder sb = new StringBuilder();
        if (department != null) {
            sb.append("Assigned to: ").append(department.getDeptName());
        }
        if (assignedAt != null) {
            sb.append(" on ").append(assignedAt.toLocalDate());
        }
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    // Getters and Setters
    // ═══════════════════════════════════════════════════════════════

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
        if (complaint != null) {
            this.complaintId = complaint.getComplaintId();
        }
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
        if (department != null) {
            this.deptId = department.getDeptId();
        }
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public User getAssignedByUser() {
        return assignedByUser;
    }

    public void setAssignedByUser(User assignedByUser) {
        this.assignedByUser = assignedByUser;
        if (assignedByUser != null) {
            this.assignedBy = assignedByUser.getUserId();
        }
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    // ═══════════════════════════════════════════════════════════════
    // Object Methods
    // ═══════════════════════════════════════════════════════════════

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return Objects.equals(assignmentId, that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "assignmentId='" + assignmentId + '\'' +
                ", complaintId='" + complaintId + '\'' +
                ", deptId=" + deptId +
                ", assignedBy='" + assignedBy + '\'' +
                ", assignedAt=" + assignedAt +
                '}';
    }
}
