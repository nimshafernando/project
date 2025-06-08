package syos.util;

import syos.interfaces.DatabaseConnectionProvider;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ensures single instance of connection manager and efficient connection
 * pooling
 * Implements SOLID principles: SRP (single responsibility for DB connections),
 * OCP (open for extension via interface), DIP (depends on abstractions)
 */
public class DatabaseConnection implements DatabaseConnectionProvider {

    // Singleton pattern implementation
    private static volatile DatabaseConnection instance;
    private static final Object lock = new Object();

    // Connection pool configuration
    private static final String URL = "jdbc:mysql://localhost:3306/syosdb";
    private static final String USER = "root";
    private static final String PASSWORD = "nimsha123";
    private static final int POOL_SIZE = 10;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    // Object Pool pattern implementation
    private final BlockingQueue<Connection> connectionPool;
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    /**
     * Private constructor for Singleton pattern
     * Implements lazy initialization with double-checked locking
     */
    private DatabaseConnection() {
        this.connectionPool = new LinkedBlockingQueue<>(POOL_SIZE);
        initializePool();
    }

    /**
     * Thread-safe Singleton instance getter
     * 
     * @return DatabaseConnection singleton instance
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize connection pool with pre-created connections
     * Follows KISS principle - simple pool initialization
     */
    private void initializePool() {
        if (isInitialized.compareAndSet(false, true)) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                for (int i = 0; i < POOL_SIZE; i++) {
                    Connection connection = createNewConnection();
                    if (connection != null) {
                        connectionPool.offer(connection);
                    }
                }
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
                throw new RuntimeException("Failed to initialize database driver", e);
            }
        }
    }

    /**
     * Create new database connection with retry mechanism
     * 
     * @return new Connection or null if failed
     */
    private Connection createNewConnection() {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                System.err.println("Connection attempt " + attempt + " failed: " + e.getMessage());
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    System.err.println("Failed to create connection after " + MAX_RETRY_ATTEMPTS + " attempts");
                }
            }
        }
        return null;
    }

    /**
     * Get connection from pool (blocking if none available)
     * Implements Object Pool pattern for resource management
     * 
     * @return Connection from pool
     * @throws Exception if unable to get connection
     */
    @Override
    public Connection getPoolConnection() throws Exception {
        try {
            // Set a timeout of 10 seconds for getting connection
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            // Set query timeout to 30 seconds
            conn.setNetworkTimeout(Executors.newFixedThreadPool(1), 30000);

            return conn;
        } catch (SQLException e) {
            System.out.println("Failed to get connection from pool: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get single connection - implements interface requirement
     * 
     * @return Connection
     * @throws Exception if unable to get connection
     */
    @Override
    public Connection getConnection() throws Exception {
        return getPoolConnection();
    }

    /**
     * Return connection to pool for reuse
     * Essential for Object Pool pattern - prevents connection leaks
     * 
     * @param connection Connection to return to pool
     */
    public void returnConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connectionPool.offer(connection);
                } else {
                    // Replace closed connection with new one
                    Connection newConnection = createNewConnection();
                    if (newConnection != null) {
                        connectionPool.offer(newConnection);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error returning connection to pool: " + e.getMessage());
            }
        }
    }

    /**
     * Static method for backward compatibility
     * 
     * @return Connection from pool
     */
    public static Connection getStaticConnection() throws Exception {
        return getInstance().getPoolConnection();
    }

    /**
     * Close all connections in pool - for cleanup
     * Implements proper resource management
     */
    public void closeAllConnections() {
        while (!connectionPool.isEmpty()) {
            try {
                Connection connection = connectionPool.poll();
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Get current pool size for monitoring
     * 
     * @return number of available connections in pool
     */
    public int getAvailableConnections() {
        return connectionPool.size();
    }
}
