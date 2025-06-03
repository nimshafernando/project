package syos.data;

import syos.dto.ItemDTO;
import syos.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemGateway {

    public void reduceStock(String itemCode, int quantitySold) {
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection()) {
            String sql = "UPDATE items SET quantity = quantity - ? WHERE code = ? AND quantity >= ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, quantitySold);
            stmt.setString(2, itemCode);
            stmt.setInt(3, quantitySold); // ensure we don’t go negative
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                System.out.println(
                        "Warning: Unable to reduce stock for item " + itemCode + " (possibly not enough stock)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ItemDTO getItemByCode(String code) {
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection()) {
            String sql = "SELECT * FROM items WHERE code = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new ItemDTO(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"));
            } else {
                return null; // not found
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ItemDTO> getItemsBelowReorderLevel() {
        List<ItemDTO> lowStockItems = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection()) {
            String sql = "SELECT * FROM items WHERE quantity <= reorder_level";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ItemDTO item = new ItemDTO(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"));
                lowStockItems.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lowStockItems;
    }

    public void increaseStock(String itemCode, int amount) {
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection()) {
            String sql = "UPDATE items SET quantity = quantity + ? WHERE code = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, amount);
            stmt.setString(2, itemCode);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean insertItem(ItemDTO item) {
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection()) {
            String sql = "INSERT INTO items (code, name, price, quantity, reorder_level) VALUES (?, ?, ?, ?, 10)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, item.getCode());
            stmt.setString(2, item.getName());
            stmt.setDouble(3, item.getPrice());
            stmt.setInt(4, item.getQuantity());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error inserting item: " + e.getMessage());
            return false;
        }
    }

    public boolean updateItemPrice(String itemCode, double newPrice) {
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection()) {
            String sql = "UPDATE items SET price = ? WHERE code = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, newPrice);
            stmt.setString(2, itemCode);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.printf("Updated price for item %s to Rs. %.2f\n", itemCode, newPrice);
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error updating item price: " + e.getMessage());
            return false;
        }
    }

    public List<ItemDTO> getAllItems() {
        List<ItemDTO> items = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection()) {
            String sql = "SELECT code, name, price, quantity FROM items ORDER BY name";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ItemDTO item = new ItemDTO(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching items: " + e.getMessage());
        }
        return items;
    }

    /**
     * Get all items where quantity is below 50 (reorder alert threshold)
     * Used for reorder level reporting and inventory management
     *
     * @return List of items requiring reorder attention
     */
    public List<ItemDTO> getReorderAlerts() {
        List<ItemDTO> reorderItems = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection()) {
            String sql = "SELECT code, name, price, quantity FROM items WHERE quantity < 50 ORDER BY quantity ASC, name";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ItemDTO item = new ItemDTO(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"));
                reorderItems.add(item);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching reorder alerts: " + e.getMessage());
        }

        return reorderItems;
    }

    /**
     * Updates reorder level for all items in the items table.
     * Used by reorder level report to maintain consistent threshold values.
     *
     * @param newReorderLevel The new reorder level threshold
     * @return true if update was successful, false otherwise
     */
    public boolean updateAllReorderLevels(int newReorderLevel) {
        String sql = "UPDATE items SET reorder_level = ?";

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newReorderLevel);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.printf("Updated reorder level for %d in-store items to %d units\n",
                        rowsAffected, newReorderLevel);
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error updating in-store reorder levels: " + e.getMessage());
        }

        return false;
    }

    /**
     * Updates reorder level for a specific item in the items table.
     *
     * @param itemCode The item code to update
     * @param newReorderLevel The new reorder level
     * @return true if update was successful, false otherwise
     */
    public boolean updateItemReorderLevel(String itemCode, int newReorderLevel) {
        String sql = "UPDATE items SET reorder_level = ? WHERE code = ?";

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newReorderLevel);
            stmt.setString(2, itemCode);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.printf("Updated reorder level for item %s to %d units\n",
                        itemCode, newReorderLevel);
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error updating item reorder level: " + e.getMessage());
        }

        return false;
    }
}
