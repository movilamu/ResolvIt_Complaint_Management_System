package com.resolvit.core.entities;

import com.resolvit.core.enums.Category;
import com.resolvit.core.enums.Priority;
import com.resolvit.core.enums.Status;
import com.resolvit.core.exceptions.InvalidStatusTransitionException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Complaint entity representing a complaint submitted by a user.
 * Uses the Builder pattern for construction.
 * Implements a state machine for status transitions.
 */
public class Complaint {

    private String complaintId;
    private User submittedBy;
    private String userId; // For cases where full User object isn't loaded
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private Status status;
    private String attachmentPath;
    private boolean isAnonymous;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    // ═══════════════════════════════════════════════════════════════
    // Constructors
    // ═══════════════════════════════════════════════════════════════

    /**
     * Private constructor - use Builder instead.
     */
    private Complaint(Builder builder) {
        this.complaintId = UUID.randomUUID().toString();
        this.submittedBy = builder.submittedBy;
        this.userId = builder.submittedBy != null ? builder.submittedBy.getUserId() : builder.userId;
        this.title = builder.title;
        this.description = builder.description;
        this.category = builder.category;
        this.priority = builder.priority;
        this.status = Status.PENDING;
        this.attachmentPath = builder.attachmentPath;
        this.isAnonymous = builder.isAnonymous;
        this.viewCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Default constructor for DAO mapping.
     */
    public Complaint() {
        this.status = Status.PENDING;
        this.viewCount = 0;
        this.isAnonymous = false;
    }

    // ═══════════════════════════════════════════════════════════════
    // State Machine - Status Transitions
    // ═══════════════════════════════════════════════════════════════

    /**
     * Transition to a new status with validation.
     * 
     * @param newStatus The target status
     * @param changedBy The user making the change
     * @param remarks Optional remarks for the transition
     * @throws InvalidStatusTransitionException if transition is not allowed
     */
    public void transitionTo(Status newStatus, User changedBy, String remarks) {
        if (!isValidTransition(this.status, newStatus)) {
            throw new InvalidStatusTransitionException(
                "Cannot transition from " + this.status + " to " + newStatus,
                this.status, newStatus
            );
        }

        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();

        if (newStatus == Status.RESOLVED) {
            this.resolvedAt = LocalDateTime.now();
        } else if (newStatus == Status.REOPENED) {
            this.resolvedAt = null;
        }
    }

    /**
     * Check if a status transition is valid according to the state machine.
     * 
     * Valid transitions:
     * - PENDING → IN_PROGRESS, REJECTED
     * - IN_PROGRESS → RESOLVED, REJECTED
     * - RESOLVED → REOPENED
     * - REJECTED → PENDING (re-submit)
     * - REOPENED → IN_PROGRESS
     */
    public static boolean isValidTransition(Status from, Status to) {
        if (from == null || to == null) {
            return false;
        }

        return switch (from) {
            case PENDING -> to == Status.IN_PROGRESS || to == Status.REJECTED;
            case IN_PROGRESS -> to == Status.RESOLVED || to == Status.REJECTED;
            case RESOLVED -> to == Status.REOPENED;
            case REJECTED -> to == Status.PENDING;
            case REOPENED -> to == Status.IN_PROGRESS;
        };
    }

    /**
     * Get valid next statuses from current status.
     */
    public Status[] getValidNextStatuses() {
        return switch (this.status) {
            case PENDING -> new Status[]{Status.IN_PROGRESS, Status.REJECTED};
            case IN_PROGRESS -> new Status[]{Status.RESOLVED, Status.REJECTED};
            case RESOLVED -> new Status[]{Status.REOPENED};
            case REJECTED -> new Status[]{Status.PENDING};
            case REOPENED -> new Status[]{Status.IN_PROGRESS};
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // Utility Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Increment view count.
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * Get a short ID for display (first 8 characters).
     */
    public String getShortId() {
        if (complaintId == null) return "";
        return "CMP-" + complaintId.substring(0, Math.min(8, complaintId.length())).toUpperCase();
    }

    /**
     * Get the submitter's display name (or "Anonymous" if anonymous).
     */
    public String getSubmitterDisplayName() {
        if (isAnonymous) {
            return "Anonymous";
        }
        return submittedBy != null ? submittedBy.getName() : "Unknown";
    }

    /**
     * Check if complaint can be reopened (within 7 days of resolution).
     */
    public boolean canReopen() {
        if (status != Status.RESOLVED || resolvedAt == null) {
            return false;
        }
        return resolvedAt.plusDays(7).isAfter(LocalDateTime.now());
    }

    /**
     * Get time elapsed since creation in human-readable format.
     */
    public String getTimeElapsed() {
        if (createdAt == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        long days = java.time.Duration.between(createdAt, now).toDays();
        
        if (days == 0) {
            long hours = java.time.Duration.between(createdAt, now).toHours();
            if (hours == 0) {
                long minutes = java.time.Duration.between(createdAt, now).toMinutes();
                return minutes + " min ago";
            }
            return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        } else if (days == 1) {
            return "Yesterday";
        } else if (days < 7) {
            return days + " days ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + " week" + (weeks != 1 ? "s" : "") + " ago";
        } else {
            long months = days / 30;
            return months + " month" + (months != 1 ? "s" : "") + " ago";
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Getters and Setters
    // ═══════════════════════════════════════════════════════════════

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
        if (submittedBy != null) {
            this.userId = submittedBy.getUserId();
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
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

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    // ═══════════════════════════════════════════════════════════════
    // Object Methods
    // ═══════════════════════════════════════════════════════════════

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Complaint complaint = (Complaint) o;
        return Objects.equals(complaintId, complaint.complaintId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(complaintId);
    }

    @Override
    public String toString() {
        return "Complaint{" +
                "complaintId='" + complaintId + '\'' +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", priority=" + priority +
                ", status=" + status +
                ", isAnonymous=" + isAnonymous +
                ", createdAt=" + createdAt +
                '}';
    }

    // ═══════════════════════════════════════════════════════════════
    // Builder Pattern
    // ═══════════════════════════════════════════════════════════════

    /**
     * Builder class for creating Complaint instances.
     */
    public static class Builder {
        private User submittedBy;
        private String userId;
        private String title;
        private String description;
        private Category category;
        private Priority priority;
        private String attachmentPath;
        private boolean isAnonymous = false;

        public Builder submittedBy(User user) {
            this.submittedBy = user;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Builder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        public Builder attachmentPath(String path) {
            this.attachmentPath = path;
            return this;
        }

        public Builder anonymous(boolean isAnonymous) {
            this.isAnonymous = isAnonymous;
            return this;
        }

        /**
         * Build the Complaint instance.
         * 
         * @throws NullPointerException if required fields are null
         */
        public Complaint build() {
            Objects.requireNonNull(submittedBy != null ? submittedBy : userId, 
                "submittedBy or userId is required");
            Objects.requireNonNull(title, "title is required");
            Objects.requireNonNull(description, "description is required");
            Objects.requireNonNull(category, "category is required");
            Objects.requireNonNull(priority, "priority is required");
            return new Complaint(this);
        }
    }
}
