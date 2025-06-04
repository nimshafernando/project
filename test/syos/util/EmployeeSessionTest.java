package syos.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeSessionTest {

    private EmployeeSession session;

    @BeforeEach
    void setUp() {
        session = EmployeeSession.getInstance();
        session.logout(); // Ensure clean state
    }

    @Test
    @DisplayName("Should be singleton")
    void testSingleton() {
        EmployeeSession session1 = EmployeeSession.getInstance();
        EmployeeSession session2 = EmployeeSession.getInstance();

        assertSame(session1, session2);
    }

    @Test
    @DisplayName("Should login employee")
    void testLoginEmployee() {
        // Act
        session.loginEmployee("John Smith", "1234", "Cashier");

        // Assert
        assertTrue(session.isLoggedIn());
        assertEquals("John Smith", session.getCurrentEmployeeName());
        assertEquals("1234", session.getEmployeeId());
        assertEquals("Cashier", session.getRole());
    }

    @Test
    @DisplayName("Should logout employee")
    void testLogoutEmployee() {
        // Arrange
        session.loginEmployee("John Smith", "1234", "Cashier");

        // Act
        session.logout();

        // Assert
        assertFalse(session.isLoggedIn());
        assertNull(session.getCurrentEmployeeName());
        assertNull(session.getEmployeeId());
        assertNull(session.getRole());
    }

    @Test
    @DisplayName("Should validate session when logged in")
    void testValidateSessionLoggedIn() {
        // Arrange
        session.loginEmployee("John Smith", "1234", "Cashier");

        // Act & Assert
        assertDoesNotThrow(() -> session.validateSession());
    }

    @Test
    @DisplayName("Should throw exception when validating without login")
    void testValidateSessionNotLoggedIn() {
        assertThrows(IllegalStateException.class, () -> session.validateSession());
    }

    @Test
    @DisplayName("Should return formatted employee info")
    void testGetEmployeeInfo() {
        // When not logged in
        assertEquals("No employee logged in", session.getEmployeeInfo());

        // When logged in
        session.loginEmployee("John Smith", "1234", "Cashier");
        assertEquals("John Smith (1234) - Cashier", session.getEmployeeInfo());
    }
}