package com.resolvit.service.factory;

import com.resolvit.dao.impl.*;
import com.resolvit.dao.interfaces.*;
import com.resolvit.service.impl.*;
import com.resolvit.service.interfaces.*;

/**
 * Factory class for creating and managing service instances.
 * Implements singleton pattern for service instances.
 */
public class ServiceFactory {
    
    private static ServiceFactory instance;
    
    // DAO instances
    private UserDAO userDAO;
    private ComplaintDAO complaintDAO;
    private DepartmentDAO departmentDAO;
    private AssignmentDAO assignmentDAO;
    private NotificationDAO notificationDAO;
    private AuditLogDAO auditLogDAO;
    
    // Service instances
    private AuthenticationService authenticationService;
    private UserService userService;
    private ComplaintService complaintService;
    private DepartmentService departmentService;
    private NotificationService notificationService;
    private AuditService auditService;
    
    private ServiceFactory() {
        initializeDAOs();
        initializeServices();
    }
    
    /**
     * Gets the singleton instance of ServiceFactory.
     *
     * @return the ServiceFactory instance
     */
    public static synchronized ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }
    
    /**
     * Initializes all DAO instances.
     */
    private void initializeDAOs() {
        userDAO = new UserDAOImpl();
        complaintDAO = new ComplaintDAOImpl();
        departmentDAO = new DepartmentDAOImpl();
        assignmentDAO = new AssignmentDAOImpl();
        notificationDAO = new NotificationDAOImpl();
        auditLogDAO = new AuditLogDAOImpl();
    }
    
    /**
     * Initializes all service instances.
     */
    private void initializeServices() {
        // Initialize AuditService first (no dependencies)
        auditService = new AuditServiceImpl(auditLogDAO);
        
        // Initialize AuthenticationService
        authenticationService = new AuthenticationServiceImpl(userDAO, auditService);
        
        // Initialize NotificationService
        notificationService = new NotificationServiceImpl(notificationDAO, complaintDAO);
        
        // Initialize UserService
        userService = new UserServiceImpl(userDAO, authenticationService);
        
        // Initialize DepartmentService
        departmentService = new DepartmentServiceImpl(departmentDAO);
        
        // Initialize ComplaintService
        complaintService = new ComplaintServiceImpl(
            complaintDAO, userDAO, departmentDAO, notificationService, auditService
        );
    }
    
    // ==================== Service Getters ====================
    
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }
    
    public UserService getUserService() {
        return userService;
    }
    
    public ComplaintService getComplaintService() {
        return complaintService;
    }
    
    public DepartmentService getDepartmentService() {
        return departmentService;
    }
    
    public NotificationService getNotificationService() {
        return notificationService;
    }
    
    public AuditService getAuditService() {
        return auditService;
    }
    
    // ==================== DAO Getters (for testing/direct access) ====================
    
    public UserDAO getUserDAO() {
        return userDAO;
    }
    
    public ComplaintDAO getComplaintDAO() {
        return complaintDAO;
    }
    
    public DepartmentDAO getDepartmentDAO() {
        return departmentDAO;
    }
    
    public AssignmentDAO getAssignmentDAO() {
        return assignmentDAO;
    }
    
    public NotificationDAO getNotificationDAO() {
        return notificationDAO;
    }
    
    public AuditLogDAO getAuditLogDAO() {
        return auditLogDAO;
    }
    
    /**
     * Resets the factory (useful for testing).
     */
    public static synchronized void reset() {
        instance = null;
    }
}
