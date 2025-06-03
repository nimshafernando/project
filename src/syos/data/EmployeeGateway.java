package syos.data;

import syos.model.Employee;
import syos.util.DatabaseConnection;
import java.sql.*;

/**
 * Employee Gateway following Repository pattern
 * Handles all employee data access operations
 */
public class EmployeeGateway {

    /**
     * Authenticate employee by ID and PIN
     */
    public Employee authenticateEmployee(String employeeId, String pin) {
        String query = "SELECT * FROM employees WHERE employee_id = ? AND pin = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, employeeId);
            stmt.setString(2, pin);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Employee(
                        rs.getString("employee_id"),
                        rs.getString("name"),
                        rs.getString("pin"),
                        rs.getString("role"));
            }

        } catch (SQLException e) {
            System.out.println("Error authenticating employee: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get employee by ID (without PIN verification)
     */
    public Employee getEmployeeById(String employeeId) {
        String query = "SELECT * FROM employees WHERE employee_id = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Employee(
                        rs.getString("employee_id"),
                        rs.getString("name"),
                        rs.getString("pin"),
                        rs.getString("role"));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving employee: " + e.getMessage());
        }

        return null;
    }

    /**
     * Check if employee ID exists
     */
    public boolean employeeExists(String employeeId) {
        String query = "SELECT 1 FROM employees WHERE employee_id = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("Error checking employee existence: " + e.getMessage());
        }

        return false;
    }
}
