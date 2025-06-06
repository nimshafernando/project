package syos.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Unit tests for EmployeeSession singleton class.
 * Covers happy paths and edge cases for login, logout, and validation.
 */
@DisplayName("EmployeeSession Tests")
class EmployeeSessionTest {

    private EmployeeSession session;

    @BeforeEach
    void setUp() {
        session = EmployeeSession.getInstance();
        session.logout(); // Ensure clean session before each test
    }

    @Test
    @DisplayName("Should return singleton instance")
    void testSingletonInstance() {
        EmployeeSession first = EmployeeSession.getInstance();
        EmployeeSession second = EmployeeSession.getInstance();
        assertNotNull(first);
        assertSame(first, second, "Should return the same instance");
    }

    @Test
    @DisplayName("Should login and set session fields")
    void testLoginEmployee() {
        session.loginEmployee("Alice", "EMP001", "Cashier");

        assertTrue(session.isLoggedIn(), "Should be logged in");
        assertEquals("Alice", session.getCurrentEmployeeName());
        assertEquals("EMP001", session.getEmployeeId());
        assertEquals("Cashier", session.getRole());
    }

    @Test
    @DisplayName("Should logout and clear session fields")
    void testLogout() {
        session.loginEmployee("Bob", "EMP002", "Manager");
        session.logout();

        assertFalse(session.isLoggedIn(), "Should be logged out");
        assertNull(session.getCurrentEmployeeName());
        assertNull(session.getEmployeeId());
        assertNull(session.getRole());
    }

    @Test
    @DisplayName("Should throw exception if session is invalid")
    void testValidateSession_Invalid() {
        session.logout(); // Ensure no session
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> session.validateSession(),
                "Should throw if no employee is logged in");
        assertTrue(exception.getMessage().contains("Please authenticate"));
    }

    @Test
    @DisplayName("Should not throw exception if session is valid")
    void testValidateSession_Valid() {
        session.loginEmployee("Charlie", "EMP003", "Supervisor");
        assertDoesNotThrow(() -> session.validateSession());
    }

    @Test
    @DisplayName("Should return correct formatted employee info")
    void testGetEmployeeInfo_WhenLoggedIn() {
        session.loginEmployee("Diana", "EMP004", "Stock");
        String expected = "Diana (EMP004) - Stock";
        assertEquals(expected, session.getEmployeeInfo());
    }

    @Test
    @DisplayName("Should return fallback info when not logged in")
    void testGetEmployeeInfo_WhenNotLoggedIn() {
        session.logout();
        assertEquals("No employee logged in", session.getEmployeeInfo());
    }

    // --- Edge Cases ---

    @Test
    @DisplayName("Should handle login with null values")
    void testLoginWithNullValues() {
        session.loginEmployee(null, null, null);
        assertTrue(session.isLoggedIn());
        assertNull(session.getCurrentEmployeeName());
        assertNull(session.getEmployeeId());
        assertNull(session.getRole());

        // Validate session should still throw due to null name
        assertThrows(IllegalStateException.class, () -> session.validateSession());
    }

    @Test
    @DisplayName("Should handle login with empty strings")
    void testLoginWithEmptyValues() {
        session.loginEmployee("", "", "");
        assertTrue(session.isLoggedIn());
        assertEquals("", session.getCurrentEmployeeName());
        assertEquals("", session.getEmployeeId());
        assertEquals("", session.getRole());

        // Should not throw because name is non-null
        assertDoesNotThrow(() -> session.validateSession());
    }

    @Test
    @DisplayName("Double logout should not crash")
    void testDoubleLogout() {
        session.loginEmployee("Eve", "EMP005", "Cashier");
        session.logout();
        assertFalse(session.isLoggedIn());

        // Second logout should be harmless
        assertDoesNotThrow(() -> session.logout());
    }
}
