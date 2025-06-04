package syos.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import syos.dto.ItemDTO;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BillTest {

    private Bill bill;
    private List<CartItem> testItems;

    @BeforeEach
    void setUp() {
        ItemDTO item1 = new ItemDTO("A001", "Item 1", 100.0, 10);
        ItemDTO item2 = new ItemDTO("A002", "Item 2", 50.0, 20);

        testItems = Arrays.asList(
                new CartItem(item1, 2),
                new CartItem(item2, 3));

        bill = new Bill(1, LocalDateTime.now(), testItems, 350.0, 0.0, 400.0, 50.0);
    }

    @Test
    @DisplayName("Should create bill in CREATED state")
    void testInitialState() {
        assertEquals("CREATED", bill.getCurrentStateName());
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
        assertFalse(bill.canModify());
        assertTrue(bill.canRefund());
        assertFalse(bill.canCancel());
    }

    @Test
    @DisplayName("Should not allow processing already processed bill")
    void testDoubleProcess() {
        // Arrange
        bill.processBill();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> bill.processBill());
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
        assertFalse(bill.canModify());
        assertFalse(bill.canRefund());
        assertFalse(bill.canCancel());
    }

    @Test
    @DisplayName("Should not allow refund of unprocessed bill")
    void testRefundUnprocessedBill() {
        assertThrows(IllegalStateException.class, () -> bill.refundBill());
    }

    @Test
    @DisplayName("Should transition to CANCELLED state")
    void testCancelBill() {
        // Act
        bill.cancelBill();

        // Assert
        assertEquals("CANCELLED", bill.getCurrentStateName());
        assertFalse(bill.canModify());
        assertFalse(bill.canRefund());
        assertFalse(bill.canCancel());
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
        assertEquals(350.0, bill.getTotal());
        assertEquals(0.0, bill.getDiscount());
        assertEquals(400.0, bill.getCashTendered());
        assertEquals(50.0, bill.getChange());
    }
}