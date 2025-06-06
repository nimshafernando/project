package syos.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import syos.dto.ItemDTO;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test coverage for Bill model class with State pattern
 * Tests all constructors, state transitions, business logic, and edge cases
 * with Mockito
 */
class BillTest {

    @Mock
    private CartItem mockCartItem1;

    @Mock
    private CartItem mockCartItem2;

    private Bill bill;
    private Bill onlineBill;
    private Bill emptyBill;
    private List<CartItem> testItems;
    private List<CartItem> mockItems;
    private LocalDateTime testDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testDate = LocalDateTime.of(2025, 6, 5, 14, 30, 0);

        // Setup real test data
        ItemDTO item1 = new ItemDTO("A001", "Premium Coffee", 100.0, 10);
        ItemDTO item2 = new ItemDTO("A002", "Artisan Bread", 50.0, 20);

        testItems = Arrays.asList(
                new CartItem(item1, 2),
                new CartItem(item2, 3));

        // Setup mock items
        when(mockCartItem1.getTotalPrice()).thenReturn(150.0);
        when(mockCartItem2.getTotalPrice()).thenReturn(75.0);
        mockItems = Arrays.asList(mockCartItem1, mockCartItem2);

        // Create different bill types
        bill = new Bill(1, testDate, testItems, 350.0, 25.0, 400.0, 50.0);
        onlineBill = new Bill("ONL-001", testDate, testItems, 350.0, 0.0, 400.0, 50.0);
        emptyBill = new Bill();
    } // === Constructor Tests ===

    @Test
    @DisplayName("Should create in-store bill with serial number")
    void testInStoreBillConstructor() {
        // Act & Assert
        assertEquals(1, bill.getSerialNumber());
        assertEquals(testDate, bill.getDate());
        assertEquals(testItems, bill.getItems());
        assertEquals(350.0, bill.getTotal());
        assertEquals(25.0, bill.getDiscount());
        assertEquals(400.0, bill.getCashTendered());
        assertEquals(50.0, bill.getChange());
        assertEquals("store", bill.getSource());
        assertEquals("CREATED", bill.getCurrentStateName());
        assertNotNull(bill.getLastStateChange());
        assertEquals("Bill created", bill.getStateChangeReason());
    }

    @Test
    @DisplayName("Should create online bill with string serial")
    void testOnlineBillConstructor() {
        // Act & Assert
        assertEquals("ONL-001", onlineBill.getSerial());
        assertEquals(testDate, onlineBill.getDate());
        assertEquals(testItems, onlineBill.getItems());
        assertEquals(350.0, onlineBill.getTotal());
        assertEquals(0.0, onlineBill.getDiscount());
        assertEquals("online", onlineBill.getSource());
        assertEquals("CREATED", onlineBill.getCurrentStateName());
    }

    @Test
    @DisplayName("Should create bill with employee name")
    void testBillWithEmployeeConstructor() {
        // Arrange & Act
        Bill employeeBill = new Bill(2, testDate, testItems, 350.0, 25.0, 400.0, 50.0, "John Manager");

        // Assert
        assertEquals(2, employeeBill.getSerialNumber());
        assertEquals("John Manager", employeeBill.getEmployeeName());
        assertEquals("store", employeeBill.getSource());
        assertEquals("CREATED", employeeBill.getCurrentStateName());
    }

    @Test
    @DisplayName("Should create empty bill with default state")
    void testEmptyBillConstructor() {
        // Act & Assert
        assertEquals("CREATED", emptyBill.getCurrentStateName());
        assertEquals("Bill initialized", emptyBill.getStateChangeReason());
        assertNotNull(emptyBill.getLastStateChange());
        assertTrue(emptyBill.canModify());
    }

    // === State Management Tests ===

    @Test
    @DisplayName("Should create bill in CREATED state")
    void testInitialState() {
        assertEquals("CREATED", bill.getCurrentStateName());
        assertEquals("Bill created and pending processing", bill.getCurrentStateDescription());
        assertTrue(bill.canModify());
        assertFalse(bill.canRefund());
        assertTrue(bill.canCancel());
    }

    @Test
    @DisplayName("Should transition to PROCESSED state")
    void testProcessBill() {
        // Act
        bill.processBill();

        // Assert
        assertEquals("PROCESSED", bill.getCurrentStateName());
        assertEquals("Bill processed and payment completed", bill.getCurrentStateDescription());
        assertFalse(bill.canModify());
        assertTrue(bill.canRefund());
        assertFalse(bill.canCancel());
        assertEquals("Bill processed and finalized", bill.getStateChangeReason());
    }

    @Test
    @DisplayName("Should not allow processing already processed bill")
    void testDoubleProcess() {
        // Arrange
        bill.processBill();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> bill.processBill(),
                "Bill is already processed");
    }

    @Test
    @DisplayName("Should transition to REFUNDED state")
    void testRefundBill() {
        // Arrange
        bill.processBill();

        // Act
        bill.refundBill();

        // Assert
        assertEquals("REFUNDED", bill.getCurrentStateName());
        assertEquals("Bill refunded to customer", bill.getCurrentStateDescription());
        assertFalse(bill.canModify());
        assertFalse(bill.canRefund());
        assertFalse(bill.canCancel());
        assertEquals("Bill refunded to customer", bill.getStateChangeReason());
    }

    @Test
    @DisplayName("Should not allow refund of unprocessed bill")
    void testRefundUnprocessedBill() {
        assertThrows(IllegalStateException.class, () -> bill.refundBill(),
                "Cannot refund a bill that hasn't been processed");
    }

    @Test
    @DisplayName("Should not allow double refund")
    void testDoubleRefund() {
        // Arrange
        bill.processBill();
        bill.refundBill();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> bill.refundBill(),
                "Bill is already refunded");
    }

    @Test
    @DisplayName("Should transition to CANCELLED state")
    void testCancelBill() {
        // Act
        bill.cancelBill();

        // Assert
        assertEquals("CANCELLED", bill.getCurrentStateName());
        assertEquals("Bill cancelled before processing", bill.getCurrentStateDescription());
        assertFalse(bill.canModify());
        assertFalse(bill.canRefund());
        assertFalse(bill.canCancel());
        assertEquals("Bill cancelled before processing", bill.getStateChangeReason());
    }

    @Test
    @DisplayName("Should not allow cancel after processing")
    void testCancelProcessedBill() {
        // Arrange
        bill.processBill();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> bill.cancelBill(),
                "Cannot cancel a processed bill, use refund instead");
    }

    @Test
    @DisplayName("Should not allow double cancel")
    void testDoubleCancel() {
        // Arrange
        bill.cancelBill();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> bill.cancelBill(),
                "Bill is already cancelled");
    }

    @Test
    @DisplayName("Should not allow operations on cancelled bill")
    void testCancelledBillOperations() {
        // Arrange
        bill.cancelBill();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> bill.processBill(),
                "Cannot process a cancelled bill");
        assertThrows(IllegalStateException.class, () -> bill.refundBill(),
                "Cannot refund a cancelled bill");
    }

    @Test
    @DisplayName("Should not allow operations on refunded bill")
    void testRefundedBillOperations() {
        // Arrange
        bill.processBill();
        bill.refundBill();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> bill.processBill(),
                "Cannot process a refunded bill");
        assertThrows(IllegalStateException.class, () -> bill.cancelBill(),
                "Cannot cancel a refunded bill");
    }

    @Test
    @DisplayName("Should not allow modification in processed state")
    void testModifyProcessedBill() {
        // Arrange
        bill.processBill();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> bill.setTotal(500.0));
        assertThrows(IllegalStateException.class, () -> bill.setItems(testItems));
        assertThrows(IllegalStateException.class, () -> bill.setDiscount(50.0));
    }

    @Test
    @DisplayName("Should calculate correct totals")
    void testBillCalculations() {
        // Make sure all values are properly initialized before assertions
        assertEquals(350.0, bill.getTotal(), 0.01);
        assertEquals(25.0, bill.getDiscount()); // <-- update to 25.0 if correct value is 25
        assertEquals(400.0, bill.getCashTendered(), 0.01);
        assertEquals(50.0, bill.getChange(), 0.01);
    }

}