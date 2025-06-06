package syos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;
import syos.dto.StockBatchDTO;
import syos.service.StockBatchReportService.BatchFilter;
import syos.service.StockBatchReportService.BatchSummary;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test coverage for StockBatchReportService
 * Tests all methods, filtering, database operations, and edge cases
 * Includes database integration tests with Mockito
 */
class StockBatchReportServiceTest {

    @Mock
    private DatabaseConnection mockDatabaseConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private StockBatchReportService stockBatchReportService;
    private MockedStatic<DatabaseConnection> mockedDatabaseConnection;

    @BeforeEach
    void setUp() throws SQLException, Exception {
        MockitoAnnotations.openMocks(this);
        stockBatchReportService = new StockBatchReportService();

        // Setup database connection mocking
        mockedDatabaseConnection = Mockito.mockStatic(DatabaseConnection.class);
        mockedDatabaseConnection.when(DatabaseConnection::getInstance).thenReturn(mockDatabaseConnection);
        when(mockDatabaseConnection.getPoolConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @AfterEach
    void tearDown() {
        if (mockedDatabaseConnection != null) {
            mockedDatabaseConnection.close();
        }
    }

    // === Database Integration Tests ===

    @Test
    @DisplayName("Should fetch all batches from database successfully")
    void testFetchAllBatchesSuccess() throws SQLException {
        // Setup mock data
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("item_code")).thenReturn("FOOD001", "DAIRY001");
        when(mockResultSet.getString("name")).thenReturn("Premium Coffee", "Organic Milk");
        when(mockResultSet.getInt("quantity")).thenReturn(100, 50);
        when(mockResultSet.getInt("used_quantity")).thenReturn(25, 10);
        when(mockResultSet.getDate("purchase_date")).thenReturn(
                Date.valueOf(LocalDate.of(2025, 5, 1)),
                Date.valueOf(LocalDate.of(2025, 6, 1)));
        when(mockResultSet.getDate("expiry_date")).thenReturn(
                Date.valueOf(LocalDate.of(2025, 12, 1)),
                Date.valueOf(LocalDate.of(2025, 8, 1)));
        when(mockResultSet.getDouble("selling_price")).thenReturn(15.99, 3.25);

        // Act
        List<StockBatchDTO> result = stockBatchReportService.getStockBatches(BatchFilter.ALL);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("FOOD001", result.get(0).getItemCode());
        assertEquals("Premium Coffee", result.get(0).getItemName());
        assertEquals(100, result.get(0).getTotalQuantity());
        assertEquals("DAIRY001", result.get(1).getItemCode());
    }

    @Test
    @DisplayName("Should handle SQLException during database fetch")
    void testFetchAllBatchesSQLException() throws SQLException {
        // Setup SQLException
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stockBatchReportService.getStockBatches(BatchFilter.ALL));
        assertTrue(exception.getMessage().contains("Error fetching stock batches"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    @DisplayName("Should handle connection exception during database fetch")
    void testFetchAllBatchesConnectionException() throws SQLException, Exception {
        // Setup connection exception
        when(mockDatabaseConnection.getPoolConnection()).thenThrow(new RuntimeException("Connection failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stockBatchReportService.getStockBatches(BatchFilter.ALL));
        assertTrue(exception.getMessage().contains("Error getting database connection"));
    }

    @Test
    @DisplayName("Should handle empty result set from database")
    void testFetchAllBatchesEmptyResultSet() throws SQLException {
        // Setup empty result set
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<StockBatchDTO> result = stockBatchReportService.getStockBatches(BatchFilter.ALL);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // === Batch Filtering Tests ===

    @Test
    @DisplayName("Should return all batches when filter is ALL")
    void testGetStockBatchesAll() throws SQLException {
        setupMockDataWithStatuses();

        // Act
        List<StockBatchDTO> result = stockBatchReportService.getStockBatches(BatchFilter.ALL);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
    }

    @Test
    @DisplayName("Should return only active batches when filter is ACTIVE_ONLY")
    void testGetStockBatchesActiveOnly() throws SQLException {
        setupMockDataWithStatuses();

        // Act
        List<StockBatchDTO> result = stockBatchReportService.getStockBatches(BatchFilter.ACTIVE_ONLY);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> "ACTIVE".equals(b.getStatus())));
    }

    @Test
    @DisplayName("Should return only expiring soon batches")
    void testGetStockBatchesExpiringSoon() throws SQLException {
        setupMockDataWithStatuses();

        // Act
        List<StockBatchDTO> result = stockBatchReportService.getStockBatches(BatchFilter.EXPIRING_SOON);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EXPIRING_SOON", result.get(0).getStatus());
    }

    @Test
    @DisplayName("Should return only expired batches")
    void testGetStockBatchesExpired() throws SQLException {
        setupMockDataWithStatuses();

        // Act
        List<StockBatchDTO> result = stockBatchReportService.getStockBatches(BatchFilter.EXPIRED);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EXPIRED", result.get(0).getStatus());
    }

    @Test
    @DisplayName("Should return only depleted batches")
    void testGetStockBatchesDepleted() throws SQLException {
        setupMockDataWithStatuses();

        // Act
        List<StockBatchDTO> result = stockBatchReportService.getStockBatches(BatchFilter.DEPLETED);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DEPLETED", result.get(0).getStatus());
    }

    // === Item-Specific Batch Tests ===

    @Test
    @DisplayName("Should return batches for specific item code")
    void testGetBatchesForItem() throws SQLException {
        setupMockDataForItemSearch();

        // Act
        List<StockBatchDTO> result = stockBatchReportService.getBatchesForItem("FOOD001");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("FOOD001", result.get(0).getItemCode());
    }

    @Test
    @DisplayName("Should return empty list for non-existent item code")
    void testGetBatchesForNonExistentItem() throws SQLException {
        setupMockDataForItemSearch();

        // Act
        List<StockBatchDTO> result = stockBatchReportService.getBatchesForItem("NONEXISTENT");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // === Batch Summary Tests ===

    @Test
    @DisplayName("Should calculate correct batch summary statistics")
    void testGetBatchSummary() throws SQLException {
        setupMockDataWithStatuses();

        // Act
        BatchSummary summary = stockBatchReportService.getBatchSummary();

        // Assert
        assertNotNull(summary);
        assertEquals(5, summary.getTotalBatches());
        assertEquals(2, summary.getActiveBatches());
        assertEquals(1, summary.getExpiringSoon());
        assertEquals(1, summary.getExpired());
        assertEquals(1, summary.getDepleted());
    }

    // === IReportService Interface Tests ===

    @Test
    @DisplayName("Should generate default report with all batches")
    void testGenerateReport() throws SQLException {
        setupMockDataWithStatuses();

        // Act
        List<StockBatchDTO> result = stockBatchReportService.generateReport();

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
    }

    @Test
    @DisplayName("Should fallback to default report with invalid filter")
    void testGenerateReportWithInvalidFilter() throws SQLException {
        setupMockDataWithStatuses();

        // Act
        List<StockBatchDTO> result = stockBatchReportService.generateReport("invalid_filter");

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
    }

    @Test
    @DisplayName("Should return correct report name")
    void testGetReportName() {
        // Act & Assert
        assertEquals("Stock Batch Report", stockBatchReportService.getReportName());
    }

    @Test
    @DisplayName("Should return correct report title")
    void testGetReportTitle() {
        // Act & Assert
        assertEquals("SYOS STOCK BATCH REPORT", stockBatchReportService.getReportTitle());
    }

    @Test
    @DisplayName("Should return correct column headers")
    void testGetColumnHeaders() {
        // Act
        List<String> headers = stockBatchReportService.getColumnHeaders();

        // Assert
        assertNotNull(headers);
        assertEquals(7, headers.size());
        assertEquals("Item Code", headers.get(0));
        assertEquals("Item Name", headers.get(1));
        assertEquals("Total Qty", headers.get(2));
        assertEquals("Used Qty", headers.get(3));
        assertEquals("Available", headers.get(4));
        assertEquals("Expiry Date", headers.get(5));
        assertEquals("Status", headers.get(6));
    }

    @Test
    @DisplayName("Should return true when data is available")
    void testIsDataAvailableWithData() throws SQLException {
        setupMockDataWithStatuses();

        // Act & Assert
        assertTrue(stockBatchReportService.isDataAvailable());
    }

    @Test
    @DisplayName("Should return false when no data is available")
    void testIsDataAvailableWithoutData() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        // Act & Assert
        assertFalse(stockBatchReportService.isDataAvailable());
    }

    // === Report Data Formatting Tests ===

    @Test
    @DisplayName("Should format report data correctly")
    void testGetReportData() throws SQLException {
        setupMockDataWithStatuses();

        // Act
        List<List<String>> reportData = stockBatchReportService.getReportData();

        // Assert
        assertNotNull(reportData);
        assertEquals(5, reportData.size());

        List<String> firstRow = reportData.get(0);
        assertEquals("FOOD001", firstRow.get(0));
        assertEquals("Premium Coffee", firstRow.get(1));
        assertEquals("100", firstRow.get(2));
        assertEquals("25", firstRow.get(3));
        assertEquals("75", firstRow.get(4));
        assertEquals("2025-12-01", firstRow.get(5));
        assertEquals("ACTIVE", firstRow.get(6));
    }

    @Test
    @DisplayName("Should handle empty report data")
    void testGetReportDataEmpty() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<List<String>> reportData = stockBatchReportService.getReportData();

        // Assert
        assertNotNull(reportData);
        assertEquals(0, reportData.size());
    }

    // === Row Formatting Tests ===

    @Test
    @DisplayName("Should format row correctly")
    void testGetFormattedRow() {
        // Arrange
        StockBatchDTO batch = new StockBatchDTO("FOOD001", "Premium Coffee", 100, 25,
                LocalDate.of(2025, 5, 1), LocalDate.of(2025, 12, 1), 15.99);

        // Act
        String formattedRow = stockBatchReportService.getFormattedRow(batch);

        // Assert
        assertNotNull(formattedRow);
        assertTrue(formattedRow.contains("FOOD001"));
        assertTrue(formattedRow.contains("Premium Coffee"));
        assertTrue(formattedRow.contains("100"));
        assertTrue(formattedRow.contains("25"));
        assertTrue(formattedRow.contains("75"));
        assertTrue(formattedRow.contains("2025-12-01"));
        assertTrue(formattedRow.contains("ACTIVE"));
    }

    @Test
    @DisplayName("Should handle exact length item names")
    void testGetFormattedRowWithExactLengthName() {
        // Arrange - Create name exactly 20 characters
        StockBatchDTO batch = new StockBatchDTO("EXACT001",
                "Exactly Twenty Chars", // Exactly 20 characters
                50, 10, LocalDate.of(2025, 5, 1), LocalDate.of(2025, 8, 1), 12.50);

        // Act
        String formattedRow = stockBatchReportService.getFormattedRow(batch);

        // Assert
        assertNotNull(formattedRow);
        assertTrue(formattedRow.contains("EXACT001"));
        assertTrue(formattedRow.contains("Exactly Twenty Chars"));
        assertFalse(formattedRow.contains("...")); // Should NOT contain truncation ellipsis
    }

    @Test
    @DisplayName("Should handle zero quantities in formatted row")
    void testGetFormattedRowWithZeroQuantities() {
        // Arrange
        StockBatchDTO batch = new StockBatchDTO("ZERO001", "Zero Quantity Item", 0, 0,
                LocalDate.of(2025, 5, 1), LocalDate.of(2025, 8, 1), 5.00);

        // Act
        String formattedRow = stockBatchReportService.getFormattedRow(batch);

        // Assert
        assertNotNull(formattedRow);
        assertTrue(formattedRow.contains("ZERO001"));
        assertTrue(formattedRow.contains("Zero Quantity Item"));
        assertTrue(formattedRow.contains("0"));
    }

    // === BatchSummary Class Tests ===

    @Test
    @DisplayName("Should create BatchSummary with all parameters")
    void testBatchSummaryConstructor() {
        // Act
        BatchSummary summary = new BatchSummary(10, 6, 2, 1, 1, 500, 350, 150);

        // Assert
        assertEquals(10, summary.getTotalBatches());
        assertEquals(6, summary.getActiveBatches());
        assertEquals(2, summary.getExpiringSoon());
        assertEquals(1, summary.getExpired());
        assertEquals(1, summary.getDepleted());
        assertEquals(500, summary.getTotalStock());
        assertEquals(350, summary.getAvailableStock());
        assertEquals(150, summary.getUsedStock());
    }

    @Test
    @DisplayName("Should handle BatchSummary with zero values")
    void testBatchSummaryWithZeros() {
        // Act
        BatchSummary summary = new BatchSummary(0, 0, 0, 0, 0, 0, 0, 0);

        // Assert
        assertEquals(0, summary.getTotalBatches());
        assertEquals(0, summary.getActiveBatches());
        assertEquals(0, summary.getExpiringSoon());
        assertEquals(0, summary.getExpired());
        assertEquals(0, summary.getDepleted());
        assertEquals(0, summary.getTotalStock());
        assertEquals(0, summary.getAvailableStock());
        assertEquals(0, summary.getUsedStock());
    }

    @Test
    @DisplayName("Should handle BatchSummary with large values")
    void testBatchSummaryWithLargeValues() {
        // Act
        BatchSummary summary = new BatchSummary(1000, 600, 200, 100, 100, 50000, 35000, 15000);

        // Assert
        assertEquals(1000, summary.getTotalBatches());
        assertEquals(600, summary.getActiveBatches());
        assertEquals(200, summary.getExpiringSoon());
        assertEquals(100, summary.getExpired());
        assertEquals(100, summary.getDepleted());
        assertEquals(50000, summary.getTotalStock());
        assertEquals(35000, summary.getAvailableStock());
        assertEquals(15000, summary.getUsedStock());
    }

    // === Edge Case Tests ===

    @Test
    @DisplayName("Should handle multiple batches with same item code")
    void testMultipleBatchesSameItem() throws SQLException {
        // Setup mock for multiple batches with same item code
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("item_code")).thenReturn("FOOD001", "FOOD001");
        when(mockResultSet.getString("name")).thenReturn("Coffee Batch 1", "Coffee Batch 2");
        when(mockResultSet.getInt("quantity")).thenReturn(50, 30);
        when(mockResultSet.getInt("used_quantity")).thenReturn(10, 5);
        when(mockResultSet.getDate("purchase_date")).thenReturn(
                Date.valueOf(LocalDate.of(2025, 5, 1)),
                Date.valueOf(LocalDate.of(2025, 5, 15)));
        when(mockResultSet.getDate("expiry_date")).thenReturn(
                Date.valueOf(LocalDate.of(2025, 12, 1)),
                Date.valueOf(LocalDate.of(2025, 11, 15)));
        when(mockResultSet.getDouble("selling_price")).thenReturn(15.99, 15.99);

        // Act
        List<StockBatchDTO> result = stockBatchReportService.getBatchesForItem("FOOD001");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> "FOOD001".equals(b.getItemCode())));
    }

