package syos.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import syos.model.Bill;
import syos.model.CartItem;
import syos.dto.ItemDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Comprehensive test suite for OnlineBillPrinter
 * Tests all methods with 100% coverage including happy paths and edge cases
 */
@DisplayName("OnlineBillPrinter Tests")
class OnlineBillPrinterTest {

    @TempDir
    Path tempDir;

    private Bill mockBill;
    private CartItem mockCartItem1;
    private CartItem mockCartItem2;
    private ItemDTO mockItem1;
    private ItemDTO mockItem2;

    @BeforeEach
    void setUp() {
        setupMockData();
    }

    private void setupMockData() { // Create mock items
        mockItem1 = new ItemDTO("APPLE001", "Red Delicious Apples", 125.50, 3);

        mockItem2 = new ItemDTO("MILK002", "Fresh Whole Milk 1L", 85.75, 2); // Create mock cart items
        mockCartItem1 = new CartItem(mockItem1, 3);
        mockCartItem2 = new CartItem(mockItem2, 2);

        // Create mock bill
        mockBill = new Bill();
        mockBill.setSerialNumber(42);
        mockBill.setDate(LocalDateTime.of(2025, 6, 5, 14, 30, 0));
        mockBill.setItems(Arrays.asList(mockCartItem1, mockCartItem2));
        mockBill.setTotal(547.75);
        mockBill.setDiscount(0.0);
        mockBill.setCashTendered(600.0);
        mockBill.setChange(52.25);
    }

    @Test
    @DisplayName("Should print online bill successfully with valid data")
    void testPrintOnlineBill_Success() throws IOException {
        // Arrange
        String paymentMethod = "credit_card";
        String username = "john.doe@email.com";
        String expectedFilename = "bills/online/OnlineBill_#42-2025-06-05.txt";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(mockBill))
                    .thenReturn("#42-2025-06-05");

            // Act
            OnlineBillPrinter.print(mockBill, paymentMethod, username);

            // Assert
            File billFile = new File(expectedFilename);
            assertTrue(billFile.exists(), "Bill file should be created");

            String content = Files.readString(billFile.toPath());

            // Verify header content
            assertContains(content, "SYOS ONLINE ORDER BILL");
            assertContains(content, "Serial No    : #42-2025-06-05");
            assertContains(content, "Username     : " + username);
            assertContains(content, "Date/Time    : 2025-06-05 14:30:00");
            assertContains(content, "Payment Mode : CREDIT_CARD");

            // Verify item details
            assertContains(content, "APPLE001");
            assertContains(content, "Red Delicious Apples");
            assertContains(content, "3");
            assertContains(content, "125.50");
            assertContains(content, "376.50"); // 3 * 125.50

            assertContains(content, "MILK002");
            assertContains(content, "Fresh Whole Milk 1L");
            assertContains(content, "2");
            assertContains(content, "85.75");
            assertContains(content, "171.50"); // 2 * 85.75

            // Verify total
            assertContains(content, "TOTAL : Rs.     548.00"); // Calculated total

            // Verify footer content
            assertContains(content, "THIS IS A RESERVATION SLIP");
            assertContains(content, "Please present it when collecting your items.");

