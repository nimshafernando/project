package syos.data;

import syos.interfaces.DatabaseConnectionProvider;
import syos.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class EmployeeGatewayTest {

    @Mock
    private DatabaseConnectionProvider connectionProvider;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private EmployeeGateway employeeGateway;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionProvider.getPoolConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void testAuthenticateEmployee_Success() throws SQLException {
        // Arrange
        String employeeId = "EMP001";
        String pin = "1234";
        String name = "John Smith";
        String role = "Pharmacist";

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("employee_id")).thenReturn(employeeId);
        when(resultSet.getString("name")).thenReturn(name);
        when(resultSet.getString("pin")).thenReturn(pin);
        when(resultSet.getString("role")).thenReturn(role);

        // Act
        Employee result = employeeGateway.authenticateEmployee(employeeId, pin);

        // Assert
        assertNotNull(result);
        assertEquals(employeeId, result.getEmployeeId());
        assertEquals(name, result.getName());
        assertEquals(pin, result.getPin());
        assertEquals(role, result.getRole());

        verify(preparedStatement).setString(1, employeeId);
        verify(preparedStatement).setString(2, pin);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testAuthenticateEmployee_InvalidCredentials() throws SQLException {
        // Arrange
        String employeeId = "EMP001";
        String pin = "9999";

        when(resultSet.next()).thenReturn(false);

        // Act
        Employee result = employeeGateway.authenticateEmployee(employeeId, pin);

        // Assert
        assertNull(result);
        verify(preparedStatement).setString(1, employeeId);
        verify(preparedStatement).setString(2, pin);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testAuthenticateEmployee_DatabaseException() throws Exception {
        // Arrange
        String employeeId = "EMP001";
        String pin = "1234";

        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database connection failed"));

        // Act
        Employee result = employeeGateway.authenticateEmployee(employeeId, pin);

        // Assert
        assertNull(result);
    }

    @Test
    void testAuthenticateEmployee_NullConnection() throws Exception {
        // Arrange
        String employeeId = "EMP001";
        String pin = "1234";

        when(connectionProvider.getPoolConnection()).thenReturn(null);

        // Act
        Employee result = employeeGateway.authenticateEmployee(employeeId, pin);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetEmployeeById_Success() throws SQLException {
        // Arrange
        String employeeId = "EMP002";
        String name = "Sarah Johnson";
        String pin = "5678";
        String role = "Cashier";

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("employee_id")).thenReturn(employeeId);
        when(resultSet.getString("name")).thenReturn(name);
        when(resultSet.getString("pin")).thenReturn(pin);
        when(resultSet.getString("role")).thenReturn(role);

        // Act
        Employee result = employeeGateway.getEmployeeById(employeeId);

        // Assert
        assertNotNull(result);
        assertEquals(employeeId, result.getEmployeeId());
        assertEquals(name, result.getName());
        assertEquals(pin, result.getPin());
        assertEquals(role, result.getRole());

        verify(preparedStatement).setString(1, employeeId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetEmployeeById_NotFound() throws SQLException {
        // Arrange
        String employeeId = "EMP999";

        when(resultSet.next()).thenReturn(false);

        // Act
        Employee result = employeeGateway.getEmployeeById(employeeId);

        // Assert
        assertNull(result);
        verify(preparedStatement).setString(1, employeeId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetEmployeeById_DatabaseException() throws Exception {
        // Arrange
        String employeeId = "EMP001";

        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        Employee result = employeeGateway.getEmployeeById(employeeId);

        // Assert
        assertNull(result);
    }

    @Test
    void testEmployeeExists_True() throws SQLException {
        // Arrange
        String employeeId = "EMP003";

        when(resultSet.next()).thenReturn(true);

        // Act
        boolean result = employeeGateway.employeeExists(employeeId);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, employeeId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testEmployeeExists_False() throws SQLException {
        // Arrange
        String employeeId = "EMP999";

        when(resultSet.next()).thenReturn(false);

        // Act
        boolean result = employeeGateway.employeeExists(employeeId);

        // Assert
        assertFalse(result);
        verify(preparedStatement).setString(1, employeeId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testEmployeeExists_DatabaseException() throws Exception {
        // Arrange
        String employeeId = "EMP001";

        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database connection failed"));

        // Act
        boolean result = employeeGateway.employeeExists(employeeId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDefaultConstructor() {
        // Act
        EmployeeGateway gateway = new EmployeeGateway();

        // Assert
        assertNotNull(gateway);
    }
}
