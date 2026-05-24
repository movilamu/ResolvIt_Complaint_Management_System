package com.resolvit.dao.impl;

import com.resolvit.core.entities.Notification;
import com.resolvit.core.enums.NotificationType;
import com.resolvit.dao.connection.DatabaseConnection;
import com.resolvit.dao.interfaces.NotificationDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of NotificationDAO for MySQL database.
 */
public class NotificationDAOImpl implements NotificationDAO {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public NotificationDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public String save(Notification notification) throws SQLException {
        String sql = """
            INSERT INTO notifications (notif_id, user_id, title, message, type, is_read, related_id, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, notification.getNotifId());
                ps.setString(2, notification.getUserId());
                ps.setString(3, notification.getTitle());
                ps.setString(4, notification.getMessage());
                ps.setString(5, notification.getType().name());
                ps.setBoolean(6, notification.isRead());
                ps.setString(7, notification.getRelatedId());
                ps.setTimestamp(8, Timestamp.valueOf(notification.getCreatedAt()));

                ps.executeUpdate();
                logger.debug("Saved notification: {}", notification.getNotifId());
                return notification.getNotifId();
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public Optional<Notification> findById(String notifId) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE notif_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, notifId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToNotification(rs));
                    }
                    return Optional.empty();
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Notification> findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";

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
    public List<Notification> findByUserIdPaginated(String userId, int offset, int limit) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setInt(2, limit);
                ps.setInt(3, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Notification> findUnreadByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";

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
    public List<Notification> findByUserIdAndType(String userId, NotificationType type) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND type = ? ORDER BY created_at DESC";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setString(2, type.name());
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Notification> findByRelatedId(String relatedId) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE related_id = ? ORDER BY created_at DESC";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, relatedId);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean markAsRead(String notifId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notif_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, notifId);
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int markAllAsRead(String userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                return ps.executeUpdate();
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(String notifId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE notif_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, notifId);
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int deleteByUserId(String userId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                return ps.executeUpdate();
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int deleteOldReadNotifications(int days) throws SQLException {
        String sql = "DELETE FROM notifications WHERE is_read = TRUE AND created_at < DATE_SUB(NOW(), INTERVAL ? DAY)";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, days);
                int deleted = ps.executeUpdate();
                logger.debug("Deleted {} old read notifications", deleted);
                return deleted;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int countUnread(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int countByUserId(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
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

    private List<Notification> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        while (rs.next()) {
            notifications.add(mapResultSetToNotification(rs));
        }
        return notifications;
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();

        notification.setNotifId(rs.getString("notif_id"));
        notification.setUserId(rs.getString("user_id"));
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));
        notification.setType(NotificationType.valueOf(rs.getString("type")));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setRelatedId(rs.getString("related_id"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            notification.setCreatedAt(createdAt.toLocalDateTime());
        }

        return notification;
    }
}
