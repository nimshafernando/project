package syos.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BatchDTO Test Suite")
class BatchDTOTest {

    private BatchDTO batchDTO;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    private LocalDate futureDate;
    private LocalDate pastDate;

    @BeforeEach
    void setUp() {
        purchaseDate = LocalDate.of(2024, 1, 15);
        expiryDate = LocalDate.of(2024, 12, 31);
        futureDate = LocalDate.of(2025, 6, 30);
        pastDate = LocalDate.of(2023, 12, 1);
    }

    // Constructor tests for new batches (6-parameter constructor)
    @Test
    @DisplayName("Should create BatchDTO with new batch constructor - fresh produce")
    void testNewBatchConstructor_FreshProduce() {
        batchDTO = new BatchDTO("PROD001", "Organic Bananas", 2.99, 50, purchaseDate, expiryDate);

        assertEquals("PROD001", batchDTO.getItemCode());
        assertEquals("Organic Bananas", batchDTO.getName());
        assertEquals(2.99, batchDTO.getSellingPrice(), 0.001);
        assertEquals(50, batchDTO.getQuantity());
        assertEquals(purchaseDate, batchDTO.getPurchaseDate());
        assertEquals(expiryDate, batchDTO.getExpiryDate());
        assertEquals(0, batchDTO.getUsedQuantity());
        assertEquals(0, batchDTO.getId()); // Default value
    }

    @Test
    @DisplayName("Should create BatchDTO with new batch constructor - packaged goods")
    void testNewBatchConstructor_PackagedGoods() {
        batchDTO = new BatchDTO("PKG002", "Premium Coffee Beans", 15.99, 25, purchaseDate, futureDate);

        assertEquals("PKG002", batchDTO.getItemCode());
        assertEquals("Premium Coffee Beans", batchDTO.getName());
        assertEquals(15.99, batchDTO.getSellingPrice(), 0.001);
        assertEquals(25, batchDTO.getQuantity());
        assertEquals(purchaseDate, batchDTO.getPurchaseDate());
        assertEquals(futureDate, batchDTO.getExpiryDate());
        assertEquals(0, batchDTO.getUsedQuantity());
    }

    @Test
    @DisplayName("Should handle null values in new batch constructor")
    void testNewBatchConstructor_NullValues() {
        batchDTO = new BatchDTO(null, null, 0.0, 0, null, null);

        assertNull(batchDTO.getItemCode());
        assertNull(batchDTO.getName());
        assertEquals(0.0, batchDTO.getSellingPrice(), 0.001);
        assertEquals(0, batchDTO.getQuantity());
        assertNull(batchDTO.getPurchaseDate());
        assertNull(batchDTO.getExpiryDate());
        assertEquals(0, batchDTO.getUsedQuantity());
    }

    @Test
    @DisplayName("Should handle negative values in new batch constructor")
    void testNewBatchConstructor_NegativeValues() {
        batchDTO = new BatchDTO("NEG001", "Test Item", -5.50, -10, pastDate, expiryDate);

        assertEquals("NEG001", batchDTO.getItemCode());
        assertEquals("Test Item", batchDTO.getName());
        assertEquals(-5.50, batchDTO.getSellingPrice(), 0.001);
        assertEquals(-10, batchDTO.getQuantity());
        assertEquals(pastDate, batchDTO.getPurchaseDate());
        assertEquals(expiryDate, batchDTO.getExpiryDate());
    }

    // Constructor tests for existing batches (8-parameter constructor)
    @Test
    @DisplayName("Should create BatchDTO with existing batch constructor - dairy products")
    void testExistingBatchConstructor_DairyProducts() {
        batchDTO = new BatchDTO(101, "DAIRY003", "Fresh Milk 2L", 4.50, 30, purchaseDate, expiryDate, 5);

        assertEquals(101, batchDTO.getId());
        assertEquals("DAIRY003", batchDTO.getItemCode());
        assertEquals("Fresh Milk 2L", batchDTO.getName());
        assertEquals(4.50, batchDTO.getSellingPrice(), 0.001);
        assertEquals(30, batchDTO.getQuantity());
        assertEquals(purchaseDate, batchDTO.getPurchaseDate());
        assertEquals(expiryDate, batchDTO.getExpiryDate());
        assertEquals(5, batchDTO.getUsedQuantity());
    }

