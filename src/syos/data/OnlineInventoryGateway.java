package syos.data;

import syos.dto.ItemDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OnlineInventoryGateway {

    private static final String URL = "jdbc:mysql://localhost:3306/syos";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public List<ItemDTO> getAllOnlineItems() {
        List<ItemDTO> items = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM online_inventory";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String code = rs.getString("code");
                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");

                // Use the correct constructor
                ItemDTO item = new ItemDTO(code, name, price, quantity);
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public ItemDTO getOnlineItemByCode(String code) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM online_inventory WHERE code = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");

                // Use the correct constructor
                return new ItemDTO(code, name, price, quantity);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean reduceOnlineStock(String code, int quantity) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "UPDATE online_inventory SET quantity = quantity - ? WHERE code = ? AND quantity >= ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, quantity);
            stmt.setString(2, code);
            stmt.setInt(3, quantity);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