    @Test
    @DisplayName("Should handle all BatchFilter enum values")
    void testAllBatchFilterValues() {
        // Test that all enum values are handled
        BatchFilter[] filters = BatchFilter.values();
        assertEquals(5, filters.length);

        // Verify enum values exist
        assertNotNull(BatchFilter.ALL);
        assertNotNull(BatchFilter.ACTIVE_ONLY);
        assertNotNull(BatchFilter.EXPIRING_SOON);
        assertNotNull(BatchFilter.EXPIRED);
        assertNotNull(BatchFilter.DEPLETED);

        // Verify enum names
        assertEquals("ALL", BatchFilter.ALL.name());
        assertEquals("ACTIVE_ONLY", BatchFilter.ACTIVE_ONLY.name());
        assertEquals("EXPIRING_SOON", BatchFilter.EXPIRING_SOON.name());
        assertEquals("EXPIRED", BatchFilter.EXPIRED.name());
        assertEquals("DEPLETED", BatchFilter.DEPLETED.name());
    }

    @Test
    @DisplayName("Should handle generateReport with empty filters array")
    void testGenerateReportWithEmptyFilters() throws SQLException {
        setupMockDataWithStatuses();

        // Act
        List<StockBatchDTO> result = stockBatchReportService.generateReport(new Object[0]);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
    }

