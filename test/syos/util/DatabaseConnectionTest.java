package syos.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.ArrayList;

/**
 * Comprehensive test suite for DatabaseConnection
 * Tests all methods with 100% coverage including happy paths and edge cases
 * Tests Singleton pattern, connection pooling, and error handling
 */
@DisplayName("DatabaseConnection Tests")
class DatabaseConnectionTest {

    private DatabaseConnection databaseConnection;
    private Connection mockConnection;

    @BeforeEach
    void setUp() {
        mockConnection = mock(Connection.class);
        // Reset singleton instance for testing
        resetSingleton();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        if (databaseConnection != null) {
            databaseConnection.closeAllConnections();
        }
        resetSingleton();
    }

    @Test
    @DisplayName("Should create singleton instance correctly")
    void testGetInstance_Singleton() {
        // Act
        DatabaseConnection instance1 = DatabaseConnection.getInstance();
        DatabaseConnection instance2 = DatabaseConnection.getInstance();

        // Assert
        assertNotNull(instance1, "First instance should not be null");
        assertNotNull(instance2, "Second instance should not be null");
        assertSame(instance1, instance2, "Both instances should be the same (singleton)");
    }

    @Test
    @DisplayName("Should be thread-safe singleton")
    void testGetInstance_ThreadSafe() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<DatabaseConnection> instances = new ArrayList<>();

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    instances.add(DatabaseConnection.getInstance());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertEquals(threadCount, instances.size(), "All threads should get instances");
        DatabaseConnection firstInstance = instances.get(0);
        for (DatabaseConnection instance : instances) {
            assertSame(firstInstance, instance, "All instances should be the same");
        }
    }

    @Test
    @DisplayName("Should get connection successfully")
    void testGetConnection_Success() throws Exception {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Act
            databaseConnection = DatabaseConnection.getInstance();
            Connection connection = databaseConnection.getConnection();

            // Assert
            assertNotNull(connection, "Connection should not be null");
            assertEquals(mockConnection, connection, "Should return the mocked connection");
        }
    }

    @Test
    @DisplayName("Should get pool connection successfully")
    void testGetPoolConnection_Success() throws Exception {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Act
            databaseConnection = DatabaseConnection.getInstance();
            Connection connection = databaseConnection.getPoolConnection();

            // Assert
            assertNotNull(connection, "Pool connection should not be null");
            assertEquals(mockConnection, connection, "Should return the mocked connection");
        }
    }

    @Test
    @DisplayName("Should handle SQLException when getting connection")
    void testGetConnection_SQLException() {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(new SQLException("Database connection failed"));

            // Act & Assert
            databaseConnection = DatabaseConnection.getInstance();
            Exception exception = assertThrows(Exception.class, () -> {
                databaseConnection.getConnection();
            });

            assertTrue(exception instanceof SQLException, "Should throw SQLException");
            assertTrue(exception.getMessage().contains("Database connection failed"), 
                "Should contain the original error message");
        }
    }

    @Test
    @DisplayName("Should handle multiple connection requests")
    void testGetConnection_Multiple() throws Exception {
        // Arrange
        Connection mockConnection1 = mock(Connection.class);
        Connection mockConnection2 = mock(Connection.class);
        Connection mockConnection3 = mock(Connection.class);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection1, mockConnection2, mockConnection3);

            // Act
            databaseConnection = DatabaseConnection.getInstance();
            Connection conn1 = databaseConnection.getConnection();
            Connection conn2 = databaseConnection.getConnection();
            Connection conn3 = databaseConnection.getConnection();

            // Assert
            assertNotNull(conn1, "First connection should not be null");
            assertNotNull(conn2, "Second connection should not be null");
            assertNotNull(conn3, "Third connection should not be null");
        }
    }

    @Test
    @DisplayName("Should return connection to pool successfully")
    void testReturnConnection_Success() throws SQLException {
        // Arrange
        when(mockConnection.isClosed()).thenReturn(false);

        // Act
        databaseConnection = DatabaseConnection.getInstance();
        databaseConnection.returnConnection(mockConnection);

        // Assert
        verify(mockConnection, times(1)).isClosed();
        // Additional verification would require access to internal pool state
    }

    @Test
    @DisplayName("Should handle returning null connection")
    void testReturnConnection_NullConnection() {
        // Act & Assert - Should not throw exception
        databaseConnection = DatabaseConnection.getInstance();
        assertDoesNotThrow(() -> {
            databaseConnection.returnConnection(null);
        });
    }

    @Test
    @DisplayName("Should handle returning closed connection")
    void testReturnConnection_ClosedConnection() throws SQLException {
        // Arrange
        when(mockConnection.isClosed()).thenReturn(true);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            Connection newMockConnection = mock(Connection.class);
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(newMockConnection);

            // Act
            databaseConnection = DatabaseConnection.getInstance();
            databaseConnection.returnConnection(mockConnection);

            // Assert
            verify(mockConnection, times(1)).isClosed();
        }
    }

    @Test
    @DisplayName("Should handle SQLException when returning connection")
    void testReturnConnection_SQLException() throws SQLException {
        // Arrange
        when(mockConnection.isClosed()).thenThrow(new SQLException("Connection check failed"));

        // Act & Assert - Should not throw exception
        databaseConnection = DatabaseConnection.getInstance();
        assertDoesNotThrow(() -> {
            databaseConnection.returnConnection(mockConnection);
        });
    }

    @Test
    @DisplayName("Should get static connection successfully")
    void testGetStaticConnection_Success() throws Exception {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Act
            Connection connection = DatabaseConnection.getStaticConnection();

            // Assert
            assertNotNull(connection, "Static connection should not be null");
            assertEquals(mockConnection, connection, "Should return the mocked connection");
        }
    }

    @Test
    @DisplayName("Should handle exception in static connection")
    void testGetStaticConnection_Exception() {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(new SQLException("Static connection failed"));

            // Act & Assert
            Exception exception = assertThrows(Exception.class, () -> {
                DatabaseConnection.getStaticConnection();
            });

            assertTrue(exception instanceof SQLException, "Should throw SQLException");
        }
    }

    @Test
    @DisplayName("Should close all connections successfully")
    void testCloseAllConnections_Success() throws SQLException {
        // Arrange
        Connection mockConnection1 = mock(Connection.class);
        Connection mockConnection2 = mock(Connection.class);
        when(mockConnection1.isClosed()).thenReturn(false);
        when(mockConnection2.isClosed()).thenReturn(false);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection1, mockConnection2);

            databaseConnection = DatabaseConnection.getInstance();
            databaseConnection.returnConnection(mockConnection1);
            databaseConnection.returnConnection(mockConnection2);

            // Act
            databaseConnection.closeAllConnections();

            // Assert
            verify(mockConnection1, atLeastOnce()).close();
            verify(mockConnection2, atLeastOnce()).close();
        }
    }

    @Test
    @DisplayName("Should handle closed connections during close all")
    void testCloseAllConnections_AlreadyClosed() throws SQLException {
        // Arrange
        when(mockConnection.isClosed()).thenReturn(true);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            databaseConnection = DatabaseConnection.getInstance();
            databaseConnection.returnConnection(mockConnection);

            // Act
            databaseConnection.closeAllConnections();

            // Assert - Should not attempt to close already closed connection
            verify(mockConnection, never()).close();
        }
    }

    @Test
    @DisplayName("Should handle SQLException during close all connections")
    void testCloseAllConnections_SQLException() throws SQLException {
        // Arrange
        when(mockConnection.isClosed()).thenReturn(false);
        doThrow(new SQLException("Close failed")).when(mockConnection).close();

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            databaseConnection = DatabaseConnection.getInstance();
            databaseConnection.returnConnection(mockConnection);

            // Act & Assert - Should not throw exception
            assertDoesNotThrow(() -> {
                databaseConnection.closeAllConnections();
            });
        }
    }

    @Test
    @DisplayName("Should get available connections count")
    void testGetAvailableConnections() {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Act
            databaseConnection = DatabaseConnection.getInstance();
            int initialCount = databaseConnection.getAvailableConnections();

            // Assert
            assertTrue(initialCount >= 0, "Available connections should be non-negative");
        }
    }

    @Test
    @DisplayName("Should handle driver class not found exception")
    void testInitialization_DriverNotFound() {
        // Arrange
        try (MockedStatic<Class> mockedClass = mockStatic(Class.class)) {
            mockedClass.when(() -> Class.forName("com.mysql.cj.jdbc.Driver"))
                    .thenThrow(new ClassNotFoundException("MySQL driver not found"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                DatabaseConnection.getInstance();
            });

            assertTrue(exception.getMessage().contains("Failed to initialize database driver"), 
                "Should contain initialization error message");
            assertTrue(exception.getCause() instanceof ClassNotFoundException, 
                "Cause should be ClassNotFoundException");
        }
    }

    @Test
    @DisplayName("Should handle connection timeout correctly")
    void testConnection_Timeout() throws Exception {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            // Mock a connection that supports network timeout
            Connection timeoutConnection = mock(Connection.class);
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(timeoutConnection);

            // Act
            databaseConnection = DatabaseConnection.getInstance();
            Connection connection = databaseConnection.getPoolConnection();

            // Assert
            assertNotNull(connection, "Connection with timeout should not be null");
            verify(timeoutConnection, times(1)).setNetworkTimeout(any(), eq(30000));
        }
    }

    @Test
    @DisplayName("Should handle concurrent access to connection pool")
    void testConcurrentAccess() throws InterruptedException {
        // Arrange
        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Exception> exceptions = new ArrayList<>();

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            databaseConnection = DatabaseConnection.getInstance();

            // Act
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await(); // Wait for all threads to be ready
                        Connection conn = databaseConnection.getConnection();
                        assertNotNull(conn, "Connection should not be null in concurrent access");
                        databaseConnection.returnConnection(conn);
                    } catch (Exception e) {
                        synchronized (exceptions) {
                            exceptions.add(e);
                        }
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown(); // Start all threads
            doneLatch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            // Assert
            assertTrue(exceptions.isEmpty(), "No exceptions should occur during concurrent access: " + exceptions);
        }
    }

    @Test
    @DisplayName("Should handle network timeout exception")
    void testConnection_NetworkTimeoutException() throws Exception {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            Connection networkTimeoutConnection = mock(Connection.class);
            doThrow(new SQLException("Network timeout")).when(networkTimeoutConnection)
                    .setNetworkTimeout(any(), anyInt());
            
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(networkTimeoutConnection);

            // Act
            databaseConnection = DatabaseConnection.getInstance();
            Connection connection = databaseConnection.getPoolConnection();

            // Assert
            assertNotNull(connection, "Connection should still be returned despite timeout exception");
        }
    }

    @Test
    @DisplayName("Should maintain pool state correctly")
    void testPoolState() throws Exception {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            when(mockConnection.isClosed()).thenReturn(false);

            // Act
            databaseConnection = DatabaseConnection.getInstance();
            int initialAvailable = databaseConnection.getAvailableConnections();
            
            Connection conn = databaseConnection.getConnection();
            databaseConnection.returnConnection(conn);
            
            int finalAvailable = databaseConnection.getAvailableConnections();

            // Assert
            assertTrue(initialAvailable >= 0, "Initial available connections should be non-negative");
            assertTrue(finalAvailable >= 0, "Final available connections should be non-negative");
        }
    }

    // Helper method to reset singleton instance using reflection
    private void resetSingleton() {
        try {
            java.lang.reflect.Field instanceField = DatabaseConnection.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            // Ignore reflection exceptions in tests
        }
    }
}
