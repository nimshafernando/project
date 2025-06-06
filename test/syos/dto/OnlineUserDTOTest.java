package syos.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OnlineUserDTO Test Suite")
class OnlineUserDTOTest {

    private OnlineUserDTO onlineUserDTO;

    @BeforeEach
    void setUp() {
        // Setup is done in individual test methods due to multiple constructors
    }

    // Constructor tests - Basic login constructor (2-parameter)
    @Test
    @DisplayName("Should create OnlineUserDTO with basic constructor - admin user")
    void testBasicConstructor_AdminUser() {
        onlineUserDTO = new OnlineUserDTO("admin@store.com", "StrongP@ssw0rd!");

        assertEquals("admin@store.com", onlineUserDTO.getUsername());
        assertEquals("StrongP@ssw0rd!", onlineUserDTO.getPassword());
        assertEquals(0, onlineUserDTO.getId()); // Default value
        assertNull(onlineUserDTO.getContactNumber()); // Default value
        assertNull(onlineUserDTO.getAddress()); // Default value
    }

    @Test
    @DisplayName("Should create OnlineUserDTO with basic constructor - customer user")
    void testBasicConstructor_CustomerUser() {
        onlineUserDTO = new OnlineUserDTO("customer123", "mySecurePass");

        assertEquals("customer123", onlineUserDTO.getUsername());
        assertEquals("mySecurePass", onlineUserDTO.getPassword());
        assertEquals(0, onlineUserDTO.getId());
        assertNull(onlineUserDTO.getContactNumber());
        assertNull(onlineUserDTO.getAddress());
    }

    @Test
    @DisplayName("Should create OnlineUserDTO with basic constructor - employee user")
    void testBasicConstructor_EmployeeUser() {
        onlineUserDTO = new OnlineUserDTO("emp.john.smith", "EmployeePass2024");

        assertEquals("emp.john.smith", onlineUserDTO.getUsername());
        assertEquals("EmployeePass2024", onlineUserDTO.getPassword());
        assertEquals(0, onlineUserDTO.getId());
        assertNull(onlineUserDTO.getContactNumber());
        assertNull(onlineUserDTO.getAddress());
    }

    @Test
    @DisplayName("Should handle null values in basic constructor")
    void testBasicConstructor_NullValues() {
        onlineUserDTO = new OnlineUserDTO(null, null);

        assertNull(onlineUserDTO.getUsername());
        assertNull(onlineUserDTO.getPassword());
        assertEquals(0, onlineUserDTO.getId());
        assertNull(onlineUserDTO.getContactNumber());
        assertNull(onlineUserDTO.getAddress());
    }

    @Test
    @DisplayName("Should handle empty strings in basic constructor")
    void testBasicConstructor_EmptyStrings() {
        onlineUserDTO = new OnlineUserDTO("", "");

        assertEquals("", onlineUserDTO.getUsername());
        assertEquals("", onlineUserDTO.getPassword());
        assertEquals(0, onlineUserDTO.getId());
        assertNull(onlineUserDTO.getContactNumber());
        assertNull(onlineUserDTO.getAddress());
    }

    // Constructor tests - Full details constructor (5-parameter)
    @Test
    @DisplayName("Should create OnlineUserDTO with full constructor - complete customer profile")
    void testFullConstructor_CompleteCustomerProfile() {
        onlineUserDTO = new OnlineUserDTO(
                1001, "sarah.johnson@email.com", "SecurePass123!",
                "+1-555-0123", "123 Main St, Springfield, IL 62701");

        assertEquals(1001, onlineUserDTO.getId());
        assertEquals("sarah.johnson@email.com", onlineUserDTO.getUsername());
        assertEquals("SecurePass123!", onlineUserDTO.getPassword());
        assertEquals("+1-555-0123", onlineUserDTO.getContactNumber());
        assertEquals("123 Main St, Springfield, IL 62701", onlineUserDTO.getAddress());
    }

