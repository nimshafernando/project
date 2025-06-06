package syos.ui.views;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import syos.dto.ItemDTO;
import syos.model.Bill;
import syos.model.CartItem;
import syos.model.OnlineUser;
import syos.service.OnlinePOS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Comprehensive test suite for OnlineCheckoutUI
 * Tests all methods with 100% coverage including happy paths and edge cases
 */
@DisplayName("OnlineCheckoutUI Tests")
class OnlineCheckoutUITest {

    @Mock
    private OnlinePOS mockPos;

    @Mock
    private OnlineUser mockUser;

    private OnlineCheckoutUI checkoutUI;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        mocks.close();
    }

    private Scanner createScanner(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    private String getOutput() {
        return outputStream.toString();
    }

    private CartItem createMockCartItem(String code, String name, double price, int quantity) {
        ItemDTO mockItem = new ItemDTO(code, name, price, 100); // Available stock

        CartItem cartItem = new CartItem(mockItem, quantity);
        return cartItem;
    }

    private Bill createMockBill() {
        return mock(Bill.class);
    }

    @Test
    @DisplayName("Should create OnlineCheckoutUI with valid parameters")
    void shouldCreateOnlineCheckoutUIWithValidParameters() {
        // Arrange
        Scanner scanner = createScanner("1\n");

        // Act
        OnlineCheckoutUI ui = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        // Assert
        assertNotNull(ui);
    }

    @Test
    @DisplayName("Should handle empty cart gracefully")
    void shouldHandleEmptyCartGracefully() {
        // Arrange
        Scanner scanner = createScanner("\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        when(mockPos.getCartItems()).thenReturn(Collections.emptyList());

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Your cart is empty"));
        verify(mockPos).getCartItems();
    }

    @Test
    @DisplayName("Should handle null cart gracefully")
    void shouldHandleNullCartGracefully() {
        // Arrange
        Scanner scanner = createScanner("\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        when(mockPos.getCartItems()).thenReturn(null);

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Your cart is empty"));
        verify(mockPos).getCartItems();
    }

    @Test
    @DisplayName("Should successfully process checkout with pay in store")
    void shouldSuccessfullyProcessCheckoutWithPayInStore() {
        // Arrange
        Scanner scanner = createScanner("1\n\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        List<CartItem> cartItems = Arrays.asList(
                createMockCartItem("FOOD001", "Organic Apples", 125.99, 3),
                createMockCartItem("ELEC002", "Wireless Mouse", 845.50, 1));

        Bill mockBill = createMockBill();
        when(mockPos.getCartItems()).thenReturn(cartItems);
        when(mockPos.checkout(eq("Pay in Store"), eq(mockUser))).thenReturn(mockBill);

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("FOOD001"));
        assertTrue(output.contains("Organic Apples"));
        assertTrue(output.contains("125.99"));
        assertTrue(output.contains("ELEC002"));
        assertTrue(output.contains("Wireless Mouse"));
        assertTrue(output.contains("845.50"));
        assertTrue(output.contains("Select Payment Method"));
        assertTrue(output.contains("Pay in Store"));
        assertTrue(output.contains("Checkout successful"));

        verify(mockPos).getCartItems();
        verify(mockPos).checkout("Pay in Store", mockUser);
    }

    @Test
    @DisplayName("Should successfully process checkout with cash on delivery")
    void shouldSuccessfullyProcessCheckoutWithCashOnDelivery() {
        // Arrange
        Scanner scanner = createScanner("2\n\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        List<CartItem> cartItems = Arrays.asList(
                createMockCartItem("BOOK001", "Java Programming", 599.99, 2));

        Bill mockBill = createMockBill();
        when(mockPos.getCartItems()).thenReturn(cartItems);
        when(mockPos.checkout(eq("Cash on Delivery"), eq(mockUser))).thenReturn(mockBill);

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("BOOK001"));
        assertTrue(output.contains("Java Programming"));
        assertTrue(output.contains("599.99"));
        assertTrue(output.contains("Cash on Delivery"));
        assertTrue(output.contains("Checkout successful"));

        verify(mockPos).getCartItems();
        verify(mockPos).checkout("Cash on Delivery", mockUser);
    }

    @ParameterizedTest
    @ValueSource(strings = { "3", "0", "invalid", "", "abc", "-1" })
    @DisplayName("Should handle invalid payment method choices")
    void shouldHandleInvalidPaymentMethodChoices(String invalidChoice) {
        // Arrange
        Scanner scanner = createScanner(invalidChoice + "\n\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        List<CartItem> cartItems = Arrays.asList(
                createMockCartItem("TEST001", "Test Item", 100.0, 1));

        when(mockPos.getCartItems()).thenReturn(cartItems);

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Invalid choice"));
        verify(mockPos).getCartItems();
        verify(mockPos, never()).checkout(anyString(), any(OnlineUser.class));
    }

    @Test
    @DisplayName("Should handle cart items with null items")
    void shouldHandleCartItemsWithNullItems() {
        // Arrange
        Scanner scanner = createScanner("1\n\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        CartItem nullItemCart = new CartItem(null, 1);
        List<CartItem> cartItems = Arrays.asList(nullItemCart);

        when(mockPos.getCartItems()).thenReturn(cartItems);

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Select Payment Method"));
        verify(mockPos).getCartItems();
    }

    @Test
    @DisplayName("Should handle checkout service exception")
    void shouldHandleCheckoutServiceException() {
        // Arrange
        Scanner scanner = createScanner("1\n\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        List<CartItem> cartItems = Arrays.asList(
                createMockCartItem("ERROR001", "Error Item", 50.0, 1));

        when(mockPos.getCartItems()).thenReturn(cartItems);
        when(mockPos.checkout(anyString(), any(OnlineUser.class)))
                .thenThrow(new RuntimeException("Checkout failed"));

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Checkout failed"));
        verify(mockPos).getCartItems();
        verify(mockPos).checkout("Pay in Store", mockUser);
    }

    @Test
    @DisplayName("Should handle exception during cart retrieval")
    void shouldHandleExceptionDuringCartRetrieval() {
        // Arrange
        Scanner scanner = createScanner("\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        when(mockPos.getCartItems()).thenThrow(new RuntimeException("Database error"));

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("An error occurred during checkout"));
        verify(mockPos).getCartItems();
    }

    @Test
    @DisplayName("Should calculate and display correct totals")
    void shouldCalculateAndDisplayCorrectTotals() {
        // Arrange
        Scanner scanner = createScanner("1\n\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        List<CartItem> cartItems = Arrays.asList(
                createMockCartItem("ITEM001", "Item One", 10.50, 2), // 21.00
                createMockCartItem("ITEM002", "Item Two", 25.75, 3), // 77.25
                createMockCartItem("ITEM003", "Item Three", 5.25, 4) // 21.00
        );

        Bill mockBill = createMockBill();
        when(mockPos.getCartItems()).thenReturn(cartItems);
        when(mockPos.checkout(anyString(), any(OnlineUser.class))).thenReturn(mockBill);

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("21.00")); // First item subtotal
        assertTrue(output.contains("77.25")); // Second item subtotal
        assertTrue(output.contains("21.00")); // Third item subtotal
        assertTrue(output.contains("119.25")); // Total: 21.00 + 77.25 + 21.00

        verify(mockPos).getCartItems();
    }

    @Test
    @DisplayName("Should handle scanner input exception during payment selection")
    void shouldHandleInputExceptionDuringPaymentSelection() {
        // Arrange
        Scanner scanner = mock(Scanner.class);
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        List<CartItem> cartItems = Arrays.asList(
                createMockCartItem("TEST001", "Test Item", 100.0, 1));

        when(mockPos.getCartItems()).thenReturn(cartItems);
        when(scanner.nextLine()).thenThrow(new RuntimeException("Scanner error"));

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Error reading input"));
        verify(mockPos).getCartItems();
        verify(mockPos, never()).checkout(anyString(), any(OnlineUser.class));
    }

    @Test
    @DisplayName("Should display cart with multiple items correctly")
    void shouldDisplayCartWithMultipleItemsCorrectly() {
        // Arrange
        Scanner scanner = createScanner("1\n\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        List<CartItem> cartItems = Arrays.asList(
                createMockCartItem("FOOD001", "Premium Organic Apples From Local Farm", 125.99, 3),
                createMockCartItem("ELEC002", "Wireless Bluetooth Gaming Mouse", 845.50, 1));

        Bill mockBill = createMockBill();
        when(mockPos.getCartItems()).thenReturn(cartItems);
        when(mockPos.checkout(anyString(), any(OnlineUser.class))).thenReturn(mockBill);

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("FOOD001"));
        assertTrue(output.contains("Premium Organic Apples From Local"));
        assertTrue(output.contains("125.99"));
        assertTrue(output.contains("3"));
        assertTrue(output.contains("377.97")); // 125.99 * 3

        assertTrue(output.contains("ELEC002"));
        assertTrue(output.contains("Wireless Bluetooth Gaming Mouse"));
        assertTrue(output.contains("845.50"));
        assertTrue(output.contains("1"));
        assertTrue(output.contains("845.50")); // 845.50 * 1

        assertTrue(output.contains("1223.47")); // Total
    }

    @Test
    @DisplayName("Should display payment method options correctly")
    void shouldDisplayPaymentMethodOptionsCorrectly() {
        // Arrange
        Scanner scanner = createScanner("1\n\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        List<CartItem> cartItems = Arrays.asList(
                createMockCartItem("TEST001", "Test Item", 100.0, 1));

        Bill mockBill = createMockBill();
        when(mockPos.getCartItems()).thenReturn(cartItems);
        when(mockPos.checkout(anyString(), any(OnlineUser.class))).thenReturn(mockBill);

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Select Payment Method:"));
        assertTrue(output.contains("1. Pay in Store"));
        assertTrue(output.contains("2. Cash on Delivery"));
        assertTrue(output.contains("Enter choice:"));
    }

    @Test
    @DisplayName("Should handle empty input for payment method")
    void shouldHandleEmptyInputForPaymentMethod() {
        // Arrange
        Scanner scanner = createScanner("\n\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        List<CartItem> cartItems = Arrays.asList(
                createMockCartItem("TEST001", "Test Item", 100.0, 1));

        when(mockPos.getCartItems()).thenReturn(cartItems);

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("Invalid choice: ''"));
        verify(mockPos, never()).checkout(anyString(), any(OnlineUser.class));
    }

    @Test
    @DisplayName("Should handle cart with zero price items")
    void shouldHandleCartWithZeroPriceItems() {
        // Arrange
        Scanner scanner = createScanner("1\n\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        List<CartItem> cartItems = Arrays.asList(
                createMockCartItem("FREE001", "Free Sample", 0.0, 5));

        Bill mockBill = createMockBill();
        when(mockPos.getCartItems()).thenReturn(cartItems);
        when(mockPos.checkout(anyString(), any(OnlineUser.class))).thenReturn(mockBill);

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("FREE001"));
        assertTrue(output.contains("Free Sample"));
        assertTrue(output.contains("0.00"));
        assertTrue(output.contains("5"));
        assertTrue(output.contains("TOTAL:"));

        verify(mockPos).checkout("Pay in Store", mockUser);
    }

    @Test
    @DisplayName("Should handle cart with very large quantities")
    void shouldHandleCartWithVeryLargeQuantities() {
        // Arrange
        Scanner scanner = createScanner("2\n\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        List<CartItem> cartItems = Arrays.asList(
                createMockCartItem("BULK001", "Bulk Item", 1.99, 9999));

        Bill mockBill = createMockBill();
        when(mockPos.getCartItems()).thenReturn(cartItems);
        when(mockPos.checkout(anyString(), any(OnlineUser.class))).thenReturn(mockBill);

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("BULK001"));
        assertTrue(output.contains("Bulk Item"));
        assertTrue(output.contains("1.99"));
        assertTrue(output.contains("9999"));

        verify(mockPos).checkout("Cash on Delivery", mockUser);
    }

    @Test
    @DisplayName("Should handle cart with very long item names")
    void shouldHandleCartWithVeryLongItemNames() {
        // Arrange
        Scanner scanner = createScanner("1\n\n");
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        String longName = "This is an extremely long product name that exceeds normal display limits and should be handled gracefully by the UI system";
        List<CartItem> cartItems = Arrays.asList(
                createMockCartItem("LONG001", longName, 299.99, 1));

        Bill mockBill = createMockBill();
        when(mockPos.getCartItems()).thenReturn(cartItems);
        when(mockPos.checkout(anyString(), any(OnlineUser.class))).thenReturn(mockBill);

        // Act
        checkoutUI.start();

        // Assert
        String output = getOutput();
        assertTrue(output.contains("LONG001"));
        assertTrue(output.contains("299.99"));

        verify(mockPos).checkout("Pay in Store", mockUser);
    }

    @Test
    @DisplayName("Should handle pause method exception gracefully")
    void shouldHandlePauseMethodExceptionGracefully() {
        // Arrange
        Scanner scanner = mock(Scanner.class);
        checkoutUI = new OnlineCheckoutUI(scanner, mockPos, mockUser);

        when(mockPos.getCartItems()).thenReturn(Collections.emptyList());
        when(scanner.nextLine()).thenThrow(new RuntimeException("Scanner closed"));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> checkoutUI.start());

        verify(mockPos).getCartItems();
    }
}