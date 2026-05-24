package com.resolvit.core.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AuditLog entity for MNC-grade compliance logging.
 * Records all significant actions in the system.
 */
public class AuditLog {

    private long logId;
    private String userId;
    private User user;
    private String action;
    private String entityType;
    private String entityId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;

    /**
     * Default constructor.
     */
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor with required fields.
     */
    public AuditLog(String userId, String action, String entityType, String entityId) {
        this();
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    /**
     * Full constructor.
     */
    public AuditLog(long logId, String userId, String action, String entityType,
                    String entityId, String oldValue, String newValue,
                    String ipAddress, String userAgent, LocalDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.timestamp = timestamp;
    }

    // ═══════════════════════════════════════════════════════════════
    // Builder Pattern
    // ═══════════════════════════════════════════════════════════════

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private String action;
        private String entityType;
        private String entityId;
        private String oldValue;
        private String newValue;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime timestamp;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder oldValue(String oldValue) {
            this.oldValue = oldValue;
            return this;
        }

        public Builder newValue(String newValue) {
            this.newValue = newValue;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AuditLog build() {
            AuditLog log = new AuditLog();
            log.userId = this.userId;
            log.action = this.action;
            log.entityType = this.entityType;
            log.entityId = this.entityId;
            log.oldValue = this.oldValue;
            log.newValue = this.newValue;
            log.ipAddress = this.ipAddress;
            log.userAgent = this.userAgent;
            log.timestamp = this.timestamp != null ? this.timestamp : LocalDateTime.now();
            return log;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Utility Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get formatted log entry for display.
     */
    public String getFormattedEntry() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(timestamp).append("] ");
        sb.append(action).append(" ");
        if (entityType != null) {
            sb.append(entityType);
            if (entityId != null) {
                sb.append(":").append(entityId);
            }
        }
        if (oldValue != null || newValue != null) {
            sb.append(" (").append(oldValue).append(" → ").append(newValue).append(")");
        }
        return sb.toString();
    }

    /**
     * Check if this is a security-related event.
     */
    public boolean isSecurityEvent() {
        if (action == null) return false;
        return action.contains("LOGIN") || 
               action.contains("PASSWORD") || 
               action.contains("PERMISSION") ||
               action.contains("UNAUTHORIZED");
    }

    // ═══════════════════════════════════════════════════════════════
    // Getters and Setters
    // ═══════════════════════════════════════════════════════════════

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // ═══════════════════════════════════════════════════════════════
    // Object Methods
    // ═══════════════════════════════════════════════════════════════

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return logId == auditLog.logId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId);
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "logId=" + logId +
                ", userId='" + userId + '\'' +
                ", action='" + action + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId='" + entityId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
