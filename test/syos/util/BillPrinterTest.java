package syos.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import syos.model.Bill;
import syos.model.CartItem;
import syos.dto.ItemDTO;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test coverage for BillPrinter utility class
 * Tests all printing scenarios with mocked dependencies
 */
class BillPrinterTest {

    @Mock
    private Bill mockBill;

    @Mock
    private CartItem mockCartItem1;

    @Mock
    private CartItem mockCartItem2;

    @Mock
    private ItemDTO mockItem1;

    @Mock
    private ItemDTO mockItem2;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Should print complete bill with all details")
    void testPrintCompleteBill() {
        // Arrange
        LocalDateTime testDate = LocalDateTime.of(2025, 6, 5, 14, 30, 45);

        // Setup mock items
        when(mockItem1.getName()).thenReturn("Premium Coffee Beans");
        when(mockItem2.getName()).thenReturn("Artisan Sourdough Bread");

        // Setup mock cart items
        when(mockCartItem1.getItem()).thenReturn(mockItem1);
        when(mockCartItem1.getQuantity()).thenReturn(2);
        when(mockCartItem1.getTotalPrice()).thenReturn(450.00);

        when(mockCartItem2.getItem()).thenReturn(mockItem2);
        when(mockCartItem2.getQuantity()).thenReturn(1);
        when(mockCartItem2.getTotalPrice()).thenReturn(125.50);

        // Setup mock bill
        when(mockBill.getSerialNumber()).thenReturn(12345);
        when(mockBill.getDate()).thenReturn(testDate);
        when(mockBill.getItems()).thenReturn(Arrays.asList(mockCartItem1, mockCartItem2));
        when(mockBill.getTotal()).thenReturn(575.50);
        when(mockBill.getDiscount()).thenReturn(25.00);
        when(mockBill.getCashTendered()).thenReturn(600.00);
        when(mockBill.getChange()).thenReturn(24.50);

        // Act
        BillPrinter.print(mockBill);

        // Assert
        String output = outputStreamCaptor.toString();

        // Verify header
        assertTrue(output.contains("===== SYOS BILL ====="));

        // Verify bill details
        assertTrue(output.contains("Bill No: 12345"));
        assertTrue(output.contains("Date: " + testDate.toString()));

        // Verify items
        assertTrue(output.contains("Premium Coffee Beans x 2 = Rs. 450.00"));
        assertTrue(output.contains("Artisan Sourdough Bread x 1 = Rs. 125.50"));

        // Verify totals
        assertTrue(output.contains("Total: Rs. 575.50"));
        assertTrue(output.contains("Discount: Rs. 25.00"));
        assertTrue(output.contains("Cash Tendered: Rs. 600.00"));
        assertTrue(output.contains("Change: Rs. 24.50"));

        // Verify separators
        assertTrue(output.contains("----------------------"));
        assertTrue(output.contains("======================"));

        // Verify all methods were called
        verify(mockBill).getSerialNumber();
        verify(mockBill).getDate();
        verify(mockBill).getItems();
        verify(mockBill).getTotal();
        verify(mockBill).getDiscount();
        verify(mockBill).getCashTendered();
        verify(mockBill).getChange();
        verify(mockCartItem1, times(1)).getItem();
        verify(mockCartItem1).getQuantity();
        verify(mockCartItem1).getTotalPrice();

        verify(mockCartItem2, times(1)).getItem();
        verify(mockCartItem2).getQuantity();
        verify(mockCartItem2).getTotalPrice();
    }

