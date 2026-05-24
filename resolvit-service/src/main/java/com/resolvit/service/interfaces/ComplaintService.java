package com.resolvit.service.interfaces;

import com.resolvit.core.entities.Complaint;
import com.resolvit.core.entities.Feedback;
import com.resolvit.core.entities.StatusHistory;
import com.resolvit.core.entities.User;
import com.resolvit.core.enums.Category;
import com.resolvit.core.enums.Priority;
import com.resolvit.core.enums.Status;
import com.resolvit.core.exceptions.ComplaintNotFoundException;
import com.resolvit.core.exceptions.DuplicateComplaintException;
import com.resolvit.core.exceptions.InvalidStatusTransitionException;
import com.resolvit.core.exceptions.UnauthorizedAccessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for complaint management operations.
 * Handles CRUD operations, status transitions, and complaint workflows.
 */
public interface ComplaintService {
    
    // ==================== CRUD Operations ====================
    
    /**
     * Creates a new complaint.
     *
     * @param complaint the complaint to create
     * @param submitterId the ID of the student submitting the complaint
     * @return the created complaint with generated ID and tracking number
     * @throws DuplicateComplaintException if a similar complaint already exists
     */
    Complaint createComplaint(Complaint complaint, Long submitterId) throws DuplicateComplaintException;
    
    /**
     * Updates an existing complaint.
     *
     * @param complaint the complaint with updated information
     * @param userId the ID of the user making the update
     * @return the updated complaint
     * @throws ComplaintNotFoundException if the complaint doesn't exist
     * @throws UnauthorizedAccessException if the user doesn't have permission
     */
    Complaint updateComplaint(Complaint complaint, Long userId) 
            throws ComplaintNotFoundException, UnauthorizedAccessException;
    
    /**
     * Deletes a complaint (soft delete).
     *
     * @param complaintId the ID of the complaint to delete
     * @param userId the ID of the user performing the deletion
     * @throws ComplaintNotFoundException if the complaint doesn't exist
     * @throws UnauthorizedAccessException if the user doesn't have permission
     */
    void deleteComplaint(Long complaintId, Long userId) 
            throws ComplaintNotFoundException, UnauthorizedAccessException;
    
    /**
     * Finds a complaint by its ID.
     *
     * @param complaintId the complaint ID
     * @return an Optional containing the complaint if found
     */
    Optional<Complaint> findById(Long complaintId);
    
    /**
     * Finds a complaint by its tracking number.
     *
     * @param trackingNumber the tracking number
     * @return an Optional containing the complaint if found
     */
    Optional<Complaint> findByTrackingNumber(String trackingNumber);
    
    // ==================== Query Operations ====================
    
    /**
     * Gets all complaints submitted by a specific student.
     *
     * @param studentId the student's ID
     * @return list of complaints
     */
    List<Complaint> getComplaintsByStudent(Long studentId);
    
    /**
     * Gets all complaints assigned to a specific admin.
     *
     * @param adminId the admin's ID
     * @return list of complaints
     */
    List<Complaint> getComplaintsByAssignedAdmin(Long adminId);
    
    /**
     * Gets all complaints for a specific department.
     *
     * @param departmentId the department's ID
     * @return list of complaints
     */
    List<Complaint> getComplaintsByDepartment(Long departmentId);
    
    /**
     * Gets all complaints with a specific status.
     *
     * @param status the status to filter by
     * @return list of complaints
     */
    List<Complaint> getComplaintsByStatus(Status status);
    
    /**
     * Gets all complaints with a specific category.
     *
     * @param category the category to filter by
     * @return list of complaints
     */
    List<Complaint> getComplaintsByCategory(Category category);
    
    /**
     * Gets all complaints with a specific priority.
     *
     * @param priority the priority to filter by
     * @return list of complaints
     */
    List<Complaint> getComplaintsByPriority(Priority priority);
    
    /**
     * Gets complaints created within a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of complaints
     */
    List<Complaint> getComplaintsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Gets all complaints (for super admin).
     *
     * @return list of all complaints
     */
    List<Complaint> getAllComplaints();
    
    /**
     * Searches complaints by keyword in title and description.
     *
     * @param keyword the search keyword
     * @return list of matching complaints
     */
    List<Complaint> searchComplaints(String keyword);
    
    /**
     * Gets overdue complaints (past SLA deadline).
     *
     * @return list of overdue complaints
     */
    List<Complaint> getOverdueComplaints();
    
    /**
     * Gets complaints approaching their SLA deadline.
     *
     * @param hoursBeforeDeadline hours before deadline to consider
     * @return list of complaints approaching deadline
     */
    List<Complaint> getComplaintsApproachingDeadline(int hoursBeforeDeadline);
    
    // ==================== Status Management ====================
    
