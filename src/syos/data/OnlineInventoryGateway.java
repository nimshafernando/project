package syos.data;

import syos.dto.ItemDTO;
import syos.interfaces.OnlineInventoryDataAccess;
import syos.interfaces.DatabaseConnectionProvider;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OnlineInventoryGateway implementing OnlineInventoryDataAccess interface
 * Follows SOLID principles for online inventory data operations
 */
public class OnlineInventoryGateway implements OnlineInventoryDataAccess {

    private final DatabaseConnectionProvider connectionProvider;

    // Constructor injection for DIP compliance
    public OnlineInventoryGateway(DatabaseConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    // Default constructor for backward compatibility
    public OnlineInventoryGateway() {
        this.connectionProvider = DatabaseConnection.getInstance();
    }

    // Implementation of DataAccessInterface methods

    @Override
    public boolean insert(ItemDTO item) {
        try (Connection conn = connectionProvider.getPoolConnection()) {
            String sql = "INSERT INTO online_inventory (code, name, price, quantity, reorder_level) VALUES (?, ?, ?, ?, 10)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, item.getCode());
            stmt.setString(2, item.getName());
            stmt.setDouble(3, item.getPrice());
            stmt.setInt(4, item.getQuantity());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error inserting online item: " + e.getMessage());
            return false;
        }
    }

    @Override
    public ItemDTO findById(String code) {
        return getOnlineItemByCode(code); // Delegate to interface method
    }

    @Override
    public List<ItemDTO> findAll() {
        return getAllOnlineItems(); // Delegate to interface method
    }

    @Override
    public boolean update(ItemDTO item) {
        try (Connection conn = connectionProvider.getPoolConnection()) {
            String sql = "UPDATE online_inventory SET name = ?, price = ?, quantity = ? WHERE code = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, item.getName());
            stmt.setDouble(2, item.getPrice());
            stmt.setInt(3, item.getQuantity());
            stmt.setString(4, item.getCode());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error updating online item: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String code) {
        try (Connection conn = connectionProvider.getPoolConnection()) {
            String sql = "DELETE FROM online_inventory WHERE code = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error deleting online item: " + e.getMessage());
            return false;
        }
    }

    // Implementation of OnlineInventoryDataAccess specific methods

    @Override
    public List<ItemDTO> getAllOnlineItems() {
        List<ItemDTO> items = new ArrayList<>();
        try (Connection conn = connectionProvider.getPoolConnection()) {
            String sql = "SELECT * FROM online_inventory ORDER BY name";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String code = rs.getString("code");
                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");

                ItemDTO item = new ItemDTO(code, name, price, quantity);
                items.add(item);
            }
        } catch (Exception e) {
            System.out.println("Error fetching online items: " + e.getMessage());
        }
        return items;
    }

    @Override
    public ItemDTO getOnlineItemByCode(String code) {
        try (Connection conn = connectionProvider.getPoolConnection()) {
            String sql = "SELECT * FROM online_inventory WHERE code = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");

                return new ItemDTO(code, name, price, quantity);
            }
        } catch (Exception e) {
            System.out.println("Error finding online item by code: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean reduceOnlineStock(String code, int quantity) {
        try (Connection conn = connectionProvider.getPoolConnection()) {
            String sql = "UPDATE online_inventory SET quantity = quantity - ? WHERE code = ? AND quantity >= ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, quantity);
            stmt.setString(2, code);
            stmt.setInt(3, quantity);

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error reducing online stock: " + e.getMessage());
        }
        return false;
    }
}
