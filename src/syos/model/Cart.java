package syos.model;

import syos.dto.ItemDTO;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    public void addItem(ItemDTO item, int quantity) {
        items.add(new CartItem(item, quantity));
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double calculateTotal() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
