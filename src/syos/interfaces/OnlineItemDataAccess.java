package syos.interfaces;

import syos.dto.ItemDTO;
import syos.model.CartItem;
import java.util.List;

/**
 * Online Item-specific data access interface following Interface Segregation
 * Principle
 * Extends the base DataAccessInterface and adds online-specific operations
 */
public interface OnlineItemDataAccess extends DataAccessInterface<ItemDTO, String> {

    /**
     * Get all items from online inventory
     * 
     * @return List of all online items
     */
    List<ItemDTO> getAllItems();

    /**
     * Get item by code from online inventory
     * 
     * @param code The item code
     * @return ItemDTO if found, null otherwise
     */
    ItemDTO getItemByCode(String code);

    /**
     * Reduce stock for a specific item
     * 
     * @param code     The item code
     * @param quantity Quantity to reduce
     */
    void reduceStock(String code, int quantity);

    /**
     * Increase stock for a specific item
     * 
     * @param itemCode The item code
     * @param amount   Amount to increase
     */
    void increaseStock(String itemCode, int amount);

    /**
     * Update item price
     * 
     * @param itemCode The item code
     * @param newPrice The new price
     * @return true if successful, false otherwise
     */
    boolean updateItemPrice(String itemCode, double newPrice);

    /**
     * Reduce stock for multiple items in a single transaction (batch operation)
     * 
     * @param cartItems List of cart items to reduce stock for
     * @return true if all operations successful, false otherwise
     */
    boolean reduceStockBatch(List<CartItem> cartItems);
}
