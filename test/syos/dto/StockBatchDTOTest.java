package syos.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockBatchDTO Test Suite")
class StockBatchDTOTest {

    private StockBatchDTO stockBatchDTO;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    private LocalDate futureDate;
    private LocalDate pastDate;
    private LocalDate expiringSoonDate;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        purchaseDate = today.minusDays(30);
        expiryDate = today.plusDays(30);
        futureDate = today.plusDays(365);
        pastDate = today.minusDays(5);
        expiringSoonDate = today.plusDays(5); // Within 7 days
    }

    // Constructor tests - ACTIVE status
    @Test
    @DisplayName("Should create StockBatchDTO with ACTIVE status - fresh produce")
    void testConstructor_ActiveStatus_FreshProduce() {
        stockBatchDTO = new StockBatchDTO(
                "PROD001", "Organic Bananas", 100, 25,
                purchaseDate, expiryDate, 2.99);

        assertEquals("PROD001", stockBatchDTO.getItemCode());
        assertEquals("Organic Bananas", stockBatchDTO.getItemName());
        assertEquals(100, stockBatchDTO.getTotalQuantity());
        assertEquals(25, stockBatchDTO.getUsedQuantity());
        assertEquals(75, stockBatchDTO.getAvailableQuantity()); // 100 - 25
        assertEquals(purchaseDate, stockBatchDTO.getPurchaseDate());
        assertEquals(expiryDate, stockBatchDTO.getExpiryDate());
        assertEquals(2.99, stockBatchDTO.getSellingPrice(), 0.001);
        assertEquals("ACTIVE", stockBatchDTO.getStatus());
    }

    @Test
    @DisplayName("Should create StockBatchDTO with ACTIVE status - packaged goods")
    void testConstructor_ActiveStatus_PackagedGoods() {
        stockBatchDTO = new StockBatchDTO(
                "PKG002", "Premium Coffee Beans", 50, 10,
                purchaseDate, futureDate, 15.99);

        assertEquals("PKG002", stockBatchDTO.getItemCode());
        assertEquals("Premium Coffee Beans", stockBatchDTO.getItemName());
        assertEquals(50, stockBatchDTO.getTotalQuantity());
        assertEquals(10, stockBatchDTO.getUsedQuantity());
        assertEquals(40, stockBatchDTO.getAvailableQuantity());
        assertEquals(purchaseDate, stockBatchDTO.getPurchaseDate());
        assertEquals(futureDate, stockBatchDTO.getExpiryDate());
        assertEquals(15.99, stockBatchDTO.getSellingPrice(), 0.001);
        assertEquals("ACTIVE", stockBatchDTO.getStatus());
    }

    // Constructor tests - DEPLETED status
    @Test
    @DisplayName("Should create StockBatchDTO with DEPLETED status - zero available")
    void testConstructor_DepletedStatus_ZeroAvailable() {
        stockBatchDTO = new StockBatchDTO(
                "DEPL001", "Sold Out Item", 100, 100,
                purchaseDate, expiryDate, 9.99);

        assertEquals("DEPL001", stockBatchDTO.getItemCode());
        assertEquals("Sold Out Item", stockBatchDTO.getItemName());
        assertEquals(100, stockBatchDTO.getTotalQuantity());
        assertEquals(100, stockBatchDTO.getUsedQuantity());
        assertEquals(0, stockBatchDTO.getAvailableQuantity());
        assertEquals("DEPLETED", stockBatchDTO.getStatus());
    }

    @Test
    @DisplayName("Should create StockBatchDTO with DEPLETED status - overused")
    void testConstructor_DepletedStatus_Overused() {
        stockBatchDTO = new StockBatchDTO(
                "OVER001", "Overused Item", 50, 75,
                purchaseDate, expiryDate, 5.50);

        assertEquals("OVER001", stockBatchDTO.getItemCode());
        assertEquals("Overused Item", stockBatchDTO.getItemName());
        assertEquals(50, stockBatchDTO.getTotalQuantity());
        assertEquals(75, stockBatchDTO.getUsedQuantity());
        assertEquals(-25, stockBatchDTO.getAvailableQuantity()); // 50 - 75
        assertEquals("DEPLETED", stockBatchDTO.getStatus());
    }

    // Constructor tests - EXPIRED status
    @Test
    @DisplayName("Should create StockBatchDTO with EXPIRED status - past expiry")
    void testConstructor_ExpiredStatus() {
        stockBatchDTO = new StockBatchDTO(
                "EXP001", "Expired Dairy", 30, 5,
                purchaseDate, pastDate, 4.50);

        assertEquals("EXP001", stockBatchDTO.getItemCode());
        assertEquals("Expired Dairy", stockBatchDTO.getItemName());
        assertEquals(30, stockBatchDTO.getTotalQuantity());
        assertEquals(5, stockBatchDTO.getUsedQuantity());
        assertEquals(25, stockBatchDTO.getAvailableQuantity());
        assertEquals(pastDate, stockBatchDTO.getExpiryDate());
        assertEquals("EXPIRED", stockBatchDTO.getStatus());
    }

    @Test
    @DisplayName("Should create StockBatchDTO with EXPIRED status - yesterday expiry")
    void testConstructor_ExpiredStatus_Yesterday() {
        LocalDate yesterday = today.minusDays(1);
        stockBatchDTO = new StockBatchDTO(
                "YEST001", "Yesterday Expired", 20, 0,
                purchaseDate, yesterday, 3.25);

        assertEquals("YEST001", stockBatchDTO.getItemCode());
        assertEquals("Yesterday Expired", stockBatchDTO.getItemName());
        assertEquals(yesterday, stockBatchDTO.getExpiryDate());
        assertEquals("EXPIRED", stockBatchDTO.getStatus());
    }

    // Constructor tests - EXPIRING_SOON status
    @Test
    @DisplayName("Should create StockBatchDTO with EXPIRING_SOON status - within 7 days")
    void testConstructor_ExpiringSoonStatus() {
        stockBatchDTO = new StockBatchDTO(
                "SOON001", "Expiring Soon Item", 40, 10,
                purchaseDate, expiringSoonDate, 7.99);

        assertEquals("SOON001", stockBatchDTO.getItemCode());
        assertEquals("Expiring Soon Item", stockBatchDTO.getItemName());
        assertEquals(40, stockBatchDTO.getTotalQuantity());
        assertEquals(10, stockBatchDTO.getUsedQuantity());
        assertEquals(30, stockBatchDTO.getAvailableQuantity());
        assertEquals(expiringSoonDate, stockBatchDTO.getExpiryDate());
        assertEquals("EXPIRING_SOON", stockBatchDTO.getStatus());
    }

    // Priority of status determination - EXPIRED takes precedence over DEPLETED
    @Test
    @DisplayName("Should prioritize EXPIRED status over DEPLETED when both conditions apply")
    void testStatusPriority_ExpiredOverDepleted() {
        stockBatchDTO = new StockBatchDTO(
                "PRIORITY001", "Expired and Depleted", 50, 50,
                purchaseDate, pastDate, 8.75);

        assertEquals("PRIORITY001", stockBatchDTO.getItemCode());
        assertEquals(0, stockBatchDTO.getAvailableQuantity()); // Should be depleted
        assertTrue(stockBatchDTO.getExpiryDate().isBefore(today)); // Should be expired
        assertEquals("EXPIRED", stockBatchDTO.getStatus()); // EXPIRED takes priority
    }

    // Constructor with null values
    @Test
    @DisplayName("Should handle null string values in constructor")
    void testConstructor_NullStringValues() {
        stockBatchDTO = new StockBatchDTO(
                null, null, 10, 5,
                purchaseDate, expiryDate, 1.0);

        assertNull(stockBatchDTO.getItemCode());
        assertNull(stockBatchDTO.getItemName());
        assertEquals(10, stockBatchDTO.getTotalQuantity());
        assertEquals(5, stockBatchDTO.getUsedQuantity());
        assertEquals(5, stockBatchDTO.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should handle null date values in constructor")
    void testConstructor_NullDateValues() {
        // This will cause NullPointerException in status calculation, which is expected
        // behavior
        assertThrows(NullPointerException.class, () -> {
            stockBatchDTO = new StockBatchDTO(
                    "NULL001", "Null Dates", 10, 5,
                    null, null, 1.0);
        });
    }

    // Getter tests
    @Test
    @DisplayName("Should return correct item code from getter")
    void testGetItemCode() {
        stockBatchDTO = new StockBatchDTO(
                "CODE123", "Test Item", 10, 2,
                purchaseDate, expiryDate, 5.0);
        assertEquals("CODE123", stockBatchDTO.getItemCode());
    }

    @Test
    @DisplayName("Should return correct item name from getter")
    void testGetItemName() {
        stockBatchDTO = new StockBatchDTO(
                "NAME001", "Detailed Product Name", 10, 2,
                purchaseDate, expiryDate, 5.0);
        assertEquals("Detailed Product Name", stockBatchDTO.getItemName());
    }

    @Test
    @DisplayName("Should return correct total quantity from getter")
    void testGetTotalQuantity() {
        stockBatchDTO = new StockBatchDTO(
                "QTY001", "Quantity Test", 150, 50,
                purchaseDate, expiryDate, 5.0);
        assertEquals(150, stockBatchDTO.getTotalQuantity());
    }

    @Test
    @DisplayName("Should return correct used quantity from getter")
    void testGetUsedQuantity() {
        stockBatchDTO = new StockBatchDTO(
                "USED001", "Used Test", 100, 35,
                purchaseDate, expiryDate, 5.0);
        assertEquals(35, stockBatchDTO.getUsedQuantity());
    }

    @Test
    @DisplayName("Should return correct available quantity from getter")
    void testGetAvailableQuantity() {
        stockBatchDTO = new StockBatchDTO(
                "AVAIL001", "Available Test", 80, 20,
                purchaseDate, expiryDate, 5.0);
        assertEquals(60, stockBatchDTO.getAvailableQuantity()); // 80 - 20
    }

    @Test
    @DisplayName("Should return correct purchase date from getter")
    void testGetPurchaseDate() {
        stockBatchDTO = new StockBatchDTO(
                "PDATE001", "Purchase Date Test", 10, 2,
                purchaseDate, expiryDate, 5.0);
        assertEquals(purchaseDate, stockBatchDTO.getPurchaseDate());
    }

    @Test
    @DisplayName("Should return correct expiry date from getter")
    void testGetExpiryDate() {
        stockBatchDTO = new StockBatchDTO(
                "EDATE001", "Expiry Date Test", 10, 2,
                purchaseDate, expiryDate, 5.0);
        assertEquals(expiryDate, stockBatchDTO.getExpiryDate());
    }

    @Test
    @DisplayName("Should return correct selling price from getter")
    void testGetSellingPrice() {
        stockBatchDTO = new StockBatchDTO(
                "PRICE001", "Price Test", 10, 2,
                purchaseDate, expiryDate, 19.99);
        assertEquals(19.99, stockBatchDTO.getSellingPrice(), 0.001);
    }

    @Test
    @DisplayName("Should return correct status from getter")
    void testGetStatus() {
        stockBatchDTO = new StockBatchDTO(
                "STATUS001", "Status Test", 10, 2,
                purchaseDate, expiryDate, 5.0);
        assertEquals("ACTIVE", stockBatchDTO.getStatus());
    }

    // Business logic test - getDaysToExpiry()

    @Test
    @DisplayName("Should calculate days to expiry correctly - past date")
    void testGetDaysToExpiry_PastDate() {
        LocalDate pastExpiryDate = today.minusDays(10);
        stockBatchDTO = new StockBatchDTO(
                "PAST001", "Past Item", 10, 2,
                purchaseDate, pastExpiryDate, 5.0);
        assertEquals(-10, stockBatchDTO.getDaysToExpiry());
    }

    @Test
    @DisplayName("Should calculate days to expiry correctly - today")
    void testGetDaysToExpiry_Today() {
        stockBatchDTO = new StockBatchDTO(
                "TODAY001", "Today Item", 10, 2,
                purchaseDate, today, 5.0);
        assertEquals(0, stockBatchDTO.getDaysToExpiry());
    }

    @Test
    @DisplayName("Should calculate days to expiry correctly - tomorrow")
    void testGetDaysToExpiry_Tomorrow() {
        LocalDate tomorrow = today.plusDays(1);
        stockBatchDTO = new StockBatchDTO(
                "TOMORROW001", "Tomorrow Item", 10, 2,
                purchaseDate, tomorrow, 5.0);
        assertEquals(1, stockBatchDTO.getDaysToExpiry());
    }

    // Edge case tests
    @Test
    @DisplayName("Should handle empty strings in constructor")
    void testConstructor_EmptyStrings() {
        stockBatchDTO = new StockBatchDTO(
                "", "", 10, 2,
                purchaseDate, expiryDate, 5.0);

        assertEquals("", stockBatchDTO.getItemCode());
        assertEquals("", stockBatchDTO.getItemName());
    }

    @Test
    @DisplayName("Should handle very long strings")
    void testConstructor_VeryLongStrings() {
        String longCode = "A".repeat(1000);
        String longName = "B".repeat(2000);

        stockBatchDTO = new StockBatchDTO(
                longCode, longName, 10, 2,
                purchaseDate, expiryDate, 5.0);

        assertEquals(longCode, stockBatchDTO.getItemCode());
        assertEquals(longName, stockBatchDTO.getItemName());
    }

    @Test
    @DisplayName("Should handle negative quantities")
    void testConstructor_NegativeQuantities() {
        stockBatchDTO = new StockBatchDTO(
                "NEG001", "Negative Item", -10, -5,
                purchaseDate, expiryDate, 5.0);

        assertEquals(-10, stockBatchDTO.getTotalQuantity());
        assertEquals(-5, stockBatchDTO.getUsedQuantity());
        assertEquals(-5, stockBatchDTO.getAvailableQuantity()); // -10 - (-5)
    }

    @Test
    @DisplayName("Should handle zero quantities")
    void testConstructor_ZeroQuantities() {
        stockBatchDTO = new StockBatchDTO(
                "ZERO001", "Zero Item", 0, 0,
                purchaseDate, expiryDate, 5.0);

        assertEquals(0, stockBatchDTO.getTotalQuantity());
        assertEquals(0, stockBatchDTO.getUsedQuantity());
        assertEquals(0, stockBatchDTO.getAvailableQuantity());
        assertEquals("DEPLETED", stockBatchDTO.getStatus()); // 0 available = DEPLETED
    }

    @Test
    @DisplayName("Should handle maximum integer values")
    void testConstructor_MaxIntegerValues() {
        stockBatchDTO = new StockBatchDTO(
                "MAX001", "Max Item", Integer.MAX_VALUE, Integer.MAX_VALUE,
                purchaseDate, expiryDate, 5.0);

        assertEquals(Integer.MAX_VALUE, stockBatchDTO.getTotalQuantity());
        assertEquals(Integer.MAX_VALUE, stockBatchDTO.getUsedQuantity());
        assertEquals(0, stockBatchDTO.getAvailableQuantity()); // MAX_VALUE - MAX_VALUE
        assertEquals("DEPLETED", stockBatchDTO.getStatus());
    }

    @Test
    @DisplayName("Should handle very small decimal values for price")
    void testConstructor_VerySmallPrice() {
        stockBatchDTO = new StockBatchDTO(
                "SMALL001", "Small Price", 10, 2,
                purchaseDate, expiryDate, 0.001);
        assertEquals(0.001, stockBatchDTO.getSellingPrice(), 0.0001);
    }

    @Test
    @DisplayName("Should handle very large decimal values for price")
    void testConstructor_VeryLargePrice() {
        stockBatchDTO = new StockBatchDTO(
                "LARGE001", "Large Price", 10, 2,
                purchaseDate, expiryDate, Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, stockBatchDTO.getSellingPrice(), 0.001);
    }

    @Test
    @DisplayName("Should handle negative price")
    void testConstructor_NegativePrice() {
        stockBatchDTO = new StockBatchDTO(
                "NEGPRICE001", "Negative Price", 10, 2,
                purchaseDate, expiryDate, -5.99);
        assertEquals(-5.99, stockBatchDTO.getSellingPrice(), 0.001);
    }

    @Test
    @DisplayName("Should handle date edge cases")
    void testConstructor_DateEdgeCases() {
        LocalDate minDate = LocalDate.MIN;
        LocalDate maxDate = LocalDate.MAX;

        stockBatchDTO = new StockBatchDTO(
                "DATE001", "Date Edge", 10, 2,
                minDate, maxDate, 5.0);

        assertEquals(minDate, stockBatchDTO.getPurchaseDate());
        assertEquals(maxDate, stockBatchDTO.getExpiryDate());
        assertEquals("ACTIVE", stockBatchDTO.getStatus()); // Far future date
    }

    @Test
    @DisplayName("Should maintain immutability - object state cannot be changed")
    void testImmutability() {
        stockBatchDTO = new StockBatchDTO(
                "IMMUTABLE001", "Immutable Item", 100, 25,
                purchaseDate, expiryDate, 9.99);

        // Store original values
        String originalCode = stockBatchDTO.getItemCode();
        String originalName = stockBatchDTO.getItemName();
        int originalTotal = stockBatchDTO.getTotalQuantity();
        int originalUsed = stockBatchDTO.getUsedQuantity();
        int originalAvailable = stockBatchDTO.getAvailableQuantity();
        LocalDate originalPurchase = stockBatchDTO.getPurchaseDate();
        LocalDate originalExpiry = stockBatchDTO.getExpiryDate();
        double originalPrice = stockBatchDTO.getSellingPrice();
        String originalStatus = stockBatchDTO.getStatus();

        // Verify values haven't changed (immutable)
        assertEquals(originalCode, stockBatchDTO.getItemCode());
        assertEquals(originalName, stockBatchDTO.getItemName());
        assertEquals(originalTotal, stockBatchDTO.getTotalQuantity());
        assertEquals(originalUsed, stockBatchDTO.getUsedQuantity());
        assertEquals(originalAvailable, stockBatchDTO.getAvailableQuantity());
        assertEquals(originalPurchase, stockBatchDTO.getPurchaseDate());
        assertEquals(originalExpiry, stockBatchDTO.getExpiryDate());
        assertEquals(originalPrice, stockBatchDTO.getSellingPrice(), 0.001);
        assertEquals(originalStatus, stockBatchDTO.getStatus());
    }

    @Test
    @DisplayName("Should handle boundary case for EXPIRING_SOON status - exactly 6 days")
    void testExpiringSoonBoundary_SixDays() {
        LocalDate sixDaysFromNow = today.plusDays(6);
        stockBatchDTO = new StockBatchDTO(
                "SIX001", "Six Days Item", 10, 2,
                purchaseDate, sixDaysFromNow, 5.0);
        assertEquals("EXPIRING_SOON", stockBatchDTO.getStatus());
    }

    @Test
    @DisplayName("Should handle boundary case for ACTIVE status - exactly 8 days")
    void testActiveBoundary_EightDays() {
        LocalDate eightDaysFromNow = today.plusDays(8);
        stockBatchDTO = new StockBatchDTO(
                "EIGHT001", "Eight Days Item", 10, 2,
                purchaseDate, eightDaysFromNow, 5.0);
        assertEquals("ACTIVE", stockBatchDTO.getStatus());
    }
}
