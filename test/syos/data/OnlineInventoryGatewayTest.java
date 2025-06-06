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

public class OnlineInventoryGatewayTest {

    @Mock
    private DatabaseConnectionProvider connectionProvider;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private OnlineInventoryGateway onlineInventoryGateway;

    private ItemDTO sampleOnlineItem;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionProvider.getPoolConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Sample online item for testing
        sampleOnlineItem = new ItemDTO("ONLINE001", "Online Vitamin D", 35.50, 150);
    }

    @Test
    void testInsert_Success() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = onlineInventoryGateway.insert(sampleOnlineItem);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, "ONLINE001");
        verify(preparedStatement).setString(2, "Online Vitamin D");
        verify(preparedStatement).setDouble(3, 35.50);
        verify(preparedStatement).setInt(4, 150);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInsert_Failure() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = onlineInventoryGateway.insert(sampleOnlineItem);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInsert_SQLException() throws Exception {
        // Arrange
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = onlineInventoryGateway.insert(sampleOnlineItem);

        // Assert
        assertFalse(result);
    }

    @Test
    void testFindById_Success() throws SQLException {
        // Arrange
        String itemCode = "ONLINE002";
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("code")).thenReturn(itemCode);
        when(resultSet.getString("name")).thenReturn("Online Calcium");
        when(resultSet.getDouble("price")).thenReturn(28.75);
        when(resultSet.getInt("quantity")).thenReturn(200);

        // Act
        ItemDTO result = onlineInventoryGateway.findById(itemCode);

        // Assert
        assertNotNull(result);
        assertEquals(itemCode, result.getCode());
        assertEquals("Online Calcium", result.getName());
        assertEquals(28.75, result.getPrice());
        assertEquals(200, result.getQuantity());

        verify(preparedStatement).setString(1, itemCode);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindById_NotFound() throws SQLException {
        // Arrange
        String itemCode = "NOTFOUND";
        when(resultSet.next()).thenReturn(false);

        // Act
        ItemDTO result = onlineInventoryGateway.findById(itemCode);

        // Assert
        assertNull(result);
        verify(preparedStatement).setString(1, itemCode);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindById_SQLException() throws Exception {
        // Arrange
        String itemCode = "ONLINE001";
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        ItemDTO result = onlineInventoryGateway.findById(itemCode);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindAll_Success() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("code")).thenReturn("ONLINE001", "ONLINE002");
        when(resultSet.getString("name")).thenReturn("Online Vitamin D", "Online Calcium");
        when(resultSet.getDouble("price")).thenReturn(35.50, 28.75);
        when(resultSet.getInt("quantity")).thenReturn(150, 200);

        // Act
        List<ItemDTO> result = onlineInventoryGateway.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        ItemDTO item1 = result.get(0);
        assertEquals("ONLINE001", item1.getCode());
        assertEquals("Online Vitamin D", item1.getName());
        assertEquals(35.50, item1.getPrice());
        assertEquals(150, item1.getQuantity());

        ItemDTO item2 = result.get(1);
        assertEquals("ONLINE002", item2.getCode());
        assertEquals("Online Calcium", item2.getName());
        assertEquals(28.75, item2.getPrice());
        assertEquals(200, item2.getQuantity());

        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindAll_Empty() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(false);

        // Act
        List<ItemDTO> result = onlineInventoryGateway.findAll();

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
        List<ItemDTO> result = onlineInventoryGateway.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdate_Success() throws SQLException {
        // Arrange
        ItemDTO updatedItem = new ItemDTO("ONLINE001", "Online Vitamin D Updated", 40.00, 180);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = onlineInventoryGateway.update(updatedItem);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, "Online Vitamin D Updated");
        verify(preparedStatement).setDouble(2, 40.00);
        verify(preparedStatement).setInt(3, 180);
        verify(preparedStatement).setString(4, "ONLINE001");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdate_Failure() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = onlineInventoryGateway.update(sampleOnlineItem);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdate_SQLException() throws Exception {
        // Arrange
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = onlineInventoryGateway.update(sampleOnlineItem);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDelete_Success() throws SQLException {
        // Arrange
        String itemCode = "ONLINE001";
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = onlineInventoryGateway.delete(itemCode);

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
        boolean result = onlineInventoryGateway.delete(itemCode);

        // Assert
        assertFalse(result);
        verify(preparedStatement).setString(1, itemCode);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDelete_SQLException() throws Exception {
        // Arrange
        String itemCode = "ONLINE001";
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = onlineInventoryGateway.delete(itemCode);

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetAllOnlineItems_Success() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("code")).thenReturn("ONLINE003", "ONLINE004");
        when(resultSet.getString("name")).thenReturn("Online Iron", "Online Zinc");
        when(resultSet.getDouble("price")).thenReturn(22.50, 18.00);
        when(resultSet.getInt("quantity")).thenReturn(120, 90);

        // Act
        List<ItemDTO> result = onlineInventoryGateway.getAllOnlineItems();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        ItemDTO item1 = result.get(0);
        assertEquals("ONLINE003", item1.getCode());
        assertEquals("Online Iron", item1.getName());
        assertEquals(22.50, item1.getPrice());
        assertEquals(120, item1.getQuantity());

        ItemDTO item2 = result.get(1);
        assertEquals("ONLINE004", item2.getCode());
        assertEquals("Online Zinc", item2.getName());
        assertEquals(18.00, item2.getPrice());
        assertEquals(90, item2.getQuantity());

        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetAllOnlineItems_SQLException() throws Exception {
        // Arrange
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        List<ItemDTO> result = onlineInventoryGateway.getAllOnlineItems();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetOnlineItemByCode_Success() throws SQLException {
        // Arrange
        String itemCode = "ONLINE005";
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("name")).thenReturn("Online Magnesium");
        when(resultSet.getDouble("price")).thenReturn(30.25);
        when(resultSet.getInt("quantity")).thenReturn(75);

        // Act
        ItemDTO result = onlineInventoryGateway.getOnlineItemByCode(itemCode);

        // Assert
        assertNotNull(result);
        assertEquals(itemCode, result.getCode());
        assertEquals("Online Magnesium", result.getName());
        assertEquals(30.25, result.getPrice());
        assertEquals(75, result.getQuantity());

        verify(preparedStatement).setString(1, itemCode);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetOnlineItemByCode_NotFound() throws SQLException {
        // Arrange
        String itemCode = "NOTFOUND";
        when(resultSet.next()).thenReturn(false);

        // Act
        ItemDTO result = onlineInventoryGateway.getOnlineItemByCode(itemCode);

        // Assert
        assertNull(result);
        verify(preparedStatement).setString(1, itemCode);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetOnlineItemByCode_SQLException() throws Exception {
        // Arrange
        String itemCode = "ONLINE001";
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        ItemDTO result = onlineInventoryGateway.getOnlineItemByCode(itemCode);

        // Assert
        assertNull(result);
    }

    @Test
    void testReduceOnlineStock_Success() throws SQLException {
        // Arrange
        String itemCode = "ONLINE001";
        int quantity = 25;
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = onlineInventoryGateway.reduceOnlineStock(itemCode, quantity);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setInt(1, quantity);
        verify(preparedStatement).setString(2, itemCode);
        verify(preparedStatement).setInt(3, quantity);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testReduceOnlineStock_InsufficientStock() throws SQLException {
        // Arrange
        String itemCode = "ONLINE001";
        int quantity = 25;
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = onlineInventoryGateway.reduceOnlineStock(itemCode, quantity);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testReduceOnlineStock_SQLException() throws Exception {
        // Arrange
        String itemCode = "ONLINE001";
        int quantity = 25;
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = onlineInventoryGateway.reduceOnlineStock(itemCode, quantity);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDefaultConstructor() {
        // Act
        OnlineInventoryGateway gateway = new OnlineInventoryGateway();

        // Assert
        assertNotNull(gateway);
    }
}
