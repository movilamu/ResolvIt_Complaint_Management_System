package com.resolvit.dao.interfaces;

import com.resolvit.core.entities.User;
import com.resolvit.core.enums.Role;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User operations.
 */
public interface UserDAO {

    /**
     * Save a new user to the database.
     * 
     * @param user the user to save
     * @return the saved user's ID
     * @throws SQLException if a database error occurs
     */
    String save(User user) throws SQLException;

    /**
     * Find a user by their ID.
     * 
     * @param userId the user ID
     * @return Optional containing the user if found
     * @throws SQLException if a database error occurs
     */
    Optional<User> findById(String userId) throws SQLException;

    /**
     * Find a user by their email address.
     * 
     * @param email the email address
     * @return Optional containing the user if found
     * @throws SQLException if a database error occurs
     */
    Optional<User> findByEmail(String email) throws SQLException;

    /**
     * Get all users.
     * 
     * @return list of all users
     * @throws SQLException if a database error occurs
     */
    List<User> findAll() throws SQLException;

    /**
     * Get all users with a specific role.
     * 
     * @param role the role to filter by
     * @return list of users with the specified role
     * @throws SQLException if a database error occurs
     */
    List<User> findByRole(Role role) throws SQLException;

    /**
     * Get all users in a specific department.
     * 
     * @param deptId the department ID
     * @return list of users in the department
     * @throws SQLException if a database error occurs
     */
    List<User> findByDepartment(int deptId) throws SQLException;

    /**
     * Get all active users.
     * 
     * @return list of active users
     * @throws SQLException if a database error occurs
     */
    List<User> findAllActive() throws SQLException;

    /**
     * Update user information.
     * 
     * @param user the user with updated information
     * @return true if update was successful
     * @throws SQLException if a database error occurs
     */
    boolean update(User user) throws SQLException;

    /**
     * Update user's password hash.
     * 
     * @param userId the user ID
     * @param newPasswordHash the new password hash
     * @return true if update was successful
     * @throws SQLException if a database error occurs
     */
    boolean updatePassword(String userId, String newPasswordHash) throws SQLException;

    /**
     * Update user's last login timestamp.
     * 
     * @param userId the user ID
     * @return true if update was successful
     * @throws SQLException if a database error occurs
     */
    boolean updateLastLogin(String userId) throws SQLException;

    /**
     * Deactivate a user account.
     * 
     * @param userId the user ID
     * @return true if deactivation was successful
     * @throws SQLException if a database error occurs
     */
    boolean deactivate(String userId) throws SQLException;

    /**
     * Activate a user account.
     * 
     * @param userId the user ID
     * @return true if activation was successful
     * @throws SQLException if a database error occurs
     */
    boolean activate(String userId) throws SQLException;

    /**
     * Delete a user permanently.
     * 
     * @param userId the user ID
     * @return true if deletion was successful
     * @throws SQLException if a database error occurs
     */
    boolean delete(String userId) throws SQLException;

    /**
     * Check if an email already exists.
     * 
     * @param email the email to check
     * @return true if email exists
     * @throws SQLException if a database error occurs
     */
    boolean existsByEmail(String email) throws SQLException;

    /**
     * Count total users.
     * 
     * @return total user count
     * @throws SQLException if a database error occurs
     */
    int count() throws SQLException;

    /**
     * Count users by role.
     * 
     * @param role the role to count
     * @return count of users with the role
     * @throws SQLException if a database error occurs
     */
    int countByRole(Role role) throws SQLException;

    /**
     * Search users by name or email.
     * 
     * @param searchTerm the search term
     * @return list of matching users
     * @throws SQLException if a database error occurs
     */
    List<User> search(String searchTerm) throws SQLException;
}
