package syos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import syos.data.OnlineBillGateway;
import syos.data.OnlineItemGateway;
import syos.dto.ItemDTO;
import syos.model.Bill;
import syos.model.CartItem;
import syos.model.OnlineUser;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OnlinePOSTest {

    @Mock
    private OnlineItemGateway itemGateway;

    @Mock
    private OnlineBillGateway billGateway;

    private OnlinePOS onlinePOS;
    private OnlineUser testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        onlinePOS = new OnlinePOS();
        testUser = new OnlineUser("testuser", "password123", "1234567890", "Test Address");
    }

    @Test
    @DisplayName("Should add item to cart successfully")
    void testAddToCart() {
        // Arrange
        ItemDTO item = new ItemDTO("ONL001", "Online Product", 100.0, 10);

        // Act
        onlinePOS.addToCart(item, 2);

        // Assert
        assertEquals(200.0, onlinePOS.getCartTotal());
        assertEquals(1, onlinePOS.getCartItems().size());
    }

    @Test
    @DisplayName("Should throw exception when adding null item")
    void testAddToCartNullItem() {
        assertThrows(IllegalArgumentException.class,
                () -> onlinePOS.addToCart(null, 1));
    }

    @Test
    @DisplayName("Should throw exception when quantity exceeds stock")
    void testAddToCartExceedsStock() {
        ItemDTO item = new ItemDTO("ONL001", "Online Product", 100.0, 5);

        assertThrows(IllegalArgumentException.class,
                () -> onlinePOS.addToCart(item, 10));
    }

    @Test
    @DisplayName("Should merge quantities for same item")
    void testAddToCartMergeItems() {
        // Arrange
        ItemDTO item = new ItemDTO("ONL001", "Online Product", 100.0, 10);

        // Act
        onlinePOS.addToCart(item, 2);
        onlinePOS.addToCart(item, 3);

        // Assert
        assertEquals(500.0, onlinePOS.getCartTotal());
        assertEquals(1, onlinePOS.getCartItems().size());
        assertEquals(5, onlinePOS.getCartItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("Should clear cart")
    void testClearCart() {
        // Arrange
        ItemDTO item = new ItemDTO("ONL001", "Online Product", 100.0, 10);
        onlinePOS.addToCart(item, 2);

        // Act
        onlinePOS.clearCart();

        // Assert
        assertTrue(onlinePOS.getCartItems().isEmpty());
        assertEquals(0.0, onlinePOS.getCartTotal());
    }

    @Test
    @DisplayName("Should throw exception for checkout with null user")
    void testCheckoutNullUser() {
        ItemDTO item = new ItemDTO("ONL001", "Online Product", 100.0, 10);
        onlinePOS.addToCart(item, 1);

        assertThrows(IllegalArgumentException.class,
                () -> onlinePOS.checkout("Cash on Delivery", null));
    }

    @Test
    @DisplayName("Should throw exception for checkout with empty cart")
    void testCheckoutEmptyCart() {
        assertThrows(IllegalArgumentException.class,
                () -> onlinePOS.checkout("Cash on Delivery", testUser));
    }
}