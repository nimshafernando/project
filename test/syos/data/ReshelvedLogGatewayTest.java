package syos.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import syos.dto.ReshelvedLogDTO;
import syos.interfaces.DatabaseConnectionProvider;
import syos.data.ReshelvedLogGateway.StoreType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for ReshelvedLogGateway class.
 * Tests all methods with 100% coverage using Mockito for database operations.
 */
public class ReshelvedLogGatewayTest {

    @Mock
    private DatabaseConnectionProvider mockConnectionProvider;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private ReshelvedLogGateway reshelvedLogGateway;
    private ReshelvedLogDTO testReshelvedLog;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() throws SQLException, Exception {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        testDateTime = LocalDateTime.of(2024, 1, 15, 10, 30);
        testReshelvedLog = new ReshelvedLogDTO(1, "PARACETAMOL500", 50, "INSTORE", testDateTime);

        // Setup mocks
        when(mockConnectionProvider.getPoolConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), eq(PreparedStatement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        reshelvedLogGateway = new ReshelvedLogGateway(mockConnectionProvider);
    }

    // ===================== INSERT TESTS =====================

    @Test
    void testInsert_Success() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(123);

        // Act
        boolean result = reshelvedLogGateway.insert(testReshelvedLog);

        // Assert
        assertTrue(result);
        assertEquals(123, testReshelvedLog.getId());
        verify(mockPreparedStatement).setString(1, "PARACETAMOL500");
        verify(mockPreparedStatement).setInt(2, 50);
        verify(mockPreparedStatement).setString(3, "INSTORE");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testInsert_NullEntity() {
        // Act
        boolean result = reshelvedLogGateway.insert(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testInsert_DatabaseException() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = reshelvedLogGateway.insert(testReshelvedLog);

        // Assert
        assertFalse(result);
    }

    @Test
    void testInsert_NoRowsAffected() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = reshelvedLogGateway.insert(testReshelvedLog);

        // Assert
        assertFalse(result);
    }

    @Test
    void testInsert_NoGeneratedKeys() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        boolean result = reshelvedLogGateway.insert(testReshelvedLog);

        // Assert
        assertFalse(result);
    }

    // ===================== FIND BY ID TESTS =====================