    @Test
    @DisplayName("Should print bill with single item")
    void testPrintBillWithSingleItem() {
        // Arrange
        LocalDateTime testDate = LocalDateTime.of(2025, 6, 5, 9, 15, 30);

        when(mockItem1.getName()).thenReturn("Fresh Milk");
        when(mockCartItem1.getItem()).thenReturn(mockItem1);
        when(mockCartItem1.getQuantity()).thenReturn(1);
        when(mockCartItem1.getTotalPrice()).thenReturn(85.00);

        when(mockBill.getSerialNumber()).thenReturn(1);
        when(mockBill.getDate()).thenReturn(testDate);
        when(mockBill.getItems()).thenReturn(Collections.singletonList(mockCartItem1));
        when(mockBill.getTotal()).thenReturn(85.00);
        when(mockBill.getDiscount()).thenReturn(0.00);
        when(mockBill.getCashTendered()).thenReturn(100.00);
        when(mockBill.getChange()).thenReturn(15.00);

        // Act
        BillPrinter.print(mockBill);

        // Assert
        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("Bill No: 1"));
        assertTrue(output.contains("Fresh Milk x 1 = Rs. 85.00"));
        assertTrue(output.contains("Total: Rs. 85.00"));
        assertTrue(output.contains("Discount: Rs. 0.00"));
        assertTrue(output.contains("Change: Rs. 15.00"));
    }

    @Test
    @DisplayName("Should print bill with zero discount and exact change")
    void testPrintBillWithZeroDiscountAndExactChange() {
        // Arrange
        LocalDateTime testDate = LocalDateTime.of(2025, 6, 5, 16, 45, 0);

        when(mockItem1.getName()).thenReturn("Organic Honey");
        when(mockCartItem1.getItem()).thenReturn(mockItem1);
        when(mockCartItem1.getQuantity()).thenReturn(3);
        when(mockCartItem1.getTotalPrice()).thenReturn(750.00);

        when(mockBill.getSerialNumber()).thenReturn(9999);
        when(mockBill.getDate()).thenReturn(testDate);
        when(mockBill.getItems()).thenReturn(Collections.singletonList(mockCartItem1));
        when(mockBill.getTotal()).thenReturn(750.00);
        when(mockBill.getDiscount()).thenReturn(0.00);
        when(mockBill.getCashTendered()).thenReturn(750.00);
        when(mockBill.getChange()).thenReturn(0.00);

        // Act
        BillPrinter.print(mockBill);

        // Assert
        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("Bill No: 9999"));
        assertTrue(output.contains("Organic Honey x 3 = Rs. 750.00"));
        assertTrue(output.contains("Discount: Rs. 0.00"));
        assertTrue(output.contains("Cash Tendered: Rs. 750.00"));
        assertTrue(output.contains("Change: Rs. 0.00"));
    }

    @Test
    @DisplayName("Should print bill with large quantities and amounts")
    void testPrintBillWithLargeQuantitiesAndAmounts() {
        // Arrange
        LocalDateTime testDate = LocalDateTime.of(2025, 12, 25, 11, 30, 0);

        when(mockItem1.getName()).thenReturn("Bulk Rice Package");
        when(mockCartItem1.getItem()).thenReturn(mockItem1);
        when(mockCartItem1.getQuantity()).thenReturn(50);
        when(mockCartItem1.getTotalPrice()).thenReturn(12500.00);

        when(mockBill.getSerialNumber()).thenReturn(999999);
        when(mockBill.getDate()).thenReturn(testDate);
        when(mockBill.getItems()).thenReturn(Collections.singletonList(mockCartItem1));
        when(mockBill.getTotal()).thenReturn(11875.00);
        when(mockBill.getDiscount()).thenReturn(625.00);
        when(mockBill.getCashTendered()).thenReturn(12000.00);
        when(mockBill.getChange()).thenReturn(125.00);

        // Act
        BillPrinter.print(mockBill);

        // Assert
        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("Bill No: 999999"));
        assertTrue(output.contains("Bulk Rice Package x 50 = Rs. 12500.00"));
        assertTrue(output.contains("Total: Rs. 11875.00"));
        assertTrue(output.contains("Discount: Rs. 625.00"));
    }

    @Test
    @DisplayName("Should print bill with special characters in item names")
    void testPrintBillWithSpecialCharactersInItemNames() {
        // Arrange
        LocalDateTime testDate = LocalDateTime.of(2025, 6, 5, 20, 0, 0);

        when(mockItem1.getName()).thenReturn("Café Espresso - Premium Blend (200g)");
        when(mockItem2.getName()).thenReturn("Crème Brûlée & Vanilla Extract");

        when(mockCartItem1.getItem()).thenReturn(mockItem1);
        when(mockCartItem1.getQuantity()).thenReturn(1);
        when(mockCartItem1.getTotalPrice()).thenReturn(299.99);

        when(mockCartItem2.getItem()).thenReturn(mockItem2);
        when(mockCartItem2.getQuantity()).thenReturn(2);
        when(mockCartItem2.getTotalPrice()).thenReturn(149.98);

        when(mockBill.getSerialNumber()).thenReturn(12345);
        when(mockBill.getDate()).thenReturn(testDate);
        when(mockBill.getItems()).thenReturn(Arrays.asList(mockCartItem1, mockCartItem2));
        when(mockBill.getTotal()).thenReturn(449.97);
        when(mockBill.getDiscount()).thenReturn(0.00);
        when(mockBill.getCashTendered()).thenReturn(500.00);
        when(mockBill.getChange()).thenReturn(50.03);

        // Act
        BillPrinter.print(mockBill);

        // Assert
        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("Café Espresso - Premium Blend (200g) x 1 = Rs. 299.99"));
        assertTrue(output.contains("Crème Brûlée & Vanilla Extract x 2 = Rs. 149.98"));
        assertTrue(output.contains("Total: Rs. 449.97"));
        assertTrue(output.contains("Change: Rs. 50.03"));
    }

    @Test
    @DisplayName("Should handle decimal precision correctly")
    void testPrintBillWithDecimalPrecision() {
        // Arrange
        LocalDateTime testDate = LocalDateTime.of(2025, 6, 5, 8, 30, 15);

        when(mockItem1.getName()).thenReturn("Test Item");
        when(mockCartItem1.getItem()).thenReturn(mockItem1);
        when(mockCartItem1.getQuantity()).thenReturn(1);
        when(mockCartItem1.getTotalPrice()).thenReturn(123.456789); // Will be formatted to 2 decimal places

        when(mockBill.getSerialNumber()).thenReturn(777);
        when(mockBill.getDate()).thenReturn(testDate);
        when(mockBill.getItems()).thenReturn(Collections.singletonList(mockCartItem1));
        when(mockBill.getTotal()).thenReturn(123.45);
        when(mockBill.getDiscount()).thenReturn(1.23);
        when(mockBill.getCashTendered()).thenReturn(125.00);
        when(mockBill.getChange()).thenReturn(1.55);

        // Act
        BillPrinter.print(mockBill);

        // Assert
        String output = outputStreamCaptor.toString();

        // Verify decimal formatting (2 decimal places)
        assertTrue(output.contains("Test Item x 1 = Rs. 123.46")); // Rounded up
        assertTrue(output.contains("Total: Rs. 123.45"));
        assertTrue(output.contains("Discount: Rs. 1.23"));
        assertTrue(output.contains("Change: Rs. 1.55"));
    }

    @Test
    @DisplayName("Should print bill structure correctly with real Bill object")
    void testPrintBillWithRealBillObject() {
        // Arrange - Create real objects to test integration
        ItemDTO realItem1 = new ItemDTO("COFFEE001", "Colombian Coffee", 250.00, 10);
        ItemDTO realItem2 = new ItemDTO("BREAD002", "Whole Wheat Bread", 45.00, 25);

        CartItem realCartItem1 = new CartItem(realItem1, 2);
        CartItem realCartItem2 = new CartItem(realItem2, 3);

        LocalDateTime billDate = LocalDateTime.of(2025, 6, 5, 12, 0, 0);

        Bill realBill = new Bill(54321, billDate,
                Arrays.asList(realCartItem1, realCartItem2),
                635.00, 30.00, 700.00, 65.00);

        // Act
        BillPrinter.print(realBill);

        // Assert
        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("Bill No: 54321"));
        assertTrue(output.contains("Colombian Coffee x 2 = Rs. 500.00"));
        assertTrue(output.contains("Whole Wheat Bread x 3 = Rs. 135.00"));
        assertTrue(output.contains("Total: Rs. 635.00"));
        assertTrue(output.contains("Discount: Rs. 30.00"));
        assertTrue(output.contains("Cash Tendered: Rs. 700.00"));
        assertTrue(output.contains("Change: Rs. 65.00"));
    }
}
