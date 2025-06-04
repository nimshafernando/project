package syos.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class OnlineUserTest {

    @Test
    @DisplayName("Should create online user with full constructor")
    void testCreateFullUser() {
        // Arrange & Act
        OnlineUser user = new OnlineUser(1, "testuser", "test@email.com",
                "1234567890", "123 Test St");

        // Assert
        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@email.com", user.getEmail());
        assertEquals("1234567890", user.getContactNumber());
        assertEquals("123 Test St", user.getAddress());
    }

    @Test
    @DisplayName("Should create online user for login")
    void testCreateLoginUser() {
        // Arrange & Act
        OnlineUser user = new OnlineUser("testuser", "password123");

        // Assert
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
    }

    @Test
    @DisplayName("Should validate password match")
    void testPasswordMatch() {
        // Arrange
        OnlineUser user = new OnlineUser("testuser", "password123");

        // Act & Assert
        assertTrue(user.isPasswordMatch("password123"));
        assertFalse(user.isPasswordMatch("wrongpassword"));
        assertFalse(user.isPasswordMatch(null));
    }

    @Test
    @DisplayName("Should validate user")
    void testIsValid() {
        // Arrange
        OnlineUser validUser = new OnlineUser("testuser", "password123");
        OnlineUser invalidUser1 = new OnlineUser("", "password123");
        OnlineUser invalidUser2 = new OnlineUser("testuser", "");

        // Act & Assert
        assertTrue(validUser.isValid());
        assertFalse(invalidUser1.isValid());
        assertFalse(invalidUser2.isValid());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullHandling() {
        // Arrange
        OnlineUser user = new OnlineUser("testuser");
        user.setEmail(null);
        user.setContactNumber(null);
        user.setAddress(null);

        // Act & Assert
        assertEquals("", user.getEmail());
        assertEquals("", user.getContactNumber());
        assertEquals("", user.getAddress());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        OnlineUser user1 = new OnlineUser(1, "testuser", "test@email.com",
                "1234567890", "123 Test St");
        OnlineUser user2 = new OnlineUser(1, "testuser", "different@email.com",
                "0987654321", "456 Other St");
        OnlineUser user3 = new OnlineUser(2, "otheruser", "other@email.com",
                "1111111111", "789 Another St");

        // Act & Assert
        assertEquals(user1, user2); // Same ID and username
        assertNotEquals(user1, user3); // Different ID
        assertEquals(user1.hashCode(), user2.hashCode());
    }
}