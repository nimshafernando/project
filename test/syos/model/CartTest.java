package syos.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import syos.service.Cart;
import syos.dto.ItemDTO;

public class CartTest {

    @Test
    public void testAddSingleItem() {
        Cart cart = new Cart();
        ItemDTO item = new ItemDTO("A101", "Milk", 120.0, 10);
        cart.addItem(item, 2);

        assertEquals(1, cart.getItems().size());
        assertEquals(240.0, cart.getTotal(), 0.01);
    }

    @Test
    public void testAddMultipleItems() {
        Cart cart = new Cart();
        cart.addItem(new ItemDTO("A101", "Milk", 120.0, 10), 2);
        cart.addItem(new ItemDTO("B202", "Bread", 80.0, 5), 1);

        assertEquals(2, cart.getItems().size());
        assertEquals(320.0, cart.getTotal(), 0.01);
    }
}
