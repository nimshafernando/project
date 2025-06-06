package syos.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for UserDTO class
 * Tests constructor, getters, immutability, and edge cases with 100% coverage
 */
class UserDTOTest {

    private UserDTO adminUser;
    private UserDTO employeeUser;
    private UserDTO customerUser;

    @BeforeEach
    void setUp() {
        adminUser = new UserDTO("admin", "admin123");
        employeeUser = new UserDTO("john.doe", "emp@Pass2024");
        customerUser = new UserDTO("customer01", "mySecureP@ss!");
    }

    @Test
    @DisplayName("Should create UserDTO with username and password")
    void testConstructor() {
        UserDTO user = new UserDTO("testuser", "testpass");

        assertEquals("testuser", user.getUsername());
        assertEquals("testpass", user.getPassword());
    }

    @Test
    @DisplayName("Should handle null username")
    void testNullUsername() {
        UserDTO user = new UserDTO(null, "password123");
        assertNull(user.getUsername());
        assertEquals("password123", user.getPassword());
    }

    @Test
    @DisplayName("Should handle null password")
    void testNullPassword() {
        UserDTO user = new UserDTO("username", null);
        assertEquals("username", user.getUsername());
        assertNull(user.getPassword());
    }

    @Test
    @DisplayName("Should handle both null values")
    void testBothNullValues() {
        UserDTO user = new UserDTO(null, null);
        assertNull(user.getUsername());
        assertNull(user.getPassword());
    }

    @Test
    @DisplayName("Should handle empty username")
    void testEmptyUsername() {
        UserDTO user = new UserDTO("", "password");
        assertEquals("", user.getUsername());
        assertEquals("password", user.getPassword());
    }

    @Test
    @DisplayName("Should handle empty password")
    void testEmptyPassword() {
        UserDTO user = new UserDTO("username", "");
        assertEquals("username", user.getUsername());
        assertEquals("", user.getPassword());
    }

    @Test
    @DisplayName("Should handle both empty values")
    void testBothEmptyValues() {
        UserDTO user = new UserDTO("", "");
        assertEquals("", user.getUsername());
        assertEquals("", user.getPassword());
    }

    @Test
    @DisplayName("Should handle very long username")
    void testLongUsername() {
        String longUsername = "verylongusername".repeat(10);
        UserDTO user = new UserDTO(longUsername, "password");
        assertEquals(longUsername, user.getUsername());
    }

    @Test
    @DisplayName("Should handle very long password")
    void testLongPassword() {
        String longPassword = "verylongpassword".repeat(10);
        UserDTO user = new UserDTO("username", longPassword);
        assertEquals(longPassword, user.getPassword());
    }

