package syos.data;

import syos.model.OnlineUser;
import syos.util.DatabaseConnection;

import java.sql.*;

public class OnlineUserGateway {

    public boolean registerUser(OnlineUser user) {
        if (isUsernameTaken(user.getUsername()))
            return false;

        String query = "INSERT INTO online_users (username, password, contact_number, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getContactNumber());
            stmt.setString(4, user.getAddress());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error registering online user: " + e.getMessage());
            return false;
        }
    }

    public boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM online_users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("Error authenticating user: " + e.getMessage());
        }
        return false;
    }

    public boolean isUsernameTaken(String username) {
        String query = "SELECT * FROM online_users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("Error checking username availability: " + e.getMessage());
        }
        return false;
    }

    public OnlineUser getUserByUsername(String username) {
        String query = "SELECT * FROM online_users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new OnlineUser(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("contact_number"),
                        rs.getString("address"));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving user: " + e.getMessage());
        }
        return null;
    }

    public boolean saveUser(OnlineUser user) {
        String query = "INSERT INTO online_users (username, password) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error saving user: " + e.getMessage());
        }
        return false;
    }
}
