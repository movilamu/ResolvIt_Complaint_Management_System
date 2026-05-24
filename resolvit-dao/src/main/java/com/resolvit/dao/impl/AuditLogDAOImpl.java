package com.resolvit.dao.impl;

import com.resolvit.core.entities.AuditLog;
import com.resolvit.dao.connection.DatabaseConnection;
import com.resolvit.dao.interfaces.AuditLogDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of AuditLogDAO for MySQL database.
 */
public class AuditLogDAOImpl implements AuditLogDAO {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public AuditLogDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public long insert(AuditLog auditLog) throws SQLException {
        String sql = """
            INSERT INTO audit_log (user_id, action, entity_type, entity_id, 
                                  old_value, new_value, ip_address, user_agent, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, auditLog.getUserId());
                ps.setString(2, auditLog.getAction());
                ps.setString(3, auditLog.getEntityType());
                ps.setString(4, auditLog.getEntityId());
                ps.setString(5, auditLog.getOldValue());
                ps.setString(6, auditLog.getNewValue());
                ps.setString(7, auditLog.getIpAddress());
                ps.setString(8, auditLog.getUserAgent());
                ps.setTimestamp(9, Timestamp.valueOf(auditLog.getTimestamp()));

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        auditLog.setLogId(id);
                        logger.trace("Inserted audit log: {}", id);
                        return id;
                    }
                }
                return -1;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public void log(String userId, String action, String entityType,
                   String entityId, String oldValue, String newValue) throws SQLException {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(LocalDateTime.now())
                .build();
        insert(log);
    }

    @Override
    public Optional<AuditLog> findById(long logId) throws SQLException {
        String sql = "SELECT * FROM audit_log WHERE log_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, logId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToAuditLog(rs));
                    }
                    return Optional.empty();
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<AuditLog> findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM audit_log WHERE user_id = ? ORDER BY timestamp DESC";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<AuditLog> findByAction(String action) throws SQLException {
        String sql = "SELECT * FROM audit_log WHERE action = ? ORDER BY timestamp DESC";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, action);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<AuditLog> findByEntity(String entityType, String entityId) throws SQLException {
        String sql = "SELECT * FROM audit_log WHERE entity_type = ? AND entity_id = ? ORDER BY timestamp DESC";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entityType);
                ps.setString(2, entityId);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<AuditLog> findByTimeRange(LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql = "SELECT * FROM audit_log WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(from));
                ps.setTimestamp(2, Timestamp.valueOf(to));
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<AuditLog> findRecentPaginated(int offset, int limit) throws SQLException {
        String sql = "SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT ? OFFSET ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limit);
                ps.setInt(2, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<AuditLog> findSecurityEvents() throws SQLException {
        String sql = """
            SELECT * FROM audit_log 
            WHERE action LIKE '%LOGIN%' 
               OR action LIKE '%PASSWORD%' 
               OR action LIKE '%PERMISSION%'
               OR action LIKE '%UNAUTHORIZED%'
            ORDER BY timestamp DESC
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                return mapResultSetToList(rs);
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<AuditLog> findFailedLogins(LocalDateTime since) throws SQLException {
        String sql = """
            SELECT * FROM audit_log 
            WHERE action = 'FAILED_LOGIN' AND timestamp >= ?
            ORDER BY timestamp DESC
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(since));
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int deleteOlderThan(int days) throws SQLException {
        String sql = "DELETE FROM audit_log WHERE timestamp < DATE_SUB(NOW(), INTERVAL ? DAY)";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, days);
                int deleted = ps.executeUpdate();
                logger.info("Deleted {} audit log entries older than {} days", deleted, days);
                return deleted;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM audit_log";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int countByAction(String action) throws SQLException {
        String sql = "SELECT COUNT(*) FROM audit_log WHERE action = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, action);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════

    private List<AuditLog> mapResultSetToList(ResultSet rs) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        while (rs.next()) {
            logs.add(mapResultSetToAuditLog(rs));
        }
        return logs;
    }

    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();

        log.setLogId(rs.getLong("log_id"));
        log.setUserId(rs.getString("user_id"));
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));
        log.setEntityId(rs.getString("entity_id"));
        log.setOldValue(rs.getString("old_value"));
        log.setNewValue(rs.getString("new_value"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setUserAgent(rs.getString("user_agent"));

        Timestamp timestamp = rs.getTimestamp("timestamp");
        if (timestamp != null) {
            log.setTimestamp(timestamp.toLocalDateTime());
        }

        return log;
    }
}