            // Cleanup
            billFile.delete();
        }
    }

    @Test
    @DisplayName("Should handle different payment methods correctly")
    void testPrintOnlineBill_DifferentPaymentMethods() throws IOException {
        // Test cases for different payment methods
        String[] paymentMethods = { "debit_card", "paypal", "bank_transfer", "digital_wallet" };
        String username = "jane.smith@email.com";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(mockBill))
                    .thenReturn("#42-2025-06-05");

            for (String paymentMethod : paymentMethods) {
                // Act
                OnlineBillPrinter.print(mockBill, paymentMethod, username);

                // Assert
                File billFile = new File("bills/online/OnlineBill_#42-2025-06-05.txt");
                assertTrue(billFile.exists(), "Bill file should be created for " + paymentMethod);

                String content = Files.readString(billFile.toPath());
                assertContains(content, "Payment Mode : " + paymentMethod.toUpperCase());

                // Cleanup
                billFile.delete();
            }
        }
    }

    @Test
    @DisplayName("Should handle special characters in username")
    void testPrintOnlineBill_SpecialCharactersInUsername() throws IOException {
        // Arrange
        String paymentMethod = "online";
        String username = "user.name+test@domain-name.co.uk";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(mockBill))
                    .thenReturn("#42-2025-06-05");

            // Act
            OnlineBillPrinter.print(mockBill, paymentMethod, username);

            // Assert
            File billFile = new File("bills/online/OnlineBill_#42-2025-06-05.txt");
            assertTrue(billFile.exists(), "Bill file should be created");

            String content = Files.readString(billFile.toPath());
            assertContains(content, "Username     : " + username);

            // Cleanup
            billFile.delete();
        }
    }

    @Test
    @DisplayName("Should handle single item bill")
    void testPrintOnlineBill_SingleItem() throws IOException {
        // Arrange
        Bill singleItemBill = new Bill();
        singleItemBill.setSerialNumber(1);
        singleItemBill.setDate(LocalDateTime.of(2025, 6, 5, 9, 0, 0));
        singleItemBill.setItems(Arrays.asList(mockCartItem1));

        String paymentMethod = "cash";
        String username = "single.user@test.com";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(singleItemBill))
                    .thenReturn("#1-2025-06-05");

            // Act
            OnlineBillPrinter.print(singleItemBill, paymentMethod, username);

            // Assert
            File billFile = new File("bills/online/OnlineBill_#1-2025-06-05.txt");
            assertTrue(billFile.exists(), "Bill file should be created");

            String content = Files.readString(billFile.toPath());
            assertContains(content, "APPLE001");
            assertContains(content, "Red Delicious Apples");

            // Cleanup
            billFile.delete();
        }
    }

    @Test
    @DisplayName("Should handle large quantity items")
    void testPrintOnlineBill_LargeQuantity() throws IOException {
        // Arrange
        CartItem largeQuantityItem = new CartItem(mockItem1, 999);
        Bill largeBill = new Bill();
        largeBill.setSerialNumber(123);
        largeBill.setDate(LocalDateTime.of(2025, 6, 5, 16, 45, 30));
        largeBill.setItems(Arrays.asList(largeQuantityItem));

        String paymentMethod = "credit_card";
        String username = "bulk.buyer@wholesale.com";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(largeBill))
                    .thenReturn("#123-2025-06-05");

            // Act
            OnlineBillPrinter.print(largeBill, paymentMethod, username);

            // Assert
            File billFile = new File("bills/online/OnlineBill_#123-2025-06-05.txt");
            assertTrue(billFile.exists(), "Bill file should be created");

            String content = Files.readString(billFile.toPath());
            assertContains(content, "999"); // Large quantity
            assertContains(content, "125,374.50"); // 999 * 125.50 formatted with comma

            // Cleanup
            billFile.delete();
        }
    }

    @Test
    @DisplayName("Should handle expensive items with proper formatting")
    void testPrintOnlineBill_ExpensiveItems() throws IOException { // Arrange
        ItemDTO expensiveItem = new ItemDTO("LAPTOP001", "Premium Gaming Laptop", 125000.99, 1);

        CartItem expensiveCartItem = new CartItem(expensiveItem, 1);
        Bill expensiveBill = new Bill();
        expensiveBill.setSerialNumber(999);
        expensiveBill.setDate(LocalDateTime.of(2025, 6, 5, 20, 15, 45));
        expensiveBill.setItems(Arrays.asList(expensiveCartItem));

        String paymentMethod = "bank_transfer";
        String username = "premium.customer@corp.com";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(expensiveBill))
                    .thenReturn("#999-2025-06-05");

            // Act
            OnlineBillPrinter.print(expensiveBill, paymentMethod, username);

            // Assert
            File billFile = new File("bills/online/OnlineBill_#999-2025-06-05.txt");
            assertTrue(billFile.exists(), "Bill file should be created");

            String content = Files.readString(billFile.toPath());
            assertContains(content, "LAPTOP001");
            assertContains(content, "Premium Gaming Laptop");
            assertContains(content, "125000.99");
            assertContains(content, "125,000.99"); // Total with comma formatting

            // Cleanup
            billFile.delete();
        }
    }

    @Test
    @DisplayName("Should create directory if it doesn't exist")
    void testPrintOnlineBill_CreateDirectory() throws IOException {
        // Arrange - Delete the directory if it exists
        File billsDir = new File("bills");
        File onlineDir = new File("bills/online");
        if (onlineDir.exists()) {
            // Delete all files in the directory first
            File[] files = onlineDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            onlineDir.delete();
        }
        if (billsDir.exists() && billsDir.listFiles() != null && billsDir.listFiles().length == 0) {
            billsDir.delete();
        }

        String paymentMethod = "digital_wallet";
        String username = "test.directory@example.com";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(mockBill))
                    .thenReturn("#42-2025-06-05");

            // Act
            OnlineBillPrinter.print(mockBill, paymentMethod, username);

            // Assert
            assertTrue(onlineDir.exists(), "Online bills directory should be created");
            File billFile = new File("bills/online/OnlineBill_#42-2025-06-05.txt");
            assertTrue(billFile.exists(), "Bill file should be created");

            // Cleanup
            billFile.delete();
        }
    }

    @Test
    @DisplayName("Should handle items with long names correctly")
    void testPrintOnlineBill_LongItemNames() throws IOException { // Arrange
        ItemDTO longNameItem = new ItemDTO("PROD001",
                "Ultra Premium Organic Free Range Grass Fed Beef Ribeye Steak Extra Large Cut", 2500.75, 2);

        CartItem longNameCartItem = new CartItem(longNameItem, 2);
        Bill longNameBill = new Bill();
        longNameBill.setSerialNumber(555);
        longNameBill.setDate(LocalDateTime.of(2025, 6, 5, 12, 0, 0));
        longNameBill.setItems(Arrays.asList(longNameCartItem));

        String paymentMethod = "credit_card";
        String username = "gourmet.chef@restaurant.com";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(longNameBill))
                    .thenReturn("#555-2025-06-05");

            // Act
            OnlineBillPrinter.print(longNameBill, paymentMethod, username);

            // Assert
            File billFile = new File("bills/online/OnlineBill_#555-2025-06-05.txt");
            assertTrue(billFile.exists(), "Bill file should be created");

            String content = Files.readString(billFile.toPath());
            assertContains(content, "PROD001");
            // Name should be truncated to fit the format (35 characters)
            assertContains(content, "Ultra Premium Organic Free Range Gr");

            // Cleanup
            billFile.delete();
        }
    }

    @Test
    @DisplayName("Should handle multiple items with mixed quantities and prices")
    void testPrintOnlineBill_MultipleItemsMixed() throws IOException { // Arrange
        ItemDTO cheapItem = new ItemDTO("PEN001", "Blue Ballpoint Pen", 15.25, 10);

        ItemDTO moderateItem = new ItemDTO("BOOK001", "Programming Fundamentals", 850.00, 3);

        ItemDTO expensiveItem = new ItemDTO("WATCH001", "Luxury Swiss Watch", 25000.99, 1);

        CartItem cheapCartItem = new CartItem(cheapItem, 10);
        CartItem moderateCartItem = new CartItem(moderateItem, 3);
        CartItem expensiveCartItem = new CartItem(expensiveItem, 1);

        Bill mixedBill = new Bill();
        mixedBill.setSerialNumber(777);
        mixedBill.setDate(LocalDateTime.of(2025, 6, 5, 18, 30, 15));
        mixedBill.setItems(Arrays.asList(cheapCartItem, moderateCartItem, expensiveCartItem));

        String paymentMethod = "bank_transfer";
        String username = "premium.shopper@luxury.com";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(mixedBill))
                    .thenReturn("#777-2025-06-05");

            // Act
            OnlineBillPrinter.print(mixedBill, paymentMethod, username);

            // Assert
            File billFile = new File("bills/online/OnlineBill_#777-2025-06-05.txt");
            assertTrue(billFile.exists(), "Bill file should be created");

            String content = Files.readString(billFile.toPath());

            // Verify all items are present
            assertContains(content, "PEN001");
            assertContains(content, "BOOK001");
            assertContains(content, "WATCH001");

            // Verify quantities
            assertContains(content, "10");
            assertContains(content, "3");
            assertContains(content, "1");

            // Verify subtotals
            assertContains(content, "152.50"); // 10 * 15.25
            assertContains(content, "2550.00"); // 3 * 850.00
            assertContains(content, "25000.99"); // 1 * 25000.99

            // Cleanup
            billFile.delete();
        }
    }

    @Test
    @DisplayName("Should handle zero decimal prices correctly")
    void testPrintOnlineBill_ZeroDecimals() throws IOException { // Arrange
        ItemDTO wholeNumberItem = new ItemDTO("ITEM001", "Whole Number Item", 100.00, 5);

        CartItem wholeNumberCartItem = new CartItem(wholeNumberItem, 5);
        Bill wholeNumberBill = new Bill();
        wholeNumberBill.setSerialNumber(888);
        wholeNumberBill.setDate(LocalDateTime.of(2025, 6, 5, 10, 0, 0));
        wholeNumberBill.setItems(Arrays.asList(wholeNumberCartItem));

        String paymentMethod = "cash";
        String username = "whole.number@test.com";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(wholeNumberBill))
                    .thenReturn("#888-2025-06-05");

            // Act
            OnlineBillPrinter.print(wholeNumberBill, paymentMethod, username);

            // Assert
            File billFile = new File("bills/online/OnlineBill_#888-2025-06-05.txt");
            assertTrue(billFile.exists(), "Bill file should be created");

            String content = Files.readString(billFile.toPath());
            assertContains(content, "100.00");
            assertContains(content, "500.00"); // 5 * 100.00

            // Cleanup
            billFile.delete();
        }
    }

    @Test
    @DisplayName("Should handle IOException during file writing")
    void testPrintOnlineBill_IOException() {
        // Arrange
        String paymentMethod = "credit_card";
        String username = "error.test@example.com";

        // Create a directory where we want to write the file to simulate IO error
        File invalidDir = new File("bills/online/OnlineBill_#42-2025-06-05.txt");
        invalidDir.mkdirs(); // Create as directory instead of file to cause IOException

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(mockBill))
                    .thenReturn("#42-2025-06-05");

            // Act & Assert - Should not throw exception, should handle IOException
            // gracefully
            assertDoesNotThrow(() -> OnlineBillPrinter.print(mockBill, paymentMethod, username));

            // Cleanup
            if (invalidDir.exists()) {
                invalidDir.delete();
            }
        }
    }

    @Test
    @DisplayName("Should handle null payment method")
    void testPrintOnlineBill_NullPaymentMethod() throws IOException {
        // Arrange
        String paymentMethod = null;
        String username = "null.payment@test.com";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(mockBill))
                    .thenReturn("#42-2025-06-05");

            // Act & Assert - Should handle null gracefully
            assertDoesNotThrow(() -> OnlineBillPrinter.print(mockBill, paymentMethod, username));

            // Verify file was created and contains "NULL" for payment method
            File billFile = new File("bills/online/OnlineBill_#42-2025-06-05.txt");
            if (billFile.exists()) {
                String content = Files.readString(billFile.toPath());
                assertContains(content, "Payment Mode : NULL");
                billFile.delete();
            }
        }
    }

    @Test
    @DisplayName("Should handle empty username")
    void testPrintOnlineBill_EmptyUsername() throws IOException {
        // Arrange
        String paymentMethod = "cash";
        String username = "";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(mockBill))
                    .thenReturn("#42-2025-06-05");

            // Act
            OnlineBillPrinter.print(mockBill, paymentMethod, username);

            // Assert
            File billFile = new File("bills/online/OnlineBill_#42-2025-06-05.txt");
            assertTrue(billFile.exists(), "Bill file should be created");

            String content = Files.readString(billFile.toPath());
            assertContains(content, "Username     : ");

            // Cleanup
            billFile.delete();
        }
    }

    @Test
    @DisplayName("Should handle null username")
    void testPrintOnlineBill_NullUsername() throws IOException {
        // Arrange
        String paymentMethod = "debit_card";
        String username = null;

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(mockBill))
                    .thenReturn("#42-2025-06-05");

            // Act & Assert - Should handle null gracefully
            assertDoesNotThrow(() -> OnlineBillPrinter.print(mockBill, paymentMethod, username));

            // Verify file was created
            File billFile = new File("bills/online/OnlineBill_#42-2025-06-05.txt");
            if (billFile.exists()) {
                String content = Files.readString(billFile.toPath());
                assertContains(content, "Username     : null");
                billFile.delete();
            }
        }
    }

    @Test
    @DisplayName("Should handle bill with no items")
    void testPrintOnlineBill_NoItems() throws IOException {
        // Arrange
        Bill emptyBill = new Bill();
        emptyBill.setSerialNumber(999);
        emptyBill.setDate(LocalDateTime.of(2025, 6, 5, 12, 0, 0));
        emptyBill.setItems(new ArrayList<>());

        String paymentMethod = "online";
        String username = "empty.cart@test.com";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(emptyBill))
                    .thenReturn("#999-2025-06-05");

            // Act
            OnlineBillPrinter.print(emptyBill, paymentMethod, username);

            // Assert
            File billFile = new File("bills/online/OnlineBill_#999-2025-06-05.txt");
            assertTrue(billFile.exists(), "Bill file should be created");

            String content = Files.readString(billFile.toPath());
            assertContains(content, "SYOS ONLINE ORDER BILL");
            assertContains(content, "TOTAL : Rs.       0.00"); // Should show 0.00 total
            assertContains(content, "THIS IS A RESERVATION SLIP");

            // Cleanup
            billFile.delete();
        }
    }

    @Test
    @DisplayName("Should handle items with zero prices")
    void testPrintOnlineBill_ZeroPriceItems() throws IOException {
        // Arrange
        ItemDTO freeItem = new ItemDTO("FREE001", "Free Sample Item", 0.00, 1);
        CartItem freeCartItem = new CartItem(freeItem, 5);

        Bill freeBill = new Bill();
        freeBill.setSerialNumber(123);
        freeBill.setDate(LocalDateTime.of(2025, 6, 5, 10, 0, 0));
        freeBill.setItems(Arrays.asList(freeCartItem));

        String paymentMethod = "free";
        String username = "free.user@test.com";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(freeBill))
                    .thenReturn("#123-2025-06-05");

            // Act
            OnlineBillPrinter.print(freeBill, paymentMethod, username);

            // Assert
            File billFile = new File("bills/online/OnlineBill_#123-2025-06-05.txt");
            assertTrue(billFile.exists(), "Bill file should be created");

            String content = Files.readString(billFile.toPath());
            assertContains(content, "FREE001");
            assertContains(content, "Free Sample Item");
            assertContains(content, "0.00");
            assertContains(content, "TOTAL : Rs.       0.00");

            // Cleanup
            billFile.delete();
        }
    }

    @Test
    @DisplayName("Should format very long item names correctly")
    void testPrintOnlineBill_VeryLongItemName() throws IOException {
        // Arrange - Name longer than 35 characters
        String veryLongName = "This is an extremely long product name that exceeds the maximum character limit for display purposes";
        ItemDTO longNameItem = new ItemDTO("LONG001", veryLongName, 99.99, 1);
        CartItem longNameCartItem = new CartItem(longNameItem, 1);

        Bill longNameBill = new Bill();
        longNameBill.setSerialNumber(444);
        longNameBill.setDate(LocalDateTime.of(2025, 6, 5, 15, 30, 0));
        longNameBill.setItems(Arrays.asList(longNameCartItem));

        String paymentMethod = "credit_card";
        String username = "long.name@test.com";

        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(longNameBill))
                    .thenReturn("#444-2025-06-05");

            // Act
            OnlineBillPrinter.print(longNameBill, paymentMethod, username);

            // Assert
            File billFile = new File("bills/online/OnlineBill_#444-2025-06-05.txt");
            assertTrue(billFile.exists(), "Bill file should be created");

            String content = Files.readString(billFile.toPath());
            assertContains(content, "LONG001");
            // Name should be truncated/formatted to fit 35 character limit
            assertTrue(content.contains(veryLongName.substring(0, Math.min(35, veryLongName.length()))));

            // Cleanup
            billFile.delete();
        }
    }

    // Helper method to assert that content contains expected text
    private void assertContains(String content, String expected) {
        assertTrue(content.contains(expected),
                "Content should contain: '" + expected + "'\nActual content:\n" + content);
    }
}
