package syos.data;

import syos.dto.ItemDTO;
import syos.interfaces.DatabaseConnectionProvider;
import syos.interfaces.OnlineItemDataAccess;
import syos.model.CartItem;
import syos.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OnlineItemGateway implementing OnlineItemDataAccess interface
 * Follows SOLID principles for online item data operations
 */
public class OnlineItemGateway implements OnlineItemDataAccess {

    private static final Logger LOGGER = Logger.getLogger(OnlineItemGateway.class.getName());
    private final DatabaseConnectionProvider connectionProvider;

    // Constructor injection for DIP compliance
    public OnlineItemGateway(DatabaseConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    // Default constructor for backward compatibility
    public OnlineItemGateway() {
        this.connectionProvider = new DatabaseConnectionProvider() {
            @Override
            public Connection getConnection() throws Exception {
                return DatabaseConnection.getInstance().getConnection();
            }

            @Override
            public Connection getPoolConnection() throws Exception {
                return DatabaseConnection.getInstance().getPoolConnection();
            }
        };
    }

    // Implementation of DataAccessInterface methods

    @Override
    public boolean insert(ItemDTO item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        String sql = "INSERT INTO online_inventory (item_code, name, price, quantity, reorder_level) VALUES (?, ?, ?, ?, 10)";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getCode());
            stmt.setString(2, item.getName());
            stmt.setDouble(3, item.getPrice());
            stmt.setInt(4, item.getQuantity());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inserting item: " + item.getCode(), e);
            return false;
        }
    }

    @Override
    public ItemDTO findById(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Item code cannot be null or empty");
        }

        String sql = "SELECT * FROM online_inventory WHERE item_code = ?";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ItemDTO(
                            rs.getString("item_code"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"));
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding item by code: " + code, e);
        }

        return null;
    }

    @Override
    public List<ItemDTO> findAll() {
        List<ItemDTO> items = new ArrayList<>();
        String sql = "SELECT item_code, name, price, quantity FROM online_inventory ORDER BY name";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ItemDTO item = new ItemDTO(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"));
                items.add(item);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all items", e);
        }

        return items;
    }

    @Override
    public boolean update(ItemDTO item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        String sql = "UPDATE online_inventory SET name = ?, price = ?, quantity = ? WHERE item_code = ?";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getName());
            stmt.setDouble(2, item.getPrice());
            stmt.setInt(3, item.getQuantity());
            stmt.setString(4, item.getCode());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating item: " + item.getCode(), e);
            return false;
        }
    }

    @Override
    public boolean delete(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Item code cannot be null or empty");
        }

        String sql = "DELETE FROM online_inventory WHERE item_code = ?";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting item: " + code, e);
            return false;
        }
    }

    // Implementation of OnlineItemDataAccess specific methods

    @Override
    public List<ItemDTO> getAllItems() {
        return findAll(); // Delegate to base method
    }

    @Override
    public ItemDTO getItemByCode(String code) {
        return findById(code); // Delegate to base method
    }

    @Override
    public void reduceStock(String code, int quantity) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Item code cannot be null or empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        String sql = "UPDATE online_inventory SET quantity = quantity - ? WHERE item_code = ?";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setString(2, code);
            stmt.executeUpdate();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error reducing stock for item: " + code, e);
            throw new RuntimeException("Failed to reduce stock for item: " + code, e);
        }
    }

    @Override
    public void increaseStock(String itemCode, int amount) {
        if (itemCode == null || itemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Item code cannot be null or empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        String sql = "UPDATE online_inventory SET quantity = quantity + ? WHERE item_code = ?";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, amount);
            stmt.setString(2, itemCode);
            stmt.executeUpdate();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error increasing stock for item: " + itemCode, e);
            throw new RuntimeException("Failed to increase stock for item: " + itemCode, e);
        }
    }

    @Override
    public boolean updateItemPrice(String itemCode, double newPrice) {
        if (itemCode == null || itemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Item code cannot be null or empty");
        }
        if (newPrice < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        String sql = "UPDATE online_inventory SET price = ? WHERE item_code = ?";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newPrice);
            stmt.setString(2, itemCode);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating price for item: " + itemCode, e);
            return false;
        }
    }

    @Override
    public boolean reduceStockBatch(List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart items cannot be null or empty");
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = connectionProvider.getPoolConnection();
            conn.setAutoCommit(false);

            String sql = "UPDATE online_inventory SET quantity = quantity - ? WHERE item_code = ?";
            stmt = conn.prepareStatement(sql);

            for (CartItem cartItem : cartItems) {
                if (cartItem == null || cartItem.getItem() == null) {
                    throw new IllegalArgumentException("Cart item or item cannot be null");
                }

                stmt.setInt(1, cartItem.getQuantity());
                stmt.setString(2, cartItem.getItem().getCode());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            conn.commit();

            // Check if all operations were successful
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in batch stock reduction", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error during rollback", rollbackEx);
                }
            }
            return false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                    LOGGER.log(Level.WARNING, "Error closing statement", ignored);
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException ignored) {
                    LOGGER.log(Level.WARNING, "Error closing connection", ignored);
                }
            }
        }
    }
}
