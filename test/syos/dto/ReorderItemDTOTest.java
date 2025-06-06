package syos.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ReorderItemDTO class
 * Tests constructor, getters, calculated shortfall method, and edge cases with
 * 100% coverage
 */
class ReorderItemDTOTest {

    private ReorderItemDTO criticalItem;
    private ReorderItemDTO lowStockItem;
    private ReorderItemDTO sufficientStockItem;

    @BeforeEach
    void setUp() {
        criticalItem = new ReorderItemDTO("CRIT001", "Critical Stock Item", 5, 50, 25.99, "store");
        lowStockItem = new ReorderItemDTO("LOW001", "Low Stock Product", 15, 25, 12.50, "online");
        sufficientStockItem = new ReorderItemDTO("SUFF001", "Sufficient Stock", 100, 50, 8.75, "store");
    }

    @Test
    @DisplayName("Should create ReorderItemDTO with all parameters")
    void testConstructor() {
        ReorderItemDTO item = new ReorderItemDTO("TEST001", "Test Product", 20, 30, 45.99, "warehouse");

        assertEquals("TEST001", item.getCode());
        assertEquals("Test Product", item.getName());
        assertEquals(20, item.getCurrentStock());
        assertEquals(30, item.getReorderLevel());
        assertEquals(45.99, item.getPrice(), 0.001);
        assertEquals("warehouse", item.getStoreType());
    }

    @Test
    @DisplayName("Should handle null code")
    void testNullCode() {
        ReorderItemDTO item = new ReorderItemDTO(null, "Product", 10, 20, 15.0, "store");
        assertNull(item.getCode());
        assertEquals("Product", item.getName());
        assertEquals(10, item.getCurrentStock());
    }

    @Test
    @DisplayName("Should handle null name")
    void testNullName() {
        ReorderItemDTO item = new ReorderItemDTO("CODE001", null, 15, 25, 20.0, "online");
        assertEquals("CODE001", item.getCode());
        assertNull(item.getName());
        assertEquals(15, item.getCurrentStock());
    }

    @Test
    @DisplayName("Should handle null store type")
    void testNullStoreType() {
        ReorderItemDTO item = new ReorderItemDTO("STORE001", "Store Product", 30, 40, 18.5, null);
        assertEquals("STORE001", item.getCode());
        assertEquals(30, item.getCurrentStock());
        assertNull(item.getStoreType());
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        ReorderItemDTO item = new ReorderItemDTO("", "", 0, 0, 0.0, "");
        assertEquals("", item.getCode());
        assertEquals("", item.getName());
        assertEquals(0, item.getCurrentStock());
        assertEquals(0, item.getReorderLevel());
        assertEquals(0.0, item.getPrice(), 0.001);
        assertEquals("", item.getStoreType());
    }

    @Test
    @DisplayName("Should calculate correct shortfall when stock is below reorder level")
    void testShortfallBelowReorderLevel() {
        // Current stock: 5, Reorder level: 50, Expected shortfall: 45
        assertEquals(45, criticalItem.getShortfall());

        // Current stock: 15, Reorder level: 25, Expected shortfall: 10
        assertEquals(10, lowStockItem.getShortfall());

        // Test with different values
        ReorderItemDTO testItem = new ReorderItemDTO("TEST001", "Test", 8, 20, 10.0, "store");
        assertEquals(12, testItem.getShortfall());
    }

    @Test
    @DisplayName("Should return zero shortfall when stock meets or exceeds reorder level")
    void testNoShortfall() {
        // Current stock: 100, Reorder level: 50, Expected shortfall: 0
        assertEquals(0, sufficientStockItem.getShortfall());

        // Test exact match
        ReorderItemDTO exactMatch = new ReorderItemDTO("EXACT001", "Exact Match", 25, 25, 15.0, "store");
        assertEquals(0, exactMatch.getShortfall());

        // Test above reorder level
        ReorderItemDTO aboveLevel = new ReorderItemDTO("ABOVE001", "Above Level", 75, 50, 20.0, "online");
        assertEquals(0, aboveLevel.getShortfall());
    }

    @Test
    @DisplayName("Should handle zero current stock")
    void testZeroCurrentStock() {
        ReorderItemDTO item = new ReorderItemDTO("ZERO001", "Zero Stock", 0, 30, 25.0, "store");
        assertEquals(0, item.getCurrentStock());
        assertEquals(30, item.getShortfall());
    }

    @Test
    @DisplayName("Should handle zero reorder level")
    void testZeroReorderLevel() {
        ReorderItemDTO item = new ReorderItemDTO("ZERO_REORDER", "Zero Reorder", 10, 0, 15.0, "store");
        assertEquals(0, item.getReorderLevel());
        assertEquals(0, item.getShortfall()); // No shortfall when reorder level is 0
    }

    @Test
    @DisplayName("Should handle negative current stock")
    void testNegativeCurrentStock() {
        ReorderItemDTO item = new ReorderItemDTO("NEG001", "Negative Stock", -5, 20, 30.0, "store");
        assertEquals(-5, item.getCurrentStock());
        assertEquals(25, item.getShortfall()); // 20 - (-5) = 25
    }

