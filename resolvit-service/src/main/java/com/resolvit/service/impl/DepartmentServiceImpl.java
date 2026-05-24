package com.resolvit.service.impl;

import com.resolvit.core.entities.Department;
import com.resolvit.core.enums.Category;
import com.resolvit.core.exceptions.DuplicateEntryException;
import com.resolvit.dao.interfaces.DepartmentDAO;
import com.resolvit.service.interfaces.DepartmentService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * Implementation of the DepartmentService interface.
 */
public class DepartmentServiceImpl implements DepartmentService {
    
    private static final Logger LOGGER = Logger.getLogger(DepartmentServiceImpl.class.getName());
    
    private final DepartmentDAO departmentDAO;
    
    public DepartmentServiceImpl(DepartmentDAO departmentDAO) {
        this.departmentDAO = departmentDAO;
    }
    
    @Override
    public Department createDepartment(Department department) throws DuplicateEntryException {
        LOGGER.info("Creating department: " + department.getName());
        
        // Check for duplicates
        if (!isNameAvailable(department.getName())) {
            throw new DuplicateEntryException("Department name already exists: " + department.getName());
        }
        if (!isCodeAvailable(department.getCode())) {
            throw new DuplicateEntryException("Department code already exists: " + department.getCode());
        }
        
        department.setActive(true);
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());
        
        Department saved = departmentDAO.save(department);
        LOGGER.info("Department created with ID: " + saved.getId());
        return saved;
    }
    
    @Override
    public Department updateDepartment(Department department) {
        LOGGER.info("Updating department: " + department.getId());
        department.setUpdatedAt(LocalDateTime.now());
        return departmentDAO.update(department);
    }
    
    @Override
    public boolean deleteDepartment(Long departmentId) {
        LOGGER.info("Deleting department: " + departmentId);
        return departmentDAO.delete(departmentId);
    }
    
    @Override
    public Optional<Department> findById(Long departmentId) {
        return departmentDAO.findById(departmentId);
    }
    
    @Override
    public Optional<Department> findByName(String name) {
        return departmentDAO.findByName(name);
    }
    
    @Override
    public Optional<Department> findByCode(String code) {
        return departmentDAO.findByCode(code);
    }
    
    @Override
    public List<Department> getAllDepartments() {
        return departmentDAO.findAll();
    }
    
    @Override
    public List<Department> getActiveDepartments() {
        return departmentDAO.findActive();
    }
    
    @Override
    public Optional<Department> getDepartmentByCategory(Category category) {
        return departmentDAO.findByCategory(category);
    }
    
    @Override
    public boolean addCategoryToDepartment(Long departmentId, Category category) {
        LOGGER.info("Adding category " + category + " to department " + departmentId);
        return departmentDAO.addCategory(departmentId, category);
    }
    
    @Override
    public boolean removeCategoryFromDepartment(Long departmentId, Category category) {
        LOGGER.info("Removing category " + category + " from department " + departmentId);
        return departmentDAO.removeCategory(departmentId, category);
    }
    
    @Override
    public List<Category> getCategoriesByDepartment(Long departmentId) {
        return departmentDAO.getCategoriesByDepartment(departmentId);
    }
    
    @Override
    public Map<String, Object> getDepartmentStatistics(Long departmentId) {
        Map<String, Object> stats = new HashMap<>();
        
        Optional<Department> deptOpt = departmentDAO.findById(departmentId);
        if (deptOpt.isEmpty()) {
            return stats;
        }
        
        Department dept = deptOpt.get();
        stats.put("department", dept);
        stats.put("adminCount", getAdminCount(departmentId));
        stats.put("activeComplaintCount", getActiveComplaintCount(departmentId));
        stats.put("categories", getCategoriesByDepartment(departmentId));
        
        return stats;
    }
    
    @Override
    public int getAdminCount(Long departmentId) {
        return departmentDAO.countAdmins(departmentId);
    }
    
    @Override
    public int getActiveComplaintCount(Long departmentId) {
        return departmentDAO.countActiveComplaints(departmentId);
    }
    
    @Override
    public boolean isNameAvailable(String name) {
        return departmentDAO.findByName(name).isEmpty();
    }
    
    @Override
    public boolean isCodeAvailable(String code) {
        return departmentDAO.findByCode(code).isEmpty();
    }
}
