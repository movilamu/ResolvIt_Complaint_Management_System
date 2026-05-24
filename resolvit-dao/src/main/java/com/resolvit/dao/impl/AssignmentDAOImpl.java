package com.resolvit.dao.impl;

import com.resolvit.core.entities.Assignment;
import com.resolvit.core.entities.Department;
import com.resolvit.dao.connection.DatabaseConnection;
import com.resolvit.dao.interfaces.AssignmentDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of AssignmentDAO for MySQL database.
 */
public class AssignmentDAOImpl implements AssignmentDAO {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public AssignmentDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public String save(Assignment assignment) throws SQLException {
        String sql = """
            INSERT INTO assignments (assignment_id, complaint_id, dept_id, assigned_by, assigned_at, remarks)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, assignment.getAssignmentId());
                ps.setString(2, assignment.getComplaintId());
                ps.setInt(3, assignment.getDeptId());
                ps.setString(4, assignment.getAssignedBy());
                ps.setTimestamp(5, Timestamp.valueOf(assignment.getAssignedAt()));
                ps.setString(6, assignment.getRemarks());

                ps.executeUpdate();
                logger.debug("Saved assignment: {}", assignment.getAssignmentId());
                return assignment.getAssignmentId();
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public Optional<Assignment> findById(String assignmentId) throws SQLException {
        String sql = """
            SELECT a.*, d.dept_name, d.dept_head, d.contact_email
            FROM assignments a
            LEFT JOIN departments d ON a.dept_id = d.dept_id
            WHERE a.assignment_id = ?
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, assignmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToAssignment(rs));
                    }
                    return Optional.empty();
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Assignment> findByComplaintId(String complaintId) throws SQLException {
        String sql = """
            SELECT a.*, d.dept_name, d.dept_head, d.contact_email
            FROM assignments a
            LEFT JOIN departments d ON a.dept_id = d.dept_id
            WHERE a.complaint_id = ?
            ORDER BY a.assigned_at DESC
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, complaintId);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public Optional<Assignment> findLatestByComplaintId(String complaintId) throws SQLException {
        String sql = """
            SELECT a.*, d.dept_name, d.dept_head, d.contact_email
            FROM assignments a
            LEFT JOIN departments d ON a.dept_id = d.dept_id
            WHERE a.complaint_id = ?
            ORDER BY a.assigned_at DESC
            LIMIT 1
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, complaintId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToAssignment(rs));
                    }
                    return Optional.empty();
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Assignment> findByDepartmentId(int deptId) throws SQLException {
        String sql = """
            SELECT a.*, d.dept_name, d.dept_head, d.contact_email
            FROM assignments a
            LEFT JOIN departments d ON a.dept_id = d.dept_id
            WHERE a.dept_id = ?
            ORDER BY a.assigned_at DESC
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, deptId);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Assignment> findByAssignedBy(String userId) throws SQLException {
        String sql = """
            SELECT a.*, d.dept_name, d.dept_head, d.contact_email
            FROM assignments a
            LEFT JOIN departments d ON a.dept_id = d.dept_id
            WHERE a.assigned_by = ?
            ORDER BY a.assigned_at DESC
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Assignment> findAll() throws SQLException {
        String sql = """
            SELECT a.*, d.dept_name, d.dept_head, d.contact_email
            FROM assignments a
            LEFT JOIN departments d ON a.dept_id = d.dept_id
            ORDER BY a.assigned_at DESC
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                return mapResultSetToList(rs);
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean update(Assignment assignment) throws SQLException {
        String sql = """
            UPDATE assignments SET
                dept_id = ?, remarks = ?
            WHERE assignment_id = ?
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, assignment.getDeptId());
                ps.setString(2, assignment.getRemarks());
                ps.setString(3, assignment.getAssignmentId());

                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(String assignmentId) throws SQLException {
        String sql = "DELETE FROM assignments WHERE assignment_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, assignmentId);
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int deleteByComplaintId(String complaintId) throws SQLException {
        String sql = "DELETE FROM assignments WHERE complaint_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, complaintId);
                return ps.executeUpdate();
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int countByDepartment(int deptId) throws SQLException {
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

    private List<Assignment> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Assignment> assignments = new ArrayList<>();
        while (rs.next()) {
            assignments.add(mapResultSetToAssignment(rs));
        }
        return assignments;
    }

    private Assignment mapResultSetToAssignment(ResultSet rs) throws SQLException {
        Assignment assignment = new Assignment();

        assignment.setAssignmentId(rs.getString("assignment_id"));
        assignment.setComplaintId(rs.getString("complaint_id"));
        assignment.setDeptId(rs.getInt("dept_id"));
        assignment.setAssignedBy(rs.getString("assigned_by"));
        assignment.setRemarks(rs.getString("remarks"));

        Timestamp assignedAt = rs.getTimestamp("assigned_at");
        if (assignedAt != null) {
            assignment.setAssignedAt(assignedAt.toLocalDateTime());
        }

        // Set department info if available
        try {
            String deptName = rs.getString("dept_name");
            if (deptName != null) {
                Department dept = new Department();
                dept.setDeptId(rs.getInt("dept_id"));
                dept.setDeptName(deptName);
                dept.setDeptHead(rs.getString("dept_head"));
                dept.setContactEmail(rs.getString("contact_email"));
                assignment.setDepartment(dept);
            }
        } catch (SQLException e) {
            // Department columns not in result set, ignore
        }

        return assignment;
    }
}
