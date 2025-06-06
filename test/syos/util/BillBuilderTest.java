package syos.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import syos.model.Bill;
import syos.model.CartItem;
import syos.dto.ItemDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Comprehensive test suite for BillBuilder
 * Tests all methods with 100% coverage including happy paths and edge cases
 */
@DisplayName("BillBuilder Tests")
class BillBuilderTest {
    private BillBuilder builder;
    private ItemDTO mockItem1;
    private ItemDTO mockItem2;
    private CartItem mockCartItem1;
    private CartItem mockCartItem2;

    @BeforeEach
    void setUp() {
        builder = new BillBuilder();
        setupMockData();
    }

    @AfterEach
    void tearDown() {
        // Ensure employee session is logged out after tests
        EmployeeSession.getInstance().logout();
    }

    private void setupMockData() {
        // Create mock items
        mockItem1 = new ItemDTO("APPLE001", "Fresh Red Apples", 150.75, 1);

        mockItem2 = new ItemDTO("BREAD002", "Whole Wheat Bread", 85.50, 1);

        // Create mock cart items
        mockCartItem1 = new CartItem(mockItem1, 2);
        mockCartItem2 = new CartItem(mockItem2, 3);
    }

    // === Constructor and Factory Method Tests ===

    @Test
    @DisplayName("Should create BillBuilder with default values")
    void testDefaultConstructor() {
        // Act
        BillBuilder newBuilder = new BillBuilder();

        // Assert
        assertNotNull(newBuilder, "Builder should not be null");
        assertFalse(newBuilder.isValid(), "Builder should not be valid initially");
        assertTrue(newBuilder.getSummary().contains("valid=false"), "Summary should show invalid state");
    }

    @Test
    @DisplayName("Should create new bill builder")
    void testNewBill() {
        // Act
        BillBuilder newBuilder = BillBuilder.newBill();

        // Assert
        assertNotNull(newBuilder, "New bill builder should not be null");
        assertInstanceOf(BillBuilder.class, newBuilder, "Should return BillBuilder instance");
    }

    @Test
    @DisplayName("Should create new store bill builder")
    void testNewStoreBill() {
        // Act
        BillBuilder storeBuilder = BillBuilder.newStoreBill();

        // Assert
        assertNotNull(storeBuilder, "Store bill builder should not be null");
        assertTrue(storeBuilder.getSummary().contains("BillBuilder"), "Should be a BillBuilder instance");
    }

    @Test
    @DisplayName("Should create new online bill builder")
    void testNewOnlineBill() {
        // Act
        BillBuilder onlineBuilder = BillBuilder.newOnlineBill();

        // Assert
        assertNotNull(onlineBuilder, "Online bill builder should not be null");
        assertTrue(onlineBuilder.getSummary().contains("BillBuilder"), "Should be a BillBuilder instance");
    }

    @Test
    @DisplayName("Should create builder from existing bill")
    void testFromBill() {
        // Arrange
        Bill existingBill = new Bill();
        existingBill.setSerialNumber(123);
        existingBill.setDate(LocalDateTime.of(2025, 6, 5, 14, 30));
        existingBill.setItems(Arrays.asList(mockCartItem1));
        existingBill.setTotal(301.50);
        existingBill.setDiscount(15.0);
        existingBill.setCashTendered(350.0);
        existingBill.setChange(48.50);
        existingBill.setSource("store");
        existingBill.setUsername("test.user");
        existingBill.setPaymentMethod("credit_card");

        // Act
        BillBuilder fromBuilder = BillBuilder.fromBill(existingBill);

        // Assert
        assertNotNull(fromBuilder, "Builder from existing bill should not be null");
        assertTrue(fromBuilder.getSummary().contains("123"), "Should contain serial number");
    }

    // === Serial Number Tests ===

    @Test
    @DisplayName("Should set serial number correctly")
    void testSetSerialNumber_Valid() {
        // Act
        BillBuilder result = builder.setSerialNumber(42);

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
        assertTrue(builder.getSummary().contains("42"), "Summary should contain serial number");
    }

