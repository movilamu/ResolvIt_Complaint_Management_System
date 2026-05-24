package com.resolvit.dao.interfaces;

import com.resolvit.core.entities.Department;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Department operations.
 */
public interface DepartmentDAO {

    /**
     * Save a new department.
     * 
     * @param department the department to save
     * @return the saved department's ID
     * @throws SQLException if a database error occurs
     */
    int save(Department department) throws SQLException;

    /**
     * Find a department by ID.
     * 
     * @param deptId the department ID
     * @return Optional containing the department if found
     * @throws SQLException if a database error occurs
     */
    Optional<Department> findById(int deptId) throws SQLException;

    /**
     * Find a department by name.
     * 
     * @param deptName the department name
     * @return Optional containing the department if found
     * @throws SQLException if a database error occurs
     */
    Optional<Department> findByName(String deptName) throws SQLException;

    /**
     * Get all departments.
     * 
     * @return list of all departments
     * @throws SQLException if a database error occurs
     */
    List<Department> findAll() throws SQLException;

    /**
     * Get all active departments.
     * 
     * @return list of active departments
     * @throws SQLException if a database error occurs
     */
    List<Department> findAllActive() throws SQLException;

    /**
     * Update department information.
     * 
     * @param department the department with updated information
     * @return true if update was successful
     * @throws SQLException if a database error occurs
     */
    boolean update(Department department) throws SQLException;

    /**
     * Deactivate a department.
     * 
     * @param deptId the department ID
     * @return true if deactivation was successful
     * @throws SQLException if a database error occurs
     */
    boolean deactivate(int deptId) throws SQLException;

    /**
     * Activate a department.
     * 
     * @param deptId the department ID
     * @return true if activation was successful
     * @throws SQLException if a database error occurs
     */
    boolean activate(int deptId) throws SQLException;

    /**
     * Delete a department permanently.
     * 
     * @param deptId the department ID
     * @return true if deletion was successful
     * @throws SQLException if a database error occurs
     */
    boolean delete(int deptId) throws SQLException;

    /**
     * Count total departments.
     * 
     * @return total department count
     * @throws SQLException if a database error occurs
     */
    int count() throws SQLException;

    /**
     * Get complaint count by department.
     * 
     * @param deptId the department ID
     * @return count of complaints assigned to the department
     * @throws SQLException if a database error occurs
     */
    int getComplaintCount(int deptId) throws SQLException;
}
