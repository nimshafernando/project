package syos.data;

import syos.util.DatabaseConnection;
import syos.dto.ReshelvedLogDTO;
import syos.interfaces.ReshelvedLogDataAccess;
import syos.interfaces.DatabaseConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ReshelvedLogGateway implements ReshelvedLogDataAccess {
    private static final Logger LOGGER = Logger.getLogger(ReshelvedLogGateway.class.getName());
    private final DatabaseConnectionProvider connectionProvider;

    public enum StoreType {
        INSTORE, ONLINE
    }

    // Constructor with dependency injection (DIP compliance)
    public ReshelvedLogGateway(DatabaseConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    // Default constructor for backward compatibility
    public ReshelvedLogGateway() {
        this.connectionProvider = DatabaseConnection.getInstance();
    } // Implementation of DataAccessInterface methods

    @Override
    public boolean insert(ReshelvedLogDTO entity) {
        if (entity == null) {
            LOGGER.warning("Cannot insert null ReshelvedLogDTO");
            return false;
        }

        String sql = "INSERT INTO reshelved_log (item_code, quantity, store_type) VALUES (?, ?, ?)";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getItemCode());
            stmt.setInt(2, entity.getQuantity());
            stmt.setString(3, entity.getStoreType());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getInt(1));
                        LOGGER.info("Reshelved log inserted successfully with ID: " + entity.getId());
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inserting reshelved log", e);
        }
        return false;
    }

    @Override
    public ReshelvedLogDTO findById(Integer id) {
        if (id == null) {
            LOGGER.warning("Cannot find ReshelvedLogDTO with null ID");
            return null;
        }

        String sql = "SELECT * FROM reshelved_log WHERE id = ?";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ReshelvedLogDTO(
                            rs.getInt("id"),
                            rs.getString("item_code"),
                            rs.getInt("quantity"),
                            rs.getString("store_type"),
                            rs.getTimestamp("reshelved_at").toLocalDateTime());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding reshelved log by ID: " + id, e);
        }
        return null;
    }

    @Override
    public List<ReshelvedLogDTO> findAll() {
        return getAllReshelveHistory();
    }

    @Override
    public boolean update(ReshelvedLogDTO entity) {
        if (entity == null) {
            LOGGER.warning("Cannot update null ReshelvedLogDTO or entity without ID");
            return false;
        }

        String sql = "UPDATE reshelved_log SET item_code = ?, quantity = ?, store_type = ? WHERE id = ?";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getItemCode());
            stmt.setInt(2, entity.getQuantity());
            stmt.setString(3, entity.getStoreType());
            stmt.setInt(4, entity.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Reshelved log updated successfully: " + entity.getId());
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating reshelved log", e);
        }
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null) {
            LOGGER.warning("Cannot delete ReshelvedLogDTO with null ID");
            return false;
        }

        String sql = "DELETE FROM reshelved_log WHERE id = ?";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Reshelved log deleted successfully: " + id);
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting reshelved log: " + id, e);
        }
        return false;
    } // Implementation of ReshelvedLogDataAccess specific methods

    @Override
    public void logReshelving(String itemCode, int quantity, StoreType storeType) {
        if (itemCode == null || itemCode.trim().isEmpty()) {
            LOGGER.warning("Cannot log reshelving with null or empty item code");
            return;
        }

        String sql = "INSERT INTO reshelved_log (item_code, quantity, store_type) VALUES (?, ?, ?)";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            stmt.setInt(2, quantity);
            stmt.setString(3, storeType.name());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info(String.format("Reshelving logged: %s - %d units moved to %s inventory",
                        itemCode, quantity, storeType.name().toLowerCase()));
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error logging reshelving activity for item: " + itemCode, e);
        }
    }

    // Overloaded method for backward compatibility
    @Override
    public void logReshelving(String itemCode, int quantity) {
        logReshelving(itemCode, quantity, StoreType.INSTORE);
    }

    @Override
    public List<ReshelvedLogDTO> getReshelveHistory(String itemCode) {
        if (itemCode == null || itemCode.trim().isEmpty()) {
            LOGGER.warning("Cannot get reshelve history with null or empty item code");
            return new ArrayList<>();
        }

        List<ReshelvedLogDTO> logs = new ArrayList<>();
        String sql = "SELECT * FROM reshelved_log WHERE item_code = ? ORDER BY reshelved_at DESC";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new ReshelvedLogDTO(
                            rs.getInt("id"),
                            rs.getString("item_code"),
                            rs.getInt("quantity"),
                            rs.getString("store_type"),
                            rs.getTimestamp("reshelved_at").toLocalDateTime()));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching reshelve history for item: " + itemCode, e);
        }

        return logs;
    }

    @Override
    public List<ReshelvedLogDTO> getAllReshelveHistory() {
        List<ReshelvedLogDTO> logs = new ArrayList<>();
        String sql = "SELECT * FROM reshelved_log ORDER BY reshelved_at DESC LIMIT 100";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new ReshelvedLogDTO(
                            rs.getInt("id"),
                            rs.getString("item_code"),
                            rs.getInt("quantity"),
                            rs.getString("store_type"),
                            rs.getTimestamp("reshelved_at").toLocalDateTime()));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching all reshelve history", e);
        }

        return logs;
    }
}
