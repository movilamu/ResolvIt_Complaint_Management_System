package com.resolvit.core.entities;

import com.resolvit.core.enums.Status;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * StatusHistory entity representing a status change in the complaint lifecycle.
 * Used for maintaining a complete audit trail of status transitions.
 */
public class StatusHistory {

    private long historyId;
    private String complaintId;
    private Complaint complaint;
    private Status oldStatus;
    private Status newStatus;
    private String changedBy;
    private User changedByUser;
    private String remarks;
    private LocalDateTime changedAt;

    /**
     * Default constructor.
     */
    public StatusHistory() {
        this.changedAt = LocalDateTime.now();
    }

    /**
     * Constructor with required fields.
     */
    public StatusHistory(String complaintId, Status oldStatus, Status newStatus,
                         String changedBy, String remarks) {
        this();
        this.complaintId = complaintId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.remarks = remarks;
    }

    /**
     * Full constructor.
     */
    public StatusHistory(long historyId, String complaintId, Status oldStatus,
                         Status newStatus, String changedBy, String remarks,
                         LocalDateTime changedAt) {
        this.historyId = historyId;
        this.complaintId = complaintId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.remarks = remarks;
        this.changedAt = changedAt;
    }

    // ═══════════════════════════════════════════════════════════════
    // Utility Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get formatted change description.
     */
    public String getChangeDescription() {
        StringBuilder sb = new StringBuilder();
        if (oldStatus != null) {
            sb.append(oldStatus.getDisplayName());
        } else {
            sb.append("Created");
        }
        sb.append(" → ").append(newStatus.getDisplayName());
        return sb.toString();
    }

    /**
     * Get timeline display text.
     */
    public String getTimelineText() {
        StringBuilder sb = new StringBuilder();
        sb.append(newStatus.getDisplayName());
        if (remarks != null && !remarks.isBlank()) {
            sb.append(": ").append(remarks);
        }
        return sb.toString();
    }

    /**
     * Get time elapsed since change.
     */
    public String getTimeAgo() {
        if (changedAt == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        long days = java.time.Duration.between(changedAt, now).toDays();
        
        if (days == 0) {
            long hours = java.time.Duration.between(changedAt, now).toHours();
            if (hours == 0) {
                long minutes = java.time.Duration.between(changedAt, now).toMinutes();
                return minutes + " min ago";
            }
            return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        }
        return changedAt.toLocalDate().toString();
    }

    // ═══════════════════════════════════════════════════════════════
    // Getters and Setters
    // ═══════════════════════════════════════════════════════════════

    public long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(long historyId) {
        this.historyId = historyId;
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

    public Status getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(Status oldStatus) {
        this.oldStatus = oldStatus;
    }

    public Status getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(Status newStatus) {
        this.newStatus = newStatus;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public User getChangedByUser() {
        return changedByUser;
    }

    public void setChangedByUser(User changedByUser) {
        this.changedByUser = changedByUser;
        if (changedByUser != null) {
            this.changedBy = changedByUser.getUserId();
        }
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    // ═══════════════════════════════════════════════════════════════
    // Object Methods
    // ═══════════════════════════════════════════════════════════════

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusHistory that = (StatusHistory) o;
        return historyId == that.historyId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(historyId);
    }

    @Override
    public String toString() {
        return "StatusHistory{" +
                "historyId=" + historyId +
                ", complaintId='" + complaintId + '\'' +
                ", oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", changedBy='" + changedBy + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}
