package com.resolvit.service.interfaces;

import com.resolvit.core.entities.Admin;
import com.resolvit.core.entities.Student;
import com.resolvit.core.entities.SuperAdmin;
import com.resolvit.core.entities.User;
import com.resolvit.core.enums.Role;
import com.resolvit.core.exceptions.DuplicateEntryException;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for user management operations.
 * Handles CRUD operations for all user types.
 */
public interface UserService {
    
    // ==================== Generic User Operations ====================
    
    /**
     * Finds a user by ID.
     *
     * @param userId the user ID
     * @return an Optional containing the user if found
     */
    Optional<User> findById(Long userId);
    
    /**
     * Finds a user by username.
     *
     * @param username the username
     * @return an Optional containing the user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Finds a user by email.
     *
     * @param email the email
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Gets all users.
     *
     * @return list of all users
     */
    List<User> getAllUsers();
    
    /**
     * Gets all users by role.
     *
     * @param role the role to filter by
     * @return list of users with the specified role
     */
    List<User> getUsersByRole(Role role);
    
    /**
     * Gets all active users.
     *
     * @return list of active users
     */
    List<User> getActiveUsers();
    
    /**
     * Deactivates a user account.
     *
     * @param userId the user ID
     * @return true if deactivated successfully
     */
    boolean deactivateUser(Long userId);
    
    /**
     * Activates a user account.
     *
     * @param userId the user ID
     * @return true if activated successfully
     */
    boolean activateUser(Long userId);
    
    /**
     * Deletes a user (soft delete).
     *
     * @param userId the user ID
     * @return true if deleted successfully
     */
    boolean deleteUser(Long userId);
    
    /**
     * Checks if a username is available.
     *
     * @param username the username to check
     * @return true if available
     */
    boolean isUsernameAvailable(String username);
    
    /**
     * Checks if an email is available.
     *
     * @param email the email to check
     * @return true if available
     */
    boolean isEmailAvailable(String email);
    
    // ==================== Student Operations ====================
    
    /**
     * Creates a new student account.
     *
     * @param student the student to create
     * @param plainPassword the plain text password
     * @return the created student
     * @throws DuplicateEntryException if username or email already exists
     */
    Student createStudent(Student student, String plainPassword) throws DuplicateEntryException;
    
    /**
     * Updates a student's information.
     *
     * @param student the student with updated information
     * @return the updated student
     */
    Student updateStudent(Student student);
    
    /**
     * Finds a student by student number.
     *
     * @param studentNumber the student number
     * @return an Optional containing the student if found
     */
    Optional<Student> findStudentByStudentNumber(String studentNumber);
    
    /**
     * Gets all students.
     *
     * @return list of all students
     */
    List<Student> getAllStudents();
    
    /**
     * Gets students by department.
     *
     * @param departmentId the department ID
     * @return list of students in the department
     */
    List<Student> getStudentsByDepartment(Long departmentId);
    
    // ==================== Admin Operations ====================
    
    /**
     * Creates a new admin account.
     *
     * @param admin the admin to create
     * @param plainPassword the plain text password
     * @return the created admin
     * @throws DuplicateEntryException if username or email already exists
     */
    Admin createAdmin(Admin admin, String plainPassword) throws DuplicateEntryException;
    
    /**
     * Updates an admin's information.
     *
     * @param admin the admin with updated information
     * @return the updated admin
     */
    Admin updateAdmin(Admin admin);
    
    /**
     * Finds an admin by employee ID.
     *
     * @param employeeId the employee ID
     * @return an Optional containing the admin if found
     */
    Optional<Admin> findAdminByEmployeeId(String employeeId);
    
    /**
     * Gets all admins.
     *
     * @return list of all admins
     */
    List<Admin> getAllAdmins();
    
    /**
     * Gets admins by department.
     *
     * @param departmentId the department ID
     * @return list of admins in the department
     */
    List<Admin> getAdminsByDepartment(Long departmentId);
    
    /**
     * Gets active admins by department (for assignment).
     *
     * @param departmentId the department ID
     * @return list of active admins in the department
     */
    List<Admin> getActiveAdminsByDepartment(Long departmentId);
    
    /**
     * Gets the admin with the least active complaints in a department.
     *
     * @param departmentId the department ID
     * @return an Optional containing the admin with lowest workload
     */
    Optional<Admin> getAdminWithLowestWorkload(Long departmentId);
    
    // ==================== Super Admin Operations ====================
    
    /**
     * Creates a new super admin account.
     *
     * @param superAdmin the super admin to create
     * @param plainPassword the plain text password
     * @return the created super admin
     * @throws DuplicateEntryException if username or email already exists
     */
    SuperAdmin createSuperAdmin(SuperAdmin superAdmin, String plainPassword) throws DuplicateEntryException;
    
    /**
     * Updates a super admin's information.
     *
     * @param superAdmin the super admin with updated information
     * @return the updated super admin
     */
    SuperAdmin updateSuperAdmin(SuperAdmin superAdmin);
    
    /**
     * Gets all super admins.
     *
     * @return list of all super admins
     */
    List<SuperAdmin> getAllSuperAdmins();
    
    // ==================== Profile Operations ====================
    
    /**
     * Updates a user's profile information.
     *
     * @param userId the user ID
     * @param firstName the new first name
     * @param lastName the new last name
     * @param phone the new phone number
     * @return the updated user
     */
    User updateProfile(Long userId, String firstName, String lastName, String phone);
    
    /**
     * Updates a user's email.
     *
     * @param userId the user ID
     * @param newEmail the new email
     * @return true if updated successfully
     * @throws DuplicateEntryException if email already exists
     */
    boolean updateEmail(Long userId, String newEmail) throws DuplicateEntryException;
    
    // ==================== Statistics ====================
    
    /**
     * Gets the total user count.
     *
     * @return total number of users
     */
    long getTotalUserCount();
    
    /**
     * Gets user count by role.
     *
     * @param role the role
     * @return count of users with the role
     */
    long getUserCountByRole(Role role);
    
    /**
     * Gets active user count.
     *
     * @return count of active users
     */
    long getActiveUserCount();
}
