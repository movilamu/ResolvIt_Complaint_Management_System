package com.resolvit.dao.interfaces;

import com.resolvit.core.entities.Notification;
import com.resolvit.core.enums.NotificationType;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Notification operations.
 */
public interface NotificationDAO {

    /**
     * Save a new notification.
     * 
     * @param notification the notification to save
     * @return the saved notification's ID
     * @throws SQLException if a database error occurs
     */
    String save(Notification notification) throws SQLException;

    /**
     * Find a notification by ID.
     * 
     * @param notifId the notification ID
     * @return Optional containing the notification if found
     * @throws SQLException if a database error occurs
     */
    Optional<Notification> findById(String notifId) throws SQLException;

    /**
     * Find all notifications for a user.
     * 
     * @param userId the user ID
     * @return list of notifications for the user
     * @throws SQLException if a database error occurs
     */
    List<Notification> findByUserId(String userId) throws SQLException;

    /**
     * Find all notifications for a user with pagination.
     * 
     * @param userId the user ID
     * @param offset the starting offset
     * @param limit the maximum number of results
     * @return list of notifications for the user
     * @throws SQLException if a database error occurs
     */
    List<Notification> findByUserIdPaginated(String userId, int offset, int limit) throws SQLException;

    /**
     * Find all unread notifications for a user.
     * 
     * @param userId the user ID
     * @return list of unread notifications
     * @throws SQLException if a database error occurs
     */
    List<Notification> findUnreadByUserId(String userId) throws SQLException;

    /**
     * Find notifications by type for a user.
     * 
     * @param userId the user ID
     * @param type the notification type
     * @return list of notifications of the specified type
     * @throws SQLException if a database error occurs
     */
    List<Notification> findByUserIdAndType(String userId, NotificationType type) throws SQLException;

    /**
     * Find notifications related to a specific entity.
     * 
     * @param relatedId the related entity ID
     * @return list of related notifications
     * @throws SQLException if a database error occurs
     */
    List<Notification> findByRelatedId(String relatedId) throws SQLException;

    /**
     * Mark a notification as read.
     * 
     * @param notifId the notification ID
     * @return true if update was successful
     * @throws SQLException if a database error occurs
     */
    boolean markAsRead(String notifId) throws SQLException;

    /**
     * Mark all notifications for a user as read.
     * 
     * @param userId the user ID
     * @return number of notifications marked as read
     * @throws SQLException if a database error occurs
     */
    int markAllAsRead(String userId) throws SQLException;

    /**
     * Delete a notification.
     * 
     * @param notifId the notification ID
     * @return true if deletion was successful
     * @throws SQLException if a database error occurs
     */
    boolean delete(String notifId) throws SQLException;

    /**
     * Delete all notifications for a user.
     * 
     * @param userId the user ID
     * @return number of deleted notifications
     * @throws SQLException if a database error occurs
     */
    int deleteByUserId(String userId) throws SQLException;

    /**
     * Delete old read notifications (older than specified days).
     * 
     * @param days number of days
     * @return number of deleted notifications
     * @throws SQLException if a database error occurs
     */
    int deleteOldReadNotifications(int days) throws SQLException;

    /**
     * Count unread notifications for a user.
     * 
     * @param userId the user ID
     * @return count of unread notifications
     * @throws SQLException if a database error occurs
     */
    int countUnread(String userId) throws SQLException;

    /**
     * Count total notifications for a user.
     * 
     * @param userId the user ID
     * @return total notification count
     * @throws SQLException if a database error occurs
     */
    int countByUserId(String userId) throws SQLException;
}
