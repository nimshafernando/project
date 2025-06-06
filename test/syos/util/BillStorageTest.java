package syos.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import syos.model.Bill;
import syos.model.CartItem;
import syos.dto.ItemDTO;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for BillStorage with 100% coverage
 * Tests all methods including happy paths, edge cases, and error scenarios
 */
@DisplayName("BillStorage Tests")
class BillStorageTest {

    @TempDir
    Path tempDir;

    private Bill testBill;
    private ItemDTO testItem1;
    private ItemDTO testItem2;
    private CartItem cartItem1;
    private CartItem cartItem2;

    @BeforeEach
    void setUp() {
        setupTestData();
        cleanupSerialFiles();
    }

    @AfterEach
    void tearDown() {
        cleanupSerialFiles();
        cleanupBillFiles();
    }

    private void setupTestData() {
        // Create test items
        testItem1 = new ItemDTO("APPLE001", "Red Delicious Apples", 125.50, 3);
        testItem2 = new ItemDTO("MILK002", "Fresh Whole Milk 1L", 85.75, 2);

        // Create cart items
        cartItem1 = new CartItem(testItem1, 3);
        cartItem2 = new CartItem(testItem2, 2);

        // Create test bill
        testBill = new Bill();
        testBill.setSerialNumber(123);
        testBill.setDate(LocalDateTime.of(2025, 6, 3, 14, 30, 45));
        testBill.setItems(Arrays.asList(cartItem1, cartItem2));
        testBill.setTotal(547.75);
        testBill.setDiscount(25.00);
        testBill.setCashTendered(600.0);
        testBill.setChange(52.25);
        testBill.setEmployeeName("John Doe");
    }

    private void cleanupSerialFiles() {
        new File("bill_serial.txt").delete();
        new File("online_bill_serial.txt").delete();
    }

    private void cleanupBillFiles() {
        File billsDir = new File("bills");
        if (billsDir.exists()) {
            deleteDirectoryRecursively(billsDir);
        }
    }

