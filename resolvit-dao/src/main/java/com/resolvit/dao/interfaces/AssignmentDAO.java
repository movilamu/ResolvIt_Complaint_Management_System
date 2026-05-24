package com.resolvit.dao.interfaces;

import com.resolvit.core.entities.Assignment;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Assignment operations.
 */
public interface AssignmentDAO {

    /**
     * Save a new assignment.
     * 
     * @param assignment the assignment to save
     * @return the saved assignment's ID
     * @throws SQLException if a database error occurs
     */
    String save(Assignment assignment) throws SQLException;

    /**
     * Find an assignment by ID.
     * 
     * @param assignmentId the assignment ID
     * @return Optional containing the assignment if found
     * @throws SQLException if a database error occurs
     */
    Optional<Assignment> findById(String assignmentId) throws SQLException;

    /**
     * Find assignments for a specific complaint.
     * 
     * @param complaintId the complaint ID
     * @return list of assignments for the complaint
     * @throws SQLException if a database error occurs
     */
    List<Assignment> findByComplaintId(String complaintId) throws SQLException;

    /**
     * Find the latest assignment for a complaint.
     * 
     * @param complaintId the complaint ID
     * @return Optional containing the latest assignment if found
     * @throws SQLException if a database error occurs
     */
    Optional<Assignment> findLatestByComplaintId(String complaintId) throws SQLException;

    /**
     * Find all assignments for a department.
     * 
     * @param deptId the department ID
     * @return list of assignments for the department
     * @throws SQLException if a database error occurs
     */
    List<Assignment> findByDepartmentId(int deptId) throws SQLException;

    /**
     * Find all assignments made by a specific user.
     * 
     * @param userId the user ID
     * @return list of assignments made by the user
     * @throws SQLException if a database error occurs
     */
    List<Assignment> findByAssignedBy(String userId) throws SQLException;

    /**
     * Get all assignments.
     * 
     * @return list of all assignments
     * @throws SQLException if a database error occurs
     */
    List<Assignment> findAll() throws SQLException;

    /**
     * Update an assignment.
     * 
     * @param assignment the assignment with updated information
     * @return true if update was successful
     * @throws SQLException if a database error occurs
     */
    boolean update(Assignment assignment) throws SQLException;

    /**
     * Delete an assignment.
     * 
     * @param assignmentId the assignment ID
     * @return true if deletion was successful
     * @throws SQLException if a database error occurs
     */
    boolean delete(String assignmentId) throws SQLException;

    /**
     * Delete all assignments for a complaint.
     * 
     * @param complaintId the complaint ID
     * @return number of deleted assignments
     * @throws SQLException if a database error occurs
     */
    int deleteByComplaintId(String complaintId) throws SQLException;

    /**
     * Count assignments by department.
     * 
     * @param deptId the department ID
     * @return count of assignments for the department
     * @throws SQLException if a database error occurs
     */
    int countByDepartment(int deptId) throws SQLException;
}
