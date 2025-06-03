package syos.model;

import syos.dto.ItemDTO;

public class CartItem {
    private ItemDTO item;
    private int quantity;

    public CartItem(ItemDTO item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public ItemDTO getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return item.getPrice() * quantity;
    }
}
