package syos.data;

import syos.dto.ItemDTO;
import syos.interfaces.DatabaseConnectionProvider;
import syos.model.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OnlineItemGatewayTest {

    @Mock
    private DatabaseConnectionProvider connectionProvider;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private OnlineItemGateway onlineItemGateway;

    private ItemDTO sampleOnlineItem;

    // Mocked CartItem for testing
    private static class MockCartItem extends CartItem {
        private final ItemDTO item;
        private final int quantity;

        public MockCartItem(ItemDTO item, int quantity) {
            super(item, quantity);
            this.item = item;
            this.quantity = quantity;
        }

        @Override
        public ItemDTO getItem() {
            return item;
        }

        @Override
        public int getQuantity() {
            return quantity;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionProvider.getPoolConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Sample online item for testing
        sampleOnlineItem = new ItemDTO("ONL001", "Online Protein Powder", 85.99, 50);
    }

    @Test
    void testInsert_Success() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = onlineItemGateway.insert(sampleOnlineItem);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, "ONL001");
        verify(preparedStatement).setString(2, "Online Protein Powder");
        verify(preparedStatement).setDouble(3, 85.99);
        verify(preparedStatement).setInt(4, 50);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInsert_Failure() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = onlineItemGateway.insert(sampleOnlineItem);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInsert_NullItem() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.insert(null));
    }

    @Test
    void testInsert_SQLException() throws Exception {
        // Arrange
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = onlineItemGateway.insert(sampleOnlineItem);

        // Assert
        assertFalse(result);
    }

    @Test
    void testUpdate_Success() throws SQLException {
        // Arrange
        ItemDTO updatedItem = new ItemDTO("ONL001", "Online Protein Powder Updated", 95.99, 75);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = onlineItemGateway.update(updatedItem);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, "Online Protein Powder Updated");
        verify(preparedStatement).setDouble(2, 95.99);
        verify(preparedStatement).setInt(3, 75);
        verify(preparedStatement).setString(4, "ONL001");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdate_Failure() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = onlineItemGateway.update(sampleOnlineItem);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdate_NullItem() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.update(null));
    }

    @Test
    void testDelete_Success() throws SQLException {
        // Arrange
        String itemCode = "ONL001";
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = onlineItemGateway.delete(itemCode);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, itemCode);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDelete_Failure() throws SQLException {
        // Arrange
        String itemCode = "NOTFOUND";
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = onlineItemGateway.delete(itemCode);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDelete_InvalidCode() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.delete(null));
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.delete(""));
    }

    @Test
    void testFindById_Success() throws SQLException {
        // Arrange
        String itemCode = "ONL002";
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("item_code")).thenReturn(itemCode);
        when(resultSet.getString("name")).thenReturn("Online Creatine");
        when(resultSet.getDouble("price")).thenReturn(45.50);
        when(resultSet.getInt("quantity")).thenReturn(100);

        // Act
        ItemDTO result = onlineItemGateway.findById(itemCode);

        // Assert
        assertNotNull(result);
        assertEquals(itemCode, result.getCode());
        assertEquals("Online Creatine", result.getName());
        assertEquals(45.50, result.getPrice());
        assertEquals(100, result.getQuantity());

        verify(preparedStatement).setString(1, itemCode);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindById_NotFound() throws SQLException {
        // Arrange
        String itemCode = "NOTFOUND";
        when(resultSet.next()).thenReturn(false);

        // Act
        ItemDTO result = onlineItemGateway.findById(itemCode);

        // Assert
        assertNull(result);
        verify(preparedStatement).setString(1, itemCode);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindById_InvalidCode() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.findById(null));
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.findById(""));
    }

    @Test
    void testFindById_SQLException() throws Exception {
        // Arrange
        String itemCode = "ONL001";
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        ItemDTO result = onlineItemGateway.findById(itemCode);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindAll_Success() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("item_code")).thenReturn("ONL001", "ONL002");
        when(resultSet.getString("name")).thenReturn("Online Protein Powder", "Online Creatine");
        when(resultSet.getDouble("price")).thenReturn(85.99, 45.50);
        when(resultSet.getInt("quantity")).thenReturn(50, 100);

        // Act
        List<ItemDTO> result = onlineItemGateway.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        ItemDTO item1 = result.get(0);
        assertEquals("ONL001", item1.getCode());
        assertEquals("Online Protein Powder", item1.getName());
        assertEquals(85.99, item1.getPrice());
        assertEquals(50, item1.getQuantity());

        ItemDTO item2 = result.get(1);
        assertEquals("ONL002", item2.getCode());
        assertEquals("Online Creatine", item2.getName());
        assertEquals(45.50, item2.getPrice());
        assertEquals(100, item2.getQuantity());

        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindAll_Empty() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(false);

        // Act
        List<ItemDTO> result = onlineItemGateway.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindAll_SQLException() throws Exception {
        // Arrange
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        List<ItemDTO> result = onlineItemGateway.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testReduceStock_Success() throws SQLException {
        // Arrange
        String itemCode = "ONL001";
        int quantity = 10;

        // Act
        onlineItemGateway.reduceStock(itemCode, quantity);

        // Assert
        verify(preparedStatement).setInt(1, quantity);
        verify(preparedStatement).setString(2, itemCode);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testReduceStock_InvalidParameters() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.reduceStock(null, 10));
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.reduceStock("", 10));
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.reduceStock("ONL001", 0));
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.reduceStock("ONL001", -5));
    }

    @Test
    void testReduceStock_SQLException() throws Exception {
        // Arrange
        String itemCode = "ONL001";
        int quantity = 10;
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act & Assert (should not throw exception)
        assertDoesNotThrow(() -> onlineItemGateway.reduceStock(itemCode, quantity));
    }

    @Test
    void testIncreaseStock_Success() throws SQLException {
        // Arrange
        String itemCode = "ONL001";
        int quantity = 20;

        // Act
        onlineItemGateway.increaseStock(itemCode, quantity);

        // Assert
        verify(preparedStatement).setInt(1, quantity);
        verify(preparedStatement).setString(2, itemCode);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testIncreaseStock_InvalidParameters() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.increaseStock(null, 10));
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.increaseStock("", 10));
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.increaseStock("ONL001", 0));
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.increaseStock("ONL001", -5));
    }

    @Test
    void testUpdateItemPrice_Success() throws SQLException {
        // Arrange
        String itemCode = "ONL001";
        double newPrice = 99.99;
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = onlineItemGateway.updateItemPrice(itemCode, newPrice);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setDouble(1, newPrice);
        verify(preparedStatement).setString(2, itemCode);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateItemPrice_Failure() throws SQLException {
        // Arrange
        String itemCode = "NOTFOUND";
        double newPrice = 99.99;
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = onlineItemGateway.updateItemPrice(itemCode, newPrice);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateItemPrice_InvalidParameters() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.updateItemPrice(null, 99.99));
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.updateItemPrice("", 99.99));
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.updateItemPrice("ONL001", -10.0));
    }

    @Test
    void testUpdateItemPrice_SQLException() throws Exception {
        // Arrange
        String itemCode = "ONL001";
        double newPrice = 99.99;
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = onlineItemGateway.updateItemPrice(itemCode, newPrice);

        // Assert
        assertFalse(result);
    }

    @Test
    void testReduceStockBatch_Success() throws SQLException {
        // Arrange
        List<CartItem> cartItems = new ArrayList<>();
        ItemDTO item1 = new ItemDTO("ONL001", "Online Protein Powder", 85.99, 50);
        ItemDTO item2 = new ItemDTO("ONL002", "Online Creatine", 45.50, 100);
        cartItems.add(new MockCartItem(item1, 2));
        cartItems.add(new MockCartItem(item2, 3));

        when(preparedStatement.executeBatch()).thenReturn(new int[] { 1, 1 });

        // Act
        boolean result = onlineItemGateway.reduceStockBatch(cartItems);

        // Assert
        assertTrue(result);
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(preparedStatement, times(2)).addBatch();
        verify(preparedStatement).executeBatch();
    }

    @Test
    void testReduceStockBatch_PartialFailure() throws SQLException {
        // Arrange
        List<CartItem> cartItems = new ArrayList<>();
        ItemDTO item1 = new ItemDTO("ONL001", "Online Protein Powder", 85.99, 50);
        ItemDTO item2 = new ItemDTO("ONL002", "Online Creatine", 45.50, 100);
        cartItems.add(new MockCartItem(item1, 2));
        cartItems.add(new MockCartItem(item2, 3));

        when(preparedStatement.executeBatch()).thenReturn(new int[] { 1, 0 }); // Second update failed

        // Act
        boolean result = onlineItemGateway.reduceStockBatch(cartItems);

        // Assert
        assertFalse(result);
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(preparedStatement).executeBatch();
    }

    @Test
    void testReduceStockBatch_EmptyCart() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.reduceStockBatch(null));
        assertThrows(IllegalArgumentException.class, () -> onlineItemGateway.reduceStockBatch(new ArrayList<>()));
    }

    @Test
    void testReduceStockBatch_SQLException() throws Exception {
        // Arrange
        List<CartItem> cartItems = new ArrayList<>();
        ItemDTO item = new ItemDTO("ONL001", "Online Protein Powder", 85.99, 50);
        cartItems.add(new MockCartItem(item, 2));

        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = onlineItemGateway.reduceStockBatch(cartItems);

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetAllItems_DelegatesToFindAll() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("item_code")).thenReturn("ONL001");
        when(resultSet.getString("name")).thenReturn("Online Protein Powder");
        when(resultSet.getDouble("price")).thenReturn(85.99);
        when(resultSet.getInt("quantity")).thenReturn(50);

        // Act
        List<ItemDTO> result = onlineItemGateway.getAllItems();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetItemByCode_DelegatesToFindById() throws SQLException {
        // Arrange
        String itemCode = "ONL001";
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("item_code")).thenReturn(itemCode);
        when(resultSet.getString("name")).thenReturn("Online Protein Powder");
        when(resultSet.getDouble("price")).thenReturn(85.99);
        when(resultSet.getInt("quantity")).thenReturn(50);

        // Act
        ItemDTO result = onlineItemGateway.getItemByCode(itemCode);

        // Assert
        assertNotNull(result);
        assertEquals(itemCode, result.getCode());
        verify(preparedStatement).setString(1, itemCode);
    }

    @Test
    void testDefaultConstructor() {
        // Act
        OnlineItemGateway gateway = new OnlineItemGateway();

        // Assert
        assertNotNull(gateway);
    }
}
