package syos.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test coverage for Employee model class
 * Tests all constructors, methods, authentication, null object pattern, and
 * edge cases
 */
class EmployeeTest {

    private Employee employee;
    private Employee.NullEmployee nullEmployee;

    @BeforeEach
    void setUp() {
        employee = new Employee("EMP001", "John Smith", "1234", "Manager");
        nullEmployee = Employee.NullEmployee.getInstance();
    }

    // === Constructor Tests ===

    @Test
    @DisplayName("Should create employee with all fields")
    void testEmployeeConstructor() {
        // Act & Assert
        assertEquals("EMP001", employee.getEmployeeId());
        assertEquals("John Smith", employee.getName());
        assertEquals("1234", employee.getPin());
        assertEquals("Manager", employee.getRole());
        assertTrue(employee.isActive());
    }

    @Test
    @DisplayName("Should create employee with empty values")
    void testEmployeeConstructorWithEmptyValues() {
        // Arrange & Act
        Employee emptyEmployee = new Employee("", "", "", "");

        // Assert
        assertEquals("", emptyEmployee.getEmployeeId());
        assertEquals("", emptyEmployee.getName());
        assertEquals("", emptyEmployee.getPin());
        assertEquals("", emptyEmployee.getRole());
        assertTrue(emptyEmployee.isActive());
    }

    @Test
    @DisplayName("Should create employee with null values")
    void testEmployeeConstructorWithNullValues() {
        // Arrange & Act
        Employee nullFieldEmployee = new Employee(null, null, null, null);

        // Assert
        assertNull(nullFieldEmployee.getEmployeeId());
        assertNull(nullFieldEmployee.getName());
        assertNull(nullFieldEmployee.getPin());
        assertNull(nullFieldEmployee.getRole());
        assertTrue(nullFieldEmployee.isActive());
    }

    @Test
    @DisplayName("Should create employee with special characters")
    void testEmployeeConstructorWithSpecialCharacters() {
        // Arrange & Act
        Employee specialEmployee = new Employee("EMP@#$", "José María", "!@#$", "Manager/Supervisor");

        // Assert
        assertEquals("EMP@#$", specialEmployee.getEmployeeId());
        assertEquals("José María", specialEmployee.getName());
        assertEquals("!@#$", specialEmployee.getPin());
        assertEquals("Manager/Supervisor", specialEmployee.getRole());
        assertTrue(specialEmployee.isActive());
    }

    // === Getter Tests ===

    @Test
    @DisplayName("Should return correct employee ID")
    void testGetEmployeeId() {
        assertEquals("EMP001", employee.getEmployeeId());
    }

    @Test
    @DisplayName("Should return correct name")
    void testGetName() {
        assertEquals("John Smith", employee.getName());
    }

    @Test
    @DisplayName("Should return correct PIN")
    void testGetPin() {
        assertEquals("1234", employee.getPin());
    }

    @Test
    @DisplayName("Should return correct role")
    void testGetRole() {
        assertEquals("Manager", employee.getRole());
    }

    @Test
    @DisplayName("Should return active status")
    void testIsActive() {
        assertTrue(employee.isActive());
    }

    // === Authentication Tests ===

    @Test
    @DisplayName("Should authenticate with correct PIN")
    void testAuthenticatePinCorrect() {
        assertTrue(employee.authenticatePin("1234"));
    }

    @Test
    @DisplayName("Should not authenticate with incorrect PIN")
    void testAuthenticatePinIncorrect() {
        assertFalse(employee.authenticatePin("5678"));
        assertFalse(employee.authenticatePin("wrong"));
        assertFalse(employee.authenticatePin(""));
    }

    @Test
    @DisplayName("Should not authenticate with null PIN")
    void testAuthenticatePinNull() {
        assertFalse(employee.authenticatePin(null));
    }

    @Test
    @DisplayName("Should handle null stored PIN")
    void testAuthenticatePinWithNullStoredPin() {
        // Arrange
        Employee nullPinEmployee = new Employee("EMP002", "Jane Doe", null, "Cashier");

        // Act & Assert
        assertFalse(nullPinEmployee.authenticatePin("1234"));
        assertFalse(nullPinEmployee.authenticatePin(null));
    }

    @Test
    @DisplayName("Should authenticate empty PIN if stored PIN is empty")
    void testAuthenticateEmptyPin() {
        // Arrange
        Employee emptyPinEmployee = new Employee("EMP003", "Bob Wilson", "", "Clerk");

        // Act & Assert
        assertTrue(emptyPinEmployee.authenticatePin(""));
        assertFalse(emptyPinEmployee.authenticatePin("1234"));
    }

