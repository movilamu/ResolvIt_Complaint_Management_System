package com.resolvit.core.entities;

import com.resolvit.core.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Notification entity representing a user notification.
 */
public class Notification {

    private String notifId;
    private String userId;
    private User user;
    private String title;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private String relatedId;
    private LocalDateTime createdAt;

    /**
     * Default constructor.
     */
    public Notification() {
        this.notifId = UUID.randomUUID().toString();
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor with required fields.
     */
    public Notification(String userId, String title, String message, NotificationType type) {
        this();
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
    }

    /**
     * Full constructor.
     */
    public Notification(String notifId, String userId, String title, String message,
                        NotificationType type, boolean isRead, String relatedId,
                        LocalDateTime createdAt) {
        this.notifId = notifId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.relatedId = relatedId;
        this.createdAt = createdAt;
    }

    // ═══════════════════════════════════════════════════════════════
    // Utility Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Mark this notification as read.
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * Get time elapsed since notification in human-readable format.
     */
    public String getTimeAgo() {
        if (createdAt == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(createdAt, now).toMinutes();
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (minutes < 1440) { // less than 24 hours
            long hours = minutes / 60;
            return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        } else if (minutes < 10080) { // less than 7 days
            long days = minutes / 1440;
            return days + " day" + (days != 1 ? "s" : "") + " ago";
        } else {
            long weeks = minutes / 10080;
            return weeks + " week" + (weeks != 1 ? "s" : "") + " ago";
        }
    }

    /**
     * Get display title with icon.
     */
    public String getDisplayTitle() {
        if (type != null) {
            return type.getIcon() + " " + title;
        }
        return title;
    }

    // ═══════════════════════════════════════════════════════════════
    // Getters and Setters
    // ═══════════════════════════════════════════════════════════════

    public String getNotifId() {
        return notifId;
    }

    public void setNotifId(String notifId) {
        this.notifId = notifId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getUserId();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ═══════════════════════════════════════════════════════════════
    // Object Methods
    // ═══════════════════════════════════════════════════════════════

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(notifId, that.notifId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notifId);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notifId='" + notifId + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}
