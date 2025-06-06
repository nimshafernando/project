package syos.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import syos.dto.ItemDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test coverage for Cart model class
 * Tests all methods, collections handling, calculations, and edge cases with
 * Mockito
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
        realItem1 = new ItemDTO("ITEM001", "Apple", 1.50, 100);
        realItem2 = new ItemDTO("ITEM002", "Banana", 0.80, 200);
        realItem3 = new ItemDTO("ITEM003", "Orange", 2.25, 150);

        // Setup mock ItemDTOs
        when(mockItem1.getCode()).thenReturn("MOCK001");
        when(mockItem1.getName()).thenReturn("Mock Apple");
        when(mockItem1.getPrice()).thenReturn(3.50);
        when(mockItem1.getQuantity()).thenReturn(50);

        when(mockItem2.getCode()).thenReturn("MOCK002");
        when(mockItem2.getName()).thenReturn("Mock Banana");
        when(mockItem2.getPrice()).thenReturn(1.25);
        when(mockItem2.getQuantity()).thenReturn(75);

        when(mockItem3.getCode()).thenReturn("MOCK003");
        when(mockItem3.getName()).thenReturn("Mock Orange");
        when(mockItem3.getPrice()).thenReturn(4.00);
        when(mockItem3.getQuantity()).thenReturn(30);
    }

    // === Constructor and Initial State Tests ===

    @Test
    @DisplayName("Should create empty cart")
    void testCartConstructor() {
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItems().size());
        assertEquals(0.0, cart.calculateTotal(), 0.01);
    }

    @Test
    @DisplayName("Should have empty items list initially")
    void testInitialItemsList() {
        List<CartItem> items = cart.getItems();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    // === Add Item Tests ===

    @Test
    @DisplayName("Should add single item to cart")
    void testAddSingleItem() {
        // Act
        cart.addItem(realItem1, 3);

        // Assert
        assertFalse(cart.isEmpty());
        assertEquals(1, cart.getItems().size());

        CartItem cartItem = cart.getItems().get(0);
        assertEquals(realItem1, cartItem.getItem());
        assertEquals(3, cartItem.getQuantity());
    }

    @Test
    @DisplayName("Should add multiple different items")
    void testAddMultipleDifferentItems() {
        // Act
        cart.addItem(realItem1, 2);
        cart.addItem(realItem2, 5);
        cart.addItem(realItem3, 1);

        // Assert
        assertEquals(3, cart.getItems().size());
        assertFalse(cart.isEmpty());

        // Verify first item
        CartItem item1 = cart.getItems().get(0);
        assertEquals(realItem1, item1.getItem());
        assertEquals(2, item1.getQuantity());

        // Verify second item
        CartItem item2 = cart.getItems().get(1);
        assertEquals(realItem2, item2.getItem());
        assertEquals(5, item2.getQuantity());

        // Verify third item
        CartItem item3 = cart.getItems().get(2);
        assertEquals(realItem3, item3.getItem());
        assertEquals(1, item3.getQuantity());
    }

    @Test
    @DisplayName("Should add mock items correctly")
    void testAddMockItems() {
        // Act
        cart.addItem(mockItem1, 4);
        cart.addItem(mockItem2, 7);

        // Assert
        assertEquals(2, cart.getItems().size());

        CartItem mockCartItem1 = cart.getItems().get(0);
        assertEquals(mockItem1, mockCartItem1.getItem());
        assertEquals(4, mockCartItem1.getQuantity());

        CartItem mockCartItem2 = cart.getItems().get(1);
        assertEquals(mockItem2, mockCartItem2.getItem());
        assertEquals(7, mockCartItem2.getQuantity());
    }

    @Test
    @DisplayName("Should add item with zero quantity")
    void testAddItemZeroQuantity() {
        // Act
        cart.addItem(realItem1, 0);

        // Assert
        assertEquals(1, cart.getItems().size());
        CartItem cartItem = cart.getItems().get(0);
        assertEquals(0, cartItem.getQuantity());
    }

    @Test
    @DisplayName("Should add item with negative quantity")
    void testAddItemNegativeQuantity() {
        // Act
        cart.addItem(realItem1, -5);

        // Assert
        assertEquals(1, cart.getItems().size());
        CartItem cartItem = cart.getItems().get(0);
        assertEquals(-5, cartItem.getQuantity());
    }

    @Test
    @DisplayName("Should add item with maximum quantity")
    void testAddItemMaxQuantity() {
        // Act
        cart.addItem(realItem1, Integer.MAX_VALUE);

        // Assert
        assertEquals(1, cart.getItems().size());
        CartItem cartItem = cart.getItems().get(0);
        assertEquals(Integer.MAX_VALUE, cartItem.getQuantity());
    }

    // === Get Items Tests ===

    @Test
    @DisplayName("Should return correct items list")
    void testGetItems() {
        // Arrange
        cart.addItem(realItem1, 2);
        cart.addItem(realItem2, 3);

        // Act
        List<CartItem> items = cart.getItems();

        // Assert
        assertEquals(2, items.size());
        assertEquals(realItem1, items.get(0).getItem());
        assertEquals(2, items.get(0).getQuantity());
        assertEquals(realItem2, items.get(1).getItem());
        assertEquals(3, items.get(1).getQuantity());
    }

    @Test
    @DisplayName("Should return empty list when cart is empty")
    void testGetItemsEmpty() {
        // Act
        List<CartItem> items = cart.getItems();

        // Assert
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("Should return same reference to items list")
    void testGetItemsReference() {
        // Act
        List<CartItem> items1 = cart.getItems();
        List<CartItem> items2 = cart.getItems();

        // Assert
        assertSame(items1, items2);
    }

    // === Calculate Total Tests ===

    @Test
    @DisplayName("Should calculate correct total for single item")
    void testCalculateTotalSingleItem() {
        // Arrange
        cart.addItem(realItem1, 4); // 1.50 * 4 = 6.00

        // Act
        double total = cart.calculateTotal();

        // Assert
        assertEquals(6.0, total, 0.01);
    }

    @Test
    @DisplayName("Should calculate correct total for multiple items")
    void testCalculateTotalMultipleItems() {
        // Arrange
        cart.addItem(realItem1, 3); // 1.50 * 3 = 4.50
        cart.addItem(realItem2, 5); // 0.80 * 5 = 4.00
        cart.addItem(realItem3, 2); // 2.25 * 2 = 4.50
        // Total = 4.50 + 4.00 + 4.50 = 13.00

        // Act
        double total = cart.calculateTotal();

        // Assert
        assertEquals(13.0, total, 0.01);
    }

    @Test
    @DisplayName("Should calculate zero total for empty cart")
    void testCalculateTotalEmptyCart() {
        // Act
        double total = cart.calculateTotal();

        // Assert
        assertEquals(0.0, total, 0.01);
    }

    @Test
    @DisplayName("Should calculate total with mock items")
    void testCalculateTotalWithMocks() {
        // Arrange
        cart.addItem(mockItem1, 2); // 3.50 * 2 = 7.00
        cart.addItem(mockItem2, 3); // 1.25 * 3 = 3.75
        // Total = 7.00 + 3.75 = 10.75

        // Act
        double total = cart.calculateTotal();

        // Assert
        assertEquals(10.75, total, 0.01);
        verify(mockItem1, times(1)).getPrice();
        verify(mockItem2, times(1)).getPrice();
    }

    @Test
    @DisplayName("Should calculate total with zero quantity items")
    void testCalculateTotalZeroQuantity() {
        // Arrange
        cart.addItem(realItem1, 0); // 1.50 * 0 = 0.00
        cart.addItem(realItem2, 3); // 0.80 * 3 = 2.40
        // Total = 0.00 + 2.40 = 2.40

        // Act
        double total = cart.calculateTotal();

        // Assert
        assertEquals(2.40, total, 0.01);
    }

    @Test
    @DisplayName("Should calculate total with negative quantity")
    void testCalculateTotalNegativeQuantity() {
        // Arrange
        cart.addItem(realItem1, -2); // 1.50 * -2 = -3.00
        cart.addItem(realItem2, 4); // 0.80 * 4 = 3.20
        // Total = -3.00 + 3.20 = 0.20

        // Act
        double total = cart.calculateTotal();

        // Assert
        assertEquals(0.20, total, 0.01);
    }

    // === Clear Tests ===

    @Test
    @DisplayName("Should clear empty cart")
    void testClearEmptyCart() {
        // Act
        cart.clear();

        // Assert
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItems().size());
        assertEquals(0.0, cart.calculateTotal(), 0.01);
    }

    @Test
    @DisplayName("Should clear cart with items")
    void testClearCartWithItems() {
        // Arrange
        cart.addItem(realItem1, 3);
        cart.addItem(realItem2, 5);
        assertFalse(cart.isEmpty());

        // Act
        cart.clear();

        // Assert
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItems().size());
        assertEquals(0.0, cart.calculateTotal(), 0.01);
    }

    @Test
    @DisplayName("Should clear cart multiple times")
    void testClearCartMultipleTimes() {
        // Arrange
        cart.addItem(realItem1, 2);
        cart.clear();
        cart.addItem(realItem2, 3);

        // Act
        cart.clear();

        // Assert
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItems().size());
    }

    // === Is Empty Tests ===

    @Test
    @DisplayName("Should return true for empty cart")
    void testIsEmptyTrue() {
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("Should return false for cart with items")
    void testIsEmptyFalse() {
        // Arrange
        cart.addItem(realItem1, 1);

        // Act & Assert
        assertFalse(cart.isEmpty());
    }

    @Test
    @DisplayName("Should return true after clearing cart")
    void testIsEmptyAfterClear() {
        // Arrange
        cart.addItem(realItem1, 5);
        assertFalse(cart.isEmpty());

        // Act
        cart.clear();

        // Assert
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("Should return false even with zero quantity items")
    void testIsEmptyWithZeroQuantityItems() {
        // Arrange
        cart.addItem(realItem1, 0);

        // Act & Assert
        assertFalse(cart.isEmpty()); // Cart still contains an item, even with 0 quantity
    }

    // === Real-world Business Scenarios ===

    @Test
    @DisplayName("Should handle grocery shopping scenario")
    void testGroceryShoppingScenario() {
        // Arrange - Typical grocery shopping
        ItemDTO bread = new ItemDTO("GRO001", "Whole Wheat Bread", 3.49, 20);
        ItemDTO milk = new ItemDTO("GRO002", "2% Milk 2L", 4.29, 15);
        ItemDTO eggs = new ItemDTO("GRO003", "Large Eggs (12pk)", 4.99, 25);
        ItemDTO apples = new ItemDTO("GRO004", "Gala Apples (3lb)", 5.99, 30);

        // Act
        cart.addItem(bread, 2); // 3.49 * 2 = 6.98
        cart.addItem(milk, 1); // 4.29 * 1 = 4.29
        cart.addItem(eggs, 2); // 4.99 * 2 = 9.98
        cart.addItem(apples, 1); // 5.99 * 1 = 5.99
        // Total = 6.98 + 4.29 + 9.98 + 5.99 = 27.24

        // Assert
        assertEquals(4, cart.getItems().size());
        assertEquals(27.24, cart.calculateTotal(), 0.01);
        assertFalse(cart.isEmpty());
    }

    @Test
    @DisplayName("Should handle online electronics shopping")
    void testElectronicsShoppingScenario() {
        // Arrange - Electronics shopping
        ItemDTO laptop = new ItemDTO("ELEC001", "Gaming Laptop", 1299.99, 3);
        ItemDTO mouse = new ItemDTO("ELEC002", "Wireless Mouse", 59.99, 10);
        ItemDTO keyboard = new ItemDTO("ELEC003", "Mechanical Keyboard", 149.99, 8);
        ItemDTO monitor = new ItemDTO("ELEC004", "27\" Monitor", 329.99, 5);

        // Act
        cart.addItem(laptop, 1); // 1299.99 * 1 = 1299.99
        cart.addItem(mouse, 2); // 59.99 * 2 = 119.98
        cart.addItem(keyboard, 1); // 149.99 * 1 = 149.99
        cart.addItem(monitor, 2); // 329.99 * 2 = 659.98
        // Total = 1299.99 + 119.98 + 149.99 + 659.98 = 2229.94

        // Assert
        assertEquals(4, cart.getItems().size());
        assertEquals(2229.94, cart.calculateTotal(), 0.01);
    }

    @Test
    @DisplayName("Should handle bulk warehouse shopping")
    void testBulkWarehouseScenario() {
        // Arrange - Bulk/warehouse shopping
        ItemDTO toiletPaper = new ItemDTO("BULK001", "Toilet Paper (36 rolls)", 24.99, 40);
        ItemDTO paperTowels = new ItemDTO("BULK002", "Paper Towels (12 rolls)", 18.99, 25);
        ItemDTO detergent = new ItemDTO("BULK003", "Laundry Detergent (5L)", 29.99, 15);

        // Act
        cart.addItem(toiletPaper, 3); // 24.99 * 3 = 74.97
        cart.addItem(paperTowels, 2); // 18.99 * 2 = 37.98
        cart.addItem(detergent, 1); // 29.99 * 1 = 29.99
        // Total = 74.97 + 37.98 + 29.99 = 142.94

        // Assert
        assertEquals(3, cart.getItems().size());
        assertEquals(142.94, cart.calculateTotal(), 0.01);
    }

    // === Large Scale Tests ===

    @Test
    @DisplayName("Should handle large number of items")
    void testLargeNumberOfItems() {
        // Arrange & Act - Add 100 different items
        for (int i = 0; i < 100; i++) {
            ItemDTO item = new ItemDTO("ITEM" + String.format("%03d", i),
                    "Item " + i,
                    (i + 1) * 1.0,
                    100);
            cart.addItem(item, 1);
        }

        // Assert
        assertEquals(100, cart.getItems().size());
        assertFalse(cart.isEmpty());

        // Total should be 1 + 2 + 3 + ... + 100 = 5050
        assertEquals(5050.0, cart.calculateTotal(), 0.01);
    }

    @Test
    @DisplayName("Should handle large quantities")
    void testLargeQuantities() {
        // Arrange
        ItemDTO item = new ItemDTO("LARGE001", "Bulk Item", 0.01, 1000000);

        // Act
        cart.addItem(item, 100000); // 0.01 * 100000 = 1000.0

        // Assert
        assertEquals(1, cart.getItems().size());
        assertEquals(1000.0, cart.calculateTotal(), 0.01);
    }

    // === Mock Interaction Tests ===

    @Test
    @DisplayName("Should verify mock interactions during total calculation")
    void testMockInteractionsForTotal() {
        // Arrange
        cart.addItem(mockItem1, 3);
        cart.addItem(mockItem2, 5);
        cart.addItem(mockItem3, 2);

        // Act
        cart.calculateTotal(); // First calculation
        cart.calculateTotal(); // Second calculation

        // Assert - Each item's getPrice() should be called twice
        verify(mockItem1, times(2)).getPrice();
        verify(mockItem2, times(2)).getPrice();
        verify(mockItem3, times(2)).getPrice();
    }

    @Test
    @DisplayName("Should handle mock returning different prices")
    void testMockChangingPrices() {
        // Arrange
        when(mockItem1.getPrice())
                .thenReturn(10.0)
                .thenReturn(20.0);

        cart.addItem(mockItem1, 2);

        // Act & Assert
        assertEquals(20.0, cart.calculateTotal(), 0.01); // 10.0 * 2 = 20.0
        assertEquals(40.0, cart.calculateTotal(), 0.01); // 20.0 * 2 = 40.0

        verify(mockItem1, times(2)).getPrice();
    }

    // === Edge Cases and Error Handling ===

    @Test
    @DisplayName("Should handle special double values in prices")
    void testSpecialDoubleValues() {
        // Test with NaN price
        ItemDTO nanItem = new ItemDTO("NAN001", "NaN Item", Double.NaN, 1);
        cart.addItem(nanItem, 1);
        assertTrue(Double.isNaN(cart.calculateTotal()));

        cart.clear();

        // Test with Infinity price
        ItemDTO infItem = new ItemDTO("INF001", "Infinite Item", Double.POSITIVE_INFINITY, 1);
        cart.addItem(infItem, 1);
        assertTrue(Double.isInfinite(cart.calculateTotal()));
    }

    @Test
    @DisplayName("Should handle precision edge cases")
    void testPrecisionEdgeCases() {
        // Arrange - Items with high precision prices
        ItemDTO item1 = new ItemDTO("PREC001", "Precision Item 1", 0.1, 1);
        ItemDTO item2 = new ItemDTO("PREC002", "Precision Item 2", 0.2, 1);
        ItemDTO item3 = new ItemDTO("PREC003", "Precision Item 3", 0.3, 1);

        // Act
        cart.addItem(item1, 1);
        cart.addItem(item2, 1);
        cart.addItem(item3, 1);

        // Assert - Should handle floating point precision
        assertEquals(0.6, cart.calculateTotal(), 0.01);
    }

    // === Integration Tests ===

    @Test
    @DisplayName("Should support complete shopping workflow")
    void testCompleteShoppingWorkflow() {
        // Initial state
        assertTrue(cart.isEmpty());
        assertEquals(0.0, cart.calculateTotal(), 0.01);

        // Add items
        cart.addItem(realItem1, 2);
        cart.addItem(realItem2, 3);
        assertEquals(2, cart.getItems().size());
        assertEquals(5.40, cart.calculateTotal(), 0.01); // (1.50*2) + (0.80*3) = 3.00 + 2.40

        // Add more items
        cart.addItem(realItem3, 1);
        assertEquals(3, cart.getItems().size());
        assertEquals(7.65, cart.calculateTotal(), 0.01); // 5.40 + (2.25*1)

        // Clear cart
        cart.clear();
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItems().size());
        assertEquals(0.0, cart.calculateTotal(), 0.01);

        // Add items again
        cart.addItem(realItem1, 5);
        assertEquals(1, cart.getItems().size());
        assertEquals(7.50, cart.calculateTotal(), 0.01); // 1.50 * 5
    }

    @Test
    @DisplayName("Should maintain consistent state across operations")
    void testStateConsistency() {
        // Test that isEmpty() is consistent with getItems().size()
        assertTrue(cart.isEmpty() == (cart.getItems().size() == 0));

        cart.addItem(realItem1, 1);
        assertTrue(cart.isEmpty() == (cart.getItems().size() == 0));

        cart.addItem(realItem2, 2);
        assertTrue(cart.isEmpty() == (cart.getItems().size() == 0));

        cart.clear();
        assertTrue(cart.isEmpty() == (cart.getItems().size() == 0));
    }
}
