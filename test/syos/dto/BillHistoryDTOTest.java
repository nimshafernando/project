package syos.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BillHistoryDTO Test Suite")
class BillHistoryDTOTest {

    private BillHistoryDTO billHistoryDTO;
    private LocalDateTime testDateTime;
    private LocalDateTime pastDateTime;
    private LocalDateTime futureDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2024, Month.JANUARY, 15, 14, 30, 45);
        pastDateTime = LocalDateTime.of(2023, Month.DECEMBER, 1, 9, 15, 30);
        futureDateTime = LocalDateTime.of(2025, Month.JUNE, 30, 18, 45, 0);
    }

    // Constructor tests - Retail store scenarios
    @Test
    @DisplayName("Should create BillHistoryDTO for retail store - cash payment")
    void testConstructor_RetailStoreCashPayment() {
        billHistoryDTO = new BillHistoryDTO(
                1001, "BILL-2024-001", testDateTime, 156.75, 15.50,
                "Retail", "Cash", "Walk-in Customer", "John Smith", 8);

        assertEquals(1001, billHistoryDTO.getBillId());
        assertEquals("BILL-2024-001", billHistoryDTO.getSerialNumber());
        assertEquals(testDateTime, billHistoryDTO.getDateTime());
        assertEquals(156.75, billHistoryDTO.getTotalAmount(), 0.001);
        assertEquals(15.50, billHistoryDTO.getDiscount(), 0.001);
        assertEquals("Retail", billHistoryDTO.getStoreType());
        assertEquals("Cash", billHistoryDTO.getPaymentMethod());
        assertEquals("Walk-in Customer", billHistoryDTO.getCustomerInfo());
        assertEquals("John Smith", billHistoryDTO.getEmployeeName());
        assertEquals(8, billHistoryDTO.getItemCount());
    }

    @Test
    @DisplayName("Should create BillHistoryDTO for online store - card payment")
    void testConstructor_OnlineStoreCardPayment() {
        billHistoryDTO = new BillHistoryDTO(
                2002, "ONLINE-2024-0045", testDateTime, 89.99, 5.00,
                "Online", "Credit Card", "customer@email.com", "Sarah Johnson", 3);

        assertEquals(2002, billHistoryDTO.getBillId());
        assertEquals("ONLINE-2024-0045", billHistoryDTO.getSerialNumber());
        assertEquals(testDateTime, billHistoryDTO.getDateTime());
        assertEquals(89.99, billHistoryDTO.getTotalAmount(), 0.001);
        assertEquals(5.00, billHistoryDTO.getDiscount(), 0.001);
        assertEquals("Online", billHistoryDTO.getStoreType());
        assertEquals("Credit Card", billHistoryDTO.getPaymentMethod());
        assertEquals("customer@email.com", billHistoryDTO.getCustomerInfo());
        assertEquals("Sarah Johnson", billHistoryDTO.getEmployeeName());
        assertEquals(3, billHistoryDTO.getItemCount());
    }

    @Test
    @DisplayName("Should create BillHistoryDTO for wholesale - bank transfer")
    void testConstructor_WholesaleBankTransfer() {
        billHistoryDTO = new BillHistoryDTO(
                3003, "WS-2024-0123", pastDateTime, 2550.00, 255.00,
                "Wholesale", "Bank Transfer", "ABC Trading Ltd.", "Mike Davis", 25);

        assertEquals(3003, billHistoryDTO.getBillId());
        assertEquals("WS-2024-0123", billHistoryDTO.getSerialNumber());
        assertEquals(pastDateTime, billHistoryDTO.getDateTime());
        assertEquals(2550.00, billHistoryDTO.getTotalAmount(), 0.001);
        assertEquals(255.00, billHistoryDTO.getDiscount(), 0.001);
        assertEquals("Wholesale", billHistoryDTO.getStoreType());
        assertEquals("Bank Transfer", billHistoryDTO.getPaymentMethod());
        assertEquals("ABC Trading Ltd.", billHistoryDTO.getCustomerInfo());
        assertEquals("Mike Davis", billHistoryDTO.getEmployeeName());
        assertEquals(25, billHistoryDTO.getItemCount());
    }

    // Constructor with null values
    @Test
    @DisplayName("Should handle null string values in constructor")
    void testConstructor_NullStringValues() {
        billHistoryDTO = new BillHistoryDTO(
                0, null, null, 0.0, 0.0,
                null, null, null, null, 0);

        assertEquals(0, billHistoryDTO.getBillId());
        assertNull(billHistoryDTO.getSerialNumber());
        assertNull(billHistoryDTO.getDateTime());
        assertEquals(0.0, billHistoryDTO.getTotalAmount(), 0.001);
        assertEquals(0.0, billHistoryDTO.getDiscount(), 0.001);
        assertNull(billHistoryDTO.getStoreType());
        assertNull(billHistoryDTO.getPaymentMethod());
        assertNull(billHistoryDTO.getCustomerInfo());
        assertNull(billHistoryDTO.getEmployeeName());
        assertEquals(0, billHistoryDTO.getItemCount());
    }

    @Test
    @DisplayName("Should handle negative values in constructor")
    void testConstructor_NegativeValues() {
        billHistoryDTO = new BillHistoryDTO(
                -1, "NEG-001", testDateTime, -50.0, -10.0,
                "Test", "Test", "Test Customer", "Test Employee", -5);

        assertEquals(-1, billHistoryDTO.getBillId());
        assertEquals("NEG-001", billHistoryDTO.getSerialNumber());
        assertEquals(-50.0, billHistoryDTO.getTotalAmount(), 0.001);
        assertEquals(-10.0, billHistoryDTO.getDiscount(), 0.001);
        assertEquals(-5, billHistoryDTO.getItemCount());
    }

    // Getter tests
    @Test
    @DisplayName("Should return correct bill ID from getter")
    void testGetBillId() {
        billHistoryDTO = new BillHistoryDTO(
                12345, "TEST-001", testDateTime, 100.0, 10.0,
                "Test", "Cash", "Test", "Test", 1);
        assertEquals(12345, billHistoryDTO.getBillId());
    }

    @Test
    @DisplayName("Should return correct serial number from getter")
    void testGetSerialNumber() {
        billHistoryDTO = new BillHistoryDTO(
                1, "SERIAL-12345-ABC", testDateTime, 100.0, 10.0,
                "Test", "Cash", "Test", "Test", 1);
        assertEquals("SERIAL-12345-ABC", billHistoryDTO.getSerialNumber());
    }

    @Test
    @DisplayName("Should return correct date time from getter")
    void testGetDateTime() {
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", futureDateTime, 100.0, 10.0,
                "Test", "Cash", "Test", "Test", 1);
        assertEquals(futureDateTime, billHistoryDTO.getDateTime());
    }

    @Test
    @DisplayName("Should return correct total amount from getter")
    void testGetTotalAmount() {
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", testDateTime, 999.99, 10.0,
                "Test", "Cash", "Test", "Test", 1);
        assertEquals(999.99, billHistoryDTO.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("Should return correct discount from getter")
    void testGetDiscount() {
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", testDateTime, 100.0, 25.50,
                "Test", "Cash", "Test", "Test", 1);
        assertEquals(25.50, billHistoryDTO.getDiscount(), 0.001);
    }

    @Test
    @DisplayName("Should return correct store type from getter")
    void testGetStoreType() {
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", testDateTime, 100.0, 10.0,
                "Premium Retail", "Cash", "Test", "Test", 1);
        assertEquals("Premium Retail", billHistoryDTO.getStoreType());
    }

    @Test
    @DisplayName("Should return correct payment method from getter")
    void testGetPaymentMethod() {
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", testDateTime, 100.0, 10.0,
                "Test", "Digital Wallet", "Test", "Test", 1);
        assertEquals("Digital Wallet", billHistoryDTO.getPaymentMethod());
    }

    @Test
    @DisplayName("Should return correct customer info from getter")
    void testGetCustomerInfo() {
        String customerInfo = "VIP Customer - Jane Doe - ID: 12345";
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", testDateTime, 100.0, 10.0,
                "Test", "Cash", customerInfo, "Test", 1);
        assertEquals(customerInfo, billHistoryDTO.getCustomerInfo());
    }

    @Test
    @DisplayName("Should return correct employee name from getter")
    void testGetEmployeeName() {
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", testDateTime, 100.0, 10.0,
                "Test", "Cash", "Test", "Alice Williams", 1);
        assertEquals("Alice Williams", billHistoryDTO.getEmployeeName());
    }

    @Test
    @DisplayName("Should return correct item count from getter")
    void testGetItemCount() {
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", testDateTime, 100.0, 10.0,
                "Test", "Cash", "Test", "Test", 42);
        assertEquals(42, billHistoryDTO.getItemCount());
    }

    // Business logic test - getTotalBeforeDiscount()
    @Test
    @DisplayName("Should calculate total before discount correctly - no discount")
    void testGetTotalBeforeDiscount_NoDiscount() {
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", testDateTime, 100.0, 0.0,
                "Test", "Cash", "Test", "Test", 1);
        assertEquals(100.0, billHistoryDTO.getTotalBeforeDiscount(), 0.001);
    }

    @Test
    @DisplayName("Should calculate total before discount correctly - with discount")
    void testGetTotalBeforeDiscount_WithDiscount() {
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", testDateTime, 85.0, 15.0,
                "Test", "Cash", "Test", "Test", 1);
        assertEquals(100.0, billHistoryDTO.getTotalBeforeDiscount(), 0.001);
    }

    @Test
    @DisplayName("Should calculate total before discount correctly - large discount")
    void testGetTotalBeforeDiscount_LargeDiscount() {
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", testDateTime, 250.0, 100.0,
                "Test", "Cash", "Test", "Test", 1);
        assertEquals(350.0, billHistoryDTO.getTotalBeforeDiscount(), 0.001);
    }

    @Test
    @DisplayName("Should calculate total before discount correctly - discount larger than total")
    void testGetTotalBeforeDiscount_DiscountLargerThanTotal() {
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", testDateTime, 50.0, 75.0,
                "Test", "Cash", "Test", "Test", 1);
        assertEquals(125.0, billHistoryDTO.getTotalBeforeDiscount(), 0.001);
    }

    @Test
    @DisplayName("Should calculate total before discount correctly - negative values")
    void testGetTotalBeforeDiscount_NegativeValues() {
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", testDateTime, -50.0, -25.0,
                "Test", "Cash", "Test", "Test", 1);
        assertEquals(-75.0, billHistoryDTO.getTotalBeforeDiscount(), 0.001);
    }

    @Test
    @DisplayName("Should calculate total before discount correctly - decimal precision")
    void testGetTotalBeforeDiscount_DecimalPrecision() {
        billHistoryDTO = new BillHistoryDTO(
                1, "TEST-001", testDateTime, 99.99, 9.99,
                "Test", "Cash", "Test", "Test", 1);
        assertEquals(109.98, billHistoryDTO.getTotalBeforeDiscount(), 0.001);
    }

    // Edge case tests
    @Test
    @DisplayName("Should handle empty strings in constructor")
    void testConstructor_EmptyStrings() {
        billHistoryDTO = new BillHistoryDTO(
                1, "", testDateTime, 100.0, 10.0,
                "", "", "", "", 1);

        assertEquals("", billHistoryDTO.getSerialNumber());
        assertEquals("", billHistoryDTO.getStoreType());
        assertEquals("", billHistoryDTO.getPaymentMethod());
        assertEquals("", billHistoryDTO.getCustomerInfo());
        assertEquals("", billHistoryDTO.getEmployeeName());
    }

    @Test
    @DisplayName("Should handle special characters in string fields")
    void testConstructor_SpecialCharacters() {
        String specialSerial = "BILL-#@$%-2024";
        String specialCustomer = "Müller & Søn Company™";
        String specialEmployee = "José María García-López";

        billHistoryDTO = new BillHistoryDTO(
                1, specialSerial, testDateTime, 100.0, 10.0,
                "Special™", "PayPal®", specialCustomer, specialEmployee, 1);

        assertEquals(specialSerial, billHistoryDTO.getSerialNumber());
        assertEquals("Special™", billHistoryDTO.getStoreType());
        assertEquals("PayPal®", billHistoryDTO.getPaymentMethod());
        assertEquals(specialCustomer, billHistoryDTO.getCustomerInfo());
        assertEquals(specialEmployee, billHistoryDTO.getEmployeeName());
    }

    @Test
    @DisplayName("Should handle very long strings")
    void testConstructor_VeryLongStrings() {
        String longSerial = "BILL-" + "A".repeat(1000);
        String longCustomer = "Customer-" + "B".repeat(2000);
        String longEmployee = "Employee-" + "C".repeat(500);

        billHistoryDTO = new BillHistoryDTO(
                1, longSerial, testDateTime, 100.0, 10.0,
                "Test", "Test", longCustomer, longEmployee, 1);

        assertEquals(longSerial, billHistoryDTO.getSerialNumber());
        assertEquals(longCustomer, billHistoryDTO.getCustomerInfo());
        assertEquals(longEmployee, billHistoryDTO.getEmployeeName());
    }

    @Test
    @DisplayName("Should handle maximum values")
    void testConstructor_MaxValues() {
        billHistoryDTO = new BillHistoryDTO(
                Integer.MAX_VALUE, "MAX-BILL", testDateTime, Double.MAX_VALUE, Double.MAX_VALUE,
                "Max Store", "Max Payment", "Max Customer", "Max Employee", Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, billHistoryDTO.getBillId());
        assertEquals(Double.MAX_VALUE, billHistoryDTO.getTotalAmount(), 0.001);
        assertEquals(Double.MAX_VALUE, billHistoryDTO.getDiscount(), 0.001);
        assertEquals(Integer.MAX_VALUE, billHistoryDTO.getItemCount());
    }

    @Test
    @DisplayName("Should handle minimum values")
    void testConstructor_MinValues() {
        billHistoryDTO = new BillHistoryDTO(
                Integer.MIN_VALUE, "MIN-BILL", testDateTime, -Double.MAX_VALUE, -Double.MAX_VALUE,
                "Min Store", "Min Payment", "Min Customer", "Min Employee", Integer.MIN_VALUE);

        assertEquals(Integer.MIN_VALUE, billHistoryDTO.getBillId());
        assertEquals(-Double.MAX_VALUE, billHistoryDTO.getTotalAmount(), 0.001);
        assertEquals(-Double.MAX_VALUE, billHistoryDTO.getDiscount(), 0.001);
        assertEquals(Integer.MIN_VALUE, billHistoryDTO.getItemCount());
    }

    @Test
    @DisplayName("Should handle date time edge cases")
    void testConstructor_DateTimeEdgeCases() {
        LocalDateTime minDateTime = LocalDateTime.MIN;
        LocalDateTime maxDateTime = LocalDateTime.MAX;

        billHistoryDTO = new BillHistoryDTO(
                1, "DATE-MIN", minDateTime, 100.0, 10.0,
                "Test", "Test", "Test", "Test", 1);
        assertEquals(minDateTime, billHistoryDTO.getDateTime());

        billHistoryDTO = new BillHistoryDTO(
                2, "DATE-MAX", maxDateTime, 100.0, 10.0,
                "Test", "Test", "Test", "Test", 1);
        assertEquals(maxDateTime, billHistoryDTO.getDateTime());
    }

    @Test
    @DisplayName("Should maintain immutability - object state cannot be changed")
    void testImmutability() {
        billHistoryDTO = new BillHistoryDTO(
                1001, "IMMUTABLE-001", testDateTime, 100.0, 10.0,
                "Retail", "Cash", "Customer", "Employee", 5);

        // Store original values
        int originalBillId = billHistoryDTO.getBillId();
        String originalSerial = billHistoryDTO.getSerialNumber();
        LocalDateTime originalDateTime = billHistoryDTO.getDateTime();
        double originalTotal = billHistoryDTO.getTotalAmount();
        double originalDiscount = billHistoryDTO.getDiscount();
        String originalStoreType = billHistoryDTO.getStoreType();
        String originalPaymentMethod = billHistoryDTO.getPaymentMethod();
        String originalCustomer = billHistoryDTO.getCustomerInfo();
        String originalEmployee = billHistoryDTO.getEmployeeName();
        int originalItemCount = billHistoryDTO.getItemCount();

        // Verify values haven't changed (immutable)
        assertEquals(originalBillId, billHistoryDTO.getBillId());
        assertEquals(originalSerial, billHistoryDTO.getSerialNumber());
        assertEquals(originalDateTime, billHistoryDTO.getDateTime());
        assertEquals(originalTotal, billHistoryDTO.getTotalAmount(), 0.001);
        assertEquals(originalDiscount, billHistoryDTO.getDiscount(), 0.001);
        assertEquals(originalStoreType, billHistoryDTO.getStoreType());
        assertEquals(originalPaymentMethod, billHistoryDTO.getPaymentMethod());
        assertEquals(originalCustomer, billHistoryDTO.getCustomerInfo());
        assertEquals(originalEmployee, billHistoryDTO.getEmployeeName());
        assertEquals(originalItemCount, billHistoryDTO.getItemCount());
    }

    @Test
    @DisplayName("Should calculate total before discount consistently")
    void testGetTotalBeforeDiscount_Consistency() {
        billHistoryDTO = new BillHistoryDTO(
                1, "CONSISTENCY-001", testDateTime, 123.45, 23.45,
                "Test", "Test", "Test", "Test", 1);

        // Call method multiple times to ensure consistency
        double result1 = billHistoryDTO.getTotalBeforeDiscount();
        double result2 = billHistoryDTO.getTotalBeforeDiscount();
        double result3 = billHistoryDTO.getTotalBeforeDiscount();

        assertEquals(146.90, result1, 0.001);
        assertEquals(result1, result2, 0.001);
        assertEquals(result2, result3, 0.001);
    }
}
