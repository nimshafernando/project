package syos.data;

import syos.dto.BatchDTO;
import syos.interfaces.DatabaseConnectionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BatchGatewayTest {

    @Mock
    private DatabaseConnectionProvider connectionProvider;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private BatchGateway batchGateway;

    private BatchDTO sampleBatch;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionProvider.getPoolConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        sampleBatch = new BatchDTO(
                1,
                "ITEM001",
                "Paracetamol",
                10.50,
                100,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                20);
    }

    @Test
    void testInsertBatch_Success() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean result = batchGateway.insertBatch(sampleBatch);
        assertTrue(result);
        verify(preparedStatement).setString(1, "ITEM001");
        verify(preparedStatement).setString(2, "Paracetamol");
        verify(preparedStatement).setDouble(3, 10.50);
        verify(preparedStatement).setInt(4, 100);
        verify(preparedStatement).setDate(5, Date.valueOf(LocalDate.of(2025, 1, 1)));
        verify(preparedStatement).setDate(6, Date.valueOf(LocalDate.of(2025, 12, 31)));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInsertBatch_SQLException() throws SQLException {
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));
        boolean result = batchGateway.insertBatch(sampleBatch);
        assertFalse(result);
    }

    @Test
    void testInsertBatch_NullBatch() {
        boolean result = batchGateway.insertBatch(null);
        assertFalse(result);
    }

    @Test
    void testFindById_Success() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("item_code")).thenReturn("ITEM001");
        when(resultSet.getString("name")).thenReturn("Paracetamol");
        when(resultSet.getDouble("selling_price")).thenReturn(10.50);
        when(resultSet.getInt("quantity")).thenReturn(100);
        when(resultSet.getDate("purchase_date")).thenReturn(Date.valueOf(LocalDate.of(2025, 1, 1)));
        when(resultSet.getDate("expiry_date")).thenReturn(Date.valueOf(LocalDate.of(2025, 12, 31)));
        when(resultSet.getInt("used_quantity")).thenReturn(20);
        BatchDTO result = batchGateway.findById(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("ITEM001", result.getItemCode());
        assertEquals("Paracetamol", result.getName());
        assertEquals(10.50, result.getSellingPrice());
        assertEquals(100, result.getQuantity());
        assertEquals(LocalDate.of(2025, 1, 1), result.getPurchaseDate());
        assertEquals(LocalDate.of(2025, 12, 31), result.getExpiryDate());
        assertEquals(20, result.getUsedQuantity());
    }

    @Test
    void testFindById_NotFound() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        BatchDTO result = batchGateway.findById(1);
        assertNull(result);
    }

    @Test
    void testFindById_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));
        BatchDTO result = batchGateway.findById(1);
        assertNull(result);
    }

    @Test
    void testFindAll_Success() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("item_code")).thenReturn("ITEM001");
        when(resultSet.getString("name")).thenReturn("Paracetamol");
        when(resultSet.getDouble("selling_price")).thenReturn(10.50);
        when(resultSet.getInt("quantity")).thenReturn(100);
        when(resultSet.getDate("purchase_date")).thenReturn(Date.valueOf(LocalDate.of(2025, 1, 1)));
        when(resultSet.getDate("expiry_date")).thenReturn(Date.valueOf(LocalDate.of(2025, 12, 31)));
        when(resultSet.getInt("used_quantity")).thenReturn(20);
        List<BatchDTO> result = batchGateway.findAll();
        assertEquals(1, result.size());
        BatchDTO batch = result.get(0);
        assertEquals(1, batch.getId());
        assertEquals("ITEM001", batch.getItemCode());
        assertEquals("Paracetamol", batch.getName());
        assertEquals(10.50, batch.getSellingPrice());
        assertEquals(100, batch.getQuantity());
        assertEquals(LocalDate.of(2025, 1, 1), batch.getPurchaseDate());
        assertEquals(LocalDate.of(2025, 12, 31), batch.getExpiryDate());
        assertEquals(20, batch.getUsedQuantity());
    }

    @Test
    void testFindAll_Empty() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        List<BatchDTO> result = batchGateway.findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAll_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));
        List<BatchDTO> result = batchGateway.findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdate_Success() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean result = batchGateway.update(sampleBatch);
        assertTrue(result);
        verify(preparedStatement).setString(1, "Paracetamol");
        verify(preparedStatement).setDouble(2, 10.50);
        verify(preparedStatement).setInt(3, 100);
        verify(preparedStatement).setDate(4, Date.valueOf(LocalDate.of(2025, 1, 1)));
        verify(preparedStatement).setDate(5, Date.valueOf(LocalDate.of(2025, 12, 31)));
        verify(preparedStatement).setInt(6, 20);
        verify(preparedStatement).setInt(7, 1);
    }

    @Test
    void testUpdate_SQLException() throws SQLException {
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));
        boolean result = batchGateway.update(sampleBatch);
        assertFalse(result);
    }

    @Test
    void testUpdate_NullBatch() {
        boolean result = batchGateway.update(null);
        assertFalse(result);
    }

    @Test
    void testDelete_Success() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean result = batchGateway.delete(1);
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void testDelete_NotFound() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(0);
        boolean result = batchGateway.delete(1);
        assertFalse(result);
    }

    @Test
    void testDelete_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));
        boolean result = batchGateway.delete(1);
        assertFalse(result);
    }

    @Test
    void testGetExpiredBatches_Success() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("item_code")).thenReturn("ITEM001");
        when(resultSet.getString("name")).thenReturn("Paracetamol");
        when(resultSet.getDouble("selling_price")).thenReturn(10.50);
        when(resultSet.getInt("quantity")).thenReturn(100);
        when(resultSet.getInt("used_quantity")).thenReturn(20);
        when(resultSet.getDate("purchase_date")).thenReturn(Date.valueOf(LocalDate.of(2025, 1, 1)));
        when(resultSet.getDate("expiry_date")).thenReturn(Date.valueOf(LocalDate.of(2024, 12, 31)));

        List<BatchDTO> result = batchGateway.getExpiredBatches(LocalDate.of(2025, 1, 1));
        assertEquals(1, result.size());
        assertEquals("ITEM001", result.get(0).getItemCode());
    }

    @Test
    void testGetExpiredBatches_Empty() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        List<BatchDTO> result = batchGateway.getExpiredBatches(LocalDate.of(2025, 1, 1));
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetExpiredBatches_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));
        List<BatchDTO> result = batchGateway.getExpiredBatches(LocalDate.of(2025, 1, 1));
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAvailableBatchesForItem_Success() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("item_code")).thenReturn("ITEM001");
        when(resultSet.getString("name")).thenReturn("Paracetamol");
        when(resultSet.getDouble("selling_price")).thenReturn(10.50);
        when(resultSet.getInt("quantity")).thenReturn(100);
        when(resultSet.getInt("used_quantity")).thenReturn(20);
        when(resultSet.getDate("purchase_date")).thenReturn(Date.valueOf(LocalDate.of(2025, 1, 1)));
        when(resultSet.getDate("expiry_date")).thenReturn(Date.valueOf(LocalDate.of(2025, 12, 31)));

        List<BatchDTO> result = batchGateway.getAvailableBatchesForItem("ITEM001");
        assertEquals(1, result.size());
        assertEquals(sampleBatch, result.get(0));
    }

    @Test
    void testGetAvailableBatchesForItem_Empty() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        List<BatchDTO> result = batchGateway.getAvailableBatchesForItem("ITEM001");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAvailableBatchesForItem_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));
        List<BatchDTO> result = batchGateway.getAvailableBatchesForItem("ITEM001");
        assertTrue(result.isEmpty());
    }

    @Test
    void testReduceBatchQuantity_Success() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("quantity")).thenReturn(80);
        boolean result = batchGateway.reduceBatchQuantity("ITEM001", LocalDate.of(2025, 1, 1), 20);
        assertTrue(result);
        verify(preparedStatement).setInt(1, 20);
        verify(preparedStatement).setInt(2, 20);
        verify(preparedStatement).setString(3, "ITEM001");
        verify(preparedStatement).setDate(4, Date.valueOf(LocalDate.of(2025, 1, 1)));
    }

    @Test
    void testReduceBatchQuantity_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));
        boolean result = batchGateway.reduceBatchQuantity("ITEM001", LocalDate.of(2025, 1, 1), 20);
        assertFalse(result);
    }

    @Test
    void testGetRemainingQuantity_Success() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("quantity")).thenReturn(80);

        Method method = BatchGateway.class.getDeclaredMethod("getRemainingQuantity", String.class, LocalDate.class);
        method.setAccessible(true);
        int result = (int) method.invoke(batchGateway, "ITEM001", LocalDate.of(2025, 1, 1));
        assertEquals(80, result);
    }

    @Test
    void testGetRemainingQuantity_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        Method method = BatchGateway.class.getDeclaredMethod("getRemainingQuantity", String.class, LocalDate.class);
        method.setAccessible(true);
        int result = (int) method.invoke(batchGateway, "ITEM001", LocalDate.of(2025, 1, 1));
        assertEquals(0, result);
    }

    @Test
    void testGetStockSummaryPerItemWithNames_Success() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("item_code")).thenReturn("ITEM001");
        when(resultSet.getString("item_name")).thenReturn("Paracetamol");
        when(resultSet.getInt("total")).thenReturn(100);
        when(resultSet.getInt("used")).thenReturn(20);

        Map<String, Object[]> result = batchGateway.getStockSummaryPerItemWithNames();
        assertEquals(1, result.size());
        Object[] summary = result.get("ITEM001");
        assertEquals("Paracetamol", summary[0]);
        assertEquals(100, summary[1]);
        assertEquals(20, summary[2]);
        assertEquals(80, summary[3]);
    }

    @Test
    void testGetStockSummaryPerItemWithNames_Empty() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        Map<String, Object[]> result = batchGateway.getStockSummaryPerItemWithNames();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetStockSummaryPerItemWithNames_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));
        Map<String, Object[]> result = batchGateway.getStockSummaryPerItemWithNames();
        assertTrue(result.isEmpty());
    }

    @Test
    void testArchiveBatch_Success() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        batchGateway.archiveBatch(sampleBatch, "Paracetamol");
        verify(preparedStatement).setString(1, "ITEM001");
        verify(preparedStatement).setString(2, "Paracetamol");
        verify(preparedStatement).setInt(3, 100);
        verify(preparedStatement).setInt(4, 20);
        verify(preparedStatement).setDate(5, Date.valueOf(LocalDate.of(2025, 1, 1)));
        verify(preparedStatement).setDate(6, Date.valueOf(LocalDate.of(2025, 12, 31)));
    }

    @Test
    void testArchiveBatch_SQLException() throws SQLException {
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));
        batchGateway.archiveBatch(sampleBatch, "Paracetamol");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testRemoveExpiredBatches_Success() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("item_code")).thenReturn("ITEM001");
        when(resultSet.getString("name")).thenReturn("Paracetamol");
        when(resultSet.getDouble("selling_price")).thenReturn(10.50);
        when(resultSet.getInt("quantity")).thenReturn(100);
        when(resultSet.getInt("used_quantity")).thenReturn(20);
        when(resultSet.getDate("purchase_date")).thenReturn(Date.valueOf(LocalDate.of(2025, 1, 1)));
        when(resultSet.getDate("expiry_date")).thenReturn(Date.valueOf(LocalDate.of(2024, 12, 31)));
        when(preparedStatement.executeUpdate()).thenReturn(1);

        int result = batchGateway.removeExpiredBatches(LocalDate.of(2025, 1, 1));
        assertEquals(1, result);
    }

    @Test
    void testRemoveExpiredBatches_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));
        int result = batchGateway.removeExpiredBatches(LocalDate.of(2025, 1, 1));
        assertEquals(0, result);
    }

    @Test
    void testGetExpiredBatchesAll_Success() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("item_code")).thenReturn("ITEM001");
        when(resultSet.getString("name")).thenReturn("Paracetamol");
        when(resultSet.getDouble("selling_price")).thenReturn(10.50);
        when(resultSet.getInt("quantity")).thenReturn(100);
        when(resultSet.getInt("used_quantity")).thenReturn(20);
        when(resultSet.getDate("purchase_date")).thenReturn(Date.valueOf(LocalDate.of(2025, 1, 1)));
        when(resultSet.getDate("expiry_date")).thenReturn(Date.valueOf(LocalDate.of(2024, 12, 31)));

        List<BatchDTO> result = batchGateway.getExpiredBatchesAll(LocalDate.of(2025, 1, 1));
        assertEquals(1, result.size());
    }

    @Test
    void testGetExpiredBatchesAll_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));
        List<BatchDTO> result = batchGateway.getExpiredBatchesAll(LocalDate.of(2025, 1, 1));
        assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteExpiredBatch_BatchDTO_Success() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("name")).thenReturn("Paracetamol");
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = batchGateway.deleteExpiredBatch(sampleBatch);
        assertTrue(result);
    }

    @Test
    void testDeleteExpiredBatch_BatchDTO_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));
        boolean result = batchGateway.deleteExpiredBatch(sampleBatch);
        assertFalse(result);
    }

    @Test
    void testDeleteExpiredBatch_ItemCodePurchaseDate_Success() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean result = batchGateway.deleteExpiredBatch("ITEM001", LocalDate.of(2025, 1, 1));
        assertTrue(result);
    }

    @Test
    void testDeleteExpiredBatch_ItemCodePurchaseDate_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));
        boolean result = batchGateway.deleteExpiredBatch("ITEM001", LocalDate.of(2025, 1, 1));
        assertFalse(result);
    }

    @Test
    void testGetItemName_Success() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("name")).thenReturn("Paracetamol");

        Method method = BatchGateway.class.getDeclaredMethod("getItemName", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(batchGateway, "ITEM001");
        assertEquals("Paracetamol", result);
    }

    @Test
    void testGetItemName_NotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        Method method = BatchGateway.class.getDeclaredMethod("getItemName", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(batchGateway, "ITEM001");
        assertEquals("Unknown Item", result);
    }

    @Test
    void testGetItemName_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        Method method = BatchGateway.class.getDeclaredMethod("getItemName", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(batchGateway, "ITEM001");
        assertEquals("Unknown Item", result);
    }

    @Test
    void testGetArchivedExpiredBatches_Success() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("item_code")).thenReturn("ITEM001");
        when(resultSet.getString("item_name")).thenReturn("Paracetamol");
        when(resultSet.getInt("quantity")).thenReturn(100);
        when(resultSet.getInt("used_quantity")).thenReturn(20);
        when(resultSet.getDate("purchase_date")).thenReturn(Date.valueOf(LocalDate.of(2025, 1, 1)));
        when(resultSet.getDate("expiry_date")).thenReturn(Date.valueOf(LocalDate.of(2024, 12, 31)));

        List<BatchDTO> result = batchGateway.getArchivedExpiredBatches();
        assertEquals(1, result.size());
        assertEquals("ITEM001", result.get(0).getItemCode());
    }

    @Test
    void testGetArchivedExpiredBatches_Empty() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        List<BatchDTO> result = batchGateway.getArchivedExpiredBatches();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetArchivedExpiredBatches_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));
        List<BatchDTO> result = batchGateway.getArchivedExpiredBatches();
        assertTrue(result.isEmpty());
    }

    @Test
    void testClearArchivedExpiredBatches_Success() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean result = batchGateway.clearArchivedExpiredBatches();
        assertTrue(result);
    }

    @Test
    void testClearArchivedExpiredBatches_SQLException() throws Exception {
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));
        boolean result = batchGateway.clearArchivedExpiredBatches();
        assertFalse(result);
    }
}