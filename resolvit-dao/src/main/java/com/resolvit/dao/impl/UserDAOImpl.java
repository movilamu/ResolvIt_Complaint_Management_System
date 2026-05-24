package com.resolvit.dao.impl;

import com.resolvit.core.entities.*;
import com.resolvit.core.enums.Role;
import com.resolvit.dao.connection.DatabaseConnection;
import com.resolvit.dao.interfaces.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserDAO for MySQL database.
 */
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public UserDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public String save(User user) throws SQLException {
        String sql = """
            INSERT INTO users (user_id, name, email, password_hash, role, dept_id,
                             roll_number, class_name, phone, profile_pic, is_active,
                             created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getUserId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPasswordHash());
                ps.setString(5, user.getRole().name());

                // Handle department ID based on user type
                if (user instanceof Student student && student.getDepartment() != null) {
                    ps.setInt(6, student.getDepartment().getDeptId());
                } else if (user instanceof Admin admin && admin.getDepartment() != null) {
                    ps.setInt(6, admin.getDepartment().getDeptId());
                } else {
                    ps.setNull(6, Types.INTEGER);
                }

                // Handle student-specific fields
                if (user instanceof Student student) {
                    ps.setString(7, student.getRollNumber());
                    ps.setString(8, student.getClassName());
                } else {
                    ps.setNull(7, Types.VARCHAR);
                    ps.setNull(8, Types.VARCHAR);
                }

                ps.setString(9, user.getPhone());
                ps.setString(10, user.getProfilePic());
                ps.setBoolean(11, user.isActive());
                ps.setTimestamp(12, Timestamp.valueOf(user.getCreatedAt()));
                ps.setTimestamp(13, Timestamp.valueOf(user.getUpdatedAt()));

                ps.executeUpdate();
                logger.debug("Saved user: {}", user.getUserId());
                return user.getUserId();
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public Optional<User> findById(String userId) throws SQLException {
        String sql = """
            SELECT u.*, d.dept_name, d.dept_head, d.contact_email
            FROM users u
            LEFT JOIN departments d ON u.dept_id = d.dept_id
            WHERE u.user_id = ?
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                    return Optional.empty();
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = """
            SELECT u.*, d.dept_name, d.dept_head, d.contact_email
            FROM users u
            LEFT JOIN departments d ON u.dept_id = d.dept_id
            WHERE LOWER(u.email) = LOWER(?)
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                    return Optional.empty();
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = """
            SELECT u.*, d.dept_name, d.dept_head, d.contact_email
            FROM users u
            LEFT JOIN departments d ON u.dept_id = d.dept_id
            ORDER BY u.created_at DESC
            """;

        return executeQueryForList(sql);
    }

    @Override
    public List<User> findByRole(Role role) throws SQLException {
        String sql = """
            SELECT u.*, d.dept_name, d.dept_head, d.contact_email
            FROM users u
            LEFT JOIN departments d ON u.dept_id = d.dept_id
            WHERE u.role = ?
            ORDER BY u.name
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, role.name());
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<User> findByDepartment(int deptId) throws SQLException {
        String sql = """
            SELECT u.*, d.dept_name, d.dept_head, d.contact_email
            FROM users u
            LEFT JOIN departments d ON u.dept_id = d.dept_id
            WHERE u.dept_id = ?
            ORDER BY u.name
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
    public List<User> findAllActive() throws SQLException {
        String sql = """
            SELECT u.*, d.dept_name, d.dept_head, d.contact_email
            FROM users u
            LEFT JOIN departments d ON u.dept_id = d.dept_id
            WHERE u.is_active = TRUE
            ORDER BY u.name
            """;

        return executeQueryForList(sql);
    }

    @Override
    public boolean update(User user) throws SQLException {
        String sql = """
            UPDATE users SET
                name = ?, email = ?, phone = ?, profile_pic = ?,
                dept_id = ?, roll_number = ?, class_name = ?,
                is_active = ?, updated_at = ?
            WHERE user_id = ?
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPhone());
                ps.setString(4, user.getProfilePic());

                // Handle department
                if (user instanceof Student student && student.getDepartment() != null) {
                    ps.setInt(5, student.getDepartment().getDeptId());
                } else if (user instanceof Admin admin && admin.getDepartment() != null) {
                    ps.setInt(5, admin.getDepartment().getDeptId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }

                // Handle student fields
                if (user instanceof Student student) {
                    ps.setString(6, student.getRollNumber());
                    ps.setString(7, student.getClassName());
                } else {
                    ps.setNull(6, Types.VARCHAR);
                    ps.setNull(7, Types.VARCHAR);
                }

                ps.setBoolean(8, user.isActive());
                ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(10, user.getUserId());

                int rows = ps.executeUpdate();
                logger.debug("Updated user: {}, rows affected: {}", user.getUserId(), rows);
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean updatePassword(String userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ?, updated_at = ? WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newPasswordHash);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(3, userId);

                int rows = ps.executeUpdate();
                logger.debug("Updated password for user: {}", userId);
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean updateLastLogin(String userId) throws SQLException {
        String sql = "UPDATE users SET last_login = ? WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(2, userId);

                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean deactivate(String userId) throws SQLException {
        return setActiveStatus(userId, false);
    }

    @Override
    public boolean activate(String userId) throws SQLException {
        return setActiveStatus(userId, true);
    }

    private boolean setActiveStatus(String userId, boolean active) throws SQLException {
        String sql = "UPDATE users SET is_active = ?, updated_at = ? WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, active);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(3, userId);

                int rows = ps.executeUpdate();
                logger.debug("Set active status for user {} to {}", userId, active);
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(String userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);

                int rows = ps.executeUpdate();
                logger.debug("Deleted user: {}", userId);
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                    return false;
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        return executeCountQuery(sql);
    }

    @Override
    public int countByRole(Role role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, role.name());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<User> search(String searchTerm) throws SQLException {
        String sql = """
            SELECT u.*, d.dept_name, d.dept_head, d.contact_email
            FROM users u
            LEFT JOIN departments d ON u.dept_id = d.dept_id
            WHERE LOWER(u.name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?)
            ORDER BY u.name
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String pattern = "%" + searchTerm + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════

    private List<User> executeQueryForList(String sql) throws SQLException {
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

    private int executeCountQuery(String sql) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    private List<User> mapResultSetToList(ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            users.add(mapResultSetToUser(rs));
        }
        return users;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Role role = Role.valueOf(rs.getString("role"));

        User user;
        switch (role) {
            case STUDENT -> {
                Student student = new Student();
                student.setRollNumber(rs.getString("roll_number"));
                student.setClassName(rs.getString("class_name"));
                user = student;
            }
            case SUPERADMIN -> {
                user = new SuperAdmin();
            }
            case ADMIN -> {
                user = new Admin();
            }
            default -> throw new IllegalStateException("Unknown role: " + role);
        }

        // Set common fields
        user.setUserId(rs.getString("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(role);
        user.setPhone(rs.getString("phone"));
        user.setProfilePic(rs.getString("profile_pic"));
        user.setActive(rs.getBoolean("is_active"));

        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(lastLogin.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        // Set department if exists
        int deptId = rs.getInt("dept_id");
        if (!rs.wasNull()) {
            Department dept = new Department();
            dept.setDeptId(deptId);
            dept.setDeptName(rs.getString("dept_name"));
            dept.setDeptHead(rs.getString("dept_head"));
            dept.setContactEmail(rs.getString("contact_email"));

            if (user instanceof Student student) {
                student.setDepartment(dept);
            } else if (user instanceof Admin admin) {
                admin.setDepartment(dept);
            }
        }

        return user;
    }
}
