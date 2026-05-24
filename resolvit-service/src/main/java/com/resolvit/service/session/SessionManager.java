package com.resolvit.service.session;

import com.resolvit.core.entities.User;
import com.resolvit.core.enums.Role;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Singleton class to manage the current user session.
 * Provides thread-safe access to the logged-in user's information.
 */
public class SessionManager {
    
    private static SessionManager instance;
    
    private User currentUser;
    private String sessionToken;
    private LocalDateTime sessionStart;
    private LocalDateTime lastActivity;
    
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    
    private SessionManager() {
        // Private constructor for singleton
    }
    
    /**
     * Gets the singleton instance of SessionManager.
     *
     * @return the SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Creates a new session for the given user.
     *
     * @param user the user to create a session for
     * @param token the session token
     */
    public void createSession(User user, String token) {
        this.currentUser = user;
        this.sessionToken = token;
        this.sessionStart = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
    }
    
    /**
     * Destroys the current session.
     */
    public void destroySession() {
        this.currentUser = null;
        this.sessionToken = null;
        this.sessionStart = null;
        this.lastActivity = null;
    }
    
    /**
     * Gets the currently logged-in user.
     *
     * @return an Optional containing the current user if logged in
     */
    public Optional<User> getCurrentUser() {
        if (isSessionValid()) {
            updateLastActivity();
            return Optional.ofNullable(currentUser);
        }
        return Optional.empty();
    }
    
    /**
     * Gets the current session token.
     *
     * @return the session token, or null if no session
     */
    public String getSessionToken() {
        return sessionToken;
    }
    
    /**
     * Checks if a user is currently logged in with a valid session.
     *
     * @return true if a valid session exists
     */
    public boolean isLoggedIn() {
        return isSessionValid() && currentUser != null;
    }
    
    /**
     * Checks if the current session is valid (not expired).
     *
     * @return true if the session is valid
     */
    public boolean isSessionValid() {
        if (lastActivity == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(lastActivity.plusMinutes(SESSION_TIMEOUT_MINUTES));
    }
    
    /**
     * Updates the last activity timestamp.
     */
    public void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }
    
    /**
     * Gets the current user's ID.
     *
     * @return the user ID, or null if not logged in
     */
    public Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }
    
    /**
     * Gets the current user's role.
     *
     * @return the user's role, or null if not logged in
     */
    public Role getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }
    
    /**
     * Checks if the current user has a specific role.
     *
     * @param role the role to check
     * @return true if the user has the role
     */
    public boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }
    
    /**
     * Checks if the current user is a student.
     *
     * @return true if the user is a student
     */
    public boolean isStudent() {
        return hasRole(Role.STUDENT);
    }
    
    /**
     * Checks if the current user is an admin.
     *
     * @return true if the user is an admin
     */
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }
    
    /**
     * Checks if the current user is a super admin.
     *
     * @return true if the user is a super admin
     */
    public boolean isSuperAdmin() {
        return hasRole(Role.SUPER_ADMIN);
    }
    
    /**
     * Checks if the current user has admin privileges (admin or super admin).
     *
     * @return true if the user has admin privileges
     */
    public boolean hasAdminPrivileges() {
        return isAdmin() || isSuperAdmin();
    }
    
    /**
     * Gets the session start time.
     *
     * @return the session start time
     */
    public LocalDateTime getSessionStart() {
        return sessionStart;
    }
    
    /**
     * Gets the last activity time.
     *
     * @return the last activity time
     */
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    /**
     * Gets the remaining session time in minutes.
     *
     * @return remaining minutes, or 0 if session expired
     */
    public long getRemainingMinutes() {
        if (lastActivity == null) {
            return 0;
        }
        LocalDateTime expiry = lastActivity.plusMinutes(SESSION_TIMEOUT_MINUTES);
        if (LocalDateTime.now().isAfter(expiry)) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiry).toMinutes();
    }
}
