package syos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import syos.dto.ReportItemDTO;
import syos.util.DatabaseConnection;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("SalesReportService Tests")
class SalesReportServiceTest {

    @Mock
    private DatabaseConnection databaseConnection;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private SalesReportService service; // Test data constants
    private static final String ITEM_CODE_LAPTOP = "LAP001";
    private static final String ITEM_CODE_PHONE = "PHN001";
    private static final String ITEM_CODE_TABLET = "TAB001";
    private static final String ITEM_NAME_LAPTOP = "Gaming Laptop Pro";
    private static final String ITEM_NAME_PHONE = "Smartphone X";
    private static final String ITEM_NAME_TABLET = "Android Tablet";
    private static final int QUANTITY_SOLD_10 = 10;
    private static final int QUANTITY_SOLD_5 = 5;
    private static final int QUANTITY_SOLD_15 = 15;
    private static final double REVENUE_1000 = 1000.0;
    private static final double REVENUE_500 = 500.0;
    private static final double REVENUE_1500 = 1500.0;
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 6, 15);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SalesReportService();
    }

    private DatabaseConnection setupMockDatabaseConnection() {
        DatabaseConnection mockDbConnection = mock(DatabaseConnection.class);
        try {
            doReturn(connection).when(mockDbConnection).getPoolConnection();
        } catch (Exception e) {
            fail("Mock setup failed: " + e.getMessage());
        }
        return mockDbConnection;
    }

    @Test
    @DisplayName("Should return in-store items when using in-store store type and in-store transaction type")
    void getSalesReport_WithInStoreStoreTypeAndInStoreTransactionType_ShouldReturnInStoreItems() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                    SalesReportService.StoreType.IN_STORE,
                    SalesReportService.TransactionType.IN_STORE);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(ITEM_CODE_LAPTOP, result.get(0).getCode());
            assertTrue(result.get(0).getName().contains("(In-Store)"));
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return online items when using online store type and all transaction type")
    void getSalesReport_WithOnlineStoreTypeAndAllTransactionType_ShouldReturnOnlineItems() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            setupDatabaseMocks(mockedStatic);
            setupOnlineResultSet();

            List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                    SalesReportService.StoreType.ONLINE,
                    SalesReportService.TransactionType.ALL);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(ITEM_CODE_PHONE, result.get(0).getCode());
            assertTrue(result.get(0).getName().contains("(Online)"));
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return both store types when using combined store type and all transaction type")
    void getSalesReport_WithCombinedStoreTypeAndAllTransactionType_ShouldReturnBothStoreItems() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            mockedStatic.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);

            // Create separate result sets for in-store and online queries
            ResultSet inStoreResultSet = mock(ResultSet.class);
            ResultSet onlineResultSet = mock(ResultSet.class);

            // Setup in-store result set
            when(inStoreResultSet.next()).thenReturn(true, false);
            when(inStoreResultSet.getString("item_code")).thenReturn(ITEM_CODE_LAPTOP);
            when(inStoreResultSet.getString("item_name")).thenReturn(ITEM_NAME_LAPTOP);
            when(inStoreResultSet.getInt("total_qty")).thenReturn(QUANTITY_SOLD_10);
            when(inStoreResultSet.getDouble("total_revenue")).thenReturn(REVENUE_1000);

            // Setup online result set
            when(onlineResultSet.next()).thenReturn(true, false);
            when(onlineResultSet.getString("item_code")).thenReturn(ITEM_CODE_PHONE);
            when(onlineResultSet.getString("name")).thenReturn(ITEM_NAME_PHONE);
            when(onlineResultSet.getInt("total_qty")).thenReturn(QUANTITY_SOLD_5);
            when(onlineResultSet.getDouble("total_revenue")).thenReturn(REVENUE_500);

            // Setup prepared statements to return appropriate result sets
            PreparedStatement inStoreStmt = mock(PreparedStatement.class);
            PreparedStatement onlineStmt = mock(PreparedStatement.class);

            when(inStoreStmt.executeQuery()).thenReturn(inStoreResultSet);
            when(onlineStmt.executeQuery()).thenReturn(onlineResultSet);

            // Configure connection to return appropriate prepared statements based on SQL
            when(connection.prepareStatement(contains("bill_items"))).thenReturn(inStoreStmt);
            when(connection.prepareStatement(contains("online_bill_items"))).thenReturn(onlineStmt);

            // Act
            List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                    SalesReportService.StoreType.COMBINED,
                    SalesReportService.TransactionType.ALL);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());

            // Verify both in-store and online items are present
            assertTrue(result.stream().anyMatch(item -> item.getName().contains("(In-Store)")));
            assertTrue(result.stream().anyMatch(item -> item.getName().contains("(Online)")));

            // Verify both queries were executed (in-store and online)
            verify(connection, times(2)).prepareStatement(anyString());
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should filter by reservation pay-in-store payment method for online transactions")
    void getSalesReport_WithOnlineStoreTypeAndReservationPayInStoreTransactionType_ShouldFilterByPaymentMethod() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupOnlineResultSet();

            // Act
            List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                    SalesReportService.StoreType.ONLINE,
                    SalesReportService.TransactionType.RESERVATION_PAY_IN_STORE);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());

            // Verify the SQL contains the payment method filter
            verify(connection).prepareStatement(contains("payment_method = 'Pay in Store'"));
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should filter by reservation COD payment method for online transactions")
    void getSalesReport_WithOnlineStoreTypeAndReservationCODTransactionType_ShouldFilterByPaymentMethod() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupOnlineResultSet();

            // Act
            List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                    SalesReportService.StoreType.ONLINE,
                    SalesReportService.TransactionType.RESERVATION_COD);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());

            verify(connection).prepareStatement(contains("payment_method = 'Cash on Delivery'"));
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should filter by both reservation payment methods for online transactions")
    void getSalesReport_WithOnlineStoreTypeAndReservationTransactionType_ShouldFilterByBothPaymentMethods() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupOnlineResultSet();

            // Act
            List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                    SalesReportService.StoreType.ONLINE,
                    SalesReportService.TransactionType.RESERVATION);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());

            verify(connection).prepareStatement(contains("payment_method IN ('Pay in Store', 'Cash on Delivery')"));
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return empty list when using online store type with in-store transaction type")
    void getSalesReport_WithOnlineStoreTypeAndInStoreTransactionType_ShouldReturnEmptyList() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);

            // Act
            List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                    SalesReportService.StoreType.ONLINE,
                    SalesReportService.TransactionType.IN_STORE);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());

            // Verify no database queries were executed
            verify(connection, never()).prepareStatement(anyString());
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return only in-store items when using combined store type with in-store transaction type")
    void getSalesReport_WithCombinedStoreTypeAndInStoreTransactionType_ShouldReturnOnlyInStoreItems() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            // Act
            List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                    SalesReportService.StoreType.COMBINED,
                    SalesReportService.TransactionType.IN_STORE);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getName().contains("(In-Store)"));

            // Verify only in-store query was executed
            verify(connection, times(1)).prepareStatement(anyString());
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should handle SQL exceptions gracefully and continue processing")
    void getSalesReport_WithInStoreSQLException_ShouldHandleGracefullyAndContinue() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            PrintStream originalOut = System.out;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));

            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            mockedStatic.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            try {
                // Act
                List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                        SalesReportService.StoreType.IN_STORE,
                        SalesReportService.TransactionType.IN_STORE);

                // Assert
                assertNotNull(result);
                assertTrue(result.isEmpty());

                String output = baos.toString();
                assertTrue(output.contains("Error fetching in-store bill data"));
            } finally {
                System.setOut(originalOut);
            }
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should handle online connection exceptions gracefully and continue processing")
    void getSalesReport_WithOnlineConnectionException_ShouldHandleGracefullyAndContinue() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            PrintStream originalOut = System.out;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));

            DatabaseConnection mockDbConnection = mock(DatabaseConnection.class);
            try {
                doThrow(new RuntimeException("Connection failed")).when(mockDbConnection).getPoolConnection();
            } catch (Exception e) {
                fail("Mock setup failed: " + e.getMessage());
            }
            mockedStatic.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);

            try {
                // Act
                List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                        SalesReportService.StoreType.ONLINE,
                        SalesReportService.TransactionType.ALL);

                // Assert
                assertNotNull(result);
                assertTrue(result.isEmpty());

                String output = baos.toString();
                assertTrue(output.contains("Error getting database connection"));
            } finally {
                System.setOut(originalOut);
            }
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should return empty list when in-store results are empty")
    void getSalesReport_WithEmptyInStoreResults_ShouldReturnEmptyList() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            when(resultSet.next()).thenReturn(false); // No results

            // Act
            List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                    SalesReportService.StoreType.IN_STORE,
                    SalesReportService.TransactionType.IN_STORE);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return empty list when online results are empty")
    void getSalesReport_WithEmptyOnlineResults_ShouldReturnEmptyList() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            when(resultSet.next()).thenReturn(false); // No results

            // Act
            List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                    SalesReportService.StoreType.ONLINE,
                    SalesReportService.TransactionType.ALL);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return all items when multiple in-store items exist")
    void getSalesReport_WithMultipleInStoreItems_ShouldReturnAllItems() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupMultipleInStoreResultSet();

            // Act
            List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                    SalesReportService.StoreType.IN_STORE,
                    SalesReportService.TransactionType.IN_STORE);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());

            // Verify both items are present
            assertTrue(result.stream().anyMatch(item -> ITEM_CODE_LAPTOP.equals(item.getCode())));
            assertTrue(result.stream().anyMatch(item -> ITEM_CODE_TABLET.equals(item.getCode())));
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return all items when multiple online items exist")
    void getSalesReport_WithMultipleOnlineItems_ShouldReturnAllItems() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupMultipleOnlineResultSet();

            // Act
            List<ReportItemDTO> result = service.getSalesReport(TEST_DATE,
                    SalesReportService.StoreType.ONLINE,
                    SalesReportService.TransactionType.ALL);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());

            // Verify both items are present
            assertTrue(result.stream().anyMatch(item -> ITEM_CODE_PHONE.equals(item.getCode())));
            assertTrue(result.stream().anyMatch(item -> ITEM_CODE_TABLET.equals(item.getCode())));
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should handle gracefully when null date is provided")
    void getSalesReport_WithNullDate_ShouldHandleGracefully() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);

            // Act & Assert
            assertDoesNotThrow(() -> {
                List<ReportItemDTO> result = service.getSalesReport(null,
                        SalesReportService.StoreType.IN_STORE,
                        SalesReportService.TransactionType.IN_STORE);
                assertNotNull(result);
            });
        } catch (Exception e) {
            // Expected to handle null date gracefully or throw appropriate exception
            assertTrue(e instanceof RuntimeException || e instanceof NullPointerException);
        }
    }

    @Test
    @DisplayName("Should return results when future date is provided")
    void getSalesReport_WithFutureDate_ShouldReturnResults() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            LocalDate futureDate = LocalDate.of(2025, 12, 31);
            setupDatabaseMocks(mockedStatic);
            when(resultSet.next()).thenReturn(false); // No results for future date

            // Act
            List<ReportItemDTO> result = service.getSalesReport(futureDate,
                    SalesReportService.StoreType.IN_STORE,
                    SalesReportService.TransactionType.IN_STORE);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(preparedStatement).setDate(1, Date.valueOf(futureDate));
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should return results when past date is provided")
    void getSalesReport_WithPastDate_ShouldReturnResults() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            LocalDate pastDate = LocalDate.of(2020, 1, 1);
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            // Act
            List<ReportItemDTO> result = service.getSalesReport(pastDate,
                    SalesReportService.StoreType.IN_STORE,
                    SalesReportService.TransactionType.IN_STORE);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(preparedStatement).setDate(1, Date.valueOf(pastDate));
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    }

    @Test
    @DisplayName("Should handle correctly all enum combinations of store and transaction types")
    void getSalesReport_WithAllEnumCombinations_ShouldHandleCorrectly() {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            // Arrange
            setupDatabaseMocks(mockedStatic);
            setupInStoreResultSet();

            // Act & Assert - Test all valid combinations
            for (SalesReportService.StoreType storeType : SalesReportService.StoreType.values()) {
                for (SalesReportService.TransactionType transactionType : SalesReportService.TransactionType.values()) {
                    assertDoesNotThrow(() -> {
                        List<ReportItemDTO> result = service.getSalesReport(TEST_DATE, storeType, transactionType);
                        assertNotNull(result);
                    }, "Failed for storeType: " + storeType + ", transactionType: " + transactionType);
                }
            }
        } catch (SQLException e) {
            fail("SQLException should not occur in this test");
        }
    } // Helper methods

    private void setupDatabaseMocks(MockedStatic<DatabaseConnection> mockedStatic) throws SQLException {
        DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
        mockedStatic.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void setupInStoreResultSet() throws SQLException {
        when(resultSet.next()).thenReturn(true, false); // One record, then end
        when(resultSet.getString("item_code")).thenReturn(ITEM_CODE_LAPTOP);
        when(resultSet.getString("item_name")).thenReturn(ITEM_NAME_LAPTOP);
        when(resultSet.getInt("total_qty")).thenReturn(QUANTITY_SOLD_10);
        when(resultSet.getDouble("total_revenue")).thenReturn(REVENUE_1000);
    }

    private void setupOnlineResultSet() throws SQLException {
        when(resultSet.next()).thenReturn(true, false); // One record, then end
        when(resultSet.getString("item_code")).thenReturn(ITEM_CODE_PHONE);
        when(resultSet.getString("name")).thenReturn(ITEM_NAME_PHONE);
        when(resultSet.getInt("total_qty")).thenReturn(QUANTITY_SOLD_5);
        when(resultSet.getDouble("total_revenue")).thenReturn(REVENUE_500);
    }

    private void setupCombinedResultSet() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false); // Two records, then end
        when(resultSet.getString("item_code"))
                .thenReturn(ITEM_CODE_LAPTOP)
                .thenReturn(ITEM_CODE_PHONE);
        when(resultSet.getString("item_name"))
                .thenReturn(ITEM_NAME_LAPTOP)
                .thenReturn(null); // Will use "name" for online
        when(resultSet.getString("name"))
                .thenReturn(null) // Will use "item_name" for in-store
                .thenReturn(ITEM_NAME_PHONE);
        when(resultSet.getInt("total_qty"))
                .thenReturn(QUANTITY_SOLD_10)
                .thenReturn(QUANTITY_SOLD_5);
        when(resultSet.getDouble("total_revenue"))
                .thenReturn(REVENUE_1000)
                .thenReturn(REVENUE_500);
    }

    private void setupMultipleInStoreResultSet() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false); // Two records, then end
        when(resultSet.getString("item_code"))
                .thenReturn(ITEM_CODE_LAPTOP)
                .thenReturn(ITEM_CODE_TABLET);
        when(resultSet.getString("item_name"))
                .thenReturn(ITEM_NAME_LAPTOP)
                .thenReturn(ITEM_NAME_TABLET);
        when(resultSet.getInt("total_qty"))
                .thenReturn(QUANTITY_SOLD_10)
                .thenReturn(QUANTITY_SOLD_15);
        when(resultSet.getDouble("total_revenue"))
                .thenReturn(REVENUE_1000)
                .thenReturn(REVENUE_1500);
    }

    private void setupMultipleOnlineResultSet() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false); // Two records, then end
        when(resultSet.getString("item_code"))
                .thenReturn(ITEM_CODE_PHONE)
                .thenReturn(ITEM_CODE_TABLET);
        when(resultSet.getString("name"))
                .thenReturn(ITEM_NAME_PHONE)
                .thenReturn(ITEM_NAME_TABLET);
        when(resultSet.getInt("total_qty"))
                .thenReturn(QUANTITY_SOLD_5)
                .thenReturn(QUANTITY_SOLD_15);
        when(resultSet.getDouble("total_revenue"))
                .thenReturn(REVENUE_500)
                .thenReturn(REVENUE_1500);
    }

    private void setupDuplicateItemsResultSet() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false); // Two records, then end
        when(resultSet.getString("item_code"))
                .thenReturn(ITEM_CODE_LAPTOP)
                .thenReturn(ITEM_CODE_LAPTOP); // Same item code
        when(resultSet.getString("item_name"))
                .thenReturn(ITEM_NAME_LAPTOP)
                .thenReturn(null);
        when(resultSet.getString("name"))
                .thenReturn(null)
                .thenReturn(ITEM_NAME_LAPTOP);
        when(resultSet.getInt("total_qty"))
                .thenReturn(QUANTITY_SOLD_10)
                .thenReturn(QUANTITY_SOLD_5);
        when(resultSet.getDouble("total_revenue"))
                .thenReturn(REVENUE_1000)
                .thenReturn(REVENUE_500);
    }
}