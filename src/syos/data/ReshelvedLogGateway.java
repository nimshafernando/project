package syos.data;

import syos.util.DatabaseConnection;
import syos.dto.ReshelvedLogDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReshelvedLogGateway {

    public enum StoreType {
        INSTORE, ONLINE
    }

    public void logReshelving(String itemCode, int quantity, StoreType storeType) {
        String sql = "INSERT INTO reshelved_log (item_code, quantity, store_type) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            stmt.setInt(2, quantity);
            stmt.setString(3, storeType.name());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.printf("Reshelving logged: %s - %d units moved to %s inventory\n",
                        itemCode, quantity, storeType.name().toLowerCase());
            }

        } catch (SQLException e) {
            System.out.println("Error logging reshelving activity: " + e.getMessage());
        }
    }

    // Overloaded method for backward compatibility
    public void logReshelving(String itemCode, int quantity) {
        logReshelving(itemCode, quantity, StoreType.INSTORE);
    }

    public List<ReshelvedLogDTO> getReshelveHistory(String itemCode) {
        List<ReshelvedLogDTO> logs = new ArrayList<>();
        String sql = "SELECT * FROM reshelved_log WHERE item_code = ? ORDER BY reshelved_at DESC";

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(new ReshelvedLogDTO(
                        rs.getInt("id"),
                        rs.getString("item_code"),
                        rs.getInt("quantity"),
                        rs.getString("store_type"),
                        rs.getTimestamp("reshelved_at").toLocalDateTime()));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching reshelve history: " + e.getMessage());
        }

        return logs;
    }

    public List<ReshelvedLogDTO> getAllReshelveHistory() {
        List<ReshelvedLogDTO> logs = new ArrayList<>();
        String sql = "SELECT * FROM reshelved_log ORDER BY reshelved_at DESC LIMIT 100";

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(new ReshelvedLogDTO(
                        rs.getInt("id"),
                        rs.getString("item_code"),
                        rs.getInt("quantity"),
                        rs.getString("store_type"),
                        rs.getTimestamp("reshelved_at").toLocalDateTime()));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching all reshelve history: " + e.getMessage());
        }

        return logs;
    }
}
