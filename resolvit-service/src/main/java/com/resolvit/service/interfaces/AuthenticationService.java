package com.resolvit.service.interfaces;

import com.resolvit.core.entities.User;
import com.resolvit.core.exceptions.AuthenticationException;
import com.resolvit.core.exceptions.SessionExpiredException;

import java.util.Optional;

/**
 * Service interface for authentication operations.
 * Handles user login, logout, session management, and password operations.
 */
public interface AuthenticationService {
    
    /**
     * Authenticates a user with their credentials.
     *
     * @param username the username
     * @param password the plain text password
     * @return the authenticated user
     * @throws AuthenticationException if authentication fails
     */
    User login(String username, String password) throws AuthenticationException;
    
    /**
     * Logs out the current user and invalidates their session.
     *
     * @param userId the ID of the user to logout
     */
    void logout(Long userId);
    
    /**
     * Validates if the current session is still active.
     *
     * @param sessionToken the session token to validate
     * @return true if the session is valid
     * @throws SessionExpiredException if the session has expired
     */
    boolean validateSession(String sessionToken) throws SessionExpiredException;
    
    /**
     * Gets the currently logged-in user from the session.
     *
     * @param sessionToken the session token
     * @return an Optional containing the user if logged in
     */
    Optional<User> getCurrentUser(String sessionToken);
    
    /**
     * Changes a user's password.
     *
     * @param userId the user's ID
     * @param oldPassword the current password
     * @param newPassword the new password
     * @return true if the password was changed successfully
     * @throws AuthenticationException if the old password is incorrect
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword) throws AuthenticationException;
    
    /**
     * Resets a user's password (admin function).
     *
     * @param userId the user's ID
     * @param newPassword the new password
     * @return true if the password was reset successfully
     */
    boolean resetPassword(Long userId, String newPassword);
    
    /**
     * Generates a new session token for the user.
     *
     * @param user the user
     * @return the generated session token
     */
    String generateSessionToken(User user);
    
    /**
     * Hashes a password using BCrypt.
     *
     * @param plainPassword the plain text password
     * @return the hashed password
     */
    String hashPassword(String plainPassword);
    
    /**
     * Verifies a password against a hash.
     *
     * @param plainPassword the plain text password
     * @param hashedPassword the hashed password
     * @return true if the password matches
     */
    boolean verifyPassword(String plainPassword, String hashedPassword);
}