    @Test
    @DisplayName("Should be case sensitive for PIN authentication")
    void testAuthenticatePinCaseSensitive() {
        // Arrange
        Employee letterPinEmployee = new Employee("EMP004", "Alice Brown", "AbCd", "Supervisor");

        // Act & Assert
        assertTrue(letterPinEmployee.authenticatePin("AbCd"));
        assertFalse(letterPinEmployee.authenticatePin("abcd"));
        assertFalse(letterPinEmployee.authenticatePin("ABCD"));
    }

    @Test
    @DisplayName("Should authenticate special character PIN")
    void testAuthenticateSpecialCharacterPin() {
        // Arrange
        Employee specialPinEmployee = new Employee("EMP005", "Charlie Davis", "!@#$%", "Manager");

        // Act & Assert
        assertTrue(specialPinEmployee.authenticatePin("!@#$%"));
        assertFalse(specialPinEmployee.authenticatePin("12345"));
    }

    // === Display Name Tests ===

    @Test
    @DisplayName("Should return name as display name when name is not null")
    void testGetDisplayNameWithName() {
        assertEquals("John Smith", employee.getDisplayName());
    }

    @Test
    @DisplayName("Should return employee ID when name is null")
    void testGetDisplayNameWithNullName() {
        // Arrange
        Employee nullNameEmployee = new Employee("EMP002", null, "5678", "Cashier");

        // Act & Assert
        assertEquals("Employee EMP002", nullNameEmployee.getDisplayName());
    }

    @Test
    @DisplayName("Should return employee ID when name is empty")
    void testGetDisplayNameWithEmptyName() {
        // Arrange
        Employee emptyNameEmployee = new Employee("EMP003", "", "9012", "Clerk");

        // Act & Assert
        assertEquals("", emptyNameEmployee.getDisplayName());
    }

    @Test
    @DisplayName("Should handle null employee ID in display name")
    void testGetDisplayNameWithNullEmployeeId() {
        // Arrange
        Employee nullIdEmployee = new Employee(null, null, "1111", "Staff");

        // Act & Assert
        assertEquals("Employee null", nullIdEmployee.getDisplayName());
    }

    // === Null Object Pattern Tests ===

