package syos.ui.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import syos.model.Employee;
import syos.model.Bill;
import syos.model.CartItem;
import syos.dto.ItemDTO;
import syos.service.POS;
import syos.data.ItemGateway;
import syos.data.BillGateway;
import syos.util.BillStorage;
import syos.util.ConsoleUtils;
import syos.util.EmployeeSession;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Comprehensive test suite for CheckoutAndBillingController
 * Tests all methods with 100% coverage including happy paths and edge cases
 */
@DisplayName("CheckoutAndBillingController Tests")
class CheckoutAndBillingControllerTest {

    @Mock
    private ItemGateway mockItemGateway;

    @Mock
    private BillGateway mockBillGateway;

    @Mock
    private POS mockPOS;

    @Mock
    private EmployeeSession mockEmployeeSession;

    private Employee testEmployee;
    private List<ItemDTO> testItems;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        setupTestData();
        captureSystemOut();
    }

    @AfterEach
    void tearDown() {
        restoreSystemOut();
    }

    private void setupTestData() {
        // Create test employee with realistic data
        testEmployee = new Employee("EMP001", "John Smith", "1234", "Cashier");

        // Create test items with realistic Sri Lankan store items
        testItems = Arrays.asList(
                new ItemDTO("APPLE001", "Red Delicious Apples", 125.50, 50),
                new ItemDTO("MILK002", "Fresh Whole Milk 1L", 85.75, 25),
                new ItemDTO("BREAD003", "Whole Wheat Bread", 65.00, 15),
                new ItemDTO("EGGS004", "Farm Fresh Eggs (12 pack)", 180.00, 30));
    }

    private void captureSystemOut() {
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    private void restoreSystemOut() {
        System.setOut(originalOut);
    }

    private String getOutput() {
        return outputStream.toString();
    }

    private Scanner createScannerWithInput(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    @Test
    @DisplayName("Should handle successful checkout with multiple items")
    void testLaunch_SuccessfulCheckout() {
        // Arrange
        String input = "APPLE001\n3\nMILK002\n2\ncheckout\n5%\n500\n\n"; // simulate user input
        Scanner scanner = createScannerWithInput(input);

        // Stub item gateway to return items
        when(mockItemGateway.getAllItems()).thenReturn(testItems);
        when(mockItemGateway.getItemByCode("APPLE001")).thenReturn(testItems.get(0));
        when(mockItemGateway.getItemByCode("MILK002")).thenReturn(testItems.get(1));

        CartItem cartItem1 = new CartItem(testItems.get(0), 3); // 125.50 x 3 = 376.50
        CartItem cartItem2 = new CartItem(testItems.get(1), 2); // 85.75 x 2 = 171.50
        List<CartItem> cartItems = Arrays.asList(cartItem1, cartItem2);

        // Stub POS cart
        when(mockPOS.getCartItems()).thenReturn(cartItems);
        when(mockPOS.getCartTotal()).thenReturn(548.00);

        // Prepare a mock bill object
        Bill mockBill = new Bill();
        mockBill.setItems(cartItems);
        mockBill.setTotal(548.00);
        mockBill.setDiscount(27.40);
        mockBill.setCashTendered(500.00);
        mockBill.setChange(-75.40);

        // Return the mock bill from checkout
        when(mockPOS.checkout(anyDouble(), anyDouble())).thenReturn(mockBill);

        // Mock static methods and session
        try (
                MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {
            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(true);
            when(mockEmployeeSession.getCurrentEmployeeName()).thenReturn("John Smith");
            when(mockEmployeeSession.getEmployeeId()).thenReturn("EMP001");

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            verify(mockItemGateway, atLeastOnce()).getAllItems();
            verify(mockPOS, times(2)).addToCart(any(ItemDTO.class), anyInt());
            verify(mockPOS).checkout(anyDouble(), anyDouble());
            verify(mockBillGateway).saveBill(eq(mockBill));
            verify(mockItemGateway).reduceStock("APPLE001", 3);
            verify(mockItemGateway).reduceStock("MILK002", 2);

            String output = getOutput();
            assertContains(output, "CHECKOUT AND BILLING MODULE");
            assertContains(output, "Cashier: John Smith (EMP001)");
            assertContains(output, "Transaction completed successfully");
        }
    }

    @Test
    @DisplayName("Should handle empty inventory gracefully")
    void testLaunch_EmptyInventory() {
        // Arrange
        Scanner scanner = createScannerWithInput("\n");
        when(mockItemGateway.getAllItems()).thenReturn(Collections.emptyList());

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(false);

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            verify(mockItemGateway).getAllItems();
            String output = getOutput();
            assertContains(output, "No items available in inventory");
        }
    }

    @Test
    @DisplayName("Should handle item not found")
    void testLaunch_ItemNotFound() {
        // Arrange
        String input = "INVALID001\ncheckout\n\n"; // Exit after showing empty cart message
        Scanner scanner = createScannerWithInput(input);

        when(mockItemGateway.getAllItems()).thenReturn(testItems);
        when(mockItemGateway.getItemByCode("INVALID001")).thenReturn(null);
        when(mockPOS.getCartItems()).thenReturn(Collections.emptyList());

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(true);
            when(mockEmployeeSession.getCurrentEmployeeName()).thenReturn("John Smith");
            when(mockEmployeeSession.getEmployeeId()).thenReturn("EMP001");

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            String output = getOutput();
            assertContains(output, "Item not found in inventory");
            assertContains(output, "No items in cart. Cancelling transaction");
        }
    }

    @Test
    @DisplayName("Should handle invalid quantity input")
    void testLaunch_InvalidQuantity() {
        // Arrange - all invalid quantities then checkout with empty cart
        String input = "APPLE001\nabc\nAPPLE001\n-5\nAPPLE001\n0\ncheckout\n\n";
        Scanner scanner = createScannerWithInput(input);

        when(mockItemGateway.getAllItems()).thenReturn(testItems);
        when(mockItemGateway.getItemByCode("APPLE001")).thenReturn(testItems.get(0));
        when(mockPOS.getCartItems()).thenReturn(Collections.emptyList());

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(true);
            when(mockEmployeeSession.getCurrentEmployeeName()).thenReturn("John Smith");
            when(mockEmployeeSession.getEmployeeId()).thenReturn("EMP001");

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            String output = getOutput();
            assertContains(output, "Invalid quantity. Please enter a valid number");
            assertContains(output, "Quantity must be a positive integer");
        }
    }

    @Test
    @DisplayName("Should handle insufficient stock")
    void testLaunch_InsufficientStock() {
        // Arrange
        String input = "APPLE001\n100\ncheckout\n\n";
        Scanner scanner = createScannerWithInput(input);

        when(mockItemGateway.getAllItems()).thenReturn(testItems);
        when(mockItemGateway.getItemByCode("APPLE001")).thenReturn(testItems.get(0));
        when(mockPOS.getCartItems()).thenReturn(Collections.emptyList());

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(true);
            when(mockEmployeeSession.getCurrentEmployeeName()).thenReturn("John Smith");
            when(mockEmployeeSession.getEmployeeId()).thenReturn("EMP001");

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            String output = getOutput();
            assertContains(output, "Only 50 units available in stock");
        }
    }

    @Test
    @DisplayName("Should handle percentage discount correctly")
    void testLaunch_PercentageDiscount() {
        // Arrange
        String input = "APPLE001\n2\ncheckout\n10%\n300\n\n";
        Scanner scanner = createScannerWithInput(input);

        when(mockItemGateway.getAllItems()).thenReturn(testItems);
        when(mockItemGateway.getItemByCode("APPLE001")).thenReturn(testItems.get(0));

        CartItem cartItem = new CartItem(testItems.get(0), 2);
        List<CartItem> cartItems = Arrays.asList(cartItem);

        when(mockPOS.getCartItems()).thenReturn(cartItems); // Always return cart items
        when(mockPOS.getCartTotal()).thenReturn(251.00);

        Bill mockBill = createMockBill(cartItems, 251.00, 25.10, 300.00, 74.10);
        when(mockPOS.checkout(300.00, 25.10)).thenReturn(mockBill);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(true);
            when(mockEmployeeSession.getCurrentEmployeeName()).thenReturn("John Smith");
            when(mockEmployeeSession.getEmployeeId()).thenReturn("EMP001");

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            String output = getOutput();
            assertContains(output, "Discount applied: Rs. 25.10");
            assertContains(output, "Total after discount: Rs. 225.90");
        }
    }

    @Test
    @DisplayName("Should handle fixed amount discount")
    void testLaunch_FixedDiscount() {
        // Arrange
        String input = "MILK002\n1\ncheckout\n15\n100\n\n";
        Scanner scanner = createScannerWithInput(input);

        when(mockItemGateway.getAllItems()).thenReturn(testItems);
        when(mockItemGateway.getItemByCode("MILK002")).thenReturn(testItems.get(1));

        CartItem cartItem = new CartItem(testItems.get(1), 1);
        List<CartItem> cartItems = Arrays.asList(cartItem);

        when(mockPOS.getCartItems()).thenReturn(cartItems); // Always return cart items
        when(mockPOS.getCartTotal()).thenReturn(85.75);

        Bill mockBill = createMockBill(cartItems, 85.75, 15.00, 100.00, 29.25);
        when(mockPOS.checkout(100.00, 15.00)).thenReturn(mockBill);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(true);
            when(mockEmployeeSession.getCurrentEmployeeName()).thenReturn("John Smith");
            when(mockEmployeeSession.getEmployeeId()).thenReturn("EMP001");

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            String output = getOutput();
            assertContains(output, "Discount applied: Rs. 15.00");
            assertContains(output, "Total after discount: Rs. 70.75");
        }
    }

    @Test
    @DisplayName("Should handle invalid discount format")
    void testLaunch_InvalidDiscount() {
        // Arrange
        String input = "BREAD003\n1\ncheckout\ninvalid\n70\n\n";
        Scanner scanner = createScannerWithInput(input);

        when(mockItemGateway.getAllItems()).thenReturn(testItems);
        when(mockItemGateway.getItemByCode("BREAD003")).thenReturn(testItems.get(2));

        CartItem cartItem = new CartItem(testItems.get(2), 1);
        List<CartItem> cartItems = Arrays.asList(cartItem);

        when(mockPOS.getCartItems()).thenReturn(cartItems); // Always return cart items
        when(mockPOS.getCartTotal()).thenReturn(65.00);

        Bill mockBill = createMockBill(cartItems, 65.00, 0.0, 70.00, 5.00);
        when(mockPOS.checkout(70.00, 0.0)).thenReturn(mockBill);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(true);
            when(mockEmployeeSession.getCurrentEmployeeName()).thenReturn("John Smith");
            when(mockEmployeeSession.getEmployeeId()).thenReturn("EMP001");

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            String output = getOutput();
            assertContains(output, "Invalid discount format. Assuming no discount");
        }
    }

    @Test
    @DisplayName("Should handle invalid cash amount")
    void testLaunch_InvalidCashAmount() {
        // Arrange
        String input = "EGGS004\n1\ncheckout\n0\nabc\n";
        Scanner scanner = createScannerWithInput(input);

        when(mockItemGateway.getAllItems()).thenReturn(testItems);
        when(mockItemGateway.getItemByCode("EGGS004")).thenReturn(testItems.get(3));

        CartItem cartItem = new CartItem(testItems.get(3), 1);
        List<CartItem> cartItems = Arrays.asList(cartItem);

        when(mockPOS.getCartItems()).thenReturn(cartItems); // Always return cart items
        when(mockPOS.getCartTotal()).thenReturn(180.00);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(true);
            when(mockEmployeeSession.getCurrentEmployeeName()).thenReturn("John Smith");
            when(mockEmployeeSession.getEmployeeId()).thenReturn("EMP001");

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            String output = getOutput();
            assertContains(output, "Invalid cash amount entered. Cancelling transaction");
        }
    }

    @Test
    @DisplayName("Should handle POS checkout failure")
    void testLaunch_CheckoutFailure() {
        // Arrange
        String input = "APPLE001\n1\ncheckout\n0\n100\n\n";
        Scanner scanner = createScannerWithInput(input);

        when(mockItemGateway.getAllItems()).thenReturn(testItems);
        when(mockItemGateway.getItemByCode("APPLE001")).thenReturn(testItems.get(0));

        CartItem cartItem = new CartItem(testItems.get(0), 1);
        List<CartItem> cartItems = Arrays.asList(cartItem);

        when(mockPOS.getCartItems()).thenReturn(cartItems); // Always return cart items
        when(mockPOS.getCartTotal()).thenReturn(125.50);
        when(mockPOS.checkout(100.00, 0.0)).thenThrow(new IllegalArgumentException("Insufficient cash"));

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(true);
            when(mockEmployeeSession.getCurrentEmployeeName()).thenReturn("John Smith");
            when(mockEmployeeSession.getEmployeeId()).thenReturn("EMP001");

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            String output = getOutput();
            assertContains(output, "Error: Insufficient cash");
        }
    }

    @Test
    @DisplayName("Should handle empty input for early exit")
    void testLaunch_EmptyInputExit() {
        // Arrange
        Scanner scanner = createScannerWithInput("\n");
        when(mockItemGateway.getAllItems()).thenReturn(testItems);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(true);
            when(mockEmployeeSession.getCurrentEmployeeName()).thenReturn("John Smith");
            when(mockEmployeeSession.getEmployeeId()).thenReturn("EMP001");

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            verify(mockItemGateway).getAllItems();
            // Should exit silently without further processing
        }
    }

    @Test
    @DisplayName("Should handle exception when loading items")
    void testLaunch_ItemLoadingException() {
        // Arrange
        Scanner scanner = createScannerWithInput("\n");
        when(mockItemGateway.getAllItems()).thenThrow(new RuntimeException("Database connection failed"));

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(false);

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            String output = getOutput();
            assertContains(output, "Error loading items: Database connection failed");
        }
    }

    @Test
    @DisplayName("Should display employee header when logged in")
    void testShowEmployeeHeader_LoggedIn() {
        // Arrange
        try (MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {
            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(true);
            when(mockEmployeeSession.getCurrentEmployeeName()).thenReturn("Jane Doe");
            when(mockEmployeeSession.getEmployeeId()).thenReturn("EMP002");

            Scanner scanner = createScannerWithInput("\n");
            when(mockItemGateway.getAllItems()).thenReturn(testItems);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

                // Assert
                String output = getOutput();
                assertContains(output, "Cashier: Jane Doe");
                assertContains(output, "ID: EMP002");
            }
        }
    }

    @Test
    @DisplayName("Should handle long item names with truncation")
    void testTruncateString_LongName() {
        // Arrange
        String input = "LONG001\n1\ncheckout\n0\n100\n\n";
        Scanner scanner = createScannerWithInput(input);

        // Create item with very long name
        ItemDTO longNameItem = new ItemDTO("LONG001",
                "This is an extremely long product name that should be truncated for display purposes in the UI",
                100.00, 10);
        when(mockItemGateway.getAllItems()).thenReturn(Arrays.asList(longNameItem));
        when(mockItemGateway.getItemByCode("LONG001")).thenReturn(longNameItem);

        CartItem cartItem = new CartItem(longNameItem, 1);
        List<CartItem> cartItems = Arrays.asList(cartItem);

        when(mockPOS.getCartItems()).thenReturn(cartItems); // Always return cart items
        when(mockPOS.getCartTotal()).thenReturn(100.00);

        Bill mockBill = createMockBill(cartItems, 100.00, 0.0, 100.00, 0.00);
        when(mockPOS.checkout(100.00, 0.0)).thenReturn(mockBill);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(true);
            when(mockEmployeeSession.getCurrentEmployeeName()).thenReturn("John Smith");
            when(mockEmployeeSession.getEmployeeId()).thenReturn("EMP001");

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            String output = getOutput();
            // Should contain truncated version with "..."
            assertContains(output, "This is an extremely long p...");
        }
    }

    @Test
    @DisplayName("Should handle edge case with exact 30 character name")
    void testTruncateString_Exact30Chars() {
        // Arrange
        String input = "\n";
        Scanner scanner = createScannerWithInput(input);

        // Create item with exactly 30 characters
        ItemDTO exactLengthItem = new ItemDTO("EXACT30",
                "123456789012345678901234567890", // Exactly 30 chars
                50.00, 5);
        when(mockItemGateway.getAllItems()).thenReturn(Arrays.asList(exactLengthItem));

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(false);

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            String output = getOutput();
            // Should contain full name without truncation
            assertContains(output, "123456789012345678901234567890");
            assertFalse(output.contains("12345678901234567890123456..."));
        }
    }

    @Test
    @DisplayName("Should handle employee not logged in")
    void testShowEmployeeHeader_NotLoggedIn() {
        // Arrange
        try (MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {
            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(false);

            Scanner scanner = createScannerWithInput("\n");
            when(mockItemGateway.getAllItems()).thenReturn(testItems);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

                // Assert
                String output = getOutput();
                // Should not contain employee header box
                assertFalse(output.contains("+---------------------------------------------+"));
            }
        }
    }

    @Test
    @DisplayName("Should handle null item name in truncation")
    void testTruncateString_NullName() {
        // Arrange
        String input = "\n";
        Scanner scanner = createScannerWithInput(input);

        // Create item with null name
        ItemDTO nullNameItem = new ItemDTO("NULL001", null, 50.00, 5);
        when(mockItemGateway.getAllItems()).thenReturn(Arrays.asList(nullNameItem));

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<EmployeeSession> mockedEmployeeSession = mockStatic(EmployeeSession.class)) {

            mockedEmployeeSession.when(EmployeeSession::getInstance).thenReturn(mockEmployeeSession);
            when(mockEmployeeSession.isLoggedIn()).thenReturn(false);

            // Act
            CheckoutAndBillingController.launch(scanner, mockItemGateway, mockPOS, testEmployee);

            // Assert
            String output = getOutput();
            // Should handle null gracefully
            assertNotNull(output);
            assertTrue(output.contains("NULL001"));
        }
    }

    // Helper methods
    private Bill createMockBill(List<CartItem> cartItems, double total, double discount,
            double cashTendered, double change) {
        Bill bill = new Bill();
        bill.setSerialNumber(12345);
        bill.setDate(LocalDateTime.of(2025, 6, 5, 14, 30, 0));
        bill.setItems(cartItems);
        bill.setTotal(total);
        bill.setDiscount(discount);
        bill.setCashTendered(cashTendered);
        bill.setChange(change);
        return bill;
    }

    private void assertContains(String content, String expected) {
        assertTrue(content.contains(expected),
                "Content should contain: '" + expected + "'\nActual content:\n" + content);
    }

    private int countOccurrences(String str, String findStr) {
        int lastIndex = 0;
        int count = 0;
        while (lastIndex != -1) {
            lastIndex = str.indexOf(findStr, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += findStr.length();
            }
        }
        return count;
    }
}