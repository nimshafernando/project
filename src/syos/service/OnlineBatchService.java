package syos.service;

import syos.data.BatchGateway;
import syos.data.OnlineItemGateway;
import syos.data.ReshelvedLogGateway;
import syos.dto.BatchDTO;

import java.util.List;

public class OnlineBatchService {
    private final BatchGateway batchGateway;
    private final OnlineItemGateway onlineItemGateway;
    private final ReshelvedLogGateway reshelvedLogGateway;

    // Improved constructor with all dependencies injected
    public OnlineBatchService(BatchGateway batchGateway,
            OnlineItemGateway onlineItemGateway,
            ReshelvedLogGateway reshelvedLogGateway) {
        this.batchGateway = batchGateway;
        this.onlineItemGateway = onlineItemGateway;
        this.reshelvedLogGateway = reshelvedLogGateway;
    }

    // Convenience constructor for backward compatibility
    public OnlineBatchService(BatchGateway batchGateway, OnlineItemGateway onlineItemGateway) {
        this(batchGateway, onlineItemGateway, new ReshelvedLogGateway());
    }

    public boolean restockOnlineItem(String itemCode, int quantityToRestock) {
        List<BatchDTO> batches = batchGateway.getAvailableBatchesForItem(itemCode);

        if (batches.isEmpty())
            return false;

        // Sort batches by purchase date first (oldest first), then by expiry date
        batches.sort((b1, b2) -> {
            int purchaseCompare = b1.getPurchaseDate().compareTo(b2.getPurchaseDate());
            return purchaseCompare != 0 ? purchaseCompare : b1.getExpiryDate().compareTo(b2.getExpiryDate());
        });

        int remainingQty = quantityToRestock;
        for (BatchDTO batch : batches) {
            if (remainingQty <= 0)
                break;

            int available = batch.getQuantity() - batch.getUsedQuantity();
            int toUse = Math.min(available, remainingQty);

            if (toUse > 0) {
                batchGateway.reduceBatchQuantity(itemCode, batch.getPurchaseDate(), toUse);
                onlineItemGateway.increaseStock(itemCode, toUse);

                // Log the reshelving activity for ONLINE
                reshelvedLogGateway.logReshelving(itemCode, toUse, ReshelvedLogGateway.StoreType.ONLINE);

                remainingQty -= toUse;
            }
        }

        return remainingQty == 0;
    }
}