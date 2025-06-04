package syos.interfaces;

import syos.dto.ItemDTO;
import java.util.List;

/**
 * Online Inventory data access interface following Interface Segregation
 * Principle
 */
public interface OnlineInventoryDataAccess extends DataAccessInterface<ItemDTO, String> {

    /**
     * Get all online items
     * 
     * @return List of all online items
     */
    List<ItemDTO> getAllOnlineItems();

    /**
     * Get online item by code
     * 
     * @param code The item code
     * @return ItemDTO if found, null otherwise
     */
    ItemDTO getOnlineItemByCode(String code);

    /**
     * Reduce online stock for an item
     * 
     * @param code     The item code
     * @param quantity Quantity to reduce
     * @return true if successful, false otherwise
     */
    boolean reduceOnlineStock(String code, int quantity);
}