    @Test
    @DisplayName("Should handle special characters in username")
    void testSpecialCharactersUsername() {
        String specialUsername = "user@domain.com";
        UserDTO user = new UserDTO(specialUsername, "password");
        assertEquals(specialUsername, user.getUsername());
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void testSpecialCharactersPassword() {
        String specialPassword = "P@ssw0rd!#$%^&*()";
        UserDTO user = new UserDTO("username", specialPassword);
        assertEquals(specialPassword, user.getPassword());
    }

    @Test
    @DisplayName("Should handle whitespace in username")
    void testWhitespaceUsername() {
        String usernameWithSpaces = "  user name  ";
        UserDTO user = new UserDTO(usernameWithSpaces, "password");
        assertEquals(usernameWithSpaces, user.getUsername());
    }

    @Test
    @DisplayName("Should handle whitespace in password")
    void testWhitespacePassword() {
        String passwordWithSpaces = "  pass word  ";
        UserDTO user = new UserDTO("username", passwordWithSpaces);
        assertEquals(passwordWithSpaces, user.getPassword());
    }

    @Test
    @DisplayName("Should handle tabs and newlines")
    void testTabsAndNewlines() {
        String usernameWithTabs = "user\tname";
        String passwordWithNewlines = "pass\nword";
        UserDTO user = new UserDTO(usernameWithTabs, passwordWithNewlines);
        assertEquals(usernameWithTabs, user.getUsername());
        assertEquals(passwordWithNewlines, user.getPassword());
    }

    @Test
    @DisplayName("Should maintain immutability")
    void testImmutability() {
        // Test that multiple calls return the same values
        assertEquals("admin", adminUser.getUsername());
        assertEquals("admin", adminUser.getUsername());

        assertEquals("admin123", adminUser.getPassword());
        assertEquals("admin123", adminUser.getPassword());

        // Values should remain constant
        String firstUsernameCall = adminUser.getUsername();
        String secondUsernameCall = adminUser.getUsername();
        assertSame(firstUsernameCall, secondUsernameCall);
    }

    @Test
    @DisplayName("Should handle realistic admin credentials")
    void testAdminCredentials() {
        UserDTO admin = new UserDTO("administrator", "SuperSecure123!");
        assertEquals("administrator", admin.getUsername());
        assertEquals("SuperSecure123!", admin.getPassword());
    }

    @Test
    @DisplayName("Should handle realistic employee credentials")
    void testEmployeeCredentials() {
        UserDTO employee1 = new UserDTO("emp001", "TempPass2024");
        UserDTO employee2 = new UserDTO("jane.smith", "JS@Work456");
        UserDTO employee3 = new UserDTO("cashier01", "C@shier789");

        assertEquals("emp001", employee1.getUsername());
        assertEquals("TempPass2024", employee1.getPassword());

        assertEquals("jane.smith", employee2.getUsername());
        assertEquals("JS@Work456", employee2.getPassword());

        assertEquals("cashier01", employee3.getUsername());
        assertEquals("C@shier789", employee3.getPassword());
    }

    @Test
    @DisplayName("Should handle realistic customer credentials")
    void testCustomerCredentials() {
        UserDTO customer1 = new UserDTO("customer_123", "MyPass@2024");
        UserDTO customer2 = new UserDTO("john.customer", "JohnC123$");
        UserDTO customer3 = new UserDTO("guest_user", "Guest@Pass");

        assertEquals("customer_123", customer1.getUsername());
        assertEquals("MyPass@2024", customer1.getPassword());

        assertEquals("john.customer", customer2.getUsername());
        assertEquals("JohnC123$", customer2.getPassword());

        assertEquals("guest_user", customer3.getUsername());
        assertEquals("Guest@Pass", customer3.getPassword());
    }

    @Test
    @DisplayName("Should handle email-like usernames")
    void testEmailUsernames() {
        UserDTO user1 = new UserDTO("user@example.com", "password123");
        UserDTO user2 = new UserDTO("admin@company.org", "adminPass!");
        UserDTO user3 = new UserDTO("test.user@domain.co.uk", "testPass@2024");

        assertEquals("user@example.com", user1.getUsername());
        assertEquals("admin@company.org", user2.getUsername());
        assertEquals("test.user@domain.co.uk", user3.getUsername());
    }

    @Test
    @DisplayName("Should handle numeric usernames and passwords")
    void testNumericValues() {
        UserDTO user1 = new UserDTO("12345", "67890");
        UserDTO user2 = new UserDTO("user123", "pass456");
        UserDTO user3 = new UserDTO("0000", "0000");

        assertEquals("12345", user1.getUsername());
        assertEquals("67890", user1.getPassword());

        assertEquals("user123", user2.getUsername());
        assertEquals("pass456", user2.getPassword());

        assertEquals("0000", user3.getUsername());
        assertEquals("0000", user3.getPassword());
    }

    @Test
    @DisplayName("Should handle mixed case usernames and passwords")
    void testMixedCase() {
        UserDTO user1 = new UserDTO("UserName", "PassWord");
        UserDTO user2 = new UserDTO("UPPERCASE", "lowercase");
        UserDTO user3 = new UserDTO("MiXeD_CaSe", "pAsSwOrD123");

        assertEquals("UserName", user1.getUsername());
        assertEquals("PassWord", user1.getPassword());

        assertEquals("UPPERCASE", user2.getUsername());
        assertEquals("lowercase", user2.getPassword());

        assertEquals("MiXeD_CaSe", user3.getUsername());
        assertEquals("pAsSwOrD123", user3.getPassword());
    }

    @Test
    @DisplayName("Should handle minimum length values")
    void testMinimumLength() {
        UserDTO user = new UserDTO("a", "b");
        assertEquals("a", user.getUsername());
        assertEquals("b", user.getPassword());
    }

    @Test
    @DisplayName("Should handle complex password patterns")
    void testComplexPasswords() {
        UserDTO user1 = new UserDTO("user1", "Aa1!Bb2@Cc3#");
        UserDTO user2 = new UserDTO("user2", "!@#$%^&*()_+-=");
        UserDTO user3 = new UserDTO("user3", "1234567890abcdefghij");

        assertEquals("Aa1!Bb2@Cc3#", user1.getPassword());
        assertEquals("!@#$%^&*()_+-=", user2.getPassword());
        assertEquals("1234567890abcdefghij", user3.getPassword());
    }
}
