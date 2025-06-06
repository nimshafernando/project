package syos.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for OnlineInventoryDTO class
 * Tests constructor, getters, setter, and edge cases with 100% coverage
 */
class OnlineInventoryDTOTest {

    private OnlineInventoryDTO standardItem;
    private OnlineInventoryDTO groceryItem;
    private OnlineInventoryDTO electronicsItem;

    @BeforeEach
    void setUp() {
        standardItem = new OnlineInventoryDTO("ONL001", "Standard Online Product", 99.99, 50);
        groceryItem = new OnlineInventoryDTO("GRC001", "Organic Olive Oil 500ml", 12.99, 75);
        electronicsItem = new OnlineInventoryDTO("ELC001", "Wireless Bluetooth Headphones", 149.99, 25);
    }

    @Test
    @DisplayName("Should create OnlineInventoryDTO with all parameters")
    void testConstructor() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("TEST001", "Test Product", 199.50, 30);

        assertEquals("TEST001", item.getCode());
        assertEquals("Test Product", item.getName());
        assertEquals(199.50, item.getPrice(), 0.001);
        assertEquals(30, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle null code")
    void testNullCode() {
        OnlineInventoryDTO item = new OnlineInventoryDTO(null, "Product", 50.0, 10);
        assertNull(item.getCode());
        assertEquals("Product", item.getName());
        assertEquals(50.0, item.getPrice(), 0.001);
        assertEquals(10, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle null name")
    void testNullName() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("CODE001", null, 75.0, 15);
        assertEquals("CODE001", item.getCode());
        assertNull(item.getName());
        assertEquals(75.0, item.getPrice(), 0.001);
        assertEquals(15, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("", "", 0.0, 0);
        assertEquals("", item.getCode());
        assertEquals("", item.getName());
        assertEquals(0.0, item.getPrice(), 0.001);
        assertEquals(0, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle zero price")
    void testZeroPrice() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("FREE001", "Free Sample", 0.0, 100);
        assertEquals(0.0, item.getPrice(), 0.001);
        assertEquals(100, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle negative price")
    void testNegativePrice() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("DISC001", "Discount Item", -25.5, 50);
        assertEquals(-25.5, item.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Should handle large price values")
    void testLargePrice() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("EXP001", "Expensive Item", 99999.99, 1);
        assertEquals(99999.99, item.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Should handle zero quantity")
    void testZeroQuantity() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("OUT001", "Out of Stock", 25.0, 0);
        assertEquals(0, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle negative quantity")
    void testNegativeQuantity() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("NEG001", "Negative Stock", 30.0, -10);
        assertEquals(-10, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle large quantity values")
    void testLargeQuantity() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("BULK001", "Bulk Item", 1.0, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, item.getQuantity());
    }

    @Test
    @DisplayName("Should update quantity using setter")
    void testSetQuantity() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("UPD001", "Update Test", 50.0, 20);
        assertEquals(20, item.getQuantity());

        item.setQuantity(35);
        assertEquals(35, item.getQuantity());

        item.setQuantity(0);
        assertEquals(0, item.getQuantity());

        item.setQuantity(1000);
        assertEquals(1000, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle negative quantity in setter")
    void testSetNegativeQuantity() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("SET001", "Setter Test", 25.0, 10);

        item.setQuantity(-5);
        assertEquals(-5, item.getQuantity());

        item.setQuantity(-100);
        assertEquals(-100, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle maximum integer in setter")
    void testSetMaxQuantity() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("MAX001", "Max Test", 10.0, 5);

        item.setQuantity(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, item.getQuantity());

        item.setQuantity(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle special characters in code and name")
    void testSpecialCharacters() {
        String specialCode = "ON-L@001#2024";
        String specialName = "Premium Café & Pastry (50% OFF) - ₹299.99";

        OnlineInventoryDTO item = new OnlineInventoryDTO(specialCode, specialName, 299.99, 20);
        assertEquals(specialCode, item.getCode());
        assertEquals(specialName, item.getName());
    }

    @Test
    @DisplayName("Should handle very long strings")
    void testLongStrings() {
        String longCode = "ONLINE_PRODUCT_CODE_".repeat(10);
        String longName = "Very Long Online Product Description ".repeat(20);

        OnlineInventoryDTO item = new OnlineInventoryDTO(longCode, longName, 125.0, 8);
        assertEquals(longCode, item.getCode());
        assertEquals(longName, item.getName());
    }

    @Test
    @DisplayName("Should handle whitespace in strings")
    void testWhitespaceHandling() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("  ONL  001  ", "  Online Product  ", 45.0, 25);
        assertEquals("  ONL  001  ", item.getCode());
        assertEquals("  Online Product  ", item.getName());
    }

    @Test
    @DisplayName("Should handle decimal precision")
    void testDecimalPrecision() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("DEC001", "Decimal Test", 123.456789, 15);
        assertEquals(123.456789, item.getPrice(), 0.000001);
    }

    @Test
    @DisplayName("Should handle realistic online grocery items")
    void testRealisticGroceryItems() {
        OnlineInventoryDTO milk = new OnlineInventoryDTO("ONL_GRC001", "Organic Whole Milk 1L", 5.99, 120);
        OnlineInventoryDTO rice = new OnlineInventoryDTO("ONL_GRC002", "Basmati Rice 5kg", 18.99, 45);
        OnlineInventoryDTO apple = new OnlineInventoryDTO("ONL_GRC003", "Fresh Red Apples (per kg)", 4.50, 200);

        assertEquals("ONL_GRC001", milk.getCode());
        assertEquals("Organic Whole Milk 1L", milk.getName());
        assertEquals(5.99, milk.getPrice(), 0.001);
        assertEquals(120, milk.getQuantity());

        assertEquals(18.99, rice.getPrice(), 0.001);
        assertEquals(4.50, apple.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Should handle realistic online electronics items")
    void testRealisticElectronicsItems() {
        OnlineInventoryDTO laptop = new OnlineInventoryDTO("ONL_ELC001", "Gaming Laptop 16GB RAM", 1299.99, 8);
        OnlineInventoryDTO phone = new OnlineInventoryDTO("ONL_ELC002", "Smartphone 128GB", 699.00, 25);
        OnlineInventoryDTO charger = new OnlineInventoryDTO("ONL_ELC003", "Fast Wireless Charger", 39.99, 150);

        assertEquals("Gaming Laptop 16GB RAM", laptop.getName());
        assertEquals(1299.99, laptop.getPrice(), 0.001);
        assertEquals(8, laptop.getQuantity());

        assertEquals(699.00, phone.getPrice(), 0.001);
        assertEquals(39.99, charger.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Should handle realistic online fashion items")
    void testRealisticFashionItems() {
        OnlineInventoryDTO shirt = new OnlineInventoryDTO("ONL_FSH001", "Cotton T-Shirt (Medium)", 24.99, 85);
        OnlineInventoryDTO jeans = new OnlineInventoryDTO("ONL_FSH002", "Denim Jeans (Size 32)", 59.99, 40);
        OnlineInventoryDTO shoes = new OnlineInventoryDTO("ONL_FSH003", "Running Shoes (Size 9)", 89.99, 30);

        assertEquals("Cotton T-Shirt (Medium)", shirt.getName());
        assertEquals(24.99, shirt.getPrice(), 0.001);
        assertEquals(85, shirt.getQuantity());

        assertEquals(59.99, jeans.getPrice(), 0.001);
        assertEquals(89.99, shoes.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Should handle stock updates for online inventory")
    void testStockUpdates() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("STK001", "Stock Test Item", 15.99, 100);

        // Simulate sales - reducing stock
        item.setQuantity(95);
        assertEquals(95, item.getQuantity());

        item.setQuantity(80);
        assertEquals(80, item.getQuantity());

        // Simulate restocking
        item.setQuantity(150);
        assertEquals(150, item.getQuantity());

        // Simulate out of stock
        item.setQuantity(0);
        assertEquals(0, item.getQuantity());

        // Simulate overstocking
        item.setQuantity(1000);
        assertEquals(1000, item.getQuantity());
    }

    @Test
    @DisplayName("Should maintain other properties when quantity is updated")
    void testQuantityUpdateIsolation() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("ISO001", "Isolation Test", 99.99, 50);

        String originalCode = item.getCode();
        String originalName = item.getName();
        double originalPrice = item.getPrice();

        // Update quantity multiple times
        item.setQuantity(25);
        item.setQuantity(75);
        item.setQuantity(100);

        // Verify other properties remain unchanged
        assertEquals(originalCode, item.getCode());
        assertEquals(originalName, item.getName());
        assertEquals(originalPrice, item.getPrice(), 0.001);
        assertEquals(100, item.getQuantity()); // Only quantity should change
    }

    @Test
    @DisplayName("Should handle edge case quantities in updates")
    void testEdgeCaseQuantityUpdates() {
        OnlineInventoryDTO item = new OnlineInventoryDTO("EDGE001", "Edge Case Test", 50.0, 10);

        // Test boundary values
        item.setQuantity(1);
        assertEquals(1, item.getQuantity());

        item.setQuantity(0);
        assertEquals(0, item.getQuantity());

        item.setQuantity(-1);
        assertEquals(-1, item.getQuantity());

        item.setQuantity(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, item.getQuantity());

        item.setQuantity(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, item.getQuantity());
    }

    @Test
    @DisplayName("Should handle tabs and newlines in strings")
    void testTabsAndNewlines() {
        String codeWithTabs = "ONL\t001";
        String nameWithNewlines = "Online\nProduct\nDescription";

        OnlineInventoryDTO item = new OnlineInventoryDTO(codeWithTabs, nameWithNewlines, 65.0, 18);
        assertEquals(codeWithTabs, item.getCode());
        assertEquals(nameWithNewlines, item.getName());
    }
}
