package syos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import syos.dto.ItemDTO;
import syos.model.CartItem;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test coverage for Cart service class
 * Tests all methods, cart operations, calculations, and edge cases with Mockito
 */
class CartTest {

    @Mock
    private ItemDTO mockItem1;

    @Mock
    private ItemDTO mockItem2;

    @Mock
    private ItemDTO mockItem3;

    private Cart cart;
    private ItemDTO realItem1;
    private ItemDTO realItem2;
    private ItemDTO realItem3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cart = new Cart();

        // Setup real ItemDTOs for basic tests
        realItem1 = new ItemDTO("FOOD001", "Premium Organic Apples", 4.50, 100);
        realItem2 = new ItemDTO("BVRG002", "Italian Espresso Coffee", 12.99, 50);
        realItem3 = new ItemDTO("SNCK003", "Artisan Dark Chocolate", 8.75, 25);

        // Setup mock ItemDTOs with realistic grocery store data
        when(mockItem1.getCode()).thenReturn("DAIRY001");
        when(mockItem1.getName()).thenReturn("Fresh Whole Milk 1L");
        when(mockItem1.getPrice()).thenReturn(3.25);
        when(mockItem1.getQuantity()).thenReturn(200);

        when(mockItem2.getCode()).thenReturn("BREAD002");
        when(mockItem2.getName()).thenReturn("Sourdough Artisan Bread");
        when(mockItem2.getPrice()).thenReturn(5.50);
        when(mockItem2.getQuantity()).thenReturn(30);

