package com.resolvit.service.impl;

import com.resolvit.core.entities.*;
import com.resolvit.core.enums.Category;
import com.resolvit.core.enums.Priority;
import com.resolvit.core.enums.Role;
import com.resolvit.core.enums.Status;
import com.resolvit.core.exceptions.ComplaintNotFoundException;
import com.resolvit.core.exceptions.DuplicateComplaintException;
import com.resolvit.core.exceptions.InvalidStatusTransitionException;
import com.resolvit.core.exceptions.UnauthorizedAccessException;
import com.resolvit.dao.interfaces.ComplaintDAO;
import com.resolvit.dao.interfaces.DepartmentDAO;
import com.resolvit.dao.interfaces.UserDAO;
import com.resolvit.service.interfaces.AuditService;
import com.resolvit.service.interfaces.ComplaintService;
import com.resolvit.service.interfaces.NotificationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the ComplaintService interface.
 * Handles all complaint-related business logic.
 */
public class ComplaintServiceImpl implements ComplaintService {
    
    private static final Logger LOGGER = Logger.getLogger(ComplaintServiceImpl.class.getName());
    
    // SLA deadlines in hours by priority
    private static final Map<Priority, Integer> SLA_HOURS = Map.of(
        Priority.CRITICAL, 4,
        Priority.HIGH, 24,
        Priority.MEDIUM, 72,
        Priority.LOW, 168
    );
    
    // Valid status transitions
    private static final Map<Status, Set<Status>> VALID_TRANSITIONS = new HashMap<>();
    static {
        VALID_TRANSITIONS.put(Status.SUBMITTED, Set.of(Status.UNDER_REVIEW, Status.REJECTED, Status.CANCELLED));
        VALID_TRANSITIONS.put(Status.UNDER_REVIEW, Set.of(Status.IN_PROGRESS, Status.REJECTED, Status.CANCELLED, Status.ON_HOLD));
        VALID_TRANSITIONS.put(Status.IN_PROGRESS, Set.of(Status.RESOLVED, Status.ON_HOLD, Status.ESCALATED, Status.CANCELLED));
        VALID_TRANSITIONS.put(Status.ON_HOLD, Set.of(Status.IN_PROGRESS, Status.CANCELLED));
        VALID_TRANSITIONS.put(Status.ESCALATED, Set.of(Status.IN_PROGRESS, Status.RESOLVED));
        VALID_TRANSITIONS.put(Status.RESOLVED, Set.of(Status.CLOSED, Status.REOPENED));
        VALID_TRANSITIONS.put(Status.REOPENED, Set.of(Status.IN_PROGRESS, Status.CANCELLED));
        VALID_TRANSITIONS.put(Status.REJECTED, Set.of());
        VALID_TRANSITIONS.put(Status.CANCELLED, Set.of());
        VALID_TRANSITIONS.put(Status.CLOSED, Set.of());
    }
    
    private final ComplaintDAO complaintDAO;
    private final UserDAO userDAO;
    private final DepartmentDAO departmentDAO;
    private final NotificationService notificationService;
    private final AuditService auditService;
    
    private final AtomicLong trackingCounter = new AtomicLong(System.currentTimeMillis() % 100000);
    
    public ComplaintServiceImpl(ComplaintDAO complaintDAO, UserDAO userDAO, 
                                 DepartmentDAO departmentDAO,
                                 NotificationService notificationService,
                                 AuditService auditService) {
        this.complaintDAO = complaintDAO;
        this.userDAO = userDAO;
        this.departmentDAO = departmentDAO;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }
    
    // ==================== CRUD Operations ====================
    
    @Override
    public Complaint createComplaint(Complaint complaint, Long submitterId) throws DuplicateComplaintException {
        LOGGER.info("Creating complaint for student: " + submitterId);
        
        // Check for duplicate
        if (isPotentialDuplicate(complaint)) {
            throw new DuplicateComplaintException(
                "A similar complaint already exists. Please check your existing complaints."
            );
        }
        
        // Set complaint metadata
        complaint.setTrackingNumber(generateTrackingNumber());
        complaint.setSubmittedBy(submitterId);
        complaint.setStatus(Status.SUBMITTED);
        complaint.setSubmittedAt(LocalDateTime.now());
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());
        
