package syos.data;

import syos.dto.ItemDTO;
import syos.model.CartItem;
import syos.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OnlineItemGateway {

    public List<ItemDTO> getAllItems() {
        List<ItemDTO> items = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getPoolConnection();
            String sql = "SELECT item_code, name, price, quantity FROM online_inventory ORDER BY name";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                ItemDTO item = new ItemDTO(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"));
                items.add(item);
            }
        } catch (SQLException e) {

        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
        }
        return items;
    }

    public boolean insertItem(ItemDTO item) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getInstance().getPoolConnection();
            String sql = "INSERT INTO online_inventory (item_code, name, price, quantity, reorder_level) VALUES (?, ?, ?, ?, 10)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, item.getCode());
            stmt.setString(2, item.getName());
            stmt.setDouble(3, item.getPrice());
            stmt.setInt(4, item.getQuantity());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            return false;
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
        }
    }

    public boolean updateItemPrice(String itemCode, double newPrice) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getInstance().getPoolConnection();
            String sql = "UPDATE online_inventory SET price = ? WHERE item_code = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, newPrice);
            stmt.setString(2, itemCode);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
        }
    }

    public ItemDTO getItemByCode(String code) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getPoolConnection();
            String sql = "SELECT * FROM online_inventory WHERE item_code = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return new ItemDTO(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"));
            }
        } catch (SQLException e) {

        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
        }
        return null;
    }

    public void reduceStock(String code, int quantity) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getInstance().getPoolConnection();
            String sql = "UPDATE online_inventory SET quantity = quantity - ? WHERE item_code = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, quantity);
            stmt.setString(2, code);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // optional: log error
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
        }
    }

    public void increaseStock(String itemCode, int amount) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getInstance().getPoolConnection();
            String sql = "UPDATE online_inventory SET quantity = quantity + ? WHERE item_code = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, amount);
            stmt.setString(2, itemCode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // optional: log error
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
        }
    }

    public boolean reduceStockBatch(List<CartItem> cartItems) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getInstance().getPoolConnection();
            conn.setAutoCommit(false);

            String sql = "UPDATE online_inventory SET quantity = quantity - ? WHERE item_code = ?";
            stmt = conn.prepareStatement(sql);

            for (CartItem cartItem : cartItems) {
                stmt.setInt(1, cartItem.getQuantity());
                stmt.setString(2, cartItem.getItem().getCode());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            conn.commit();

            return results.length == cartItems.size();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            return false;
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
        }
    }
}
