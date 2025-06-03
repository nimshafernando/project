package syos.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import syos.dto.ItemDTO;
import syos.model.Bill;

public class POSTest {

    @Test
    public void testCheckoutWithExactCash() {
        POS pos = new POS();
        pos.addToCart(new ItemDTO("A101", "Milk", 100.0, 10), 2); // Total = 200

        Bill bill = pos.checkout(200.0, 0.0); // Adding the second argument as 0.0

        assertEquals(200.0, bill.getTotal(), 0.01);
        assertEquals(0.0, bill.getChange(), 0.01);
    }

    @Test
    public void testCheckoutWithExtraCash() {
        POS pos = new POS();
        pos.addToCart(new ItemDTO("A101", "Milk", 100.0, 10), 1); // Total = 100

        Bill bill = pos.checkout(150.0, 0.0); // Assuming the second argument is 0.0 for this test

        assertEquals(50.0, bill.getChange(), 0.01);
    }

    @Test
    public void testCheckoutWithInsufficientCashThrowsException() {
        POS pos = new POS();
        pos.addToCart(new ItemDTO("A101", "Milk", 100.0, 10), 2); // Total = 200

        assertThrows(IllegalArgumentException.class, () -> {
            pos.checkout(100.0, 0.0); // Assuming the second argument is 0.0 for this test
        });
    }
}
