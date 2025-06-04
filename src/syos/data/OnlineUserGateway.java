package syos.data;

import syos.model.OnlineUser;
import syos.util.DatabaseConnection;
import syos.interfaces.OnlineUserDataAccess;
import syos.interfaces.DatabaseConnectionProvider;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

public class OnlineUserGateway implements OnlineUserDataAccess {

    private static final Logger LOGGER = Logger.getLogger(OnlineUserGateway.class.getName());
    private final DatabaseConnectionProvider connectionProvider;

    // Constructor with dependency injection (DIP compliance)
    public OnlineUserGateway(DatabaseConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    // Default constructor for backward compatibility
    public OnlineUserGateway() {
        this.connectionProvider = DatabaseConnection.getInstance();
    } // Implementation of DataAccessInterface methods

    @Override
    public boolean insert(OnlineUser user) {
        if (user == null) {
            LOGGER.warning("Cannot insert null OnlineUser");
            return false;
        }

        if (isUsernameTaken(user.getUsername())) {
            LOGGER.warning("Username already taken: " + user.getUsername());
            return false;
        }

        String query = "INSERT INTO online_users (username, password, contact_number, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getContactNumber());
            stmt.setString(4, user.getAddress());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("User inserted successfully: " + user.getUsername());
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inserting online user: " + user.getUsername(), e);
        }
        return false;
    }

    @Override
    public OnlineUser findById(String username) {
        return getUserByUsername(username);
    }

    @Override
    public List<OnlineUser> findAll() {
        List<OnlineUser> users = new ArrayList<>();
        String query = "SELECT * FROM online_users";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new OnlineUser(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("contact_number"),
                        rs.getString("address")));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching all online users", e);
        }
        return users;
    }

    @Override
    public boolean update(OnlineUser user) {
        if (user == null || user.getUsername() == null) {
            LOGGER.warning("Cannot update null OnlineUser or user without username");
            return false;
        }

        String query = "UPDATE online_users SET password = ?, contact_number = ?, address = ? WHERE username = ?";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getContactNumber());
            stmt.setString(3, user.getAddress());
            stmt.setString(4, user.getUsername());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("User updated successfully: " + user.getUsername());
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating online user: " + user.getUsername(), e);
        }
        return false;
    }

    @Override
    public boolean delete(String username) {
        if (username == null || username.trim().isEmpty()) {
            LOGGER.warning("Cannot delete user with null or empty username");
            return false;
        }

        String query = "DELETE FROM online_users WHERE username = ?";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("User deleted successfully: " + username);
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting online user: " + username, e);
        }
        return false;
    } // Implementation of OnlineUserDataAccess specific methods

    @Override
    public boolean registerUser(OnlineUser user) {
        return insert(user);
    }

    @Override
    public boolean authenticateUser(String username, String password) {
        if (username == null || password == null) {
            LOGGER.warning("Cannot authenticate with null username or password");
            return false;
        }

        String query = "SELECT * FROM online_users WHERE username = ? AND password = ?";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean authenticated = rs.next();
                if (authenticated) {
                    LOGGER.info("User authenticated successfully: " + username);
                } else {
                    LOGGER.warning("Authentication failed for user: " + username);
                }
                return authenticated;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error authenticating user: " + username, e);
        }
        return false;
    }

    @Override
    public boolean isUsernameTaken(String username) {
        if (username == null || username.trim().isEmpty()) {
            LOGGER.warning("Cannot check null or empty username");
            return false;
        }

        String query = "SELECT * FROM online_users WHERE username = ?";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking username availability: " + username, e);
        }
        return false;
    }

    @Override
    public OnlineUser getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            LOGGER.warning("Cannot get user with null or empty username");
            return null;
        }

        String query = "SELECT * FROM online_users WHERE username = ?";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new OnlineUser(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("contact_number"),
                            rs.getString("address"));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user: " + username, e);
        }
        return null;
    }

    @Override
    public boolean saveUser(OnlineUser user) {
        if (user == null) {
            LOGGER.warning("Cannot save null user");
            return false;
        }

        String query = "INSERT INTO online_users (username, password) VALUES (?, ?)";
        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("User saved successfully: " + user.getUsername());
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving user: " + user.getUsername(), e);
        }
        return false;
    }
}
