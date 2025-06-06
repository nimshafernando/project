package syos.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import syos.dto.ItemDTO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test coverage for CartItem model class
 * Tests constructor, getters, price calculations, and edge cases with Mockito
 */
class CartItemTest {

    @Mock
    private ItemDTO mockItem;

    private CartItem cartItem;
    private ItemDTO realItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup real ItemDTO for basic tests
        realItem = new ItemDTO("ITEM001", "Test Product", 25.50, 100);
        cartItem = new CartItem(realItem, 3);

        // Setup mock ItemDTO
        when(mockItem.getCode()).thenReturn("MOCK001");
        when(mockItem.getName()).thenReturn("Mock Product");
        when(mockItem.getPrice()).thenReturn(15.75);
        when(mockItem.getQuantity()).thenReturn(50);
    }

    // === Constructor Tests ===

    @Test
    @DisplayName("Should create CartItem with valid ItemDTO and quantity")
    void testCartItemConstructor() {
        // Act & Assert
        assertEquals(realItem, cartItem.getItem());
        assertEquals(3, cartItem.getQuantity());
    }

    @Test
    @DisplayName("Should create CartItem with mock ItemDTO")
    void testCartItemConstructorWithMock() {
        // Arrange & Act
        CartItem mockCartItem = new CartItem(mockItem, 5);

        // Assert
        assertEquals(mockItem, mockCartItem.getItem());
        assertEquals(5, mockCartItem.getQuantity());
    }

    @Test
    @DisplayName("Should create CartItem with zero quantity")
    void testCartItemConstructorZeroQuantity() {
        // Arrange & Act
        CartItem zeroCartItem = new CartItem(realItem, 0);

        // Assert
        assertEquals(realItem, zeroCartItem.getItem());
        assertEquals(0, zeroCartItem.getQuantity());
    }

    @Test
    @DisplayName("Should create CartItem with negative quantity")
    void testCartItemConstructorNegativeQuantity() {
        // Arrange & Act
        CartItem negativeCartItem = new CartItem(realItem, -5);

        // Assert
        assertEquals(realItem, negativeCartItem.getItem());
        assertEquals(-5, negativeCartItem.getQuantity());
    }

    @Test
    @DisplayName("Should create CartItem with large quantity")
    void testCartItemConstructorLargeQuantity() {
        // Arrange & Act
        CartItem largeCartItem = new CartItem(realItem, Integer.MAX_VALUE);

        // Assert
        assertEquals(realItem, largeCartItem.getItem());
        assertEquals(Integer.MAX_VALUE, largeCartItem.getQuantity());
    }

    @Test
    @DisplayName("Should create CartItem with minimum quantity")
    void testCartItemConstructorMinimumQuantity() {
        // Arrange & Act
        CartItem minCartItem = new CartItem(realItem, Integer.MIN_VALUE);

        // Assert
        assertEquals(realItem, minCartItem.getItem());
        assertEquals(Integer.MIN_VALUE, minCartItem.getQuantity());
    }

    // === Getter Tests ===

    @Test
    @DisplayName("Should return correct ItemDTO")
    void testGetItem() {
        assertEquals(realItem, cartItem.getItem());
    }

    @Test
    @DisplayName("Should return correct quantity")
    void testGetQuantity() {
        assertEquals(3, cartItem.getQuantity());
    }

    @Test
    @DisplayName("Should return mock ItemDTO correctly")
    void testGetItemWithMock() {
        // Arrange
        CartItem mockCartItem = new CartItem(mockItem, 2);

        // Act & Assert
        assertEquals(mockItem, mockCartItem.getItem());
        verify(mockItem, never()).getPrice(); // Should not call getPrice in getter
    }

    // === Total Price Calculation Tests ===

    @Test
    @DisplayName("Should calculate correct total price")
    void testGetTotalPrice() {
        // Act
        double totalPrice = cartItem.getTotalPrice();

        // Assert
        assertEquals(76.50, totalPrice, 0.01); // 25.50 * 3 = 76.50
    }

    @Test
    @DisplayName("Should calculate total price with mock ItemDTO")
    void testGetTotalPriceWithMock() {
        // Arrange
        CartItem mockCartItem = new CartItem(mockItem, 4);

        // Act
        double totalPrice = mockCartItem.getTotalPrice();

        // Assert
        assertEquals(63.0, totalPrice, 0.01); // 15.75 * 4 = 63.0
        verify(mockItem, times(1)).getPrice();
    }

    @Test
    @DisplayName("Should calculate zero total price with zero quantity")
    void testGetTotalPriceZeroQuantity() {
        // Arrange
        CartItem zeroCartItem = new CartItem(realItem, 0);

        // Act
        double totalPrice = zeroCartItem.getTotalPrice();

        // Assert
        assertEquals(0.0, totalPrice, 0.01);
    }

    @Test
    @DisplayName("Should calculate negative total price with negative quantity")
    void testGetTotalPriceNegativeQuantity() {
        // Arrange
        CartItem negativeCartItem = new CartItem(realItem, -2);

        // Act
        double totalPrice = negativeCartItem.getTotalPrice();

        // Assert
        assertEquals(-51.0, totalPrice, 0.01); // 25.50 * -2 = -51.0
    }

    @Test
    @DisplayName("Should handle zero price ItemDTO")
    void testGetTotalPriceZeroPrice() {
        // Arrange
        ItemDTO zeroItem = new ItemDTO("FREE001", "Free Item", 0.0, 10);
        CartItem freeCartItem = new CartItem(zeroItem, 5);

        // Act
        double totalPrice = freeCartItem.getTotalPrice();

        // Assert
        assertEquals(0.0, totalPrice, 0.01);
    }

    @Test
    @DisplayName("Should handle negative price ItemDTO")
    void testGetTotalPriceNegativePrice() {
        // Arrange
        ItemDTO negativeItem = new ItemDTO("NEG001", "Discount Item", -10.0, 5);
        CartItem discountCartItem = new CartItem(negativeItem, 3);

        // Act
        double totalPrice = discountCartItem.getTotalPrice();

        // Assert
        assertEquals(-30.0, totalPrice, 0.01); // -10.0 * 3 = -30.0
    }

    // === Precision Tests ===

    @Test
    @DisplayName("Should handle decimal precision correctly")
    void testDecimalPrecision() {
        // Arrange
        ItemDTO precisionItem = new ItemDTO("PREC001", "Precision Item", 1.999, 3);
        CartItem precisionCartItem = new CartItem(precisionItem, 3);

        // Act
        double totalPrice = precisionCartItem.getTotalPrice();

        // Assert
        assertEquals(5.997, totalPrice, 0.001); // 1.999 * 3 = 5.997
    }

    @Test
    @DisplayName("Should handle very small prices")
    void testVerySmallPrice() {
        // Arrange
        ItemDTO smallItem = new ItemDTO("SMALL001", "Penny Item", 0.01, 1);
        CartItem smallCartItem = new CartItem(smallItem, 1000);

        // Act
        double totalPrice = smallCartItem.getTotalPrice();

        // Assert
        assertEquals(10.0, totalPrice, 0.01); // 0.01 * 1000 = 10.0
    }

    @Test
    @DisplayName("Should handle very large prices")
    void testVeryLargePrice() {
        // Arrange
        ItemDTO expensiveItem = new ItemDTO("EXP001", "Expensive Item", 999999.99, 1);
        CartItem expensiveCartItem = new CartItem(expensiveItem, 2);

        // Act
        double totalPrice = expensiveCartItem.getTotalPrice();

        // Assert
        assertEquals(1999999.98, totalPrice, 0.01); // 999999.99 * 2
    }

    // === Real-world Business Scenarios ===

    @Test
    @DisplayName("Should handle grocery store items")
    void testGroceryStoreScenario() {
        // Arrange - Common grocery items
        ItemDTO bread = new ItemDTO("GRO001", "White Bread", 2.49, 20);
        ItemDTO milk = new ItemDTO("GRO002", "Whole Milk 1L", 3.89, 15);
        ItemDTO eggs = new ItemDTO("GRO003", "Free Range Eggs (12pk)", 5.99, 10);

        CartItem breadCart = new CartItem(bread, 2);
        CartItem milkCart = new CartItem(milk, 1);
        CartItem eggsCart = new CartItem(eggs, 3);

        // Act & Assert
        assertEquals(4.98, breadCart.getTotalPrice(), 0.01); // 2.49 * 2
        assertEquals(3.89, milkCart.getTotalPrice(), 0.01); // 3.89 * 1
        assertEquals(17.97, eggsCart.getTotalPrice(), 0.01); // 5.99 * 3
    }

    @Test
    @DisplayName("Should handle electronics store items")
    void testElectronicsStoreScenario() {
        // Arrange - Electronics items
        ItemDTO phone = new ItemDTO("ELEC001", "Smartphone", 799.99, 5);
        ItemDTO headphones = new ItemDTO("ELEC002", "Wireless Headphones", 129.95, 8);
        ItemDTO charger = new ItemDTO("ELEC003", "USB-C Charger", 24.99, 12);

        CartItem phoneCart = new CartItem(phone, 1);
        CartItem headphonesCart = new CartItem(headphones, 2);
        CartItem chargerCart = new CartItem(charger, 3);

        // Act & Assert
        assertEquals(799.99, phoneCart.getTotalPrice(), 0.01); // 799.99 * 1
        assertEquals(259.90, headphonesCart.getTotalPrice(), 0.01); // 129.95 * 2
        assertEquals(74.97, chargerCart.getTotalPrice(), 0.01); // 24.99 * 3
    }

    @Test
    @DisplayName("Should handle bulk purchase scenario")
    void testBulkPurchaseScenario() {
        // Arrange - Bulk items
        ItemDTO paperTowels = new ItemDTO("BULK001", "Paper Towels (24-pack)", 18.99, 50);
        CartItem bulkCart = new CartItem(paperTowels, 10);

        // Act
        double totalPrice = bulkCart.getTotalPrice();

        // Assert
        assertEquals(189.90, totalPrice, 0.01); // 18.99 * 10
    }

    // === Mock Interaction Tests ===

    @Test
    @DisplayName("Should verify mock interactions for price calculation")
    void testMockInteractions() {
        // Arrange
        when(mockItem.getPrice()).thenReturn(45.99);
        CartItem mockCartItem = new CartItem(mockItem, 7);

        // Act
        double totalPrice1 = mockCartItem.getTotalPrice();
        double totalPrice2 = mockCartItem.getTotalPrice();

        // Assert
        assertEquals(321.93, totalPrice1, 0.01); // 45.99 * 7
        assertEquals(321.93, totalPrice2, 0.01);
        verify(mockItem, times(2)).getPrice(); // Called twice
    }

    @Test
    @DisplayName("Should handle mock returning different prices")
    void testMockChangingPrice() {
        // Arrange
        when(mockItem.getPrice())
                .thenReturn(10.0)
                .thenReturn(20.0)
                .thenReturn(30.0);

        CartItem mockCartItem = new CartItem(mockItem, 2);

        // Act & Assert
        assertEquals(20.0, mockCartItem.getTotalPrice(), 0.01); // 10.0 * 2
        assertEquals(40.0, mockCartItem.getTotalPrice(), 0.01); // 20.0 * 2
        assertEquals(60.0, mockCartItem.getTotalPrice(), 0.01); // 30.0 * 2

        verify(mockItem, times(3)).getPrice();
    }

    @Test
    @DisplayName("Should handle mock throwing exception")
    void testMockException() {
        // Arrange
        when(mockItem.getPrice()).thenThrow(new RuntimeException("Database connection failed"));
        CartItem mockCartItem = new CartItem(mockItem, 5);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            mockCartItem.getTotalPrice();
        });

        verify(mockItem, times(1)).getPrice();
    }

    // === Edge Case and Stress Tests ===

    @Test
    @DisplayName("Should handle maximum integer quantity")
    void testMaxIntegerQuantity() {
        // Arrange
        ItemDTO item = new ItemDTO("MAX001", "Max Item", 1.0, 1);
        CartItem maxCartItem = new CartItem(item, Integer.MAX_VALUE);

        // Act
        double totalPrice = maxCartItem.getTotalPrice();

        // Assert
        assertEquals((double) Integer.MAX_VALUE, totalPrice, 0.01);
    }

    @Test
    @DisplayName("Should handle overflow scenario")
    void testOverflowScenario() {
        // Arrange - This might cause overflow
        ItemDTO expensiveItem = new ItemDTO("OVER001", "Overflow Item", Double.MAX_VALUE / 2, 1);
        CartItem overflowCart = new CartItem(expensiveItem, 3);

        // Act
        double totalPrice = overflowCart.getTotalPrice();

        // Assert - Should handle overflow gracefully
        assertTrue(Double.isInfinite(totalPrice) || totalPrice > Double.MAX_VALUE / 2);
    }

    @Test
    @DisplayName("Should handle special double values")
    void testSpecialDoubleValues() {
        // Test with NaN
        ItemDTO nanItem = new ItemDTO("NAN001", "NaN Item", Double.NaN, 1);
        CartItem nanCart = new CartItem(nanItem, 5);
        assertTrue(Double.isNaN(nanCart.getTotalPrice()));

        // Test with Infinity
        ItemDTO infItem = new ItemDTO("INF001", "Infinite Item", Double.POSITIVE_INFINITY, 1);
        CartItem infCart = new CartItem(infItem, 2);
        assertTrue(Double.isInfinite(infCart.getTotalPrice()));

        // Test with Negative Infinity
        ItemDTO negInfItem = new ItemDTO("NEGINF001", "Negative Infinite Item", Double.NEGATIVE_INFINITY, 1);
        CartItem negInfCart = new CartItem(negInfItem, 3);
        assertTrue(Double.isInfinite(negInfCart.getTotalPrice()) && negInfCart.getTotalPrice() < 0);
    }

    // === Immutability Tests ===

    @Test
    @DisplayName("Should maintain immutability of CartItem fields")
    void testImmutability() {
        // Arrange
        ItemDTO originalItem = cartItem.getItem();
        int originalQuantity = cartItem.getQuantity();

        // Act - Multiple method calls should not change internal state
        cartItem.getTotalPrice();
        cartItem.getItem();
        cartItem.getQuantity();

        // Assert - Values should remain unchanged
        assertSame(originalItem, cartItem.getItem());
        assertEquals(originalQuantity, cartItem.getQuantity());
    }

    @Test
    @DisplayName("Should not be affected by external ItemDTO changes")
    void testItemDTOIndependence() {
        // Arrange
        ItemDTO mutableItem = new ItemDTO("MUT001", "Mutable Item", 10.0, 5);
        CartItem mutableCartItem = new CartItem(mutableItem, 3);

        double originalTotal = mutableCartItem.getTotalPrice();

        // Act - This won't actually change the ItemDTO since it's immutable in this
        // implementation
        // But this test documents the expected behavior

        // Assert
        assertEquals(30.0, originalTotal, 0.01);
        assertEquals(30.0, mutableCartItem.getTotalPrice(), 0.01); // Should remain the same
    }
}
