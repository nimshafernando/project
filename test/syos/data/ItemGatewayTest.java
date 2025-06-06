package syos.data;

import syos.dto.ItemDTO;
import syos.interfaces.DatabaseConnectionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ItemGatewayTest {

    @Mock
    private DatabaseConnectionProvider connectionProvider;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private ItemGateway itemGateway;

    private ItemDTO sampleItem;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionProvider.getPoolConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Sample item for testing
        sampleItem = new ItemDTO("PARA001", "Paracetamol 500mg", 12.50, 100);
    }

    @Test
    void testInsert_Success() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = itemGateway.insert(sampleItem);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, "PARA001");
        verify(preparedStatement).setString(2, "Paracetamol 500mg");
        verify(preparedStatement).setDouble(3, 12.50);
        verify(preparedStatement).setInt(4, 100);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInsert_Failure() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = itemGateway.insert(sampleItem);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInsert_SQLException() throws Exception {
        // Arrange
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = itemGateway.insert(sampleItem);

        // Assert
        assertFalse(result);
    }

    @Test
    void testFindById_Success() throws SQLException {
        // Arrange
        String itemCode = "IBUP001";
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("code")).thenReturn(itemCode);
        when(resultSet.getString("name")).thenReturn("Ibuprofen 400mg");
        when(resultSet.getDouble("price")).thenReturn(18.75);
        when(resultSet.getInt("quantity")).thenReturn(75);

        // Act
        ItemDTO result = itemGateway.findById(itemCode);

        // Assert
        assertNotNull(result);
        assertEquals(itemCode, result.getCode());
        assertEquals("Ibuprofen 400mg", result.getName());
        assertEquals(18.75, result.getPrice());
        assertEquals(75, result.getQuantity());

        verify(preparedStatement).setString(1, itemCode);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindById_NotFound() throws SQLException {
        // Arrange
        String itemCode = "NOTFOUND";
        when(resultSet.next()).thenReturn(false);

        // Act
        ItemDTO result = itemGateway.findById(itemCode);

        // Assert
        assertNull(result);
        verify(preparedStatement).setString(1, itemCode);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindById_SQLException() throws Exception {
        // Arrange
        String itemCode = "PARA001";
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        ItemDTO result = itemGateway.findById(itemCode);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindAll_Success() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("code")).thenReturn("PARA001", "IBUP001");
        when(resultSet.getString("name")).thenReturn("Paracetamol 500mg", "Ibuprofen 400mg");
        when(resultSet.getDouble("price")).thenReturn(12.50, 18.75);
        when(resultSet.getInt("quantity")).thenReturn(100, 75);

        // Act
        List<ItemDTO> result = itemGateway.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        ItemDTO item1 = result.get(0);
        assertEquals("PARA001", item1.getCode());
        assertEquals("Paracetamol 500mg", item1.getName());
        assertEquals(12.50, item1.getPrice());
        assertEquals(100, item1.getQuantity());

        ItemDTO item2 = result.get(1);
        assertEquals("IBUP001", item2.getCode());
        assertEquals("Ibuprofen 400mg", item2.getName());
        assertEquals(18.75, item2.getPrice());
        assertEquals(75, item2.getQuantity());

        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindAll_Empty() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(false);

        // Act
        List<ItemDTO> result = itemGateway.findAll();

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
        List<ItemDTO> result = itemGateway.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdate_Success() throws SQLException {
        // Arrange
        ItemDTO updatedItem = new ItemDTO("PARA001", "Paracetamol 500mg Updated", 15.00, 120);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = itemGateway.update(updatedItem);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, "Paracetamol 500mg Updated");
        verify(preparedStatement).setDouble(2, 15.00);
        verify(preparedStatement).setInt(3, 120);
        verify(preparedStatement).setString(4, "PARA001");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdate_Failure() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = itemGateway.update(sampleItem);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdate_SQLException() throws Exception {
        // Arrange
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = itemGateway.update(sampleItem);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDelete_Success() throws SQLException {
        // Arrange
        String itemCode = "PARA001";
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = itemGateway.delete(itemCode);

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
        boolean result = itemGateway.delete(itemCode);

        // Assert
        assertFalse(result);
        verify(preparedStatement).setString(1, itemCode);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDelete_SQLException() throws Exception {
        // Arrange
        String itemCode = "PARA001";
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = itemGateway.delete(itemCode);

        // Assert
        assertFalse(result);
    }

    @Test
    void testReduceStock_Success() throws SQLException {
        // Arrange
        String itemCode = "PARA001";
        int quantitySold = 10;
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        itemGateway.reduceStock(itemCode, quantitySold);

        // Assert
        verify(preparedStatement).setInt(1, quantitySold);
        verify(preparedStatement).setString(2, itemCode);
        verify(preparedStatement).setInt(3, quantitySold);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testReduceStock_InsufficientStock() throws SQLException {
        // Arrange
        String itemCode = "PARA001";
        int quantitySold = 10;
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        itemGateway.reduceStock(itemCode, quantitySold);

        // Assert
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testReduceStock_SQLException() throws Exception {
        // Arrange
        String itemCode = "PARA001";
        int quantitySold = 10;
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act & Assert (should not throw exception)
        assertDoesNotThrow(() -> itemGateway.reduceStock(itemCode, quantitySold));
    }

    @Test
    void testIncreaseStock_Success() throws SQLException {
        // Arrange
        String itemCode = "PARA001";
        int amount = 50;

        // Act
        itemGateway.increaseStock(itemCode, amount);

        // Assert
        verify(preparedStatement).setInt(1, amount);
        verify(preparedStatement).setString(2, itemCode);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testIncreaseStock_SQLException() throws Exception {
        // Arrange
        String itemCode = "PARA001";
        int amount = 50;
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act & Assert (should not throw exception)
        assertDoesNotThrow(() -> itemGateway.increaseStock(itemCode, amount));
    }

    @Test
    void testGetItemsBelowReorderLevel_Success() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("code")).thenReturn("LOW001", "LOW002");
        when(resultSet.getString("name")).thenReturn("Low Stock Item 1", "Low Stock Item 2");
        when(resultSet.getDouble("price")).thenReturn(10.00, 20.00);
        when(resultSet.getInt("quantity")).thenReturn(5, 8);

        // Act
        List<ItemDTO> result = itemGateway.getItemsBelowReorderLevel();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        ItemDTO lowItem1 = result.get(0);
        assertEquals("LOW001", lowItem1.getCode());
        assertEquals("Low Stock Item 1", lowItem1.getName());
        assertEquals(10.00, lowItem1.getPrice());
        assertEquals(5, lowItem1.getQuantity());

        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetItemsBelowReorderLevel_SQLException() throws Exception {
        // Arrange
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        List<ItemDTO> result = itemGateway.getItemsBelowReorderLevel();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateItemPrice_Success() throws SQLException {
        // Arrange
        String itemCode = "PARA001";
        double newPrice = 15.00;
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = itemGateway.updateItemPrice(itemCode, newPrice);

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
        double newPrice = 15.00;
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = itemGateway.updateItemPrice(itemCode, newPrice);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateItemPrice_SQLException() throws Exception {
        // Arrange
        String itemCode = "PARA001";
        double newPrice = 15.00;
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = itemGateway.updateItemPrice(itemCode, newPrice);

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetReorderAlerts_Success() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("code")).thenReturn("ALERT001");
        when(resultSet.getString("name")).thenReturn("Alert Item");
        when(resultSet.getDouble("price")).thenReturn(25.00);
        when(resultSet.getInt("quantity")).thenReturn(30);

        // Act
        List<ItemDTO> result = itemGateway.getReorderAlerts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ItemDTO alertItem = result.get(0);
        assertEquals("ALERT001", alertItem.getCode());
        assertEquals("Alert Item", alertItem.getName());
        assertEquals(25.00, alertItem.getPrice());
        assertEquals(30, alertItem.getQuantity());

        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetReorderAlerts_SQLException() throws Exception {
        // Arrange
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        List<ItemDTO> result = itemGateway.getReorderAlerts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateAllReorderLevels_Success() throws SQLException {
        // Arrange
        int newReorderLevel = 25;
        when(preparedStatement.executeUpdate()).thenReturn(5);

        // Act
        boolean result = itemGateway.updateAllReorderLevels(newReorderLevel);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setInt(1, newReorderLevel);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateAllReorderLevels_NoRowsAffected() throws SQLException {
        // Arrange
        int newReorderLevel = 25;
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = itemGateway.updateAllReorderLevels(newReorderLevel);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateAllReorderLevels_SQLException() throws Exception {
        // Arrange
        int newReorderLevel = 25;
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = itemGateway.updateAllReorderLevels(newReorderLevel);

        // Assert
        assertFalse(result);
    }

    @Test
    void testUpdateItemReorderLevel_Success() throws SQLException {
        // Arrange
        String itemCode = "PARA001";
        int newReorderLevel = 15;
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = itemGateway.updateItemReorderLevel(itemCode, newReorderLevel);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setInt(1, newReorderLevel);
        verify(preparedStatement).setString(2, itemCode);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateItemReorderLevel_Failure() throws SQLException {
        // Arrange
        String itemCode = "NOTFOUND";
        int newReorderLevel = 15;
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = itemGateway.updateItemReorderLevel(itemCode, newReorderLevel);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateItemReorderLevel_SQLException() throws Exception {
        // Arrange
        String itemCode = "PARA001";
        int newReorderLevel = 15;
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = itemGateway.updateItemReorderLevel(itemCode, newReorderLevel);

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetItemByCode_DelegatesToFindById() throws SQLException {
        // Arrange
        String itemCode = "PARA001";
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("code")).thenReturn(itemCode);
        when(resultSet.getString("name")).thenReturn("Paracetamol 500mg");
        when(resultSet.getDouble("price")).thenReturn(12.50);
        when(resultSet.getInt("quantity")).thenReturn(100);

        // Act
        ItemDTO result = itemGateway.getItemByCode(itemCode);

        // Assert
        assertNotNull(result);
        assertEquals(itemCode, result.getCode());
        verify(preparedStatement).setString(1, itemCode);
    }

    @Test
    void testInsertItem_DelegatesToInsert() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = itemGateway.insertItem(sampleItem);

        // Assert
        assertTrue(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testGetAllItems_DelegatesToFindAll() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("code")).thenReturn("PARA001");
        when(resultSet.getString("name")).thenReturn("Paracetamol 500mg");
        when(resultSet.getDouble("price")).thenReturn(12.50);
        when(resultSet.getInt("quantity")).thenReturn(100);

        // Act
        List<ItemDTO> result = itemGateway.getAllItems();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testDefaultConstructor() {
        // Act
        ItemGateway gateway = new ItemGateway();

        // Assert
        assertNotNull(gateway);
    }
}
