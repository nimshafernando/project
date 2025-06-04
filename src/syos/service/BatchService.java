package syos.service;

import syos.data.BatchGateway;
import syos.data.ItemGateway;
import syos.data.OnlineItemGateway;
import syos.data.ReshelvedLogGateway;
import syos.dto.BatchDTO;
import syos.dto.ItemDTO;

import java.time.LocalDate;
import java.util.List;

public class BatchService {
    private final BatchGateway batchGateway;
    private final ItemGateway itemGateway;
    private final ReshelvedLogGateway reshelvedLogGateway;

    public BatchService(BatchGateway batchGateway, ItemGateway itemGateway) {
        this.batchGateway = batchGateway;
        this.itemGateway = itemGateway;
        this.reshelvedLogGateway = new ReshelvedLogGateway();
    }

    public enum StoreType {
        IN_STORE, ONLINE
    }

    public boolean addNewBatch(BatchDTO batch, StoreType storeType) {
        if (batch.getQuantity() <= 0)
            throw new IllegalArgumentException("Quantity must be positive.");
        if (batch.getExpiryDate().isBefore(batch.getPurchaseDate()))
            throw new IllegalArgumentException("Expiry cannot be before purchase date.");
        if (batch.getExpiryDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Cannot add batch that is already expired.");

        ItemDTO existingItem = null;

        if (storeType == StoreType.IN_STORE) {
            // Check if item exists in items table
            existingItem = itemGateway.getItemByCode(batch.getItemCode());

            if (existingItem == null) {
                // Create new item in items table
                ItemDTO newItem = new ItemDTO(
                        batch.getItemCode(),
                        batch.getName(),
                        batch.getSellingPrice(),
                        0 // Initial stock is 0, will be added when restocking from batch
                );
                if (!itemGateway.insertItem(newItem)) {
                    throw new RuntimeException("Failed to create new item in in-store inventory.");
                }
            } else {
                // Update existing item's price if different
                if (existingItem.getPrice() != batch.getSellingPrice()) {
                    itemGateway.updateItemPrice(batch.getItemCode(), batch.getSellingPrice());
                }
            }
        } else { // ONLINE
            // Check if item exists in online_inventory table
            OnlineItemGateway onlineItemGateway = new OnlineItemGateway();
            existingItem = onlineItemGateway.getItemByCode(batch.getItemCode());

            if (existingItem == null) {
                // Create new item in online_inventory table
                ItemDTO newItem = new ItemDTO(
                        batch.getItemCode(),
                        batch.getName(),
                        batch.getSellingPrice(),
                        0 // Initial stock is 0, will be added when restocking from batch
                );
                if (!onlineItemGateway.insert(newItem)) {
                    throw new RuntimeException("Failed to create new item in online inventory.");
                }
            } else {
                // Update existing item's price if different
                if (existingItem.getPrice() != batch.getSellingPrice()) {
                    onlineItemGateway.updateItemPrice(batch.getItemCode(), batch.getSellingPrice());
                }
            }
        }

        return batchGateway.insertBatch(batch);
    }

    /**
     * Add batch stock - wrapper method for batch insertion
     *
     * @param batch BatchDTO to add to the system
     * @return true if batch was successfully added, false otherwise
     */
    public boolean addBatchStock(BatchDTO batch) {
        return batchGateway.insertBatch(batch);
    }

    /**
     * Get expired batches - delegates to existing getAllExpiredBatches method
     *
     * @return List of expired batches
     */
    public List<BatchDTO> getExpiredBatches() {
        return batchGateway.getExpiredBatchesAll(LocalDate.now());
    }

    public List<BatchDTO> getAllExpiredBatches() {
        return batchGateway.getExpiredBatchesAll(LocalDate.now());
    }

    public boolean archiveAndRemoveBatch(BatchDTO batch) {
        return batchGateway.deleteExpiredBatch(batch);
    }

    public boolean restockItem(String itemCode, int quantityToRestock) {
        List<BatchDTO> batches = batchGateway.getAvailableBatchesForItem(itemCode);

        if (batches.isEmpty())
            return false;

        // THIS IS WHERE EXPIRY-FIRST SORTING HAPPENS
        batches.sort((b1, b2) -> {
            int expiryCompare = b1.getExpiryDate().compareTo(b2.getExpiryDate());
            return expiryCompare != 0 ? expiryCompare : b1.getPurchaseDate().compareTo(b2.getPurchaseDate());
        });
        // END OF EXPIRY-FIRST LOGIC

        int remainingQty = quantityToRestock;
        for (BatchDTO batch : batches) {
            if (remainingQty <= 0)
                break;

            int available = batch.getQuantity() - batch.getUsedQuantity();
            int toUse = Math.min(available, remainingQty);

            if (toUse > 0) {
                batchGateway.reduceBatchQuantity(itemCode, batch.getPurchaseDate(), toUse);
                itemGateway.increaseStock(itemCode, toUse);
                reshelvedLogGateway.logReshelving(itemCode, toUse, ReshelvedLogGateway.StoreType.INSTORE);
                remainingQty -= toUse;
            }
        }

        return remainingQty == 0;
    }

    public List<BatchDTO> getExpiredBatchesWithNames() {
        return batchGateway.getExpiredBatchesWithItemNames(LocalDate.now());
    }

    public boolean deleteExpiredBatch(BatchDTO batch) {
        return batchGateway.deleteExpiredBatch(batch);
    }

    public void autoCleanupExpiredBatches() {
        List<BatchDTO> expiredBatches = batchGateway.getExpiredBatchesAll(LocalDate.now());

        if (!expiredBatches.isEmpty()) {
            System.out.println("Auto-cleanup: Found " + expiredBatches.size() + " expired batch(es) to archive...");

            int archivedCount = 0;
            for (BatchDTO batch : expiredBatches) {
                if (batchGateway.deleteExpiredBatch(batch)) {
                    archivedCount++;
                    System.out.printf("Archived: Item %s, Qty: %d, Expired: %s\n",
                            batch.getItemCode(), batch.getQuantity(), batch.getExpiryDate());
                }
            }

            if (archivedCount > 0) {
                System.out.println("Auto-cleanup completed: " + archivedCount + " batch(es) archived.");
            } else {
                System.out.println("No expired batches found to archive.");
            }
        } else {
            System.out.println("No expired batches found.");
        }
    }

    public List<BatchDTO> getArchivedExpiredBatches() {
        return batchGateway.getArchivedExpiredBatches();
    }

    public boolean clearArchivedExpiredBatches() {
        return batchGateway.clearArchivedExpiredBatches();
    }
}
