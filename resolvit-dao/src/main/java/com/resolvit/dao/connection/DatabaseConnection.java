package com.resolvit.dao.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Singleton database connection pool for ResolvIt.
 * Provides thread-safe connection management with configurable pool size.
 */
public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    private static volatile DatabaseConnection instance;
    private static final int DEFAULT_POOL_SIZE = 10;
    private static final int CONNECTION_TIMEOUT_SECONDS = 30;

    private final BlockingQueue<Connection> connectionPool;
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final int poolSize;
    private boolean isInitialized;

    /**
     * Private constructor - use getInstance() instead.
     */
    private DatabaseConnection() {
        Properties props = loadProperties();
        this.dbUrl = props.getProperty("db.url", 
            "jdbc:mysql://localhost:3306/resolvit_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        this.dbUsername = props.getProperty("db.username", "root");
        this.dbPassword = props.getProperty("db.password", "");
        this.poolSize = Integer.parseInt(props.getProperty("db.pool.size", String.valueOf(DEFAULT_POOL_SIZE)));
        this.connectionPool = new ArrayBlockingQueue<>(poolSize);
        this.isInitialized = false;
    }

    /**
     * Get the singleton instance using double-checked locking.
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize the connection pool.
     * Must be called before using connections.
     */
    public synchronized void initialize() {
        if (isInitialized) {
            logger.warn("Connection pool already initialized");
            return;
        }

        logger.info("Initializing database connection pool with {} connections", poolSize);

        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }

        // Create initial connections
        for (int i = 0; i < poolSize; i++) {
            try {
                Connection conn = createConnection();
                connectionPool.offer(conn);
                logger.debug("Created connection {} of {}", i + 1, poolSize);
            } catch (SQLException e) {
                logger.error("Failed to create connection {} of {}: {}", i + 1, poolSize, e.getMessage());
                throw new RuntimeException("Failed to initialize connection pool", e);
            }
        }

        isInitialized = true;
        logger.info("Connection pool initialized successfully with {} connections", connectionPool.size());
    }

    /**
     * Get a connection from the pool.
     * Blocks until a connection is available or timeout is reached.
     */
    public Connection getConnection() throws SQLException {
        if (!isInitialized) {
            initialize();
        }

        try {
            Connection conn = connectionPool.poll(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (conn == null) {
                logger.warn("Connection pool exhausted, creating new connection");
                return createConnection();
            }

            // Validate connection
            if (conn.isClosed() || !conn.isValid(5)) {
                logger.debug("Invalid connection found, creating new one");
                conn = createConnection();
            }

            return conn;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for database connection", e);
        }
    }

    /**
     * Release a connection back to the pool.
     */
    public void releaseConnection(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            if (!conn.isClosed() && conn.isValid(5)) {
                // Reset connection state
                if (!conn.getAutoCommit()) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                }

                // Try to return to pool
                if (!connectionPool.offer(conn)) {
                    // Pool is full, close the connection
                    logger.debug("Connection pool full, closing connection");
                    conn.close();
                }
            } else {
                conn.close();
            }
        } catch (SQLException e) {
            logger.error("Error releasing connection: {}", e.getMessage());
            try {
                conn.close();
            } catch (SQLException ex) {
                // Ignore
            }
        }
    }

    /**
     * Close all connections and shutdown the pool.
     */
    public synchronized void shutdown() {
        logger.info("Shutting down connection pool");

        Connection conn;
        while ((conn = connectionPool.poll()) != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("Error closing connection during shutdown: {}", e.getMessage());
            }
        }

        isInitialized = false;
        logger.info("Connection pool shutdown complete");
    }

    /**
     * Get the current pool size.
     */
    public int getAvailableConnections() {
        return connectionPool.size();
    }

    /**
     * Check if the pool is initialized.
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Create a new database connection.
     */
    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    /**
     * Load database properties from db.properties file.
     */
    private Properties loadProperties() {
        Properties props = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
                logger.info("Loaded database configuration from db.properties");
            } else {
                logger.warn("db.properties not found, using default configuration");
            }
        } catch (IOException e) {
            logger.error("Error loading db.properties: {}", e.getMessage());
        }

        return props;
    }

    /**
     * Test the database connection.
     */
    public boolean testConnection() {
        try (Connection conn = createConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            logger.error("Database connection test failed: {}", e.getMessage());
            return false;
        }
    }
}
