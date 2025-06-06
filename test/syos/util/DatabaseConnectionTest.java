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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Field;

/**
 * 
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

    /**
     * Helper method to reset the singleton instance using reflection
     */
    private void resetSingleton() {
        try {
            Field instanceField = DatabaseConnection.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            // If reflection fails, we can continue with tests
            System.out.println("Warning: Could not reset singleton instance");
        }
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

}
