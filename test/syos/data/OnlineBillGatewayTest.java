package syos.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;
import syos.dto.ItemDTO;
import syos.interfaces.DatabaseConnectionProvider;
import syos.model.Bill;
import syos.model.CartItem;
import syos.util.BillStorage;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OnlineBillGatewayTest {

    @Mock
    private DatabaseConnectionProvider connectionProvider;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private PreparedStatement batchStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSet generatedKeysResultSet;

    private OnlineBillGateway onlineBillGateway;

    // Test data
    private Bill testBill;
    private List<CartItem> testItems;
    private final String testUsername = "john_doe";
    private final String testPaymentMethod = "Cash on Delivery";
    private final LocalDateTime testDateTime = LocalDateTime.of(2025, 6, 3, 14, 30, 0);

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Setup test items
        ItemDTO item1 = new ItemDTO("ONL001", "Online Hand Sanitizer 100ml", 250.00, 50);
        ItemDTO item2 = new ItemDTO("ONL002", "Online Face Mask Pack (5 pcs)", 150.00, 100);

        testItems = Arrays.asList(
                new CartItem(item1, 2),
                new CartItem(item2, 3));

        // Setup test bill
        testBill = new Bill();
        testBill.setSerialNumber(101);
        testBill.setDate(testDateTime);
        testBill.setItems(testItems);
        testBill.setTotal(950.00);
        testBill.setSource("online");

        // Default mock behavior
        when(connectionProvider.getPoolConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with custom connection provider")
        void testConstructorWithProvider() {
            OnlineBillGateway gateway = new OnlineBillGateway(connectionProvider);
            assertNotNull(gateway);
        }

        @Test
        @DisplayName("Should create with default connection provider")
        void testDefaultConstructor() {
            try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
                DatabaseConnection mockDbConnection = mock(DatabaseConnection.class);
                mockedStatic.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);

                OnlineBillGateway gateway = new OnlineBillGateway();
                assertNotNull(gateway);
            }
        }
    }

    @Nested
    @DisplayName("saveOnlineBill Tests")
    class SaveOnlineBillTests {

        @BeforeEach
        void setUp() throws Exception {
            onlineBillGateway = new OnlineBillGateway(connectionProvider);

            // Mock BillStorage static method
            try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
                mockedBillStorage.when(() -> BillStorage.getFormattedSerial(any(Bill.class)))
                        .thenReturn("#101-2025-06-03");
            }
        }

        @Test
        @DisplayName("Should save bill successfully")
        void testSaveOnlineBillSuccess() throws Exception {
            // Mock BillStorage
            try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
                mockedBillStorage.when(() -> BillStorage.getFormattedSerial(testBill))
                        .thenReturn("#101-2025-06-03");

                // Mock bill insertion
                when(preparedStatement.executeUpdate()).thenReturn(1);
                when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeysResultSet);
                when(generatedKeysResultSet.next()).thenReturn(true);
                when(generatedKeysResultSet.getInt(1)).thenReturn(1001);

                // Mock items batch insertion
                when(connection.prepareStatement(contains("INSERT INTO online_bill_items"))).thenReturn(batchStatement);
                when(batchStatement.executeBatch()).thenReturn(new int[] { 1, 1 });

                // Act
                boolean result = onlineBillGateway.saveOnlineBill(testBill, testUsername, testPaymentMethod);

                // Assert
                assertTrue(result);
                verify(connection).setAutoCommit(false);
                verify(connection).commit();
                verify(connection).setAutoCommit(true);
                verify(connection).close();

                // Verify bill parameters
                verify(preparedStatement).setString(1, "#101-2025-06-03");
                verify(preparedStatement).setString(2, testUsername);
                verify(preparedStatement).setTime(3, Time.valueOf(LocalTime.of(14, 30, 0)));
                verify(preparedStatement).setDouble(4, 950.00);
                verify(preparedStatement).setString(5, "online");
                verify(preparedStatement).setString(6, testPaymentMethod);
                verify(preparedStatement).setDate(7, Date.valueOf(LocalDate.of(2025, 6, 3)));

                // Verify items batch
                verify(batchStatement, times(2)).setInt(1, 1001);
                verify(batchStatement).setString(2, "ONL001");
                verify(batchStatement).setInt(3, 2);
                verify(batchStatement).setDouble(4, 250.00);
                verify(batchStatement).setString(2, "ONL002");
                verify(batchStatement).setInt(3, 3);
                verify(batchStatement).setDouble(4, 150.00);
                verify(batchStatement, times(2)).addBatch();
            }
        }

        @Test
        @DisplayName("Should return false when connection is null")
        void testSaveOnlineBillNullConnection() throws Exception {
            // Arrange
            when(connectionProvider.getPoolConnection()).thenReturn(null);

            // Act
            boolean result = onlineBillGateway.saveOnlineBill(testBill, testUsername, testPaymentMethod);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should rollback when bill insertion fails")
        void testSaveOnlineBillInsertionFailure() throws Exception {
            try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
                mockedBillStorage.when(() -> BillStorage.getFormattedSerial(testBill))
                        .thenReturn("#101-2025-06-03");

                // Mock bill insertion failure
                when(preparedStatement.executeUpdate()).thenReturn(0);

                // Act
                boolean result = onlineBillGateway.saveOnlineBill(testBill, testUsername, testPaymentMethod);

                // Assert
                assertFalse(result);
                verify(connection).rollback();
                verify(connection).setAutoCommit(true);
                verify(connection).close();
            }
        }

        @Test
        @DisplayName("Should rollback when no generated key returned")
        void testSaveOnlineBillNoGeneratedKey() throws Exception {
            try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
                mockedBillStorage.when(() -> BillStorage.getFormattedSerial(testBill))
                        .thenReturn("#101-2025-06-03");

                // Mock successful insertion but no generated key
                when(preparedStatement.executeUpdate()).thenReturn(1);
                when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeysResultSet);
                when(generatedKeysResultSet.next()).thenReturn(false);

                // Act
                boolean result = onlineBillGateway.saveOnlineBill(testBill, testUsername, testPaymentMethod);

                // Assert
                assertFalse(result);
                verify(connection).rollback();
            }
        }

        @Test
        @DisplayName("Should rollback when items batch insertion fails")
        void testSaveOnlineBillItemsFailure() throws Exception {
            try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
                mockedBillStorage.when(() -> BillStorage.getFormattedSerial(testBill))
                        .thenReturn("#101-2025-06-03");

                // Mock successful bill insertion
                when(preparedStatement.executeUpdate()).thenReturn(1);
                when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeysResultSet);
                when(generatedKeysResultSet.next()).thenReturn(true);
                when(generatedKeysResultSet.getInt(1)).thenReturn(1001);

                // Mock items batch insertion failure
                when(connection.prepareStatement(contains("INSERT INTO online_bill_items"))).thenReturn(batchStatement);
                when(batchStatement.executeBatch()).thenReturn(new int[] { 1, Statement.EXECUTE_FAILED });

                // Act
                boolean result = onlineBillGateway.saveOnlineBill(testBill, testUsername, testPaymentMethod);

                // Assert
                assertFalse(result);
                verify(connection).rollback();
            }
        }

        @Test
        @DisplayName("Should handle exception and rollback")
        void testSaveOnlineBillException() throws Exception {
            try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
                mockedBillStorage.when(() -> BillStorage.getFormattedSerial(testBill))
                        .thenReturn("#101-2025-06-03");

                // Mock exception during bill insertion
                when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

                // Act
                boolean result = onlineBillGateway.saveOnlineBill(testBill, testUsername, testPaymentMethod);

                // Assert
                assertFalse(result);
                verify(connection).rollback();
                verify(connection).close();
            }
        }

        @Test
        @DisplayName("Should handle rollback exception silently")
        void testSaveOnlineBillRollbackException() throws Exception {
            try (MockedStatic<BillStorage> mockedBillStorage = mockStatic(BillStorage.class)) {
                mockedBillStorage.when(() -> BillStorage.getFormattedSerial(testBill))
                        .thenReturn("#101-2025-06-03");

                // Mock exception during bill insertion
                when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));
                doThrow(new SQLException("Rollback failed")).when(connection).rollback();

                // Act
                boolean result = onlineBillGateway.saveOnlineBill(testBill, testUsername, testPaymentMethod);

                // Assert
                assertFalse(result);
                verify(connection).close();
            }
        }
    }

    @Nested
    class GetBillsByUsernameTests {

        @BeforeEach
        void setup() {
            onlineBillGateway = new OnlineBillGateway(connectionProvider);
        }

        @Test
        void testGetBillsByUsernameSuccess() throws Exception {
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultSet.getInt("id")).thenReturn(1001, 1002);
            when(resultSet.getString("serial_number")).thenReturn("#101-2025-06-03", "#102-2025-06-03");
            when(resultSet.getString("payment_method")).thenReturn("Cash on Delivery", "Pay in Store");
            when(resultSet.getDouble("total")).thenReturn(950.00, 1200.00);
            when(resultSet.getTimestamp("date")).thenReturn(
                    Timestamp.valueOf(LocalDateTime.of(2025, 6, 3, 14, 30)),
                    Timestamp.valueOf(LocalDateTime.of(2025, 6, 3, 16, 45)));

            List<Bill> bills = onlineBillGateway.getBillsByUsername(testUsername);

            assertEquals(2, bills.size());
            assertEquals("10120250603", bills.get(0).getSerial());
            assertEquals("10220250603", bills.get(1).getSerial());
        }

        @Test
        void testGetBillsByUsernameNoResults() throws Exception {
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);
            List<Bill> bills = onlineBillGateway.getBillsByUsername("none");
            assertTrue(bills.isEmpty());
        }

        @Test
        void testGetBillsByUsernameException() throws Exception {
            when(preparedStatement.executeQuery()).thenThrow(new SQLException("fail"));
            List<Bill> bills = onlineBillGateway.getBillsByUsername(testUsername);
            assertTrue(bills.isEmpty());
        }
    }

    @Nested
    class GetItemsForBillTests {

        @BeforeEach
        void setup() {
            onlineBillGateway = new OnlineBillGateway(connectionProvider);
        }

        @Test
        void testGetItemsForBillSuccess() throws Exception {
            int billId = 1001;
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultSet.getString("item_code")).thenReturn("ONL001", "ONL002");
            when(resultSet.getString("name")).thenReturn("Online Hand Sanitizer 100ml",
                    "Online Face Mask Pack (5 pcs)");
            when(resultSet.getDouble("price")).thenReturn(250.00, 150.00);
            when(resultSet.getInt("quantity")).thenReturn(2, 3);

            List<CartItem> items = onlineBillGateway.getItemsForBill(billId);
            assertEquals(2, items.size());
        }

        @Test
        void testGetItemsForBillNoResults() throws Exception {
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);
            List<CartItem> items = onlineBillGateway.getItemsForBill(9999);
            assertTrue(items.isEmpty());
        }

        @Test
        void testGetItemsForBillException() throws Exception {
            when(preparedStatement.executeQuery()).thenThrow(new SQLException("fail"));
            List<CartItem> items = onlineBillGateway.getItemsForBill(1001);
            assertTrue(items.isEmpty());
        }
    }
}