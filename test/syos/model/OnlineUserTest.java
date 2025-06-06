package syos.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test coverage for OnlineUser model class
 * Tests all constructors, getters, setters, business logic, and edge cases
 */
class OnlineUserTest {

    private OnlineUser fullUser;
    private OnlineUser loginUser;
    private OnlineUser registrationUser;
    private OnlineUser usernameOnlyUser;

    @BeforeEach
    void setUp() {
        fullUser = new OnlineUser(1, "johndoe", "john@email.com", "+1234567890", "123 Main St, City");
        loginUser = new OnlineUser("alice_smith", "SecurePass123!");
        registrationUser = new OnlineUser("bob_wilson", "MyPassword456", "+0987654321", "456 Oak Ave");
        usernameOnlyUser = new OnlineUser("guest_user");
    } // === Constructor Tests ===

    @Test
    @DisplayName("Should create online user with full constructor")
    void testCreateFullUser() {
        // Act & Assert
        assertEquals(1, fullUser.getId());
        assertEquals("johndoe", fullUser.getUsername());
        assertEquals("john@email.com", fullUser.getEmail());
        assertEquals("+1234567890", fullUser.getContactNumber());
        assertEquals("123 Main St, City", fullUser.getAddress());
        assertEquals("", fullUser.getPassword()); // Password not set in full constructor
    }

    @Test
    @DisplayName("Should create online user for login")
    void testCreateLoginUser() {
        // Act & Assert
        assertEquals("alice_smith", loginUser.getUsername());
        assertEquals("SecurePass123!", loginUser.getPassword());
        assertEquals(0, loginUser.getId()); // Default ID
        assertEquals("", loginUser.getEmail());
        assertEquals("", loginUser.getContactNumber());
        assertEquals("", loginUser.getAddress());
    }

    @Test
    @DisplayName("Should create online user for registration")
    void testCreateRegistrationUser() {
        // Act & Assert
        assertEquals("bob_wilson", registrationUser.getUsername());
        assertEquals("MyPassword456", registrationUser.getPassword());
        assertEquals("+0987654321", registrationUser.getContactNumber());
        assertEquals("456 Oak Ave", registrationUser.getAddress());
        assertEquals(0, registrationUser.getId()); // Default ID
        assertEquals("", registrationUser.getEmail()); // Email not set in registration constructor
    }

    @Test
    @DisplayName("Should create online user with username only")
    void testCreateUsernameOnlyUser() {
        // Act & Assert
        assertEquals("guest_user", usernameOnlyUser.getUsername());
        assertEquals("", usernameOnlyUser.getPassword());
        assertEquals("", usernameOnlyUser.getEmail());
        assertEquals("", usernameOnlyUser.getContactNumber());
        assertEquals("", usernameOnlyUser.getAddress());
        assertEquals(0, usernameOnlyUser.getId());
    }

    @Test
    @DisplayName("Should create user with empty values")
    void testCreateUserWithEmptyValues() {
        // Arrange & Act
        OnlineUser emptyUser = new OnlineUser("", "");

        // Assert
        assertEquals("", emptyUser.getUsername());
        assertEquals("", emptyUser.getPassword());
        assertEquals("", emptyUser.getEmail());
        assertEquals("", emptyUser.getContactNumber());
        assertEquals("", emptyUser.getAddress());
    }

    @Test
    @DisplayName("Should create user with extreme values")
    void testCreateUserWithExtremeValues() {
        // Arrange
        String longUsername = "a".repeat(100);
        String longEmail = "user@" + "domain".repeat(50) + ".com";
        String longPhone = "+1" + "9".repeat(50);
        String longAddress = "123 " + "Very Long Street Name ".repeat(20) + "City";

        // Act
        OnlineUser extremeUser = new OnlineUser(Integer.MAX_VALUE, longUsername, longEmail, longPhone, longAddress);

        // Assert
        assertEquals(Integer.MAX_VALUE, extremeUser.getId());
        assertEquals(longUsername, extremeUser.getUsername());
        assertEquals(longEmail, extremeUser.getEmail());
        assertEquals(longPhone, extremeUser.getContactNumber());
        assertEquals(longAddress, extremeUser.getAddress());
    }

    // === Getter Tests ===

    @Test
    @DisplayName("Should return empty strings for null values")
    void testNullSafeGetters() {
        // Arrange
        OnlineUser user = new OnlineUser("testuser");
        user.setEmail(null);
        user.setContactNumber(null);
        user.setAddress(null);

        // Act & Assert
        assertEquals("", user.getEmail());
        assertEquals("", user.getContactNumber());
        assertEquals("", user.getAddress());
        assertEquals("", user.getPassword()); // Password null by default in username-only constructor
    }