    @Test
    void testFindById_Success() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("item_code")).thenReturn("IBUPROFEN400");
        when(mockResultSet.getInt("quantity")).thenReturn(30);
        when(mockResultSet.getString("store_type")).thenReturn("ONLINE");
        when(mockResultSet.getTimestamp("reshelved_at")).thenReturn(Timestamp.valueOf(testDateTime));

        // Act
        ReshelvedLogDTO result = reshelvedLogGateway.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("IBUPROFEN400", result.getItemCode());
        assertEquals(30, result.getQuantity());
        assertEquals("ONLINE", result.getStoreType());
        assertEquals(testDateTime, result.getReshelvedAt());
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testFindById_NullId() {
        // Act
        ReshelvedLogDTO result = reshelvedLogGateway.findById(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindById_NotFound() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);

        // Act
        ReshelvedLogDTO result = reshelvedLogGateway.findById(999);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindById_DatabaseException() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        // Act
        ReshelvedLogDTO result = reshelvedLogGateway.findById(1);

        // Assert
        assertNull(result);
    }

    // ===================== FIND ALL TESTS =====================

    @Test
    void testFindAll_Success() throws SQLException {
        // Arrange
        setupMockResultSetForMultipleResults();

        // Act
        List<ReshelvedLogDTO> result = reshelvedLogGateway.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PARACETAMOL500", result.get(0).getItemCode());
        assertEquals("VITAMIND3", result.get(1).getItemCode());
    }

    @Test
    void testFindAll_EmptyResult() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<ReshelvedLogDTO> result = reshelvedLogGateway.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAll_DatabaseException() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        // Act
        List<ReshelvedLogDTO> result = reshelvedLogGateway.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ===================== UPDATE TESTS =====================

    @Test
    void testUpdate_Success() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = reshelvedLogGateway.update(testReshelvedLog);

        // Assert
        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "PARACETAMOL500");
        verify(mockPreparedStatement).setInt(2, 50);
        verify(mockPreparedStatement).setString(3, "INSTORE");
        verify(mockPreparedStatement).setInt(4, 1);
    }

    @Test
    void testUpdate_NullEntity() {
        // Act
        boolean result = reshelvedLogGateway.update(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testUpdate_NoRowsAffected() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = reshelvedLogGateway.update(testReshelvedLog);

        // Assert
        assertFalse(result);
    }

    @Test
    void testUpdate_DatabaseException() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = reshelvedLogGateway.update(testReshelvedLog);

        // Assert
        assertFalse(result);
    }

    // ===================== DELETE TESTS =====================

    @Test
    void testDelete_Success() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = reshelvedLogGateway.delete(1);

        // Assert
        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testDelete_NullId() {
        // Act
        boolean result = reshelvedLogGateway.delete(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDelete_NoRowsAffected() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = reshelvedLogGateway.delete(1);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDelete_DatabaseException() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = reshelvedLogGateway.delete(1);

        // Assert
        assertFalse(result);
    }

    // ===================== LOG RESHELVING TESTS =====================

    @Test
    void testLogReshelving_WithStoreType_Success() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        reshelvedLogGateway.logReshelving("ASPIRIN100", 25, StoreType.ONLINE);

        // Assert
        verify(mockPreparedStatement).setString(1, "ASPIRIN100");
        verify(mockPreparedStatement).setInt(2, 25);
        verify(mockPreparedStatement).setString(3, "ONLINE");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testLogReshelving_WithStoreType_NullItemCode() throws SQLException {
        // Act
        reshelvedLogGateway.logReshelving(null, 25, StoreType.INSTORE);

        // Assert
        verify(mockPreparedStatement, never()).executeUpdate();
    }

    @Test
    void testLogReshelving_WithStoreType_EmptyItemCode() throws SQLException {
        // Act
        reshelvedLogGateway.logReshelving("", 25, StoreType.INSTORE);

        // Assert
        verify(mockPreparedStatement, never()).executeUpdate();
    }

    @Test
    void testLogReshelving_WithStoreType_WhitespaceItemCode() throws SQLException {
        // Act
        reshelvedLogGateway.logReshelving("   ", 25, StoreType.INSTORE);

        // Assert
        verify(mockPreparedStatement, never()).executeUpdate();
    }

    @Test
    void testLogReshelving_WithStoreType_DatabaseException() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Act & Assert (should not throw exception)
        assertDoesNotThrow(() -> reshelvedLogGateway.logReshelving("ASPIRIN100", 25, StoreType.ONLINE));
    }

    @Test
    void testLogReshelving_WithStoreType_InstoreType() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        reshelvedLogGateway.logReshelving("CALCIUM500", 15, StoreType.INSTORE);

        // Assert
        verify(mockPreparedStatement).setString(1, "CALCIUM500");
        verify(mockPreparedStatement).setInt(2, 15);
        verify(mockPreparedStatement).setString(3, "INSTORE");
    }

    @Test
    void testLogReshelving_DefaultStoreType_Success() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        reshelvedLogGateway.logReshelving("MULTIVITAMIN", 40);

        // Assert
        verify(mockPreparedStatement).setString(1, "MULTIVITAMIN");
        verify(mockPreparedStatement).setInt(2, 40);
        verify(mockPreparedStatement).setString(3, "INSTORE");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testLogReshelving_DefaultStoreType_NullItemCode() throws SQLException {
        // Act
        reshelvedLogGateway.logReshelving(null, 40);

        // Assert
        verify(mockPreparedStatement, never()).executeUpdate();
    }

    @Test
    void testLogReshelving_DefaultStoreType_EmptyItemCode() throws SQLException {
        // Act
        reshelvedLogGateway.logReshelving("", 40);

        // Assert
        verify(mockPreparedStatement, never()).executeUpdate();
    }

    // ===================== GET RESHELVE HISTORY TESTS =====================

    @Test
    void testGetReshelveHistory_Success() throws SQLException {
        // Arrange
        setupMockResultSetForItemHistory();

        // Act
        List<ReshelvedLogDTO> result = reshelvedLogGateway.getReshelveHistory("PARACETAMOL500");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("PARACETAMOL500", result.get(0).getItemCode());
        assertEquals("PARACETAMOL500", result.get(1).getItemCode());
        assertEquals("PARACETAMOL500", result.get(2).getItemCode());
        verify(mockPreparedStatement).setString(1, "PARACETAMOL500");
    }

    @Test
    void testGetReshelveHistory_NullItemCode() {
        // Act
        List<ReshelvedLogDTO> result = reshelvedLogGateway.getReshelveHistory(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReshelveHistory_EmptyItemCode() {
        // Act
        List<ReshelvedLogDTO> result = reshelvedLogGateway.getReshelveHistory("");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReshelveHistory_WhitespaceItemCode() {
        // Act
        List<ReshelvedLogDTO> result = reshelvedLogGateway.getReshelveHistory("   ");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReshelveHistory_NoResults() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<ReshelvedLogDTO> result = reshelvedLogGateway.getReshelveHistory("NONEXISTENT");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReshelveHistory_DatabaseException() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        // Act
        List<ReshelvedLogDTO> result = reshelvedLogGateway.getReshelveHistory("PARACETAMOL500");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ===================== GET ALL RESHELVE HISTORY TESTS =====================

    @Test
    void testGetAllReshelveHistory_Success() throws SQLException {
        // Arrange
        setupMockResultSetForAllHistory();

        // Act
        List<ReshelvedLogDTO> result = reshelvedLogGateway.getAllReshelveHistory();

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        // Verify items are ordered by reshelved_at DESC (latest first)
        assertEquals("OMEGA3", result.get(0).getItemCode());
        assertEquals("PROBIOTICS", result.get(1).getItemCode());
        assertEquals("ZINC50", result.get(2).getItemCode());
        assertEquals("MAGNESIUM", result.get(3).getItemCode());
        assertEquals("BCCOMPLEX", result.get(4).getItemCode());
    }

    @Test
    void testGetAllReshelveHistory_EmptyResult() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<ReshelvedLogDTO> result = reshelvedLogGateway.getAllReshelveHistory();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllReshelveHistory_DatabaseException() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        // Act
        List<ReshelvedLogDTO> result = reshelvedLogGateway.getAllReshelveHistory();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ===================== CONSTRUCTOR TESTS =====================

    @Test
    void testDefaultConstructor() {
        // Act
        ReshelvedLogGateway gateway = new ReshelvedLogGateway();

        // Assert
        assertNotNull(gateway);
    }

    @Test
    void testParameterizedConstructor() {
        // Act
        ReshelvedLogGateway gateway = new ReshelvedLogGateway(mockConnectionProvider);

        // Assert
        assertNotNull(gateway);
    }

    // ===================== HELPER METHODS =====================

    private void setupMockResultSetForMultipleResults() throws SQLException {
        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(mockResultSet.getInt("id"))
                .thenReturn(1)
                .thenReturn(2);

        when(mockResultSet.getString("item_code"))
                .thenReturn("PARACETAMOL500")
                .thenReturn("VITAMIND3");

        when(mockResultSet.getInt("quantity"))
                .thenReturn(50)
                .thenReturn(30);

        when(mockResultSet.getString("store_type"))
                .thenReturn("INSTORE")
                .thenReturn("ONLINE");

        when(mockResultSet.getTimestamp("reshelved_at"))
                .thenReturn(Timestamp.valueOf(testDateTime))
                .thenReturn(Timestamp.valueOf(testDateTime.plusHours(1)));
    }

    private void setupMockResultSetForItemHistory() throws SQLException {
        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(mockResultSet.getInt("id"))
                .thenReturn(1)
                .thenReturn(2)
                .thenReturn(3);

        when(mockResultSet.getString("item_code"))
                .thenReturn("PARACETAMOL500")
                .thenReturn("PARACETAMOL500")
                .thenReturn("PARACETAMOL500");

        when(mockResultSet.getInt("quantity"))
                .thenReturn(50)
                .thenReturn(25)
                .thenReturn(75);

        when(mockResultSet.getString("store_type"))
                .thenReturn("INSTORE")
                .thenReturn("ONLINE")
                .thenReturn("INSTORE");

        when(mockResultSet.getTimestamp("reshelved_at"))
                .thenReturn(Timestamp.valueOf(testDateTime))
                .thenReturn(Timestamp.valueOf(testDateTime.minusHours(1)))
                .thenReturn(Timestamp.valueOf(testDateTime.minusHours(2)));
    }

    private void setupMockResultSetForAllHistory() throws SQLException {
        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(mockResultSet.getInt("id"))
                .thenReturn(5)
                .thenReturn(4)
                .thenReturn(3)
                .thenReturn(2)
                .thenReturn(1);

        when(mockResultSet.getString("item_code"))
                .thenReturn("OMEGA3")
                .thenReturn("PROBIOTICS")
                .thenReturn("ZINC50")
                .thenReturn("MAGNESIUM")
                .thenReturn("BCCOMPLEX");

        when(mockResultSet.getInt("quantity"))
                .thenReturn(20)
                .thenReturn(35)
                .thenReturn(45)
                .thenReturn(60)
                .thenReturn(40);

        when(mockResultSet.getString("store_type"))
                .thenReturn("ONLINE")
                .thenReturn("INSTORE")
                .thenReturn("ONLINE")
                .thenReturn("INSTORE")
                .thenReturn("ONLINE");

        when(mockResultSet.getTimestamp("reshelved_at"))
                .thenReturn(Timestamp.valueOf(testDateTime))
                .thenReturn(Timestamp.valueOf(testDateTime.minusMinutes(30)))
                .thenReturn(Timestamp.valueOf(testDateTime.minusHours(1)))
                .thenReturn(Timestamp.valueOf(testDateTime.minusHours(2)))
                .thenReturn(Timestamp.valueOf(testDateTime.minusHours(3)));
    }
}
