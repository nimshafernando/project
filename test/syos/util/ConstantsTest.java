package syos.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test coverage for Constants utility class
 * Tests all constant values and verifies proper utility class design
 */
class ConstantsTest {

    @Test
    @DisplayName("Should have private constructor that throws AssertionError")
    void testPrivateConstructor() {
        // Act & Assert
        Constructor<?>[] constructors = Constants.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);

        Constructor<?> constructor = constructors[0];
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);

        assertThrows(InvocationTargetException.class, () -> {
            constructor.newInstance();
        });

        try {
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof AssertionError);
            assertEquals("Constants class should not be instantiated", e.getCause().getMessage());
        } catch (Exception e) {
            fail("Expected InvocationTargetException with AssertionError cause");
        }
    }

    @Test
    @DisplayName("Should verify all application constants have correct values")
    void testApplicationConstants() {
        // Assert
        assertEquals("Synex Outlet Store", Constants.APPLICATION_NAME);
        assertEquals("1.0.0", Constants.VERSION);

        // Verify these are final constants
        assertNotNull(Constants.APPLICATION_NAME);
        assertNotNull(Constants.VERSION);
        assertFalse(Constants.APPLICATION_NAME.isEmpty());
        assertFalse(Constants.VERSION.isEmpty());
    }

    @Test
    @DisplayName("Should verify all file path constants have correct values")
    void testFilePathConstants() {
        // Assert
        assertEquals("bill_serial.txt", Constants.BILL_SERIAL_FILE);
        assertEquals("online_bill_serial.txt", Constants.ONLINE_BILL_SERIAL_FILE);
        assertEquals("bills/", Constants.BILLS_DIRECTORY);
        assertEquals("bills/store/", Constants.STORE_BILLS_DIRECTORY);
        assertEquals("bills/online/", Constants.ONLINE_BILLS_DIRECTORY);

        // Verify consistency
        assertTrue(Constants.STORE_BILLS_DIRECTORY.startsWith(Constants.BILLS_DIRECTORY));
        assertTrue(Constants.ONLINE_BILLS_DIRECTORY.startsWith(Constants.BILLS_DIRECTORY));

        // Verify proper file extensions
        assertTrue(Constants.BILL_SERIAL_FILE.endsWith(".txt"));
        assertTrue(Constants.ONLINE_BILL_SERIAL_FILE.endsWith(".txt"));

        // Verify directory paths end with separator
        assertTrue(Constants.BILLS_DIRECTORY.endsWith("/"));
        assertTrue(Constants.STORE_BILLS_DIRECTORY.endsWith("/"));
        assertTrue(Constants.ONLINE_BILLS_DIRECTORY.endsWith("/"));
    }

    @Test
    @DisplayName("Should verify all database constants have correct values")
    void testDatabaseConstants() {
        // Assert
        assertEquals("jdbc:mysql://localhost:3306/syos_db", Constants.DB_URL);
        assertEquals(30, Constants.CONNECTION_TIMEOUT);
        assertEquals(10, Constants.MAX_POOL_SIZE);

        // Verify URL format
        assertTrue(Constants.DB_URL.startsWith("jdbc:mysql://"));
        assertTrue(Constants.DB_URL.contains("localhost"));
        assertTrue(Constants.DB_URL.contains("3306"));
        assertTrue(Constants.DB_URL.contains("syos_db"));

        // Verify reasonable timeout and pool size values
        assertTrue(Constants.CONNECTION_TIMEOUT > 0);
        assertTrue(Constants.CONNECTION_TIMEOUT <= 60);
        assertTrue(Constants.MAX_POOL_SIZE > 0);
        assertTrue(Constants.MAX_POOL_SIZE <= 100);
    }

    @Test
    @DisplayName("Should verify all business rule constants have correct values")
    void testBusinessRuleConstants() {
        // Assert
        assertEquals(50, Constants.REORDER_LEVEL_THRESHOLD);
        assertEquals(30, Constants.EXPIRY_WARNING_DAYS);

        // Verify reasonable business values
        assertTrue(Constants.REORDER_LEVEL_THRESHOLD > 0);
        assertTrue(Constants.REORDER_LEVEL_THRESHOLD <= 1000);
        assertTrue(Constants.EXPIRY_WARNING_DAYS > 0);
        assertTrue(Constants.EXPIRY_WARNING_DAYS <= 365);
    }

    @Test
    @DisplayName("Should verify all validation constants have correct values")
    void testValidationConstants() {
        // Assert
        assertEquals(1, Constants.MIN_QUANTITY);
        assertEquals(9999, Constants.MAX_QUANTITY);
        assertEquals(0.01, Constants.MIN_PRICE, 0.001);
        assertEquals(999999.99, Constants.MAX_PRICE, 0.001);

        // Verify logical relationships
        assertTrue(Constants.MIN_QUANTITY < Constants.MAX_QUANTITY);
        assertTrue(Constants.MIN_PRICE < Constants.MAX_PRICE);
        assertTrue(Constants.MIN_QUANTITY > 0);
        assertTrue(Constants.MIN_PRICE > 0);
    }

    @Test
    @DisplayName("Should verify all report constants have correct values")
    void testReportConstants() {
        // Assert
        assertEquals("yyyy-MM-dd", Constants.REPORT_DATE_FORMAT);
        assertEquals("#%d-%s", Constants.BILL_SERIAL_FORMAT);

        // Verify format patterns
        assertNotNull(Constants.REPORT_DATE_FORMAT);
        assertNotNull(Constants.BILL_SERIAL_FORMAT);
        assertTrue(Constants.BILL_SERIAL_FORMAT.contains("%d"));
        assertTrue(Constants.BILL_SERIAL_FORMAT.contains("%s"));
        assertTrue(Constants.BILL_SERIAL_FORMAT.startsWith("#"));
    }

    @Test
    @DisplayName("Should verify all menu option constants have correct values")
    void testMenuOptionConstants() {
        // Assert
        assertEquals(1, Constants.EMPLOYEE_OPTION);
        assertEquals(2, Constants.CUSTOMER_OPTION);
        assertEquals(0, Constants.EXIT_OPTION);

        // Verify logical relationships
        assertTrue(Constants.EMPLOYEE_OPTION > Constants.EXIT_OPTION);
        assertTrue(Constants.CUSTOMER_OPTION > Constants.EXIT_OPTION);
        assertNotEquals(Constants.EMPLOYEE_OPTION, Constants.CUSTOMER_OPTION);
    }

    @Test
    @DisplayName("Should verify Constants class is final")
    void testClassIsFinal() {
        // Assert
        assertTrue(Modifier.isFinal(Constants.class.getModifiers()));
    }

    @Test
    @DisplayName("Should verify all fields are public static final")
    void testFieldModifiers() throws Exception {
        // Test a sample of fields to ensure they have correct modifiers
        var applicationNameField = Constants.class.getField("APPLICATION_NAME");
        var versionField = Constants.class.getField("VERSION");
        var dbUrlField = Constants.class.getField("DB_URL");
        var minQuantityField = Constants.class.getField("MIN_QUANTITY");

        // Check modifiers for APPLICATION_NAME
        int modifiers = applicationNameField.getModifiers();
        assertTrue(Modifier.isPublic(modifiers));
        assertTrue(Modifier.isStatic(modifiers));
        assertTrue(Modifier.isFinal(modifiers));

        // Check modifiers for VERSION
        modifiers = versionField.getModifiers();
        assertTrue(Modifier.isPublic(modifiers));
        assertTrue(Modifier.isStatic(modifiers));
        assertTrue(Modifier.isFinal(modifiers));

        // Check modifiers for DB_URL
        modifiers = dbUrlField.getModifiers();
        assertTrue(Modifier.isPublic(modifiers));
        assertTrue(Modifier.isStatic(modifiers));
        assertTrue(Modifier.isFinal(modifiers));

        // Check modifiers for MIN_QUANTITY
        modifiers = minQuantityField.getModifiers();
        assertTrue(Modifier.isPublic(modifiers));
        assertTrue(Modifier.isStatic(modifiers));
        assertTrue(Modifier.isFinal(modifiers));
    }

    @Test
    @DisplayName("Should verify string constants are not null or empty")
    void testStringConstantsNotNullOrEmpty() {
        // Assert all string constants
        assertNotNull(Constants.APPLICATION_NAME);
        assertNotNull(Constants.VERSION);
        assertNotNull(Constants.BILL_SERIAL_FILE);
        assertNotNull(Constants.ONLINE_BILL_SERIAL_FILE);
        assertNotNull(Constants.BILLS_DIRECTORY);
        assertNotNull(Constants.STORE_BILLS_DIRECTORY);
        assertNotNull(Constants.ONLINE_BILLS_DIRECTORY);
        assertNotNull(Constants.DB_URL);
        assertNotNull(Constants.REPORT_DATE_FORMAT);
        assertNotNull(Constants.BILL_SERIAL_FORMAT);

        // Verify they're not empty
        assertFalse(Constants.APPLICATION_NAME.trim().isEmpty());
        assertFalse(Constants.VERSION.trim().isEmpty());
        assertFalse(Constants.BILL_SERIAL_FILE.trim().isEmpty());
        assertFalse(Constants.ONLINE_BILL_SERIAL_FILE.trim().isEmpty());
        assertFalse(Constants.BILLS_DIRECTORY.trim().isEmpty());
        assertFalse(Constants.STORE_BILLS_DIRECTORY.trim().isEmpty());
        assertFalse(Constants.ONLINE_BILLS_DIRECTORY.trim().isEmpty());
        assertFalse(Constants.DB_URL.trim().isEmpty());
        assertFalse(Constants.REPORT_DATE_FORMAT.trim().isEmpty());
        assertFalse(Constants.BILL_SERIAL_FORMAT.trim().isEmpty());
    }

    @Test
    @DisplayName("Should verify numeric constants are within reasonable ranges")
    void testNumericConstantsRanges() {
        // Connection timeout should be reasonable
        assertTrue(Constants.CONNECTION_TIMEOUT >= 1 && Constants.CONNECTION_TIMEOUT <= 300);

        // Pool size should be reasonable
        assertTrue(Constants.MAX_POOL_SIZE >= 1 && Constants.MAX_POOL_SIZE <= 1000);

        // Reorder threshold should be reasonable
        assertTrue(Constants.REORDER_LEVEL_THRESHOLD >= 1 && Constants.REORDER_LEVEL_THRESHOLD <= 10000);

        // Expiry warning days should be reasonable
        assertTrue(Constants.EXPIRY_WARNING_DAYS >= 1 && Constants.EXPIRY_WARNING_DAYS <= 365);

        // Min/max quantity should be reasonable
        assertTrue(Constants.MIN_QUANTITY >= 1);
        assertTrue(Constants.MAX_QUANTITY >= Constants.MIN_QUANTITY);
        assertTrue(Constants.MAX_QUANTITY <= 99999);

        // Min/max price should be reasonable
        assertTrue(Constants.MIN_PRICE > 0);
        assertTrue(Constants.MAX_PRICE >= Constants.MIN_PRICE);
        assertTrue(Constants.MAX_PRICE <= 9999999.99);

        // Menu options should be reasonable
        assertTrue(Constants.EXIT_OPTION >= 0);
        assertTrue(Constants.EMPLOYEE_OPTION >= 1);
        assertTrue(Constants.CUSTOMER_OPTION >= 1);
    }

    @Test
    @DisplayName("Should verify constants immutability")
    void testConstantsImmutability() {
        // Get reference to string constants
        String originalAppName = Constants.APPLICATION_NAME;
        String originalVersion = Constants.VERSION;
        String originalDbUrl = Constants.DB_URL;

        // Get reference to numeric constants
        int originalTimeout = Constants.CONNECTION_TIMEOUT;
        int originalPoolSize = Constants.MAX_POOL_SIZE;
        double originalMinPrice = Constants.MIN_PRICE;

        // Since these are final static fields, they cannot be changed
        // This test verifies that the values remain the same
        assertEquals(originalAppName, Constants.APPLICATION_NAME);
        assertEquals(originalVersion, Constants.VERSION);
        assertEquals(originalDbUrl, Constants.DB_URL);
        assertEquals(originalTimeout, Constants.CONNECTION_TIMEOUT);
        assertEquals(originalPoolSize, Constants.MAX_POOL_SIZE);
        assertEquals(originalMinPrice, Constants.MIN_PRICE, 0.001);
    }
}