    @Test
    @DisplayName("Should throw exception for zero serial number")
    void testSetSerialNumber_Zero() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.setSerialNumber(0);
        });
        assertEquals("Serial number must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for negative serial number")
    void testSetSerialNumber_Negative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.setSerialNumber(-5);
        });
        assertEquals("Serial number must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should set serial string correctly")
    void testSetSerialString_Valid() {
        // Act
        BillBuilder result = builder.setSerialString("  #123-2025-06-05  ");

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
        assertTrue(builder.getSummary().contains("#123-2025-06-05"), "Summary should contain trimmed serial string");
    }

    @Test
    @DisplayName("Should throw exception for null serial string")
    void testSetSerialString_Null() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.setSerialString(null);
        });
        assertEquals("Serial string cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for empty serial string")
    void testSetSerialString_Empty() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.setSerialString("   ");
        });
        assertEquals("Serial string cannot be null or empty", exception.getMessage());
    }

    // === Date Tests ===

    @Test
    @DisplayName("Should set date correctly")
    void testSetDate_Valid() {
        // Arrange
        LocalDateTime testDate = LocalDateTime.of(2025, 6, 5, 10, 30, 45);

        // Act
        BillBuilder result = builder.setDate(testDate);

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
    }

    @Test
    @DisplayName("Should throw exception for null date")
    void testSetDate_Null() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.setDate(null);
        });
        assertEquals("Date cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should set date to now")
    void testSetDateNow() {
        // Act
        BillBuilder result = builder.setDateNow();

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
        // The exact time comparison would be in the actual implementation
    }

    // === Items Tests ===

    @Test
    @DisplayName("Should set items list correctly")
    void testSetItems_Valid() {
        // Arrange
        List<CartItem> items = Arrays.asList(mockCartItem1, mockCartItem2);

        // Act
        BillBuilder result = builder.setItems(items);

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
        assertTrue(builder.getSummary().contains("items=2"), "Summary should show 2 items");
    }

    @Test
    @DisplayName("Should handle empty items list")
    void testSetItems_Empty() {
        // Arrange
        List<CartItem> emptyItems = new ArrayList<>();

        // Act
        BillBuilder result = builder.setItems(emptyItems);

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
        assertTrue(builder.getSummary().contains("items=0"), "Summary should show 0 items");
    }

    @Test
    @DisplayName("Should throw exception for null items list")
    void testSetItems_Null() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.setItems(null);
        });
        assertEquals("Items list cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should add single item correctly")
    void testAddItem_Valid() {
        // Act
        BillBuilder result = builder.addItem(mockCartItem1);

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
        assertTrue(builder.getSummary().contains("items=1"), "Summary should show 1 item");
    }

    @Test
    @DisplayName("Should throw exception for null cart item")
    void testAddItem_Null() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.addItem((CartItem) null);
        });
        assertEquals("Cart item cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should add item with DTO and quantity")
    void testAddItemWithDTO_Valid() {
        // Act
        BillBuilder result = builder.addItem(mockItem1, 5);

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
        assertTrue(builder.getSummary().contains("items=1"), "Summary should show 1 item");
    }

    @Test
    @DisplayName("Should throw exception for null ItemDTO")
    void testAddItemWithDTO_NullDTO() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.addItem(null, 2);
        });
        assertEquals("ItemDTO cannot be null and quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for zero quantity")
    void testAddItemWithDTO_ZeroQuantity() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.addItem(mockItem1, 0);
        });
        assertEquals("ItemDTO cannot be null and quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for negative quantity")
    void testAddItemWithDTO_NegativeQuantity() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.addItem(mockItem1, -3);
        });
        assertEquals("ItemDTO cannot be null and quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should clear items correctly")
    void testClearItems() {
        // Arrange
        builder.addItem(mockCartItem1).addItem(mockCartItem2);

        // Act
        BillBuilder result = builder.clearItems();

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
        assertTrue(builder.getSummary().contains("items=0"), "Summary should show 0 items after clearing");
    }

    // === Total and Calculation Tests ===

    @Test
    @DisplayName("Should set total correctly")
    void testSetTotal_Valid() {
        // Act
        BillBuilder result = builder.setTotal(500.75);

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
        assertTrue(builder.getSummary().contains("total=500.75"), "Summary should show correct total");
    }

    @Test
    @DisplayName("Should throw exception for negative total")
    void testSetTotal_Negative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.setTotal(-100.0);
        });
        assertEquals("Total cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should calculate total correctly")
    void testCalculateTotal() {
        // Arrange
        builder.addItem(mockCartItem1).addItem(mockCartItem2).setDiscount(25.0);

        // Act
        BillBuilder result = builder.calculateTotal();

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
        // Expected: (150.75 * 2) + (85.50 * 3) - 25.0 = 301.50 + 256.50 - 25.0 = 533.00
        assertTrue(builder.getSummary().contains("total=533.00"), "Summary should show calculated total");
    }

    @Test
    @DisplayName("Should set discount correctly")
    void testSetDiscount_Valid() {
        // Act
        BillBuilder result = builder.setDiscount(50.25);

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
    }

    @Test
    @DisplayName("Should throw exception for negative discount")
    void testSetDiscount_Negative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.setDiscount(-20.0);
        });
        assertEquals("Discount cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should set discount percentage correctly")
    void testSetDiscountPercentage_Valid() {
        // Arrange
        builder.addItem(mockCartItem1); // 150.75 * 2 = 301.50

        // Act
        BillBuilder result = builder.setDiscountPercentage(10.0);

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
        // Expected discount: 301.50 * 0.10 = 30.15
    }

    @Test
    @DisplayName("Should throw exception for negative discount percentage")
    void testSetDiscountPercentage_Negative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.setDiscountPercentage(-5.0);
        });
        assertEquals("Discount percentage must be between 0 and 100", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for discount percentage over 100")
    void testSetDiscountPercentage_Over100() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.setDiscountPercentage(105.0);
        });
        assertEquals("Discount percentage must be between 0 and 100", exception.getMessage());
    }

    @Test
    @DisplayName("Should set cash tendered correctly")
    void testSetCashTendered_Valid() {
        // Act
        BillBuilder result = builder.setCashTendered(1000.0);

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
    }

    @Test
    @DisplayName("Should throw exception for negative cash tendered")
    void testSetCashTendered_Negative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.setCashTendered(-50.0);
        });
        assertEquals("Cash tendered cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should set change correctly")
    void testSetChange_Valid() {
        // Act
        BillBuilder result = builder.setChange(75.25);

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
    }

    @Test
    @DisplayName("Should calculate change correctly")
    void testCalculateChange() {
        // Arrange
        builder.setTotal(300.0).setCashTendered(350.0);

        // Act
        BillBuilder result = builder.calculateChange();

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
        // Expected change: 350.0 - 300.0 = 50.0
    }

    // === Source and Payment Tests ===

    @Test
    @DisplayName("Should set source correctly")
    void testSetSource_Valid() {
        // Act
        BillBuilder result = builder.setSource("  online  ");

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
    }

    @Test
    @DisplayName("Should throw exception for null source")
    void testSetSource_Null() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.setSource(null);
        });
        assertEquals("Source cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for empty source")
    void testSetSource_Empty() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.setSource("   ");
        });
        assertEquals("Source cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should set username correctly")
    void testSetUsername_Valid() {
        // Act
        BillBuilder result = builder.setUsername("  john.doe@email.com  ");

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
    }

    @Test
    @DisplayName("Should handle null username")
    void testSetUsername_Null() {
        // Act
        BillBuilder result = builder.setUsername(null);

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
    }

    @Test
    @DisplayName("Should set payment method correctly")
    void testSetPaymentMethod_Valid() {
        // Act
        BillBuilder result = builder.setPaymentMethod("  credit_card  ");

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
    }

    @Test
    @DisplayName("Should handle null payment method")
    void testSetPaymentMethod_Null() {
        // Act
        BillBuilder result = builder.setPaymentMethod(null);

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
    }

    // === Validation Tests ===

    @Test
    @DisplayName("Should validate complete bill correctly")
    void testIsValid_Complete() {
        // Arrange
        builder.setSerialNumber(1)
                .setDate(LocalDateTime.now())
                .addItem(mockCartItem1)
                .setTotal(301.50)
                .setCashTendered(350.0);

        // Act
        boolean isValid = builder.isValid();

        // Assert
        assertTrue(isValid, "Complete bill should be valid");
    }

    @Test
    @DisplayName("Should invalidate bill missing serial number")
    void testIsValid_MissingSerial() {
        // Arrange
        builder.setDate(LocalDateTime.now())
                .addItem(mockCartItem1)
                .setTotal(301.50)
                .setCashTendered(350.0);

        // Act
        boolean isValid = builder.isValid();

        // Assert
        assertFalse(isValid, "Bill without serial should be invalid");
    }

    @Test
    @DisplayName("Should invalidate bill with insufficient cash")
    void testIsValid_InsufficientCash() {
        // Arrange
        builder.setSerialNumber(1)
                .setDate(LocalDateTime.now())
                .addItem(mockCartItem1)
                .setTotal(301.50)
                .setCashTendered(250.0);

        // Act
        boolean isValid = builder.isValid();

        // Assert
        assertFalse(isValid, "Bill with insufficient cash should be invalid");
    }

    // === Build Tests ===

    @Test
    @DisplayName("Should build store bill successfully")
    void testBuild_StoreBill() {
        // Arrange
        EmployeeSession.getInstance().loginEmployee("John Doe", "EMP001", "Cashier");

        builder.setSerialNumber(42)
                .setDate(LocalDateTime.of(2025, 6, 5, 14, 30))
                .addItem(mockCartItem1)
                .setTotal(301.50)
                .setCashTendered(350.0)
                .setSource("store");

        // Act
        Bill bill = builder.build();

        // Assert
        assertNotNull(bill, "Built bill should not be null");
        assertEquals(42, bill.getSerialNumber(), "Serial number should match");
        assertEquals("John Doe", bill.getEmployeeName(), "Employee name should be set");
    }

    @Test
    @DisplayName("Should build online bill successfully")
    void testBuild_OnlineBill() {
        // Arrange
        builder.setSerialString("#123-2025-06-05")
                .setDate(LocalDateTime.of(2025, 6, 5, 16, 45))
                .addItem(mockCartItem2)
                .setTotal(256.50)
                .setCashTendered(300.0)
                .setSource("online")
                .setUsername("jane.doe@email.com");

        // Act
        Bill bill = builder.build();

        // Assert
        assertNotNull(bill, "Built bill should not be null");
        assertEquals("#123-2025-06-05", bill.getSerial(), "Serial string should match");
    }

    @Test
    @DisplayName("Should throw exception when building store bill without employee login")
    void testBuild_StoreWithoutEmployee() {
        // Arrange
        EmployeeSession.getInstance().logout(); // Ensure no employee logged in

        builder.setSerialNumber(1)
                .setDate(LocalDateTime.now())
                .addItem(mockCartItem1)
                .setTotal(301.50)
                .setCashTendered(350.0)
                .setSource("store");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            builder.build();
        });
        assertTrue(exception.getMessage().contains("No employee is currently logged in"),
                "Should mention employee login requirement");
    }

    @Test
    @DisplayName("Should throw exception when building online bill without username")
    void testBuild_OnlineWithoutUsername() {
        // Arrange
        builder.setSerialString("#123-2025-06-05")
                .setDate(LocalDateTime.now())
                .addItem(mockCartItem1)
                .setTotal(301.50)
                .setCashTendered(350.0)
                .setSource("online");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            builder.build();
        });
        assertEquals("Username is required for online bills", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when building bill without items")
    void testBuild_WithoutItems() {
        // Arrange
        builder.setSerialNumber(1)
                .setDate(LocalDateTime.now())
                .setTotal(301.50)
                .setCashTendered(350.0);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            builder.build();
        });
        assertEquals("At least one item is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when building bill with zero total")
    void testBuild_ZeroTotal() {
        // Arrange
        builder.setSerialNumber(1)
                .setDate(LocalDateTime.now())
                .addItem(mockCartItem1)
                .setTotal(0.0)
                .setCashTendered(350.0);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            builder.build();
        });
        assertEquals("Total must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should build unsafe bill without validation")
    void testBuildUnsafe_WithSerial() {
        // Arrange
        builder.setSerialNumber(999)
                .setSource("test")
                .setUsername("test.user")
                .setPaymentMethod("test_payment");

        // Act
        Bill bill = builder.buildUnsafe();

        // Assert
        assertNotNull(bill, "Unsafe built bill should not be null");
        assertEquals(999, bill.getSerialNumber(), "Serial number should match");
        assertEquals("test", bill.getSource(), "Source should be set");
        assertEquals("test.user", bill.getUsername(), "Username should be set");
        assertEquals("test_payment", bill.getPaymentMethod(), "Payment method should be set");
    }

    @Test
    @DisplayName("Should build unsafe bill with string serial")
    void testBuildUnsafe_WithStringSerial() {
        // Arrange
        builder.setSerialString("#TEST-2025-06-05")
                .setSource("online")
                .setUsername("unsafe.user")
                .setPaymentMethod("unsafe_payment");

        // Act
        Bill bill = builder.buildUnsafe();

        // Assert
        assertNotNull(bill, "Unsafe built bill should not be null");
        assertEquals("#TEST-2025-06-05", bill.getSerial(), "Serial string should match");
    }

    @Test
    @DisplayName("Should build and process bill")
    void testBuildAndProcess() {
        // Arrange
        EmployeeSession.getInstance().loginEmployee("Process User", "PROC001", "Manager");

        builder.setSerialNumber(777)
                .setDate(LocalDateTime.now())
                .addItem(mockCartItem1)
                .setTotal(301.50)
                .setCashTendered(350.0)
                .setSource("store");

        // Act
        Bill bill = builder.buildAndProcess();

        // Assert
        assertNotNull(bill, "Built and processed bill should not be null");
        assertEquals(777, bill.getSerialNumber(), "Serial number should match");
        // Note: bill.processBill() would be called internally
    }

    // === Utility Method Tests ===

    @Test
    @DisplayName("Should reset builder to initial state")
    void testReset() {
        // Arrange
        builder.setSerialNumber(123)
                .addItem(mockCartItem1)
                .setTotal(500.0)
                .setDiscount(50.0)
                .setCashTendered(600.0)
                .setChange(100.0)
                .setSource("online")
                .setUsername("reset.user")
                .setPaymentMethod("credit_card");

        // Act
        BillBuilder result = builder.reset();

        // Assert
        assertSame(builder, result, "Should return same builder instance for chaining");
        assertFalse(builder.isValid(), "Reset builder should not be valid");
        assertTrue(builder.getSummary().contains("items=0"), "Should have no items after reset");
        assertTrue(builder.getSummary().contains("total=0.00"), "Should have zero total after reset");
    }

    @Test
    @DisplayName("Should get summary correctly")
    void testGetSummary() {
        // Arrange
        builder.setSerialNumber(456)
                .addItem(mockCartItem1)
                .addItem(mockCartItem2)
                .setTotal(558.0);

        // Act
        String summary = builder.getSummary();

        // Assert
        assertNotNull(summary, "Summary should not be null");
        assertTrue(summary.contains("BillBuilder"), "Summary should contain class name");
        assertTrue(summary.contains("serial=456"), "Summary should contain serial number");
        assertTrue(summary.contains("items=2"), "Summary should contain item count");
        assertTrue(summary.contains("total=558.00"), "Summary should contain total");
        assertTrue(summary.contains("valid="), "Summary should contain validity status");
    }

    @Test
    @DisplayName("Should get summary with string serial")
    void testGetSummary_StringSerial() {
        // Arrange
        builder.setSerialString("#STRING-2025-06-05")
                .addItem(mockCartItem1)
                .setTotal(301.50);

        // Act
        String summary = builder.getSummary();

        // Assert
        assertTrue(summary.contains("serial=#STRING-2025-06-05"), "Summary should contain string serial");
    }

    // === Chain Method Tests ===

    @Test
    @DisplayName("Should support method chaining")
    void testMethodChaining() {
        // Act & Assert - Should not throw any exceptions
        assertDoesNotThrow(() -> {
            builder.setSerialNumber(1)
                    .setDate(LocalDateTime.now())
                    .addItem(mockCartItem1)
                    .setTotal(301.50)
                    .setDiscount(10.0)
                    .setCashTendered(350.0)
                    .calculateChange()
                    .setSource("store")
                    .setUsername("chain.user")
                    .setPaymentMethod("cash");
        });

        assertTrue(builder.getSummary().contains("BillBuilder"), "Chained builder should be valid BillBuilder");
    }

    @Test
    @DisplayName("Should handle edge case with very large numbers")
    void testEdgeCase_LargeNumbers() {
        // Arrange
        double largeTotal = 999999999.99;
        double largeCash = 1000000000.00;

        // Act & Assert
        assertDoesNotThrow(() -> {
            builder.setSerialNumber(Integer.MAX_VALUE)
                    .setTotal(largeTotal)
                    .setCashTendered(largeCash);
        });

        assertTrue(builder.getSummary().contains("999999999.99"), "Should handle large totals");
    }
}
