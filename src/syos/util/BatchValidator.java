package syos.util;

import syos.dto.BatchDTO;

/**
 * Batch validation following Single Responsibility Principle
 * Only responsible for validating BatchDTO objects
 */
public class BatchValidator {

    public boolean isValid(BatchDTO batch) {
        return batch != null &&
                isValidItemCode(batch.getItemCode()) &&
                isValidName(batch.getName()) &&
                isValidPrice(batch.getSellingPrice()) &&
                isValidQuantity(batch.getQuantity()) &&
                isValidDates(batch);
    }

    private boolean isValidItemCode(String itemCode) {
        return itemCode != null && !itemCode.trim().isEmpty() && itemCode.length() <= 20;
    }

    private boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() <= 100;
    }

    private boolean isValidPrice(double price) {
        return price > 0 && price <= 999999.99;
    }

    private boolean isValidQuantity(int quantity) {
        return quantity >= 0;
    }

    private boolean isValidDates(BatchDTO batch) {
        return batch.getPurchaseDate() != null &&
                batch.getExpiryDate() != null &&
                !batch.getExpiryDate().isBefore(batch.getPurchaseDate());
    }
}
