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
    @DisplayName("Should reject item with invalid code")
    void testInvalidCode() {
        ItemDTO item = new ItemDTO("", "Item", 100.0, 50);
        assertFalse(validator.isValid(item));
    }

    @Test
    @DisplayName("Should reject item with invalid name")
    void testInvalidName() {
        ItemDTO item = new ItemDTO("ITEM001", null, 100.0, 50);
        assertFalse(validator.isValid(item));
    }

    @Test
    @DisplayName("Should reject item with invalid price")
    void testInvalidPrice() {
        ItemDTO item1 = new ItemDTO("ITEM001", "Item", 0.0, 50);
        ItemDTO item2 = new ItemDTO("ITEM001", "Item", -50.0, 50);
        ItemDTO item3 = new ItemDTO("ITEM001", "Item", 1000000.0, 50);

        assertFalse(validator.isValid(item1));
        assertFalse(validator.isValid(item2));
        assertFalse(validator.isValid(item3));
    }

    @Test
    @DisplayName("Should reject item with negative quantity")
    void testInvalidQuantity() {
        ItemDTO item = new ItemDTO("ITEM001", "Item", 100.0, -1);
        assertFalse(validator.isValid(item));
    }
}