        // Calculate SLA deadline
        complaint.setSlaDeadline(calculateSLADeadline(complaint.getPriority()));
        
        // Determine department based on category
        Optional<Department> deptOpt = departmentDAO.findByCategory(complaint.getCategory());
        deptOpt.ifPresent(dept -> complaint.setDepartmentId(dept.getId()));
        
        // Save complaint
        Complaint saved = complaintDAO.save(complaint);
        
        // Create initial status history
        addStatusHistory(saved.getId(), null, Status.SUBMITTED, submitterId, "Complaint submitted");
        
        // Send notifications
        notificationService.notifyNewComplaint(saved.getId(), submitterId);
        
        // Log action
        auditService.logComplaintAction(submitterId, saved.getId(), "CREATE", 
            "Complaint created with tracking number: " + saved.getTrackingNumber());
        
        LOGGER.info("Complaint created with ID: " + saved.getId() + ", Tracking: " + saved.getTrackingNumber());
        return saved;
    }
    
    @Override
    public Complaint updateComplaint(Complaint complaint, Long userId) 
            throws ComplaintNotFoundException, UnauthorizedAccessException {
        LOGGER.info("Updating complaint: " + complaint.getId() + " by user: " + userId);
        
        Optional<Complaint> existingOpt = complaintDAO.findById(complaint.getId());
        if (existingOpt.isEmpty()) {
            throw new ComplaintNotFoundException(complaint.getId());
        }
        
        Complaint existing = existingOpt.get();
        
        // Check authorization
        if (!canModifyComplaint(existing, userId)) {
            throw new UnauthorizedAccessException(userId, "modify complaint " + complaint.getId());
        }
        
        // Only allow updates if complaint is in editable state
        if (!existing.getStatus().isEditable()) {
            throw new UnauthorizedAccessException(userId, 
                "modify complaint in " + existing.getStatus() + " status");
        }
        
        // Update allowed fields
        existing.setTitle(complaint.getTitle());
        existing.setDescription(complaint.getDescription());
        existing.setCategory(complaint.getCategory());
        existing.setPriority(complaint.getPriority());
        existing.setLocation(complaint.getLocation());
        existing.setUpdatedAt(LocalDateTime.now());
        
        // Recalculate SLA if priority changed
        if (complaint.getPriority() != existing.getPriority()) {
            existing.setSlaDeadline(calculateSLADeadline(complaint.getPriority()));
        }
        
        Complaint updated = complaintDAO.update(existing);
        
        // Log action
        auditService.logComplaintAction(userId, complaint.getId(), "UPDATE", "Complaint details updated");
        
        return updated;
    }
    
    @Override
    public void deleteComplaint(Long complaintId, Long userId) 
            throws ComplaintNotFoundException, UnauthorizedAccessException {
        LOGGER.info("Deleting complaint: " + complaintId + " by user: " + userId);
        
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            throw new ComplaintNotFoundException(complaintId);
        }
        
        Complaint complaint = complaintOpt.get();
        
        // Check authorization (only submitter or super admin can delete)
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UnauthorizedAccessException(userId, "delete complaint");
        }
        
        User user = userOpt.get();
        if (!complaint.getSubmittedBy().equals(userId) && user.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedAccessException(userId, "delete complaint " + complaintId);
        }
        
        // Soft delete
        complaintDAO.delete(complaintId);
        
        // Log action
        auditService.logComplaintAction(userId, complaintId, "DELETE", "Complaint deleted");
        
        LOGGER.info("Complaint deleted: " + complaintId);
    }
    
    @Override
    public Optional<Complaint> findById(Long complaintId) {
        return complaintDAO.findById(complaintId);
    }
    
    @Override
    public Optional<Complaint> findByTrackingNumber(String trackingNumber) {
        return complaintDAO.findByTrackingNumber(trackingNumber);
    }
    
    // ==================== Query Operations ====================
    
    @Override
    public List<Complaint> getComplaintsByStudent(Long studentId) {
        return complaintDAO.findBySubmitter(studentId);
    }
    
    @Override
    public List<Complaint> getComplaintsByAssignedAdmin(Long adminId) {
        return complaintDAO.findByAssignedAdmin(adminId);
    }
    
    @Override
    public List<Complaint> getComplaintsByDepartment(Long departmentId) {
        return complaintDAO.findByDepartment(departmentId);
    }
    
    @Override
    public List<Complaint> getComplaintsByStatus(Status status) {
        return complaintDAO.findByStatus(status);
    }
    
    @Override
    public List<Complaint> getComplaintsByCategory(Category category) {
        return complaintDAO.findByCategory(category);
    }
    
    @Override
    public List<Complaint> getComplaintsByPriority(Priority priority) {
        return complaintDAO.findByPriority(priority);
    }
    
    @Override
    public List<Complaint> getComplaintsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return complaintDAO.findByDateRange(startDate, endDate);
    }
    
    @Override
    public List<Complaint> getAllComplaints() {
        return complaintDAO.findAll();
    }
    
    @Override
    public List<Complaint> searchComplaints(String keyword) {
        return complaintDAO.searchByKeyword(keyword);
    }
    
    @Override
    public List<Complaint> getOverdueComplaints() {
        return complaintDAO.findOverdue();
    }
    
    @Override
    public List<Complaint> getComplaintsApproachingDeadline(int hoursBeforeDeadline) {
        LocalDateTime threshold = LocalDateTime.now().plusHours(hoursBeforeDeadline);
        return complaintDAO.findApproachingDeadline(threshold);
    }
    
    // ==================== Status Management ====================
    
    @Override
    public Complaint updateStatus(Long complaintId, Status newStatus, Long userId, String notes)
            throws ComplaintNotFoundException, InvalidStatusTransitionException, UnauthorizedAccessException {
        LOGGER.info("Updating status of complaint " + complaintId + " to " + newStatus);
        
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            throw new ComplaintNotFoundException(complaintId);
        }
        
        Complaint complaint = complaintOpt.get();
        Status oldStatus = complaint.getStatus();
        
        // Validate transition
        if (!isValidStatusTransition(oldStatus, newStatus)) {
            throw new InvalidStatusTransitionException(oldStatus, newStatus);
        }
        
        // Check authorization
        if (!canChangeStatus(complaint, userId, newStatus)) {
            throw new UnauthorizedAccessException(userId, "change status to " + newStatus);
        }
        
        // Update status
        complaint.setStatus(newStatus);
        complaint.setUpdatedAt(LocalDateTime.now());
        
        // Set resolved time if applicable
        if (newStatus == Status.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }
        
        Complaint updated = complaintDAO.update(complaint);
        
        // Create status history entry
        addStatusHistory(complaintId, oldStatus, newStatus, userId, notes);
        
        // Send notification
        notificationService.notifyStatusChange(complaintId, newStatus.getDisplayName());
        
        // Log action
        auditService.logComplaintAction(userId, complaintId, "STATUS_CHANGE", 
            "Status changed from " + oldStatus + " to " + newStatus + ". Notes: " + notes);
        
        return updated;
    }
    
    @Override
    public List<StatusHistory> getStatusHistory(Long complaintId) {
        return complaintDAO.getStatusHistory(complaintId);
    }
    
    @Override
    public boolean isValidStatusTransition(Status currentStatus, Status newStatus) {
        if (currentStatus == null) {
            return newStatus == Status.SUBMITTED;
        }
        Set<Status> validTargets = VALID_TRANSITIONS.get(currentStatus);
        return validTargets != null && validTargets.contains(newStatus);
    }
    
    // ==================== Assignment Operations ====================
    
    @Override
    public Complaint assignToAdmin(Long complaintId, Long adminId, Long assignedBy)
            throws ComplaintNotFoundException {
        LOGGER.info("Assigning complaint " + complaintId + " to admin " + adminId);
        
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            throw new ComplaintNotFoundException(complaintId);
        }
        
        Complaint complaint = complaintOpt.get();
        complaint.setAssignedTo(adminId);
        complaint.setAssignedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());
        
        // If status is SUBMITTED, move to UNDER_REVIEW
        if (complaint.getStatus() == Status.SUBMITTED) {
            complaint.setStatus(Status.UNDER_REVIEW);
            addStatusHistory(complaintId, Status.SUBMITTED, Status.UNDER_REVIEW, 
                assignedBy, "Complaint assigned to admin");
        }
        
        Complaint updated = complaintDAO.update(complaint);
        
        // Create assignment record
        complaintDAO.createAssignment(complaintId, adminId, assignedBy, null);
        
        // Notify admin
        notificationService.notifyComplaintAssigned(complaintId, adminId);
        
        // Log action
        auditService.logComplaintAction(assignedBy, complaintId, "ASSIGN", 
            "Complaint assigned to admin ID: " + adminId);
        
        return updated;
    }
    
    @Override
    public Complaint reassignComplaint(Long complaintId, Long newAdminId, Long reassignedBy, String reason)
            throws ComplaintNotFoundException {
        LOGGER.info("Reassigning complaint " + complaintId + " to admin " + newAdminId);
        
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            throw new ComplaintNotFoundException(complaintId);
        }
        
        Complaint complaint = complaintOpt.get();
        Long oldAdminId = complaint.getAssignedTo();
        
        complaint.setAssignedTo(newAdminId);
        complaint.setAssignedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());
        
        Complaint updated = complaintDAO.update(complaint);
        
        // Create new assignment record
        complaintDAO.createAssignment(complaintId, newAdminId, reassignedBy, reason);
        
        // Notify new admin
        notificationService.notifyComplaintAssigned(complaintId, newAdminId);
        
        // Log action
        auditService.logComplaintAction(reassignedBy, complaintId, "REASSIGN", 
            "Complaint reassigned from admin " + oldAdminId + " to " + newAdminId + ". Reason: " + reason);
        
        return updated;
    }
    
    @Override
    public Complaint autoAssignComplaint(Long complaintId) throws ComplaintNotFoundException {
        LOGGER.info("Auto-assigning complaint: " + complaintId);
        
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            throw new ComplaintNotFoundException(complaintId);
        }
        
        Complaint complaint = complaintOpt.get();
        
        // Find admin with lowest workload in the department
        if (complaint.getDepartmentId() != null) {
            List<Admin> admins = userDAO.findAdminsByDepartment(complaint.getDepartmentId())
                .stream()
                .filter(User::isActive)
                .map(u -> (Admin) u)
                .collect(Collectors.toList());
            
            if (!admins.isEmpty()) {
                // Find admin with fewest active complaints
                Admin bestAdmin = null;
                int lowestCount = Integer.MAX_VALUE;
                
                for (Admin admin : admins) {
                    int count = complaintDAO.countActiveByAssignedAdmin(admin.getId());
                    if (count < lowestCount) {
                        lowestCount = count;
                        bestAdmin = admin;
                    }
                }
                
                if (bestAdmin != null) {
                    return assignToAdmin(complaintId, bestAdmin.getId(), null);
                }
            }
        }
        
        LOGGER.warning("Could not auto-assign complaint " + complaintId + ": no available admins");
        return complaint;
    }
    
    // ==================== Feedback Operations ====================
    
    @Override
    public Feedback addFeedback(Long complaintId, Feedback feedback, Long studentId)
            throws ComplaintNotFoundException, UnauthorizedAccessException {
        LOGGER.info("Adding feedback to complaint: " + complaintId);
        
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            throw new ComplaintNotFoundException(complaintId);
        }
        
        Complaint complaint = complaintOpt.get();
        
        // Verify student is the complainant
        if (!complaint.getSubmittedBy().equals(studentId)) {
            throw new UnauthorizedAccessException(studentId, "provide feedback for complaint " + complaintId);
        }
        
        // Verify complaint is resolved or closed
        if (complaint.getStatus() != Status.RESOLVED && complaint.getStatus() != Status.CLOSED) {
            throw new UnauthorizedAccessException(studentId, 
                "provide feedback for complaint in " + complaint.getStatus() + " status");
        }
        
        feedback.setComplaintId(complaintId);
        feedback.setSubmittedBy(studentId);
        feedback.setSubmittedAt(LocalDateTime.now());
        
        Feedback saved = complaintDAO.saveFeedback(feedback);
        
        // Notify admin if assigned
        if (complaint.getAssignedTo() != null) {
            notificationService.notifyFeedbackReceived(complaintId, complaint.getAssignedTo());
        }
        
        // Log action
        auditService.logComplaintAction(studentId, complaintId, "FEEDBACK", 
            "Feedback submitted with rating: " + feedback.getRating());
        
        return saved;
    }
    
    @Override
    public Optional<Feedback> getFeedback(Long complaintId) {
        return complaintDAO.getFeedback(complaintId);
    }
    
    // ==================== Statistics and Reports ====================
    
    @Override
    public Map<String, Object> getDepartmentStatistics(Long departmentId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Complaint> complaints = complaintDAO.findByDepartment(departmentId);
        
        stats.put("totalComplaints", complaints.size());
        stats.put("byStatus", getCountByStatusForList(complaints));
        stats.put("byPriority", getCountByPriorityForList(complaints));
        stats.put("averageResolutionTime", calculateAverageResolutionTime(complaints));
        stats.put("overdueCount", complaints.stream()
            .filter(c -> c.getSlaDeadline() != null && 
                        LocalDateTime.now().isAfter(c.getSlaDeadline()) &&
                        c.getStatus().isActive())
            .count());
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Complaint> allComplaints = complaintDAO.findAll();
        
        stats.put("totalComplaints", allComplaints.size());
        stats.put("byStatus", getCountByStatus());
        stats.put("byCategory", getCountByCategory());
        stats.put("byPriority", getCountByPriorityForList(allComplaints));
        stats.put("averageResolutionTime", getAverageResolutionTime());
        stats.put("overdueCount", getOverdueComplaints().size());
        
        return stats;
    }
    
    @Override
    public Map<Status, Long> getCountByStatus() {
        return complaintDAO.countByStatus();
    }
    
    @Override
    public Map<Category, Long> getCountByCategory() {
        return complaintDAO.countByCategory();
    }
    
    @Override
    public double getAverageResolutionTime() {
        return complaintDAO.getAverageResolutionTimeHours();
    }
    
    @Override
    public double getAverageResolutionTimeByDepartment(Long departmentId) {
        return complaintDAO.getAverageResolutionTimeHoursByDepartment(departmentId);
    }
    
    // ==================== Utility Operations ====================
    
    @Override
    public String generateTrackingNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = trackingCounter.incrementAndGet();
        return String.format("CMP-%s-%05d", datePart, counter % 100000);
    }
    
    @Override
    public LocalDateTime calculateSLADeadline(Priority priority) {
        int hours = SLA_HOURS.getOrDefault(priority, 72);
        return LocalDateTime.now().plusHours(hours);
    }
    
    @Override
    public boolean isPotentialDuplicate(Complaint complaint) {
        // Check for similar complaints from same student in last 24 hours
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<Complaint> recent = complaintDAO.findBySubmitter(complaint.getSubmittedBy())
            .stream()
            .filter(c -> c.getCreatedAt().isAfter(since))
            .filter(c -> c.getCategory() == complaint.getCategory())
            .collect(Collectors.toList());
        
        for (Complaint existing : recent) {
            if (isSimilarTitle(existing.getTitle(), complaint.getTitle())) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public Complaint escalateComplaint(Long complaintId, Long userId, String reason)
            throws ComplaintNotFoundException {
        LOGGER.info("Escalating complaint: " + complaintId);
        
        Optional<Complaint> complaintOpt = complaintDAO.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            throw new ComplaintNotFoundException(complaintId);
        }
        
        Complaint complaint = complaintOpt.get();
        
        // Increase priority if possible
        Priority currentPriority = complaint.getPriority();
        if (currentPriority.getSeverityLevel() < Priority.CRITICAL.getSeverityLevel()) {
            Priority newPriority = Priority.values()[currentPriority.ordinal() - 1];
            complaint.setPriority(newPriority);
            complaint.setSlaDeadline(calculateSLADeadline(newPriority));
        }
        
        // Update status to ESCALATED
        Status oldStatus = complaint.getStatus();
        complaint.setStatus(Status.ESCALATED);
        complaint.setUpdatedAt(LocalDateTime.now());
        
        Complaint updated = complaintDAO.update(complaint);
        
        // Add status history
        addStatusHistory(complaintId, oldStatus, Status.ESCALATED, userId, 
            "Complaint escalated. Reason: " + reason);
        
        // Log action
        auditService.logComplaintAction(userId, complaintId, "ESCALATE", 
            "Complaint escalated. Reason: " + reason);
        
        return updated;
    }
    
    // ==================== Private Helper Methods ====================
    
    private boolean canModifyComplaint(Complaint complaint, Long userId) {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) return false;
        
        User user = userOpt.get();
        
        // Super admin can modify any complaint
        if (user.getRole() == Role.SUPER_ADMIN) return true;
        
        // Admin assigned to the complaint can modify
        if (user.getRole() == Role.ADMIN && userId.equals(complaint.getAssignedTo())) return true;
        
        // Student who submitted can modify their own complaint
        if (user.getRole() == Role.STUDENT && userId.equals(complaint.getSubmittedBy())) return true;
        
        return false;
    }
    
    private boolean canChangeStatus(Complaint complaint, Long userId, Status newStatus) {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) return false;
        
        User user = userOpt.get();
        
        // Super admin can change any status
        if (user.getRole() == Role.SUPER_ADMIN) return true;
        
        // Admin can change status of assigned complaints
        if (user.getRole() == Role.ADMIN) {
            if (userId.equals(complaint.getAssignedTo())) return true;
            // Admin in same department can also change status
            if (user instanceof Admin) {
                Admin admin = (Admin) user;
                if (admin.getDepartmentId() != null && 
                    admin.getDepartmentId().equals(complaint.getDepartmentId())) {
                    return true;
                }
            }
        }
        
        // Student can only cancel their own complaint
        if (user.getRole() == Role.STUDENT && userId.equals(complaint.getSubmittedBy())) {
            return newStatus == Status.CANCELLED;
        }
        
        return false;
    }
    
    private void addStatusHistory(Long complaintId, Status oldStatus, Status newStatus, 
                                   Long changedBy, String notes) {
        StatusHistory history = new StatusHistory();
        history.setComplaintId(complaintId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setNotes(notes);
        history.setChangedAt(LocalDateTime.now());
        
        complaintDAO.addStatusHistory(history);
    }
    
    private boolean isSimilarTitle(String title1, String title2) {
        if (title1 == null || title2 == null) return false;
        
        String norm1 = title1.toLowerCase().trim();
        String norm2 = title2.toLowerCase().trim();
        
        // Simple similarity check - exact match or high word overlap
        if (norm1.equals(norm2)) return true;
        
        Set<String> words1 = new HashSet<>(Arrays.asList(norm1.split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(norm2.split("\\s+")));
        
        // Calculate Jaccard similarity
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        double similarity = (double) intersection.size() / union.size();
        return similarity > 0.7;
    }
    
    private Map<Status, Long> getCountByStatusForList(List<Complaint> complaints) {
        return complaints.stream()
            .collect(Collectors.groupingBy(Complaint::getStatus, Collectors.counting()));
    }
    
    private Map<Priority, Long> getCountByPriorityForList(List<Complaint> complaints) {
        return complaints.stream()
            .collect(Collectors.groupingBy(Complaint::getPriority, Collectors.counting()));
    }
    
    private double calculateAverageResolutionTime(List<Complaint> complaints) {
        List<Complaint> resolved = complaints.stream()
            .filter(c -> c.getResolvedAt() != null && c.getSubmittedAt() != null)
            .collect(Collectors.toList());
        
        if (resolved.isEmpty()) return 0.0;
        
        double totalHours = resolved.stream()
            .mapToDouble(c -> java.time.Duration.between(c.getSubmittedAt(), c.getResolvedAt()).toHours())
            .sum();
        
        return totalHours / resolved.size();
    }
}
