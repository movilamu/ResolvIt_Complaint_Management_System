package com.resolvit.dao.interfaces;

import com.resolvit.core.entities.AuditLog;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for AuditLog operations.
 */
public interface AuditLogDAO {

    /**
     * Insert a new audit log entry.
     * 
     * @param auditLog the audit log entry
     * @return the saved log ID
     * @throws SQLException if a database error occurs
     */
    long insert(AuditLog auditLog) throws SQLException;

    /**
     * Log an action.
     * 
     * @param userId the user performing the action
     * @param action the action name
     * @param entityType the entity type
     * @param entityId the entity ID
     * @param oldValue the old value
     * @param newValue the new value
     * @throws SQLException if a database error occurs
     */
    void log(String userId, String action, String entityType, 
             String entityId, String oldValue, String newValue) throws SQLException;

    /**
     * Find an audit log entry by ID.
     * 
     * @param logId the log ID
     * @return Optional containing the audit log if found
     * @throws SQLException if a database error occurs
     */
    Optional<AuditLog> findById(long logId) throws SQLException;

    /**
     * Find audit logs by user ID.
     * 
     * @param userId the user ID
     * @return list of audit logs for the user
     * @throws SQLException if a database error occurs
     */
    List<AuditLog> findByUserId(String userId) throws SQLException;

    /**
     * Find audit logs by action.
     * 
     * @param action the action name
     * @return list of audit logs for the action
     * @throws SQLException if a database error occurs
     */
    List<AuditLog> findByAction(String action) throws SQLException;

    /**
     * Find audit logs by entity.
     * 
     * @param entityType the entity type
     * @param entityId the entity ID
     * @return list of audit logs for the entity
     * @throws SQLException if a database error occurs
     */
    List<AuditLog> findByEntity(String entityType, String entityId) throws SQLException;

    /**
     * Find audit logs within a time range.
     * 
     * @param from start timestamp
     * @param to end timestamp
     * @return list of audit logs in the range
     * @throws SQLException if a database error occurs
     */
    List<AuditLog> findByTimeRange(LocalDateTime from, LocalDateTime to) throws SQLException;

    /**
     * Find recent audit logs with pagination.
     * 
     * @param offset the starting offset
     * @param limit the maximum number of results
     * @return list of audit logs
     * @throws SQLException if a database error occurs
     */
    List<AuditLog> findRecentPaginated(int offset, int limit) throws SQLException;

    /**
     * Find security-related events (login, password changes, etc.).
     * 
     * @return list of security audit logs
     * @throws SQLException if a database error occurs
     */
    List<AuditLog> findSecurityEvents() throws SQLException;

    /**
     * Find failed login attempts.
     * 
     * @param since since timestamp
     * @return list of failed login audit logs
     * @throws SQLException if a database error occurs
     */
    List<AuditLog> findFailedLogins(LocalDateTime since) throws SQLException;

    /**
     * Delete old audit logs (older than specified days).
     * 
     * @param days number of days
     * @return number of deleted logs
     * @throws SQLException if a database error occurs
     */
    int deleteOlderThan(int days) throws SQLException;

    /**
     * Count total audit logs.
     * 
     * @return total log count
     * @throws SQLException if a database error occurs
     */
    int count() throws SQLException;

    /**
     * Count audit logs by action.
     * 
     * @param action the action name
     * @return count of logs for the action
     * @throws SQLException if a database error occurs
     */
    int countByAction(String action) throws SQLException;
}
