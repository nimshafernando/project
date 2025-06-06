package syos.ui.views;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import syos.dto.BillHistoryDTO;
import syos.service.BillHistoryReportService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for BillHistoryReportUI class.
 * Tests all public methods, constructor, and error handling scenarios.
 */
@DisplayName("BillHistoryReportUI Tests")
class BillHistoryReportUITest {

    private BillHistoryReportUI billHistoryReportUI;
    private Scanner mockScanner;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private AutoCloseable closeable;

    @Mock
    private BillHistoryReportService mockService;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Capture system output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore original system output
        System.setOut(originalOut);

        if (closeable != null) {
            closeable.close();
        }
    }

    private void createBillHistoryReportUI(String input) {
        mockScanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        billHistoryReportUI = new BillHistoryReportUI(mockScanner);

        // Initialize filter variables to prevent null pointer exceptions
        // when calling renderReport directly via reflection
        try {
            // Set default filter values using reflection
            setPrivateField("storeFilter", "ALL");
            setPrivateField("dateFilter", "ALL_TIME");
            setPrivateField("paymentMethodFilter", "ALL_PAYMENT_METHODS");
            setPrivateField("startDate", LocalDate.now());
            setPrivateField("endDate", LocalDate.now());
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize filter variables: " + e.getMessage());
        }
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = billHistoryReportUI.getClass().getDeclaredField(fieldName);
        field.setAccessible(true); // Convert string values to proper enum types
        if (value instanceof String) {
            String stringValue = (String) value;
            switch (fieldName) {
                case "storeFilter":
                    // Get the StoreFilter enum from BillHistoryReportService
                    Class<?> storeFilterClass = Class.forName("syos.service.BillHistoryReportService$StoreFilter");
                    @SuppressWarnings("unchecked")
                    Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) storeFilterClass;
                    value = Enum.valueOf((Class) enumClass, stringValue);
                    break;
                case "dateFilter":
                    Class<?> dateFilterClass = Class.forName("syos.service.BillHistoryReportService$DateFilter");
                    @SuppressWarnings("unchecked")
                    Class<? extends Enum<?>> enumClass2 = (Class<? extends Enum<?>>) dateFilterClass;
                    value = Enum.valueOf((Class) enumClass2, stringValue);
                    break;
                case "paymentMethodFilter":
                    Class<?> paymentMethodFilterClass = Class
                            .forName("syos.service.BillHistoryReportService$PaymentMethodFilter");
                    @SuppressWarnings("unchecked")
                    Class<? extends Enum<?>> enumClass3 = (Class<? extends Enum<?>>) paymentMethodFilterClass;
                    value = Enum.valueOf((Class) enumClass3, stringValue);
                    break;
            }
        }

        field.set(billHistoryReportUI, value);
    }

    private String getOutput() {
        return outputStream.toString();
    }

    // Helper method to create sample BillHistoryDTO objects
    private BillHistoryDTO createBillHistoryDTO(int billId, String storeType, String paymentMethod,
            double totalAmount, String customerInfo, String employeeName) {
        String serialNumber = "BILL-" + billId;
        LocalDateTime dateTime = LocalDateTime.now();
        double discount = totalAmount * 0.1; // 10% discount
        int itemCount = 5; // Default item count

        BillHistoryDTO dto = new BillHistoryDTO(billId, serialNumber, dateTime, totalAmount, discount,
                storeType, paymentMethod, customerInfo, employeeName, itemCount);
        return dto;
    } // Constructor Tests

    @Test
    @DisplayName("Should create BillHistoryReportUI with valid scanner")
    void testConstructor_ValidScanner() {
        createBillHistoryReportUI("1\n1\n1\n\n");

        assertNotNull(billHistoryReportUI);
        assertTrue(getOutput().isEmpty()); // Constructor should not produce output
    }

    @Test
    @DisplayName("Should create BillHistoryReportUI with null scanner")
    void testConstructor_NullScanner() {
        // Based on implementation analysis, constructor accepts null scanner without
        // throwing exception
        assertDoesNotThrow(() -> {
            new BillHistoryReportUI(null);
        });
    }

    // Report Criteria Gathering Tests
    @Test
    @DisplayName("Should gather criteria successfully for in-store reports")
    void testGatherReportCriteria_InStore() {
        createBillHistoryReportUI("1\n1\n\n"); // In-store, today

        boolean result = invokeGatherReportCriteria();

        assertTrue(result);
        String output = getOutput();
        assertTrue(output.contains("Select Store Type"));
        assertTrue(output.contains("Select Date Range"));
    }

    @Test
    @DisplayName("Should gather criteria successfully for online reports with payment filter")
    void testGatherReportCriteria_OnlineWithPaymentFilter() {
        createBillHistoryReportUI("2\n1\n2\n\n"); // Online, today, cash on delivery

        boolean result = invokeGatherReportCriteria();

        assertTrue(result);
        String output = getOutput();
        assertTrue(output.contains("Select Store Type"));
        assertTrue(output.contains("Select Date Range"));
        assertTrue(output.contains("Select Payment Method Filter"));
    }

    @Test
    @DisplayName("Should gather criteria with custom date range")
    void testGatherReportCriteria_CustomDateRange() {
        String input = "3\n5\n2024-01-01\n2024-12-31\n\n"; // All stores, custom range
        createBillHistoryReportUI(input);

        boolean result = invokeGatherReportCriteria();

        assertTrue(result);
        String output = getOutput();
        assertTrue(output.contains("Enter start date"));
        assertTrue(output.contains("Enter end date"));
    }

    @Test
    @DisplayName("Should handle invalid date format gracefully")
    void testGatherReportCriteria_InvalidDateFormat() {
        String input = "1\n5\ninvalid-date\n2024-01-01\ninvalid-date\n2024-12-31\n\n";
        createBillHistoryReportUI(input);

        boolean result = invokeGatherReportCriteria();

        assertTrue(result);
        String output = getOutput();
        assertTrue(output.contains("[Invalid] Format must be yyyy-MM-dd"));
    }

    @Test
    @DisplayName("Should return false when user chooses to go back from store filter")
    void testGatherReportCriteria_BackFromStoreFilter() {
        createBillHistoryReportUI("0\n"); // Back from store filter

        boolean result = invokeGatherReportCriteria();

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when user chooses to go back from date filter")
    void testGatherReportCriteria_BackFromDateFilter() {
        createBillHistoryReportUI("1\n0\n"); // In-store, then back from date filter

        boolean result = invokeGatherReportCriteria();

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when user chooses to go back from payment filter")
    void testGatherReportCriteria_BackFromPaymentFilter() {
        createBillHistoryReportUI("2\n1\n0\n"); // Online, today, then back from payment filter

        boolean result = invokeGatherReportCriteria();

        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid", "5", "-1", "abc" })
    @DisplayName("Should handle invalid store filter choices")
    void testGatherReportCriteria_InvalidStoreFilterChoices(String invalidChoice) {
        createBillHistoryReportUI(invalidChoice + "\n1\n1\n\n");

        boolean result = invokeGatherReportCriteria();

        assertTrue(result);
        String output = getOutput();
        assertTrue(output.contains("[Invalid] Please enter a valid option"));
    } // Report Rendering Tests

    @Test
    @DisplayName("Should render report with multiple bills successfully")
    void testRenderReport_MultipleBills() {
        createBillHistoryReportUI("\n");

        List<BillHistoryDTO> bills = Arrays.asList(
                createBillHistoryDTO(1001, "IN_STORE", "CASH", 150.75, null, "John Doe"),
                createBillHistoryDTO(1002, "ONLINE", "CASH_ON_DELIVERY", 275.50, "jane@email.com", null),
                createBillHistoryDTO(1003, "ONLINE", "PAY_IN_STORE", 89.99, "bob@email.com", null));

        invokeRenderReport(bills);

        String output = getOutput();
        assertTrue(output.contains("TRANSACTION SUMMARY"));
        assertTrue(output.contains("TRANSACTION DETAILS"));
        assertTrue(output.contains("Bill ID"));
        assertTrue(output.contains("1001"));
        assertTrue(output.contains("1002"));
        assertTrue(output.contains("1003"));
        assertTrue(output.contains("John Doe"));
        assertTrue(output.contains("jane@email.com"));
        assertTrue(output.contains("150.75"));
        assertTrue(output.contains("275.50"));
        assertTrue(output.contains("89.99"));
    }

    @Test
    @DisplayName("Should render empty report message when no bills found")
    void testRenderReport_EmptyBills() {
        createBillHistoryReportUI("\n");

        invokeRenderReport(Collections.emptyList());

        String output = getOutput();
        assertTrue(output.contains("No transactions found for the selected criteria"));
        assertTrue(output.contains("TRANSACTION SUMMARY"));
    }

    @Test
    @DisplayName("Should render in-store transactions correctly")
    void testRenderReport_InStoreTransactions() {
        createBillHistoryReportUI("\n");

        List<BillHistoryDTO> bills = Arrays.asList(
                createBillHistoryDTO(2001, "IN_STORE", "CASH", 125.50, null, "Alice Smith"),
                createBillHistoryDTO(2002, "IN_STORE", "CREDIT_CARD", 300.25, null, "Bob Johnson"));
        invokeRenderReport(bills);

        String output = getOutput();
        assertTrue(output.contains("In-Store Transactions"));
        assertTrue(output.contains("Alice Smith"));
        assertTrue(output.contains("Bob Johnson"));
        assertTrue(output.contains("TOTAL REVENUE: Rs.     425.75"));
    }

    @Test
    @DisplayName("Should render online transactions with payment method breakdown")
    void testRenderReport_OnlineTransactions() {
        createBillHistoryReportUI("\n");

        List<BillHistoryDTO> bills = Arrays.asList(
                createBillHistoryDTO(3001, "ONLINE", "CASH_ON_DELIVERY", 180.75, "customer1@email.com", null),
                createBillHistoryDTO(3002, "ONLINE", "PAY_IN_STORE", 220.50, "customer2@email.com", null),
                createBillHistoryDTO(3003, "ONLINE", "CASH_ON_DELIVERY", 95.25, "customer3@email.com", null));

        invokeRenderReport(bills);
        String output = getOutput();

        // Check the summary section content
        assertTrue(output.contains("Online Transactions"));
        assertTrue(output.contains("Payment Methods:"));
        assertTrue(output.contains("PAY_IN_STORE"));
        assertTrue(output.contains("CASH_ON_DELIVERY"));
        // Check for email addresses - they are truncated in the output
        assertTrue(output.contains("customer1@em"));
        assertTrue(output.contains("customer2@em"));
        assertTrue(output.contains("customer3@em"));
    }

    @Test
    @DisplayName("Should handle bills with null customer info and employee names")
    void testRenderReport_NullUserInfo() {
        createBillHistoryReportUI("\n");

        List<BillHistoryDTO> bills = Arrays.asList(
                createBillHistoryDTO(4001, "IN_STORE", "CASH", 100.00, null, null),
                createBillHistoryDTO(4002, "ONLINE", "CASH_ON_DELIVERY", 200.00, null, null));

        invokeRenderReport(bills);

        String output = getOutput();
        assertTrue(output.contains("N/A")); // Should show N/A for null values
    }

    @Test
    @DisplayName("Should truncate long text fields correctly")
    void testRenderReport_LongTextTruncation() {
        createBillHistoryReportUI("\n");

        BillHistoryDTO billWithLongText = createBillHistoryDTO(5001, "ONLINE",
                "VERY_LONG_PAYMENT_METHOD_NAME_THAT_EXCEEDS_LIMIT", 150.00,
                "very.long.customer.email.address.that.exceeds.the.display.limit@company.com", null);

        List<BillHistoryDTO> bills = Arrays.asList(billWithLongText);

        invokeRenderReport(bills);

        String output = getOutput();
        assertTrue(output.contains("...")); // Should show truncation
    } // Utility Method Tests

    @Test
    @DisplayName("Should return correct report title")
    void testGetReportTitle() {
        createBillHistoryReportUI("");

        String title = invokeGetReportTitle();

        assertEquals("Bill History Report", title);
    }

    @Test
    @DisplayName("Should show no data message correctly")
    void testShowNoDataMessage() {
        createBillHistoryReportUI("\n");

        invokeShowNoDataMessage();

        String output = getOutput();
        assertTrue(output.contains("No transactions found for the selected criteria"));
        assertTrue(output.contains("TRANSACTION SUMMARY"));
    }

    @Test
    @DisplayName("Should handle different date filter descriptions")
    void testDateFilterDescriptions() {
        createBillHistoryReportUI("1\n1\n\n"); // In-store, today
        invokeGatherReportCriteria();

        invokeShowNoDataMessage();
        String output = getOutput();
        assertTrue(output.contains("Date Range: Today"));
    }

    @Test
    @DisplayName("Should handle different store filter descriptions")
    void testStoreFilterDescriptions() {
        createBillHistoryReportUI("1\n1\n\n"); // In-store, today
        invokeGatherReportCriteria();

        invokeShowNoDataMessage();
        String output = getOutput();
        assertTrue(output.contains("Store Filter: In-Store Only"));
    } // Integration Tests

    @Test
    @DisplayName("Should handle complete workflow for in-store report")
    void testCompleteWorkflow_InStore() {
        createBillHistoryReportUI("1\n2\n\n"); // In-store, this week

        // Test the complete workflow
        boolean criteriaResult = invokeGatherReportCriteria();
        assertTrue(criteriaResult);

        String title = invokeGetReportTitle();
        assertEquals("Bill History Report", title);

        // Test with sample data
        List<BillHistoryDTO> sampleBills = Arrays.asList(
                createBillHistoryDTO(6001, "IN_STORE", "CASH", 500.00, null, "Manager Smith"));

        invokeRenderReport(sampleBills);

        String output = getOutput();
        assertTrue(output.contains("Bill History Report") || output.contains("TRANSACTION SUMMARY"));
        assertTrue(output.contains("Manager Smith"));
    } // Helper methods for reflection-based method calls

    private boolean invokeGatherReportCriteria() {
        try {
            java.lang.reflect.Method method = BillHistoryReportUI.class.getDeclaredMethod("gatherReportCriteria");
            method.setAccessible(true);
            return (Boolean) method.invoke(billHistoryReportUI);
        } catch (Exception e) {
            fail("Failed to invoke gatherReportCriteria: " + e.getMessage());
            return false;
        }
    }

    private void invokeRenderReport(List<BillHistoryDTO> bills) {
        try {
            // Look for the method in the class hierarchy
            java.lang.reflect.Method method = findMethod(billHistoryReportUI.getClass(), "renderReport", List.class);
            if (method == null) {
                fail("renderReport method not found in class hierarchy");
                return;
            }
            method.setAccessible(true);
            method.invoke(billHistoryReportUI, bills);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to invoke renderReport: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private String invokeGetReportTitle() {
        try {
            // Look for the method in the class hierarchy
            java.lang.reflect.Method method = findMethod(billHistoryReportUI.getClass(), "getReportTitle");
            if (method == null) {
                fail("getReportTitle method not found in class hierarchy");
                return null;
            }
            method.setAccessible(true);
            return (String) method.invoke(billHistoryReportUI);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to invoke getReportTitle: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return null;
        }
    }

    private void invokeShowNoDataMessage() {
        try {
            // Look for the method in the class hierarchy
            java.lang.reflect.Method method = findMethod(billHistoryReportUI.getClass(), "showNoDataMessage");
            if (method == null) {
                fail("showNoDataMessage method not found in class hierarchy");
                return;
            }
            method.setAccessible(true);
            method.invoke(billHistoryReportUI);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to invoke showNoDataMessage: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    // Utility method to find method in class hierarchy
    private java.lang.reflect.Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