    @Test
    @DisplayName("Should create OnlineUserDTO with full constructor - business account")
    void testFullConstructor_BusinessAccount() {
        onlineUserDTO = new OnlineUserDTO(
                2002, "purchasing@abccompany.com", "CorpPass2024#",
                "+1-800-555-0199", "456 Business Blvd, Suite 100, Chicago, IL 60601");

        assertEquals(2002, onlineUserDTO.getId());
        assertEquals("purchasing@abccompany.com", onlineUserDTO.getUsername());
        assertEquals("CorpPass2024#", onlineUserDTO.getPassword());
        assertEquals("+1-800-555-0199", onlineUserDTO.getContactNumber());
        assertEquals("456 Business Blvd, Suite 100, Chicago, IL 60601", onlineUserDTO.getAddress());
    }

    @Test
    @DisplayName("Should create OnlineUserDTO with full constructor - international user")
    void testFullConstructor_InternationalUser() {
        onlineUserDTO = new OnlineUserDTO(
                3003, "user@international.co.uk", "GlobalPass!2024",
                "+44-20-7946-0958", "10 Downing Street, London SW1A 2AA, UK");

        assertEquals(3003, onlineUserDTO.getId());
        assertEquals("user@international.co.uk", onlineUserDTO.getUsername());
        assertEquals("GlobalPass!2024", onlineUserDTO.getPassword());
        assertEquals("+44-20-7946-0958", onlineUserDTO.getContactNumber());
        assertEquals("10 Downing Street, London SW1A 2AA, UK", onlineUserDTO.getAddress());
    }

    @Test
    @DisplayName("Should handle null string values in full constructor")
    void testFullConstructor_NullStringValues() {
        onlineUserDTO = new OnlineUserDTO(0, null, null, null, null);

        assertEquals(0, onlineUserDTO.getId());
        assertNull(onlineUserDTO.getUsername());
        assertNull(onlineUserDTO.getPassword());
        assertNull(onlineUserDTO.getContactNumber());
        assertNull(onlineUserDTO.getAddress());
    }

    @Test
    @DisplayName("Should handle empty strings in full constructor")
    void testFullConstructor_EmptyStrings() {
        onlineUserDTO = new OnlineUserDTO(1, "", "", "", "");

        assertEquals(1, onlineUserDTO.getId());
        assertEquals("", onlineUserDTO.getUsername());
        assertEquals("", onlineUserDTO.getPassword());
        assertEquals("", onlineUserDTO.getContactNumber());
        assertEquals("", onlineUserDTO.getAddress());
    }

    @Test
    @DisplayName("Should handle negative ID in full constructor")
    void testFullConstructor_NegativeId() {
        onlineUserDTO = new OnlineUserDTO(-999, "testuser", "testpass", "123456789", "Test Address");

        assertEquals(-999, onlineUserDTO.getId());
        assertEquals("testuser", onlineUserDTO.getUsername());
        assertEquals("testpass", onlineUserDTO.getPassword());
        assertEquals("123456789", onlineUserDTO.getContactNumber());
        assertEquals("Test Address", onlineUserDTO.getAddress());
    }

    // Getter tests
    @Test
    @DisplayName("Should return correct ID from getter")
    void testGetId() {
        onlineUserDTO = new OnlineUserDTO(12345, "testuser", "testpass", "1234567890", "Test Address");
        assertEquals(12345, onlineUserDTO.getId());
    }

    @Test
    @DisplayName("Should return correct username from getter")
    void testGetUsername() {
        onlineUserDTO = new OnlineUserDTO("unique.username@domain.com", "password123");
        assertEquals("unique.username@domain.com", onlineUserDTO.getUsername());
    }

    @Test
    @DisplayName("Should return correct password from getter")
    void testGetPassword() {
        onlineUserDTO = new OnlineUserDTO("testuser", "ComplexP@ssw0rd!");
        assertEquals("ComplexP@ssw0rd!", onlineUserDTO.getPassword());
    }

    @Test
    @DisplayName("Should return correct contact number from getter")
    void testGetContactNumber() {
        onlineUserDTO = new OnlineUserDTO(1, "user", "pass", "+1-555-987-6543", "Address");
        assertEquals("+1-555-987-6543", onlineUserDTO.getContactNumber());
    }

