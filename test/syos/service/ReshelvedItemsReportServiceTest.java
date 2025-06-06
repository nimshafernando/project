package syos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import syos.dto.ReshelvedItemDTO;
import syos.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("ReshelvedItemsReportService Tests")
class ReshelvedItemsReportServiceTest {

    @Mock
    private DatabaseConnection databaseConnection;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private ReshelvedItemsReportService reportService;

    // Test data constants
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 12, 15);
    private static final LocalDate START_DATE = LocalDate.of(2024, 12, 1);
    private static final LocalDate END_DATE = LocalDate.of(2024, 12, 31);

    private static final String ITEM_CODE_1 = "ITM001";
    private static final String ITEM_CODE_2 = "ITM002";
    private static final String ITEM_CODE_3 = "ITM003";

    private static final String ITEM_NAME_1 = "Premium Coffee Beans";
    private static final String ITEM_NAME_2 = "Organic Green Tea";
    private static final String ITEM_NAME_3 = "This is a very long item name that exceeds thirty characters and should be truncated";

    private static final int QUANTITY_1 = 150;
    private static final int QUANTITY_2 = 75;
    private static final int QUANTITY_3 = 200;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reportService = new ReshelvedItemsReportService();
    }

    @Nested
    @DisplayName("In-Store Reshelved Items Tests")
    class InStoreTests {

        @Test
        @DisplayName("Should return reshelved items for in-store on specific date")
        void shouldReturnReshelvedItemsForInStoreOnDate() throws SQLException {
            // Arrange
            setupMockForInStoreQuery();
            setupResultSetWithMultipleItems();

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act
                List<ReshelvedItemDTO> result = reportService.getReshelvedItemsForInStore(TEST_DATE);

                // Assert
                assertNotNull(result);
                assertEquals(2, result.size());

                ReshelvedItemDTO firstItem = result.get(0);
                assertEquals(ITEM_CODE_1, firstItem.getCode());
                assertEquals(ITEM_NAME_1, firstItem.getName());
                assertEquals(QUANTITY_1, firstItem.getQuantity());

                ReshelvedItemDTO secondItem = result.get(1);
                assertEquals(ITEM_CODE_2, secondItem.getCode());
                assertEquals(ITEM_NAME_2, secondItem.getName());
                assertEquals(QUANTITY_2, secondItem.getQuantity());

                verify(preparedStatement).setDate(1, Date.valueOf(TEST_DATE));
            }
        }

        @Test
        @DisplayName("Should return reshelved items for in-store within date range")
        void shouldReturnReshelvedItemsForInStoreDateRange() throws SQLException {
            // Arrange
            setupMockForInStoreQuery();
            setupResultSetWithSingleItem();

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act
                List<ReshelvedItemDTO> result = reportService.getReshelvedItemsForInStore(START_DATE, END_DATE);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(ITEM_CODE_1, result.get(0).getCode());
                assertEquals(ITEM_NAME_1, result.get(0).getName());
                assertEquals(QUANTITY_1, result.get(0).getQuantity());

                verify(preparedStatement).setDate(1, Date.valueOf(START_DATE));
                verify(preparedStatement).setDate(2, Date.valueOf(END_DATE));
            }
        }

        @Test
        @DisplayName("Should return empty list when no in-store items found")
        void shouldReturnEmptyListWhenNoInStoreItemsFound() throws SQLException {
            // Arrange
            setupMockForInStoreQuery();
            when(resultSet.next()).thenReturn(false);

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act
                List<ReshelvedItemDTO> result = reportService.getReshelvedItemsForInStore(TEST_DATE);

                // Assert
                assertNotNull(result);
                assertTrue(result.isEmpty());
            }
        }

        @Test
        @DisplayName("Should call range method correctly")
        void shouldCallRangeMethodCorrectly() throws SQLException {
            // Arrange
            setupMockForInStoreQuery();
            setupResultSetWithSingleItem();

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act
                List<ReshelvedItemDTO> result = reportService.getReshelvedItemsForInStoreRange(START_DATE, END_DATE);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                verify(preparedStatement).setDate(1, Date.valueOf(START_DATE));
                verify(preparedStatement).setDate(2, Date.valueOf(END_DATE));
            }
        }
    }

    @Nested
    @DisplayName("Online Reshelved Items Tests")
    class OnlineTests {

        @Test
        @DisplayName("Should return reshelved items for online on specific date")
        void shouldReturnReshelvedItemsForOnlineOnDate() throws SQLException {
            // Arrange
            setupMockForOnlineQuery();
            setupResultSetWithMultipleItems();

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act
                List<ReshelvedItemDTO> result = reportService.getReshelvedItemsForOnline(TEST_DATE);

                // Assert
                assertNotNull(result);
                assertEquals(2, result.size());
                assertEquals(ITEM_CODE_1, result.get(0).getCode());
                assertEquals(ITEM_CODE_2, result.get(1).getCode());

                verify(preparedStatement).setDate(1, Date.valueOf(TEST_DATE));
            }
        }

        @Test
        @DisplayName("Should return reshelved items for online within date range")
        void shouldReturnReshelvedItemsForOnlineDateRange() throws SQLException {
            // Arrange
            setupMockForOnlineQuery();
            setupResultSetWithSingleItem();

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act
                List<ReshelvedItemDTO> result = reportService.getReshelvedItemsForOnline(START_DATE, END_DATE);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                verify(preparedStatement).setDate(1, Date.valueOf(START_DATE));
                verify(preparedStatement).setDate(2, Date.valueOf(END_DATE));
            }
        }

        @Test
        @DisplayName("Should call range method correctly")
        void shouldCallOnlineRangeMethodCorrectly() throws SQLException {
            // Arrange
            setupMockForOnlineQuery();
            setupResultSetWithSingleItem();

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act
                List<ReshelvedItemDTO> result = reportService.getReshelvedItemsForOnlineRange(START_DATE, END_DATE);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                verify(preparedStatement).setDate(1, Date.valueOf(START_DATE));
                verify(preparedStatement).setDate(2, Date.valueOf(END_DATE));
            }
        }
    }

    @Nested
    @DisplayName("All Reshelved Items Tests")
    class AllItemsTests {

        @Test
        @DisplayName("Should return all reshelved items with store type labels")
        void shouldReturnAllReshelvedItemsWithStoreTypeLabels() throws SQLException {
            // Arrange
            setupMockForAllItemsQuery();
            setupResultSetWithMixedStoreTypes();

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act
                List<ReshelvedItemDTO> result = reportService.getAllReshelvedItems(TEST_DATE);

                // Assert
                assertNotNull(result);
                assertEquals(2, result.size());
                assertEquals(ITEM_NAME_1 + " (In-Store)", result.get(0).getName());
                assertEquals(ITEM_NAME_2 + " (Online)", result.get(1).getName());

                verify(preparedStatement).setDate(1, Date.valueOf(TEST_DATE));
            }
        }

        @Test
        @DisplayName("Should return all reshelved items within date range")
        void shouldReturnAllReshelvedItemsWithinDateRange() throws SQLException {
            // Arrange
            setupMockForAllItemsQuery();
            setupResultSetWithMixedStoreTypes();

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act
                List<ReshelvedItemDTO> result = reportService.getAllReshelvedItemsRange(START_DATE, END_DATE);

                // Assert
                assertNotNull(result);
                assertEquals(2, result.size());
                verify(preparedStatement).setDate(1, Date.valueOf(START_DATE));
                verify(preparedStatement).setDate(2, Date.valueOf(END_DATE));
            }
        }
    }

    @Nested
    @DisplayName("IReportService Interface Implementation Tests")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("Should generate report with current date")
        void shouldGenerateReportWithCurrentDate() throws SQLException {
            // Arrange
            setupMockForInStoreQuery();
            setupResultSetWithSingleItem();

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act
                List<ReshelvedItemDTO> result = reportService.generateReport();

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(ITEM_CODE_1, result.get(0).getCode());
            }
        }

        @Test
        @DisplayName("Should generate report with provided date filter")
        void shouldGenerateReportWithProvidedDateFilter() throws SQLException {
            // Arrange
            setupMockForInStoreQuery();
            setupResultSetWithSingleItem();

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act
                List<ReshelvedItemDTO> result = reportService.generateReport(TEST_DATE.toString());

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
            }
        }

        @Test
        @DisplayName("Should generate report with non-date filter")
        void shouldGenerateReportWithNonDateFilter() throws SQLException {
            // Arrange
            setupMockForInStoreQuery();
            setupResultSetWithSingleItem();

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act
                List<ReshelvedItemDTO> result = reportService.generateReport("invalid_filter");

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
            }
        }

        @Test
        @DisplayName("Should return correct report name")
        void shouldReturnCorrectReportName() {
            // Act & Assert
            assertEquals("Reshelved Items Report", reportService.getReportName());
        }

        @Test
        @DisplayName("Should return correct report title")
        void shouldReturnCorrectReportTitle() {
            // Act & Assert
            assertEquals("SYOS RESHELVED ITEMS REPORT", reportService.getReportTitle());
        }

        @Test
        @DisplayName("Should return correct column headers")
        void shouldReturnCorrectColumnHeaders() {
            // Act
            List<String> headers = reportService.getColumnHeaders();

            // Assert
            assertNotNull(headers);
            assertEquals(3, headers.size());
            assertEquals("Item Code", headers.get(0));
            assertEquals("Item Name", headers.get(1));
            assertEquals("Quantity Reshelved", headers.get(2));
        }

        @Test
        @DisplayName("Should check data availability when data exists")
        void shouldCheckDataAvailabilityWhenDataExists() throws SQLException {
            // Arrange
            setupMockForInStoreQuery();
            setupResultSetWithSingleItem();

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act & Assert
                assertTrue(reportService.isDataAvailable());
            }
        }

        @Test
        @DisplayName("Should check data availability when no data exists")
        void shouldCheckDataAvailabilityWhenNoDataExists() throws SQLException {
            // Arrange
            setupMockForInStoreQuery();
            when(resultSet.next()).thenReturn(false);

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act & Assert
                assertFalse(reportService.isDataAvailable());
            }
        }

        @Test
        @DisplayName("Should return report data in correct format")
        void shouldReturnReportDataInCorrectFormat() throws SQLException {
            // Arrange
            setupMockForAllItemsQuery();
            setupResultSetWithMixedStoreTypes();

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act
                List<List<String>> reportData = reportService.getReportData();

                // Assert
                assertNotNull(reportData);
                assertEquals(2, reportData.size());

                List<String> firstRow = reportData.get(0);
                assertEquals(3, firstRow.size());
                assertEquals(ITEM_CODE_1, firstRow.get(0));
                assertEquals(ITEM_NAME_1, firstRow.get(1));
                assertEquals(String.valueOf(QUANTITY_1), firstRow.get(2));

                List<String> secondRow = reportData.get(1);
                assertEquals(3, secondRow.size());
                assertEquals(ITEM_CODE_2, secondRow.get(0));
                assertEquals(ITEM_NAME_2, secondRow.get(1));
                assertEquals(String.valueOf(QUANTITY_2), secondRow.get(2));
            }
        }

        @Test
        @DisplayName("Should format row correctly")
        void shouldFormatRowCorrectly() {
            // Arrange
            ReshelvedItemDTO item = new ReshelvedItemDTO(ITEM_CODE_1, ITEM_NAME_1, QUANTITY_1);

            // Act
            String formattedRow = reportService.getFormattedRow(item);

            // Assert
            assertNotNull(formattedRow);
            assertTrue(formattedRow.contains(ITEM_CODE_1));
            assertTrue(formattedRow.contains(ITEM_NAME_1));
            assertTrue(formattedRow.contains(String.valueOf(QUANTITY_1)));
        }

        @Test
        @DisplayName("Should format row with truncated long name")
        void shouldFormatRowWithTruncatedLongName() {
            // Arrange
            ReshelvedItemDTO item = new ReshelvedItemDTO(ITEM_CODE_3, ITEM_NAME_3, QUANTITY_3);

            // Act
            String formattedRow = reportService.getFormattedRow(item);

            // Assert
            assertNotNull(formattedRow);
            assertTrue(formattedRow.contains(ITEM_CODE_3));
            assertTrue(formattedRow.contains("..."));
            assertTrue(formattedRow.contains(String.valueOf(QUANTITY_3)));
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should throw RuntimeException when SQLException occurs in in-store query")
        void shouldThrowRuntimeExceptionWhenSQLExceptionInInStoreQuery() throws SQLException {
            // Arrange
            setupMockDatabaseConnection();
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act & Assert
                RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> reportService.getReshelvedItemsForInStore(TEST_DATE));
                assertTrue(exception.getMessage().contains("Error fetching reshelved items from log"));
            }
        }

        @Test
        @DisplayName("Should throw RuntimeException when SQLException occurs in online query")
        void shouldThrowRuntimeExceptionWhenSQLExceptionInOnlineQuery() throws SQLException {
            // Arrange
            setupMockDatabaseConnection();
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act & Assert
                RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> reportService.getReshelvedItemsForOnline(TEST_DATE));
                assertTrue(exception.getMessage().contains("Error fetching online reshelved items from log"));
            }
        }

        @Test
        @DisplayName("Should throw RuntimeException when SQLException occurs in all items query")
        void shouldThrowRuntimeExceptionWhenSQLExceptionInAllItemsQuery() throws SQLException {
            // Arrange
            setupMockDatabaseConnection();
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);

                // Act & Assert
                RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> reportService.getAllReshelvedItems(TEST_DATE));
                assertTrue(exception.getMessage().contains("Error fetching all reshelved items from log"));
            }
        }

        @Test
        @DisplayName("Should throw RuntimeException when general Exception occurs")
        void shouldThrowRuntimeExceptionWhenGeneralExceptionOccurs() throws SQLException {
            // Arrange
            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                mockedStatic.when(DatabaseConnection::getInstance).thenThrow(new RuntimeException("Connection failed"));

                // Act & Assert
                RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> reportService.getReshelvedItemsForInStore(TEST_DATE));
                assertTrue(exception.getMessage().contains("Connection failed"));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideTruncationTestCases")
    @DisplayName("Should handle name truncation correctly")
    void shouldHandleNameTruncationCorrectly(String inputName, String expectedOutput) {
        // Arrange
        ReshelvedItemDTO item = new ReshelvedItemDTO("TEST001", inputName, 100);

        // Act
        String formattedRow = reportService.getFormattedRow(item);

        // Assert
        assertNotNull(formattedRow);
        if (expectedOutput.contains("...")) {
            assertTrue(formattedRow.contains("..."));
        } else {
            assertTrue(formattedRow.contains(inputName));
        }
    }

    private static Stream<Arguments> provideTruncationTestCases() {
        return Stream.of(
                Arguments.of("Short Name", "Short Name"),
                Arguments.of("Exactly Thirty Characters Here", "Exactly Thirty Characters Here"),
                Arguments.of("This name is definitely longer than thirty characters and should be truncated",
                        "truncated..."));
    }

    // Helper methods for setting up mocks
    private void setupMockForInStoreQuery() throws SQLException {
        setupMockDatabaseConnection();
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void setupMockForOnlineQuery() throws SQLException {
        setupMockDatabaseConnection();
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void setupMockForAllItemsQuery() throws SQLException {
        setupMockDatabaseConnection();
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void setupMockDatabaseConnection() throws SQLException {
        try {
            doReturn(connection).when(databaseConnection).getPoolConnection();
        } catch (Exception e) {
            fail("Mock setup failed: " + e.getMessage());
        }
    }

    private void setupResultSetWithSingleItem() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("item_code")).thenReturn(ITEM_CODE_1);
        when(resultSet.getString("name")).thenReturn(ITEM_NAME_1);
        when(resultSet.getInt("qty")).thenReturn(QUANTITY_1);
    }

    private void setupResultSetWithMultipleItems() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("item_code")).thenReturn(ITEM_CODE_1, ITEM_CODE_2);
        when(resultSet.getString("name")).thenReturn(ITEM_NAME_1, ITEM_NAME_2);
        when(resultSet.getInt("qty")).thenReturn(QUANTITY_1, QUANTITY_2);
    }

    private void setupResultSetWithMixedStoreTypes() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("item_code")).thenReturn(ITEM_CODE_1, ITEM_CODE_2);
        when(resultSet.getString("name")).thenReturn(ITEM_NAME_1, ITEM_NAME_2);
        when(resultSet.getInt("qty")).thenReturn(QUANTITY_1, QUANTITY_2);
        when(resultSet.getString("store_type")).thenReturn("INSTORE", "ONLINE");
    }
}