        when(mockItem3.getCode()).thenReturn("MEAT003");
        when(mockItem3.getName()).thenReturn("Premium Beef Steak 500g");
        when(mockItem3.getPrice()).thenReturn(24.99);
        when(mockItem3.getQuantity()).thenReturn(15);
    }

    // === Constructor Tests ===

    @Test
    @DisplayName("Should create empty cart")
    void testCartConstructor() {
        // Act & Assert
        assertNotNull(cart.getItems());
        assertTrue(cart.getItems().isEmpty());
        assertEquals(0.0, cart.getTotal(), 0.01);
    }

    // === Add Item Tests ===

    @Test
    @DisplayName("Should add single item to cart")
    void testAddSingleItem() {
        // Act
        cart.addItem(realItem1, 3);

        // Assert
        List<CartItem> items = cart.getItems();
        assertEquals(1, items.size());
        assertEquals(realItem1, items.get(0).getItem());
        assertEquals(3, items.get(0).getQuantity());
        assertEquals(13.50, cart.getTotal(), 0.01); // 4.50 * 3
    }

    @Test
    @DisplayName("Should add multiple different items to cart")
    void testAddMultipleItems() {
        // Act
        cart.addItem(realItem1, 2);
        cart.addItem(realItem2, 1);
        cart.addItem(realItem3, 3);

        // Assert
        List<CartItem> items = cart.getItems();
        assertEquals(3, items.size());

        // Verify first item
        assertEquals(realItem1, items.get(0).getItem());
        assertEquals(2, items.get(0).getQuantity());

        // Verify second item
        assertEquals(realItem2, items.get(1).getItem());
        assertEquals(1, items.get(1).getQuantity());

        // Verify third item
        assertEquals(realItem3, items.get(2).getItem());
        assertEquals(3, items.get(2).getQuantity());

        // Verify total: (4.50*2) + (12.99*1) + (8.75*3) = 9.00 + 12.99 + 26.25 = 48.24
        assertEquals(48.24, cart.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Should add item with quantity 1")
    void testAddItemQuantityOne() {
        // Act
        cart.addItem(realItem2, 1);

        // Assert
        List<CartItem> items = cart.getItems();
        assertEquals(1, items.size());
        assertEquals(1, items.get(0).getQuantity());
        assertEquals(12.99, cart.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Should add item with large quantity")
    void testAddItemLargeQuantity() {
        // Act
        cart.addItem(realItem1, 50);

        // Assert
        List<CartItem> items = cart.getItems();
        assertEquals(1, items.size());
        assertEquals(50, items.get(0).getQuantity());
        assertEquals(225.00, cart.getTotal(), 0.01); // 4.50 * 50
    }

    @Test
    @DisplayName("Should handle adding expensive items")
    void testAddExpensiveItems() {
        // Act
        cart.addItem(mockItem3, 2); // Premium Beef Steak

        // Assert
        List<CartItem> items = cart.getItems();
        assertEquals(1, items.size());
        assertEquals(2, items.get(0).getQuantity());
        assertEquals(49.98, cart.getTotal(), 0.01); // 24.99 * 2
    }

    // === Get Total Tests ===

    @Test
    @DisplayName("Should return zero total for empty cart")
    void testGetTotalEmptyCart() {
        // Act & Assert
        assertEquals(0.0, cart.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Should calculate total for single item")
    void testGetTotalSingleItem() {
        // Arrange
        cart.addItem(realItem3, 4);

        // Act & Assert
        assertEquals(35.00, cart.getTotal(), 0.01); // 8.75 * 4
    }

    @Test
    @DisplayName("Should calculate total for multiple items")
    void testGetTotalMultipleItems() {
        // Arrange
        cart.addItem(realItem1, 5); // 4.50 * 5 = 22.50
        cart.addItem(mockItem1, 3); // 3.25 * 3 = 9.75
        cart.addItem(realItem3, 2); // 8.75 * 2 = 17.50

        // Act & Assert
        assertEquals(49.75, cart.getTotal(), 0.01); // 22.50 + 9.75 + 17.50
    }

    @Test
    @DisplayName("Should handle decimal price calculations correctly")
    void testGetTotalDecimalPrecision() {
        // Arrange
        cart.addItem(realItem2, 3); // 12.99 * 3 = 38.97

        // Act & Assert
        assertEquals(38.97, cart.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Should calculate total for large cart")
    void testGetTotalLargeCart() {
        // Arrange - Add many items
        cart.addItem(realItem1, 10); // 4.50 * 10 = 45.00
        cart.addItem(realItem2, 5); // 12.99 * 5 = 64.95
        cart.addItem(realItem3, 8); // 8.75 * 8 = 70.00
        cart.addItem(mockItem1, 12); // 3.25 * 12 = 39.00
        cart.addItem(mockItem2, 6); // 5.50 * 6 = 33.00
        cart.addItem(mockItem3, 3); // 24.99 * 3 = 74.97

        // Act & Assert
        assertEquals(326.92, cart.getTotal(), 0.01); // Sum of all above
    }

    // === Get Items Tests ===

    @Test
    @DisplayName("Should return empty list for new cart")
    void testGetItemsEmptyCart() {
        // Act
        List<CartItem> items = cart.getItems();

        // Assert
        assertNotNull(items);
        assertTrue(items.isEmpty());
        assertEquals(0, items.size());
    }

    @Test
    @DisplayName("Should return correct items list")
    void testGetItemsWithContent() {
        // Arrange
        cart.addItem(realItem1, 2);
        cart.addItem(mockItem2, 3);

        // Act
        List<CartItem> items = cart.getItems();

        // Assert
        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals(realItem1, items.get(0).getItem());
        assertEquals(2, items.get(0).getQuantity());
        assertEquals(mockItem2, items.get(1).getItem());
        assertEquals(3, items.get(1).getQuantity());
    }

    @Test
    @DisplayName("Should return list that reflects current cart state")
    void testGetItemsReflectsCurrentState() {
        // Arrange
        cart.addItem(realItem1, 1);
        List<CartItem> firstCheck = cart.getItems();
        assertEquals(1, firstCheck.size());

        // Act - Add more items
        cart.addItem(realItem2, 2);
        cart.addItem(realItem3, 3);
        List<CartItem> secondCheck = cart.getItems();

        // Assert
        assertEquals(3, secondCheck.size());
        assertEquals(realItem1, secondCheck.get(0).getItem());
        assertEquals(realItem2, secondCheck.get(1).getItem());
        assertEquals(realItem3, secondCheck.get(2).getItem());
    }

    // === Business Logic Tests ===

    @Test
    @DisplayName("Should handle realistic grocery shopping scenario")
    void testRealisticGroceryScenario() {
        // Arrange - Typical grocery shopping cart
        cart.addItem(realItem1, 6); // 6 apples
        cart.addItem(mockItem1, 2); // 2 milk cartons
        cart.addItem(mockItem2, 1); // 1 bread
        cart.addItem(realItem3, 4); // 4 chocolate bars

        // Act & Assert
        List<CartItem> items = cart.getItems();
        assertEquals(4, items.size());

        // Verify total: (4.50*6) + (3.25*2) + (5.50*1) + (8.75*4) = 27.00 + 6.50 + 5.50
        // + 35.00 = 74.00
        assertEquals(74.00, cart.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Should handle restaurant order scenario")
    void testRestaurantOrderScenario() {
        // Arrange - Restaurant order with expensive items
        cart.addItem(mockItem3, 2); // 2 steaks
        cart.addItem(realItem2, 3); // 3 coffees

        // Act & Assert
        List<CartItem> items = cart.getItems();
        assertEquals(2, items.size());

        // Verify total: (24.99*2) + (12.99*3) = 49.98 + 38.97 = 88.95
        assertEquals(88.95, cart.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Should handle bulk purchase scenario")
    void testBulkPurchaseScenario() {
        // Arrange - Bulk buying for store
        cart.addItem(realItem1, 100); // 100 units of apples

        // Act & Assert
        assertEquals(1, cart.getItems().size());
        assertEquals(450.00, cart.getTotal(), 0.01); // 4.50 * 100
    }

    // === Edge Cases and Error Scenarios ===

    @Test
    @DisplayName("Should handle zero quantity gracefully")
    void testAddItemZeroQuantity() {
        // Act
        cart.addItem(realItem1, 0);

        // Assert
        List<CartItem> items = cart.getItems();
        assertEquals(1, items.size());
        assertEquals(0, items.get(0).getQuantity());
        assertEquals(0.0, cart.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Should handle item with zero price")
    void testAddItemZeroPrice() {
        // Arrange
        ItemDTO freeItem = new ItemDTO("FREE001", "Free Sample", 0.0, 100);

        // Act
        cart.addItem(freeItem, 5);

        // Assert
        assertEquals(1, cart.getItems().size());
        assertEquals(0.0, cart.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Should handle null item gracefully")
    void testAddNullItem() {
        // Act & Assert - Should not throw exception, but behavior may vary
        assertDoesNotThrow(() -> cart.addItem(null, 1));
    }

    @Test
    @DisplayName("Should handle negative quantity")
    void testAddItemNegativeQuantity() {
        // Act
        cart.addItem(realItem1, -2);

        // Assert
        assertEquals(1, cart.getItems().size());
        assertEquals(-2, cart.getItems().get(0).getQuantity());
        assertEquals(-9.0, cart.getTotal(), 0.01); // 4.50 * -2
    }

    @Test
    @DisplayName("Should maintain cart state consistency")
    void testCartStateConsistency() {
        // Arrange
        cart.addItem(realItem1, 3);
        cart.addItem(realItem2, 2);

        double initialTotal = cart.getTotal();
        int initialSize = cart.getItems().size();

        // Act - Get items multiple times
        List<CartItem> items1 = cart.getItems();
        List<CartItem> items2 = cart.getItems();

        // Assert - State should remain consistent
        assertEquals(initialTotal, cart.getTotal(), 0.01);
        assertEquals(initialSize, cart.getItems().size());
        assertEquals(items1.size(), items2.size());
    }

    // === Performance and Large Data Tests ===

    @Test
    @DisplayName("Should handle many items efficiently")
    void testManyItemsPerformance() {
        // Arrange & Act
        for (int i = 0; i < 100; i++) {
            cart.addItem(realItem1, 1);
        }

        // Assert
        assertEquals(100, cart.getItems().size());
        assertEquals(450.0, cart.getTotal(), 0.01); // 4.50 * 100
    }

    @Test
    @DisplayName("Should handle items with very high prices")
    void testHighPriceItems() {
        // Arrange
        ItemDTO luxuryItem = new ItemDTO("LUX001", "Luxury Watch", 5999.99, 1);

        // Act
        cart.addItem(luxuryItem, 1);

        // Assert
        assertEquals(1, cart.getItems().size());
        assertEquals(5999.99, cart.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Should verify mock interactions")
    void testMockInteractions() {
        // Act
        cart.addItem(mockItem1, 2);
        cart.addItem(mockItem2, 3);
        double total = cart.getTotal();

        // Assert mock interactions
        verify(mockItem1, atLeastOnce()).getPrice();
        verify(mockItem2, atLeastOnce()).getPrice();

        // Verify no unexpected calls
        verify(mockItem1, never()).getQuantity();
        verify(mockItem2, never()).getQuantity();
        verify(mockItem1, never()).getName();
        verify(mockItem2, never()).getName();
    }
}
