package syos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import syos.dto.BillHistoryDTO;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

class BillHistoryReportServiceTest {

    private BillHistoryReportService reportService;
    private Connection mockConnection;
    private PreparedStatement mockInStoreStmt;
    private PreparedStatement mockOnlineStmt;
    private PreparedStatement mockDataAvailStmt;
    private ResultSet mockInStoreRs;
    private ResultSet mockOnlineRs;
    private ResultSet mockDataAvailRs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reportService = new BillHistoryReportService();
        mockConnection = mock(Connection.class);
        mockInStoreStmt = mock(PreparedStatement.class);
        mockOnlineStmt = mock(PreparedStatement.class);
        mockDataAvailStmt = mock(PreparedStatement.class);
        mockInStoreRs = mock(ResultSet.class);
        mockOnlineRs = mock(ResultSet.class);
        mockDataAvailRs = mock(ResultSet.class);
        try {
            when(mockConnection.prepareStatement(startsWith("SELECT id, date, time, total, employee_name")))
                    .thenReturn(mockInStoreStmt);
            when(mockConnection.prepareStatement(startsWith("SELECT id, date, time, total, payment_method")))
                    .thenReturn(mockOnlineStmt);
            when(mockConnection.prepareStatement(startsWith("SELECT 1 FROM bills UNION")))
                    .thenReturn(mockDataAvailStmt);
            when(mockInStoreStmt.executeQuery()).thenReturn(mockInStoreRs);
            when(mockOnlineStmt.executeQuery()).thenReturn(mockOnlineRs);
            when(mockDataAvailStmt.executeQuery()).thenReturn(mockDataAvailRs);
        } catch (SQLException e) {
            fail("Mock setup failed: " + e.getMessage());
        }
    }

    private DatabaseConnection setupMockDatabaseConnection() {
        DatabaseConnection mockDbConnection = mock(DatabaseConnection.class);
        try {
            when(mockDbConnection.getPoolConnection()).thenReturn(mockConnection);
        } catch (Exception e) {
            fail("Mock setup failed: " + e.getMessage());
        }
        return mockDbConnection;
    }

    @Test
    @DisplayName("Should generate default report")
    void testGenerateReport() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            setupMockResultSetForAllStores();
            List<BillHistoryDTO> result = reportService.generateReport();
            assertNotNull(result);
            assertEquals(4, result.size());
            verify(mockInStoreStmt, atLeastOnce()).executeQuery();
            verify(mockOnlineStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should generate report with filters")
    void testGenerateReportWithFilters() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            setupMockResultSetForToday();
            List<BillHistoryDTO> result = reportService.generateReport(
                    BillHistoryReportService.StoreFilter.IN_STORE,
                    BillHistoryReportService.DateFilter.TODAY,
                    LocalDate.now(),
                    LocalDate.now());
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(mockInStoreStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should return correct report name")
    void testGetReportName() {
        assertEquals("Bill History Report", reportService.getReportName());
    }

    @Test
    @DisplayName("Should return correct report title")
    void testGetReportTitle() {
        assertEquals("Bill History Report - Transaction Overview", reportService.getReportTitle());
    }

    @Test
    @DisplayName("Should return correct column headers")
    void testGetColumnHeaders() {
        List<String> headers = reportService.getColumnHeaders();
        assertNotNull(headers);
        assertEquals(9, headers.size());
        assertTrue(headers.contains("Bill ID"));
        assertTrue(headers.contains("Serial Number"));
        assertTrue(headers.contains("Date & Time"));
        assertTrue(headers.contains("Total Amount"));
        assertTrue(headers.contains("Store Type"));
    }

    @Test
    @DisplayName("Should get bill history for in-store only")
    void testGetBillHistoryInStore() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            setupMockResultSetForInStore();
            List<BillHistoryDTO> result = reportService.getBillHistory(BillHistoryReportService.StoreFilter.IN_STORE);
            assertNotNull(result);
            assertEquals(2, result.size());
            BillHistoryDTO bill1 = result.get(0);
            assertEquals(1, bill1.getBillId());
            assertEquals("John Manager", bill1.getEmployeeName());
            assertEquals("IN_STORE", bill1.getStoreType());
            BillHistoryDTO bill2 = result.get(1);
            assertEquals(2, bill2.getBillId());
            assertEquals("Sarah Cashier", bill2.getEmployeeName());
            assertEquals("IN_STORE", bill2.getStoreType());
            verify(mockInStoreStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should get bill history for online only")
    void testGetBillHistoryOnline() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            setupMockResultSetForOnline();
            List<BillHistoryDTO> result = reportService.getBillHistory(BillHistoryReportService.StoreFilter.ONLINE);
            assertNotNull(result);
            assertEquals(2, result.size());
            BillHistoryDTO bill1 = result.get(0);
            assertEquals(101, bill1.getBillId());
            assertEquals("customer123", bill1.getCustomerInfo());
            assertEquals("ONLINE", bill1.getStoreType());
            assertEquals("Cash on Delivery", bill1.getPaymentMethod());
            BillHistoryDTO bill2 = result.get(1);
            assertEquals(102, bill2.getBillId());
            assertEquals("shopper456", bill2.getCustomerInfo());
            assertEquals("ONLINE", bill2.getStoreType());
            assertEquals("Pay in Store", bill2.getPaymentMethod());
            verify(mockOnlineStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should get bill history for all stores")
    void testGetBillHistoryAllStores() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            setupMockResultSetForAllStores();
            List<BillHistoryDTO> result = reportService.getBillHistory(BillHistoryReportService.StoreFilter.ALL);
            assertNotNull(result);
            assertEquals(4, result.size());
            boolean hasInStore = result.stream().anyMatch(bill -> "IN_STORE".equals(bill.getStoreType()));
            boolean hasOnline = result.stream().anyMatch(bill -> "ONLINE".equals(bill.getStoreType()));
            assertTrue(hasInStore, "No in-store bills found");
            assertTrue(hasOnline, "No online bills found");
            verify(mockInStoreStmt, atLeastOnce()).executeQuery();
            verify(mockOnlineStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should filter bills by today's date")
    void testGetBillHistoryToday() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            setupMockResultSetForToday();
            List<BillHistoryDTO> result = reportService.getBillHistory(BillHistoryReportService.StoreFilter.ALL,
                    BillHistoryReportService.DateFilter.TODAY);
            assertNotNull(result);
            assertEquals(1, result.size());
            BillHistoryDTO todayBill = result.get(0);
            assertEquals(LocalDate.now(), todayBill.getDateTime().toLocalDate());
            verify(mockInStoreStmt, atLeastOnce()).executeQuery();
            verify(mockOnlineStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should filter bills by this week")
    void testGetBillHistoryThisWeek() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            setupMockResultSetForWeek();
            List<BillHistoryDTO> result = reportService.getBillHistory(BillHistoryReportService.StoreFilter.ALL,
                    BillHistoryReportService.DateFilter.THIS_WEEK);
            assertNotNull(result);
            assertEquals(3, result.size());
            LocalDate startOfWeek = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue());
            for (BillHistoryDTO bill : result) {
                LocalDate billDate = bill.getDateTime().toLocalDate();
                assertTrue(billDate.isEqual(startOfWeek) || billDate.isAfter(startOfWeek),
                        "Bill date is before start of week: " + billDate);
                assertTrue(billDate.isEqual(LocalDate.now()) || billDate.isBefore(LocalDate.now().plusDays(1)),
                        "Bill date is after today: " + billDate);
            }
            verify(mockInStoreStmt, atLeastOnce()).executeQuery();
            verify(mockOnlineStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should filter bills by this month")
    void testGetBillHistoryThisMonth() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            setupMockResultSetForMonth();
            List<BillHistoryDTO> result = reportService.getBillHistory(BillHistoryReportService.StoreFilter.ALL,
                    BillHistoryReportService.DateFilter.THIS_MONTH);
            assertNotNull(result);
            assertEquals(5, result.size());
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            for (BillHistoryDTO bill : result) {
                LocalDate billDate = bill.getDateTime().toLocalDate();
                assertTrue(!billDate.isBefore(startOfMonth),
                        "Bill date is before start of month: " + billDate);
                assertEquals(LocalDate.now().getMonth(), billDate.getMonth(),
                        "Bill month does not match current month: " + billDate);
            }
            verify(mockInStoreStmt, atLeastOnce()).executeQuery();
            verify(mockOnlineStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should get all time bill history")
    void testGetBillHistoryAllTime() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            setupMockResultSetForAllTime();
            List<BillHistoryDTO> result = reportService.getBillHistory(BillHistoryReportService.StoreFilter.ALL,
                    BillHistoryReportService.DateFilter.ALL_TIME);
            assertNotNull(result);
            assertEquals(10, result.size());
            verify(mockInStoreStmt, atLeastOnce()).executeQuery();
            verify(mockOnlineStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should filter bills by custom date range")
    void testGetBillHistoryCustomRange() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            setupMockResultSetForCustomRange();
            LocalDate startDate = LocalDate.of(2025, 6, 1);
            LocalDate endDate = LocalDate.of(2025, 6, 3);
            List<BillHistoryDTO> result = reportService.getBillHistory(
                    BillHistoryReportService.StoreFilter.ALL,
                    BillHistoryReportService.DateFilter.CUSTOM_RANGE,
                    startDate,
                    endDate);
            assertNotNull(result);
            assertEquals(2, result.size());
            for (BillHistoryDTO bill : result) {
                LocalDate billDate = bill.getDateTime().toLocalDate();
                assertTrue(billDate.isEqual(startDate) || billDate.isAfter(startDate));
                assertTrue(billDate.isEqual(endDate) || billDate.isBefore(endDate.plusDays(1)));
            }
            verify(mockInStoreStmt, atLeastOnce()).executeQuery();
            verify(mockOnlineStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should filter online bills by cash on delivery")
    void testGetBillHistoryCashOnDelivery() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            setupMockResultSetForCashOnDelivery();
            List<BillHistoryDTO> result = reportService.getBillHistory(
                    BillHistoryReportService.StoreFilter.ONLINE,
                    BillHistoryReportService.DateFilter.ALL_TIME,
                    BillHistoryReportService.PaymentMethodFilter.CASH_ON_DELIVERY);
            assertNotNull(result);
            assertEquals(1, result.size());
            BillHistoryDTO bill = result.get(0);
            assertEquals("ONLINE", bill.getStoreType());
            assertEquals("Cash on Delivery", bill.getPaymentMethod());
            assertEquals(100.0, bill.getTotalAmount(), 0.01);
            verify(mockOnlineStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should filter online bills by pay in store")
    void testGetBillHistoryPayInStore() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            setupMockResultSetForPayInStore();
            List<BillHistoryDTO> result = reportService.getBillHistory(
                    BillHistoryReportService.StoreFilter.ONLINE,
                    BillHistoryReportService.DateFilter.ALL_TIME,
                    BillHistoryReportService.PaymentMethodFilter.PAY_IN_STORE);
            assertNotNull(result);
            assertEquals(1, result.size());
            BillHistoryDTO bill = result.get(0);
            assertEquals("ONLINE", bill.getStoreType());
            assertEquals("Pay in Store", bill.getPaymentMethod());
            assertEquals(50.0, bill.getTotalAmount(), 0.01);
            verify(mockOnlineStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should handle empty result set")
    void testEmptyResultSet() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            when(mockInStoreRs.next()).thenReturn(false);
            when(mockOnlineRs.next()).thenReturn(false);
            List<BillHistoryDTO> result = reportService.getBillHistory(BillHistoryReportService.StoreFilter.ALL);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(mockInStoreStmt, atLeastOnce()).executeQuery();
            verify(mockOnlineStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should handle database connection errors")
    void testDatabaseConnectionError() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = mock(DatabaseConnection.class);
            when(mockDbConnection.getPoolConnection()).thenThrow(new Exception("Connection failed"));
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            List<BillHistoryDTO> result = reportService.getBillHistory(BillHistoryReportService.StoreFilter.ALL);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        } catch (Exception e) {
            fail("Exception should not occur in test setup: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should check data availability")
    void testIsDataAvailable() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            when(mockDataAvailRs.next()).thenReturn(true);
            boolean hasData = reportService.isDataAvailable();
            assertTrue(hasData);
            verify(mockDataAvailStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should get report data in list format")
    void testGetReportData() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            setupMockResultSetForReportData();
            List<List<String>> reportData = reportService.getReportData();
            assertNotNull(reportData);
            assertFalse(reportData.isEmpty());
            List<String> firstRow = reportData.get(0);
            assertEquals(9, firstRow.size());
            verify(mockInStoreStmt, atLeastOnce()).executeQuery();
            verify(mockOnlineStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should format row correctly")
    void testGetFormattedRow() {
        BillHistoryDTO bill = new BillHistoryDTO(
                1, "BILL-001", LocalDateTime.now(), 150.0, 0.0,
                "IN_STORE", "CASH", null, "John Manager", 0);
        String formattedRow = reportService.getFormattedRow(bill);
        assertNotNull(formattedRow);
        assertTrue(formattedRow.contains("Bill ID: 1"));
        assertTrue(formattedRow.contains("Serial: BILL-001"));
        assertTrue(formattedRow.contains("Total: $150.00"));
        assertTrue(formattedRow.contains("Type: IN_STORE"));
        assertTrue(formattedRow.contains("Payment: CASH"));
        assertTrue(formattedRow.contains("Customer: N/A"));
    }

    @Test
    @DisplayName("Should handle null date in result set")
    void testHandleNullDate() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            DatabaseConnection mockDbConnection = setupMockDatabaseConnection();
            dbMock.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
            when(mockInStoreRs.next()).thenReturn(true).thenReturn(false);
            when(mockInStoreRs.getInt("id")).thenReturn(1);
            when(mockInStoreRs.getDate("date")).thenReturn(null);
            when(mockInStoreRs.getTime("time")).thenReturn(Time.valueOf(LocalTime.now()));
            when(mockInStoreRs.getDouble("total")).thenReturn(100.0);
            when(mockInStoreRs.getString("employee_name")).thenReturn("Manager");
            when(mockOnlineRs.next()).thenReturn(false);
            List<BillHistoryDTO> result = reportService.getBillHistory(BillHistoryReportService.StoreFilter.ALL);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(mockInStoreStmt, atLeastOnce()).executeQuery();
            verify(mockOnlineStmt, atLeastOnce()).executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not occur in test: " + e.getMessage());
        }
    }

    private void setupMockResultSetForInStore() throws SQLException {
        when(mockInStoreRs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockInStoreRs.getInt("id")).thenReturn(1).thenReturn(2);
        when(mockInStoreRs.getString("serial_number")).thenReturn(null).thenReturn(null);
        when(mockInStoreRs.getDate("date")).thenReturn(Date.valueOf(LocalDate.now()))
                .thenReturn(Date.valueOf(LocalDate.now().minusDays(1)));
        when(mockInStoreRs.getTime("time")).thenReturn(Time.valueOf(LocalTime.now()))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(1)));
        when(mockInStoreRs.getDouble("total")).thenReturn(100.0).thenReturn(80.0);
        when(mockInStoreRs.getString("employee_name")).thenReturn("John Manager").thenReturn("Sarah Cashier");
    }

    private void setupMockResultSetForOnline() throws SQLException {
        when(mockOnlineRs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockOnlineRs.getInt("id")).thenReturn(101).thenReturn(102);
        when(mockOnlineRs.getString("serial_number")).thenReturn("ONL-001").thenReturn("ONL-002");
        when(mockOnlineRs.getDate("date")).thenReturn(Date.valueOf(LocalDate.now()))
                .thenReturn(Date.valueOf(LocalDate.now().minusDays(1)));
        when(mockOnlineRs.getTime("time")).thenReturn(Time.valueOf(LocalTime.now()))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(2)));
        when(mockOnlineRs.getDouble("total")).thenReturn(120.0).thenReturn(85.0);
        when(mockOnlineRs.getString("payment_method")).thenReturn("Cash on Delivery").thenReturn("Pay in Store");
        when(mockOnlineRs.getString("username")).thenReturn("customer123").thenReturn("shopper456");
    }

    private void setupMockResultSetForAllStores() throws SQLException {
        // In-store bills
        when(mockInStoreRs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockInStoreRs.getInt("id")).thenReturn(1).thenReturn(2);
        when(mockInStoreRs.getString("serial_number")).thenReturn(null).thenReturn(null);
        when(mockInStoreRs.getDate("date")).thenReturn(Date.valueOf(LocalDate.now()))
                .thenReturn(Date.valueOf(LocalDate.now().minusDays(1)));
        when(mockInStoreRs.getTime("time")).thenReturn(Time.valueOf(LocalTime.now()))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(1)));
        when(mockInStoreRs.getDouble("total")).thenReturn(100.0).thenReturn(80.0);
        when(mockInStoreRs.getString("employee_name")).thenReturn("John Manager").thenReturn("Sarah Cashier");

        // Online bills
        when(mockOnlineRs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockOnlineRs.getInt("id")).thenReturn(101).thenReturn(102);
        when(mockOnlineRs.getString("serial_number")).thenReturn("ONL-001").thenReturn("ONL-002");
        when(mockOnlineRs.getDate("date")).thenReturn(Date.valueOf(LocalDate.now()))
                .thenReturn(Date.valueOf(LocalDate.now().minusDays(1)));
        when(mockOnlineRs.getTime("time")).thenReturn(Time.valueOf(LocalTime.now()))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(2)));
        when(mockOnlineRs.getDouble("total")).thenReturn(120.0).thenReturn(85.0);
        when(mockOnlineRs.getString("payment_method")).thenReturn("Cash on Delivery").thenReturn("Pay in Store");
        when(mockOnlineRs.getString("username")).thenReturn("customer123").thenReturn("shopper456");
    }

    private void setupMockResultSetForToday() throws SQLException {
        when(mockInStoreRs.next()).thenReturn(true).thenReturn(false);
        when(mockInStoreRs.getInt("id")).thenReturn(1);
        when(mockInStoreRs.getString("serial_number")).thenReturn(null);
        when(mockInStoreRs.getDate("date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockInStoreRs.getTime("time")).thenReturn(Time.valueOf(LocalTime.now()));
        when(mockInStoreRs.getDouble("total")).thenReturn(150.0);
        when(mockInStoreRs.getString("employee_name")).thenReturn("Today Manager");
        when(mockOnlineRs.next()).thenReturn(false);
    }

    private void setupMockResultSetForWeek() throws SQLException {
        when(mockInStoreRs.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockInStoreRs.getInt("id")).thenReturn(1).thenReturn(2).thenReturn(3);
        when(mockInStoreRs.getString("serial_number")).thenReturn(null).thenReturn(null).thenReturn(null);
        LocalDate now = LocalDate.now();
        when(mockInStoreRs.getDate("date"))
                .thenReturn(Date.valueOf(now))
                .thenReturn(Date.valueOf(now.minusDays(2)))
                .thenReturn(Date.valueOf(now.minusDays(4)));
        when(mockInStoreRs.getTime("time"))
                .thenReturn(Time.valueOf(LocalTime.now()))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(1)))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(2)));
        when(mockInStoreRs.getDouble("total")).thenReturn(100.0).thenReturn(150.0).thenReturn(80.0);
        when(mockInStoreRs.getString("employee_name")).thenReturn("Week Manager 1").thenReturn("Week Manager 2")
                .thenReturn("Week Manager 3");
        when(mockOnlineRs.next()).thenReturn(false);
    }

    private void setupMockResultSetForMonth() throws SQLException {
        when(mockInStoreRs.next()).thenReturn(true, true, true, true, true, false);
        when(mockInStoreRs.getInt("id")).thenReturn(1, 2, 3, 4, 5);
        when(mockInStoreRs.getString("serial_number")).thenReturn(null, null, null, null, null);

        // Always pick safe dates from the current month
        LocalDate today = LocalDate.now();
        LocalDate start = today.withDayOfMonth(1);

        // These 5 dates are guaranteed to be in this month regardless of today
        when(mockInStoreRs.getDate("date"))
                .thenReturn(Date.valueOf(start.plusDays(0)),
                        Date.valueOf(start.plusDays(1)),
                        Date.valueOf(start.plusDays(2)),
                        Date.valueOf(start.plusDays(3)),
                        Date.valueOf(start.plusDays(4)));

        when(mockInStoreRs.getTime("time"))
                .thenReturn(Time.valueOf(LocalTime.of(10, 0)),
                        Time.valueOf(LocalTime.of(11, 0)),
                        Time.valueOf(LocalTime.of(12, 0)),
                        Time.valueOf(LocalTime.of(13, 0)),
                        Time.valueOf(LocalTime.of(14, 0)));

        when(mockInStoreRs.getDouble("total"))
                .thenReturn(100.0, 150.0, 80.0, 200.0, 120.0);

        when(mockInStoreRs.getString("employee_name"))
                .thenReturn("Month Manager",
                        "Month Manager 2",
                        "Month Manager 3",
                        "Month Manager 4",
                        "Month Manager 5");

        // No online results for this test
        when(mockOnlineRs.next()).thenReturn(false);
    }

    private void setupMockResultSetForAllTime() throws SQLException {
        when(mockInStoreRs.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true)
                .thenReturn(false);
        when(mockInStoreRs.getInt("id")).thenReturn(1).thenReturn(2).thenReturn(3).thenReturn(4).thenReturn(5);
        when(mockInStoreRs.getString("serial_number")).thenReturn(null).thenReturn(null).thenReturn(null)
                .thenReturn(null).thenReturn(null);
        LocalDate now = LocalDate.now();
        when(mockInStoreRs.getDate("date"))
                .thenReturn(Date.valueOf(now))
                .thenReturn(Date.valueOf(now.minusDays(30)))
                .thenReturn(Date.valueOf(now.minusDays(60)))
                .thenReturn(Date.valueOf(now.minusDays(90)))
                .thenReturn(Date.valueOf(now.minusDays(120)));
        when(mockInStoreRs.getTime("time"))
                .thenReturn(Time.valueOf(LocalTime.now()))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(1)))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(2)))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(3)))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(4)));
        when(mockInStoreRs.getDouble("total")).thenReturn(100.0).thenReturn(150.0).thenReturn(80.0).thenReturn(200.0)
                .thenReturn(120.0);
        when(mockInStoreRs.getString("employee_name")).thenReturn("Manager 1").thenReturn("Manager 2")
                .thenReturn("Manager 3").thenReturn("Manager 4").thenReturn("Manager 5");
        when(mockOnlineRs.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true)
                .thenReturn(false);
        when(mockOnlineRs.getInt("id")).thenReturn(6).thenReturn(7).thenReturn(8).thenReturn(9).thenReturn(10);
        when(mockOnlineRs.getString("serial_number")).thenReturn("ONL-006").thenReturn("ONL-007").thenReturn("ONL-008")
                .thenReturn("ONL-009").thenReturn("ONL-010");
        when(mockOnlineRs.getDate("date"))
                .thenReturn(Date.valueOf(now))
                .thenReturn(Date.valueOf(now.minusDays(30)))
                .thenReturn(Date.valueOf(now.minusDays(60)))
                .thenReturn(Date.valueOf(now.minusDays(90)))
                .thenReturn(Date.valueOf(now.minusDays(120)));
        when(mockOnlineRs.getTime("time"))
                .thenReturn(Time.valueOf(LocalTime.now()))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(1)))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(2)))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(3)))
                .thenReturn(Time.valueOf(LocalTime.now().minusHours(4)));
        when(mockOnlineRs.getDouble("total")).thenReturn(90.0).thenReturn(175.0).thenReturn(95.0).thenReturn(210.0)
                .thenReturn(130.0);
        when(mockOnlineRs.getString("payment_method")).thenReturn("Cash on Delivery").thenReturn("Pay in Store")
                .thenReturn("Cash on Delivery").thenReturn("Pay in Store").thenReturn("Cash on Delivery");
        when(mockOnlineRs.getString("username")).thenReturn("user6").thenReturn("user7").thenReturn("user8")
                .thenReturn("user9").thenReturn("user10");
    }

    private void setupMockResultSetForCustomRange() throws SQLException {
        when(mockInStoreRs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockInStoreRs.getInt("id")).thenReturn(1).thenReturn(2);
        when(mockInStoreRs.getString("serial_number")).thenReturn(null).thenReturn(null);
        when(mockInStoreRs.getDate("date"))
                .thenReturn(Date.valueOf(LocalDate.of(2025, 6, 1)))
                .thenReturn(Date.valueOf(LocalDate.of(2025, 6, 2)));
        when(mockInStoreRs.getTime("time"))
                .thenReturn(Time.valueOf(LocalTime.of(10, 0)))
                .thenReturn(Time.valueOf(LocalTime.of(15, 30)));
        when(mockInStoreRs.getDouble("total")).thenReturn(100.0).thenReturn(150.0);
        when(mockInStoreRs.getString("employee_name")).thenReturn("Custom Manager 1").thenReturn("Custom Manager 2");
        when(mockOnlineRs.next()).thenReturn(false);
    }

    private void setupMockResultSetForCashOnDelivery() throws SQLException {
        when(mockOnlineRs.next()).thenReturn(true).thenReturn(false);
        when(mockOnlineRs.getInt("id")).thenReturn(101);
        when(mockOnlineRs.getString("serial_number")).thenReturn("COD-001");
        when(mockOnlineRs.getDate("date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockOnlineRs.getTime("time")).thenReturn(Time.valueOf(LocalTime.now()));
        when(mockOnlineRs.getDouble("total")).thenReturn(100.0);
        when(mockOnlineRs.getString("payment_method")).thenReturn("Cash on Delivery");
        when(mockOnlineRs.getString("username")).thenReturn("cod_customer");
        when(mockInStoreRs.next()).thenReturn(false);
    }

    private void setupMockResultSetForPayInStore() throws SQLException {
        when(mockOnlineRs.next()).thenReturn(true).thenReturn(false);
        when(mockOnlineRs.getInt("id")).thenReturn(102);
        when(mockOnlineRs.getString("serial_number")).thenReturn("PIS-001");
        when(mockOnlineRs.getDate("date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockOnlineRs.getTime("time")).thenReturn(Time.valueOf(LocalTime.now()));
        when(mockOnlineRs.getDouble("total")).thenReturn(50.0);
        when(mockOnlineRs.getString("payment_method")).thenReturn("Pay in Store");
        when(mockOnlineRs.getString("username")).thenReturn("store_customer");
        when(mockInStoreRs.next()).thenReturn(false);
    }

    private void setupMockResultSetForReportData() throws SQLException {
        when(mockInStoreRs.next()).thenReturn(true).thenReturn(false);
        when(mockInStoreRs.getInt("id")).thenReturn(1);
        when(mockInStoreRs.getString("serial_number")).thenReturn(null);
        when(mockInStoreRs.getDate("date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockInStoreRs.getTime("time")).thenReturn(Time.valueOf(LocalTime.now()));
        when(mockInStoreRs.getDouble("total")).thenReturn(100.0);
        when(mockInStoreRs.getString("employee_name")).thenReturn("Report Manager");
        when(mockOnlineRs.next()).thenReturn(false);
    }
}