package syos.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    @Test
    @DisplayName("Should create ItemDTO correctly")
    void testItemDTO() {
        // Arrange & Act
        ItemDTO item = new ItemDTO("ITEM001", "Test Item", 100.0, 50);

        // Assert
        assertEquals("ITEM001", item.getCode());
        assertEquals("Test Item", item.getName());
        assertEquals(100.0, item.getPrice());
        assertEquals(50, item.getQuantity());
    }

    @Test
    @DisplayName("Should create BatchDTO with all constructors")
    void testBatchDTO() {
        // Test main constructor
        BatchDTO batch1 = new BatchDTO("ITEM001", "Test Item", 100.0, 50,
                LocalDate.now(), LocalDate.now().plusDays(30));

        assertEquals("ITEM001", batch1.getItemCode());
        assertEquals("Test Item", batch1.getName());
        assertEquals(100.0, batch1.getSellingPrice());
        assertEquals(50, batch1.getQuantity());
        assertEquals(0, batch1.getUsedQuantity());

        // Test simple constructor
        BatchDTO batch2 = new BatchDTO("ITEM002", 25, LocalDate.now().plusDays(15));
        assertEquals("ITEM002", batch2.getItemCode());
        assertEquals(25, batch2.getQuantity());
        assertEquals(LocalDate.now(), batch2.getPurchaseDate());
    }

    @Test
    @DisplayName("Should create ReorderItemDTO and calculate shortfall")
    void testReorderItemDTO() {
        // Arrange & Act
        ReorderItemDTO item = new ReorderItemDTO("ITEM001", "Low Stock Item",
                15, 50, 100.0, "IN_STORE");

        // Assert
        assertEquals("ITEM001", item.getCode());
        assertEquals("Low Stock Item", item.getName());
        assertEquals(15, item.getCurrentStock());
        assertEquals(50, item.getReorderLevel());
        assertEquals(35, item.getShortfall()); // 50 - 15
        assertEquals("IN_STORE", item.getStoreType());
    }

    @Test
    @DisplayName("Should create StockBatchDTO with status calculation")
    void testStockBatchDTO() {
        // Test expired batch
        StockBatchDTO expiredBatch = new StockBatchDTO("ITEM001", "Expired Item",
                100, 20, LocalDate.now().minusDays(10), LocalDate.now().minusDays(1), 50.0);
        assertEquals("EXPIRED", expiredBatch.getStatus());

        // Test active batch
        StockBatchDTO activeBatch = new StockBatchDTO("ITEM002", "Active Item",
                100, 20, LocalDate.now().minusDays(10), LocalDate.now().plusDays(60), 75.0);
        assertEquals("ACTIVE", activeBatch.getStatus());
        assertEquals(80, activeBatch.getAvailableQuantity());

        // Test expiring soon batch
        StockBatchDTO expiringSoon = new StockBatchDTO("ITEM003", "Expiring Item",
                50, 10, LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), 60.0);
        assertEquals("EXPIRING_SOON", expiringSoon.getStatus());
    }

    @Test
    @DisplayName("Should create BillHistoryDTO with all fields")
    void testBillHistoryDTO() {
        // Arrange & Act
        BillHistoryDTO billHistory = new BillHistoryDTO(
                1, "BILL-001", LocalDateTime.now(), 1000.0, 100.0,
                "IN_STORE", "CASH", "John Doe", "Jane Smith", 5);

        // Assert
        assertEquals(1, billHistory.getBillId());
        assertEquals("BILL-001", billHistory.getSerialNumber());
        assertEquals(1000.0, billHistory.getTotalAmount());
        assertEquals(100.0, billHistory.getDiscount());
        assertEquals(1100.0, billHistory.getTotalBeforeDiscount());
        assertEquals("IN_STORE", billHistory.getStoreType());
        assertEquals("CASH", billHistory.getPaymentMethod());
        assertEquals("John Doe", billHistory.getCustomerInfo());
        assertEquals("Jane Smith", billHistory.getEmployeeName());
        assertEquals(5, billHistory.getItemCount());
    }

    @Test
    @DisplayName("Should merge ReportItemDTO correctly")
    void testReportItemDTOMerge() {
        // Arrange
        ReportItemDTO item1 = new ReportItemDTO("ITEM001", "Test Item", 10, 1000.0);
        ReportItemDTO item2 = new ReportItemDTO("ITEM001", "Test Item", 5, 500.0);

        // Act
        ReportItemDTO merged = ReportItemDTO.merge(item1, item2);

        // Assert
        assertEquals("ITEM001", merged.getCode());
        assertEquals("Test Item", merged.getName());
        assertEquals(15, merged.getQuantity());
        assertEquals(1500.0, merged.getRevenue());
    }
}