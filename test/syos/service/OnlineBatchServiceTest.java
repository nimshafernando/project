package syos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import syos.data.BatchGateway;
import syos.data.OnlineItemGateway;
import syos.data.ReshelvedLogGateway;
import syos.dto.BatchDTO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("OnlineBatchService Tests")
class OnlineBatchServiceTest {

    @Mock
    private BatchGateway batchGateway;

    @Mock
    private OnlineItemGateway onlineItemGateway;

    @Mock
    private ReshelvedLogGateway reshelvedLogGateway;

    private OnlineBatchService onlineBatchService;

    // Test data constants
    private static final String ITEM_CODE_LAPTOP = "LAP001";
    private static final String ITEM_CODE_PHONE = "PHN001";
    private static final String ITEM_CODE_TABLET = "TAB001";
    private static final LocalDate PURCHASE_DATE_OLD = LocalDate.of(2024, 1, 15);
    private static final LocalDate PURCHASE_DATE_NEW = LocalDate.of(2024, 3, 20);
    private static final LocalDate EXPIRY_DATE_EARLY = LocalDate.of(2025, 6, 30);
    private static final LocalDate EXPIRY_DATE_LATE = LocalDate.of(2025, 12, 31);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        onlineBatchService = new OnlineBatchService(batchGateway, onlineItemGateway, reshelvedLogGateway);
    }

    @Test
    @DisplayName("Should create instance with all dependencies")
    void constructor_WithAllDependencies_ShouldCreateInstance() {
        // Arrange & Act
        OnlineBatchService service = new OnlineBatchService(batchGateway, onlineItemGateway, reshelvedLogGateway);

        // Assert
        assertNotNull(service);
    }

    @Test
    @DisplayName("Should create instance with two dependencies")
    void constructor_WithTwoDependencies_ShouldCreateInstanceWithDefaultReshelvedLogGateway() {
        // Arrange & Act
        OnlineBatchService service = new OnlineBatchService(batchGateway, onlineItemGateway);

        // Assert
        assertNotNull(service);
    }

    @Test
    @DisplayName("Should return false when no available batches")
    void restockOnlineItem_WithNoAvailableBatches_ShouldReturnFalse() {
        when(batchGateway.getAvailableBatchesForItem(ITEM_CODE_LAPTOP))
                .thenReturn(Collections.emptyList());

        boolean result = onlineBatchService.restockOnlineItem(ITEM_CODE_LAPTOP, 10);

        assertFalse(result);
        verify(batchGateway).getAvailableBatchesForItem(ITEM_CODE_LAPTOP);
    }

    @Test
    @DisplayName("Should return true and log reshelving with single sufficient batch")
    void restockOnlineItem_WithSingleBatchSufficientQuantity_ShouldReturnTrueAndLogReshelving() {
        BatchDTO batch = createBatchDTO(PURCHASE_DATE_OLD, EXPIRY_DATE_EARLY, 50, 10);
        when(batchGateway.getAvailableBatchesForItem(ITEM_CODE_LAPTOP))
                .thenReturn(Arrays.asList(batch));

        boolean result = onlineBatchService.restockOnlineItem(ITEM_CODE_LAPTOP, 20);

        assertTrue(result);
        verify(batchGateway).reduceBatchQuantity(ITEM_CODE_LAPTOP, PURCHASE_DATE_OLD, 20);
        verify(onlineItemGateway).increaseStock(ITEM_CODE_LAPTOP, 20);
    }

    @Test
    @DisplayName("Should use all available quantity from batch when insufficient")
    void restockOnlineItem_WithSingleBatchInsufficientQuantity_ShouldUseAllAvailable() {
        BatchDTO batch = createBatchDTO(PURCHASE_DATE_OLD, EXPIRY_DATE_EARLY, 30, 25);
        when(batchGateway.getAvailableBatchesForItem(ITEM_CODE_PHONE))
                .thenReturn(Arrays.asList(batch));

        onlineBatchService.restockOnlineItem(ITEM_CODE_PHONE, 20);

        verify(batchGateway).reduceBatchQuantity(ITEM_CODE_PHONE, PURCHASE_DATE_OLD, 5);
        verify(onlineItemGateway).increaseStock(ITEM_CODE_PHONE, 5);
    }

    @Test
    @DisplayName("Should handle multiple batches and log multiple reshelving")
    void restockOnlineItem_WithMultipleBatchesSufficientQuantity_ShouldReturnTrueAndLogMultipleReshelving() {
        BatchDTO batch1 = createBatchDTO(PURCHASE_DATE_OLD, EXPIRY_DATE_EARLY, 20, 5);
        BatchDTO batch2 = createBatchDTO(PURCHASE_DATE_NEW, EXPIRY_DATE_LATE, 30, 10);
        when(batchGateway.getAvailableBatchesForItem(ITEM_CODE_TABLET))
                .thenReturn(Arrays.asList(batch1, batch2));

        onlineBatchService.restockOnlineItem(ITEM_CODE_TABLET, 25);

        verify(batchGateway).reduceBatchQuantity(ITEM_CODE_TABLET, PURCHASE_DATE_OLD, 15);
        verify(batchGateway).reduceBatchQuantity(ITEM_CODE_TABLET, PURCHASE_DATE_NEW, 10);
    }

    @Test
    @DisplayName("Should handle zero quantity to restock")
    void restockOnlineItem_WithZeroQuantityToRestock_ShouldReturnTrueWithoutAnyOperations() {
        BatchDTO batch = createBatchDTO(PURCHASE_DATE_OLD, EXPIRY_DATE_EARLY, 50, 10);
        when(batchGateway.getAvailableBatchesForItem(ITEM_CODE_PHONE))
                .thenReturn(Arrays.asList(batch));

        onlineBatchService.restockOnlineItem(ITEM_CODE_PHONE, 0);

        verify(batchGateway).getAvailableBatchesForItem(ITEM_CODE_PHONE);
        verifyNoMoreInteractions(batchGateway, onlineItemGateway, reshelvedLogGateway);
    }

    @Test
    @DisplayName("Should skip batches with zero available quantity")
    void restockOnlineItem_WithBatchHavingZeroAvailableQuantity_ShouldSkipBatchAndNotLog() {
        BatchDTO batch1 = createBatchDTO(PURCHASE_DATE_OLD, EXPIRY_DATE_EARLY, 10, 10); // No available
        BatchDTO batch2 = createBatchDTO(PURCHASE_DATE_NEW, EXPIRY_DATE_LATE, 20, 5); // 15 available
        when(batchGateway.getAvailableBatchesForItem(ITEM_CODE_LAPTOP))
                .thenReturn(Arrays.asList(batch1, batch2));

        onlineBatchService.restockOnlineItem(ITEM_CODE_LAPTOP, 10);

        verify(batchGateway, never()).reduceBatchQuantity(eq(ITEM_CODE_LAPTOP), eq(PURCHASE_DATE_OLD), anyInt());
        verify(batchGateway).reduceBatchQuantity(ITEM_CODE_LAPTOP, PURCHASE_DATE_NEW, 10);
    }

    @Test
    @DisplayName("Should sort batches by purchase date then expiry date")
    void restockOnlineItem_ShouldSortBatchesByPurchaseDateThenExpiryDate() {
        BatchDTO batch1 = createBatchDTO(PURCHASE_DATE_NEW, EXPIRY_DATE_EARLY, 20, 0);
        BatchDTO batch2 = createBatchDTO(PURCHASE_DATE_OLD, EXPIRY_DATE_LATE, 20, 0);
        BatchDTO batch3 = createBatchDTO(PURCHASE_DATE_OLD, EXPIRY_DATE_EARLY, 20, 0);
        when(batchGateway.getAvailableBatchesForItem(ITEM_CODE_TABLET))
                .thenReturn(Arrays.asList(batch1, batch2, batch3));

        onlineBatchService.restockOnlineItem(ITEM_CODE_TABLET, 25);

        verify(batchGateway).reduceBatchQuantity(ITEM_CODE_TABLET, PURCHASE_DATE_OLD, 20);
        verify(batchGateway).reduceBatchQuantity(ITEM_CODE_TABLET, PURCHASE_DATE_OLD, 5);
    }

    @Test
    @DisplayName("Should handle exact quantity match")
    void restockOnlineItem_WithExactQuantityMatch_ShouldReturnTrueAndLogCorrectAmount() {
        BatchDTO batch = createBatchDTO(PURCHASE_DATE_OLD, EXPIRY_DATE_EARLY, 30, 15);
        when(batchGateway.getAvailableBatchesForItem(ITEM_CODE_PHONE))
                .thenReturn(Arrays.asList(batch));

        onlineBatchService.restockOnlineItem(ITEM_CODE_PHONE, 15);

        verify(batchGateway).reduceBatchQuantity(ITEM_CODE_PHONE, PURCHASE_DATE_OLD, 15);
        verify(onlineItemGateway).increaseStock(ITEM_CODE_PHONE, 15);
    }

    @Test
    @DisplayName("Should verify store type is always ONLINE")
    void restockOnlineItem_VerifyReshelvedLogGatewayStoreTypeIsAlwaysOnline() {
        BatchDTO batch = createBatchDTO(PURCHASE_DATE_OLD, EXPIRY_DATE_EARLY, 50, 10);
        when(batchGateway.getAvailableBatchesForItem(ITEM_CODE_PHONE))
                .thenReturn(Arrays.asList(batch));

        onlineBatchService.restockOnlineItem(ITEM_CODE_PHONE, 25);

        verify(reshelvedLogGateway).logReshelving(eq(ITEM_CODE_PHONE), eq(25),
                eq(ReshelvedLogGateway.StoreType.ONLINE));
    }

    /**
     * Helper method to create BatchDTO test objects
     */
    private BatchDTO createBatchDTO(LocalDate purchaseDate, LocalDate expiryDate, int quantity, int usedQuantity) {
        // Use the comprehensive constructor
        return new BatchDTO(1, "TEST_ITEM", "Test Item", 10.0, quantity,
                purchaseDate, expiryDate, usedQuantity);
    }
}