package syos.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import syos.model.Bill;
import syos.model.CartItem;
import syos.dto.ItemDTO;

import java.time.LocalDateTime;
import java.util.Collections;

public class BillBuilderTest {

    @Test
    public void testBuildSimpleBill() {
        ItemDTO item = new ItemDTO("A101", "Milk", 100.0, 10);
        CartItem cartItem = new CartItem(item, 2); // 200 total

        Bill bill = new BillBuilder()
                .setSerialNumber(1)
                .setDate(LocalDateTime.of(2025, 5, 1, 10, 30))
                .setItems(Collections.singletonList(cartItem))
                .setTotal(200.0)
                .setDiscount(0.0)
                .setCashTendered(250.0)
                .setChange(50.0)
                .build();

        assertEquals(1, bill.getSerialNumber());
        assertEquals(200.0, bill.getTotal(), 0.01);
        assertEquals(50.0, bill.getChange(), 0.01);
        assertEquals(1, bill.getItems().size());
    }
}