    @Test
    @DisplayName("Should handle null username gracefully")
    void testNullUsernameHandling() {
        // Arrange
        OnlineUser user = new OnlineUser("testuser");
        user.setUsername(null);

        // Act & Assert
        assertEquals("", user.getUsername());
    } // === Setter Tests ===

    @Test
    @DisplayName("Should set and get ID correctly")
    void testSetAndGetId() {
        // Arrange
        OnlineUser user = new OnlineUser("testuser");

        // Act
        user.setId(999);

        // Assert
        assertEquals(999, user.getId());
    }

    @Test
    @DisplayName("Should set and get username correctly")
    void testSetAndGetUsername() {
        // Arrange
        OnlineUser user = new OnlineUser("olduser");

        // Act
        user.setUsername("newuser");

        // Assert
        assertEquals("newuser", user.getUsername());
    }

    @Test
    @DisplayName("Should set and get password correctly")
    void testSetAndGetPassword() {
        // Arrange
        OnlineUser user = new OnlineUser("testuser");

        // Act
        user.setPassword("NewSecurePassword!");

        // Assert
        assertEquals("NewSecurePassword!", user.getPassword());
    }

    @Test
    @DisplayName("Should set and get email correctly")
    void testSetAndGetEmail() {
        // Arrange
        OnlineUser user = new OnlineUser("testuser");

        // Act
        user.setEmail("newemail@domain.com");

        // Assert
        assertEquals("newemail@domain.com", user.getEmail());
    }

    @Test
    @DisplayName("Should set and get contact number correctly")
    void testSetAndGetContactNumber() {
        // Arrange
        OnlineUser user = new OnlineUser("testuser");

        // Act
        user.setContactNumber("+1555123456");

        // Assert
        assertEquals("+1555123456", user.getContactNumber());
    }

    @Test
    @DisplayName("Should set and get address correctly")
    void testSetAndGetAddress() {
        // Arrange
        OnlineUser user = new OnlineUser("testuser");

        // Act
        user.setAddress("789 New Address St, New City");

        // Assert
        assertEquals("789 New Address St, New City", user.getAddress());
    }

    // === Business Logic Tests ===

    @Test
    @DisplayName("Should validate password match")
    void testPasswordMatch() {
        // Act & Assert
        assertTrue(loginUser.isPasswordMatch("SecurePass123!"));
        assertFalse(loginUser.isPasswordMatch("wrongpassword"));
        assertFalse(loginUser.isPasswordMatch(null));
        assertFalse(loginUser.isPasswordMatch(""));
        assertFalse(loginUser.isPasswordMatch("SecurePass123")); // Missing exclamation
        assertFalse(loginUser.isPasswordMatch("securepass123!")); // Case sensitive
    }

    @Test
    @DisplayName("Should handle null password in isPasswordMatch")
    void testPasswordMatchWithNullPassword() {
        // Arrange
        OnlineUser userWithNullPassword = new OnlineUser("testuser");
        userWithNullPassword.setPassword(null);

        // Act & Assert
        assertFalse(userWithNullPassword.isPasswordMatch("anypassword"));
        assertFalse(userWithNullPassword.isPasswordMatch(null));
        assertFalse(userWithNullPassword.isPasswordMatch(""));
    }

    @Test
    @DisplayName("Should validate user")
    void testIsValid() {
        // Arrange
        OnlineUser validUser = new OnlineUser("testuser", "password123");
        OnlineUser invalidUser1 = new OnlineUser("", "password123");
        OnlineUser invalidUser2 = new OnlineUser("testuser", "");
        OnlineUser invalidUser3 = new OnlineUser(null, "password123");
        OnlineUser invalidUser4 = new OnlineUser("testuser", null);
        OnlineUser invalidUser5 = new OnlineUser("   ", "password123"); // Whitespace only
        OnlineUser invalidUser6 = new OnlineUser("testuser", "   "); // Whitespace only

        // Act & Assert
        assertTrue(validUser.isValid());
        assertFalse(invalidUser1.isValid());
        assertFalse(invalidUser2.isValid());
        assertFalse(invalidUser3.isValid());
        assertFalse(invalidUser4.isValid());
        assertFalse(invalidUser5.isValid());
        assertFalse(invalidUser6.isValid());
    }

    // === Utility Method Tests ===

    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        // Act
        String result = fullUser.toString();

