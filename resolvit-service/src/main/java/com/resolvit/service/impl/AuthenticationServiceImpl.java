package com.resolvit.service.impl;

import com.resolvit.core.entities.User;
import com.resolvit.core.exceptions.AuthenticationException;
import com.resolvit.core.exceptions.SessionExpiredException;
import com.resolvit.dao.interfaces.UserDAO;
import com.resolvit.service.interfaces.AuditService;
import com.resolvit.service.interfaces.AuthenticationService;
import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the AuthenticationService interface.
 * Handles user authentication, session management, and password operations.
 */
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private static final Logger LOGGER = Logger.getLogger(AuthenticationServiceImpl.class.getName());
    private static final int BCRYPT_ROUNDS = 12;
    private static final int SESSION_TOKEN_LENGTH = 32;
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;
    
    private final UserDAO userDAO;
    private final AuditService auditService;
    
    // In-memory session store (in production, use Redis or database)
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, FailedAttemptInfo> failedAttempts = new ConcurrentHashMap<>();
    
    public AuthenticationServiceImpl(UserDAO userDAO, AuditService auditService) {
        this.userDAO = userDAO;
        this.auditService = auditService;
    }
    
    @Override
    public User login(String username, String password) throws AuthenticationException {
        LOGGER.info("Login attempt for user: " + username);
        
        // Check for account lockout
        if (isAccountLocked(username)) {
            LOGGER.warning("Account locked for user: " + username);
            auditService.logLoginAttempt(username, false, "N/A");
            throw new AuthenticationException("Account is temporarily locked due to too many failed attempts. Please try again later.");
        }
        
        // Find user by username
        Optional<User> userOpt = userDAO.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            LOGGER.warning("User not found: " + username);
            recordFailedAttempt(username);
            auditService.logLoginAttempt(username, false, "N/A");
            throw new AuthenticationException("Invalid username or password");
        }
        
        User user = userOpt.get();
        
        // Check if user is active
        if (!user.isActive()) {
            LOGGER.warning("Inactive user attempted login: " + username);
            auditService.logLoginAttempt(username, false, "N/A");
            throw new AuthenticationException("Your account has been deactivated. Please contact support.");
        }
        
        // Verify password
        if (!verifyPassword(password, user.getPasswordHash())) {
            LOGGER.warning("Invalid password for user: " + username);
            recordFailedAttempt(username);
            auditService.logLoginAttempt(username, false, "N/A");
            throw new AuthenticationException("Invalid username or password");
        }
        
        // Clear failed attempts on successful login
        clearFailedAttempts(username);
        
        // Generate session token
        String sessionToken = generateSessionToken(user);
        
        // Update last login time
        user.setLastLogin(LocalDateTime.now());
        userDAO.update(user);
        
        // Log successful login
        auditService.logLoginAttempt(username, true, "N/A");
        
        LOGGER.info("User logged in successfully: " + username);
        return user;
    }
    
    @Override
    public void logout(Long userId) {
        LOGGER.info("Logout for user ID: " + userId);
        
        // Remove all sessions for this user
        activeSessions.entrySet().removeIf(entry -> entry.getValue().getUserId().equals(userId));
        
        // Log logout
        auditService.logLogout(userId, "N/A");
        
        LOGGER.info("User logged out successfully: " + userId);
    }
    
    @Override
    public boolean validateSession(String sessionToken) throws SessionExpiredException {
        if (sessionToken == null || sessionToken.isEmpty()) {
            throw new SessionExpiredException("No session token provided");
        }
        
        SessionInfo session = activeSessions.get(sessionToken);
        
        if (session == null) {
            throw new SessionExpiredException("Session not found or has expired");
        }
        
        if (session.isExpired()) {
            activeSessions.remove(sessionToken);
            throw new SessionExpiredException("Session has expired. Please log in again.");
        }
        
        // Extend session on activity
        session.extendSession(SESSION_TIMEOUT_MINUTES);
        
        return true;
    }
    
    @Override
    public Optional<User> getCurrentUser(String sessionToken) {
        if (sessionToken == null || sessionToken.isEmpty()) {
            return Optional.empty();
        }
        
        SessionInfo session = activeSessions.get(sessionToken);
        
        if (session == null || session.isExpired()) {
            return Optional.empty();
        }
        
        return userDAO.findById(session.getUserId());
    }
    
    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) throws AuthenticationException {
        LOGGER.info("Password change attempt for user ID: " + userId);
        
        Optional<User> userOpt = userDAO.findById(userId);
        
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("User not found");
        }
        
        User user = userOpt.get();
        
        // Verify old password
        if (!verifyPassword(oldPassword, user.getPasswordHash())) {
            LOGGER.warning("Invalid old password for user ID: " + userId);
            throw new AuthenticationException("Current password is incorrect");
        }
        
        // Validate new password
        validatePasswordStrength(newPassword);
        
        // Hash and set new password
        String newHash = hashPassword(newPassword);
        user.setPasswordHash(newHash);
        user.setUpdatedAt(LocalDateTime.now());
        
        userDAO.update(user);
        
        // Log password change
        auditService.logAction(userId, "PASSWORD_CHANGE", "USER", userId, null, null, "N/A");
        
        LOGGER.info("Password changed successfully for user ID: " + userId);
        return true;
    }
    
    @Override
    public boolean resetPassword(Long userId, String newPassword) {
        LOGGER.info("Password reset for user ID: " + userId);
        
        Optional<User> userOpt = userDAO.findById(userId);
        
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Validate new password
        try {
            validatePasswordStrength(newPassword);
        } catch (AuthenticationException e) {
            LOGGER.warning("Weak password during reset: " + e.getMessage());
            return false;
        }
        
        // Hash and set new password
        String newHash = hashPassword(newPassword);
        user.setPasswordHash(newHash);
        user.setUpdatedAt(LocalDateTime.now());
        
        userDAO.update(user);
        
        // Invalidate all existing sessions for this user
        activeSessions.entrySet().removeIf(entry -> entry.getValue().getUserId().equals(userId));
        
        // Log password reset
        auditService.logAction(null, "PASSWORD_RESET", "USER", userId, null, null, "N/A");
        
        LOGGER.info("Password reset successfully for user ID: " + userId);
        return true;
    }
    
    @Override
    public String generateSessionToken(User user) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SESSION_TOKEN_LENGTH];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        
        // Store session info
        SessionInfo session = new SessionInfo(user.getId(), SESSION_TIMEOUT_MINUTES);
        activeSessions.put(token, session);
        
        return token;
    }
    
    @Override
    public String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
    
    @Override
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error verifying password", e);
            return false;
        }
    }
    
    // ==================== Private Helper Methods ====================
    
    private boolean isAccountLocked(String username) {
        FailedAttemptInfo info = failedAttempts.get(username);
        
        if (info == null) {
            return false;
        }
        
        if (info.getAttempts() >= MAX_FAILED_ATTEMPTS) {
            if (info.isLockoutExpired(LOCKOUT_MINUTES)) {
                failedAttempts.remove(username);
                return false;
            }
            return true;
        }
        
        return false;
    }
    
    private void recordFailedAttempt(String username) {
        failedAttempts.compute(username, (key, info) -> {
            if (info == null) {
                return new FailedAttemptInfo();
            }
            info.incrementAttempts();
            return info;
        });
    }
    
    private void clearFailedAttempts(String username) {
        failedAttempts.remove(username);
    }
    
    private void validatePasswordStrength(String password) throws AuthenticationException {
        if (password == null || password.length() < 8) {
            throw new AuthenticationException("Password must be at least 8 characters long");
        }
        
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new AuthenticationException(
                "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
            );
        }
    }
    
    // ==================== Inner Classes ====================
    
    private static class SessionInfo {
        private final Long userId;
        private LocalDateTime expiresAt;
        
        public SessionInfo(Long userId, int timeoutMinutes) {
            this.userId = userId;
            this.expiresAt = LocalDateTime.now().plusMinutes(timeoutMinutes);
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
        
        public void extendSession(int minutes) {
            this.expiresAt = LocalDateTime.now().plusMinutes(minutes);
        }
    }
    
    private static class FailedAttemptInfo {
        private int attempts;
        private LocalDateTime lastAttempt;
        
        public FailedAttemptInfo() {
            this.attempts = 1;
            this.lastAttempt = LocalDateTime.now();
        }
        
        public int getAttempts() {
            return attempts;
        }
        
        public void incrementAttempts() {
            this.attempts++;
            this.lastAttempt = LocalDateTime.now();
        }
        
        public boolean isLockoutExpired(int lockoutMinutes) {
            return LocalDateTime.now().isAfter(lastAttempt.plusMinutes(lockoutMinutes));
        }
    }
}
