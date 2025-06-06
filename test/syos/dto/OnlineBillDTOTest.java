package syos.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import syos.model.CartItem;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("OnlineBillDTO Test Suite")
class OnlineBillDTOTest {

    private OnlineBillDTO onlineBillDTO;
    private LocalTime testTime;
    private LocalTime morningTime;
    private LocalTime eveningTime;
    private LocalTime midnightTime;

    @Mock
    private CartItem mockCartItem1;
    @Mock
    private CartItem mockCartItem2;
    @Mock
    private CartItem mockCartItem3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testTime = LocalTime.of(14, 30, 45);
        morningTime = LocalTime.of(9, 15, 0);
        eveningTime = LocalTime.of(20, 45, 30);
        midnightTime = LocalTime.of(0, 0, 0);
    }

    // Constructor tests - Retail online orders
    @Test
    @DisplayName("Should create OnlineBillDTO for retail customer order - multiple items")
    void testConstructor_RetailCustomerOrder() {
        List<CartItem> items = Arrays.asList(mockCartItem1, mockCartItem2);

        onlineBillDTO = new OnlineBillDTO(
                "ONLINE-2024-001", "customer@email.com", 156.75, 15.50,
                170.00, 13.25, testTime, items);

        assertEquals("ONLINE-2024-001", onlineBillDTO.getSerial());
        assertEquals("customer@email.com", onlineBillDTO.getUsername());
        assertEquals(156.75, onlineBillDTO.getTotal(), 0.001);
        assertEquals(15.50, onlineBillDTO.getDiscount(), 0.001);
        assertEquals(170.00, onlineBillDTO.getCash(), 0.001);
        assertEquals(13.25, onlineBillDTO.getChange(), 0.001);
        assertEquals(testTime, onlineBillDTO.getTime());
        assertEquals(items, onlineBillDTO.getItems());
        assertEquals(2, onlineBillDTO.getItems().size());
    }

    @Test
    @DisplayName("Should create OnlineBillDTO for premium customer - large order")
    void testConstructor_PremiumCustomerLargeOrder() {
        List<CartItem> items = Arrays.asList(mockCartItem1, mockCartItem2, mockCartItem3);

        onlineBillDTO = new OnlineBillDTO(
                "PREMIUM-2024-0052", "vip.customer@premium.com", 850.99, 85.10,
                900.00, 49.01, eveningTime, items);

        assertEquals("PREMIUM-2024-0052", onlineBillDTO.getSerial());
        assertEquals("vip.customer@premium.com", onlineBillDTO.getUsername());
        assertEquals(850.99, onlineBillDTO.getTotal(), 0.001);
        assertEquals(85.10, onlineBillDTO.getDiscount(), 0.001);
        assertEquals(900.00, onlineBillDTO.getCash(), 0.001);
        assertEquals(49.01, onlineBillDTO.getChange(), 0.001);
        assertEquals(eveningTime, onlineBillDTO.getTime());
        assertEquals(3, onlineBillDTO.getItems().size());
    }

    @Test
    @DisplayName("Should create OnlineBillDTO for business customer - corporate order")
    void testConstructor_BusinessCustomerOrder() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);

        onlineBillDTO = new OnlineBillDTO(
                "CORP-2024-789", "purchasing@businesscorp.com", 2550.00, 255.00,
                2550.00, 0.00, morningTime, items);

        assertEquals("CORP-2024-789", onlineBillDTO.getSerial());
        assertEquals("purchasing@businesscorp.com", onlineBillDTO.getUsername());
        assertEquals(2550.00, onlineBillDTO.getTotal(), 0.001);
        assertEquals(255.00, onlineBillDTO.getDiscount(), 0.001);
        assertEquals(2550.00, onlineBillDTO.getCash(), 0.001);
        assertEquals(0.00, onlineBillDTO.getChange(), 0.001);
        assertEquals(morningTime, onlineBillDTO.getTime());
        assertEquals(1, onlineBillDTO.getItems().size());
    }

    @Test
    @DisplayName("Should create OnlineBillDTO with exact payment - no change")
    void testConstructor_ExactPaymentNoChange() {
        List<CartItem> items = Arrays.asList(mockCartItem1, mockCartItem2);

        onlineBillDTO = new OnlineBillDTO(
                "EXACT-2024-100", "exact.customer@email.com", 99.99, 0.00,
                99.99, 0.00, midnightTime, items);

        assertEquals("EXACT-2024-100", onlineBillDTO.getSerial());
        assertEquals("exact.customer@email.com", onlineBillDTO.getUsername());
        assertEquals(99.99, onlineBillDTO.getTotal(), 0.001);
        assertEquals(0.00, onlineBillDTO.getDiscount(), 0.001);
        assertEquals(99.99, onlineBillDTO.getCash(), 0.001);
        assertEquals(0.00, onlineBillDTO.getChange(), 0.001);
        assertEquals(midnightTime, onlineBillDTO.getTime());
    }

    // Constructor with null values
    @Test
    @DisplayName("Should handle null values in constructor")
    void testConstructor_NullValues() {
        onlineBillDTO = new OnlineBillDTO(
                null, null, 0.0, 0.0,
                0.0, 0.0, null, null);

        assertNull(onlineBillDTO.getSerial());
        assertNull(onlineBillDTO.getUsername());
        assertEquals(0.0, onlineBillDTO.getTotal(), 0.001);
        assertEquals(0.0, onlineBillDTO.getDiscount(), 0.001);
        assertEquals(0.0, onlineBillDTO.getCash(), 0.001);
        assertEquals(0.0, onlineBillDTO.getChange(), 0.001);
        assertNull(onlineBillDTO.getTime());
        assertNull(onlineBillDTO.getItems());
    }

    @Test
    @DisplayName("Should handle empty list in constructor")
    void testConstructor_EmptyList() {
        List<CartItem> emptyItems = new ArrayList<>();

        onlineBillDTO = new OnlineBillDTO(
                "EMPTY-2024-001", "empty.customer@email.com", 0.0, 0.0,
                0.0, 0.0, testTime, emptyItems);

        assertEquals("EMPTY-2024-001", onlineBillDTO.getSerial());
        assertEquals("empty.customer@email.com", onlineBillDTO.getUsername());
        assertEquals(emptyItems, onlineBillDTO.getItems());
        assertTrue(onlineBillDTO.getItems().isEmpty());
    }

    @Test
    @DisplayName("Should handle negative values in constructor")
    void testConstructor_NegativeValues() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);

        onlineBillDTO = new OnlineBillDTO(
                "NEG-2024-001", "negative@test.com", -50.0, -10.0,
                -60.0, -20.0, testTime, items);

        assertEquals("NEG-2024-001", onlineBillDTO.getSerial());
        assertEquals("negative@test.com", onlineBillDTO.getUsername());
        assertEquals(-50.0, onlineBillDTO.getTotal(), 0.001);
        assertEquals(-10.0, onlineBillDTO.getDiscount(), 0.001);
        assertEquals(-60.0, onlineBillDTO.getCash(), 0.001);
        assertEquals(-20.0, onlineBillDTO.getChange(), 0.001);
    }

    // Getter tests
    @Test
    @DisplayName("Should return correct serial from getter")
    void testGetSerial() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);
        onlineBillDTO = new OnlineBillDTO(
                "SERIAL-TEST-123", "user@test.com", 100.0, 10.0,
                110.0, 10.0, testTime, items);
        assertEquals("SERIAL-TEST-123", onlineBillDTO.getSerial());
    }

    @Test
    @DisplayName("Should return correct username from getter")
    void testGetUsername() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);
        onlineBillDTO = new OnlineBillDTO(
                "BILL-001", "unique.username@domain.co.uk", 100.0, 10.0,
                110.0, 10.0, testTime, items);
        assertEquals("unique.username@domain.co.uk", onlineBillDTO.getUsername());
    }

    @Test
    @DisplayName("Should return correct total from getter")
    void testGetTotal() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);
        onlineBillDTO = new OnlineBillDTO(
                "BILL-001", "user@test.com", 567.89, 10.0,
                580.0, 12.11, testTime, items);
        assertEquals(567.89, onlineBillDTO.getTotal(), 0.001);
    }

    @Test
    @DisplayName("Should return correct discount from getter")
    void testGetDiscount() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);
        onlineBillDTO = new OnlineBillDTO(
                "BILL-001", "user@test.com", 100.0, 37.50,
                140.0, 40.0, testTime, items);
        assertEquals(37.50, onlineBillDTO.getDiscount(), 0.001);
    }

    @Test
    @DisplayName("Should return correct cash from getter")
    void testGetCash() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);
        onlineBillDTO = new OnlineBillDTO(
                "BILL-001", "user@test.com", 100.0, 10.0,
                250.75, 150.75, testTime, items);
        assertEquals(250.75, onlineBillDTO.getCash(), 0.001);
    }

    @Test
    @DisplayName("Should return correct change from getter")
    void testGetChange() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);
        onlineBillDTO = new OnlineBillDTO(
                "BILL-001", "user@test.com", 100.0, 10.0,
                150.0, 60.25, testTime, items);
        assertEquals(60.25, onlineBillDTO.getChange(), 0.001);
    }

    @Test
    @DisplayName("Should return correct time from getter")
    void testGetTime() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);
        LocalTime specificTime = LocalTime.of(16, 42, 33);
        onlineBillDTO = new OnlineBillDTO(
                "BILL-001", "user@test.com", 100.0, 10.0,
                110.0, 10.0, specificTime, items);
        assertEquals(specificTime, onlineBillDTO.getTime());
    }

    @Test
    @DisplayName("Should return correct items list from getter")
    void testGetItems() {
        List<CartItem> items = Arrays.asList(mockCartItem1, mockCartItem2, mockCartItem3);
        onlineBillDTO = new OnlineBillDTO(
                "BILL-001", "user@test.com", 100.0, 10.0,
                110.0, 10.0, testTime, items);

        List<CartItem> returnedItems = onlineBillDTO.getItems();
        assertEquals(items, returnedItems);
        assertEquals(3, returnedItems.size());
        assertTrue(returnedItems.contains(mockCartItem1));
        assertTrue(returnedItems.contains(mockCartItem2));
        assertTrue(returnedItems.contains(mockCartItem3));
    }

    // Edge case tests
    @Test
    @DisplayName("Should handle empty strings in constructor")
    void testConstructor_EmptyStrings() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);
        onlineBillDTO = new OnlineBillDTO(
                "", "", 100.0, 10.0,
                110.0, 10.0, testTime, items);

        assertEquals("", onlineBillDTO.getSerial());
        assertEquals("", onlineBillDTO.getUsername());
    }

    @Test
    @DisplayName("Should handle very long strings")
    void testConstructor_VeryLongStrings() {
        String longSerial = "BILL-" + "A".repeat(1000);
        String longUsername = "user" + "b".repeat(2000) + "@domain.com";
        List<CartItem> items = Collections.singletonList(mockCartItem1);

        onlineBillDTO = new OnlineBillDTO(
                longSerial, longUsername, 100.0, 10.0,
                110.0, 10.0, testTime, items);

        assertEquals(longSerial, onlineBillDTO.getSerial());
        assertEquals(longUsername, onlineBillDTO.getUsername());
    }

    @Test
    @DisplayName("Should handle maximum double values")
    void testConstructor_MaxDoubleValues() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);
        onlineBillDTO = new OnlineBillDTO(
                "MAX-BILL", "max@test.com", Double.MAX_VALUE, Double.MAX_VALUE,
                Double.MAX_VALUE, Double.MAX_VALUE, testTime, items);

        assertEquals(Double.MAX_VALUE, onlineBillDTO.getTotal(), 0.001);
        assertEquals(Double.MAX_VALUE, onlineBillDTO.getDiscount(), 0.001);
        assertEquals(Double.MAX_VALUE, onlineBillDTO.getCash(), 0.001);
        assertEquals(Double.MAX_VALUE, onlineBillDTO.getChange(), 0.001);
    }

    @Test
    @DisplayName("Should handle minimum double values")
    void testConstructor_MinDoubleValues() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);
        onlineBillDTO = new OnlineBillDTO(
                "MIN-BILL", "min@test.com", -Double.MAX_VALUE, -Double.MAX_VALUE,
                -Double.MAX_VALUE, -Double.MAX_VALUE, testTime, items);

        assertEquals(-Double.MAX_VALUE, onlineBillDTO.getTotal(), 0.001);
        assertEquals(-Double.MAX_VALUE, onlineBillDTO.getDiscount(), 0.001);
        assertEquals(-Double.MAX_VALUE, onlineBillDTO.getCash(), 0.001);
        assertEquals(-Double.MAX_VALUE, onlineBillDTO.getChange(), 0.001);
    }

    @Test
    @DisplayName("Should handle very small decimal values")
    void testConstructor_VerySmallDecimals() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);
        onlineBillDTO = new OnlineBillDTO(
                "SMALL-BILL", "small@test.com", 0.001, 0.001,
                0.002, 0.001, testTime, items);

        assertEquals(0.001, onlineBillDTO.getTotal(), 0.0001);
        assertEquals(0.001, onlineBillDTO.getDiscount(), 0.0001);
        assertEquals(0.002, onlineBillDTO.getCash(), 0.0001);
        assertEquals(0.001, onlineBillDTO.getChange(), 0.0001);
    }

    @Test
    @DisplayName("Should handle time edge cases")
    void testConstructor_TimeEdgeCases() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);

        // Test with minimum time
        LocalTime minTime = LocalTime.MIN;
        onlineBillDTO = new OnlineBillDTO(
                "MIN-TIME", "user@test.com", 100.0, 10.0,
                110.0, 10.0, minTime, items);
        assertEquals(minTime, onlineBillDTO.getTime());

        // Test with maximum time
        LocalTime maxTime = LocalTime.MAX;
        onlineBillDTO = new OnlineBillDTO(
                "MAX-TIME", "user@test.com", 100.0, 10.0,
                110.0, 10.0, maxTime, items);
        assertEquals(maxTime, onlineBillDTO.getTime());
    }

    @Test
    @DisplayName("Should handle large collections of cart items")
    void testConstructor_LargeCartItemCollection() {
        List<CartItem> largeItemList = new ArrayList<>();

        // Create a large list of mock cart items
        for (int i = 0; i < 1000; i++) {
            CartItem mockItem = mock(CartItem.class);
            largeItemList.add(mockItem);
        }

        onlineBillDTO = new OnlineBillDTO(
                "LARGE-BILL", "bulk.customer@test.com", 10000.0, 1000.0,
                11000.0, 1000.0, testTime, largeItemList);

        assertEquals(1000, onlineBillDTO.getItems().size());
        assertEquals(largeItemList, onlineBillDTO.getItems());
    }

    @Test
    @DisplayName("Should handle various time formats")
    void testConstructor_VariousTimeFormats() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);

        LocalTime[] timeFormats = {
                LocalTime.of(0, 0, 0), // Midnight
                LocalTime.of(12, 0, 0), // Noon
                LocalTime.of(23, 59, 59), // End of day
                LocalTime.of(1, 30, 45), // Early morning
                LocalTime.of(15, 45, 30), // Afternoon
                LocalTime.of(6, 0, 0), // Early morning
                LocalTime.of(18, 30, 0) // Evening
        };

        for (int i = 0; i < timeFormats.length; i++) {
            onlineBillDTO = new OnlineBillDTO(
                    "TIME-" + i, "user" + i + "@test.com", 100.0, 10.0,
                    110.0, 10.0, timeFormats[i], items);
            assertEquals(timeFormats[i], onlineBillDTO.getTime());
        }
    }

    @Test
    @DisplayName("Should maintain state consistency")
    void testStateConsistency() {
        List<CartItem> items = Arrays.asList(mockCartItem1, mockCartItem2);

        onlineBillDTO = new OnlineBillDTO(
                "CONSISTENCY-001", "consistent@test.com", 299.99, 29.99,
                330.0, 30.01, testTime, items);

        // Verify all values are correctly maintained
        assertEquals("CONSISTENCY-001", onlineBillDTO.getSerial());
        assertEquals("consistent@test.com", onlineBillDTO.getUsername());
        assertEquals(299.99, onlineBillDTO.getTotal(), 0.001);
        assertEquals(29.99, onlineBillDTO.getDiscount(), 0.001);
        assertEquals(330.0, onlineBillDTO.getCash(), 0.001);
        assertEquals(30.01, onlineBillDTO.getChange(), 0.001);
        assertEquals(testTime, onlineBillDTO.getTime());
        assertEquals(items, onlineBillDTO.getItems());
        assertEquals(2, onlineBillDTO.getItems().size());
    }

    @Test
    @DisplayName("Should handle null items within list")
    void testConstructor_ListWithNullItems() {
        List<CartItem> itemsWithNull = Arrays.asList(mockCartItem1, null, mockCartItem2);

        onlineBillDTO = new OnlineBillDTO(
                "NULL-ITEMS", "null.items@test.com", 100.0, 10.0,
                110.0, 10.0, testTime, itemsWithNull);

        assertEquals(itemsWithNull, onlineBillDTO.getItems());
        assertEquals(3, onlineBillDTO.getItems().size());
        assertNull(onlineBillDTO.getItems().get(1));
    }

    @Test
    @DisplayName("Should handle various username formats")
    void testConstructor_VariousUsernameFormats() {
        List<CartItem> items = Collections.singletonList(mockCartItem1);

        String[] usernameFormats = {
                "simple@email.com",
                "user.name@domain.co.uk",
                "user+tag@domain.com",
                "user123@domain123.org",
                "123456789",
                "user_name@domain-name.com",
                "firstname.lastname@subdomain.domain.com"
        };

        for (int i = 0; i < usernameFormats.length; i++) {
            onlineBillDTO = new OnlineBillDTO(
                    "BILL-" + i, usernameFormats[i], 100.0, 10.0,
                    110.0, 10.0, testTime, items);
            assertEquals(usernameFormats[i], onlineBillDTO.getUsername());
        }
    }
}
