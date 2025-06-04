package syos.interfaces;

import syos.dto.ReshelvedLogDTO;
import syos.data.ReshelvedLogGateway.StoreType;
import java.util.List;

/**
 * Interface for reshelved log data access operations.
 * Implements Interface Segregation Principle by providing specific methods
 * for reshelved log operations.
 */
public interface ReshelvedLogDataAccess extends DataAccessInterface<ReshelvedLogDTO, Integer> {

    /**
     * Log a reshelving activity.
     * 
     * @param itemCode  The item code being reshelved
     * @param quantity  The quantity being reshelved
     * @param storeType The store type (INSTORE or ONLINE)
     */
    void logReshelving(String itemCode, int quantity, StoreType storeType);

    /**
     * Log a reshelving activity with default store type (INSTORE).
     * 
     * @param itemCode The item code being reshelved
     * @param quantity The quantity being reshelved
     */
    void logReshelving(String itemCode, int quantity);

    /**
     * Get reshelve history for a specific item.
     * 
     * @param itemCode The item code to get history for
     * @return List of reshelved log entries
     */
    List<ReshelvedLogDTO> getReshelveHistory(String itemCode);

    /**
     * Get all reshelve history (limited to 100 recent entries).
     * 
     * @return List of all reshelved log entries
     */
    List<ReshelvedLogDTO> getAllReshelveHistory();
}
