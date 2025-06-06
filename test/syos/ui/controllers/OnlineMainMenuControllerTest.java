package syos.ui.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import syos.data.OnlineBillGateway;
import syos.data.OnlineItemGateway;
import syos.data.OnlineUserGateway;
import syos.dto.ItemDTO;
import syos.model.Bill;
import syos.model.CartItem;
import syos.model.OnlineUser;
import syos.service.OnlinePOS;
import syos.ui.views.OnlineCheckoutUI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for OnlineMainMenuController
 * Tests all methods with 100% coverage including happy paths and edge cases
 * Covers user authentication, registration, shopping flow, and bill viewing
 */
@DisplayName("OnlineMainMenuController Tests")
class OnlineMainMenuControllerTest {

    @Mock
    private OnlineUserGateway mockUserGateway;

    @Mock
    private OnlineItemGateway mockItemGateway;

    @Mock
    private OnlineBillGateway mockBillGateway;

    @Mock
    private OnlinePOS mockOnlinePOS;

    @Mock
    private OnlineCheckoutUI mockCheckoutUI;

    private OnlineUser testUser;
    private ItemDTO testItem1;
    private ItemDTO testItem2;
    private CartItem testCartItem1;
    private CartItem testCartItem2;
    private Bill testBill;

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        MockitoAnnotations.openMocks(this);
        setupTestData();
        captureSystemOut();
        // Mock ProcessBuilder for clearScreen
        System.setProperty("os.name", "Windows");
        try (MockedConstruction<ProcessBuilder> processBuilderMock = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    when(mock.inheritIO().start().waitFor()).thenReturn(0);
                })) {
            // No action needed, just ensure ProcessBuilder is mocked
        }
    }

    @AfterEach
    void tearDown() {
        restoreSystemOut();
        System.clearProperty("os.name");
    }

    private void setupTestData() {
        // Create test user
        testUser = new OnlineUser("testuser123", "password123", "0712345678", "123 Test Street, Colombo");

        // Create test items
        testItem1 = new ItemDTO("PHONE001", "Samsung Galaxy S24", 175000.00, 15);
        testItem2 = new ItemDTO("LAPTOP001", "MacBook Pro M3", 425000.00, 8);

        // Create test cart items
        testCartItem1 = new CartItem(testItem1, 2);
        testCartItem2 = new CartItem(testItem2, 1);

        // Create test bill
        testBill = new Bill();
        testBill.setId(101);
        testBill.setDate(LocalDateTime.of(2025, 6, 5, 14, 30, 0));
        testBill.setTotal(775000.00);
        testBill.setPaymentMethod("Credit Card");
        testBill.setItems(Arrays.asList(testCartItem1, testCartItem2));
    }

    private void captureSystemOut() {
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    private void restoreSystemOut() {
        System.setOut(originalOut);
    }

    private String getOutput() {
        return outputStream.toString();
    }

    private Scanner createScanner(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    private void invokePrivateMethod(String methodName, Class<?>[] paramTypes, Object[] args) {
        try {
            Method method = OnlineMainMenuController.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            method.invoke(null, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            fail("Method " + methodName + " threw exception: " + (cause != null ? cause.getMessage() : "null"), cause);
        } catch (Exception e) {
            fail("Failed to invoke method " + methodName + ": " + e.getMessage(), e);
        }
    }

    // ============ Main Launch Method Tests ============

    @Test
    @DisplayName("Should display main menu and exit with option 0")
    void testLaunch_ExitWithOption0() {
        Scanner scanner = createScanner("0\n\n"); // Extra \n for pause

        try (MockedConstruction<OnlineUserGateway> userGatewayMock = mockConstruction(OnlineUserGateway.class);
                MockedConstruction<OnlinePOS> onlinePOSMock = mockConstruction(OnlinePOS.class)) {

            OnlineMainMenuController.launch(scanner);

            String output = getOutput();
            assertTrue(output.contains("SYOS ONLINE STORE PORTAL"));
            assertTrue(output.contains("1. Login"));
            assertTrue(output.contains("2. Register New User"));
            assertTrue(output.contains("0. Return to Main Menu"));
            assertTrue(output.contains("Returning to main menu..."));
            assertTrue(output.contains("Press Enter to continue..."));
        }
    }

    @Test
    @DisplayName("Should handle successful login")
    void testLaunch_SuccessfulLogin() {
        Scanner scanner = createScanner("1\ntestuser123\npassword123\n0\n\n0\n\n"); // Extra \n for pauses

        try (MockedConstruction<OnlineUserGateway> userGatewayMock = mockConstruction(OnlineUserGateway.class,
                (mock, context) -> {
                    when(mock.authenticateUser("testuser123", "password123")).thenReturn(true);
                    when(mock.getUserByUsername("testuser123")).thenReturn(testUser);
                });
                MockedConstruction<OnlinePOS> onlinePOSMock = mockConstruction(OnlinePOS.class);
                MockedConstruction<OnlineItemGateway> itemGatewayMock = mockConstruction(OnlineItemGateway.class)) {

            OnlineMainMenuController.launch(scanner);

            String output = getOutput();
            assertTrue(output.contains("=== LOGIN ==="));
            assertTrue(output.contains("WELCOME TESTUSER123"));
            assertTrue(output.contains("Logging out..."));
            assertTrue(output.contains("Press Enter to continue..."));
        }
    }

    @Test
    @DisplayName("Should handle failed login")
    void testLaunch_FailedLogin() {
        Scanner scanner = createScanner("1\nwronguser\nwrongpass\n\n0\n\n"); // Extra \n for pause

        try (MockedConstruction<OnlineUserGateway> userGatewayMock = mockConstruction(OnlineUserGateway.class,
                (mock, context) -> {
                    when(mock.authenticateUser("wronguser", "wrongpass")).thenReturn(false);
                });
                MockedConstruction<OnlinePOS> onlinePOSMock = mockConstruction(OnlinePOS.class)) {

            OnlineMainMenuController.launch(scanner);

            String output = getOutput();
            assertTrue(output.contains("=== LOGIN ==="));
            assertTrue(output.contains("Login failed. Invalid credentials."));
            assertTrue(output.contains("Press Enter to continue..."));
        }
    }

    @Test
    @DisplayName("Should handle successful user registration")
    void testLaunch_SuccessfulRegistration() {
        Scanner scanner = createScanner("2\nnewuser\nnewpass\n0712345678\n123 New Street\n\n0\n\n"); // Extra \n for
                                                                                                     // pause

        try (MockedConstruction<OnlineUserGateway> userGatewayMock = mockConstruction(OnlineUserGateway.class,
                (mock, context) -> {
                    when(mock.registerUser(any(OnlineUser.class))).thenReturn(true);
                });
                MockedConstruction<OnlinePOS> onlinePOSMock = mockConstruction(OnlinePOS.class)) {

            OnlineMainMenuController.launch(scanner);

            String output = getOutput();
            assertTrue(output.contains("=== REGISTER NEW USER ==="));
            assertTrue(output.contains("Registration successful. You can now login."));
            assertTrue(output.contains("Press Enter to continue..."));
        }
    }

    @Test
    @DisplayName("Should handle failed user registration - username taken")
    void testLaunch_FailedRegistration_UsernameTaken() {
        Scanner scanner = createScanner("2\nexistinguser\npassword\n0712345678\n123 Street\n\n0\n\n"); // Extra \n for
                                                                                                       // pause

        try (MockedConstruction<OnlineUserGateway> userGatewayMock = mockConstruction(OnlineUserGateway.class,
                (mock, context) -> {
                    when(mock.registerUser(any(OnlineUser.class))).thenReturn(false);
                });
                MockedConstruction<OnlinePOS> onlinePOSMock = mockConstruction(OnlinePOS.class)) {

            OnlineMainMenuController.launch(scanner);

            String output = getOutput();
            assertTrue(output.contains("=== REGISTER NEW USER ==="));
            assertTrue(output.contains("Username already taken. Please try again."));
            assertTrue(output.contains("Press Enter to continue..."));
        }
    }

    @Test
    @DisplayName("Should handle invalid main menu option")
    void testLaunch_InvalidMainMenuOption() {
        Scanner scanner = createScanner("3\n\n0\n\n"); // Extra \n for pause

        try (MockedConstruction<OnlineUserGateway> userGatewayMock = mockConstruction(OnlineUserGateway.class);
                MockedConstruction<OnlinePOS> onlinePOSMock = mockConstruction(OnlinePOS.class)) {

            OnlineMainMenuController.launch(scanner);

            String output = getOutput();
            assertTrue(output.contains("Invalid option. Please try again."));
            assertTrue(output.contains("Press Enter to continue..."));
        }
    }

    // ============ Online Store Menu Tests ============

    @Test
    @DisplayName("Should display online store menu and logout")
    void testLaunchOnlineStore_Logout() {
        Scanner scanner = createScanner("0\n\n"); // Extra \n for pause

        try (MockedConstruction<OnlineItemGateway> itemGatewayMock = mockConstruction(OnlineItemGateway.class)) {
            invokePrivateMethod("launchOnlineStore",
                    new Class[] { Scanner.class, OnlinePOS.class, OnlineUser.class },
                    new Object[] { scanner, mockOnlinePOS, testUser });

            String output = getOutput();
            assertTrue(output.contains("WELCOME TESTUSER123"));
            assertTrue(output.contains("1. View All Items"));
            assertTrue(output.contains("2. View Cart"));
            assertTrue(output.contains("3. Checkout"));
            assertTrue(output.contains("4. View Past Bills"));
            assertTrue(output.contains("0. Logout"));
            assertTrue(output.contains("Logging out..."));
            assertTrue(output.contains("Press Enter to continue..."));
        }
    }

    @Test
    @DisplayName("Should handle view cart option")
    void testLaunchOnlineStore_ViewCart() {
        Scanner scanner = createScanner("2\n\n0\n\n"); // Extra \n for pause

        when(mockOnlinePOS.getCartItems()).thenReturn(Arrays.asList(testCartItem1, testCartItem2));

        try (MockedConstruction<OnlineItemGateway> itemGatewayMock = mockConstruction(OnlineItemGateway.class)) {
            invokePrivateMethod("launchOnlineStore",
                    new Class[] { Scanner.class, OnlinePOS.class, OnlineUser.class },
                    new Object[] { scanner, mockOnlinePOS, testUser });

            String output = getOutput();
            assertTrue(output.contains("Code"));
            assertTrue(output.contains("Name"));
            assertTrue(output.contains("Qty"));
            assertTrue(output.contains("Subtotal"));
            assertTrue(output.contains("PHONE001"));
            assertTrue(output.contains("LAPTOP001"));
            assertTrue(output.contains("Press Enter to continue..."));
        }
    }

    @Test
    @DisplayName("Should handle checkout option")
    void testLaunchOnlineStore_Checkout() {
        Scanner scanner = createScanner("3\n0\n\n"); // Extra \n for pause

        try (MockedConstruction<OnlineItemGateway> itemGatewayMock = mockConstruction(OnlineItemGateway.class);
                MockedConstruction<OnlineCheckoutUI> checkoutUIMock = mockConstruction(OnlineCheckoutUI.class,
                        (mock, context) -> {
                            doNothing().when(mock).start();
                        })) {
            invokePrivateMethod("launchOnlineStore",
                    new Class[] { Scanner.class, OnlinePOS.class, OnlineUser.class },
                    new Object[] { scanner, mockOnlinePOS, testUser });

            String output = getOutput();
            assertTrue(output.contains("WELCOME TESTUSER123"));
            assertTrue(output.contains("Logging out..."));
            assertEquals(1, checkoutUIMock.constructed().size());
            verify(checkoutUIMock.constructed().get(0)).start();
        }
    }

    @Test
    @DisplayName("Should handle view past bills option")
    void testLaunchOnlineStore_ViewPastBills() {
        Scanner scanner = createScanner("4\n0\n\n0\n\n"); // Extra \n for pauses

        try (MockedConstruction<OnlineItemGateway> itemGatewayMock = mockConstruction(OnlineItemGateway.class);
                MockedConstruction<OnlineBillGateway> billGatewayMock = mockConstruction(OnlineBillGateway.class,
                        (mock, context) -> {
                            when(mock.getBillsByUsername("testuser123")).thenReturn(Arrays.asList(testBill));
                        })) {
            invokePrivateMethod("launchOnlineStore",
                    new Class[] { Scanner.class, OnlinePOS.class, OnlineUser.class },
                    new Object[] { scanner, mockOnlinePOS, testUser });

            String output = getOutput();
            assertTrue(output.contains("YOUR PAST BILLS"));
            assertTrue(output.contains("ID"));
            assertTrue(output.contains("Date"));
            assertTrue(output.contains("Payment"));
            assertTrue(output.contains("Total (Rs.)"));
            assertTrue(output.contains("Press Enter to continue..."));
        }
    }

    @Test
    @DisplayName("Should handle invalid store menu option")
    void testLaunchOnlineStore_InvalidOption() {
        Scanner scanner = createScanner("5\n\n0\n\n"); // Extra \n for pause

        try (MockedConstruction<OnlineItemGateway> itemGatewayMock = mockConstruction(OnlineItemGateway.class)) {
            invokePrivateMethod("launchOnlineStore",
                    new Class[] { Scanner.class, OnlinePOS.class, OnlineUser.class },
                    new Object[] { scanner, mockOnlinePOS, testUser });

            String output = getOutput();
            assertTrue(output.contains("Invalid option. Please try again."));
            assertTrue(output.contains("Press Enter to continue..."));
        }
    }

    // ============ Show Main Menu Tests ============

    @Test
    @DisplayName("Should display showMainMenu and exit with logout")
    void testShowMainMenu_Logout() {
        Scanner scanner = createScanner("0\n"); // No pause after logout

        try (MockedConstruction<OnlineItemGateway> itemGatewayMock = mockConstruction(OnlineItemGateway.class);
                MockedConstruction<OnlinePOS> onlinePOSMock = mockConstruction(OnlinePOS.class)) {
            OnlineMainMenuController.showMainMenu(scanner, testUser);

            String output = getOutput();
            assertTrue(output.contains("SYOS ONLINE STORE"));
            assertTrue(output.contains("Welcome, testuser123!"));
            assertTrue(output.contains("5. View Past Bills"));
            assertTrue(output.contains("Goodbye, testuser123!"));
        }
    }

    @Test
    @DisplayName("Should handle clear cart option in showMainMenu")
    void testShowMainMenu_ClearCart() {
        Scanner scanner = createScanner("4\n\n0\n"); // Extra \n for pause

        try (MockedConstruction<OnlineItemGateway> itemGatewayMock = mockConstruction(OnlineItemGateway.class);
                MockedConstruction<OnlinePOS> onlinePOSMock = mockConstruction(OnlinePOS.class,
                        (mock, context) -> {
                            doNothing().when(mock).clearCart();
                        })) {
            OnlineMainMenuController.showMainMenu(scanner, testUser);

            String output = getOutput();
            assertTrue(output.contains("Cart cleared!"));
            assertTrue(output.contains("Press Enter to continue..."));
            verify(onlinePOSMock.constructed().get(0)).clearCart();
        }
    }

    // ============ View All Items Tests ============

    @Test
    @DisplayName("Should display no items message when inventory is empty")
    void testViewAllItems_EmptyInventory() {
        Scanner scanner = createScanner("\n"); // \n for pause

        when(mockItemGateway.getAllItems()).thenReturn(Collections.emptyList());

        invokePrivateMethod("viewAllItems",
                new Class[] { OnlineItemGateway.class, OnlinePOS.class, Scanner.class },
                new Object[] { mockItemGateway, mockOnlinePOS, scanner });

        String output = getOutput();
        assertTrue(output.contains("No items found in online inventory."));
        assertTrue(output.contains("Press Enter to continue..."));
    }

    @Test
    @DisplayName("Should display items and handle back to menu option")
    void testViewAllItems_BackToMenu() {
        Scanner scanner = createScanner("0\n"); // No pause needed for return

        when(mockItemGateway.getAllItems()).thenReturn(Arrays.asList(testItem1, testItem2));

        invokePrivateMethod("viewAllItems",
                new Class[] { OnlineItemGateway.class, OnlinePOS.class, Scanner.class },
                new Object[] { mockItemGateway, mockOnlinePOS, scanner });

        String output = getOutput();
        assertTrue(output.contains("PHONE001"));
        assertTrue(output.contains("Samsung Galaxy S24"));
        assertTrue(output.contains("LAPTOP001"));
        assertTrue(output.contains("MacBook Pro M3"));
    }

    @Test
    @DisplayName("Should handle add item to cart option")
    void testViewAllItems_AddItemToCart() {
        Scanner scanner = createScanner("1\nPHONE001\n2\n\n0\n"); // Extra \n for pause

        when(mockItemGateway.getAllItems()).thenReturn(Arrays.asList(testItem1, testItem2));
        when(mockItemGateway.getItemByCode("PHONE001")).thenReturn(testItem1);
        when(mockOnlinePOS.getCartTotal()).thenReturn(350000.00);

        invokePrivateMethod("viewAllItems",
                new Class[] { OnlineItemGateway.class, OnlinePOS.class, Scanner.class },
                new Object[] { mockItemGateway, mockOnlinePOS, scanner });

        String output = getOutput();
        assertTrue(output.contains("Added 2 x Samsung Galaxy S24 to cart"));
        assertTrue(output.contains("Subtotal: Rs. 350000.00"));
        assertTrue(output.contains("Cart total: Rs. 350000.00"));
        assertTrue(output.contains("Press Enter to continue..."));
        verify(mockOnlinePOS).addToCart(testItem1, 2);
    }

    @Test
    @DisplayName("Should handle invalid option in view all items")
    void testViewAllItems_InvalidOption() {
        Scanner scanner = createScanner("9\n\n0\n"); // Extra \n for pause

        when(mockItemGateway.getAllItems()).thenReturn(Arrays.asList(testItem1));

        invokePrivateMethod("viewAllItems",
                new Class[] { OnlineItemGateway.class, OnlinePOS.class, Scanner.class },
                new Object[] { mockItemGateway, mockOnlinePOS, scanner });

        String output = getOutput();
        assertTrue(output.contains("Invalid option. Please try again."));
        assertTrue(output.contains("Press Enter to continue..."));
    }

    // ============ Add Item to Cart Tests ============

    @Test
    @DisplayName("Should handle item not found when adding to cart")
    void testAddItemToCart_ItemNotFound() {
        Scanner scanner = createScanner("NOTFOUND\n\n"); // Extra \n for pause

        when(mockItemGateway.getItemByCode("NOTFOUND")).thenReturn(null);

        invokePrivateMethod("addItemToCartFromView",
                new Class[] { Scanner.class, OnlineItemGateway.class, OnlinePOS.class },
                new Object[] { scanner, mockItemGateway, mockOnlinePOS });

        String output = getOutput();
        assertTrue(output.contains("Item not found."));
        assertTrue(output.contains("Press Enter to continue..."));
        verify(mockItemGateway).getItemByCode("NOTFOUND");
        verify(mockOnlinePOS, never()).addToCart(any(), anyInt());
    }

    @Test
    @DisplayName("Should handle out of stock item when adding to cart")
    void testAddItemToCart_OutOfStock() {
        Scanner scanner = createScanner("OUT001\n\n"); // Extra \n for pause

        ItemDTO outOfStockItem = new ItemDTO("OUT001", "Out of Stock Item", 100.0, 0);
        when(mockItemGateway.getItemByCode("OUT001")).thenReturn(outOfStockItem);

        invokePrivateMethod("addItemToCartFromView",
                new Class[] { Scanner.class, OnlineItemGateway.class, OnlinePOS.class },
                new Object[] { scanner, mockItemGateway, mockOnlinePOS });

        String output = getOutput();
        assertTrue(output.contains("Item is out of stock."));
        assertTrue(output.contains("Press Enter to continue..."));
        verify(mockItemGateway).getItemByCode("OUT001");
        verify(mockOnlinePOS, never()).addToCart(any(), anyInt());
    }

    @Test
    @DisplayName("Should handle invalid quantity when adding to cart")
    void testAddItemToCart_InvalidQuantity() {
        Scanner scanner = createScanner("PHONE001\n-1\n\n"); // Extra \n for pause

        when(mockItemGateway.getItemByCode("PHONE001")).thenReturn(testItem1);

        invokePrivateMethod("addItemToCartFromView",
                new Class[] { Scanner.class, OnlineItemGateway.class, OnlinePOS.class },
                new Object[] { scanner, mockItemGateway, mockOnlinePOS });

        String output = getOutput();
        assertTrue(output.contains("Quantity must be positive."));
        assertTrue(output.contains("Press Enter to continue..."));
        verify(mockItemGateway).getItemByCode("PHONE001");
        verify(mockOnlinePOS, never()).addToCart(any(), anyInt());
    }

    @Test
    @DisplayName("Should handle insufficient stock when adding to cart")
    void testAddItemToCart_InsufficientStock() {
        Scanner scanner = createScanner("PHONE001\n20\n\n"); // Extra \n for pause

        when(mockItemGateway.getItemByCode("PHONE001")).thenReturn(testItem1);

        invokePrivateMethod("addItemToCartFromView",
                new Class[] { Scanner.class, OnlineItemGateway.class, OnlinePOS.class },
                new Object[] { scanner, mockItemGateway, mockOnlinePOS });

        String output = getOutput();
        assertTrue(output.contains("Insufficient stock. Available: 15"));
        assertTrue(output.contains("Press Enter to continue..."));
        verify(mockItemGateway).getItemByCode("PHONE001");
        verify(mockOnlinePOS, never()).addToCart(any(), anyInt());
    }

    @Test
    @DisplayName("Should handle non-numeric quantity input")
    void testAddItemToCart_NonNumericQuantity() {
        Scanner scanner = createScanner("PHONE001\nabc\n\n"); // Extra \n for pause

        when(mockItemGateway.getItemByCode("PHONE001")).thenReturn(testItem1);

        invokePrivateMethod("addItemToCartFromView",
                new Class[] { Scanner.class, OnlineItemGateway.class, OnlinePOS.class },
                new Object[] { scanner, mockItemGateway, mockOnlinePOS });

        String output = getOutput();
        assertTrue(output.contains("Invalid quantity format."));
        assertTrue(output.contains("Press Enter to continue..."));
        verify(mockItemGateway).getItemByCode("PHONE001");
        verify(mockOnlinePOS, never()).addToCart(any(), anyInt());
    }

    // ============ View Cart Tests ============

    @Test
    @DisplayName("Should display empty cart message")
    void testViewCart_EmptyCart() {
        when(mockOnlinePOS.getCartItems()).thenReturn(Collections.emptyList());

        invokePrivateMethod("viewCart",
                new Class[] { OnlinePOS.class },
                new Object[] { mockOnlinePOS });

        String output = getOutput();
        assertTrue(output.contains("Your cart is empty."));
        verify(mockOnlinePOS).getCartItems();
    }

    @Test
    @DisplayName("Should display cart items with totals")
    void testViewCart_WithItems() {
        when(mockOnlinePOS.getCartItems()).thenReturn(Arrays.asList(testCartItem1, testCartItem2));

        invokePrivateMethod("viewCart",
                new Class[] { OnlinePOS.class },
                new Object[] { mockOnlinePOS });

        String output = getOutput();
        assertTrue(output.contains("Code"));
        assertTrue(output.contains("Name"));
        assertTrue(output.contains("Qty"));
        assertTrue(output.contains("Subtotal"));
        assertTrue(output.contains("PHONE001"));
        assertTrue(output.contains("LAPTOP001"));
        assertTrue(output.contains("TOTAL:"));
        verify(mockOnlinePOS).getCartItems();
    }

    // ============ View Past Bills Tests ============

    @Test
    @DisplayName("Should display no bills message when user has no past bills")
    void testViewPastBills_NoBills() {
        Scanner scanner = createScanner(""); // No input needed for empty bills

        try (MockedConstruction<OnlineBillGateway> billGatewayMock = mockConstruction(OnlineBillGateway.class,
                (mock, context) -> {
                    when(mock.getBillsByUsername("testuser123")).thenReturn(Collections.emptyList());
                })) {
            invokePrivateMethod("viewPastBills",
                    new Class[] { OnlineUser.class, Scanner.class },
                    new Object[] { testUser, scanner });

            String output = getOutput();
            assertTrue(output.contains("You have no past bills."));
        }
    }

    @Test
    @DisplayName("Should display past bills")
    void testViewPastBills() {
        // Simulate selecting bill ID 999 and pressing Enter to continue
        Scanner scanner = createScanner("999\n\n");

        // Prepare a sample bill with real-looking values
        Bill sampleBill = new Bill();
        sampleBill.setId(999);
        sampleBill.setDate(LocalDate.of(2025, 6, 1).atStartOfDay());
        sampleBill.setPaymentMethod("Pay in Store"); // Will be displayed as PIS
        sampleBill.setUsername("nimsha123");
        sampleBill.setSource("online");
        sampleBill.setTotal(1200.0);

        try (MockedConstruction<OnlineBillGateway> mocked = mockConstruction(OnlineBillGateway.class,
                (mock, context) -> {
                    when(mock.getBillsByUsername("testuser123")).thenReturn(List.of(sampleBill));
                    when(mock.getItemsForBill(999)).thenReturn(List.of()); // No items for simplicity
                })) {

            invokePrivateMethod("viewPastBills",
                    new Class[] { OnlineUser.class, Scanner.class },
                    new Object[] { testUser, scanner });

            String output = getOutput();

            // Validations (match actual output, case-sensitive and formatting-aware)
            assertTrue(output.contains("YOUR PAST BILLS"), "Should show past bills title");
            assertTrue(output.contains("999"), "Should include bill ID");
            assertTrue(output.contains("PIS"), "Should show 'Pay in Store' as PIS");
            assertTrue(output.contains("BILL DETAILS"), "Should show bill details section");
            assertTrue(output.contains("Press Enter to continue..."), "Should prompt to continue");
        }
    }

    @Test
    @DisplayName("Should display detailed bill when valid ID is selected")
    void testViewPastBills_DetailedBill() {
        Scanner scanner = createScanner("101\n\n\n"); // Simulate input: select bill ID, continue, exit

        try (MockedConstruction<OnlineBillGateway> billGatewayMock = mockConstruction(OnlineBillGateway.class,
                (mock, context) -> {
                    when(mock.getBillsByUsername("testuser123")).thenReturn(Arrays.asList(testBill));
                    when(mock.getItemsForBill(101)).thenReturn(Arrays.asList(testCartItem1, testCartItem2));
                })) {

            // Ensure testBill has the correct payment method
            testBill.setId(101);
            testBill.setPaymentMethod("Cash on Delivery"); // <-- change this if you want Pay in Store

            invokePrivateMethod("viewPastBills",
                    new Class[] { OnlineUser.class, Scanner.class },
                    new Object[] { testUser, scanner });

            String output = getOutput();
            assertTrue(output.contains("BILL DETAILS"));
            assertTrue(output.contains("Bill ID      : 101"));
            assertTrue(output.contains("Payment      : Cash on Delivery")); // <- fixed
            assertTrue(output.contains("Samsung Galaxy S24"));
            assertTrue(output.contains("MacBook Pro M3"));
            assertTrue(output.contains("Press Enter to continue..."));
        }
    }

    @Test
    @DisplayName("Should handle non-numeric bill ID input")
    void testViewPastBills_NonNumericBillID() {
        Scanner scanner = createScanner("abc\n\n"); // \n for pause

        try (MockedConstruction<OnlineBillGateway> billGatewayMock = mockConstruction(OnlineBillGateway.class,
                (mock, context) -> {
                    when(mock.getBillsByUsername("testuser123")).thenReturn(Arrays.asList(testBill));
                })) {
            invokePrivateMethod("viewPastBills",
                    new Class[] { OnlineUser.class, Scanner.class },
                    new Object[] { testUser, scanner });

            String output = getOutput();
            assertTrue(output.contains("Invalid input. Please enter a valid Bill ID."));
            assertTrue(output.contains("Press Enter to continue..."));
        }
    }

    // ============ Helper Method Tests ============

    @Test
    @DisplayName("Should truncate long strings correctly")
    void testTruncateString() {
        invokePrivateMethod("truncateString",
                new Class[] { String.class, int.class },
                new Object[] { "Short text", 20 });
        String result1 = getOutput(); // No output for truncateString, just verify result

        invokePrivateMethod("truncateString",
                new Class[] { String.class, int.class },
                new Object[] { "This is a very long text that needs truncation", 15 });
        String result2 = getOutput();

        invokePrivateMethod("truncateString",
                new Class[] { String.class, int.class },
                new Object[] { null, 10 });
        String result3 = getOutput();

        invokePrivateMethod("truncateString",
                new Class[] { String.class, int.class },
                new Object[] { "", 10 });
        String result4 = getOutput();

        // Use reflection to get results
        try {
            Method method = OnlineMainMenuController.class.getDeclaredMethod("truncateString", String.class, int.class);
            method.setAccessible(true);
            assertEquals("Short text", method.invoke(null, "Short text", 20));
            assertEquals("This is a ve...", method.invoke(null, "This is a very long text that needs truncation", 15));
            assertEquals("", method.invoke(null, null, 10));
            assertEquals("", method.invoke(null, "", 10));
        } catch (Exception e) {
            fail("Failed to test truncateString: " + e.getMessage());
        }
    }

    // ============ Payment Method Formatting Tests ============

    @Test
    @DisplayName("Should format payment methods correctly in bill display")
    void testPaymentMethodFormatting() {
        Scanner scanner = createScanner("0\n\n"); // Simulate entering 0 to go back

        Bill billCOD = new Bill();
        billCOD.setId(102);
        billCOD.setDate(LocalDateTime.of(2025, 6, 1, 12, 0));
        billCOD.setTotal(100.0);
        billCOD.setPaymentMethod("Cash on Delivery");

        Bill billPIS = new Bill();
        billPIS.setId(103);
        billPIS.setDate(LocalDateTime.of(2025, 6, 1, 13, 0));
        billPIS.setTotal(200.0);
        billPIS.setPaymentMethod("Pay in Store");

        try (MockedConstruction<OnlineBillGateway> gatewayMock = mockConstruction(OnlineBillGateway.class,
                (mock, context) -> {
                    when(mock.getBillsByUsername("testuser123")).thenReturn(List.of(billCOD, billPIS));
                    when(mock.getItemsForBill(anyInt())).thenReturn(List.of());
                })) {

            invokePrivateMethod("viewPastBills",
                    new Class[] { OnlineUser.class, Scanner.class },
                    new Object[] { testUser, scanner });

            String output = getOutput();
            System.out.println(output); // Debug output

            assertTrue(output.contains("COD"), "Should abbreviate 'Cash on Delivery' as COD");
            assertTrue(output.contains("PIS"), "Should abbreviate 'Pay in Store' as PIS");
        }
    }

    // ============ Edge Cases and Error Handling ============

    @Test
    @DisplayName("Should handle exception in bill details retrieval")
    void testViewPastBills_ExceptionInBillRetrieval() {
        Scanner scanner = createScanner("101\n\n\n"); // Extra \n for pause and continue

        try (MockedConstruction<OnlineBillGateway> billGatewayMock = mockConstruction(OnlineBillGateway.class,
                (mock, context) -> {
                    when(mock.getBillsByUsername("testuser123")).thenReturn(Arrays.asList(testBill));
                    when(mock.getItemsForBill(101)).thenThrow(new RuntimeException("Database error"));
                })) {
            invokePrivateMethod("viewPastBills",
                    new Class[] { OnlineUser.class, Scanner.class },
                    new Object[] { testUser, scanner });

            String output = getOutput();
            assertTrue(output.contains("Error retrieving bill details"));
            assertTrue(output.contains("Press Enter to continue..."));
        }
    }

}