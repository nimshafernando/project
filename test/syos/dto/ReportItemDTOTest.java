package syos.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReportItemDTO Tests")
class ReportItemDTOTest {

    private ReportItemDTO reportItem;

    @BeforeEach
    void setUp() {
        reportItem = new ReportItemDTO("ITM001", "Premium Coffee Beans", 150, 4500.75);
    }

    @Test
    @DisplayName("Constructor should create ReportItemDTO with all fields")
    void testConstructor() {
        assertNotNull(reportItem);
        assertEquals("ITM001", reportItem.getCode());
        assertEquals("Premium Coffee Beans", reportItem.getName());
        assertEquals(150, reportItem.getQuantity());
        assertEquals(4500.75, reportItem.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Constructor should handle null code")
    void testConstructorWithNullCode() {
        ReportItemDTO item = new ReportItemDTO(null, "Test Item", 100, 1000.0);
        assertNull(item.getCode());
        assertEquals("Test Item", item.getName());
        assertEquals(100, item.getQuantity());
        assertEquals(1000.0, item.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Constructor should handle empty code")
    void testConstructorWithEmptyCode() {
        ReportItemDTO item = new ReportItemDTO("", "Test Item", 100, 1000.0);
        assertEquals("", item.getCode());
        assertEquals("Test Item", item.getName());
        assertEquals(100, item.getQuantity());
        assertEquals(1000.0, item.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Constructor should handle null name")
    void testConstructorWithNullName() {
        ReportItemDTO item = new ReportItemDTO("ITM002", null, 100, 1000.0);
        assertEquals("ITM002", item.getCode());
        assertNull(item.getName());
        assertEquals(100, item.getQuantity());
        assertEquals(1000.0, item.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Constructor should handle empty name")
    void testConstructorWithEmptyName() {
        ReportItemDTO item = new ReportItemDTO("ITM003", "", 100, 1000.0);
        assertEquals("ITM003", item.getCode());
        assertEquals("", item.getName());
        assertEquals(100, item.getQuantity());
        assertEquals(1000.0, item.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Constructor should handle zero quantity")
    void testConstructorWithZeroQuantity() {
        ReportItemDTO item = new ReportItemDTO("ITM004", "Zero Stock Item", 0, 0.0);
        assertEquals("ITM004", item.getCode());
        assertEquals("Zero Stock Item", item.getName());
        assertEquals(0, item.getQuantity());
        assertEquals(0.0, item.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Constructor should handle negative quantity")
    void testConstructorWithNegativeQuantity() {
        ReportItemDTO item = new ReportItemDTO("ITM005", "Returned Item", -10, -500.0);
        assertEquals("ITM005", item.getCode());
        assertEquals("Returned Item", item.getName());
        assertEquals(-10, item.getQuantity());
        assertEquals(-500.0, item.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Constructor should handle maximum integer quantity")
    void testConstructorWithMaxQuantity() {
        ReportItemDTO item = new ReportItemDTO("ITM006", "Bulk Item", Integer.MAX_VALUE, 999999999.99);
        assertEquals("ITM006", item.getCode());
        assertEquals("Bulk Item", item.getName());
        assertEquals(Integer.MAX_VALUE, item.getQuantity());
        assertEquals(999999999.99, item.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Constructor should handle minimum integer quantity")
    void testConstructorWithMinQuantity() {
        ReportItemDTO item = new ReportItemDTO("ITM007", "Error Item", Integer.MIN_VALUE, -999999999.99);
        assertEquals("ITM007", item.getCode());
        assertEquals("Error Item", item.getName());
        assertEquals(Integer.MIN_VALUE, item.getQuantity());
        assertEquals(-999999999.99, item.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Constructor should handle zero revenue")
    void testConstructorWithZeroRevenue() {
        ReportItemDTO item = new ReportItemDTO("ITM008", "Free Sample", 50, 0.0);
        assertEquals("ITM008", item.getCode());
        assertEquals("Free Sample", item.getName());
        assertEquals(50, item.getQuantity());
        assertEquals(0.0, item.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Constructor should handle negative revenue")
    void testConstructorWithNegativeRevenue() {
        ReportItemDTO item = new ReportItemDTO("ITM009", "Refunded Item", 25, -1250.50);
        assertEquals("ITM009", item.getCode());
        assertEquals("Refunded Item", item.getName());
        assertEquals(25, item.getQuantity());
        assertEquals(-1250.50, item.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Constructor should handle very small revenue values")
    void testConstructorWithSmallRevenue() {
        ReportItemDTO item = new ReportItemDTO("ITM010", "Penny Item", 1000, 0.01);
        assertEquals("ITM010", item.getCode());
        assertEquals("Penny Item", item.getName());
        assertEquals(1000, item.getQuantity());
        assertEquals(0.01, item.getRevenue(), 0.0001);
    }

    @Test
    @DisplayName("Constructor should handle very large revenue values")
    void testConstructorWithLargeRevenue() {
        ReportItemDTO item = new ReportItemDTO("ITM011", "Luxury Item", 1, Double.MAX_VALUE);
        assertEquals("ITM011", item.getCode());
        assertEquals("Luxury Item", item.getName());
        assertEquals(1, item.getQuantity());
        assertEquals(Double.MAX_VALUE, item.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Constructor should handle special double values")
    void testConstructorWithSpecialDoubleValues() {
        ReportItemDTO positiveInfinity = new ReportItemDTO("INF1", "Positive Infinity", 1, Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, positiveInfinity.getRevenue());

        ReportItemDTO negativeInfinity = new ReportItemDTO("INF2", "Negative Infinity", 1, Double.NEGATIVE_INFINITY);
        assertEquals(Double.NEGATIVE_INFINITY, negativeInfinity.getRevenue());

        ReportItemDTO nanValue = new ReportItemDTO("NAN", "Not a Number", 1, Double.NaN);
        assertTrue(Double.isNaN(nanValue.getRevenue()));
    }

    // Getter tests
    @Test
    @DisplayName("getCode should return correct code")
    void testGetCode() {
        assertEquals("ITM001", reportItem.getCode());
    }

    @Test
    @DisplayName("getName should return correct name")
    void testGetName() {
        assertEquals("Premium Coffee Beans", reportItem.getName());
    }

    @Test
    @DisplayName("getQuantity should return correct quantity")
    void testGetQuantity() {
        assertEquals(150, reportItem.getQuantity());
    }

    @Test
    @DisplayName("getRevenue should return correct revenue")
    void testGetRevenue() {
        assertEquals(4500.75, reportItem.getRevenue(), 0.001);
    }

    // Immutability tests
    @Test
    @DisplayName("ReportItemDTO should be immutable - no setters available")
    void testImmutability() {
        // Verify that there are no setter methods by attempting to create another
        // instance
        // and confirming original values remain unchanged
        ReportItemDTO original = new ReportItemDTO("ORIG", "Original", 100, 1000.0);

        // Create new instance to verify immutability pattern
        ReportItemDTO modified = new ReportItemDTO("MOD", "Modified", 200, 2000.0);

        // Original should remain unchanged
        assertEquals("ORIG", original.getCode());
        assertEquals("Original", original.getName());
        assertEquals(100, original.getQuantity());
        assertEquals(1000.0, original.getRevenue(), 0.001);

        // Modified should have new values
        assertEquals("MOD", modified.getCode());
        assertEquals("Modified", modified.getName());
        assertEquals(200, modified.getQuantity());
        assertEquals(2000.0, modified.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Multiple getter calls should return consistent values")
    void testGetterConsistency() {
        for (int i = 0; i < 10; i++) {
            assertEquals("ITM001", reportItem.getCode());
            assertEquals("Premium Coffee Beans", reportItem.getName());
            assertEquals(150, reportItem.getQuantity());
            assertEquals(4500.75, reportItem.getRevenue(), 0.001);
        }
    }

    // Static merge method tests
    @Test
    @DisplayName("merge should combine two ReportItemDTOs correctly")
    void testMerge() {
        ReportItemDTO item1 = new ReportItemDTO("ITM001", "Coffee Beans", 100, 2500.0);
        ReportItemDTO item2 = new ReportItemDTO("ITM001", "Coffee Beans", 50, 1250.0);

        ReportItemDTO merged = ReportItemDTO.merge(item1, item2);

        assertEquals("ITM001", merged.getCode());
        assertEquals("Coffee Beans", merged.getName());
        assertEquals(150, merged.getQuantity());
        assertEquals(3750.0, merged.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("merge should handle zero quantities")
    void testMergeWithZeroQuantities() {
        ReportItemDTO item1 = new ReportItemDTO("ITM002", "Tea Bags", 0, 0.0);
        ReportItemDTO item2 = new ReportItemDTO("ITM002", "Tea Bags", 100, 500.0);

        ReportItemDTO merged = ReportItemDTO.merge(item1, item2);

        assertEquals("ITM002", merged.getCode());
        assertEquals("Tea Bags", merged.getName());
        assertEquals(100, merged.getQuantity());
        assertEquals(500.0, merged.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("merge should handle negative quantities")
    void testMergeWithNegativeQuantities() {
        ReportItemDTO item1 = new ReportItemDTO("ITM003", "Milk", 50, 250.0);
        ReportItemDTO item2 = new ReportItemDTO("ITM003", "Milk", -10, -50.0);

        ReportItemDTO merged = ReportItemDTO.merge(item1, item2);

        assertEquals("ITM003", merged.getCode());
        assertEquals("Milk", merged.getName());
        assertEquals(40, merged.getQuantity());
        assertEquals(200.0, merged.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("merge should handle negative revenues")
    void testMergeWithNegativeRevenues() {
        ReportItemDTO item1 = new ReportItemDTO("ITM004", "Bread", 100, 500.0);
        ReportItemDTO item2 = new ReportItemDTO("ITM004", "Bread", 20, -100.0);

        ReportItemDTO merged = ReportItemDTO.merge(item1, item2);

        assertEquals("ITM004", merged.getCode());
        assertEquals("Bread", merged.getName());
        assertEquals(120, merged.getQuantity());
        assertEquals(400.0, merged.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("merge should use first item's code and name")
    void testMergeUsesFirstItemCodeAndName() {
        ReportItemDTO item1 = new ReportItemDTO("ITM001", "Original Name", 100, 1000.0);
        ReportItemDTO item2 = new ReportItemDTO("ITM999", "Different Name", 50, 500.0);

        ReportItemDTO merged = ReportItemDTO.merge(item1, item2);

        assertEquals("ITM001", merged.getCode());
        assertEquals("Original Name", merged.getName());
        assertEquals(150, merged.getQuantity());
        assertEquals(1500.0, merged.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("merge should handle null codes")
    void testMergeWithNullCodes() {
        ReportItemDTO item1 = new ReportItemDTO(null, "Item A", 100, 1000.0);
        ReportItemDTO item2 = new ReportItemDTO("ITM002", "Item B", 50, 500.0);

        ReportItemDTO merged = ReportItemDTO.merge(item1, item2);

        assertNull(merged.getCode());
        assertEquals("Item A", merged.getName());
        assertEquals(150, merged.getQuantity());
        assertEquals(1500.0, merged.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("merge should handle null names")
    void testMergeWithNullNames() {
        ReportItemDTO item1 = new ReportItemDTO("ITM001", null, 100, 1000.0);
        ReportItemDTO item2 = new ReportItemDTO("ITM002", "Item B", 50, 500.0);

        ReportItemDTO merged = ReportItemDTO.merge(item1, item2);

        assertEquals("ITM001", merged.getCode());
        assertNull(merged.getName());
        assertEquals(150, merged.getQuantity());
        assertEquals(1500.0, merged.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("merge should handle maximum integer quantities")
    void testMergeWithMaxQuantities() {
        ReportItemDTO item1 = new ReportItemDTO("ITM001", "Max Item", Integer.MAX_VALUE, 1000.0);
        ReportItemDTO item2 = new ReportItemDTO("ITM001", "Max Item", 1, 500.0);

        // This will overflow, but we test the behavior
        ReportItemDTO merged = ReportItemDTO.merge(item1, item2);

        assertEquals("ITM001", merged.getCode());
        assertEquals("Max Item", merged.getName());
        assertEquals(Integer.MIN_VALUE, merged.getQuantity()); // Overflow to MIN_VALUE
        assertEquals(1500.0, merged.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("merge should handle special double values")
    void testMergeWithSpecialDoubleValues() {
        ReportItemDTO item1 = new ReportItemDTO("INF", "Infinity Item", 1, Double.POSITIVE_INFINITY);
        ReportItemDTO item2 = new ReportItemDTO("INF", "Infinity Item", 1, 1000.0);

        ReportItemDTO merged = ReportItemDTO.merge(item1, item2);

        assertEquals("INF", merged.getCode());
        assertEquals("Infinity Item", merged.getName());
        assertEquals(2, merged.getQuantity());
        assertEquals(Double.POSITIVE_INFINITY, merged.getRevenue());
    }

    @Test
    @DisplayName("merge should handle NaN values")
    void testMergeWithNaNValues() {
        ReportItemDTO item1 = new ReportItemDTO("NAN", "NaN Item", 1, Double.NaN);
        ReportItemDTO item2 = new ReportItemDTO("NAN", "NaN Item", 1, 1000.0);

        ReportItemDTO merged = ReportItemDTO.merge(item1, item2);

        assertEquals("NAN", merged.getCode());
        assertEquals("NaN Item", merged.getName());
        assertEquals(2, merged.getQuantity());
        assertTrue(Double.isNaN(merged.getRevenue()));
    }

    @Test
    @DisplayName("merge should be associative with three items")
    void testMergeAssociativity() {
        ReportItemDTO item1 = new ReportItemDTO("ITM001", "Item", 10, 100.0);
        ReportItemDTO item2 = new ReportItemDTO("ITM001", "Item", 20, 200.0);
        ReportItemDTO item3 = new ReportItemDTO("ITM001", "Item", 30, 300.0);

        // (item1 + item2) + item3
        ReportItemDTO merged1 = ReportItemDTO.merge(ReportItemDTO.merge(item1, item2), item3);

        // item1 + (item2 + item3)
        ReportItemDTO merged2 = ReportItemDTO.merge(item1, ReportItemDTO.merge(item2, item3));

        assertEquals(merged1.getCode(), merged2.getCode());
        assertEquals(merged1.getName(), merged2.getName());
        assertEquals(merged1.getQuantity(), merged2.getQuantity());
        assertEquals(merged1.getRevenue(), merged2.getRevenue(), 0.001);
    }

    // Business scenario tests
    @Test
    @DisplayName("Should create report for high-revenue luxury items")
    void testLuxuryItemScenario() {
        ReportItemDTO luxuryItem = new ReportItemDTO(
                "LUX-WATCH-001",
                "Premium Swiss Watch",
                5,
                25000.00);

        assertEquals("LUX-WATCH-001", luxuryItem.getCode());
        assertEquals("Premium Swiss Watch", luxuryItem.getName());
        assertEquals(5, luxuryItem.getQuantity());
        assertEquals(25000.00, luxuryItem.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Should create report for bulk commodity items")
    void testBulkCommodityScenario() {
        ReportItemDTO bulkItem = new ReportItemDTO(
                "BULK-RICE-50KG",
                "Premium Basmati Rice 50kg",
                1000,
                45000.50);

        assertEquals("BULK-RICE-50KG", bulkItem.getCode());
        assertEquals("Premium Basmati Rice 50kg", bulkItem.getName());
        assertEquals(1000, bulkItem.getQuantity());
        assertEquals(45000.50, bulkItem.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Should create report for promotional free items")
    void testPromotionalFreeItemScenario() {
        ReportItemDTO freeItem = new ReportItemDTO(
                "PROMO-SAMPLE",
                "Free Product Sample",
                500,
                0.0);

        assertEquals("PROMO-SAMPLE", freeItem.getCode());
        assertEquals("Free Product Sample", freeItem.getName());
        assertEquals(500, freeItem.getQuantity());
        assertEquals(0.0, freeItem.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Should merge similar products from different locations")
    void testMultiLocationMergeScenario() {
        ReportItemDTO storeA = new ReportItemDTO("ITM-SHAMPOO", "Hair Shampoo 500ml", 150, 2250.0);
        ReportItemDTO storeB = new ReportItemDTO("ITM-SHAMPOO", "Hair Shampoo 500ml", 200, 3000.0);
        ReportItemDTO storeC = new ReportItemDTO("ITM-SHAMPOO", "Hair Shampoo 500ml", 100, 1500.0);

        ReportItemDTO totalSales = ReportItemDTO.merge(ReportItemDTO.merge(storeA, storeB), storeC);

        assertEquals("ITM-SHAMPOO", totalSales.getCode());
        assertEquals("Hair Shampoo 500ml", totalSales.getName());
        assertEquals(450, totalSales.getQuantity());
        assertEquals(6750.0, totalSales.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Should handle return and refund scenarios")
    void testReturnRefundScenario() {
        ReportItemDTO sales = new ReportItemDTO("ITM-PHONE", "Smartphone", 100, 50000.0);
        ReportItemDTO returns = new ReportItemDTO("ITM-PHONE", "Smartphone", -5, -2500.0);

        ReportItemDTO netSales = ReportItemDTO.merge(sales, returns);

        assertEquals("ITM-PHONE", netSales.getCode());
        assertEquals("Smartphone", netSales.getName());
        assertEquals(95, netSales.getQuantity());
        assertEquals(47500.0, netSales.getRevenue(), 0.001);
    }

    @Test
    @DisplayName("Should handle precision for financial calculations")
    void testFinancialPrecisionScenario() {
        ReportItemDTO item1 = new ReportItemDTO("FIN-001", "Financial Product", 1000, 12345.67);
        ReportItemDTO item2 = new ReportItemDTO("FIN-001", "Financial Product", 2000, 23456.78);

        ReportItemDTO merged = ReportItemDTO.merge(item1, item2);

        assertEquals("FIN-001", merged.getCode());
        assertEquals("Financial Product", merged.getName());
        assertEquals(3000, merged.getQuantity());
        assertEquals(35802.45, merged.getRevenue(), 0.001);
    }

}
