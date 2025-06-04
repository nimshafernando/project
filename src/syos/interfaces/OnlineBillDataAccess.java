package syos.interfaces;

import syos.model.Bill;
import syos.model.CartItem;
import java.util.List;

/**
 * Online Bill data access interface following Interface Segregation Principle
 */
public interface OnlineBillDataAccess {

    /**
     * Save an online bill to the database
     * 
     * @param bill          The bill to save
     * @param username      The username
     * @param paymentMethod The payment method
     * @return true if successful, false otherwise
     */
    boolean saveOnlineBill(Bill bill, String username, String paymentMethod);

    /**
     * Get bills by username
     * 
     * @param username The username
     * @return List of bills
     */
    List<Bill> getBillsByUsername(String username);

    /**
     * Get items for a specific bill
     * 
     * @param billId The bill ID
     * @return List of cart items
     */
    List<CartItem> getItemsForBill(int billId);
}