    // === Helper Methods ===

    private void setupMockDataWithStatuses() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, true, true, true, false);

        // Setup item codes
        when(mockResultSet.getString("item_code"))
                .thenReturn("FOOD001", "FOOD002", "DAIRY001", "PROD001", "SNCK001");

        // Setup item names
        when(mockResultSet.getString("name"))
                .thenReturn("Premium Coffee", "Artisan Bread", "Organic Milk", "Natural Yogurt", "Dark Chocolate");

        // Setup quantities
        when(mockResultSet.getInt("quantity"))
                .thenReturn(100, 50, 30, 20, 25);
        when(mockResultSet.getInt("used_quantity"))
                .thenReturn(25, 45, 15, 5, 25);

        // Setup dates
        when(mockResultSet.getDate("purchase_date")).thenReturn(
                Date.valueOf(LocalDate.of(2025, 5, 1)),
                Date.valueOf(LocalDate.of(2025, 6, 3)),
                Date.valueOf(LocalDate.of(2025, 6, 1)),
                Date.valueOf(LocalDate.of(2025, 5, 20)),
                Date.valueOf(LocalDate.of(2025, 5, 15)));
        when(mockResultSet.getDate("expiry_date")).thenReturn(
                Date.valueOf(LocalDate.of(2025, 12, 1)),
                Date.valueOf(LocalDate.of(2025, 6, 10)),
                Date.valueOf(LocalDate.of(2025, 6, 8)),
                Date.valueOf(LocalDate.of(2025, 6, 3)),
                Date.valueOf(LocalDate.of(2025, 8, 15)));

        // Setup prices
        when(mockResultSet.getDouble("selling_price"))
                .thenReturn(15.99, 4.50, 3.25, 2.99, 8.75);
    }

    private void setupMockDataForItemSearch() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("item_code")).thenReturn("FOOD001", "DAIRY001");
        when(mockResultSet.getString("name")).thenReturn("Premium Coffee", "Organic Milk");
        when(mockResultSet.getInt("quantity")).thenReturn(100, 50);
        when(mockResultSet.getInt("used_quantity")).thenReturn(25, 10);
        when(mockResultSet.getDate("purchase_date")).thenReturn(
                Date.valueOf(LocalDate.of(2025, 5, 1)),
                Date.valueOf(LocalDate.of(2025, 6, 1)));
        when(mockResultSet.getDate("expiry_date")).thenReturn(
                Date.valueOf(LocalDate.of(2025, 12, 1)),
                Date.valueOf(LocalDate.of(2025, 8, 1)));
        when(mockResultSet.getDouble("selling_price")).thenReturn(15.99, 3.25);
    }
}
