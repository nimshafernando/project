package syos.data;

import syos.dto.BatchDTO;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchGateway {

    public boolean insertBatch(BatchDTO batch) {
        String sql = "INSERT INTO batches (item_code, name, selling_price, quantity, purchase_date, expiry_date, used_quantity) VALUES (?, ?, ?, ?, ?, ?, 0)";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, batch.getItemCode());
            stmt.setString(2, batch.getName());
            stmt.setDouble(3, batch.getSellingPrice());
            stmt.setInt(4, batch.getQuantity());
            stmt.setDate(5, Date.valueOf(batch.getPurchaseDate()));
            stmt.setDate(6, Date.valueOf(batch.getExpiryDate()));

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.printf("Batch added: %s (%s) - Qty: %d, Price: Rs. %.2f, Expires: %s\n",
                        batch.getName(), batch.getItemCode(), batch.getQuantity(),
                        batch.getSellingPrice(), batch.getExpiryDate());
            }
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error inserting batch: " + e.getMessage());
            return false;
        }
    }

    public List<BatchDTO> getAvailableBatchesForItem(String itemCode) {
        List<BatchDTO> batches = new ArrayList<>();
        String sql = "SELECT * FROM batches WHERE item_code = ? AND quantity > used_quantity AND expiry_date > CURDATE() ORDER BY expiry_date ASC, purchase_date ASC";

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                batches.add(new BatchDTO(
                        rs.getInt("id"),
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getDouble("selling_price"),
                        rs.getInt("quantity"),
                        rs.getDate("purchase_date").toLocalDate(),
                        rs.getDate("expiry_date").toLocalDate(),
                        rs.getInt("used_quantity")));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching batches: " + e.getMessage());
        }

        return batches;
    }

    public boolean reduceBatchQuantity(String itemCode, LocalDate purchaseDate, int usedQty) {
        String sql = "UPDATE batches SET used_quantity = used_quantity + ?, quantity = quantity - ? WHERE item_code = ? AND purchase_date = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usedQty); // Increase used_quantity
            stmt.setInt(2, usedQty); // Decrease total quantity
            stmt.setString(3, itemCode);
            stmt.setDate(4, Date.valueOf(purchaseDate));

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.printf("Batch updated: Item %s, Used: +%d, Remaining: %d\n",
                        itemCode, usedQty, getRemainingQuantity(itemCode, purchaseDate));
            }

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error reducing batch quantity: " + e.getMessage());
            return false;
        }
    }

    private int getRemainingQuantity(String itemCode, LocalDate purchaseDate) {
        String sql = "SELECT quantity FROM batches WHERE item_code = ? AND purchase_date = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            stmt.setDate(2, Date.valueOf(purchaseDate));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("quantity");
            }
        } catch (SQLException e) {
            System.out.println("Error getting remaining quantity: " + e.getMessage());
        }
        return 0;
    }

    public List<BatchDTO> getExpiredBatchesWithItemNames(LocalDate today) {
        List<BatchDTO> expired = new ArrayList<>();
        String sql = """
                SELECT b.item_code, b.name, b.selling_price, b.quantity, b.used_quantity,
                       b.purchase_date, b.expiry_date
                FROM batches b
                WHERE b.expiry_date < ?
                ORDER BY b.expiry_date ASC
                """;
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(today));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BatchDTO batch = new BatchDTO(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getDouble("selling_price"),
                        rs.getInt("quantity"),
                        rs.getDate("purchase_date").toLocalDate(),
                        rs.getDate("expiry_date").toLocalDate());
                batch.setUsedQuantity(rs.getInt("used_quantity"));
                expired.add(batch);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching expired batches with names: " + e.getMessage());
        }
        return expired;
    }

    public List<BatchDTO> getExpiredBatchesAll(LocalDate today) {
        List<BatchDTO> expired = new ArrayList<>();
        String sql = "SELECT item_code, name, selling_price, quantity, used_quantity, purchase_date, expiry_date FROM batches WHERE expiry_date < ?";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(today));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BatchDTO batch = new BatchDTO(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getDouble("selling_price"),
                        rs.getInt("quantity"),
                        rs.getDate("purchase_date").toLocalDate(),
                        rs.getDate("expiry_date").toLocalDate());
                batch.setUsedQuantity(rs.getInt("used_quantity"));
                expired.add(batch);
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch expired batches.");
            e.printStackTrace();
        }
        return expired;
    }

    public void archiveBatch(BatchDTO batch, String itemName) {
        String sql = "INSERT INTO expired_items (item_code, item_name, quantity, used_quantity, purchase_date, expiry_date, removed_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, batch.getItemCode());
            stmt.setString(2, itemName);
            stmt.setInt(3, batch.getQuantity());
            stmt.setInt(4, batch.getUsedQuantity());
            stmt.setDate(5, Date.valueOf(batch.getPurchaseDate()));
            stmt.setDate(6, Date.valueOf(batch.getExpiryDate()));
            stmt.setTimestamp(7, Timestamp.valueOf(java.time.LocalDateTime.now()));
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Failed to archive expired batch: " + e.getMessage());
        }
    }

    public boolean deleteExpiredBatch(BatchDTO batch) {
        String itemName = getItemName(batch.getItemCode());
        archiveBatch(batch, itemName);

        String sql = "DELETE FROM batches WHERE item_code = ? AND purchase_date = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, batch.getItemCode());
            stmt.setDate(2, Date.valueOf(batch.getPurchaseDate()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Failed to delete specific expired batch: " + e.getMessage());
            return false;
        }
    }

    public int removeExpiredBatches(LocalDate today) {
        List<BatchDTO> expired = getExpiredBatchesAll(today);
        for (BatchDTO batch : expired) {
            String itemName = getItemName(batch.getItemCode());
            archiveBatch(batch, itemName);
        }

        String sql = "DELETE FROM batches WHERE expiry_date < ?";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(today));
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to delete expired batches.");
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Object[]> getStockSummaryPerItemWithNames() {
        Map<String, Object[]> summary = new HashMap<>();
        String sql = "SELECT b.item_code, i.name AS item_name, " +
                "SUM(b.quantity) AS total, SUM(b.used_quantity) AS used " +
                "FROM batches b " +
                "JOIN items i ON b.item_code = i.code " +
                "GROUP BY b.item_code, i.name";

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String itemCode = rs.getString("item_code");
                String itemName = rs.getString("item_name");
                int total = rs.getInt("total");
                int used = rs.getInt("used");
                int available = total - used;
                summary.put(itemCode, new Object[] { itemName, total, used, available });
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch stock summary with item names.");
            e.printStackTrace();
        }
        return summary;
    }

    public boolean deleteExpiredBatch(String itemCode, LocalDate purchaseDate) {
        String sql = "DELETE FROM batches WHERE item_code = ? AND purchase_date = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemCode);
            stmt.setDate(2, Date.valueOf(purchaseDate));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Failed to delete specific expired batch: " + e.getMessage());
            return false;
        }
    }

    private String getItemName(String itemCode) {
        String sql = "SELECT name FROM items WHERE code = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch item name: " + e.getMessage());
        }
        return "Unknown Item";
    }

    public List<BatchDTO> getArchivedExpiredBatches() {
        List<BatchDTO> batches = new ArrayList<>();
        String sql = "SELECT e.item_code, e.item_name, e.quantity, e.used_quantity, " +
                "e.purchase_date, e.expiry_date " +
                "FROM expired_items e " +
                "ORDER BY e.expiry_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Use the existing constructor with all required parameters
                BatchDTO batch = new BatchDTO(
                        rs.getString("item_code"),
                        rs.getString("item_name"),
                        0.0, // Default selling price for archived items
                        rs.getInt("quantity"),
                        rs.getDate("purchase_date").toLocalDate(),
                        rs.getDate("expiry_date").toLocalDate());
                batch.setUsedQuantity(rs.getInt("used_quantity"));
                batches.add(batch);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving archived expired batches: " + e.getMessage());
        }

        return batches;
    }

    public boolean clearArchivedExpiredBatches() {
        // Changed from 'expiry_items' to 'expired_items'
        String sql = "DELETE FROM expired_items";

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted >= 0;

        } catch (SQLException e) {
            System.err.println("Error clearing archived expired batches: " + e.getMessage());
            return false;
        }
    }
}