        // Assert
        assertTrue(result.contains("OnlineUser"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("username='johndoe'"));
        assertTrue(result.contains("email='john@email.com'"));
    }

    @Test
    @DisplayName("Should handle null values gracefully in toString")
    void testToStringWithNullValues() {
        // Arrange
        OnlineUser user = new OnlineUser("testuser");
        user.setEmail(null);

        // Act
        String result = user.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("OnlineUser"));
        assertTrue(result.contains("username='testuser'"));
        assertTrue(result.contains("email=''"));
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void testEquals() {
        // Arrange
        OnlineUser user1 = new OnlineUser(1, "testuser", "test@email.com", "1234567890", "123 Test St");
        OnlineUser user2 = new OnlineUser(1, "testuser", "different@email.com", "0987654321", "456 Other St");
        OnlineUser user3 = new OnlineUser(2, "testuser", "test@email.com", "1234567890", "123 Test St");
        OnlineUser user4 = new OnlineUser(1, "otheruser", "test@email.com", "1234567890", "123 Test St");

        // Act & Assert
        assertEquals(user1, user2); // Same ID and username
        assertNotEquals(user1, user3); // Different ID
        assertNotEquals(user1, user4); // Different username
        assertEquals(user1, user1); // Reflexive
        assertNotEquals(user1, null); // Null comparison
        assertNotEquals(user1, "not a user"); // Different class
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void testHashCode() {
        // Arrange
        OnlineUser user1 = new OnlineUser(1, "testuser", "test@email.com", "1234567890", "123 Test St");
        OnlineUser user2 = new OnlineUser(1, "testuser", "different@email.com", "0987654321", "456 Other St");
        OnlineUser user3 = new OnlineUser(2, "otheruser", "test@email.com", "1234567890", "123 Test St");

        // Act & Assert
        assertEquals(user1.hashCode(), user2.hashCode()); // Equal objects must have equal hash codes
        assertNotEquals(user1.hashCode(), user3.hashCode()); // Different objects should have different hash codes
    }

    @Test
    @DisplayName("Should maintain equals and hashCode contract")
    void testEqualsHashCodeContract() {
        // Arrange
        OnlineUser user1 = new OnlineUser(5, "alice", "alice@test.com", "555-0101", "100 Main St");
        OnlineUser user2 = new OnlineUser(5, "alice", "alice@different.com", "555-9999", "999 Other St");

        // Act & Assert - If two objects are equal, they must have the same hash code
        if (user1.equals(user2)) {
            assertEquals(user1.hashCode(), user2.hashCode(),
                    "Equal objects must have equal hash codes");
        }
    }

    // === Edge Cases and Error Handling ===

    @Test
    @DisplayName("Should handle negative ID values")
    void testNegativeIdValues() {
        // Arrange & Act
        OnlineUser user = new OnlineUser(-1, "testuser", "test@email.com", "1234567890", "123 Test St");

        // Assert
        assertEquals(-1, user.getId());
        assertEquals("testuser", user.getUsername());
    }

    @Test
    @DisplayName("Should handle zero ID value")
    void testZeroIdValue() {
        // Arrange & Act
        OnlineUser user = new OnlineUser(0, "testuser", "test@email.com", "1234567890", "123 Test St");

        // Assert
        assertEquals(0, user.getId());
    }

    @Test
    @DisplayName("Should handle special characters in all fields")
    void testSpecialCharactersInFields() {
        // Arrange
        String specialUsername = "用戶名@#$%^&*()";
        String specialEmail = "test+special@domain-name.co.uk";
        String specialPhone = "+44 (0) 20-7946-0958";
        String specialAddress = "123 O'Connor St., Apt #2B, São Paulo";

        // Act
        OnlineUser user = new OnlineUser(1, specialUsername, specialEmail, specialPhone, specialAddress);

        // Assert
        assertEquals(specialUsername, user.getUsername());
        assertEquals(specialEmail, user.getEmail());
        assertEquals(specialPhone, user.getContactNumber());
        assertEquals(specialAddress, user.getAddress());
    }

    @Test
    @DisplayName("Should handle very long field values")
    void testVeryLongFieldValues() {
        // Arrange
        String veryLongUsername = "user_" + "x".repeat(1000);
        String veryLongEmail = "user@" + "domain".repeat(100) + ".com";

        // Act
        OnlineUser user = new OnlineUser(veryLongUsername, "password");
        user.setEmail(veryLongEmail);

        // Assert
        assertEquals(veryLongUsername, user.getUsername());
        assertEquals(veryLongEmail, user.getEmail());
    }
}