    @Test
    @DisplayName("Should create BatchDTO with existing batch constructor - electronics")
    void testExistingBatchConstructor_Electronics() {
        batchDTO = new BatchDTO(202, "ELEC004", "Wireless Headphones", 89.99, 15, purchaseDate, futureDate, 3);

        assertEquals(202, batchDTO.getId());
        assertEquals("ELEC004", batchDTO.getItemCode());
        assertEquals("Wireless Headphones", batchDTO.getName());
        assertEquals(89.99, batchDTO.getSellingPrice(), 0.001);
        assertEquals(15, batchDTO.getQuantity());
        assertEquals(purchaseDate, batchDTO.getPurchaseDate());
        assertEquals(futureDate, batchDTO.getExpiryDate());
        assertEquals(3, batchDTO.getUsedQuantity());
    }

    @Test
    @DisplayName("Should handle maximum values in existing batch constructor")
    void testExistingBatchConstructor_MaxValues() {
        batchDTO = new BatchDTO(Integer.MAX_VALUE, "MAX001", "Max Item", Double.MAX_VALUE,
                Integer.MAX_VALUE, purchaseDate, expiryDate, Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, batchDTO.getId());
        assertEquals("MAX001", batchDTO.getItemCode());
        assertEquals("Max Item", batchDTO.getName());
        assertEquals(Double.MAX_VALUE, batchDTO.getSellingPrice(), 0.001);
        assertEquals(Integer.MAX_VALUE, batchDTO.getQuantity());
        assertEquals(Integer.MAX_VALUE, batchDTO.getUsedQuantity());
    }

    // Constructor tests for simple batch creation (3-parameter constructor)
    @Test
    @DisplayName("Should create simple BatchDTO with current date - medicine")
    void testSimpleBatchConstructor_Medicine() {
        LocalDate currentDate = LocalDate.now();
        batchDTO = new BatchDTO("MED005", 100, expiryDate);

        assertEquals("MED005", batchDTO.getItemCode());
        assertEquals(100, batchDTO.getQuantity());
        assertEquals(expiryDate, batchDTO.getExpiryDate());
        assertEquals(currentDate, batchDTO.getPurchaseDate());
        assertEquals(0, batchDTO.getUsedQuantity());
        assertEquals("", batchDTO.getName());
        assertEquals(0.0, batchDTO.getSellingPrice(), 0.001);
        assertEquals(0, batchDTO.getId());
    }

    @Test
    @DisplayName("Should create simple BatchDTO with null item code")
    void testSimpleBatchConstructor_NullItemCode() {
        batchDTO = new BatchDTO(null, 50, expiryDate);

        assertNull(batchDTO.getItemCode());
        assertEquals(50, batchDTO.getQuantity());
        assertEquals(expiryDate, batchDTO.getExpiryDate());
        assertNotNull(batchDTO.getPurchaseDate());
    }

    @Test
    @DisplayName("Should create simple BatchDTO with zero quantity")
    void testSimpleBatchConstructor_ZeroQuantity() {
        batchDTO = new BatchDTO("ZERO001", 0, expiryDate);

        assertEquals("ZERO001", batchDTO.getItemCode());
        assertEquals(0, batchDTO.getQuantity());
        assertEquals(expiryDate, batchDTO.getExpiryDate());
    }

    // Getter tests
    @Test
    @DisplayName("Should return correct item code from getter")
    void testGetItemCode() {
        batchDTO = new BatchDTO("GET001", "Test Item", 10.0, 20, purchaseDate, expiryDate);
        assertEquals("GET001", batchDTO.getItemCode());
    }

    @Test
    @DisplayName("Should return correct name from getter and getItemName")
    void testGetName_AndGetItemName() {
        batchDTO = new BatchDTO("NAME001", "Test Product Name", 5.0, 10, purchaseDate, expiryDate);

        assertEquals("Test Product Name", batchDTO.getName());
        assertEquals("Test Product Name", batchDTO.getItemName()); // Compatibility method
    }

