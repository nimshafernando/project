package syos.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import syos.data.ItemGateway;
import syos.interfaces.DatabaseConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ItemGatewayTest {

    @Mock
    private DatabaseConnectionProvider connectionProvider;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private ItemGateway itemGateway;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionProvider.getPoolConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        itemGateway = new ItemGateway(connectionProvider);
    }

    @Test
    @DisplayName("Should insert item successfully")
    void testInsert() throws Exception {
        // Arrange
        ItemDTO item = new ItemDTO("ITEM001", "Test Item", 100.0, 50);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = itemGateway.insert(item);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, "ITEM001");
        verify(preparedStatement).setString(2, "Test Item");
        verify(preparedStatement).setDouble(3, 100.0);
        verify(preparedStatement).setInt(4, 50);
    }

    @Test
    @DisplayName("Should find item by ID")
    void testFindById() throws Exception {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("code")).thenReturn("ITEM001");
        when(resultSet.getString("name")).thenReturn("Test Item");
        when(resultSet.getDouble("price")).thenReturn(100.0);
        when(resultSet.getInt("quantity")).thenReturn(50);

        // Act
        ItemDTO item = itemGateway.findById("ITEM001");

        // Assert
        assertNotNull(item);
        assertEquals("ITEM001", item.getCode());
        assertEquals("Test Item", item.getName());
        verify(preparedStatement).setString(1, "ITEM001");
    }

    @Test
    @DisplayName("Should return null when item not found")
    void testFindByIdNotFound() throws Exception {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        ItemDTO item = itemGateway.findById("NOTFOUND");

        // Assert
        assertNull(item);
    }

    @Test
    @DisplayName("Should update item")
    void testUpdate() throws Exception {
        // Arrange
        ItemDTO item = new ItemDTO("ITEM001", "Updated Item", 150.0, 75);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = itemGateway.update(item);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, "Updated Item");
        verify(preparedStatement).setDouble(2, 150.0);
        verify(preparedStatement).setInt(3, 75);
        verify(preparedStatement).setString(4, "ITEM001");
    }

    @Test
    @DisplayName("Should reduce stock")
    void testReduceStock() throws Exception {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        itemGateway.reduceStock("ITEM001", 10);

        // Assert
        verify(preparedStatement).setInt(1, 10);
        verify(preparedStatement).setString(2, "ITEM001");
        verify(preparedStatement).setInt(3, 10);
    }

    @Test
    @DisplayName("Should get items below reorder level")
    void testGetItemsBelowReorderLevel() throws Exception {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("code")).thenReturn("ITEM001", "ITEM002");
        when(resultSet.getString("name")).thenReturn("Low Stock 1", "Low Stock 2");
        when(resultSet.getDouble("price")).thenReturn(50.0, 75.0);
        when(resultSet.getInt("quantity")).thenReturn(5, 8);

        // Act
        List<ItemDTO> items = itemGateway.getItemsBelowReorderLevel();

        // Assert
        assertEquals(2, items.size());
        assertEquals("ITEM001", items.get(0).getCode());
        assertEquals("ITEM002", items.get(1).getCode());
    }
}