package syos.data;

import syos.interfaces.DatabaseConnectionProvider;
import syos.model.OnlineUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OnlineUserGatewayTest {

    @Mock
    private DatabaseConnectionProvider connectionProvider;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private OnlineUserGateway onlineUserGateway;

    private OnlineUser sampleUser;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionProvider.getPoolConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Sample online user for testing
        sampleUser = new OnlineUser("john.doe@email.com", "securePassword123", "09123456789", "123 Main St, Manila");
    }

    @Test
    void testInsert_Success() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(1);
        // Mock isUsernameTaken to return false (username not taken)
        when(resultSet.next()).thenReturn(false);

        // Act
        boolean result = onlineUserGateway.insert(sampleUser);

        // Assert
        assertTrue(result);
        verify(preparedStatement, times(2)).setString(1, "john.doe@email.com"); // First for check, second for insert
        verify(preparedStatement).setString(2, "securePassword123");
        verify(preparedStatement).setString(3, "09123456789");
        verify(preparedStatement).setString(4, "123 Main St, Manila");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInsert_UsernameTaken() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true); // Username is already taken

        // Act
        boolean result = onlineUserGateway.insert(sampleUser);

        // Assert
        assertFalse(result);
        verify(preparedStatement).setString(1, "john.doe@email.com");
        verify(preparedStatement).executeQuery(); // Only the username check query
        verify(preparedStatement, never()).executeUpdate(); // Insert should not be called
    }

    @Test
    void testInsert_NullUser() {
        // Act
        boolean result = onlineUserGateway.insert(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testInsert_SQLException() throws Exception {
        // Arrange
        when(resultSet.next()).thenReturn(false); // Username not taken
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = onlineUserGateway.insert(sampleUser);

        // Assert
        assertFalse(result);
    }

    @Test
    void testFindById_Success() throws SQLException {
        // Arrange
        String username = "jane.smith@email.com";
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("username")).thenReturn(username);
        when(resultSet.getString("password")).thenReturn("password456");
        when(resultSet.getString("contact_number")).thenReturn("09987654321");
        when(resultSet.getString("address")).thenReturn("456 Oak Ave, Quezon City");

        // Act
        OnlineUser result = onlineUserGateway.findById(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("password456", result.getPassword());
        assertEquals("09987654321", result.getContactNumber());
        assertEquals("456 Oak Ave, Quezon City", result.getAddress());

        verify(preparedStatement).setString(1, username);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindById_NotFound() throws SQLException {
        // Arrange
        String username = "nonexistent@email.com";
        when(resultSet.next()).thenReturn(false);

        // Act
        OnlineUser result = onlineUserGateway.findById(username);

        // Assert
        assertNull(result);
        verify(preparedStatement).setString(1, username);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindById_NullUsername() {
        // Act
        OnlineUser result = onlineUserGateway.findById(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindAll_Success() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("username")).thenReturn("user1@email.com", "user2@email.com");
        when(resultSet.getString("password")).thenReturn("pass1", "pass2");
        when(resultSet.getString("contact_number")).thenReturn("09111111111", "09222222222");
        when(resultSet.getString("address")).thenReturn("Address 1", "Address 2");

        // Act
        List<OnlineUser> result = onlineUserGateway.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        OnlineUser user1 = result.get(0);
        assertEquals("user1@email.com", user1.getUsername());
        assertEquals("pass1", user1.getPassword());
        assertEquals("09111111111", user1.getContactNumber());
        assertEquals("Address 1", user1.getAddress());

        OnlineUser user2 = result.get(1);
        assertEquals("user2@email.com", user2.getUsername());
        assertEquals("pass2", user2.getPassword());
        assertEquals("09222222222", user2.getContactNumber());
        assertEquals("Address 2", user2.getAddress());

        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindAll_Empty() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(false);

        // Act
        List<OnlineUser> result = onlineUserGateway.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindAll_SQLException() throws Exception {
        // Arrange
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        List<OnlineUser> result = onlineUserGateway.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdate_Success() throws SQLException {
        // Arrange
        OnlineUser updatedUser = new OnlineUser("john.doe@email.com", "newPassword123", "09999888777", "New Address");
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = onlineUserGateway.update(updatedUser);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, "newPassword123");
        verify(preparedStatement).setString(2, "09999888777");
        verify(preparedStatement).setString(3, "New Address");
        verify(preparedStatement).setString(4, "john.doe@email.com");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdate_Failure() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = onlineUserGateway.update(sampleUser);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdate_NullUser() {
        // Act
        boolean result = onlineUserGateway.update(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testUpdate_NullUsername() {
        // Arrange
        OnlineUser userWithNullUsername = new OnlineUser(null, "password");

        // Act
        boolean result = onlineUserGateway.update(userWithNullUsername);

        // Assert
        assertFalse(result);
    }

    @Test
    void testUpdate_SQLException() throws Exception {
        // Arrange
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = onlineUserGateway.update(sampleUser);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDelete_Success() throws SQLException {
        // Arrange
        String username = "user.to.delete@email.com";
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = onlineUserGateway.delete(username);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, username);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDelete_Failure() throws SQLException {
        // Arrange
        String username = "nonexistent@email.com";
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = onlineUserGateway.delete(username);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDelete_NullUsername() {
        // Act
        boolean result = onlineUserGateway.delete(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDelete_EmptyUsername() {
        // Act
        boolean result = onlineUserGateway.delete("");

        // Assert
        assertFalse(result);
    }

    @Test
    void testDelete_SQLException() throws Exception {
        // Arrange
        String username = "test@email.com";
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = onlineUserGateway.delete(username);

        // Assert
        assertFalse(result);
    }

    @Test
    void testRegisterUser_DelegatesToInsert() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(resultSet.next()).thenReturn(false); // Username not taken

        // Act
        boolean result = onlineUserGateway.registerUser(sampleUser);

        // Assert
        assertTrue(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testAuthenticateUser_Success() throws SQLException {
        // Arrange
        String username = "test@email.com";
        String password = "correctPassword";
        when(resultSet.next()).thenReturn(true);

        // Act
        boolean result = onlineUserGateway.authenticateUser(username, password);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, username);
        verify(preparedStatement).setString(2, password);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testAuthenticateUser_InvalidCredentials() throws SQLException {
        // Arrange
        String username = "test@email.com";
        String password = "wrongPassword";
        when(resultSet.next()).thenReturn(false);

        // Act
        boolean result = onlineUserGateway.authenticateUser(username, password);

        // Assert
        assertFalse(result);
        verify(preparedStatement).setString(1, username);
        verify(preparedStatement).setString(2, password);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testAuthenticateUser_NullCredentials() {
        // Act & Assert
        assertFalse(onlineUserGateway.authenticateUser(null, "password"));
        assertFalse(onlineUserGateway.authenticateUser("username", null));
        assertFalse(onlineUserGateway.authenticateUser(null, null));
    }

    @Test
    void testAuthenticateUser_SQLException() throws Exception {
        // Arrange
        String username = "test@email.com";
        String password = "password";
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = onlineUserGateway.authenticateUser(username, password);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsUsernameTaken_True() throws SQLException {
        // Arrange
        String username = "taken@email.com";
        when(resultSet.next()).thenReturn(true);

        // Act
        boolean result = onlineUserGateway.isUsernameTaken(username);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, username);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testIsUsernameTaken_False() throws SQLException {
        // Arrange
        String username = "available@email.com";
        when(resultSet.next()).thenReturn(false);

        // Act
        boolean result = onlineUserGateway.isUsernameTaken(username);

        // Assert
        assertFalse(result);
        verify(preparedStatement).setString(1, username);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testIsUsernameTaken_NullUsername() {
        // Act
        boolean result = onlineUserGateway.isUsernameTaken(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsUsernameTaken_EmptyUsername() {
        // Act
        boolean result = onlineUserGateway.isUsernameTaken("");

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsUsernameTaken_SQLException() throws Exception {
        // Arrange
        String username = "test@email.com";
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = onlineUserGateway.isUsernameTaken(username);

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetUserByUsername_Success() throws SQLException {
        // Arrange
        String username = "existing@email.com";
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("username")).thenReturn(username);
        when(resultSet.getString("password")).thenReturn("userPassword");
        when(resultSet.getString("contact_number")).thenReturn("09555666777");
        when(resultSet.getString("address")).thenReturn("User Address");

        // Act
        OnlineUser result = onlineUserGateway.getUserByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("userPassword", result.getPassword());
        assertEquals("09555666777", result.getContactNumber());
        assertEquals("User Address", result.getAddress());

        verify(preparedStatement).setString(1, username);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetUserByUsername_NotFound() throws SQLException {
        // Arrange
        String username = "notfound@email.com";
        when(resultSet.next()).thenReturn(false);

        // Act
        OnlineUser result = onlineUserGateway.getUserByUsername(username);

        // Assert
        assertNull(result);
        verify(preparedStatement).setString(1, username);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetUserByUsername_NullUsername() {
        // Act
        OnlineUser result = onlineUserGateway.getUserByUsername(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetUserByUsername_EmptyUsername() {
        // Act
        OnlineUser result = onlineUserGateway.getUserByUsername("");

        // Assert
        assertNull(result);
    }

    @Test
    void testSaveUser_Success() throws SQLException {
        // Arrange
        OnlineUser simpleUser = new OnlineUser("simple@email.com", "simplePassword");
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = onlineUserGateway.saveUser(simpleUser);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setString(1, "simple@email.com");
        verify(preparedStatement).setString(2, "simplePassword");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testSaveUser_Failure() throws SQLException {
        // Arrange
        OnlineUser simpleUser = new OnlineUser("simple@email.com", "simplePassword");
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = onlineUserGateway.saveUser(simpleUser);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testSaveUser_NullUser() {
        // Act
        boolean result = onlineUserGateway.saveUser(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testSaveUser_SQLException() throws Exception {
        // Arrange
        OnlineUser simpleUser = new OnlineUser("simple@email.com", "simplePassword");
        when(connectionProvider.getPoolConnection()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = onlineUserGateway.saveUser(simpleUser);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDefaultConstructor() {
        // Act
        OnlineUserGateway gateway = new OnlineUserGateway();

        // Assert
        assertNotNull(gateway);
    }
}
