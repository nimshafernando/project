package syos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import syos.dto.ReorderItemDTO;
import syos.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("ReorderLevelReportService Tests")
class ReorderLevelReportServiceTest {

    @Mock
    private DatabaseConnection databaseConnection;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private ReorderLevelReportService service;

    // Test data constants
    private static final String ITEM_CODE_LAPTOP = "LAP001";
    private static final String ITEM_CODE_PHONE = "PHN001";
    private static final String ITEM_NAME_LAPTOP = "Gaming Laptop Pro";
    private static final String ITEM_NAME_PHONE = "Smartphone X";
    private static final double PRICE_LAPTOP = 1299.99;
    private static final double PRICE_PHONE = 699.99;
    private static final int QUANTITY_LOW = 25;
    private static final int QUANTITY_VERY_LOW = 5;
    private static final int DEFAULT_THRESHOLD = 50;
    private static final int CUSTOM_THRESHOLD = 30;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReorderLevelReportService();
    }

    @Test
    @DisplayName("Should return combined in-store and online items below default reorder level threshold")
    void getItemsBelowReorderLevel_WithDefaultThreshold_ShouldReturnCombinedResults() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();
            setupOnlineResultSet();

            List<ReorderItemDTO> result = service.getItemsBelowReorderLevel();

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(connection, times(2)).prepareStatement(anyString());
            verify(preparedStatement, times(2)).setInt(1, DEFAULT_THRESHOLD);
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return combined items below custom reorder level threshold")
    void getItemsBelowReorderLevel_WithCustomThreshold_ShouldReturnFilteredResults() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();
            setupOnlineResultSet();

            List<ReorderItemDTO> result = service.getItemsBelowReorderLevel(CUSTOM_THRESHOLD);

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(preparedStatement, times(2)).setInt(1, CUSTOM_THRESHOLD);
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return in-store items below default reorder level threshold")
    void getInStoreReorderItems_WithDefaultThreshold_ShouldReturnInStoreItems() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            List<ReorderItemDTO> result = service.getInStoreReorderItems();

            assertNotNull(result);
            assertEquals(1, result.size());

            ReorderItemDTO item = result.get(0);
            assertEquals(ITEM_CODE_LAPTOP, item.getCode());
            assertEquals(ITEM_NAME_LAPTOP, item.getName());
            assertEquals(QUANTITY_LOW, item.getCurrentStock());
            assertEquals(DEFAULT_THRESHOLD, item.getReorderLevel());
            assertEquals(PRICE_LAPTOP, item.getPrice());
            assertEquals("IN_STORE", item.getStoreType());

            verify(preparedStatement).setInt(1, DEFAULT_THRESHOLD);
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return in-store items below custom reorder level threshold")
    void getInStoreReorderItems_WithCustomThreshold_ShouldReturnFilteredItems() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            // Act
            List<ReorderItemDTO> result = service.getInStoreReorderItems(CUSTOM_THRESHOLD);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(preparedStatement).setInt(1, CUSTOM_THRESHOLD);
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return online items below default reorder level threshold")
    void getOnlineReorderItems_WithDefaultThreshold_ShouldReturnOnlineItems() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupOnlineResultSet();

            // Act
            List<ReorderItemDTO> result = service.getOnlineReorderItems();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());

            ReorderItemDTO item = result.get(0);
            assertEquals(ITEM_CODE_PHONE, item.getCode());
            assertEquals(ITEM_NAME_PHONE, item.getName());
            assertEquals(QUANTITY_VERY_LOW, item.getCurrentStock());
            assertEquals(DEFAULT_THRESHOLD, item.getReorderLevel());
            assertEquals(PRICE_PHONE, item.getPrice());
            assertEquals("ONLINE", item.getStoreType());
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return online items below custom reorder level threshold")
    void getOnlineReorderItems_WithCustomThreshold_ShouldReturnFilteredItems() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupOnlineResultSet();

            // Act
            List<ReorderItemDTO> result = service.getOnlineReorderItems(CUSTOM_THRESHOLD);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(preparedStatement).setInt(1, CUSTOM_THRESHOLD);
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should throw runtime exception when in-store SQL exception occurs")
    void fetchInStoreReorderItems_WithSQLException_ShouldThrowRuntimeException() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);
            when(databaseConnection.getPoolConnection()).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> service.getInStoreReorderItems(CUSTOM_THRESHOLD));
            assertTrue(exception.getMessage().contains("Error fetching in-store reorder items"));
            assertTrue(exception.getCause() instanceof SQLException);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should throw runtime exception when online connection fails")
    void fetchOnlineReorderItems_WithSQLException_ShouldThrowRuntimeException() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);
            when(databaseConnection.getPoolConnection()).thenThrow(new RuntimeException("Connection failed"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> service.getOnlineReorderItems(CUSTOM_THRESHOLD));

            assertTrue(exception.getMessage().contains("Error getting database connection"));
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should return empty list when no items below reorder level")
    void getItemsBelowReorderLevel_WithEmptyResults_ShouldReturnEmptyList() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            when(resultSet.next()).thenReturn(false); // No results

            // Act
            List<ReorderItemDTO> result = service.getItemsBelowReorderLevel(CUSTOM_THRESHOLD);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return results when zero threshold is provided")
    void getItemsBelowReorderLevel_WithZeroThreshold_ShouldReturnResults() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            // Act
            List<ReorderItemDTO> result = service.getItemsBelowReorderLevel(0);

            // Assert
            assertNotNull(result);
            verify(preparedStatement, times(2)).setInt(1, 0);
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return results when negative threshold is provided")
    void getItemsBelowReorderLevel_WithNegativeThreshold_ShouldReturnResults() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            // Act
            List<ReorderItemDTO> result = service.getItemsBelowReorderLevel(-10);

            // Assert
            assertNotNull(result);
            verify(preparedStatement, times(2)).setInt(1, -10);
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    } // IReportService interface tests

    @Test
    @DisplayName("Should return default report when generateReport is called")
    void generateReport_ShouldReturnDefaultReport() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            // Act
            List<ReorderItemDTO> result = service.generateReport();

            // Assert
            assertNotNull(result);
            verify(preparedStatement, times(2)).setInt(1, DEFAULT_THRESHOLD);
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return in-store only when using in-store filter")
    void generateReport_WithInStoreFilter_ShouldReturnInStoreOnly() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            // Act
            List<ReorderItemDTO> result = service.generateReport(ReorderLevelReportService.StoreFilter.IN_STORE_ONLY);

            // Assert
            assertNotNull(result);
            // Should only call in-store query once
            verify(connection, times(1)).prepareStatement(anyString());
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return online only when using online filter")
    void generateReport_WithOnlineFilter_ShouldReturnOnlineOnly() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupOnlineResultSet();

            // Act
            List<ReorderItemDTO> result = service.generateReport(ReorderLevelReportService.StoreFilter.ONLINE_ONLY);

            // Assert
            assertNotNull(result);
            verify(connection, times(1)).prepareStatement(anyString());
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return both stores when using both stores filter")
    void generateReport_WithBothStoresFilter_ShouldReturnBothStores() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            // Act
            List<ReorderItemDTO> result = service.generateReport(ReorderLevelReportService.StoreFilter.BOTH_STORES);

            // Assert
            assertNotNull(result);
            verify(connection, times(2)).prepareStatement(anyString());
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return default report when invalid filter is provided")
    void generateReport_WithInvalidFilter_ShouldReturnDefaultReport() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            // Act
            List<ReorderItemDTO> result = service.generateReport("invalid_filter");

            // Assert
            assertNotNull(result);
            verify(connection, times(2)).prepareStatement(anyString());
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return correct report name")
    void getReportName_ShouldReturnCorrectName() {
        // Act
        String reportName = service.getReportName();

        // Assert
        assertEquals("Reorder Level Report", reportName);
    }

    @Test
    @DisplayName("Should return correct report title")
    void getReportTitle_ShouldReturnCorrectTitle() {
        // Act
        String reportTitle = service.getReportTitle();

        // Assert
        assertEquals("SYOS REORDER LEVEL REPORT", reportTitle);
    }

    @Test
    @DisplayName("Should return correct column headers")
    void getColumnHeaders_ShouldReturnCorrectHeaders() {
        // Act
        List<String> headers = service.getColumnHeaders();

        // Assert
        assertNotNull(headers);
        assertEquals(6, headers.size());
        assertEquals("Code", headers.get(0));
        assertEquals("Name", headers.get(1));
        assertEquals("Current Stock", headers.get(2));
        assertEquals("Shortfall", headers.get(3));
        assertEquals("Price", headers.get(4));
        assertEquals("Store Type", headers.get(5));
    }

    @Test
    @DisplayName("Should return true when data is available")
    void isDataAvailable_WithData_ShouldReturnTrue() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            // Act
            boolean isAvailable = service.isDataAvailable();

            // Assert
            assertTrue(isAvailable);
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return false when no data is available")
    void isDataAvailable_WithNoData_ShouldReturnFalse() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            when(resultSet.next()).thenReturn(false); // No data

            // Act
            boolean isAvailable = service.isDataAvailable();

            // Assert
            assertFalse(isAvailable);
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return formatted data for report display")
    void getReportData_ShouldReturnFormattedData() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            // Act
            List<List<String>> reportData = service.getReportData();

            // Assert
            assertNotNull(reportData);
            assertEquals(1, reportData.size());

            List<String> firstRow = reportData.get(0);
            assertEquals(6, firstRow.size());
            assertEquals(ITEM_CODE_LAPTOP, firstRow.get(0));
            assertEquals(ITEM_NAME_LAPTOP, firstRow.get(1));
            assertEquals("25", firstRow.get(2));
            assertEquals("25", firstRow.get(3)); // shortfall = reorderLevel - currentStock
            assertEquals("1299.99", firstRow.get(4));
            assertEquals("IN_STORE", firstRow.get(5));
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return correctly formatted string for single item")
    void getFormattedRow_ShouldReturnCorrectlyFormattedString() {
        // Arrange
        ReorderItemDTO item = new ReorderItemDTO(ITEM_CODE_LAPTOP, ITEM_NAME_LAPTOP,
                QUANTITY_LOW, DEFAULT_THRESHOLD, PRICE_LAPTOP, "IN_STORE");

        // Act
        String formattedRow = service.getFormattedRow(item);

        // Assert
        assertNotNull(formattedRow);
        assertTrue(formattedRow.contains(ITEM_CODE_LAPTOP));
        assertTrue(formattedRow.contains(ITEM_NAME_LAPTOP));
        assertTrue(formattedRow.contains("25"));
        assertTrue(formattedRow.contains("1299.99"));
        assertTrue(formattedRow.contains("IN_STORE"));
    }

    @Test
    @DisplayName("Should truncate long item names with ellipsis")
    void getFormattedRow_WithLongItemName_ShouldTruncateName() {
        // Arrange
        String longName = "This is a very long item name that should be truncated for display";
        ReorderItemDTO item = new ReorderItemDTO(ITEM_CODE_LAPTOP, longName,
                QUANTITY_LOW, DEFAULT_THRESHOLD, PRICE_LAPTOP, "IN_STORE");

        // Act
        String formattedRow = service.getFormattedRow(item);

        // Assert
        assertNotNull(formattedRow);
        assertTrue(formattedRow.contains("..."));
        assertTrue(formattedRow.length() > 0);
    }

    @Test
    @DisplayName("Should return original name when name is short enough")
    void truncateName_WithShortName_ShouldReturnOriginalName() {
        // Arrange
        ReorderLevelReportService serviceForPrivateMethod = new ReorderLevelReportService();
        String shortName = "Short Name";

        // Act - using reflection to test private method
        try {
            java.lang.reflect.Method method = ReorderLevelReportService.class.getDeclaredMethod("truncateName",
                    String.class, int.class);
            method.setAccessible(true);
            String result = (String) method.invoke(serviceForPrivateMethod, shortName, 25);

            // Assert
            assertEquals(shortName, result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should truncate long names and add ellipsis when exceeding length limit")
    void truncateName_WithLongName_ShouldTruncateWithEllipsis() {
        // Arrange
        ReorderLevelReportService serviceForPrivateMethod = new ReorderLevelReportService();
        String longName = "This is a very long name that should be truncated";

        // Act - using reflection to test private method
        try {
            java.lang.reflect.Method method = ReorderLevelReportService.class.getDeclaredMethod("truncateName",
                    String.class, int.class);
            method.setAccessible(true);
            String result = (String) method.invoke(serviceForPrivateMethod, longName, 25);

            // Assert
            assertEquals(25, result.length());
            assertTrue(result.endsWith("..."));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    // Helper methods

    private void setupDatabaseMocks(MockedStatic<DatabaseConnection> mockedStatic) throws SQLException {
        mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);
        try {
            doReturn(connection).when(databaseConnection).getPoolConnection();
        } catch (Exception e) {
            fail("Mock setup failed: " + e.getMessage());
        }
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void setupInStoreResultSet() throws SQLException {
        when(resultSet.next()).thenReturn(true, false); // One record, then end
        when(resultSet.getString("code")).thenReturn(ITEM_CODE_LAPTOP);
        when(resultSet.getString("name")).thenReturn(ITEM_NAME_LAPTOP);
        when(resultSet.getInt("quantity")).thenReturn(QUANTITY_LOW);
        when(resultSet.getDouble("price")).thenReturn(PRICE_LAPTOP);
    }

    private void setupOnlineResultSet() throws SQLException {
        when(resultSet.next()).thenReturn(true, false); // One record, then end
        when(resultSet.getString("item_code")).thenReturn(ITEM_CODE_PHONE);
        when(resultSet.getString("name")).thenReturn(ITEM_NAME_PHONE);
        when(resultSet.getInt("quantity")).thenReturn(QUANTITY_VERY_LOW);
        when(resultSet.getDouble("price")).thenReturn(PRICE_PHONE);
    }
}