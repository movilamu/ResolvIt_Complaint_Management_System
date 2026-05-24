package com.resolvit.service.impl;

import com.resolvit.core.entities.AuditLog;
import com.resolvit.dao.interfaces.AuditLogDAO;
import com.resolvit.service.interfaces.AuditService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of the AuditService interface.
 */
public class AuditServiceImpl implements AuditService {
    
    private static final Logger LOGGER = Logger.getLogger(AuditServiceImpl.class.getName());
    
    private final AuditLogDAO auditLogDAO;
    
    public AuditServiceImpl(AuditLogDAO auditLogDAO) {
        this.auditLogDAO = auditLogDAO;
    }
    
    @Override
    public AuditLog logAction(Long userId, String action, String entityType, Long entityId,
                              String oldValue, String newValue, String ipAddress) {
        LOGGER.fine("Logging action: " + action + " by user " + userId);
        
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        log.setTimestamp(LocalDateTime.now());
        
        return auditLogDAO.save(log);
    }
    
    @Override
    public AuditLog logLoginAttempt(String username, boolean success, String ipAddress) {
        String action = success ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
        
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType("USER");
        log.setDetails("Username: " + username);
        log.setIpAddress(ipAddress);
        log.setTimestamp(LocalDateTime.now());
        
        return auditLogDAO.save(log);
    }
    
    @Override
    public AuditLog logLogout(Long userId, String ipAddress) {
        return logAction(userId, "LOGOUT", "USER", userId, null, null, ipAddress);
    }
    
    @Override
    public AuditLog logComplaintAction(Long userId, Long complaintId, String action, String details) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setEntityType("COMPLAINT");
        log.setEntityId(complaintId);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        
        return auditLogDAO.save(log);
    }
    
    @Override
    public List<AuditLog> getAllLogs() {
        return auditLogDAO.findAll();
    }
    
    @Override
    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogDAO.findByUser(userId);
    }
    
    @Override
    public List<AuditLog> getLogsByEntity(String entityType, Long entityId) {
        return auditLogDAO.findByEntity(entityType, entityId);
    }
    
    @Override
    public List<AuditLog> getLogsByAction(String action) {
        return auditLogDAO.findByAction(action);
    }
    
    @Override
    public List<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogDAO.findByDateRange(startDate, endDate);
    }
    
    @Override
    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogDAO.findRecent(limit);
    }
    
    @Override
    public List<AuditLog> getComplaintAuditTrail(Long complaintId) {
        return auditLogDAO.findByEntity("COMPLAINT", complaintId);
    }
    
    @Override
    public List<AuditLog> searchLogs(String keyword) {
        return auditLogDAO.searchByKeyword(keyword);
    }
    
    @Override
    public List<AuditLog> getLoginHistory(Long userId, int limit) {
        return auditLogDAO.findLoginHistory(userId, limit);
    }
    
    @Override
    public int getFailedLoginAttempts(String username, LocalDateTime sinceDate) {
        return auditLogDAO.countFailedLogins(username, sinceDate);
    }
    
    @Override
    public int purgeOldLogs(LocalDateTime olderThan) {
        LOGGER.info("Purging audit logs older than: " + olderThan);
        return auditLogDAO.deleteOlderThan(olderThan);
    }
}