    @Test
    @DisplayName("Should return correct selling price from getter")
    void testGetSellingPrice() {
        batchDTO = new BatchDTO("PRICE001", "Price Test", 99.99, 5, purchaseDate, expiryDate);
        assertEquals(99.99, batchDTO.getSellingPrice(), 0.001);
    }

    @Test
    @DisplayName("Should return correct quantity from getter")
    void testGetQuantity() {
        batchDTO = new BatchDTO("QTY001", "Quantity Test", 1.0, 75, purchaseDate, expiryDate);
        assertEquals(75, batchDTO.getQuantity());
    }

    @Test
    @DisplayName("Should return correct purchase date from getter")
    void testGetPurchaseDate() {
        batchDTO = new BatchDTO("DATE001", "Date Test", 1.0, 1, purchaseDate, expiryDate);
        assertEquals(purchaseDate, batchDTO.getPurchaseDate());
    }

    @Test
    @DisplayName("Should return correct expiry date from getter")
    void testGetExpiryDate() {
        batchDTO = new BatchDTO("EXP001", "Expiry Test", 1.0, 1, purchaseDate, expiryDate);
        assertEquals(expiryDate, batchDTO.getExpiryDate());
    }

    @Test
    @DisplayName("Should return correct used quantity from getter")
    void testGetUsedQuantity() {
        batchDTO = new BatchDTO(1, "USED001", "Used Test", 1.0, 10, purchaseDate, expiryDate, 3);
        assertEquals(3, batchDTO.getUsedQuantity());
    }

    @Test
    @DisplayName("Should return correct ID from getter")
    void testGetId() {
        batchDTO = new BatchDTO(999, "ID001", "ID Test", 1.0, 1, purchaseDate, expiryDate, 0);
        assertEquals(999, batchDTO.getId());
    }

    // Setter tests
    @Test
    @DisplayName("Should set ID correctly")
    void testSetId() {
        batchDTO = new BatchDTO("SET001", 1, expiryDate);
        batchDTO.setId(555);
        assertEquals(555, batchDTO.getId());
    }

    @Test
    @DisplayName("Should set item code correctly")
    void testSetItemCode() {
        batchDTO = new BatchDTO("OLD001", 1, expiryDate);
        batchDTO.setItemCode("NEW001");
        assertEquals("NEW001", batchDTO.getItemCode());
    }

    @Test
    @DisplayName("Should set name correctly")
    void testSetName() {
        batchDTO = new BatchDTO("NAME001", 1, expiryDate);
        batchDTO.setName("Updated Product Name");
        assertEquals("Updated Product Name", batchDTO.getName());
        assertEquals("Updated Product Name", batchDTO.getItemName());
    }

    @Test
    @DisplayName("Should set selling price correctly")
    void testSetSellingPrice() {
        batchDTO = new BatchDTO("PRICE001", 1, expiryDate);
        batchDTO.setSellingPrice(29.95);
        assertEquals(29.95, batchDTO.getSellingPrice(), 0.001);
    }

    @Test
    @DisplayName("Should set quantity correctly")
    void testSetQuantity() {
        batchDTO = new BatchDTO("QTY001", 1, expiryDate);
        batchDTO.setQuantity(150);
        assertEquals(150, batchDTO.getQuantity());
    }

    @Test
    @DisplayName("Should set purchase date correctly")
    void testSetPurchaseDate() {
        batchDTO = new BatchDTO("DATE001", 1, expiryDate);
        LocalDate newPurchaseDate = LocalDate.of(2024, 6, 15);
        batchDTO.setPurchaseDate(newPurchaseDate);
        assertEquals(newPurchaseDate, batchDTO.getPurchaseDate());
    }

    @Test
    @DisplayName("Should set expiry date correctly")
    void testSetExpiryDate() {
        batchDTO = new BatchDTO("EXP001", 1, expiryDate);
        LocalDate newExpiryDate = LocalDate.of(2025, 3, 20);
        batchDTO.setExpiryDate(newExpiryDate);
        assertEquals(newExpiryDate, batchDTO.getExpiryDate());
    }