    @Test
    @DisplayName("Should return singleton NullEmployee instance")
    void testNullEmployeeSingleton() {
        Employee.NullEmployee instance1 = Employee.NullEmployee.getInstance();
        Employee.NullEmployee instance2 = Employee.NullEmployee.getInstance();

        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Should have correct NullEmployee properties")
    void testNullEmployeeProperties() {
        assertEquals("0000", nullEmployee.getEmployeeId());
        assertEquals("Unknown", nullEmployee.getName());
        assertEquals("", nullEmployee.getPin());
        assertEquals("", nullEmployee.getRole());
        assertFalse(nullEmployee.isActive());
    }

    @Test
    @DisplayName("Should never authenticate NullEmployee")
    void testNullEmployeeAuthentication() {
        assertFalse(nullEmployee.authenticatePin(""));
        assertFalse(nullEmployee.authenticatePin("1234"));
        assertFalse(nullEmployee.authenticatePin(null));
        assertFalse(nullEmployee.authenticatePin("any_pin"));
    }

    @Test
    @DisplayName("Should return guest display name for NullEmployee")
    void testNullEmployeeDisplayName() {
        assertEquals("Guest Employee", nullEmployee.getDisplayName());
    }

    @Test
    @DisplayName("Should always be inactive for NullEmployee")
    void testNullEmployeeIsActive() {
        assertFalse(nullEmployee.isActive());
    }

    // === Safe Employee Factory Method Tests ===

    @Test
    @DisplayName("Should return same employee when valid and active")
    void testSafeEmployeeWithValidEmployee() {
        Employee result = Employee.safeEmployee(employee);
        assertSame(employee, result);
    }

    @Test
    @DisplayName("Should return NullEmployee when input is null")
    void testSafeEmployeeWithNull() {
        Employee result = Employee.safeEmployee(null);
        assertSame(Employee.NullEmployee.getInstance(), result);
    }

    @Test
    @DisplayName("Should return NullEmployee when employee is inactive")
    void testSafeEmployeeWithInactiveEmployee() {
        // Arrange - Create a custom inactive employee by extending Employee
        Employee inactiveEmployee = new Employee("EMP999", "Inactive User", "0000", "Former") {
            @Override
            public boolean isActive() {
                return false;
            }
        };

        // Act
        Employee result = Employee.safeEmployee(inactiveEmployee);

        // Assert
        assertSame(Employee.NullEmployee.getInstance(), result);
    }

    @Test
    @DisplayName("Should handle NullEmployee input to safeEmployee")
    void testSafeEmployeeWithNullEmployeeInput() {
        Employee result = Employee.safeEmployee(nullEmployee);
        assertSame(Employee.NullEmployee.getInstance(), result);
    }

    // === Edge Case Tests ===

    @Test
    @DisplayName("Should handle very long strings")
    void testVeryLongStrings() {
        // Arrange
        String longString = "A".repeat(1000);
        Employee longEmployee = new Employee(longString, longString, longString, longString);

        // Act & Assert
        assertEquals(longString, longEmployee.getEmployeeId());
        assertEquals(longString, longEmployee.getName());
        assertEquals(longString, longEmployee.getPin());
        assertEquals(longString, longEmployee.getRole());
        assertTrue(longEmployee.authenticatePin(longString));
    }

    @Test
    @DisplayName("Should handle whitespace in PIN")
    void testWhitespaceInPin() {
        // Arrange
        Employee whitespaceEmployee = new Employee("EMP006", "Space User", " 1 2 3 4 ", "Tester");

        // Act & Assert
        assertTrue(whitespaceEmployee.authenticatePin(" 1 2 3 4 "));
        assertFalse(whitespaceEmployee.authenticatePin("1234"));
        assertFalse(whitespaceEmployee.authenticatePin(" 1234 "));
    }

    @Test
    @DisplayName("Should handle numeric strings in all fields")
    void testNumericStrings() {
        // Arrange
        Employee numericEmployee = new Employee("12345", "67890", "0000", "1111");

        // Act & Assert
        assertEquals("12345", numericEmployee.getEmployeeId());
        assertEquals("67890", numericEmployee.getName());
        assertTrue(numericEmployee.authenticatePin("0000"));
        assertEquals("67890", numericEmployee.getDisplayName());
    }

    // === Business Logic Tests ===

    @Test
    @DisplayName("Should support different employee roles")
    void testDifferentEmployeeRoles() {
        // Arrange & Act
        Employee manager = new Employee("MGR001", "Manager User", "1111", "Manager");
        Employee cashier = new Employee("CSH001", "Cashier User", "2222", "Cashier");
        Employee supervisor = new Employee("SUP001", "Supervisor User", "3333", "Supervisor");
        Employee clerk = new Employee("CLK001", "Clerk User", "4444", "Clerk");

        // Assert
        assertEquals("Manager", manager.getRole());
        assertEquals("Cashier", cashier.getRole());
        assertEquals("Supervisor", supervisor.getRole());
        assertEquals("Clerk", clerk.getRole());

        // All should be active
        assertTrue(manager.isActive());
        assertTrue(cashier.isActive());
        assertTrue(supervisor.isActive());
        assertTrue(clerk.isActive());
    }

    @Test
    @DisplayName("Should handle real-world employee scenarios")
    void testRealWorldScenarios() {
        // Scenario 1: Manager with complex name
        Employee manager = new Employee("MGR-2024-001", "María José González-Smith", "Secure123!", "Store Manager");
        assertTrue(manager.authenticatePin("Secure123!"));
        assertEquals("María José González-Smith", manager.getDisplayName());

        // Scenario 2: Part-time cashier
        Employee partTime = new Employee("PT-CSH-045", "Alex Johnson", "quick99", "Part-time Cashier");
        assertTrue(partTime.authenticatePin("quick99"));
        assertFalse(partTime.authenticatePin("Quick99"));

        // Scenario 3: Security guard with numeric PIN
        Employee security = new Employee("SEC-001", "Robert Wilson", "987654", "Security Guard");
        assertTrue(security.authenticatePin("987654"));
        assertFalse(security.authenticatePin("123456"));
    }

    @Test
    @DisplayName("Should maintain object immutability where expected")
    void testImmutability() {
        // Arrange
        String originalId = employee.getEmployeeId();
        String originalName = employee.getName();
        String originalPin = employee.getPin();
        String originalRole = employee.getRole();

        // Act - Try to authenticate with different values
        employee.authenticatePin("wrong_pin");
        String displayName = employee.getDisplayName();

        // Assert - Original values should remain unchanged
        assertEquals(originalId, employee.getEmployeeId());
        assertEquals(originalName, employee.getName());
        assertEquals(originalPin, employee.getPin());
        assertEquals(originalRole, employee.getRole());
        assertTrue(employee.isActive());
    }
}
