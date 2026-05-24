package com.resolvit.service.interfaces;

import com.resolvit.core.entities.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for audit logging operations.
 */
public interface AuditService {
    
    /**
     * Logs an action.
     *
     * @param userId the user who performed the action
     * @param action the action performed
     * @param entityType the type of entity affected
     * @param entityId the ID of the entity affected
     * @param oldValue the old value (for updates)
     * @param newValue the new value (for updates)
     * @param ipAddress the IP address of the user
     * @return the created audit log entry
     */
    AuditLog logAction(Long userId, String action, String entityType, Long entityId,
                       String oldValue, String newValue, String ipAddress);
    
    /**
     * Logs a login attempt.
     *
     * @param username the username
     * @param success whether the login was successful
     * @param ipAddress the IP address
     * @return the created audit log entry
     */
    AuditLog logLoginAttempt(String username, boolean success, String ipAddress);
    
    /**
     * Logs a logout.
     *
     * @param userId the user ID
     * @param ipAddress the IP address
     * @return the created audit log entry
     */
    AuditLog logLogout(Long userId, String ipAddress);
    
    /**
     * Logs a complaint action.
     *
     * @param userId the user who performed the action
     * @param complaintId the complaint ID
     * @param action the action performed
     * @param details additional details
     * @return the created audit log entry
     */
    AuditLog logComplaintAction(Long userId, Long complaintId, String action, String details);
    
    /**
     * Gets all audit logs.
     *
     * @return list of all audit logs
     */
    List<AuditLog> getAllLogs();
    
    /**
     * Gets audit logs by user.
     *
     * @param userId the user ID
     * @return list of audit logs for the user
     */
    List<AuditLog> getLogsByUser(Long userId);
    
    /**
     * Gets audit logs by entity.
     *
     * @param entityType the entity type
     * @param entityId the entity ID
     * @return list of audit logs for the entity
     */
    List<AuditLog> getLogsByEntity(String entityType, Long entityId);
    
    /**
     * Gets audit logs by action type.
     *
     * @param action the action type
     * @return list of audit logs for the action
     */
    List<AuditLog> getLogsByAction(String action);
    
    /**
     * Gets audit logs within a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of audit logs within the range
     */
    List<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Gets recent audit logs.
     *
     * @param limit the maximum number of logs to return
     * @return list of recent audit logs
     */
    List<AuditLog> getRecentLogs(int limit);
    
    /**
     * Gets audit logs for a complaint.
     *
     * @param complaintId the complaint ID
     * @return list of audit logs for the complaint
     */
    List<AuditLog> getComplaintAuditTrail(Long complaintId);
    
    /**
     * Searches audit logs by keyword.
     *
     * @param keyword the search keyword
     * @return list of matching audit logs
     */
    List<AuditLog> searchLogs(String keyword);
    
    /**
     * Gets login history for a user.
     *
     * @param userId the user ID
     * @param limit the maximum number of entries
     * @return list of login audit logs
     */
    List<AuditLog> getLoginHistory(Long userId, int limit);
    
    /**
     * Gets failed login attempts for a username.
     *
     * @param username the username
     * @param sinceDate the date to check from
     * @return count of failed login attempts
     */
    int getFailedLoginAttempts(String username, LocalDateTime sinceDate);
    
    /**
     * Purges old audit logs.
     *
     * @param olderThan delete logs older than this date
     * @return count of deleted logs
     */
    int purgeOldLogs(LocalDateTime olderThan);
}
