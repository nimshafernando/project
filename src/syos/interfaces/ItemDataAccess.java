package syos.interfaces;

import syos.dto.ItemDTO;
import java.util.List;

/**
 * Item-specific data access interface following Interface Segregation Principle
 */
public interface ItemDataAccess extends DataAccessInterface<ItemDTO, String> {

    /**
     * Reduce stock for an item
     * 
     * @param itemCode The item code
     * @param quantity Quantity to reduce
     */
    void reduceStock(String itemCode, int quantity);

    /**
     * Increase stock for an item
     * 
     * @param itemCode The item code
     * @param quantity Quantity to increase
     */
    void increaseStock(String itemCode, int quantity);

    /**
     * Get items below reorder level
     * 
     * @return List of items below reorder level
     */
    List<ItemDTO> getItemsBelowReorderLevel();

    /**
     * Update item price
     * 
     * @param itemCode The item code
     * @param newPrice The new price
     * @return true if successful, false otherwise
     */
    boolean updateItemPrice(String itemCode, double newPrice);
}
