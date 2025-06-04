package syos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import syos.data.BatchGateway;
import syos.data.ItemGateway;
import syos.data.OnlineItemGateway;
import syos.data.ReshelvedLogGateway;
import syos.dto.BatchDTO;
import syos.dto.ItemDTO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BatchServiceTest {

    @Mock
    private BatchGateway batchGateway;
    @Mock
    private ItemGateway itemGateway;
    @Mock
    private ReshelvedLogGateway reshelvedLogGateway;

    private BatchService batchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        batchService = new BatchService(batchGateway, itemGateway);
    }

    // Happy Path Tests
    @Test
    @DisplayName("Should add new batch for in-store when item doesn't exist")
    void testAddNewBatchInStoreNewItem() {
        BatchDTO batch = new BatchDTO("ITEM001", "Test Item", 100.0, 50,
                LocalDate.now(), LocalDate.now().plusDays(30));
        when(itemGateway.getItemByCode("ITEM001")).thenReturn(null);
        when(itemGateway.insertItem(any(ItemDTO.class))).thenReturn(true);
        when(batchGateway.insertBatch(batch)).thenReturn(true);

        boolean result = batchService.addNewBatch(batch, BatchService.StoreType.IN_STORE);

        assertTrue(result);
        verify(itemGateway).insertItem(any(ItemDTO.class));
        verify(batchGateway).insertBatch(batch);
    }

    @Test
    @DisplayName("Should add new batch for online when item doesn't exist")
    void testAddNewBatchOnlineNewItem() {
        BatchDTO batch = new BatchDTO("ONL001", "Online Item", 150.0, 30,
                LocalDate.now(), LocalDate.now().plusDays(45));
        OnlineItemGateway onlineGateway = mock(OnlineItemGateway.class);

        when(onlineGateway.getItemByCode("ONL001")).thenReturn(null);
        when(onlineGateway.insert(any(ItemDTO.class))).thenReturn(true);
        when(batchGateway.insertBatch(batch)).thenReturn(true);

        boolean result = batchService.addNewBatch(batch, BatchService.StoreType.ONLINE);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should update price when item exists with different price")
    void testAddNewBatchUpdatePrice() {
        BatchDTO batch = new BatchDTO("ITEM001", "Test Item", 150.0, 50,
                LocalDate.now(), LocalDate.now().plusDays(30));
        ItemDTO existingItem = new ItemDTO("ITEM001", "Test Item", 100.0, 20);

        when(itemGateway.getItemByCode("ITEM001")).thenReturn(existingItem);
        when(itemGateway.updateItemPrice("ITEM001", 150.0)).thenReturn(true);
        when(batchGateway.insertBatch(batch)).thenReturn(true);

        boolean result = batchService.addNewBatch(batch, BatchService.StoreType.IN_STORE);

        assertTrue(result);
        verify(itemGateway).updateItemPrice("ITEM001", 150.0);
    }

    @Test
    @DisplayName("Should not update price when same as existing")
    void testAddNewBatchSamePrice() {
        BatchDTO batch = new BatchDTO("ITEM001", "Test Item", 100.0, 50,
                LocalDate.now(), LocalDate.now().plusDays(30));
        ItemDTO existingItem = new ItemDTO("ITEM001", "Test Item", 100.0, 20);

        when(itemGateway.getItemByCode("ITEM001")).thenReturn(existingItem);
        when(batchGateway.insertBatch(batch)).thenReturn(true);

        boolean result = batchService.addNewBatch(batch, BatchService.StoreType.IN_STORE);

        assertTrue(result);
        verify(itemGateway, never()).updateItemPrice(anyString(), anyDouble());
    }

    // Edge Cases
    @Test
    @DisplayName("Should throw exception for zero quantity")
    void testAddNewBatchZeroQuantity() {
        BatchDTO batch = new BatchDTO("ITEM001", "Test Item", 100.0, 0,
                LocalDate.now(), LocalDate.now().plusDays(30));

        assertThrows(IllegalArgumentException.class,
                () -> batchService.addNewBatch(batch, BatchService.StoreType.IN_STORE),
                "Quantity must be positive.");
    }

    @Test
    @DisplayName("Should throw exception for negative quantity")
    void testAddNewBatchNegativeQuantity() {
        BatchDTO batch = new BatchDTO("ITEM001", "Test Item", 100.0, -5,
                LocalDate.now(), LocalDate.now().plusDays(30));

        assertThrows(IllegalArgumentException.class,
                () -> batchService.addNewBatch(batch, BatchService.StoreType.IN_STORE));
    }

    @Test
    @DisplayName("Should throw exception when expiry is before purchase date")
    void testAddNewBatchInvalidDates() {
        LocalDate purchaseDate = LocalDate.now();
        LocalDate expiryDate = purchaseDate.minusDays(1);
        BatchDTO batch = new BatchDTO("ITEM001", "Test Item", 100.0, 50,
                purchaseDate, expiryDate);

        assertThrows(IllegalArgumentException.class,
                () -> batchService.addNewBatch(batch, BatchService.StoreType.IN_STORE));
    }

    @Test
    @DisplayName("Should throw exception when batch is already expired")
    void testAddNewBatchAlreadyExpired() {
        BatchDTO batch = new BatchDTO("ITEM001", "Test Item", 100.0, 50,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));

        assertThrows(IllegalArgumentException.class,
                () -> batchService.addNewBatch(batch, BatchService.StoreType.IN_STORE),
                "Cannot add batch that is already expired.");
    }

    @Test
    @DisplayName("Should throw exception when item creation fails")
    void testAddNewBatchItemCreationFails() {
        BatchDTO batch = new BatchDTO("ITEM001", "Test Item", 100.0, 50,
                LocalDate.now(), LocalDate.now().plusDays(30));
        when(itemGateway.getItemByCode("ITEM001")).thenReturn(null);
        when(itemGateway.insertItem(any(ItemDTO.class))).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> batchService.addNewBatch(batch, BatchService.StoreType.IN_STORE));
    }

    // Restock Tests
    @Test
    @DisplayName("Should restock item using FIFO by expiry date")
    void testRestockItemFIFO() {
        String itemCode = "ITEM001";
        BatchDTO batch1 = new BatchDTO("ITEM001", "Test Item", 100.0, 50,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(10));
        batch1.setUsedQuantity(20);

        BatchDTO batch2 = new BatchDTO("ITEM001", "Test Item", 100.0, 50,
                LocalDate.now().minusDays(3), LocalDate.now().plusDays(20));
        batch2.setUsedQuantity(0);

        List<BatchDTO> batches = Arrays.asList(batch2, batch1);

        when(batchGateway.getAvailableBatchesForItem(itemCode)).thenReturn(batches);
        when(batchGateway.reduceBatchQuantity(eq(itemCode), any(LocalDate.class), anyInt()))
                .thenReturn(true);

        boolean result = batchService.restockItem(itemCode, 40);

        assertTrue(result);
        verify(batchGateway).reduceBatchQuantity(itemCode, batch1.getPurchaseDate(), 30);
        verify(batchGateway).reduceBatchQuantity(itemCode, batch2.getPurchaseDate(), 10);
    }

    @Test
    @DisplayName("Should handle partial restock when insufficient batch quantity")
    void testRestockItemPartial() {
        String itemCode = "ITEM001";
        BatchDTO batch = new BatchDTO("ITEM001", "Test Item", 100.0, 30,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(10));
        batch.setUsedQuantity(10);

        when(batchGateway.getAvailableBatchesForItem(itemCode))
                .thenReturn(Collections.singletonList(batch));
        when(batchGateway.reduceBatchQuantity(eq(itemCode), any(LocalDate.class), anyInt()))
                .thenReturn(true);

        boolean result = batchService.restockItem(itemCode, 50);

        assertFalse(result); // Cannot fulfill full quantity
        verify(batchGateway).reduceBatchQuantity(itemCode, batch.getPurchaseDate(), 20);
    }

    @Test
    @DisplayName("Should return false when no batches available")
    void testRestockItemNoBatches() {
        when(batchGateway.getAvailableBatchesForItem("ITEM001")).thenReturn(Collections.emptyList());

        boolean result = batchService.restockItem("ITEM001", 10);

        assertFalse(result);
        verify(batchGateway, never()).reduceBatchQuantity(anyString(), any(LocalDate.class), anyInt());
    }

    @Test
    @DisplayName("Should skip fully used batches")
    void testRestockItemSkipFullyUsedBatches() {
        BatchDTO fullyUsedBatch = new BatchDTO("ITEM001", "Test Item", 100.0, 50,
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(5));
        fullyUsedBatch.setUsedQuantity(50); // Fully used

        BatchDTO availableBatch = new BatchDTO("ITEM001", "Test Item", 100.0, 30,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(15));
        availableBatch.setUsedQuantity(10);

        when(batchGateway.getAvailableBatchesForItem("ITEM001"))
                .thenReturn(Arrays.asList(fullyUsedBatch, availableBatch));
        when(batchGateway.reduceBatchQuantity(eq("ITEM001"), any(LocalDate.class), anyInt()))
                .thenReturn(true);

        boolean result = batchService.restockItem("ITEM001", 15);

        assertTrue(result);
        verify(batchGateway, times(1)).reduceBatchQuantity("ITEM001", availableBatch.getPurchaseDate(), 15);
    }

    // Expired Batch Tests
    @Test
    @DisplayName("Should get expired batches")
    void testGetExpiredBatches() {
        LocalDate today = LocalDate.now();
        List<BatchDTO> expiredBatches = Arrays.asList(
                new BatchDTO("ITEM001", "Expired Item 1", 100.0, 10,
                        today.minusDays(30), today.minusDays(1)),
                new BatchDTO("ITEM002", "Expired Item 2", 50.0, 5,
                        today.minusDays(60), today.minusDays(10)));
        when(batchGateway.getExpiredBatchesAll(today)).thenReturn(expiredBatches);

        List<BatchDTO> result = batchService.getExpiredBatches();

        assertEquals(2, result.size());
        assertEquals("ITEM001", result.get(0).getItemCode());
        assertEquals("ITEM002", result.get(1).getItemCode());
    }

    @Test
    @DisplayName("Should get expired batches with names")
    void testGetExpiredBatchesWithNames() {
        LocalDate today = LocalDate.now();
        List<BatchDTO> expiredBatches = Arrays.asList(
                new BatchDTO("ITEM001", "Expired Milk", 100.0, 10,
                        today.minusDays(30), today.minusDays(1)));
        when(batchGateway.getExpiredBatchesWithItemNames(today)).thenReturn(expiredBatches);

        List<BatchDTO> result = batchService.getExpiredBatchesWithNames();

        assertEquals(1, result.size());
        assertEquals("Expired Milk", result.get(0).getName());
    }

    @Test
    @DisplayName("Should archive and remove batch")
    void testArchiveAndRemoveBatch() {
        BatchDTO batch = new BatchDTO("ITEM001", "Test Item", 100.0, 10,
                LocalDate.now().minusDays(30), LocalDate.now().minusDays(1));
        when(batchGateway.deleteExpiredBatch(batch)).thenReturn(true);

        boolean result = batchService.archiveAndRemoveBatch(batch);

        assertTrue(result);
        verify(batchGateway).deleteExpiredBatch(batch);
    }

    @Test
    @DisplayName("Should delete specific expired batch")
    void testDeleteExpiredBatch() {
        BatchDTO batch = new BatchDTO("ITEM001", "Test Item", 100.0, 10,
                LocalDate.now().minusDays(30), LocalDate.now().minusDays(1));
        when(batchGateway.deleteExpiredBatch(batch)).thenReturn(true);

        boolean result = batchService.deleteExpiredBatch(batch);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should auto cleanup expired batches")
    void testAutoCleanupExpiredBatches() {
        List<BatchDTO> expiredBatches = Arrays.asList(
                new BatchDTO("ITEM001", "Expired Item", 100.0, 10,
                        LocalDate.now().minusDays(30), LocalDate.now().minusDays(1)));
        when(batchGateway.getExpiredBatchesAll(LocalDate.now())).thenReturn(expiredBatches);
        when(batchGateway.deleteExpiredBatch(any(BatchDTO.class))).thenReturn(true);

        batchService.autoCleanupExpiredBatches();

        verify(batchGateway).deleteExpiredBatch(any(BatchDTO.class));
    }

    @Test
    @DisplayName("Should handle empty expired batches in cleanup")
    void testAutoCleanupNoExpiredBatches() {
        when(batchGateway.getExpiredBatchesAll(LocalDate.now())).thenReturn(Collections.emptyList());

        batchService.autoCleanupExpiredBatches();

        verify(batchGateway, never()).deleteExpiredBatch(any(BatchDTO.class));
    }

    @Test
    @DisplayName("Should get archived expired batches")
    void testGetArchivedExpiredBatches() {
        List<BatchDTO> archivedBatches = Arrays.asList(
                new BatchDTO("ITEM001", "Archived Item", 100.0, 10,
                        LocalDate.now().minusDays(60), LocalDate.now().minusDays(30)));
        when(batchGateway.getArchivedExpiredBatches()).thenReturn(archivedBatches);

        List<BatchDTO> result = batchService.getArchivedExpiredBatches();

        assertEquals(1, result.size());
        assertEquals("ITEM001", result.get(0).getItemCode());
    }

    @Test
    @DisplayName("Should clear archived expired batches")
    void testClearArchivedExpiredBatches() {
        when(batchGateway.clearArchivedExpiredBatches()).thenReturn(true);

        boolean result = batchService.clearArchivedExpiredBatches();

        assertTrue(result);
    }

    @Test
    @DisplayName("Should add batch stock")
    void testAddBatchStock() {
        BatchDTO batch = new BatchDTO("ITEM001", "Test Item", 100.0, 50,
                LocalDate.now(), LocalDate.now().plusDays(30));
        when(batchGateway.insertBatch(batch)).thenReturn(true);

        boolean result = batchService.addBatchStock(batch);

        assertTrue(result);
        verify(batchGateway).insertBatch(batch);
    }
}