    @Test
    @DisplayName("Should handle negative reorder level")
    void testNegativeReorderLevel() {
        ReorderItemDTO item = new ReorderItemDTO("NEG_REORDER", "Negative Reorder", 10, -5, 12.0, "store");
        assertEquals(-5, item.getReorderLevel());
        assertEquals(0, item.getShortfall()); // Max(0, -5 - 10) = 0
    }

    @Test
    @DisplayName("Should handle both negative values")
    void testBothNegativeValues() {
        ReorderItemDTO item = new ReorderItemDTO("BOTH_NEG", "Both Negative", -10, -5, 18.0, "store");
        assertEquals(-10, item.getCurrentStock());
        assertEquals(-5, item.getReorderLevel());
        assertEquals(5, item.getShortfall()); // Max(0, -5 - (-10)) = Max(0, 5) = 5
    }

    @Test
    @DisplayName("Should handle large stock values")
    void testLargeStockValues() {
        ReorderItemDTO item = new ReorderItemDTO("LARGE001", "Large Stock", Integer.MAX_VALUE, 1000, 5.0, "warehouse");
        assertEquals(Integer.MAX_VALUE, item.getCurrentStock());
        assertEquals(0, item.getShortfall()); // Well above reorder level
    }

    @Test
    @DisplayName("Should handle large reorder level")
    void testLargeReorderLevel() {
        ReorderItemDTO item = new ReorderItemDTO("LARGE_REORDER", "Large Reorder", 100, Integer.MAX_VALUE, 10.0,
                "store");
        assertEquals(Integer.MAX_VALUE, item.getReorderLevel());
        assertEquals(Integer.MAX_VALUE - 100, item.getShortfall());
    }

    @Test
    @DisplayName("Should handle zero price")
    void testZeroPrice() {
        ReorderItemDTO item = new ReorderItemDTO("FREE001", "Free Item", 5, 10, 0.0, "store");
        assertEquals(0.0, item.getPrice(), 0.001);
        assertEquals(5, item.getShortfall());
    }

    @Test
    @DisplayName("Should handle negative price")
    void testNegativePrice() {
        ReorderItemDTO item = new ReorderItemDTO("NEG_PRICE", "Negative Price", 8, 15, -12.50, "online");
        assertEquals(-12.50, item.getPrice(), 0.001);
        assertEquals(7, item.getShortfall());
    }

    @Test
    @DisplayName("Should handle very large price")
    void testLargePrice() {
        ReorderItemDTO item = new ReorderItemDTO("EXP001", "Expensive Item", 2, 10, Double.MAX_VALUE, "store");
        assertEquals(Double.MAX_VALUE, item.getPrice());
        assertEquals(8, item.getShortfall());
    }

    @Test
    @DisplayName("Should handle decimal precision in price")
    void testDecimalPrecision() {
        ReorderItemDTO item = new ReorderItemDTO("DEC001", "Decimal Item", 12, 20, 123.456789, "online");
        assertEquals(123.456789, item.getPrice(), 0.000001);
        assertEquals(8, item.getShortfall());
    }

    @Test
    @DisplayName("Should handle special characters in strings")
    void testSpecialCharacters() {
        String specialCode = "RE-ORD@001#2024";
        String specialName = "Reorder Item (50% OFF) - €25.99";
        String specialStoreType = "store&warehouse";

        ReorderItemDTO item = new ReorderItemDTO(specialCode, specialName, 15, 30, 25.99, specialStoreType);
        assertEquals(specialCode, item.getCode());
        assertEquals(specialName, item.getName());
        assertEquals(specialStoreType, item.getStoreType());
        assertEquals(15, item.getShortfall());
    }

    @Test
    @DisplayName("Should handle realistic grocery reorder scenarios")
    void testRealisticGroceryScenarios() {
        ReorderItemDTO milk = new ReorderItemDTO("GRC001", "Organic Milk 1L", 8, 50, 5.99, "store");
        ReorderItemDTO bread = new ReorderItemDTO("GRC002", "Whole Wheat Bread", 12, 25, 3.50, "store");
        ReorderItemDTO rice = new ReorderItemDTO("GRC003", "Basmati Rice 5kg", 45, 30, 18.99, "warehouse");

        assertEquals(42, milk.getShortfall()); // 50 - 8 = 42
        assertEquals(13, bread.getShortfall()); // 25 - 12 = 13
        assertEquals(0, rice.getShortfall()); // 45 >= 30, no shortfall
    }

    @Test
    @DisplayName("Should handle realistic electronics reorder scenarios")
    void testRealisticElectronicsScenarios() {
        ReorderItemDTO laptop = new ReorderItemDTO("ELC001", "Gaming Laptop", 2, 10, 1299.99, "store");
        ReorderItemDTO phone = new ReorderItemDTO("ELC002", "Smartphone", 18, 15, 699.00, "online");
        ReorderItemDTO cable = new ReorderItemDTO("ELC003", "USB Cable", 150, 100, 12.99, "warehouse");

        assertEquals(8, laptop.getShortfall()); // 10 - 2 = 8
        assertEquals(0, phone.getShortfall()); // 18 >= 15, no shortfall
        assertEquals(0, cable.getShortfall()); // 150 >= 100, no shortfall
    }

