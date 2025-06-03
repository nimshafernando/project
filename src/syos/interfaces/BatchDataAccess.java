package syos.interfaces;

import syos.dto.BatchDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Batch-specific data access interface following Interface Segregation
 * Principle
 */
public interface BatchDataAccess extends DataAccessInterface<BatchDTO, Integer> {

    /**
     * Get available batches for an item
     * 
     * @param itemCode The item code
     * @return List of available batches
     */
    List<BatchDTO> getAvailableBatchesForItem(String itemCode);

    /**
     * Reduce batch quantity
     * 
     * @param itemCode     The item code
     * @param purchaseDate The purchase date
     * @param usedQty      Quantity used
     * @return true if successful, false otherwise
     */
    boolean reduceBatchQuantity(String itemCode, LocalDate purchaseDate, int usedQty);

    /**
     * Get expired batches
     * 
     * @param today Current date
     * @return List of expired batches
     */
    List<BatchDTO> getExpiredBatches(LocalDate today);

    /**
     * Get stock summary per item
     * 
     * @return Map of stock summary data
     */
    Map<String, Object[]> getStockSummaryPerItemWithNames();

    /**
     * Archive a batch
     * 
     * @param batch    The batch to archive
     * @param itemName The item name
     */
    void archiveBatch(BatchDTO batch, String itemName);

    /**
     * Remove expired batches
     * 
     * @param today Current date
     * @return Number of batches removed
     */
    int removeExpiredBatches(LocalDate today);
}