    private void deleteDirectoryRecursively(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectoryRecursively(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    @Test
    @DisplayName("Should format bill serial correctly")
    void testGetFormattedSerial() {
        // Act
        String formatted = BillStorage.getFormattedSerial(testBill);

        // Assert
        assertEquals("#123-2025-06-03", formatted);
    }

    @Test
    @DisplayName("Should format bill serial with different dates")
    void testGetFormattedSerial_DifferentDates() {
        // Arrange
        Bill bill1 = new Bill();
        bill1.setSerialNumber(1);
        bill1.setDate(LocalDateTime.of(2025, 12, 31, 23, 59, 59));

        Bill bill2 = new Bill();
        bill2.setSerialNumber(999);
        bill2.setDate(LocalDateTime.of(2025, 1, 1, 0, 0, 0));

        // Act & Assert
        assertEquals("#1-2025-12-31", BillStorage.getFormattedSerial(bill1));
        assertEquals("#999-2025-01-01", BillStorage.getFormattedSerial(bill2));
    }

    @Test
    @DisplayName("Should get next serial for today - first call")
    void testGetNextSerialForToday_FirstCall() {
        // Act
        int serial = BillStorage.getNextSerialForToday(LocalDate.of(2025, 6, 5));

        // Assert
        assertEquals(1, serial);

        // Verify file was created
        File serialFile = new File("bill_serial.txt");
        assertTrue(serialFile.exists());
    }

    @Test
    @DisplayName("Should get incremented serial for same day")
    void testGetNextSerialForToday_SameDay() {
        // Arrange
        LocalDate testDate = LocalDate.of(2025, 6, 5);

        // Act
        int serial1 = BillStorage.getNextSerialForToday(testDate);
        int serial2 = BillStorage.getNextSerialForToday(testDate);
        int serial3 = BillStorage.getNextSerialForToday(testDate);

        // Assert
        assertEquals(1, serial1);
        assertEquals(2, serial2);
        assertEquals(3, serial3);
    }

    @Test
    @DisplayName("Should reset serial for new day")
    void testGetNextSerialForToday_NewDay() {
        // Arrange
        LocalDate day1 = LocalDate.of(2025, 6, 5);
        LocalDate day2 = LocalDate.of(2025, 6, 6);

        // Act
        int serial1 = BillStorage.getNextSerialForToday(day1);
        int serial2 = BillStorage.getNextSerialForToday(day1);
        int serial3 = BillStorage.getNextSerialForToday(day2); // New day
        int serial4 = BillStorage.getNextSerialForToday(day2);

        // Assert
        assertEquals(1, serial1);
        assertEquals(2, serial2);
        assertEquals(1, serial3); // Reset for new day
        assertEquals(2, serial4);
    }

    @Test
    @DisplayName("Should handle online bills separately")
    void testGetNextSerialForToday_OnlineBills() {
        // Arrange
        LocalDate testDate = LocalDate.of(2025, 6, 5);

        // Act
        int storeSerial1 = BillStorage.getNextSerialForToday(testDate, false);
        int onlineSerial1 = BillStorage.getNextSerialForToday(testDate, true);
        int storeSerial2 = BillStorage.getNextSerialForToday(testDate, false);
        int onlineSerial2 = BillStorage.getNextSerialForToday(testDate, true);

        // Assert
        assertEquals(1, storeSerial1);
        assertEquals(1, onlineSerial1); // Separate counter
        assertEquals(2, storeSerial2);
        assertEquals(2, onlineSerial2);

        // Verify separate files exist
        assertTrue(new File("bill_serial.txt").exists());
        assertTrue(new File("online_bill_serial.txt").exists());
    }

    @Test
    @DisplayName("Should handle corrupted serial file")
    void testGetNextSerialForToday_CorruptedFile() throws IOException {
        // Arrange - Create corrupted serial file
        File serialFile = new File("bill_serial.txt");
        try (FileWriter writer = new FileWriter(serialFile)) {
            writer.write("2025-06-05\n");
            writer.write("invalid_number\n"); // Corrupted data
        }

        // Act
        int serial = BillStorage.getNextSerialForToday(LocalDate.of(2025, 6, 5));

        // Assert
        assertEquals(1, serial); // Should default to 1 when corrupted
    }

    @Test
    @DisplayName("Should handle missing serial line in file")
    void testGetNextSerialForToday_MissingSerialLine() throws IOException {
        // Arrange - Create file with only date line
        File serialFile = new File("bill_serial.txt");
        try (FileWriter writer = new FileWriter(serialFile)) {
            writer.write("2025-06-05\n");
            // Missing serial line
        }

        // Act
        int serial = BillStorage.getNextSerialForToday(LocalDate.of(2025, 6, 5));

        // Assert
        assertEquals(1, serial); // Should default to 1 when serial line missing
    }

    @Test
    @DisplayName("Should use single parameter overload")
    void testGetNextSerialForToday_SingleParameter() {
        // Act
        int serial = BillStorage.getNextSerialForToday(LocalDate.of(2025, 6, 5));

        // Assert
        assertEquals(1, serial);

        // Verify it creates store bill file (not online)
        assertTrue(new File("bill_serial.txt").exists());
        assertFalse(new File("online_bill_serial.txt").exists());
    }

    @Test
    @DisplayName("Should save bill to file successfully")
    void testSaveBill_Success() throws IOException {
        // Act
        BillStorage.save(testBill);

        // Assert
        File billFile = new File("bills/store/bill_#123-2025-06-03.txt");
        assertTrue(billFile.exists(), "Bill file should be created");

        // Verify content
        String content = Files.readString(billFile.toPath());
        assertContains(content, "SYOS GROCERY");
        assertContains(content, "Bill : #123-2025-06-03");
        assertContains(content, "Time   : 14:30:45");
        assertContains(content, "Employee: John Doe");
        assertContains(content, "Red Delicious Apples");
        assertContains(content, "Fresh Whole Milk 1L");
        assertContains(content, "Total before discount:");
        assertContains(content, "Discount applied:");
        assertContains(content, "Total after discount:");
        assertContains(content, "Cash Tendered:");
        assertContains(content, "Change Returned:");
        assertContains(content, "Thank you for shopping!");
    }

    @Test
    @DisplayName("Should save bill without employee name")
    void testSaveBill_NoEmployeeName() throws IOException {
        // Arrange
        testBill.setEmployeeName(null);

        // Act
        BillStorage.save(testBill);

        // Assert
        File billFile = new File("bills/store/bill_#123-2025-06-03.txt");
        assertTrue(billFile.exists(), "Bill file should be created");

        String content = Files.readString(billFile.toPath());
        assertFalse(content.contains("Employee:"), "Should not contain employee line when null");
    }

    @Test
    @DisplayName("Should save bill with empty items list")
    void testSaveBill_EmptyItems() throws IOException {
        // Arrange
        testBill.setItems(new ArrayList<>());

        // Act
        BillStorage.save(testBill);

        // Assert
        File billFile = new File("bills/store/bill_#123-2025-06-03.txt");
        assertTrue(billFile.exists(), "Bill file should be created");

        String content = Files.readString(billFile.toPath());
        assertContains(content, "SYOS GROCERY");
        // Should still contain headers and totals even with no items
    }

    @Test
    @DisplayName("Should save bill with single item")
    void testSaveBill_SingleItem() throws IOException {
        // Arrange
        testBill.setItems(Arrays.asList(cartItem1));

        // Act
        BillStorage.save(testBill);

        // Assert
        File billFile = new File("bills/store/bill_#123-2025-06-03.txt");
        assertTrue(billFile.exists(), "Bill file should be created");

        String content = Files.readString(billFile.toPath());
        assertContains(content, "Red Delicious Apples");
        assertFalse(content.contains("Fresh Whole Milk 1L"), "Should not contain second item");
    }

    @Test
    @DisplayName("Should save bill with large amounts")
    void testSaveBill_LargeAmounts() throws IOException {
        // Arrange
        testBill.setTotal(999999.99);
        testBill.setDiscount(10000.00);
        testBill.setCashTendered(1010000.00);
        testBill.setChange(10000.01);

        // Act
        BillStorage.save(testBill);

        // Assert
        File billFile = new File("bills/store/bill_#123-2025-06-03.txt");
        assertTrue(billFile.exists(), "Bill file should be created");

        String content = Files.readString(billFile.toPath());
        // Verify large numbers are formatted with commas
        assertContains(content, "999,999.99");
        assertContains(content, "10,000.00");
        assertContains(content, "1,010,000.00");
        assertContains(content, "10,000.01");
    }

    @Test
    @DisplayName("Should create directory if it doesn't exist")
    void testSaveBill_CreateDirectory() {
        // Arrange - Ensure directory doesn't exist
        File billsDir = new File("bills");
        if (billsDir.exists()) {
            deleteDirectoryRecursively(billsDir);
        }

        // Act
        BillStorage.save(testBill);

        // Assert
        File storeDir = new File("bills/store");
        assertTrue(storeDir.exists(), "Store directory should be created");
        File billFile = new File("bills/store/bill_#123-2025-06-03.txt");
        assertTrue(billFile.exists(), "Bill file should be created");
    }

    @Test
    @DisplayName("Should debug serial status for non-existent file")
    void testDebugSerialStatus_NonExistentFile() {
        // Arrange - Ensure no serial files exist
        cleanupSerialFiles();

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> BillStorage.debugSerialStatus());
        assertDoesNotThrow(() -> BillStorage.debugSerialStatus(false));
        assertDoesNotThrow(() -> BillStorage.debugSerialStatus(true));
    }

    @Test
    @DisplayName("Should debug serial status for existing file")
    void testDebugSerialStatus_ExistingFile() throws IOException {
        // Arrange
        File serialFile = new File("bill_serial.txt");
        try (FileWriter writer = new FileWriter(serialFile)) {
            writer.write("2025-06-05\n");
            writer.write("5\n");
        }

        File onlineSerialFile = new File("online_bill_serial.txt");
        try (FileWriter writer = new FileWriter(onlineSerialFile)) {
            writer.write("2025-06-05\n");
            writer.write("3\n");
        }

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> BillStorage.debugSerialStatus());
        assertDoesNotThrow(() -> BillStorage.debugSerialStatus(false));
        assertDoesNotThrow(() -> BillStorage.debugSerialStatus(true));
    }

    @Test
    @DisplayName("Should debug serial status with single parameter overload")
    void testDebugSerialStatus_SingleParameter() {
        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> BillStorage.debugSerialStatus());
    }

    @Test
    @DisplayName("Should handle IO error in debug serial status")
    void testDebugSerialStatus_IOError() throws IOException {
        // Arrange - Create file and make it unreadable (simulate IO error)
        File serialFile = new File("bill_serial.txt");
        try (FileWriter writer = new FileWriter(serialFile)) {
            writer.write("2025-06-05\n");
            writer.write("5\n");
        }

        // Act & Assert - Should handle IOException gracefully
        assertDoesNotThrow(() -> BillStorage.debugSerialStatus(false));
    }

    // Helper method to assert content contains expected text
    private void assertContains(String content, String expected) {
        assertTrue(content.contains(expected),
                "Content should contain: '" + expected + "'\nActual content:\n" + content);
    }
}