package com.resolvit.service.impl;

import com.resolvit.core.entities.*;
import com.resolvit.core.enums.Role;
import com.resolvit.core.exceptions.DuplicateEntryException;
import com.resolvit.dao.interfaces.UserDAO;
import com.resolvit.service.interfaces.AuthenticationService;
import com.resolvit.service.interfaces.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the UserService interface.
 * Handles all user management operations.
 */
public class UserServiceImpl implements UserService {
    
    private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
    
    private final UserDAO userDAO;
    private final AuthenticationService authService;
    
    public UserServiceImpl(UserDAO userDAO, AuthenticationService authService) {
        this.userDAO = userDAO;
        this.authService = authService;
    }
    
    // ==================== Generic User Operations ====================
    
    @Override
    public Optional<User> findById(Long userId) {
        return userDAO.findById(userId);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return userDAO.findByUsername(username);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userDAO.findByEmail(email);
    }
    
    @Override
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }
    
    @Override
    public List<User> getUsersByRole(Role role) {
        return userDAO.findByRole(role);
    }
    
    @Override
    public List<User> getActiveUsers() {
        return userDAO.findAll().stream()
            .filter(User::isActive)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean deactivateUser(Long userId) {
        LOGGER.info("Deactivating user: " + userId);
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userDAO.update(user);
        
        LOGGER.info("User deactivated: " + userId);
        return true;
    }
    
    @Override
    public boolean activateUser(Long userId) {
        LOGGER.info("Activating user: " + userId);
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userDAO.update(user);
        
        LOGGER.info("User activated: " + userId);
        return true;
    }
    
    @Override
    public boolean deleteUser(Long userId) {
        LOGGER.info("Deleting user: " + userId);
        return userDAO.delete(userId);
    }
    
    @Override
    public boolean isUsernameAvailable(String username) {
        return userDAO.findByUsername(username).isEmpty();
    }
    
    @Override
    public boolean isEmailAvailable(String email) {
        return userDAO.findByEmail(email).isEmpty();
    }
    
    // ==================== Student Operations ====================
    
    @Override
    public Student createStudent(Student student, String plainPassword) throws DuplicateEntryException {
        LOGGER.info("Creating student: " + student.getUsername());
        
        // Check for duplicates
        if (!isUsernameAvailable(student.getUsername())) {
            throw new DuplicateEntryException("Username already exists: " + student.getUsername());
        }
        if (!isEmailAvailable(student.getEmail())) {
            throw new DuplicateEntryException("Email already exists: " + student.getEmail());
        }
        if (student.getStudentNumber() != null && 
            userDAO.findStudentByStudentNumber(student.getStudentNumber()).isPresent()) {
            throw new DuplicateEntryException("Student number already exists: " + student.getStudentNumber());
        }
        
        // Hash password
        student.setPasswordHash(authService.hashPassword(plainPassword));
        student.setRole(Role.STUDENT);
        student.setActive(true);
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());
        
        Student saved = (Student) userDAO.saveStudent(student);
        LOGGER.info("Student created with ID: " + saved.getId());
        return saved;
    }
    
    @Override
    public Student updateStudent(Student student) {
        LOGGER.info("Updating student: " + student.getId());
        student.setUpdatedAt(LocalDateTime.now());
        return (Student) userDAO.update(student);
    }
    
    @Override
    public Optional<Student> findStudentByStudentNumber(String studentNumber) {
        return userDAO.findStudentByStudentNumber(studentNumber);
    }
    
    @Override
    public List<Student> getAllStudents() {
        return userDAO.findByRole(Role.STUDENT).stream()
            .map(u -> (Student) u)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Student> getStudentsByDepartment(Long departmentId) {
        return userDAO.findStudentsByDepartment(departmentId).stream()
            .map(u -> (Student) u)
            .collect(Collectors.toList());
    }
    
    // ==================== Admin Operations ====================
    
    @Override
    public Admin createAdmin(Admin admin, String plainPassword) throws DuplicateEntryException {
        LOGGER.info("Creating admin: " + admin.getUsername());
        
        // Check for duplicates
        if (!isUsernameAvailable(admin.getUsername())) {
            throw new DuplicateEntryException("Username already exists: " + admin.getUsername());
        }
        if (!isEmailAvailable(admin.getEmail())) {
            throw new DuplicateEntryException("Email already exists: " + admin.getEmail());
        }
        if (admin.getEmployeeId() != null && 
            userDAO.findAdminByEmployeeId(admin.getEmployeeId()).isPresent()) {
            throw new DuplicateEntryException("Employee ID already exists: " + admin.getEmployeeId());
        }
        
        // Hash password
        admin.setPasswordHash(authService.hashPassword(plainPassword));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        
        Admin saved = (Admin) userDAO.saveAdmin(admin);
        LOGGER.info("Admin created with ID: " + saved.getId());
        return saved;
    }
    
    @Override
    public Admin updateAdmin(Admin admin) {
        LOGGER.info("Updating admin: " + admin.getId());
        admin.setUpdatedAt(LocalDateTime.now());
        return (Admin) userDAO.update(admin);
    }
    
    @Override
    public Optional<Admin> findAdminByEmployeeId(String employeeId) {
        return userDAO.findAdminByEmployeeId(employeeId);
    }
    
    @Override
    public List<Admin> getAllAdmins() {
        return userDAO.findByRole(Role.ADMIN).stream()
            .map(u -> (Admin) u)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Admin> getAdminsByDepartment(Long departmentId) {
        return userDAO.findAdminsByDepartment(departmentId).stream()
            .map(u -> (Admin) u)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Admin> getActiveAdminsByDepartment(Long departmentId) {
        return getAdminsByDepartment(departmentId).stream()
            .filter(User::isActive)
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Admin> getAdminWithLowestWorkload(Long departmentId) {
        // This would typically involve a DAO call to count active complaints per admin
        // For now, return the first active admin in the department
        return getActiveAdminsByDepartment(departmentId).stream().findFirst();
    }
    
    // ==================== Super Admin Operations ====================
    
    @Override
    public SuperAdmin createSuperAdmin(SuperAdmin superAdmin, String plainPassword) throws DuplicateEntryException {
        LOGGER.info("Creating super admin: " + superAdmin.getUsername());
        
        // Check for duplicates
        if (!isUsernameAvailable(superAdmin.getUsername())) {
            throw new DuplicateEntryException("Username already exists: " + superAdmin.getUsername());
        }
        if (!isEmailAvailable(superAdmin.getEmail())) {
            throw new DuplicateEntryException("Email already exists: " + superAdmin.getEmail());
        }
        
        // Hash password
        superAdmin.setPasswordHash(authService.hashPassword(plainPassword));
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setActive(true);
        superAdmin.setCreatedAt(LocalDateTime.now());
        superAdmin.setUpdatedAt(LocalDateTime.now());
        
        SuperAdmin saved = (SuperAdmin) userDAO.saveSuperAdmin(superAdmin);
        LOGGER.info("Super admin created with ID: " + saved.getId());
        return saved;
    }
    
    @Override
    public SuperAdmin updateSuperAdmin(SuperAdmin superAdmin) {
        LOGGER.info("Updating super admin: " + superAdmin.getId());
        superAdmin.setUpdatedAt(LocalDateTime.now());
        return (SuperAdmin) userDAO.update(superAdmin);
    }
    
    @Override
    public List<SuperAdmin> getAllSuperAdmins() {
        return userDAO.findByRole(Role.SUPER_ADMIN).stream()
            .map(u -> (SuperAdmin) u)
            .collect(Collectors.toList());
    }
    
    // ==================== Profile Operations ====================
    
    @Override
    public User updateProfile(Long userId, String firstName, String lastName, String phone) {
        LOGGER.info("Updating profile for user: " + userId);
        
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }
        
        User user = userOpt.get();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userDAO.update(user);
    }
    
    @Override
    public boolean updateEmail(Long userId, String newEmail) throws DuplicateEntryException {
        LOGGER.info("Updating email for user: " + userId);
        
        // Check if email is available
        Optional<User> existingEmail = userDAO.findByEmail(newEmail);
        if (existingEmail.isPresent() && !existingEmail.get().getId().equals(userId)) {
            throw new DuplicateEntryException("Email already exists: " + newEmail);
        }
        
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        user.setEmail(newEmail);
        user.setUpdatedAt(LocalDateTime.now());
        userDAO.update(user);
        
        return true;
    }
    
    // ==================== Statistics ====================
    
    @Override
    public long getTotalUserCount() {
        return userDAO.count();
    }
    
    @Override
    public long getUserCountByRole(Role role) {
        return userDAO.countByRole(role);
    }
    
    @Override
    public long getActiveUserCount() {
        return userDAO.countActive();
    }
}
