package com.resolvit.service.impl;

import com.resolvit.core.entities.Complaint;
import com.resolvit.core.entities.Notification;
import com.resolvit.core.enums.NotificationType;
import com.resolvit.dao.interfaces.ComplaintDAO;
import com.resolvit.dao.interfaces.NotificationDAO;
import com.resolvit.service.interfaces.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementation of the NotificationService interface.
 */
public class NotificationServiceImpl implements NotificationService {
    
    private static final Logger LOGGER = Logger.getLogger(NotificationServiceImpl.class.getName());
    
    private final NotificationDAO notificationDAO;
    private final ComplaintDAO complaintDAO;
    
    public NotificationServiceImpl(NotificationDAO notificationDAO, ComplaintDAO complaintDAO) {
        this.notificationDAO = notificationDAO;
        this.complaintDAO = complaintDAO;
    }
    
    @Override
    public Notification createNotification(Notification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        return notificationDAO.save(notification);
    }
    
    @Override
    public Notification sendNotification(Long userId, String title, String message, 
                                          NotificationType type, Long referenceId) {
        LOGGER.info("Sending notification to user " + userId + ": " + title);
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        return notificationDAO.save(notification);
    }
    
    @Override
    public List<Notification> getNotificationsByUser(Long userId) {
        return notificationDAO.findByUser(userId);
    }
    
    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationDAO.findUnreadByUser(userId);
    }
    
    @Override
    public List<Notification> getRecentNotifications(Long userId, int limit) {
        return notificationDAO.findRecentByUser(userId, limit);
    }
    
    @Override
    public boolean markAsRead(Long notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }
    
    @Override
    public int markAllAsRead(Long userId) {
        return notificationDAO.markAllAsRead(userId);
    }
    
    @Override
    public boolean deleteNotification(Long notificationId) {
        return notificationDAO.delete(notificationId);
    }
    
    @Override
    public int deleteReadNotifications(Long userId) {
        return notificationDAO.deleteReadByUser(userId);
    }
    
    @Override
    public int getUnreadCount(Long userId) {
        return notificationDAO.countUnread(userId);
    }
    
    // ==================== Complaint-specific Notifications ====================
    
    @Override
    public void notifyNewComplaint(Long complaintId, Long studentId) {
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) return;
        
        Complaint complaint = complaintOpt.get();
        
        String message = String.format(
            "Your complaint '%s' has been submitted successfully. " +
            "Tracking number: %s. We will review it shortly.",
            complaint.getTitle(),
            complaint.getTrackingNumber()
        );
        
        sendNotification(
            studentId,
            "Complaint Submitted",
            message,
            NotificationType.COMPLAINT_SUBMITTED,
            complaintId
        );
    }
    
    @Override
    public void notifyComplaintAssigned(Long complaintId, Long adminId) {
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) return;
        
        Complaint complaint = complaintOpt.get();
        
        String message = String.format(
            "A new complaint has been assigned to you: '%s'. " +
            "Tracking: %s, Priority: %s. Please review and take action.",
            complaint.getTitle(),
            complaint.getTrackingNumber(),
            complaint.getPriority().getDisplayName()
        );
        
        sendNotification(
            adminId,
            "New Complaint Assigned",
            message,
            NotificationType.COMPLAINT_ASSIGNED,
            complaintId
        );
    }
    
    @Override
    public void notifyStatusChange(Long complaintId, String newStatus) {
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) return;
        
        Complaint complaint = complaintOpt.get();
        
        String message = String.format(
            "The status of your complaint '%s' (Tracking: %s) has been updated to: %s",
            complaint.getTitle(),
            complaint.getTrackingNumber(),
            newStatus
        );
        
        sendNotification(
            complaint.getSubmittedBy(),
            "Complaint Status Updated",
            message,
            NotificationType.STATUS_UPDATED,
            complaintId
        );
    }
    
    @Override
    public void notifyComplaintResolved(Long complaintId) {
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) return;
        
        Complaint complaint = complaintOpt.get();
        
        String message = String.format(
            "Great news! Your complaint '%s' (Tracking: %s) has been resolved. " +
            "Please take a moment to provide feedback on your experience.",
            complaint.getTitle(),
            complaint.getTrackingNumber()
        );
        
        sendNotification(
            complaint.getSubmittedBy(),
            "Complaint Resolved",
            message,
            NotificationType.COMPLAINT_RESOLVED,
            complaintId
        );
    }
    
    @Override
    public void notifySLAWarning(Long complaintId, Long adminId) {
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) return;
        
        Complaint complaint = complaintOpt.get();
        
        String message = String.format(
            "Warning: Complaint '%s' (Tracking: %s) is approaching its SLA deadline. " +
            "Please prioritize this complaint to avoid SLA breach.",
            complaint.getTitle(),
            complaint.getTrackingNumber()
        );
        
        sendNotification(
            adminId,
            "SLA Warning",
            message,
            NotificationType.SLA_WARNING,
            complaintId
        );
    }
    
    @Override
    public void notifySLABreach(Long complaintId) {
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) return;
        
        Complaint complaint = complaintOpt.get();
        
        // Notify assigned admin
        if (complaint.getAssignedTo() != null) {
            String adminMessage = String.format(
                "Alert: Complaint '%s' (Tracking: %s) has breached its SLA deadline. " +
                "Immediate action is required.",
                complaint.getTitle(),
                complaint.getTrackingNumber()
            );
            
            sendNotification(
                complaint.getAssignedTo(),
                "SLA Breach Alert",
                adminMessage,
                NotificationType.SLA_BREACH,
                complaintId
            );
        }
        
        // Notify student
        String studentMessage = String.format(
            "We apologize for the delay. Your complaint '%s' (Tracking: %s) " +
            "is taking longer than expected. Our team has been notified and is working on it.",
            complaint.getTitle(),
            complaint.getTrackingNumber()
        );
        
        sendNotification(
            complaint.getSubmittedBy(),
            "Complaint Update",
            studentMessage,
            NotificationType.SYSTEM,
            complaintId
        );
    }
    
    @Override
    public void notifyFeedbackReceived(Long complaintId, Long adminId) {
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) return;
        
        Complaint complaint = complaintOpt.get();
        
        String message = String.format(
            "Feedback has been received for complaint '%s' (Tracking: %s). " +
            "Thank you for your work on this case.",
            complaint.getTitle(),
            complaint.getTrackingNumber()
        );
        
        sendNotification(
            adminId,
            "Feedback Received",
            message,
            NotificationType.FEEDBACK_RECEIVED,
            complaintId
        );
    }
}
