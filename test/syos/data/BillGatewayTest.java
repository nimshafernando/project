package syos.data;

import syos.dto.ItemDTO;
import syos.interfaces.DatabaseConnectionProvider;
import syos.model.Bill;
import syos.model.CartItem;
import syos.util.BillStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BillGatewayTest {

    @Mock
    private DatabaseConnectionProvider connectionProvider;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement billStmt;

    @Mock
    private PreparedStatement itemStmt;

    @Mock
    private ResultSet generatedKeys;

    private BillGateway billGateway;

    private Bill sampleBill;
    private LocalDateTime sampleDateTime;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Setup mocks
        when(connectionProvider.getPoolConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(billStmt);
        when(connection.prepareStatement(anyString())).thenReturn(itemStmt);
        when(billStmt.getGeneratedKeys()).thenReturn(generatedKeys);

        // Initialize the gateway with mocked provider
        billGateway = new BillGateway(connectionProvider);

        // Create sample bill
        sampleDateTime = LocalDateTime.of(2025, 6, 4, 14, 30);
        List<CartItem> items = new ArrayList<>();

        // Create realistic items
        ItemDTO paracetamol = new ItemDTO("ITEM001", "Paracetamol", 10.50, 100);
        ItemDTO ibuprofen = new ItemDTO("ITEM002", "Ibuprofen", 15.75, 50);

        items.add(new CartItem(paracetamol, 2));
        items.add(new CartItem(ibuprofen, 1));

        // Create bill with employee name
        sampleBill = new Bill(
                101, // serialNumber
                sampleDateTime,
                items,
                36.75, // Total
                5.00, // Discount
                50.00, // Cash tendered
                13.25, // Change
                "John Doe" // Employee name
        );
    }

    @Test
    @DisplayName("Should create BillGateway with custom connection provider")
    void testConstructorWithProvider() {
        BillGateway gateway = new BillGateway(connectionProvider);
        assertNotNull(gateway);
    }

    @Test
    @DisplayName("Should create BillGateway with default connection provider")
    void testDefaultConstructor() {
        BillGateway gateway = new BillGateway();
        assertNotNull(gateway);
    }

    @Test
    @DisplayName("Should save bill successfully")
    void testSaveBill_Success() throws SQLException {
        // Use try-with-resources for static mock
        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            // Mock BillStorage
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(any(Bill.class)))
                    .thenReturn("#101-2025-06-04");

            // Mock successful execution
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(1);
            when(billStmt.executeUpdate()).thenReturn(1);
            when(itemStmt.executeBatch()).thenReturn(new int[] { 1, 1 });

            // Execute
            boolean result = billGateway.saveBill(sampleBill);

            // Assert
            assertTrue(result);

            // Verify bill insertion
            verify(billStmt).setString(1, "#101-2025-06-04");
            verify(billStmt).setDate(2, Date.valueOf(sampleDateTime.toLocalDate()));
            verify(billStmt).setTime(3, Time.valueOf(sampleDateTime.toLocalTime()));
            verify(billStmt).setDouble(4, 36.75);
            verify(billStmt).setDouble(5, 5.00);
            verify(billStmt).setDouble(6, 50.00);
            verify(billStmt).setDouble(7, 13.25);
            verify(billStmt).setString(8, "John Doe");
            verify(billStmt).executeUpdate();

            // Verify item insertions
            verify(itemStmt, times(2)).setInt(1, 1);
            verify(itemStmt).setString(2, "ITEM001");
            verify(itemStmt).setString(3, "Paracetamol");
            verify(itemStmt).setInt(4, 2);
            verify(itemStmt).setDouble(5, 10.50);
            verify(itemStmt).setDouble(6, 21.00);
            verify(itemStmt).setString(2, "ITEM002");
            verify(itemStmt).setString(3, "Ibuprofen");
            verify(itemStmt).setInt(4, 1);
            verify(itemStmt).setDouble(5, 15.75);
            verify(itemStmt).setDouble(6, 15.75);
            verify(itemStmt, times(2)).addBatch();
            verify(itemStmt).executeBatch();
        }
    }

    @Test
    @DisplayName("Should return false when connection is null")
    void testSaveBill_NullConnection() throws Exception {
        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(any(Bill.class)))
                    .thenReturn("#101-2025-06-04");

            when(connectionProvider.getPoolConnection()).thenReturn(null);

            boolean result = billGateway.saveBill(sampleBill);

            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should handle SQLException during connection")
    void testSaveBill_SQLException() throws Exception {
        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(any(Bill.class)))
                    .thenReturn("#101-2025-06-04");

            when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

            boolean result = billGateway.saveBill(sampleBill);

            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should return false when no generated keys")
    void testSaveBill_NoGeneratedKeys() throws SQLException {
        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(any(Bill.class)))
                    .thenReturn("#101-2025-06-04");

            when(generatedKeys.next()).thenReturn(false);
            when(billStmt.executeUpdate()).thenReturn(1);

            boolean result = billGateway.saveBill(sampleBill);

            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should save bill with empty items")
    void testSaveBill_EmptyItems() throws SQLException {
        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            // Create bill with empty items
            Bill emptyBill = new Bill(
                    102,
                    sampleDateTime,
                    new ArrayList<>(), // empty items list
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    "Jane Smith");

            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(any(Bill.class)))
                    .thenReturn("#102-2025-06-04");

            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(2);
            when(billStmt.executeUpdate()).thenReturn(1);

            boolean result = billGateway.saveBill(emptyBill);

            assertTrue(result);
            verify(itemStmt, never()).addBatch();
            verify(itemStmt, never()).executeBatch();
        }
    }

    @Test
    @DisplayName("Should handle batch execution failure")
    void testSaveBill_BatchExecutionFailure() throws SQLException {
        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(any(Bill.class)))
                    .thenReturn("#101-2025-06-04");

            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(1);
            when(billStmt.executeUpdate()).thenReturn(1);
            when(itemStmt.executeBatch()).thenThrow(new SQLException("Batch execution failed"));

            boolean result = billGateway.saveBill(sampleBill);

            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should handle exception when retrieving generated keys")
    void testSaveBill_GeneratedKeysException() throws SQLException {
        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(any(Bill.class)))
                    .thenReturn("#101-2025-06-04");

            when(billStmt.executeUpdate()).thenReturn(1);
            when(billStmt.getGeneratedKeys()).thenThrow(new SQLException("Failed to get keys"));

            boolean result = billGateway.saveBill(sampleBill);

            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should handle SQLException during bill statement preparation")
    void testSaveBill_PrepareStatementException() throws SQLException {
        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(any(Bill.class)))
                    .thenReturn("#101-2025-06-04");

            when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                    .thenThrow(new SQLException("Failed to prepare statement"));

            boolean result = billGateway.saveBill(sampleBill);

            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should handle SQLException during item statement preparation")
    void testSaveBill_ItemStatementException() throws SQLException {
        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(any(Bill.class)))
                    .thenReturn("#101-2025-06-04");

            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(1);
            when(billStmt.executeUpdate()).thenReturn(1);

            // First call returns billStmt, second call (for items) throws exception
            when(connection.prepareStatement(anyString()))
                    .thenThrow(new SQLException("Failed to prepare item statement"));

            boolean result = billGateway.saveBill(sampleBill);

            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should handle bill with null employee name")
    void testSaveBill_NullEmployeeName() throws SQLException {
        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            // Create bill without employee name
            Bill billWithoutEmployee = new Bill(
                    103,
                    sampleDateTime,
                    new ArrayList<>(),
                    100.0,
                    10.0,
                    100.0,
                    10.0);

            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(any(Bill.class)))
                    .thenReturn("#103-2025-06-04");

            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(3);
            when(billStmt.executeUpdate()).thenReturn(1);

            boolean result = billGateway.saveBill(billWithoutEmployee);

            assertTrue(result);
            verify(billStmt).setString(8, null);
        }
    }

    @Test
    @DisplayName("Should calculate total price correctly for cart items")
    void testSaveBill_VerifyTotalPriceCalculation() throws SQLException {
        try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
            mockedBillStorage.when(() -> BillStorage.getFormattedSerial(any(Bill.class)))
                    .thenReturn("#101-2025-06-04");

            // Create bill with specific items to verify price calculation
            List<CartItem> items = new ArrayList<>();
            ItemDTO item1 = new ItemDTO("TEST001", "Test Item 1", 25.50, 10);
            ItemDTO item2 = new ItemDTO("TEST002", "Test Item 2", 30.00, 5);

            items.add(new CartItem(item1, 3)); // 3 * 25.50 = 76.50
            items.add(new CartItem(item2, 2)); // 2 * 30.00 = 60.00

            Bill testBill = new Bill(104, sampleDateTime, items, 136.50, 0.0, 150.0, 13.50, "Test Employee");

            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(4);
            when(billStmt.executeUpdate()).thenReturn(1);
            when(itemStmt.executeBatch()).thenReturn(new int[] { 1, 1 });

            boolean result = billGateway.saveBill(testBill);

            assertTrue(result);

            // Verify total prices for items
            verify(itemStmt).setDouble(6, 76.50); // First item total
            verify(itemStmt).setDouble(6, 60.00); // Second item total
        }
    }
}