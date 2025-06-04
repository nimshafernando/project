package syos.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import syos.dto.BatchDTO;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BatchValidatorTest {

    private BatchValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BatchValidator();
    }

    @Test
    @DisplayName("Should validate correct batch")
    void testValidBatch() {
        // Arrange
        BatchDTO batch = new BatchDTO("ITEM001", "Valid Item", 100.0, 50,
                LocalDate.now(), LocalDate.now().plusDays(30));

        // Act & Assert
        assertTrue(validator.isValid(batch));
    }

    @Test
    @DisplayName("Should reject null batch")
    void testNullBatch() {
        assertFalse(validator.isValid(null));
    }

    @Test
    @DisplayName("Should reject batch with invalid item code")
    void testInvalidItemCode() {
        // Test null item code
        BatchDTO batch1 = new BatchDTO(null, "Item", 100.0, 50,
                LocalDate.now(), LocalDate.now().plusDays(30));
        assertFalse(validator.isValid(batch1));

        // Test empty item code
        BatchDTO batch2 = new BatchDTO("", "Item", 100.0, 50,
                LocalDate.now(), LocalDate.now().plusDays(30));
        assertFalse(validator.isValid(batch2));

        // Test too long item code
        BatchDTO batch3 = new BatchDTO("VERYLONGITEMCODETHATISINVALID", "Item", 100.0, 50,
                LocalDate.now(), LocalDate.now().plusDays(30));
        assertFalse(validator.isValid(batch3));
    }

    @Test
    @DisplayName("Should reject batch with invalid name")
    void testInvalidName() {
        BatchDTO batch = new BatchDTO("ITEM001", "", 100.0, 50,
                LocalDate.now(), LocalDate.now().plusDays(30));
        assertFalse(validator.isValid(batch));
    }

    @Test
    @DisplayName("Should reject batch with invalid price")
    void testInvalidPrice() {
        // Test zero price
        BatchDTO batch1 = new BatchDTO("ITEM001", "Item", 0.0, 50,
                LocalDate.now(), LocalDate.now().plusDays(30));
        assertFalse(validator.isValid(batch1));

        // Test negative price
        BatchDTO batch2 = new BatchDTO("ITEM001", "Item", -100.0, 50,
                LocalDate.now(), LocalDate.now().plusDays(30));
        assertFalse(validator.isValid(batch2));
    }

    @Test
    @DisplayName("Should reject batch with negative quantity")
    void testInvalidQuantity() {
        BatchDTO batch = new BatchDTO("ITEM001", "Item", 100.0, -1,
                LocalDate.now(), LocalDate.now().plusDays(30));
        assertFalse(validator.isValid(batch));
    }

    @Test
    @DisplayName("Should reject batch with invalid dates")
    void testInvalidDates() {
        // Test expiry before purchase
        BatchDTO batch = new BatchDTO("ITEM001", "Item", 100.0, 50,
                LocalDate.now(), LocalDate.now().minusDays(1));
        assertFalse(validator.isValid(batch));
    }
}