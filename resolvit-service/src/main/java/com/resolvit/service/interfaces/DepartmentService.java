package com.resolvit.service.interfaces;

import com.resolvit.core.entities.Department;
import com.resolvit.core.enums.Category;
import com.resolvit.core.exceptions.DuplicateEntryException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for department management operations.
 */
public interface DepartmentService {
    
    /**
     * Creates a new department.
     *
     * @param department the department to create
     * @return the created department
     * @throws DuplicateEntryException if department name or code already exists
     */
    Department createDepartment(Department department) throws DuplicateEntryException;
    
    /**
     * Updates a department.
     *
     * @param department the department with updated information
     * @return the updated department
     */
    Department updateDepartment(Department department);
    
    /**
     * Deletes a department (soft delete).
     *
     * @param departmentId the department ID
     * @return true if deleted successfully
     */
    boolean deleteDepartment(Long departmentId);
    
    /**
     * Finds a department by ID.
     *
     * @param departmentId the department ID
     * @return an Optional containing the department if found
     */
    Optional<Department> findById(Long departmentId);
    
    /**
     * Finds a department by name.
     *
     * @param name the department name
     * @return an Optional containing the department if found
     */
    Optional<Department> findByName(String name);
    
    /**
     * Finds a department by code.
     *
     * @param code the department code
     * @return an Optional containing the department if found
     */
    Optional<Department> findByCode(String code);
    
    /**
     * Gets all departments.
     *
     * @return list of all departments
     */
    List<Department> getAllDepartments();
    
    /**
     * Gets all active departments.
     *
     * @return list of active departments
     */
    List<Department> getActiveDepartments();
    
    /**
     * Gets the department responsible for a category.
     *
     * @param category the complaint category
     * @return an Optional containing the responsible department
     */
    Optional<Department> getDepartmentByCategory(Category category);
    
    /**
     * Adds a category to a department's responsibilities.
     *
     * @param departmentId the department ID
     * @param category the category to add
     * @return true if added successfully
     */
    boolean addCategoryToDepartment(Long departmentId, Category category);
    
    /**
     * Removes a category from a department's responsibilities.
     *
     * @param departmentId the department ID
     * @param category the category to remove
     * @return true if removed successfully
     */
    boolean removeCategoryFromDepartment(Long departmentId, Category category);
    
    /**
     * Gets categories handled by a department.
     *
     * @param departmentId the department ID
     * @return list of categories
     */
    List<Category> getCategoriesByDepartment(Long departmentId);
    
    /**
     * Gets department statistics.
     *
     * @param departmentId the department ID
     * @return map of statistics
     */
    Map<String, Object> getDepartmentStatistics(Long departmentId);
    
    /**
     * Gets admin count for a department.
     *
     * @param departmentId the department ID
     * @return count of admins
     */
    int getAdminCount(Long departmentId);
    
    /**
     * Gets active complaint count for a department.
     *
     * @param departmentId the department ID
     * @return count of active complaints
     */
    int getActiveComplaintCount(Long departmentId);
    
    /**
     * Checks if a department name is available.
     *
     * @param name the name to check
     * @return true if available
     */
    boolean isNameAvailable(String name);
    
    /**
     * Checks if a department code is available.
     *
     * @param code the code to check
     * @return true if available
     */
    boolean isCodeAvailable(String code);
}