    @Test
    @DisplayName("Should return correct address from getter")
    void testGetAddress() {
        String address = "789 Oak Avenue, Apartment 4B, New York, NY 10001";
        onlineUserDTO = new OnlineUserDTO(1, "user", "pass", "1234567890", address);
        assertEquals(address, onlineUserDTO.getAddress());
    }

    // Setter tests - only ID has a setter
    @Test
    @DisplayName("Should set ID correctly")
    void testSetId() {
        onlineUserDTO = new OnlineUserDTO("testuser", "testpass");
        onlineUserDTO.setId(999);
        assertEquals(999, onlineUserDTO.getId());
    }

    @Test
    @DisplayName("Should set ID to negative value")
    void testSetId_NegativeValue() {
        onlineUserDTO = new OnlineUserDTO("testuser", "testpass");
        onlineUserDTO.setId(-500);
        assertEquals(-500, onlineUserDTO.getId());
    }

    @Test
    @DisplayName("Should set ID to zero")
    void testSetId_Zero() {
        onlineUserDTO = new OnlineUserDTO(100, "testuser", "testpass", "123", "Address");
        onlineUserDTO.setId(0);
        assertEquals(0, onlineUserDTO.getId());
    }

    @Test
    @DisplayName("Should set ID to maximum value")
    void testSetId_MaxValue() {
        onlineUserDTO = new OnlineUserDTO("testuser", "testpass");
        onlineUserDTO.setId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, onlineUserDTO.getId());
    }

    @Test
    @DisplayName("Should set ID to minimum value")
    void testSetId_MinValue() {
        onlineUserDTO = new OnlineUserDTO("testuser", "testpass");
        onlineUserDTO.setId(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, onlineUserDTO.getId());
    }

    // Edge case tests
    @Test
    @DisplayName("Should handle special characters in username")
    void testSpecialCharacters_Username() {
        String specialUsername = "user+tag@domain-name.co.uk";
        onlineUserDTO = new OnlineUserDTO(specialUsername, "password");
        assertEquals(specialUsername, onlineUserDTO.getUsername());
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void testSpecialCharacters_Password() {
        String specialPassword = "P@$$w0rd!#$%^&*()_+-=[]{}|;:,.<>?";
        onlineUserDTO = new OnlineUserDTO("username", specialPassword);
        assertEquals(specialPassword, onlineUserDTO.getPassword());
    }

    @Test
    @DisplayName("Should handle very long strings")
    void testVeryLongStrings() {
        String longUsername = "user" + "a".repeat(1000) + "@domain.com";
        String longPassword = "pass" + "b".repeat(2000) + "!";
        String longContact = "+1-555-" + "1".repeat(500);
        String longAddress = "House " + "c".repeat(3000) + " Street";

        onlineUserDTO = new OnlineUserDTO(1, longUsername, longPassword, longContact, longAddress);

        assertEquals(longUsername, onlineUserDTO.getUsername());
        assertEquals(longPassword, onlineUserDTO.getPassword());
        assertEquals(longContact, onlineUserDTO.getContactNumber());
        assertEquals(longAddress, onlineUserDTO.getAddress());
    }

    @Test
    @DisplayName("Should handle whitespace-only strings")
    void testWhitespaceOnlyStrings() {
        String whitespaceUsername = "   ";
        String whitespacePassword = "\t\n\r ";
        String whitespaceContact = "     ";
        String whitespaceAddress = "\n\n\n";

        onlineUserDTO = new OnlineUserDTO(1, whitespaceUsername, whitespacePassword, whitespaceContact,
                whitespaceAddress);

        assertEquals(whitespaceUsername, onlineUserDTO.getUsername());
        assertEquals(whitespacePassword, onlineUserDTO.getPassword());
        assertEquals(whitespaceContact, onlineUserDTO.getContactNumber());
        assertEquals(whitespaceAddress, onlineUserDTO.getAddress());
    }

    @Test
    @DisplayName("Should handle mixed case usernames")
    void testMixedCaseUsernames() {
        String mixedCaseUsername = "UsEr.NaMe@DoMaIn.CoM";
        onlineUserDTO = new OnlineUserDTO(mixedCaseUsername, "password");
        assertEquals(mixedCaseUsername, onlineUserDTO.getUsername());
    }

    @Test
    @DisplayName("Should handle numeric usernames")
    void testNumericUsernames() {
        String numericUsername = "1234567890";
        onlineUserDTO = new OnlineUserDTO(numericUsername, "password");
        assertEquals(numericUsername, onlineUserDTO.getUsername());
    }

    @Test
    @DisplayName("Should handle phone number formats")
    void testPhoneNumberFormats() {
        // Test various phone number formats
        String[] phoneFormats = {
                "(555) 123-4567",
                "555.123.4567",
                "555-123-4567",
                "+1 555 123 4567",
                "15551234567",
                "+44 20 7946 0958",
                "001-555-123-4567"
        };

        for (int i = 0; i < phoneFormats.length; i++) {
            onlineUserDTO = new OnlineUserDTO(i, "user" + i, "pass", phoneFormats[i], "Address");
            assertEquals(phoneFormats[i], onlineUserDTO.getContactNumber());
        }
    }

    @Test
    @DisplayName("Should handle various address formats")
    void testAddressFormats() {
        String[] addressFormats = {
                "123 Main St",
                "123 Main St, Apt 4B",
                "123 Main St, Apt 4B, City, State 12345",
                "123 Main St\nApt 4B\nCity, State 12345",
                "PO Box 123, City, State 12345",
                "Rural Route 1, Box 123, City, State 12345",
                "123 Main St, City, State 12345, USA"
        };

        for (int i = 0; i < addressFormats.length; i++) {
            onlineUserDTO = new OnlineUserDTO(i, "user" + i, "pass", "123456", addressFormats[i]);
            assertEquals(addressFormats[i], onlineUserDTO.getAddress());
        }
    }

    @Test
    @DisplayName("Should maintain state consistency across operations")
    void testStateConsistency() {
        // Start with basic constructor
        onlineUserDTO = new OnlineUserDTO("initial.user", "initial.pass");

        // Update ID
        onlineUserDTO.setId(42);

        // Verify all values are maintained correctly
        assertEquals(42, onlineUserDTO.getId());
        assertEquals("initial.user", onlineUserDTO.getUsername());
        assertEquals("initial.pass", onlineUserDTO.getPassword());
        assertNull(onlineUserDTO.getContactNumber());
        assertNull(onlineUserDTO.getAddress());

        // Test with full constructor
        onlineUserDTO = new OnlineUserDTO(
                999, "full.user@email.com", "full.password",
                "+1-555-999-8888", "999 Full Street");

        // Update ID again
        onlineUserDTO.setId(1000);

        // Verify state consistency
        assertEquals(1000, onlineUserDTO.getId());
        assertEquals("full.user@email.com", onlineUserDTO.getUsername());
        assertEquals("full.password", onlineUserDTO.getPassword());
        assertEquals("+1-555-999-8888", onlineUserDTO.getContactNumber());
        assertEquals("999 Full Street", onlineUserDTO.getAddress());
    }

    @Test
    @DisplayName("Should handle constructor parameter order correctly")
    void testConstructorParameterOrder() {
        // Test that parameters are assigned to correct fields
        onlineUserDTO = new OnlineUserDTO(
                12345, "username_test", "password_test",
                "contact_test", "address_test");

        assertEquals(12345, onlineUserDTO.getId());
        assertEquals("username_test", onlineUserDTO.getUsername());
        assertEquals("password_test", onlineUserDTO.getPassword());
        assertEquals("contact_test", onlineUserDTO.getContactNumber());
        assertEquals("address_test", onlineUserDTO.getAddress());
    }

    @Test
    @DisplayName("Should handle repeated ID modifications")
    void testRepeatedIdModifications() {
        onlineUserDTO = new OnlineUserDTO("test", "test");

        // Multiple ID changes
        onlineUserDTO.setId(1);
        assertEquals(1, onlineUserDTO.getId());

        onlineUserDTO.setId(100);
        assertEquals(100, onlineUserDTO.getId());

        onlineUserDTO.setId(-50);
        assertEquals(-50, onlineUserDTO.getId());

        onlineUserDTO.setId(0);
        assertEquals(0, onlineUserDTO.getId());

        onlineUserDTO.setId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, onlineUserDTO.getId());
    }
}
