package com.resolvit.dao.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Initializes the database schema on application startup.
 * Creates tables if they don't exist.
 */
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private static final String SCHEMA_FILE = "schema.sql";

    /**
     * Initialize the database schema.
     * This method is idempotent - safe to call multiple times.
     */
    public static void initialize() {
        logger.info("Starting database initialization...");

        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        
        if (!dbConnection.testConnection()) {
            logger.error("Cannot connect to database. Please check your configuration.");
            throw new RuntimeException("Database connection failed");
        }

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            
            // Create tables if they don't exist
            createTablesIfNotExist(conn);
            
            logger.info("Database initialization completed successfully");
            
        } catch (SQLException e) {
            logger.error("Database initialization failed: {}", e.getMessage(), e);
            throw new RuntimeException("Database initialization failed", e);
        } finally {
            dbConnection.releaseConnection(conn);
        }
    }

    /**
     * Create database tables if they don't exist.
     */
    private static void createTablesIfNotExist(Connection conn) throws SQLException {
        logger.info("Creating database tables if not exist...");

        String[] tableCreationStatements = {
            // Departments table
            """
            CREATE TABLE IF NOT EXISTS departments (
                dept_id       INT AUTO_INCREMENT PRIMARY KEY,
                dept_name     VARCHAR(100) NOT NULL UNIQUE,
                dept_head     VARCHAR(100),
                contact_email VARCHAR(150),
                is_active     BOOLEAN DEFAULT TRUE,
                created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """,

            // Users table
            """
            CREATE TABLE IF NOT EXISTS users (
                user_id       VARCHAR(36) PRIMARY KEY,
                name          VARCHAR(100) NOT NULL,
                email         VARCHAR(150) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                role          ENUM('STUDENT', 'ADMIN', 'SUPERADMIN') NOT NULL,
                dept_id       INT,
                roll_number   VARCHAR(20),
                class_name    VARCHAR(50),
                phone         VARCHAR(15),
                profile_pic   VARCHAR(500),
                is_active     BOOLEAN DEFAULT TRUE,
                last_login    TIMESTAMP NULL,
                created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (dept_id) REFERENCES departments(dept_id) ON DELETE SET NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """,

            // Complaints table
            """
            CREATE TABLE IF NOT EXISTS complaints (
                complaint_id  VARCHAR(36) PRIMARY KEY,
                user_id       VARCHAR(36) NOT NULL,
                title         VARCHAR(200) NOT NULL,
                description   TEXT NOT NULL,
                category      ENUM('HOSTEL', 'ACADEMICS', 'IT', 'TRANSPORT', 'CANTEEN', 'OTHER') NOT NULL,
                priority      ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL,
                status        ENUM('PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'REOPENED') NOT NULL DEFAULT 'PENDING',
                attachment    VARCHAR(500),
                is_anonymous  BOOLEAN DEFAULT FALSE,
                view_count    INT DEFAULT 0,
                created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                resolved_at   TIMESTAMP NULL,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """,

            // Assignments table
            """
            CREATE TABLE IF NOT EXISTS assignments (
                assignment_id VARCHAR(36) PRIMARY KEY,
                complaint_id  VARCHAR(36) NOT NULL,
                dept_id       INT NOT NULL,
                assigned_by   VARCHAR(36) NOT NULL,
                assigned_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                remarks       TEXT,
                FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id) ON DELETE CASCADE,
                FOREIGN KEY (dept_id) REFERENCES departments(dept_id) ON DELETE CASCADE,
                FOREIGN KEY (assigned_by) REFERENCES users(user_id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """,

            // Status history table
            """
            CREATE TABLE IF NOT EXISTS status_history (
                history_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
                complaint_id  VARCHAR(36) NOT NULL,
                old_status    VARCHAR(20),
                new_status    VARCHAR(20) NOT NULL,
                changed_by    VARCHAR(36) NOT NULL,
                remarks       TEXT,
                changed_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id) ON DELETE CASCADE,
                FOREIGN KEY (changed_by) REFERENCES users(user_id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """,

            // Notifications table
            """
            CREATE TABLE IF NOT EXISTS notifications (
                notif_id      VARCHAR(36) PRIMARY KEY,
                user_id       VARCHAR(36) NOT NULL,
                title         VARCHAR(200) NOT NULL,
                message       TEXT NOT NULL,
                type          ENUM('STATUS_UPDATE', 'ASSIGNMENT', 'REMINDER', 'SYSTEM') NOT NULL,
                is_read       BOOLEAN DEFAULT FALSE,
                related_id    VARCHAR(36),
                created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """,

            // Audit log table
            """
            CREATE TABLE IF NOT EXISTS audit_log (
                log_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id       VARCHAR(36),
                action        VARCHAR(100) NOT NULL,
                entity_type   VARCHAR(50),
                entity_id     VARCHAR(36),
                old_value     TEXT,
                new_value     TEXT,
                ip_address    VARCHAR(45),
                user_agent    VARCHAR(500),
                timestamp     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """,

            // Feedback table
            """
            CREATE TABLE IF NOT EXISTS feedback (
                feedback_id   VARCHAR(36) PRIMARY KEY,
                complaint_id  VARCHAR(36) NOT NULL UNIQUE,
                user_id       VARCHAR(36) NOT NULL,
                rating        TINYINT NOT NULL,
                comment       TEXT,
                created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : tableCreationStatements) {
                stmt.execute(sql);
            }
            logger.info("All tables created/verified successfully");
        }

        // Create indexes
        createIndexes(conn);
    }

    /**
     * Create indexes for better query performance.
     */
    private static void createIndexes(Connection conn) throws SQLException {
        String[] indexStatements = {
            "CREATE INDEX IF NOT EXISTS idx_user_email ON users(email)",
            "CREATE INDEX IF NOT EXISTS idx_user_role ON users(role)",
            "CREATE INDEX IF NOT EXISTS idx_complaint_user ON complaints(user_id)",
            "CREATE INDEX IF NOT EXISTS idx_complaint_status ON complaints(status)",
            "CREATE INDEX IF NOT EXISTS idx_complaint_category ON complaints(category)",
            "CREATE INDEX IF NOT EXISTS idx_complaint_created ON complaints(created_at)",
            "CREATE INDEX IF NOT EXISTS idx_notif_user ON notifications(user_id)",
            "CREATE INDEX IF NOT EXISTS idx_notif_read ON notifications(is_read)",
            "CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_log(timestamp)"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : indexStatements) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    // Ignore errors for existing indexes (MySQL syntax may vary)
                    if (!e.getMessage().contains("Duplicate key name")) {
                        logger.debug("Index creation note: {}", e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Execute SQL from a resource file.
     */
    public static void executeSqlFile(String resourcePath) {
        logger.info("Executing SQL file: {}", resourcePath);

        try (InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                logger.error("SQL file not found: {}", resourcePath);
                return;
            }

            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines()
                    .collect(Collectors.joining("\n"));

            Connection conn = null;
            try {
                conn = DatabaseConnection.getInstance().getConnection();
                try (Statement stmt = conn.createStatement()) {
                    // Split by semicolons and execute each statement
                    for (String statement : sql.split(";")) {
                        String trimmed = statement.trim();
                        if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                            stmt.execute(trimmed);
                        }
                    }
                }
                logger.info("SQL file executed successfully: {}", resourcePath);
            } finally {
                DatabaseConnection.getInstance().releaseConnection(conn);
            }

        } catch (IOException | SQLException e) {
            logger.error("Error executing SQL file {}: {}", resourcePath, e.getMessage(), e);
            throw new RuntimeException("Failed to execute SQL file", e);
        }
    }
}
