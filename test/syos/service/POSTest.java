package syos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import syos.dto.ItemDTO;
import syos.model.Bill;
import syos.model.CartItem;

import java.util.List;

@DisplayName("POS Service Tests")
public class POSTest {

    private POS pos;
    private ItemDTO testItem1;
    private ItemDTO testItem2;

    @BeforeEach
    void setUp() {
        pos = new POS();
        testItem1 = new ItemDTO("A101", "Milk", 100.0, 10);
        testItem2 = new ItemDTO("A102", "Bread", 50.0, 20);
    }

    @Test
    @DisplayName("Should checkout with exact cash")
    public void testCheckoutWithExactCash() {
        pos.addToCart(testItem1, 2); // Total = 200

        Bill bill = pos.checkout(200.0, 0.0);

        assertEquals(200.0, bill.getTotal(), 0.01);
        assertEquals(0.0, bill.getChange(), 0.01);
        assertTrue(pos.getCartItems().isEmpty()); // Cart should be cleared
    }

    @Test
    @DisplayName("Should checkout with extra cash")
    public void testCheckoutWithExtraCash() {
        pos.addToCart(testItem1, 1); // Total = 100

        Bill bill = pos.checkout(150.0, 0.0);

        assertEquals(50.0, bill.getChange(), 0.01);
        assertEquals(100.0, bill.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Should throw exception when cash is insufficient")
    public void testCheckoutWithInsufficientCashThrowsException() {
        pos.addToCart(testItem1, 2); // Total = 200

        assertThrows(IllegalArgumentException.class, () -> {
            pos.checkout(100.0, 0.0);
        });
    }

    @Test
    @DisplayName("Should checkout with discount")
    public void testCheckoutWithDiscount() {
        pos.addToCart(testItem1, 2); // Total = 200

        Bill bill = pos.checkout(180.0, 20.0); // 20 discount

        assertEquals(180.0, bill.getTotal(), 0.01);
        assertEquals(0.0, bill.getChange(), 0.01);
        assertEquals(20.0, bill.getDiscount(), 0.01);
    }

    @Test
    @DisplayName("Should throw exception when discount exceeds total")
    public void testCheckoutWithExcessiveDiscount() {
        pos.addToCart(testItem1, 1); // Total = 100

        assertThrows(IllegalArgumentException.class, () -> {
            pos.checkout(50.0, 150.0); // Discount > total
        });
    }

    @Test
    @DisplayName("Should add item to cart successfully")
    public void testAddToCart() {
        pos.addToCart(testItem1, 2);

        assertEquals(200.0, pos.getCartTotal(), 0.01);
        assertEquals(1, pos.getCartItems().size());
        assertEquals(2, pos.getCartItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("Should throw exception when adding null item")
    public void testAddToCartNullItem() {
        assertThrows(IllegalArgumentException.class, () -> {
            pos.addToCart(null, 1);
        });
    }

    @Test
    @DisplayName("Should throw exception when adding zero quantity")
    public void testAddToCartZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            pos.addToCart(testItem1, 0);
        });
    }

    @Test
    @DisplayName("Should throw exception when adding negative quantity")
    public void testAddToCartNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            pos.addToCart(testItem1, -1);
        });
    }

    @Test
    @DisplayName("Should add multiple different items to cart")
    public void testAddMultipleItems() {
        pos.addToCart(testItem1, 2);
        pos.addToCart(testItem2, 3);

        assertEquals(350.0, pos.getCartTotal(), 0.01); // (100*2) + (50*3)
        assertEquals(2, pos.getCartItems().size());
    }

    @Test
    @DisplayName("Should get cart total correctly")
    public void testGetCartTotal() {
        assertEquals(0.0, pos.getCartTotal(), 0.01);

        pos.addToCart(testItem1, 2);
        assertEquals(200.0, pos.getCartTotal(), 0.01);

        pos.addToCart(testItem2, 1);
        assertEquals(250.0, pos.getCartTotal(), 0.01);
    }

    @Test
    @DisplayName("Should get cart items as defensive copy")
    public void testGetCartItems() {
        pos.addToCart(testItem1, 2);
        pos.addToCart(testItem2, 1);

        List<CartItem> items = pos.getCartItems();

        assertNotNull(items);
        assertEquals(2, items.size());

        // Should be a defensive copy
        items.clear();
        assertEquals(2, pos.getCartItems().size()); // Original should be unchanged
    }

    @Test
    @DisplayName("Should return empty list when cart is empty")
    public void testGetCartItemsEmpty() {
        List<CartItem> items = pos.getCartItems();

        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("Should remove item from cart")
    public void testRemoveFromCart() {
        pos.addToCart(testItem1, 5);
        pos.removeFromCart("A101", 2);

        assertEquals(300.0, pos.getCartTotal(), 0.01); // 100*3
        assertEquals(1, pos.getCartItems().size());
        assertEquals(3, pos.getCartItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("Should remove entire item when quantity equals cart quantity")
    public void testRemoveEntireItem() {
        pos.addToCart(testItem1, 3);
        pos.removeFromCart("A101", 3);

        assertEquals(0.0, pos.getCartTotal(), 0.01);
        assertTrue(pos.getCartItems().isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when removing with null item code")
    public void testRemoveFromCartNullCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            pos.removeFromCart(null, 1);
        });
    }

    @Test
    @DisplayName("Should throw exception when removing with empty item code")
    public void testRemoveFromCartEmptyCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            pos.removeFromCart("", 1);
        });
    }

    @Test
    @DisplayName("Should throw exception when removing zero quantity")
    public void testRemoveFromCartZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            pos.removeFromCart("A101", 0);
        });
    }

    @Test
    @DisplayName("Should throw exception when removing negative quantity")
    public void testRemoveFromCartNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            pos.removeFromCart("A101", -1);
        });
    }

    @Test
    @DisplayName("Should clear cart")
    public void testClearCart() {
        pos.addToCart(testItem1, 2);
        pos.addToCart(testItem2, 3);

        pos.clearCart();

        assertEquals(0.0, pos.getCartTotal(), 0.01);
        assertTrue(pos.getCartItems().isEmpty());
    }

    @Test
    @DisplayName("Should undo last add operation")
    public void testUndoAddOperation() {
        pos.addToCart(testItem1, 2);
        assertEquals(200.0, pos.getCartTotal(), 0.01);

        boolean undone = pos.undoLastOperation();

        assertTrue(undone);
        assertEquals(0.0, pos.getCartTotal(), 0.01);
        assertTrue(pos.getCartItems().isEmpty());
    }

    @Test
    @DisplayName("Should undo clear cart operation")
    public void testUndoClearCart() {
        pos.addToCart(testItem1, 2);
        pos.addToCart(testItem2, 1);
        pos.clearCart();
        assertTrue(pos.getCartItems().isEmpty());

        boolean undone = pos.undoLastOperation();

        assertTrue(undone);
        assertEquals(2, pos.getCartItems().size());
        assertEquals(250.0, pos.getCartTotal(), 0.01);
    }

    @Test
    @DisplayName("Should return false when no operations to undo")
    public void testUndoWhenNoOperations() {
        boolean undone = pos.undoLastOperation();

        assertFalse(undone);
    }

    @Test
    @DisplayName("Should redo last undone operation")
    public void testRedoOperation() {
        pos.addToCart(testItem1, 2);
        pos.undoLastOperation();
        assertEquals(0.0, pos.getCartTotal(), 0.01);

        boolean redone = pos.redoLastOperation();

        assertTrue(redone);
        assertEquals(200.0, pos.getCartTotal(), 0.01);
        assertEquals(1, pos.getCartItems().size());
    }

    @Test
    @DisplayName("Should return false when no operations to redo")
    public void testRedoWhenNoOperations() {
        boolean redone = pos.redoLastOperation();

        assertFalse(redone);
    }

    @Test
    @DisplayName("Should clear redo history when new operation is performed")
    public void testRedoHistoryClearedOnNewOperation() {
        pos.addToCart(testItem1, 2);
        pos.undoLastOperation();
        assertTrue(pos.canRedo());

        pos.addToCart(testItem2, 1); // This should clear redo history

        assertFalse(pos.canRedo());
    }

    @Test
    @DisplayName("Should get command history")
    public void testGetCommandHistory() {
        pos.addToCart(testItem1, 2);
        pos.addToCart(testItem2, 1);
        pos.removeFromCart("A101", 1);

        List<String> history = pos.getCommandHistory();

        assertNotNull(history);
        assertEquals(3, history.size());
        assertTrue(history.get(0).contains("Added 2 x Milk to cart"));
        assertTrue(history.get(1).contains("Added 1 x Bread to cart"));
        assertTrue(history.get(2).contains("Removed 1 x Milk from cart"));
    }

    @Test
    @DisplayName("Should return empty history when no operations performed")
    public void testGetCommandHistoryEmpty() {
        List<String> history = pos.getCommandHistory();

        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    @DisplayName("Should check if undo is possible")
    public void testCanUndo() {
        assertFalse(pos.canUndo());

        pos.addToCart(testItem1, 2);
        assertTrue(pos.canUndo());

        pos.undoLastOperation();
        assertFalse(pos.canUndo());
    }

    @Test
    @DisplayName("Should check if redo is possible")
    public void testCanRedo() {
        assertFalse(pos.canRedo());

        pos.addToCart(testItem1, 2);
        assertFalse(pos.canRedo());

        pos.undoLastOperation();
        assertTrue(pos.canRedo());

        pos.redoLastOperation();
        assertFalse(pos.canRedo());
    }

    @Test
    @DisplayName("Should get cart from POS")
    public void testGetCart() {
        assertNotNull(pos.getCart());

        pos.addToCart(testItem1, 2);
        assertEquals(200.0, pos.getCart().getTotal(), 0.01);
    }

    @Test
    @DisplayName("Should limit command history size")
    public void testCommandHistoryLimit() {
        // Add more than MAX_HISTORY_SIZE operations
        for (int i = 0; i < 60; i++) {
            pos.addToCart(new ItemDTO("ITEM" + i, "Product " + i, 10.0, 100), 1);
        }

        List<String> history = pos.getCommandHistory();

        // Should not exceed MAX_HISTORY_SIZE (50)
        assertTrue(history.size() <= 50);
    }
}