    @Test
    @DisplayName("Should set used quantity correctly")
    void testSetUsedQuantity() {
        batchDTO = new BatchDTO("USED001", 1, expiryDate);
        batchDTO.setUsedQuantity(25);
        assertEquals(25, batchDTO.getUsedQuantity());
    }

    // Edge case tests
    @Test
    @DisplayName("Should handle empty strings in setters")
    void testSetters_EmptyStrings() {
        batchDTO = new BatchDTO("TEST001", 1, expiryDate);

        batchDTO.setItemCode("");
        batchDTO.setName("");

        assertEquals("", batchDTO.getItemCode());
        assertEquals("", batchDTO.getName());
        assertEquals("", batchDTO.getItemName());
    }

    @Test
    @DisplayName("Should handle null values in setters")
    void testSetters_NullValues() {
        batchDTO = new BatchDTO("TEST001", 1, expiryDate);

        batchDTO.setItemCode(null);
        batchDTO.setName(null);
        batchDTO.setPurchaseDate(null);
        batchDTO.setExpiryDate(null);

        assertNull(batchDTO.getItemCode());
        assertNull(batchDTO.getName());
        assertNull(batchDTO.getItemName());
        assertNull(batchDTO.getPurchaseDate());
        assertNull(batchDTO.getExpiryDate());
    }

    @Test
    @DisplayName("Should handle very long strings")
    void testStringFields_VeryLongStrings() {
        String longCode = "A".repeat(1000);
        String longName = "B".repeat(2000);

        batchDTO = new BatchDTO(longCode, longName, 1.0, 1, purchaseDate, expiryDate);

        assertEquals(longCode, batchDTO.getItemCode());
        assertEquals(longName, batchDTO.getName());
    }

    @Test
    @DisplayName("Should handle very small decimal values for price")
    void testSellingPrice_VerySmallDecimals() {
        batchDTO = new BatchDTO("SMALL001", "Small Price", 0.001, 1, purchaseDate, expiryDate);
        assertEquals(0.001, batchDTO.getSellingPrice(), 0.0001);

        batchDTO.setSellingPrice(0.999999);
        assertEquals(0.999999, batchDTO.getSellingPrice(), 0.0000001);
    }

    @Test
    @DisplayName("Should handle date edge cases")
    void testDateEdgeCases() {
        LocalDate minDate = LocalDate.MIN;
        LocalDate maxDate = LocalDate.MAX;

        batchDTO = new BatchDTO("DATE001", "Date Edge", 1.0, 1, minDate, maxDate);

        assertEquals(minDate, batchDTO.getPurchaseDate());
        assertEquals(maxDate, batchDTO.getExpiryDate());

        batchDTO.setPurchaseDate(maxDate);
        batchDTO.setExpiryDate(minDate);

        assertEquals(maxDate, batchDTO.getPurchaseDate());
        assertEquals(minDate, batchDTO.getExpiryDate());
    }

    @Test
    @DisplayName("Should maintain state consistency across multiple operations")
    void testStateConsistency() {
        batchDTO = new BatchDTO("CONSISTENCY001", "Test Product", 5.99, 100, purchaseDate, expiryDate);

        // Perform multiple operations
        batchDTO.setId(42);
        batchDTO.setUsedQuantity(25);
        batchDTO.setSellingPrice(7.99);
        batchDTO.setQuantity(75);

        // Verify all values are correctly maintained
        assertEquals(42, batchDTO.getId());
        assertEquals("CONSISTENCY001", batchDTO.getItemCode());
        assertEquals("Test Product", batchDTO.getName());
        assertEquals(7.99, batchDTO.getSellingPrice(), 0.001);
        assertEquals(75, batchDTO.getQuantity());
        assertEquals(25, batchDTO.getUsedQuantity());
        assertEquals(purchaseDate, batchDTO.getPurchaseDate());
        assertEquals(expiryDate, batchDTO.getExpiryDate());
    }
}
