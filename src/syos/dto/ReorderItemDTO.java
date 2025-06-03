package syos.dto;

/**
 * Data Transfer Object for items below reorder level.
 * Follows SRP and immutability principles.
 */
public class ReorderItemDTO {
    private final String code;
    private final String name;
    private final int currentStock;
    private final int reorderLevel;
    private final double price;
    private final String storeType;

    public ReorderItemDTO(String code, String name, int currentStock, int reorderLevel, double price,
            String storeType) {
        this.code = code;
        this.name = name;
        this.currentStock = currentStock;
        this.reorderLevel = reorderLevel;
        this.price = price;
        this.storeType = storeType;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public double getPrice() {
        return price;
    }

    public String getStoreType() {
        return storeType;
    }

    /**
     * Calculates shortfall based on threshold (reorder level)
     * This method remains for business logic calculations
     */
    public int getShortfall() {
        return Math.max(0, reorderLevel - currentStock);
    }
}
