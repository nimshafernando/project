package syos.data;

import syos.dto.ItemDTO;
import syos.interfaces.DatabaseConnectionProvider;
import syos.interfaces.OnlineItemDataAccess;
import syos.model.CartItem;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.util.*;

/**
 * Refactored OnlineItemGateway
 */
public class OnlineItemGateway implements OnlineItemDataAccess {

    private final DatabaseConnectionProvider connectionProvider;

    public OnlineItemGateway(DatabaseConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public OnlineItemGateway() {
        this(DatabaseConnection.getInstance());
    }

    // === INSERT ===
    @Override
    public boolean insert(ItemDTO item) {
        validateItem(item);
        String sql = "INSERT INTO online_inventory (item_code, name, price, quantity, reorder_level) VALUES (?, ?, ?, ?, 10)";
        return executeUpdate(sql, item, OperationType.INSERT);
    }

    // === UPDATE ===
    @Override
    public boolean update(ItemDTO item) {
        validateItem(item);
        String sql = "UPDATE online_inventory SET name = ?, price = ?, quantity = ? WHERE item_code = ?";
        return executeUpdate(sql, item, OperationType.UPDATE);
    }

    // === DELETE ===
    @Override
    public boolean delete(String code) {
        validateCode(code);
        String sql = "DELETE FROM online_inventory WHERE item_code = ?";
        return executeUpdate(sql, code);
    }

    // === FIND BY ID ===
    @Override
    public ItemDTO findById(String code) {
        validateCode(code);
        String sql = "SELECT * FROM online_inventory WHERE item_code = ?";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapItem(rs) : null;

        } catch (Exception e) {
            return null;
        }
    }

    // === FIND ALL ===
    @Override
    public List<ItemDTO> findAll() {
        List<ItemDTO> items = new ArrayList<>();
        String sql = "SELECT item_code, name, price, quantity FROM online_inventory ORDER BY name";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(mapItem(rs));
            }

        } catch (Exception e) {
            // silently ignore
        }

        return items;
    }

    // === STOCK ADJUSTMENTS ===
    @Override
    public void reduceStock(String code, int quantity) {
        validateCode(code);
        validatePositive(quantity);
        updateStock("quantity = quantity - ?", code, quantity);
    }

    @Override
    public void increaseStock(String code, int quantity) {
        validateCode(code);
        validatePositive(quantity);
        updateStock("quantity = quantity + ?", code, quantity);
    }

    @Override
    public boolean updateItemPrice(String code, double price) {
        validateCode(code);
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative");

        String sql = "UPDATE online_inventory SET price = ? WHERE item_code = ?";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, price);
            stmt.setString(2, code);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            return false;
        }
    }

    // === BATCH REDUCTION ===
    @Override
    public boolean reduceStockBatch(List<CartItem> items) {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("Cart is empty");

        String sql = "UPDATE online_inventory SET quantity = quantity - ? WHERE item_code = ?";
        try (Connection conn = connectionProvider.getPoolConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (CartItem item : items) {
                    stmt.setInt(1, item.getQuantity());
                    stmt.setString(2, item.getItem().getCode());
                    stmt.addBatch();
                }

                int[] results = stmt.executeBatch();
                conn.commit();

                for (int r : results)
                    if (r <= 0)
                        return false;

                return true;
            }

        } catch (Exception e) {
            return false;
        }
    }

    // === Utility Methods ===
    private void updateStock(String expression, String code, int qty) {
        String sql = "UPDATE online_inventory SET " + expression + " WHERE item_code = ?";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, qty);
            stmt.setString(2, code);
            stmt.executeUpdate();

        } catch (Exception e) {
            // silently ignore
        }
    }

    private boolean executeUpdate(String sql, ItemDTO item, OperationType type) {
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (type == OperationType.INSERT) {
                stmt.setString(1, item.getCode());
                stmt.setString(2, item.getName());
                stmt.setDouble(3, item.getPrice());
                stmt.setInt(4, item.getQuantity());
            } else {
                stmt.setString(1, item.getName());
                stmt.setDouble(2, item.getPrice());
                stmt.setInt(3, item.getQuantity());
                stmt.setString(4, item.getCode());
            }

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            return false;
        }
    }

    private boolean executeUpdate(String sql, String code) {
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            return false;
        }
    }

    private ItemDTO mapItem(ResultSet rs) throws SQLException {
        return new ItemDTO(
                rs.getString("item_code"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getInt("quantity"));
    }

    private void validateItem(ItemDTO item) {
        if (item == null)
            throw new IllegalArgumentException("Item cannot be null");
    }

    private void validateCode(String code) {
        if (code == null || code.isEmpty())
            throw new IllegalArgumentException("Item code is invalid");
    }

    private void validatePositive(int value) {
        if (value <= 0)
            throw new IllegalArgumentException("Value must be positive");
    }

    // === Interface Delegates ===
    @Override
    public List<ItemDTO> getAllItems() {
        return findAll();
    }

    @Override
    public ItemDTO getItemByCode(String code) {
        return findById(code);
    }

    private enum OperationType {
        INSERT, UPDATE
    }
}
