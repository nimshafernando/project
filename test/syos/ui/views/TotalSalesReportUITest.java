package syos.ui.views;

import org.junit.jupiter.api.*;
import org.mockito.*;
import syos.dto.ReportItemDTO;
import syos.service.SalesReportService;
import syos.service.SalesReportService.StoreType;
import syos.service.SalesReportService.TransactionType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("TotalSalesReportUI Tests")
class TotalSalesReportUITest {
    @Mock
    private SalesReportService mockService;

    private TotalSalesReportUI totalSalesReportUI;

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);

        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        System.setErr(originalErr);
        if (mocks != null) {
            mocks.close();
        }
    }

    private void setInput(String input) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(inputStream);
        totalSalesReportUI = new TotalSalesReportUI(scanner, mockService);
    }

    private String getOutput() {
        return outputStream.toString();
    }

    private List<ReportItemDTO> createSampleReportItems() {
        List<ReportItemDTO> items = new ArrayList<>();

        ReportItemDTO item1 = new ReportItemDTO("P001", "Product 1", 10, 1000.50);
        ReportItemDTO item2 = new ReportItemDTO("P002", "Product 2", 20, 2000.75);

        items.add(item1);
        items.add(item2);
        return items;
    }

    private List<ReportItemDTO> createCombinedStoreReportItems() {
        List<ReportItemDTO> items = new ArrayList<>();

        ReportItemDTO item1 = new ReportItemDTO("P001", "Product 1 (In-Store)", 10, 1000.50);
        ReportItemDTO item2 = new ReportItemDTO("P002", "Product 2 (Online)", 20, 2000.75);
        ReportItemDTO item3 = new ReportItemDTO("P003", "Very Long Product Name That Needs To Be Truncated (In-Store)",
                5, 500.00);

        items.add(item1);
        items.add(item2);
        items.add(item3);
        return items;
    } // Constructor Tests

    @Test
    @DisplayName("Should create instance with valid scanner")
    void testConstructor_WithValidScanner() {
        Scanner scanner = new Scanner("test");
        TotalSalesReportUI ui = new TotalSalesReportUI(scanner);
        assertNotNull(ui);
    }

    @Test
    @DisplayName("Should create instance with null scanner")
    void testConstructor_WithNullScanner() {
        TotalSalesReportUI ui = new TotalSalesReportUI(null);
        assertNotNull(ui);
    }

    @Test
    @DisplayName("Should create instance with dependency injection")
    void testConstructor_WithDependencyInjection() {
        Scanner scanner = new Scanner("test");
        SalesReportService service = mock(SalesReportService.class);
        TotalSalesReportUI ui = new TotalSalesReportUI(scanner, service);
        assertNotNull(ui);
    }

    // Generate Report Tests - Happy Path
    @Test
    @DisplayName("Should generate report for today with in-store sales")
    void testGenerateReport_TodayInStore() {
        // Arrange
        String input = "1\n1\n\n"; // 1 for today, 1 for in-store, Enter for pause
        setInput(input);

        List<ReportItemDTO> mockItems = createSampleReportItems();
        when(mockService.getSalesReport(any(LocalDate.class), eq(StoreType.IN_STORE), eq(TransactionType.IN_STORE)))
                .thenReturn(mockItems);

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("SYOS DAILY SALES REPORT"));
        assertTrue(output.contains("Date       : " + LocalDate.now()));
        assertTrue(output.contains("Store Type : IN_STORE"));
        assertTrue(output.contains("Txn Type   : IN_STORE"));
        assertTrue(output.contains("P001"));
        assertTrue(output.contains("Product 1"));
        assertTrue(output.contains("1000.50"));
        assertTrue(output.contains("Total Quantity: 30"));
        assertTrue(output.contains("Total Revenue: Rs. 3001.25"));

        verify(mockService).getSalesReport(LocalDate.now(), StoreType.IN_STORE, TransactionType.IN_STORE);
    }

    @Test
    @DisplayName("Should generate report for this week with online reservation pay in store")
    void testGenerateReport_ThisWeekOnlinePayInStore() {
        // Arrange
        String input = "2\n2\n1\n\n"; // 2 for this week, 2 for online, 1 for reservation pay in store, Enter for
                                      // pause
        setInput(input);

        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        List<ReportItemDTO> mockItems = createSampleReportItems();
        when(mockService.getSalesReport(any(LocalDate.class), eq(StoreType.ONLINE),
                eq(TransactionType.RESERVATION_PAY_IN_STORE)))
                .thenReturn(mockItems);

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Date Range : " + startOfWeek));
        assertTrue(output.contains("Store Type : ONLINE"));
        assertTrue(output.contains("Txn Type   : RESERVATION_PAY_IN_STORE"));

        verify(mockService).getSalesReport(startOfWeek, StoreType.ONLINE, TransactionType.RESERVATION_PAY_IN_STORE);
    }

    @Test
    @DisplayName("Should generate report with specific date range")
    void testGenerateReport_SpecificDateRange() {
        // Arrange
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        String input = String.format("4\n%s\n%s\n2\n2\n\n",
                yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE),
                today.format(DateTimeFormatter.ISO_LOCAL_DATE));
        setInput(input);

        List<ReportItemDTO> mockItems = createSampleReportItems();
        when(mockService.getSalesReport(any(LocalDate.class), any(StoreType.class), any(TransactionType.class)))
                .thenReturn(mockItems);

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Date Range : " + yesterday + " to " + today));

        verify(mockService).getSalesReport(yesterday, StoreType.ONLINE, TransactionType.RESERVATION_COD);
    }

    // Generate Report Tests - Edge Cases
    @Test
    @DisplayName("Should handle empty report items")
    void testGenerateReport_EmptyItems() {
        // Arrange
        String input = "1\n1\n\n"; // 1 for today, 1 for in-store, Enter for pause
        setInput(input);
        when(mockService.getSalesReport(any(LocalDate.class), any(StoreType.class), any(TransactionType.class)))
                .thenReturn(new ArrayList<>());

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Total Quantity: 0"));
        assertTrue(output.contains("Total Revenue: Rs. 0.00"));
    }

    @Test
    @DisplayName("Should handle service exception")
    void testGenerateReport_ServiceException() {
        // Arrange
        String input = "1\n1\n\n\n"; // 1 for today, 1 for in-store, Enter for pauseForUser, Enter for showError
        setInput(input);
        when(mockService.getSalesReport(any(LocalDate.class), any(StoreType.class), any(TransactionType.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Error generating report") || output.contains("Database error"));
    }

    @Test
    @DisplayName("Should handle null report items")
    void testGenerateReport_NullItems() {
        // Arrange
        String input = "1\n1\n\n\n"; // 1 for today, 1 for in-store, Enter for pauseForUser, Enter for showError
        setInput(input);
        when(mockService.getSalesReport(any(LocalDate.class), any(StoreType.class), any(TransactionType.class)))
                .thenReturn(null);

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("No data available") || output.contains("Error"));
    }

    // User Input Tests - Invalid Cases
    @Test
    @DisplayName("Should handle invalid date range selection")
    void testInvalidDateRangeSelection() {
        // Arrange
        String input = "5\n1\n1\n\n"; // Invalid choice 5, then valid 1, 1 for in-store, Enter for pause
        setInput(input);

        when(mockService.getSalesReport(any(LocalDate.class), any(StoreType.class), any(TransactionType.class)))
                .thenReturn(createSampleReportItems());

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("[Invalid] Enter 0–4"));
    }

    @Test
    @DisplayName("Should handle invalid specific date format")
    void testInvalidDateFormat() {
        // Arrange
        String input = "4\ninvalid-date\n2024-01-01\n2024-01-02\n1\n\n"; // 4 for specific range, invalid date, then
                                                                         // valid dates, 1 for in-store, Enter for pause
        setInput(input);

        when(mockService.getSalesReport(any(LocalDate.class), any(StoreType.class), any(TransactionType.class)))
                .thenReturn(createSampleReportItems());

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("[Invalid] Use yyyy-mm-dd format"));
    }

    @Test
    @DisplayName("Should handle end date before start date")
    void testEndDateBeforeStartDate() {
        // Arrange
        String input = "4\n2024-01-10\n2024-01-05\n2024-01-05\n2024-01-10\n1\n\n";
        setInput(input);

        when(mockService.getSalesReport(any(LocalDate.class), any(StoreType.class), any(TransactionType.class)))
                .thenReturn(createSampleReportItems());

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("[Invalid] End date before start date"));
    }

    @Test
    @DisplayName("Should handle invalid store type selection")
    void testInvalidStoreTypeSelection() {
        // Arrange
        String input = "1\n4\n\n1\n\n"; // Invalid choice 4, Enter for pause, then valid 1, Enter for pause
        setInput(input);

        when(mockService.getSalesReport(any(LocalDate.class), any(StoreType.class), any(TransactionType.class)))
                .thenReturn(createSampleReportItems());

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("[Invalid] Try again"));
    }

    @Test
    @DisplayName("Should handle invalid transaction type selection")
    void testInvalidTransactionTypeSelection() {
        // Arrange
        String input = "1\n2\n5\n\n1\n\n"; // Invalid choice 5 for online txn type, Enter for pause, then valid 1, Enter
                                           // for pause
        setInput(input);

        when(mockService.getSalesReport(any(LocalDate.class), any(StoreType.class), any(TransactionType.class)))
                .thenReturn(createSampleReportItems());

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("[Invalid] Try again"));
    } // Combined Store Type Tests

    @Test
    @DisplayName("Should handle combined store with all transaction types")
    void testCombinedStoreAllTransactionTypes() {
        // Test each transaction type option for combined store
        String[] inputs = {
                "1\n3\n1\n\n", // IN_STORE + Enter for pause
                "1\n3\n2\n\n", // RESERVATION_PAY_IN_STORE + Enter for pause
                "1\n3\n3\n\n", // RESERVATION_COD + Enter for pause
                "1\n3\n4\n\n" // ALL + Enter for pause
        };

        TransactionType[] expectedTypes = {
                TransactionType.IN_STORE,
                TransactionType.RESERVATION_PAY_IN_STORE,
                TransactionType.RESERVATION_COD,
                TransactionType.ALL
        };

        for (int i = 0; i < inputs.length; i++) {
            try {
                setUp(); // Reset for each test
            } catch (Exception e) {
                fail("Setup failed: " + e.getMessage());
            }
            setInput(inputs[i]);
            when(mockService.getSalesReport(any(LocalDate.class), eq(StoreType.COMBINED),
                    any(TransactionType.class)))
                    .thenReturn(createCombinedStoreReportItems());

            totalSalesReportUI.display();

            verify(mockService).getSalesReport(any(), eq(StoreType.COMBINED), eq(expectedTypes[i]));
        }
    }

    @Test
    @DisplayName("Should handle unknown store type in item name")
    void testUnknownStoreTypeInItemName() {
        // Arrange
        String input = "1\n3\n4\n\n"; // Enter for pause
        setInput(input);
        List<ReportItemDTO> items = new ArrayList<>();
        ReportItemDTO item = new ReportItemDTO("P001", "Product without store indicator", 10, 100.00);
        items.add(item);

        when(mockService.getSalesReport(any(LocalDate.class), eq(StoreType.COMBINED), eq(TransactionType.ALL)))
                .thenReturn(items);

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Unknown"));
    }

    // Online Store Type Tests
    @Test
    @DisplayName("Should handle online store with COD transaction")
    void testOnlineStoreCOD() {
        // Arrange
        String input = "1\n2\n2\n\n"; // Enter for pause
        setInput(input);
        when(mockService.getSalesReport(any(LocalDate.class), eq(StoreType.ONLINE),
                eq(TransactionType.RESERVATION_COD)))
                .thenReturn(createSampleReportItems());

        // Act
        totalSalesReportUI.display();

        // Assert
        verify(mockService).getSalesReport(any(), eq(StoreType.ONLINE), eq(TransactionType.RESERVATION_COD));
    }

    @Test
    @DisplayName("Should handle online store with ALL transaction types")
    void testOnlineStoreAllTransactions() {
        // Arrange
        String input = "1\n2\n3\n\n"; // Enter for pause
        setInput(input);
        when(mockService.getSalesReport(any(LocalDate.class), eq(StoreType.ONLINE), eq(TransactionType.ALL)))
                .thenReturn(createSampleReportItems());

        // Act
        totalSalesReportUI.display();

        // Assert
        verify(mockService).getSalesReport(any(), eq(StoreType.ONLINE), eq(TransactionType.ALL));
    }

    // User Input Tests - Back Options
    @Test
    @DisplayName("Should handle back option in date range selection")
    void testBackOptionDateRange() {
        // Arrange
        String input = "0\n\n"; // 0 to go back, then Enter for pause
        setInput(input);

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Returning to reports menu"));
    }

    @Test
    @DisplayName("Should handle back option in store type selection")
    void testBackOptionStoreType() {
        // Arrange
        String input = "1\n0\n\n"; // 1 for today, 0 to go back from store type, then Enter for pause
        setInput(input);

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Returning to reports menu"));
    }

    @Test
    @DisplayName("Should handle back option in transaction type selection")
    void testBackOptionTransactionType() {
        // Arrange
        String input = "1\n2\n0\n\n"; // 1 for today, 2 for online, 0 to go back from transaction type, then Enter for
                                      // pause
        setInput(input);

        // Act
        totalSalesReportUI.display();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Returning to reports menu"));
    }

    // Private Method Tests
    @Test
    @DisplayName("Should test extractStoreType method")
    void testExtractStoreType() throws Exception {
        // Use reflection to test private method
        Method method = TotalSalesReportUI.class.getDeclaredMethod("extractStoreType", String.class);
        method.setAccessible(true);

        TotalSalesReportUI ui = new TotalSalesReportUI(null, mockService);

        assertEquals("In-Store", method.invoke(ui, "Product (In-Store)"));
        assertEquals("Online", method.invoke(ui, "Product (Online)"));
        assertEquals("Unknown", method.invoke(ui, "Product"));
    }

    @Test
    @DisplayName("Should test cleanItemName method")
    void testCleanItemName() throws Exception {
        // Use reflection to test private method
        Method method = TotalSalesReportUI.class.getDeclaredMethod("cleanItemName", String.class);
        method.setAccessible(true);

        TotalSalesReportUI ui = new TotalSalesReportUI(null, mockService);

        assertEquals("Product", method.invoke(ui, "Product (In-Store)"));
        assertEquals("Product", method.invoke(ui, "Product (Online)"));
        assertEquals("Product Name", method.invoke(ui, "Product Name (In-Store)"));
        assertEquals("Product", method.invoke(ui, "Product"));
    }

    @Test
    @DisplayName("Should test truncateName method")
    void testTruncateName() throws Exception {
        // Use reflection to test private method
        Method method = TotalSalesReportUI.class.getDeclaredMethod("truncateName", String.class, int.class);
        method.setAccessible(true);

        TotalSalesReportUI ui = new TotalSalesReportUI(null, mockService);

        assertEquals("Short", method.invoke(ui, "Short", 10));
        assertEquals("Exactly10C", method.invoke(ui, "Exactly10C", 10));
        assertEquals("Very Lo...", method.invoke(ui, "Very Long Name", 10));
    }

    // Integration Tests
    @Test
    @DisplayName("Should complete full workflow with all date range options")
    void testCompleteWorkflowAllDateRanges() {
        // Test today
        testDateRangeOption("1\n1\n", LocalDate.now(), LocalDate.now());

        // Test this week
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate weekEnd = LocalDate.now().with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
        testDateRangeOption("2\n1\n", weekStart, weekEnd);

        // Test this month
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate monthEnd = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        testDateRangeOption("3\n1\n", monthStart, monthEnd);
    }

    private void testDateRangeOption(String input, LocalDate expectedStart, LocalDate expectedEnd) {
        try {
            setUp(); // Reset for each test
        } catch (Exception e) {
            fail("Setup failed: " + e.getMessage());
        }
        setInput(input + "\n"); // Add Enter for pause
        when(mockService.getSalesReport(any(LocalDate.class), any(StoreType.class), any(TransactionType.class)))
                .thenReturn(createSampleReportItems());

        totalSalesReportUI.display();

        String output = getOutput();
        assertTrue(output.contains("SYOS DAILY SALES REPORT"));

        // Verify the service was called with expected start date
        verify(mockService).getSalesReport(eq(expectedStart), any(), any());
    }

    @Test
    @DisplayName("Should handle edge case with null items in list")
    void testNullItemsInList() {
        // Arrange
        String input = "1\n1\n\n\n"; // 1 for today, 1 for in-store, Enter for pauseForUser, Enter for showError
        setInput(input);

        List<ReportItemDTO> items = new ArrayList<>();
        items.add(null); // Add null item
        items.add(createSampleReportItems().get(0));

        when(mockService.getSalesReport(any(LocalDate.class), any(StoreType.class), any(TransactionType.class)))
                .thenReturn(items);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> totalSalesReportUI.display());
    }

    // Error Handling Tests
    @Test
    @DisplayName("Should handle scanner exception gracefully")
    void testScannerException() {
        // Create a scanner that will throw exception
        Scanner exceptionScanner = mock(Scanner.class);
        when(exceptionScanner.nextLine()).thenThrow(new NoSuchElementException());

        totalSalesReportUI = new TotalSalesReportUI(exceptionScanner);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> totalSalesReportUI.display());
    }

    @Test
    @DisplayName("Should handle report generation with very large numbers")
    void testVeryLargeNumbers() {
        // Arrange
        String input = "1\n1\n\n"; // Enter for pause
        setInput(input);
        List<ReportItemDTO> items = new ArrayList<>();
        ReportItemDTO item = new ReportItemDTO("P001", "Product", Integer.MAX_VALUE, Double.MAX_VALUE);
        items.add(item);

        when(mockService.getSalesReport(any(LocalDate.class), any(StoreType.class), any(TransactionType.class)))
                .thenReturn(items);

        // Act & Assert - should handle large numbers without overflow
        assertDoesNotThrow(() -> totalSalesReportUI.display());
    }
}