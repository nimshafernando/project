package syos.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ItemDTO class
 * Tests all constructors, getters, and edge cases with 100% coverage
 */
class ItemDTOTest {

    private ItemDTO standardItem;
    private ItemDTO premiumItem;
    private ItemDTO budgetItem;

    @BeforeEach
    void setUp() {
        standardItem = new ItemDTO("ITEM001", "Standard Product", 99.99, 50);
        premiumItem = new ItemDTO("PREM001", "Premium Coffee Beans", 299.99, 25);
        budgetItem = new ItemDTO("BUDG001", "Budget Notebook", 5.50, 100);
    }

    @Test
    @DisplayName("Should create ItemDTO with all parameters")
    void testConstructor() {
        ItemDTO item = new ItemDTO("TEST001", "Test Item", 150.75, 30);

        assertEquals("TEST001", item.getCode());
        assertEquals("Test Item", item.getName());
        assertEquals(150.75, item.getPrice(), 0.001);
        assertEquals(30, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle null code gracefully")
    void testNullCode() {
        ItemDTO item = new ItemDTO(null, "Test Item", 100.0, 10);
        assertNull(item.getCode());
        assertEquals("Test Item", item.getName());
    }

    @Test
    @DisplayName("Should handle null name gracefully")
    void testNullName() {
        ItemDTO item = new ItemDTO("CODE001", null, 100.0, 10);
        assertEquals("CODE001", item.getCode());
        assertNull(item.getName());
    }

    @Test
    @DisplayName("Should handle empty string values")
    void testEmptyStrings() {
        ItemDTO item = new ItemDTO("", "", 0.0, 0);
        assertEquals("", item.getCode());
        assertEquals("", item.getName());
        assertEquals(0.0, item.getPrice());
        assertEquals(0, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle very long strings")
    void testLongStrings() {
        String longCode = "A".repeat(100);
        String longName = "Very Long Product Name ".repeat(10);

        ItemDTO item = new ItemDTO(longCode, longName, 999.99, 1);
        assertEquals(longCode, item.getCode());
        assertEquals(longName, item.getName());
    }

    @Test
    @DisplayName("Should handle zero price")
    void testZeroPrice() {
        ItemDTO item = new ItemDTO("FREE001", "Free Sample", 0.0, 5);
        assertEquals(0.0, item.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Should handle negative price")
    void testNegativePrice() {
        ItemDTO item = new ItemDTO("NEG001", "Discount Item", -10.5, 3);
        assertEquals(-10.5, item.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Should handle very large price")
    void testLargePrice() {
        ItemDTO item = new ItemDTO("EXP001", "Expensive Item", Double.MAX_VALUE, 1);
        assertEquals(Double.MAX_VALUE, item.getPrice());
    }

    @Test
    @DisplayName("Should handle zero quantity")
    void testZeroQuantity() {
        ItemDTO item = new ItemDTO("ZERO001", "Out of Stock", 50.0, 0);
        assertEquals(0, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle negative quantity")
    void testNegativeQuantity() {
        ItemDTO item = new ItemDTO("NEG001", "Negative Stock", 25.0, -5);
        assertEquals(-5, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle maximum integer quantity")
    void testMaxQuantity() {
        ItemDTO item = new ItemDTO("MAX001", "Maximum Stock", 1.0, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle decimal precision correctly")
    void testDecimalPrecision() {
        ItemDTO item = new ItemDTO("DEC001", "Decimal Item", 123.456789, 10);
        assertEquals(123.456789, item.getPrice(), 0.000001);
    }

    @Test
    @DisplayName("Should handle special characters in code and name")
    void testSpecialCharacters() {
        String specialCode = "IT#M-001@2024";
        String specialName = "Café & Pastry (50% off) - €25.99";

        ItemDTO item = new ItemDTO(specialCode, specialName, 25.99, 15);
        assertEquals(specialCode, item.getCode());
        assertEquals(specialName, item.getName());
    }

    @Test
    @DisplayName("Should maintain immutability - getters return same values")
    void testImmutability() {
        // Test that multiple calls to getters return consistent values
        assertEquals("ITEM001", standardItem.getCode());
        assertEquals("ITEM001", standardItem.getCode());

        assertEquals("Standard Product", standardItem.getName());
        assertEquals("Standard Product", standardItem.getName());

        assertEquals(99.99, standardItem.getPrice(), 0.001);
        assertEquals(99.99, standardItem.getPrice(), 0.001);

        assertEquals(50, standardItem.getQuantity());
        assertEquals(50, standardItem.getQuantity());
    }

    @Test
    @DisplayName("Should handle realistic grocery store items")
    void testGroceryStoreItems() {
        ItemDTO milk = new ItemDTO("GRO001", "Organic Whole Milk 1L", 4.99, 48);
        ItemDTO bread = new ItemDTO("GRO002", "Artisan Sourdough Bread", 6.50, 24);
        ItemDTO apple = new ItemDTO("GRO003", "Gala Apples (per kg)", 3.25, 75);

        assertEquals("GRO001", milk.getCode());
        assertEquals("Organic Whole Milk 1L", milk.getName());
        assertEquals(4.99, milk.getPrice(), 0.001);
        assertEquals(48, milk.getQuantity());

        assertEquals(6.50, bread.getPrice(), 0.001);
        assertEquals(3.25, apple.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Should handle electronics store items")
    void testElectronicsStoreItems() {
        ItemDTO laptop = new ItemDTO("ELEC001", "Gaming Laptop RTX 4080", 2499.99, 5);
        ItemDTO phone = new ItemDTO("ELEC002", "Smartphone 256GB", 899.00, 12);
        ItemDTO cable = new ItemDTO("ELEC003", "USB-C Cable 2m", 19.99, 150);

        assertEquals("Gaming Laptop RTX 4080", laptop.getName());
        assertEquals(2499.99, laptop.getPrice(), 0.001);
        assertEquals(5, laptop.getQuantity());

        assertEquals(899.00, phone.getPrice(), 0.001);
        assertEquals(19.99, cable.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Should handle edge case prices")
    void testEdgeCasePrices() {
        ItemDTO minimumPrice = new ItemDTO("MIN001", "Penny Item", 0.01, 1000);
        ItemDTO hundredThousand = new ItemDTO("EXP001", "Luxury Watch", 99999.99, 1);
        ItemDTO fraction = new ItemDTO("FRAC001", "Fractional Price", 1.999, 50);

        assertEquals(0.01, minimumPrice.getPrice(), 0.001);
        assertEquals(99999.99, hundredThousand.getPrice(), 0.001);
        assertEquals(1.999, fraction.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Should handle whitespace in strings")
    void testWhitespaceHandling() {
        ItemDTO spacedItem = new ItemDTO("  SPACE001  ", "  Product with Spaces  ", 50.0, 10);
        assertEquals("  SPACE001  ", spacedItem.getCode());
        assertEquals("  Product with Spaces  ", spacedItem.getName());
    }

    @Test
    @DisplayName("Should handle tabs and newlines in strings")
    void testTabsAndNewlines() {
        String codeWithTabs = "TAB\t001";
        String nameWithNewlines = "Product\nWith\nNewlines";

        ItemDTO item = new ItemDTO(codeWithTabs, nameWithNewlines, 75.0, 20);
        assertEquals(codeWithTabs, item.getCode());
        assertEquals(nameWithNewlines, item.getName());
    }
}