    /**
     * Updates the status of a complaint.
     *
     * @param complaintId the complaint ID
     * @param newStatus the new status
     * @param userId the ID of the user making the change
     * @param notes optional notes about the status change
     * @return the updated complaint
     * @throws ComplaintNotFoundException if the complaint doesn't exist
     * @throws InvalidStatusTransitionException if the status transition is not allowed
     * @throws UnauthorizedAccessException if the user doesn't have permission
     */
    Complaint updateStatus(Long complaintId, Status newStatus, Long userId, String notes)
            throws ComplaintNotFoundException, InvalidStatusTransitionException, UnauthorizedAccessException;
    
    /**
     * Gets the status history of a complaint.
     *
     * @param complaintId the complaint ID
     * @return list of status history entries
     */
    List<StatusHistory> getStatusHistory(Long complaintId);
    
    /**
     * Validates if a status transition is allowed.
     *
     * @param currentStatus the current status
     * @param newStatus the proposed new status
     * @return true if the transition is valid
     */
    boolean isValidStatusTransition(Status currentStatus, Status newStatus);
    
    // ==================== Assignment Operations ====================
    
    /**
     * Assigns a complaint to an admin.
     *
     * @param complaintId the complaint ID
     * @param adminId the admin's ID
     * @param assignedBy the ID of the user making the assignment
     * @return the updated complaint
     * @throws ComplaintNotFoundException if the complaint doesn't exist
     */
    Complaint assignToAdmin(Long complaintId, Long adminId, Long assignedBy)
            throws ComplaintNotFoundException;
    
    /**
     * Reassigns a complaint to a different admin.
     *
     * @param complaintId the complaint ID
     * @param newAdminId the new admin's ID
     * @param reassignedBy the ID of the user making the reassignment
     * @param reason the reason for reassignment
     * @return the updated complaint
     * @throws ComplaintNotFoundException if the complaint doesn't exist
     */
    Complaint reassignComplaint(Long complaintId, Long newAdminId, Long reassignedBy, String reason)
            throws ComplaintNotFoundException;
    
    /**
     * Auto-assigns a complaint based on department and workload.
     *
     * @param complaintId the complaint ID
     * @return the updated complaint with assigned admin
     * @throws ComplaintNotFoundException if the complaint doesn't exist
     */
    Complaint autoAssignComplaint(Long complaintId) throws ComplaintNotFoundException;
    
    // ==================== Feedback Operations ====================
    
    /**
     * Adds feedback to a resolved complaint.
     *
     * @param complaintId the complaint ID
     * @param feedback the feedback to add
     * @param studentId the ID of the student providing feedback
     * @return the created feedback
     * @throws ComplaintNotFoundException if the complaint doesn't exist
     * @throws UnauthorizedAccessException if the student didn't submit the complaint
     */
    Feedback addFeedback(Long complaintId, Feedback feedback, Long studentId)
            throws ComplaintNotFoundException, UnauthorizedAccessException;
    
    /**
     * Gets the feedback for a complaint.
     *
     * @param complaintId the complaint ID
     * @return an Optional containing the feedback if exists
     */
    Optional<Feedback> getFeedback(Long complaintId);
    
    // ==================== Statistics and Reports ====================
    
    /**
     * Gets complaint statistics for a department.
     *
     * @param departmentId the department ID
     * @return map of statistics (counts by status, priority, etc.)
     */
    Map<String, Object> getDepartmentStatistics(Long departmentId);
    
    /**
     * Gets overall complaint statistics.
     *
     * @return map of overall statistics
     */
    Map<String, Object> getOverallStatistics();
    
    /**
     * Gets complaint count by status.
     *
     * @return map of status to count
     */
    Map<Status, Long> getCountByStatus();
    
    /**
     * Gets complaint count by category.
     *
     * @return map of category to count
     */
    Map<Category, Long> getCountByCategory();
    
    /**
     * Gets average resolution time in hours.
     *
     * @return average resolution time
     */
    double getAverageResolutionTime();
    
    /**
     * Gets average resolution time by department.
     *
     * @param departmentId the department ID
     * @return average resolution time for the department
     */
    double getAverageResolutionTimeByDepartment(Long departmentId);
    
    // ==================== Utility Operations ====================
    
    /**
     * Generates a unique tracking number for a complaint.
     *
     * @return the generated tracking number
     */
    String generateTrackingNumber();
    
    /**
     * Calculates the SLA deadline based on priority.
     *
     * @param priority the complaint priority
     * @return the SLA deadline
     */
    LocalDateTime calculateSLADeadline(Priority priority);
    
    /**
     * Checks if a complaint is a potential duplicate.
     *
     * @param complaint the complaint to check
     * @return true if a similar complaint exists
     */
    boolean isPotentialDuplicate(Complaint complaint);
    
    /**
     * Escalates a complaint to higher priority.
     *
     * @param complaintId the complaint ID
     * @param userId the ID of the user escalating
     * @param reason the reason for escalation
     * @return the updated complaint
     * @throws ComplaintNotFoundException if the complaint doesn't exist
     */
    Complaint escalateComplaint(Long complaintId, Long userId, String reason)
            throws ComplaintNotFoundException;
}
