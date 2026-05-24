package com.resolvit.dao.impl;

import com.resolvit.core.entities.Complaint;
import com.resolvit.core.entities.Student;
import com.resolvit.core.entities.User;
import com.resolvit.core.enums.Category;
import com.resolvit.core.enums.Priority;
import com.resolvit.core.enums.Status;
import com.resolvit.dao.connection.DatabaseConnection;
import com.resolvit.dao.interfaces.ComplaintDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of ComplaintDAO for MySQL database.
 */
public class ComplaintDAOImpl implements ComplaintDAO {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public ComplaintDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public String save(Complaint complaint) throws SQLException {
        String sql = """
            INSERT INTO complaints (complaint_id, user_id, title, description, category,
                                   priority, status, attachment, is_anonymous, view_count,
                                   created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, complaint.getComplaintId());
                ps.setString(2, complaint.getUserId() != null ? 
                               complaint.getUserId() : 
                               complaint.getSubmittedBy().getUserId());
                ps.setString(3, complaint.getTitle());
                ps.setString(4, complaint.getDescription());
                ps.setString(5, complaint.getCategory().name());
                ps.setString(6, complaint.getPriority().name());
                ps.setString(7, complaint.getStatus().name());
                ps.setString(8, complaint.getAttachmentPath());
                ps.setBoolean(9, complaint.isAnonymous());
                ps.setInt(10, complaint.getViewCount());
                ps.setTimestamp(11, Timestamp.valueOf(complaint.getCreatedAt()));
                ps.setTimestamp(12, Timestamp.valueOf(complaint.getUpdatedAt()));

                ps.executeUpdate();
                logger.debug("Saved complaint: {}", complaint.getComplaintId());

                // Insert initial status history
                insertStatusHistory(conn, complaint.getComplaintId(), null, Status.PENDING,
                                  complaint.getUserId(), "Complaint submitted");

                return complaint.getComplaintId();
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public Optional<Complaint> findById(String complaintId) throws SQLException {
        String sql = """
            SELECT c.*, u.name as user_name, u.email as user_email, u.roll_number
            FROM complaints c
            LEFT JOIN users u ON c.user_id = u.user_id
            WHERE c.complaint_id = ?
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, complaintId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToComplaint(rs));
                    }
                    return Optional.empty();
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Complaint> findByUserId(String userId) throws SQLException {
        String sql = """
            SELECT c.*, u.name as user_name, u.email as user_email, u.roll_number
            FROM complaints c
            LEFT JOIN users u ON c.user_id = u.user_id
            WHERE c.user_id = ?
            ORDER BY c.created_at DESC
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
    public List<Complaint> findAll() throws SQLException {
        String sql = """
            SELECT c.*, u.name as user_name, u.email as user_email, u.roll_number
            FROM complaints c
            LEFT JOIN users u ON c.user_id = u.user_id
            ORDER BY c.created_at DESC
            """;

        return executeQueryForList(sql);
    }

    @Override
    public List<Complaint> findAllPaginated(int offset, int limit) throws SQLException {
        String sql = """
            SELECT c.*, u.name as user_name, u.email as user_email, u.roll_number
            FROM complaints c
            LEFT JOIN users u ON c.user_id = u.user_id
            ORDER BY c.created_at DESC
            LIMIT ? OFFSET ?
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limit);
                ps.setInt(2, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Complaint> findByStatus(Status status) throws SQLException {
        String sql = """
            SELECT c.*, u.name as user_name, u.email as user_email, u.roll_number
            FROM complaints c
            LEFT JOIN users u ON c.user_id = u.user_id
            WHERE c.status = ?
            ORDER BY c.created_at DESC
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status.name());
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Complaint> findByCategory(Category category) throws SQLException {
        String sql = """
            SELECT c.*, u.name as user_name, u.email as user_email, u.roll_number
            FROM complaints c
            LEFT JOIN users u ON c.user_id = u.user_id
            WHERE c.category = ?
            ORDER BY c.created_at DESC
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, category.name());
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Complaint> findByPriority(Priority priority) throws SQLException {
        String sql = """
            SELECT c.*, u.name as user_name, u.email as user_email, u.roll_number
            FROM complaints c
            LEFT JOIN users u ON c.user_id = u.user_id
            WHERE c.priority = ?
            ORDER BY c.created_at DESC
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, priority.name());
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Complaint> findByDepartment(int deptId) throws SQLException {
        String sql = """
            SELECT c.*, u.name as user_name, u.email as user_email, u.roll_number
            FROM complaints c
            LEFT JOIN users u ON c.user_id = u.user_id
            INNER JOIN assignments a ON c.complaint_id = a.complaint_id
            WHERE a.dept_id = ?
            ORDER BY c.created_at DESC
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
    public List<Complaint> findWithFilters(Status status, Category category,
                                            Priority priority, LocalDate from,
                                            LocalDate to) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT c.*, u.name as user_name, u.email as user_email, u.roll_number
            FROM complaints c
            LEFT JOIN users u ON c.user_id = u.user_id
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append(" AND c.status = ?");
            params.add(status.name());
        }
        if (category != null) {
            sql.append(" AND c.category = ?");
            params.add(category.name());
        }
        if (priority != null) {
            sql.append(" AND c.priority = ?");
            params.add(priority.name());
        }
        if (from != null) {
            sql.append(" AND DATE(c.created_at) >= ?");
            params.add(from.toString());
        }
        if (to != null) {
            sql.append(" AND DATE(c.created_at) <= ?");
            params.add(to.toString());
        }

        sql.append(" ORDER BY c.created_at DESC");

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToList(rs);
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean update(Complaint complaint) throws SQLException {
        String sql = """
            UPDATE complaints SET
                title = ?, description = ?, category = ?, priority = ?,
                status = ?, attachment = ?, is_anonymous = ?, updated_at = ?
            WHERE complaint_id = ?
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, complaint.getTitle());
                ps.setString(2, complaint.getDescription());
                ps.setString(3, complaint.getCategory().name());
                ps.setString(4, complaint.getPriority().name());
                ps.setString(5, complaint.getStatus().name());
                ps.setString(6, complaint.getAttachmentPath());
                ps.setBoolean(7, complaint.isAnonymous());
                ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(9, complaint.getComplaintId());

                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean updateStatus(String complaintId, Status status,
                               String changedBy, String remarks) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                // Get current status
                Status oldStatus = null;
                String getStatusSql = "SELECT status FROM complaints WHERE complaint_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(getStatusSql)) {
                    ps.setString(1, complaintId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            oldStatus = Status.valueOf(rs.getString("status"));
                        }
                    }
                }

                // Update complaint status
                String updateSql = """
                    UPDATE complaints SET 
                        status = ?, updated_at = ?, resolved_at = ?
                    WHERE complaint_id = ?
                    """;

                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, status.name());
                    ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));

                    if (status == Status.RESOLVED) {
                        ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    } else {
                        ps.setNull(3, Types.TIMESTAMP);
                    }

                    ps.setString(4, complaintId);
                    ps.executeUpdate();
                }

                // Insert status history
                insertStatusHistory(conn, complaintId, oldStatus, status, changedBy, remarks);

                conn.commit();
                logger.debug("Updated complaint {} status from {} to {}",
                            complaintId, oldStatus, status);
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean assignDepartment(String complaintId, int deptId,
                                   String assignedBy, String remarks) throws SQLException {
        String sql = """
            INSERT INTO assignments (assignment_id, complaint_id, dept_id, assigned_by, remarks, assigned_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, complaintId);
                ps.setInt(3, deptId);
                ps.setString(4, assignedBy);
                ps.setString(5, remarks);
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

                int rows = ps.executeUpdate();
                logger.debug("Assigned complaint {} to department {}", complaintId, deptId);
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean incrementViewCount(String complaintId) throws SQLException {
        String sql = "UPDATE complaints SET view_count = view_count + 1 WHERE complaint_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, complaintId);
                return ps.executeUpdate() > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(String complaintId) throws SQLException {
        String sql = "DELETE FROM complaints WHERE complaint_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, complaintId);
                int rows = ps.executeUpdate();
                logger.debug("Deleted complaint: {}", complaintId);
                return rows > 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int count() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM complaints");
    }

    @Override
    public int countByStatus(Status status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints WHERE status = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status.name());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int countByCategory(Category category) throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints WHERE category = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, category.name());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int countByPriority(Priority priority) throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints WHERE priority = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, priority.name());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public int countByUserId(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public double avgResolutionTimeHours() throws SQLException {
        String sql = """
            SELECT AVG(TIMESTAMPDIFF(HOUR, created_at, resolved_at)) as avg_hours
            FROM complaints
            WHERE status = 'RESOLVED' AND resolved_at IS NOT NULL
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_hours");
                }
                return 0.0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Map<String, Object>> getMonthlyTrend(int year) throws SQLException {
        String sql = """
            SELECT 
                MONTH(created_at) as month,
                COUNT(*) as total,
                SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved,
                SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending
            FROM complaints
            WHERE YEAR(created_at) = ?
            GROUP BY MONTH(created_at)
            ORDER BY month
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, year);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Map<String, Object>> results = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("month", rs.getInt("month"));
                        row.put("total", rs.getInt("total"));
                        row.put("resolved", rs.getInt("resolved"));
                        row.put("pending", rs.getInt("pending"));
                        results.add(row);
                    }
                    return results;
                }
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public Map<Category, Integer> getStatsByCategory() throws SQLException {
        String sql = "SELECT category, COUNT(*) as count FROM complaints GROUP BY category";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                Map<Category, Integer> stats = new EnumMap<>(Category.class);
                while (rs.next()) {
                    Category cat = Category.valueOf(rs.getString("category"));
                    stats.put(cat, rs.getInt("count"));
                }
                return stats;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public Map<Status, Integer> getStatsByStatus() throws SQLException {
        String sql = "SELECT status, COUNT(*) as count FROM complaints GROUP BY status";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                Map<Status, Integer> stats = new EnumMap<>(Status.class);
                while (rs.next()) {
                    Status st = Status.valueOf(rs.getString("status"));
                    stats.put(st, rs.getInt("count"));
                }
                return stats;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Complaint> search(String searchTerm) throws SQLException {
        String sql = """
            SELECT c.*, u.name as user_name, u.email as user_email, u.roll_number
            FROM complaints c
            LEFT JOIN users u ON c.user_id = u.user_id
            WHERE LOWER(c.title) LIKE LOWER(?) OR LOWER(c.description) LIKE LOWER(?)
            ORDER BY c.created_at DESC
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

    private void insertStatusHistory(Connection conn, String complaintId,
                                    Status oldStatus, Status newStatus,
                                    String changedBy, String remarks) throws SQLException {
        String sql = """
            INSERT INTO status_history (complaint_id, old_status, new_status, changed_by, remarks, changed_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, complaintId);
            ps.setString(2, oldStatus != null ? oldStatus.name() : null);
            ps.setString(3, newStatus.name());
            ps.setString(4, changedBy);
            ps.setString(5, remarks);
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    private List<Complaint> executeQueryForList(String sql) throws SQLException {
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
                return rs.next() ? rs.getInt(1) : 0;
            }
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    private List<Complaint> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        while (rs.next()) {
            complaints.add(mapResultSetToComplaint(rs));
        }
        return complaints;
    }

    private Complaint mapResultSetToComplaint(ResultSet rs) throws SQLException {
        Complaint complaint = new Complaint();

        complaint.setComplaintId(rs.getString("complaint_id"));
        complaint.setUserId(rs.getString("user_id"));
        complaint.setTitle(rs.getString("title"));
        complaint.setDescription(rs.getString("description"));
        complaint.setCategory(Category.valueOf(rs.getString("category")));
        complaint.setPriority(Priority.valueOf(rs.getString("priority")));
        complaint.setStatus(Status.valueOf(rs.getString("status")));
        complaint.setAttachmentPath(rs.getString("attachment"));
        complaint.setAnonymous(rs.getBoolean("is_anonymous"));
        complaint.setViewCount(rs.getInt("view_count"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            complaint.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            complaint.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp resolvedAt = rs.getTimestamp("resolved_at");
        if (resolvedAt != null) {
            complaint.setResolvedAt(resolvedAt.toLocalDateTime());
        }

        // Set user info if available
        try {
            String userName = rs.getString("user_name");
            if (userName != null) {
                Student user = new Student();
                user.setUserId(rs.getString("user_id"));
                user.setName(userName);
                user.setEmail(rs.getString("user_email"));
                user.setRollNumber(rs.getString("roll_number"));
                complaint.setSubmittedBy(user);
            }
        } catch (SQLException e) {
            // User columns not in result set, ignore
        }

        return complaint;
    }
}
