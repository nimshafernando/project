package syos.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import syos.dto.ItemDTO;

import static org.junit.jupiter.api.Assertions.*;

class ItemValidatorTest {

    private ItemValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ItemValidator();
    }

    @Test
    @DisplayName("Should validate correct item")
    void testValidItem() {
        ItemDTO item = new ItemDTO("ITEM001", "Valid Item", 100.0, 50);
        assertTrue(validator.isValid(item));
    }

    @Test
    @DisplayName("Should reject null item")
    void testNullItem() {
        assertFalse(validator.isValid(null));
    }

    @Test
    @DisplayName("Should reject item with empty or whitespace code")
    void testInvalidCode() {
        assertFalse(validator.isValid(new ItemDTO("", "Item", 100.0, 50)));
        assertFalse(validator.isValid(new ItemDTO("   ", "Item", 100.0, 50)));
    }

    @Test
    @DisplayName("Should reject item with overly long code")
    void testOverlyLongCode() {
        String longCode = "X".repeat(21);
        assertFalse(validator.isValid(new ItemDTO(longCode, "Item", 100.0, 50)));
    }

    @Test
    @DisplayName("Should reject item with null or empty name")
    void testInvalidName() {
        assertFalse(validator.isValid(new ItemDTO("ITEM001", null, 100.0, 50)));
        assertFalse(validator.isValid(new ItemDTO("ITEM001", "   ", 100.0, 50)));
    }

    @Test
    @DisplayName("Should reject item with overly long name")
    void testOverlyLongName() {
        String longName = "Y".repeat(101);
        assertFalse(validator.isValid(new ItemDTO("ITEM001", longName, 100.0, 50)));
    }

    @Test
    @DisplayName("Should reject item with invalid price")
    void testInvalidPrice() {
        assertFalse(validator.isValid(new ItemDTO("ITEM001", "Item", 0.0, 50)));
        assertFalse(validator.isValid(new ItemDTO("ITEM001", "Item", -50.0, 50)));
        assertFalse(validator.isValid(new ItemDTO("ITEM001", "Item", 1_000_000.0, 50)));
    }

    @Test
    @DisplayName("Should reject item with invalid quantity")
    void testInvalidQuantity() {
        assertFalse(validator.isValid(new ItemDTO("ITEM001", "Item", 100.0, -1)));
    }

    @Test
    @DisplayName("Should accept boundary values for price and quantity")
    void testBoundaryValidPriceAndQuantity() {
        assertTrue(validator.isValid(new ItemDTO("ITEM001", "Item", 0.01, 0)));
        assertTrue(validator.isValid(new ItemDTO("ITEM001", "Item", 999999.99, 9999)));
    }
}
