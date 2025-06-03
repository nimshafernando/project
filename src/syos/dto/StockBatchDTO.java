package syos.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object for stock batch information.
 * Follows SRP and immutability principles.
 */
public class StockBatchDTO {
    private final String itemCode;
    private final String itemName;
    private final int totalQuantity;
    private final int usedQuantity;
    private final int availableQuantity;
    private final LocalDate purchaseDate;
    private final LocalDate expiryDate;
    private final double sellingPrice;
    private final String status;

    public StockBatchDTO(String itemCode, String itemName, int totalQuantity, int usedQuantity,
            LocalDate purchaseDate, LocalDate expiryDate, double sellingPrice) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.totalQuantity = totalQuantity;
        this.usedQuantity = usedQuantity;
        this.availableQuantity = totalQuantity - usedQuantity;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.sellingPrice = sellingPrice;

        // Determine status based on expiry and availability
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            this.status = "EXPIRED";
        } else if (availableQuantity <= 0) {
            this.status = "DEPLETED";
        } else if (expiryDate.minusDays(7).isBefore(today)) {
            this.status = "EXPIRING_SOON";
        } else {
            this.status = "ACTIVE";
        }
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getUsedQuantity() {
        return usedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public String getStatus() {
        return status;
    }

    public long getDaysToExpiry() {
        return LocalDate.now().until(expiryDate).getDays();
    }
}
