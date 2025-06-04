package syos.util;

import syos.dto.ItemDTO;

/**
 * Item validation following Single Responsibility Principle
 * Only responsible for validating ItemDTO objects
 */
public class ItemValidator {

    public boolean isValid(ItemDTO item) {
        return item != null &&
                isValidCode(item.getCode()) &&
                isValidName(item.getName()) &&
                isValidPrice(item.getPrice()) &&
                isValidQuantity(item.getQuantity());
    }

    private boolean isValidCode(String code) {
        return code != null && !code.trim().isEmpty() && code.length() <= 20;
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
}