    @Test
    @DisplayName("Should handle different store types")
    void testDifferentStoreTypes() {
        ReorderItemDTO storeItem = new ReorderItemDTO("STORE001", "Store Item", 10, 20, 15.0, "store");
        ReorderItemDTO onlineItem = new ReorderItemDTO("ONLINE001", "Online Item", 25, 30, 20.0, "online");
        ReorderItemDTO warehouseItem = new ReorderItemDTO("WARE001", "Warehouse Item", 5, 15, 30.0, "warehouse");
        ReorderItemDTO hybridItem = new ReorderItemDTO("HYBRID001", "Hybrid Item", 40, 35, 25.0, "store_online");

        assertEquals("store", storeItem.getStoreType());
        assertEquals("online", onlineItem.getStoreType());
        assertEquals("warehouse", warehouseItem.getStoreType());
        assertEquals("store_online", hybridItem.getStoreType());

        assertEquals(10, storeItem.getShortfall());
        assertEquals(5, onlineItem.getShortfall());
        assertEquals(10, warehouseItem.getShortfall());
        assertEquals(0, hybridItem.getShortfall());
    }

    @Test
    @DisplayName("Should handle whitespace in strings")
    void testWhitespaceHandling() {
        ReorderItemDTO item = new ReorderItemDTO("  REORD  001  ", "  Reorder Product  ", 15, 25, 35.0, "  store  ");
        assertEquals("  REORD  001  ", item.getCode());
        assertEquals("  Reorder Product  ", item.getName());
        assertEquals("  store  ", item.getStoreType());
        assertEquals(10, item.getShortfall());
    }

    @Test
    @DisplayName("Should handle tabs and newlines in strings")
    void testTabsAndNewlines() {
        String codeWithTabs = "REORD\t001";
        String nameWithNewlines = "Reorder\nProduct\nDescription";
        String storeWithTabs = "store\twarehouse";

        ReorderItemDTO item = new ReorderItemDTO(codeWithTabs, nameWithNewlines, 20, 35, 22.50, storeWithTabs);
        assertEquals(codeWithTabs, item.getCode());
        assertEquals(nameWithNewlines, item.getName());
        assertEquals(storeWithTabs, item.getStoreType());
        assertEquals(15, item.getShortfall());
    }

    @Test
    @DisplayName("Should handle very long strings")
    void testLongStrings() {
        String longCode = "REORDER_ITEM_CODE_".repeat(10);
        String longName = "Very Long Reorder Item Description ".repeat(20);
        String longStoreType = "very_long_store_type_description_".repeat(5);

        ReorderItemDTO item = new ReorderItemDTO(longCode, longName, 30, 50, 45.0, longStoreType);
        assertEquals(longCode, item.getCode());
        assertEquals(longName, item.getName());
        assertEquals(longStoreType, item.getStoreType());
        assertEquals(20, item.getShortfall());
    }

    @Test
    @DisplayName("Should maintain immutability of all properties")
    void testImmutability() {
        // Test that multiple calls return consistent values
        assertEquals("CRIT001", criticalItem.getCode());
        assertEquals("CRIT001", criticalItem.getCode());

        assertEquals("Critical Stock Item", criticalItem.getName());
        assertEquals("Critical Stock Item", criticalItem.getName());

        assertEquals(5, criticalItem.getCurrentStock());
        assertEquals(5, criticalItem.getCurrentStock());

        assertEquals(50, criticalItem.getReorderLevel());
        assertEquals(50, criticalItem.getReorderLevel());

        assertEquals(25.99, criticalItem.getPrice(), 0.001);
        assertEquals(25.99, criticalItem.getPrice(), 0.001);

        assertEquals("store", criticalItem.getStoreType());
        assertEquals("store", criticalItem.getStoreType());

        assertEquals(45, criticalItem.getShortfall());
        assertEquals(45, criticalItem.getShortfall());
    }

    @Test
    @DisplayName("Should handle edge case shortfall calculations")
    void testEdgeCaseShortfallCalculations() {
        // Current stock equals reorder level
        ReorderItemDTO equalItem = new ReorderItemDTO("EQUAL001", "Equal Stock", 25, 25, 10.0, "store");
        assertEquals(0, equalItem.getShortfall());

        // Current stock is 1 below reorder level
        ReorderItemDTO oneBelowItem = new ReorderItemDTO("ONE_BELOW", "One Below", 24, 25, 12.0, "store");
        assertEquals(1, oneBelowItem.getShortfall());

        // Current stock is 1 above reorder level
        ReorderItemDTO oneAboveItem = new ReorderItemDTO("ONE_ABOVE", "One Above", 26, 25, 15.0, "store");
        assertEquals(0, oneAboveItem.getShortfall());

        // Very large shortfall
        ReorderItemDTO largeShortfallItem = new ReorderItemDTO("LARGE_SHORT", "Large Shortfall", 1, 1000000, 20.0,
                "warehouse");
        assertEquals(999999, largeShortfallItem.getShortfall());
    }
}
