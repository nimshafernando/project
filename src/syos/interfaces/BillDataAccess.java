package syos.interfaces;

import syos.model.Bill;

/**
 * Bill data access interface following Interface Segregation Principle
 */
public interface BillDataAccess {

    /**
     * Save a bill to the database
     * 
     * @param bill The bill to save
     * @return true if successful, false otherwise
     */
    boolean saveBill(Bill bill);
}
