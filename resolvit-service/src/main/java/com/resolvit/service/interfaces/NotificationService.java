package com.resolvit.service.interfaces;

import com.resolvit.core.entities.Notification;
import com.resolvit.core.enums.NotificationType;

import java.util.List;

/**
 * Service interface for notification management operations.
 */
public interface NotificationService {
    
    /**
     * Creates a new notification.
     *
     * @param notification the notification to create
     * @return the created notification
     */
    Notification createNotification(Notification notification);
    
    /**
     * Sends a notification to a user.
     *
     * @param userId the recipient user ID
     * @param title the notification title
     * @param message the notification message
     * @param type the notification type
     * @param referenceId optional reference ID (e.g., complaint ID)
     * @return the created notification
     */
    Notification sendNotification(Long userId, String title, String message, 
                                   NotificationType type, Long referenceId);
    
    /**
     * Gets all notifications for a user.
     *
     * @param userId the user ID
     * @return list of notifications
     */
    List<Notification> getNotificationsByUser(Long userId);
    
    /**
     * Gets unread notifications for a user.
     *
     * @param userId the user ID
     * @return list of unread notifications
     */
    List<Notification> getUnreadNotifications(Long userId);
    
    /**
     * Gets recent notifications for a user.
     *
     * @param userId the user ID
     * @param limit the maximum number of notifications to return
     * @return list of recent notifications
     */
    List<Notification> getRecentNotifications(Long userId, int limit);
    
    /**
     * Marks a notification as read.
     *
     * @param notificationId the notification ID
     * @return true if marked successfully
     */
    boolean markAsRead(Long notificationId);
    
    /**
     * Marks all notifications for a user as read.
     *
     * @param userId the user ID
     * @return count of notifications marked as read
     */
    int markAllAsRead(Long userId);
    
    /**
     * Deletes a notification.
     *
     * @param notificationId the notification ID
     * @return true if deleted successfully
     */
    boolean deleteNotification(Long notificationId);
    
    /**
     * Deletes all read notifications for a user.
     *
     * @param userId the user ID
     * @return count of notifications deleted
     */
    int deleteReadNotifications(Long userId);
    
    /**
     * Gets unread notification count for a user.
     *
     * @param userId the user ID
     * @return count of unread notifications
     */
    int getUnreadCount(Long userId);
    
    // ==================== Complaint-specific Notifications ====================
    
    /**
     * Notifies about a new complaint submission.
     *
     * @param complaintId the complaint ID
     * @param studentId the student who submitted
     */
    void notifyNewComplaint(Long complaintId, Long studentId);
    
    /**
     * Notifies about complaint assignment.
     *
     * @param complaintId the complaint ID
     * @param adminId the assigned admin ID
     */
    void notifyComplaintAssigned(Long complaintId, Long adminId);
    
    /**
     * Notifies about complaint status change.
     *
     * @param complaintId the complaint ID
     * @param newStatus the new status
     */
    void notifyStatusChange(Long complaintId, String newStatus);
    
    /**
     * Notifies about complaint resolution.
     *
     * @param complaintId the complaint ID
     */
    void notifyComplaintResolved(Long complaintId);
    
    /**
     * Notifies about approaching SLA deadline.
     *
     * @param complaintId the complaint ID
     * @param adminId the assigned admin ID
     */
    void notifySLAWarning(Long complaintId, Long adminId);
    
    /**
     * Notifies about SLA breach.
     *
     * @param complaintId the complaint ID
     */
    void notifySLABreach(Long complaintId);
    
    /**
     * Notifies about feedback received.
     *
     * @param complaintId the complaint ID
     * @param adminId the admin who handled the complaint
     */
    void notifyFeedbackReceived(Long complaintId, Long adminId);
}
