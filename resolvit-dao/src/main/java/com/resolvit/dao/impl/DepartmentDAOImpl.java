package com.resolvit.dao.impl;

import com.resolvit.core.entities.Department;
import com.resolvit.dao.connection.DatabaseConnection;
import com.resolvit.dao.interfaces.DepartmentDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of DepartmentDAO for MySQL database.
 */
public class DepartmentDAOImpl implements DepartmentDAO {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public DepartmentDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public int save(Department department) throws SQLException {
        String sql = """
            INSERT INTO departments (dept_name, dept_head, contact_email, is_active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, department.getDeptName());
                ps.setString(2, department.getDeptHead());
                ps.setString(3, department.getContactEmail());
                ps.setBoolean(4, department.isActive());
                ps.setTimestamp(5, Timestamp.valueOf(department.getCreatedAt()));
                ps.setTimestamp(6, Timestamp.valueOf(department.getUpdatedAt()));

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        department.setDeptId(id);
                        logger.debug("Saved department: {}", id);
                        return id;
                    }
                }
                throw new SQLException("Failed to retrieve generated department ID");
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public Optional<Department> findById(int deptId) throws SQLException {
        String sql = "SELECT * FROM departments WHERE dept_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, deptId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToDepartment(rs));
                    }
                    return Optional.empty();
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public Optional<Department> findByName(String deptName) throws SQLException {
        String sql = "SELECT * FROM departments WHERE LOWER(dept_name) = LOWER(?)";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, deptName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToDepartment(rs));
                    }
                    return Optional.empty();
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Department> findAll() throws SQLException {
        String sql = "SELECT * FROM departments ORDER BY dept_name";
        return executeQueryForList(sql);
    }

    @Override
    public List<Department> findAllActive() throws SQLException {
        String sql = "SELECT * FROM departments WHERE is_active = TRUE ORDER BY dept_name";
        return executeQueryForList(sql);
    }

    @Override
    public boolean update(Department department) throws SQLException {
        String sql = """
            UPDATE departments SET
                dept_name = ?, dept_head = ?, contact_email = ?, 
                is_active = ?, updated_at = ?
            WHERE dept_id = ?
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, department.getDeptName());
                ps.setString(2, department.getDeptHead());
                ps.setString(3, department.getContactEmail());
                ps.setBoolean(4, department.isActive());
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(6, department.getDeptId());

                int rows = ps.executeUpdate();
                logger.debug("Updated department: {}", department.getDeptId());
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean deactivate(int deptId) throws SQLException {
        return setActiveStatus(deptId, false);
    }

    @Override
    public boolean activate(int deptId) throws SQLException {
        return setActiveStatus(deptId, true);
    }

    private boolean setActiveStatus(int deptId, boolean active) throws SQLException {
        String sql = "UPDATE departments SET is_active = ?, updated_at = ? WHERE dept_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, active);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(3, deptId);

                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(int deptId) throws SQLException {
        String sql = "DELETE FROM departments WHERE dept_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, deptId);
                int rows = ps.executeUpdate();
                logger.debug("Deleted department: {}", deptId);
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM departments";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int getComplaintCount(int deptId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM assignments WHERE dept_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, deptId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════

    private List<Department> executeQueryForList(String sql) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                List<Department> departments = new ArrayList<>();
                while (rs.next()) {
                    departments.add(mapResultSetToDepartment(rs));
                }
                return departments;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    private Department mapResultSetToDepartment(ResultSet rs) throws SQLException {
        Department dept = new Department();

        dept.setDeptId(rs.getInt("dept_id"));
        dept.setDeptName(rs.getString("dept_name"));
        dept.setDeptHead(rs.getString("dept_head"));
        dept.setContactEmail(rs.getString("contact_email"));
        dept.setActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            dept.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            dept.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return dept;
    }
}
