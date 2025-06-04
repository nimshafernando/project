package syos.data;

import syos.interfaces.EmployeeDataAccess;
import syos.interfaces.DatabaseConnectionProvider;
import syos.model.Employee;
import syos.util.DatabaseConnection;
import java.sql.*;

/**
 * EmployeeGateway implementing EmployeeDataAccess interface
 * Follows SOLID principles for employee data operations
 */
public class EmployeeGateway implements EmployeeDataAccess {

    private final DatabaseConnectionProvider connectionProvider;

    // Constructor injection for DIP compliance
    public EmployeeGateway(DatabaseConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    // Default constructor for backward compatibility
    public EmployeeGateway() {
        this.connectionProvider = DatabaseConnection.getInstance();
    }

    /**
     * Authenticate employee by ID and PIN
     */
    @Override
    public Employee authenticateEmployee(String employeeId, String pin) {
        String query = "SELECT * FROM employees WHERE employee_id = ? AND pin = ? AND is_active = 1";

        try (Connection conn = connectionProvider.getPoolConnection();
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

        } catch (Exception e) {
            System.out.println("Error authenticating employee: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get employee by ID (without PIN verification)
     */
    @Override
    public Employee getEmployeeById(String employeeId) {
        String query = "SELECT * FROM employees WHERE employee_id = ? AND is_active = 1";

        try (Connection conn = connectionProvider.getPoolConnection();
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

        } catch (Exception e) {
            System.out.println("Error retrieving employee: " + e.getMessage());
        }

        return null;
    }

    /**
     * Check if employee ID exists
     */
    @Override
    public boolean employeeExists(String employeeId) {
        String query = "SELECT 1 FROM employees WHERE employee_id = ? AND is_active = 1";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            System.out.println("Error checking employee existence: " + e.getMessage());
        }

        return false;
    }
}
