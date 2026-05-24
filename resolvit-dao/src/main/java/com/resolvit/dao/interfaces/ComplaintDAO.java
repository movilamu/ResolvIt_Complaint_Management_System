package com.resolvit.dao.interfaces;

import com.resolvit.core.entities.Complaint;
import com.resolvit.core.enums.Category;
import com.resolvit.core.enums.Priority;
import com.resolvit.core.enums.Status;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object interface for Complaint operations.
 */
public interface ComplaintDAO {

    /**
     * Save a new complaint to the database.
     * 
     * @param complaint the complaint to save
     * @return the saved complaint's ID
     * @throws SQLException if a database error occurs
     */
    String save(Complaint complaint) throws SQLException;

    /**
     * Find a complaint by its ID.
     * 
     * @param complaintId the complaint ID
     * @return Optional containing the complaint if found
     * @throws SQLException if a database error occurs
     */
    Optional<Complaint> findById(String complaintId) throws SQLException;

    /**
     * Find all complaints by a specific user.
     * 
     * @param userId the user ID
     * @return list of complaints by the user
     * @throws SQLException if a database error occurs
     */
    List<Complaint> findByUserId(String userId) throws SQLException;

    /**
     * Get all complaints.
     * 
     * @return list of all complaints
     * @throws SQLException if a database error occurs
     */
    List<Complaint> findAll() throws SQLException;

    /**
     * Get all complaints with pagination.
     * 
     * @param offset the starting offset
     * @param limit the maximum number of results
     * @return list of complaints
     * @throws SQLException if a database error occurs
     */
    List<Complaint> findAllPaginated(int offset, int limit) throws SQLException;

    /**
     * Find complaints by status.
     * 
     * @param status the status to filter by
     * @return list of complaints with the specified status
     * @throws SQLException if a database error occurs
     */
    List<Complaint> findByStatus(Status status) throws SQLException;

    /**
     * Find complaints by category.
     * 
     * @param category the category to filter by
     * @return list of complaints in the category
     * @throws SQLException if a database error occurs
     */
    List<Complaint> findByCategory(Category category) throws SQLException;

    /**
     * Find complaints by priority.
     * 
     * @param priority the priority to filter by
     * @return list of complaints with the priority
     * @throws SQLException if a database error occurs
     */
    List<Complaint> findByPriority(Priority priority) throws SQLException;

    /**
     * Find complaints assigned to a specific department.
     * 
     * @param deptId the department ID
     * @return list of complaints assigned to the department
     * @throws SQLException if a database error occurs
     */
    List<Complaint> findByDepartment(int deptId) throws SQLException;

    /**
     * Find complaints with multiple filters.
     * 
     * @param status the status filter (can be null)
     * @param category the category filter (can be null)
     * @param priority the priority filter (can be null)
     * @param from the start date filter (can be null)
     * @param to the end date filter (can be null)
     * @return list of filtered complaints
     * @throws SQLException if a database error occurs
     */
    List<Complaint> findWithFilters(Status status, Category category, 
                                     Priority priority, LocalDate from, 
                                     LocalDate to) throws SQLException;

    /**
     * Update a complaint.
     * 
     * @param complaint the complaint with updated information
     * @return true if update was successful
     * @throws SQLException if a database error occurs
     */
    boolean update(Complaint complaint) throws SQLException;

    /**
     * Update complaint status.
     * 
     * @param complaintId the complaint ID
     * @param status the new status
     * @param changedBy the ID of user making the change
     * @param remarks optional remarks for the change
     * @return true if update was successful
     * @throws SQLException if a database error occurs
     */
    boolean updateStatus(String complaintId, Status status, 
                        String changedBy, String remarks) throws SQLException;

    /**
     * Assign a complaint to a department.
     * 
     * @param complaintId the complaint ID
     * @param deptId the department ID
     * @param assignedBy the ID of user making the assignment
     * @param remarks optional remarks
     * @return true if assignment was successful
     * @throws SQLException if a database error occurs
     */
    boolean assignDepartment(String complaintId, int deptId, 
                            String assignedBy, String remarks) throws SQLException;

    /**
     * Increment the view count of a complaint.
     * 
     * @param complaintId the complaint ID
     * @return true if successful
     * @throws SQLException if a database error occurs
     */
    boolean incrementViewCount(String complaintId) throws SQLException;

    /**
     * Delete a complaint.
     * 
     * @param complaintId the complaint ID
     * @return true if deletion was successful
     * @throws SQLException if a database error occurs
     */
    boolean delete(String complaintId) throws SQLException;

    /**
     * Count total complaints.
     * 
     * @return total complaint count
     * @throws SQLException if a database error occurs
     */
    int count() throws SQLException;

    /**
     * Count complaints by status.
     * 
     * @param status the status to count
     * @return count of complaints with the status
     * @throws SQLException if a database error occurs
     */
    int countByStatus(Status status) throws SQLException;

    /**
     * Count complaints by category.
     * 
     * @param category the category to count
     * @return count of complaints in the category
     * @throws SQLException if a database error occurs
     */
    int countByCategory(Category category) throws SQLException;

    /**
     * Count complaints by priority.
     * 
     * @param priority the priority to count
     * @return count of complaints with the priority
     * @throws SQLException if a database error occurs
     */
    int countByPriority(Priority priority) throws SQLException;

    /**
     * Count complaints for a specific user.
     * 
     * @param userId the user ID
     * @return count of complaints by the user
     * @throws SQLException if a database error occurs
     */
    int countByUserId(String userId) throws SQLException;

    /**
     * Get average resolution time in hours.
     * 
     * @return average resolution time
     * @throws SQLException if a database error occurs
     */
    double avgResolutionTimeHours() throws SQLException;

    /**
     * Get monthly complaint trend data for a given year.
     * 
     * @param year the year
     * @return list of maps containing monthly statistics
     * @throws SQLException if a database error occurs
     */
    List<Map<String, Object>> getMonthlyTrend(int year) throws SQLException;

    /**
     * Get complaint statistics by category.
     * 
     * @return map of category to count
     * @throws SQLException if a database error occurs
     */
    Map<Category, Integer> getStatsByCategory() throws SQLException;

    /**
     * Get complaint statistics by status.
     * 
     * @return map of status to count
     * @throws SQLException if a database error occurs
     */
    Map<Status, Integer> getStatsByStatus() throws SQLException;

    /**
     * Search complaints by title or description.
     * 
     * @param searchTerm the search term
     * @return list of matching complaints
     * @throws SQLException if a database error occurs
     */
    List<Complaint> search(String searchTerm) throws SQLException;
}
