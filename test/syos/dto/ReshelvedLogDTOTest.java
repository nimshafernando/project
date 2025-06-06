package syos.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReshelvedLogDTO Tests")
class ReshelvedLogDTOTest {

    private ReshelvedLogDTO reshelvedLog;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2025, Month.JUNE, 5, 14, 30, 45);
        reshelvedLog = new ReshelvedLogDTO(1, "ITM001", 50, "MAIN_STORE", testDateTime);
    }

    @Test
    @DisplayName("Constructor should create ReshelvedLogDTO with all fields")
    void testConstructor() {
        assertNotNull(reshelvedLog);
        assertEquals(1, reshelvedLog.getId());
        assertEquals("ITM001", reshelvedLog.getItemCode());
        assertEquals(50, reshelvedLog.getQuantity());
        assertEquals("MAIN_STORE", reshelvedLog.getStoreType());
        assertEquals(testDateTime, reshelvedLog.getReshelvedAt());
    }

    @Test
    @DisplayName("Constructor should handle zero ID")
    void testConstructorWithZeroId() {
        ReshelvedLogDTO log = new ReshelvedLogDTO(0, "ITM002", 25, "WAREHOUSE", testDateTime);
        assertEquals(0, log.getId());
        assertEquals("ITM002", log.getItemCode());
        assertEquals(25, log.getQuantity());
        assertEquals("WAREHOUSE", log.getStoreType());
        assertEquals(testDateTime, log.getReshelvedAt());
    }

    @Test
    @DisplayName("Constructor should handle negative ID")
    void testConstructorWithNegativeId() {
        ReshelvedLogDTO log = new ReshelvedLogDTO(-1, "ITM003", 75, "BRANCH_STORE", testDateTime);
        assertEquals(-1, log.getId());
        assertEquals("ITM003", log.getItemCode());
        assertEquals(75, log.getQuantity());
        assertEquals("BRANCH_STORE", log.getStoreType());
        assertEquals(testDateTime, log.getReshelvedAt());
    }

    @Test
    @DisplayName("Constructor should handle maximum integer ID")
    void testConstructorWithMaxId() {
        ReshelvedLogDTO log = new ReshelvedLogDTO(Integer.MAX_VALUE, "ITM004", 100, "OUTLET", testDateTime);
        assertEquals(Integer.MAX_VALUE, log.getId());
        assertEquals("ITM004", log.getItemCode());
        assertEquals(100, log.getQuantity());
        assertEquals("OUTLET", log.getStoreType());
        assertEquals(testDateTime, log.getReshelvedAt());
    }

    @Test
    @DisplayName("Constructor should handle null item code")
    void testConstructorWithNullItemCode() {
        ReshelvedLogDTO log = new ReshelvedLogDTO(1, null, 50, "MAIN_STORE", testDateTime);
        assertEquals(1, log.getId());
        assertNull(log.getItemCode());
        assertEquals(50, log.getQuantity());
        assertEquals("MAIN_STORE", log.getStoreType());
        assertEquals(testDateTime, log.getReshelvedAt());
    }

    @Test
    @DisplayName("Constructor should handle empty item code")
    void testConstructorWithEmptyItemCode() {
        ReshelvedLogDTO log = new ReshelvedLogDTO(1, "", 50, "MAIN_STORE", testDateTime);
        assertEquals(1, log.getId());
        assertEquals("", log.getItemCode());
        assertEquals(50, log.getQuantity());
        assertEquals("MAIN_STORE", log.getStoreType());
        assertEquals(testDateTime, log.getReshelvedAt());
    }

    @Test
    @DisplayName("Constructor should handle zero quantity")
    void testConstructorWithZeroQuantity() {
        ReshelvedLogDTO log = new ReshelvedLogDTO(1, "ITM005", 0, "MAIN_STORE", testDateTime);
        assertEquals(1, log.getId());
        assertEquals("ITM005", log.getItemCode());
        assertEquals(0, log.getQuantity());
        assertEquals("MAIN_STORE", log.getStoreType());
        assertEquals(testDateTime, log.getReshelvedAt());
    }

    @Test
    @DisplayName("Constructor should handle negative quantity")
    void testConstructorWithNegativeQuantity() {
        ReshelvedLogDTO log = new ReshelvedLogDTO(1, "ITM006", -10, "MAIN_STORE", testDateTime);
        assertEquals(1, log.getId());
        assertEquals("ITM006", log.getItemCode());
        assertEquals(-10, log.getQuantity());
        assertEquals("MAIN_STORE", log.getStoreType());
        assertEquals(testDateTime, log.getReshelvedAt());
    }

    @Test
    @DisplayName("Constructor should handle null store type")
    void testConstructorWithNullStoreType() {
        ReshelvedLogDTO log = new ReshelvedLogDTO(1, "ITM007", 50, null, testDateTime);
        assertEquals(1, log.getId());
        assertEquals("ITM007", log.getItemCode());
        assertEquals(50, log.getQuantity());
        assertNull(log.getStoreType());
        assertEquals(testDateTime, log.getReshelvedAt());
    }

    @Test
    @DisplayName("Constructor should handle null reshelved date time")
    void testConstructorWithNullDateTime() {
        ReshelvedLogDTO log = new ReshelvedLogDTO(1, "ITM008", 50, "MAIN_STORE", null);
        assertEquals(1, log.getId());
        assertEquals("ITM008", log.getItemCode());
        assertEquals(50, log.getQuantity());
        assertEquals("MAIN_STORE", log.getStoreType());
        assertNull(log.getReshelvedAt());
    }

    @Test
    @DisplayName("Constructor should handle minimum date time")
    void testConstructorWithMinDateTime() {
        LocalDateTime minDateTime = LocalDateTime.MIN;
        ReshelvedLogDTO log = new ReshelvedLogDTO(1, "ITM009", 50, "MAIN_STORE", minDateTime);
        assertEquals(1, log.getId());
        assertEquals("ITM009", log.getItemCode());
        assertEquals(50, log.getQuantity());
        assertEquals("MAIN_STORE", log.getStoreType());
        assertEquals(minDateTime, log.getReshelvedAt());
    }

    @Test
    @DisplayName("Constructor should handle maximum date time")
    void testConstructorWithMaxDateTime() {
        LocalDateTime maxDateTime = LocalDateTime.MAX;
        ReshelvedLogDTO log = new ReshelvedLogDTO(1, "ITM010", 50, "MAIN_STORE", maxDateTime);
        assertEquals(1, log.getId());
        assertEquals("ITM010", log.getItemCode());
        assertEquals(50, log.getQuantity());
        assertEquals("MAIN_STORE", log.getStoreType());
        assertEquals(maxDateTime, log.getReshelvedAt());
    }

    // ID getter and setter tests
    @Test
    @DisplayName("getId should return correct ID")
    void testGetId() {
        assertEquals(1, reshelvedLog.getId());
    }

    @Test
    @DisplayName("setId should update ID correctly")
    void testSetId() {
        reshelvedLog.setId(999);
        assertEquals(999, reshelvedLog.getId());
    }

    @Test
    @DisplayName("setId should handle zero value")
    void testSetIdZero() {
        reshelvedLog.setId(0);
        assertEquals(0, reshelvedLog.getId());
    }

    @Test
    @DisplayName("setId should handle negative value")
    void testSetIdNegative() {
        reshelvedLog.setId(-100);
        assertEquals(-100, reshelvedLog.getId());
    }

    @Test
    @DisplayName("setId should handle maximum integer value")
    void testSetIdMaxValue() {
        reshelvedLog.setId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, reshelvedLog.getId());
    }

    @Test
    @DisplayName("setId should handle minimum integer value")
    void testSetIdMinValue() {
        reshelvedLog.setId(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, reshelvedLog.getId());
    }

    // Item code getter and setter tests
    @Test
    @DisplayName("getItemCode should return correct item code")
    void testGetItemCode() {
        assertEquals("ITM001", reshelvedLog.getItemCode());
    }

    @Test
    @DisplayName("setItemCode should update item code correctly")
    void testSetItemCode() {
        reshelvedLog.setItemCode("ITM999");
        assertEquals("ITM999", reshelvedLog.getItemCode());
    }

    @Test
    @DisplayName("setItemCode should handle null value")
    void testSetItemCodeNull() {
        reshelvedLog.setItemCode(null);
        assertNull(reshelvedLog.getItemCode());
    }

    @Test
    @DisplayName("setItemCode should handle empty string")
    void testSetItemCodeEmpty() {
        reshelvedLog.setItemCode("");
        assertEquals("", reshelvedLog.getItemCode());
    }

    @Test
    @DisplayName("setItemCode should handle whitespace string")
    void testSetItemCodeWhitespace() {
        reshelvedLog.setItemCode("   ");
        assertEquals("   ", reshelvedLog.getItemCode());
    }

    @Test
    @DisplayName("setItemCode should handle long item code")
    void testSetItemCodeLong() {
        String longCode = "ITM".repeat(100);
        reshelvedLog.setItemCode(longCode);
        assertEquals(longCode, reshelvedLog.getItemCode());
    }

    @Test
    @DisplayName("setItemCode should handle special characters")
    void testSetItemCodeSpecialChars() {
        String specialCode = "ITM-001_V2@PROD#1";
        reshelvedLog.setItemCode(specialCode);
        assertEquals(specialCode, reshelvedLog.getItemCode());
    }

    // Quantity getter and setter tests
    @Test
    @DisplayName("getQuantity should return correct quantity")
    void testGetQuantity() {
        assertEquals(50, reshelvedLog.getQuantity());
    }

    @Test
    @DisplayName("setQuantity should update quantity correctly")
    void testSetQuantity() {
        reshelvedLog.setQuantity(150);
        assertEquals(150, reshelvedLog.getQuantity());
    }

    @Test
    @DisplayName("setQuantity should handle zero value")
    void testSetQuantityZero() {
        reshelvedLog.setQuantity(0);
        assertEquals(0, reshelvedLog.getQuantity());
    }

    @Test
    @DisplayName("setQuantity should handle negative value")
    void testSetQuantityNegative() {
        reshelvedLog.setQuantity(-50);
        assertEquals(-50, reshelvedLog.getQuantity());
    }

    @Test
    @DisplayName("setQuantity should handle maximum integer value")
    void testSetQuantityMaxValue() {
        reshelvedLog.setQuantity(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, reshelvedLog.getQuantity());
    }

    @Test
    @DisplayName("setQuantity should handle minimum integer value")
    void testSetQuantityMinValue() {
        reshelvedLog.setQuantity(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, reshelvedLog.getQuantity());
    }

    // Store type getter and setter tests
    @Test
    @DisplayName("getStoreType should return correct store type")
    void testGetStoreType() {
        assertEquals("MAIN_STORE", reshelvedLog.getStoreType());
    }

    @Test
    @DisplayName("setStoreType should update store type correctly")
    void testSetStoreType() {
        reshelvedLog.setStoreType("WAREHOUSE");
        assertEquals("WAREHOUSE", reshelvedLog.getStoreType());
    }

    @Test
    @DisplayName("setStoreType should handle null value")
    void testSetStoreTypeNull() {
        reshelvedLog.setStoreType(null);
        assertNull(reshelvedLog.getStoreType());
    }

    @Test
    @DisplayName("setStoreType should handle empty string")
    void testSetStoreTypeEmpty() {
        reshelvedLog.setStoreType("");
        assertEquals("", reshelvedLog.getStoreType());
    }

    @Test
    @DisplayName("setStoreType should handle different store types")
    void testSetStoreTypeDifferentValues() {
        String[] storeTypes = { "MAIN_STORE", "WAREHOUSE", "BRANCH_STORE", "OUTLET", "DEPOT", "DISTRIBUTION_CENTER" };

        for (String storeType : storeTypes) {
            reshelvedLog.setStoreType(storeType);
            assertEquals(storeType, reshelvedLog.getStoreType());
        }
    }

    @Test
    @DisplayName("setStoreType should handle lowercase values")
    void testSetStoreTypeLowercase() {
        reshelvedLog.setStoreType("main_store");
        assertEquals("main_store", reshelvedLog.getStoreType());
    }

    @Test
    @DisplayName("setStoreType should handle mixed case values")
    void testSetStoreTypeMixedCase() {
        reshelvedLog.setStoreType("Main_Store");
        assertEquals("Main_Store", reshelvedLog.getStoreType());
    }

    @Test
    @DisplayName("setStoreType should handle special characters")
    void testSetStoreTypeSpecialChars() {
        reshelvedLog.setStoreType("STORE-001_V2@MAIN#1");
        assertEquals("STORE-001_V2@MAIN#1", reshelvedLog.getStoreType());
    }

    // Reshelved date time getter and setter tests
    @Test
    @DisplayName("getReshelvedAt should return correct date time")
    void testGetReshelvedAt() {
        assertEquals(testDateTime, reshelvedLog.getReshelvedAt());
    }

    @Test
    @DisplayName("setReshelvedAt should update date time correctly")
    void testSetReshelvedAt() {
        LocalDateTime newDateTime = LocalDateTime.of(2025, Month.DECEMBER, 25, 23, 59, 59);
        reshelvedLog.setReshelvedAt(newDateTime);
        assertEquals(newDateTime, reshelvedLog.getReshelvedAt());
    }

    @Test
    @DisplayName("setReshelvedAt should handle null value")
    void testSetReshelvedAtNull() {
        reshelvedLog.setReshelvedAt(null);
        assertNull(reshelvedLog.getReshelvedAt());
    }

    @Test
    @DisplayName("setReshelvedAt should handle minimum date time")
    void testSetReshelvedAtMinValue() {
        LocalDateTime minDateTime = LocalDateTime.MIN;
        reshelvedLog.setReshelvedAt(minDateTime);
        assertEquals(minDateTime, reshelvedLog.getReshelvedAt());
    }

    @Test
    @DisplayName("setReshelvedAt should handle maximum date time")
    void testSetReshelvedAtMaxValue() {
        LocalDateTime maxDateTime = LocalDateTime.MAX;
        reshelvedLog.setReshelvedAt(maxDateTime);
        assertEquals(maxDateTime, reshelvedLog.getReshelvedAt());
    }

    @Test
    @DisplayName("setReshelvedAt should handle leap year date")
    void testSetReshelvedAtLeapYear() {
        LocalDateTime leapYearDate = LocalDateTime.of(2024, Month.FEBRUARY, 29, 12, 0, 0);
        reshelvedLog.setReshelvedAt(leapYearDate);
        assertEquals(leapYearDate, reshelvedLog.getReshelvedAt());
    }

    @Test
    @DisplayName("setReshelvedAt should handle various time precisions")
    void testSetReshelvedAtTimePrecision() {
        LocalDateTime preciseTime = LocalDateTime.of(2025, Month.JUNE, 5, 14, 30, 45, 123456789);
        reshelvedLog.setReshelvedAt(preciseTime);
        assertEquals(preciseTime, reshelvedLog.getReshelvedAt());
    }

    // Business scenario tests
    @Test
    @DisplayName("Should create log for damaged goods reshelving")
    void testDamagedGoodsScenario() {
        ReshelvedLogDTO damagedLog = new ReshelvedLogDTO(
                101,
                "DMG-MILK-001",
                -5, // negative quantity for damaged goods removal
                "MAIN_STORE",
                LocalDateTime.of(2025, Month.JUNE, 5, 9, 15, 0));

        assertEquals(101, damagedLog.getId());
        assertEquals("DMG-MILK-001", damagedLog.getItemCode());
        assertEquals(-5, damagedLog.getQuantity());
        assertEquals("MAIN_STORE", damagedLog.getStoreType());
        assertEquals(LocalDateTime.of(2025, Month.JUNE, 5, 9, 15, 0), damagedLog.getReshelvedAt());
    }

    @Test
    @DisplayName("Should create log for bulk reshelving operation")
    void testBulkReshelvingScenario() {
        ReshelvedLogDTO bulkLog = new ReshelvedLogDTO(
                202,
                "BULK-CANNED-FOOD",
                500,
                "WAREHOUSE",
                LocalDateTime.of(2025, Month.JUNE, 5, 8, 0, 0));

        assertEquals(202, bulkLog.getId());
        assertEquals("BULK-CANNED-FOOD", bulkLog.getItemCode());
        assertEquals(500, bulkLog.getQuantity());
        assertEquals("WAREHOUSE", bulkLog.getStoreType());
        assertEquals(LocalDateTime.of(2025, Month.JUNE, 5, 8, 0, 0), bulkLog.getReshelvedAt());
    }

    @Test
    @DisplayName("Should create log for seasonal items reshelving")
    void testSeasonalItemsScenario() {
        ReshelvedLogDTO seasonalLog = new ReshelvedLogDTO(
                303,
                "SEASONAL-XMAS-DECOR",
                25,
                "OUTLET",
                LocalDateTime.of(2025, Month.JANUARY, 2, 16, 30, 0));

        assertEquals(303, seasonalLog.getId());
        assertEquals("SEASONAL-XMAS-DECOR", seasonalLog.getItemCode());
        assertEquals(25, seasonalLog.getQuantity());
        assertEquals("OUTLET", seasonalLog.getStoreType());
        assertEquals(LocalDateTime.of(2025, Month.JANUARY, 2, 16, 30, 0), seasonalLog.getReshelvedAt());
    }

    // State consistency tests
    @Test
    @DisplayName("Multiple setter calls should maintain state consistency")
    void testStateConsistencyAfterMultipleSetters() {
        reshelvedLog.setId(999);
        reshelvedLog.setItemCode("NEW-ITEM");
        reshelvedLog.setQuantity(200);
        reshelvedLog.setStoreType("NEW_STORE");
        LocalDateTime newDateTime = LocalDateTime.of(2025, Month.DECEMBER, 31, 23, 59, 59);
        reshelvedLog.setReshelvedAt(newDateTime);

        assertEquals(999, reshelvedLog.getId());
        assertEquals("NEW-ITEM", reshelvedLog.getItemCode());
        assertEquals(200, reshelvedLog.getQuantity());
        assertEquals("NEW_STORE", reshelvedLog.getStoreType());
        assertEquals(newDateTime, reshelvedLog.getReshelvedAt());
    }

    @Test
    @DisplayName("Getter calls should not affect object state")
    void testGettersDoNotAffectState() {
        int originalId = reshelvedLog.getId();
        String originalItemCode = reshelvedLog.getItemCode();
        int originalQuantity = reshelvedLog.getQuantity();
        String originalStoreType = reshelvedLog.getStoreType();
        LocalDateTime originalDateTime = reshelvedLog.getReshelvedAt();

        // Call getters multiple times
        for (int i = 0; i < 10; i++) {
            reshelvedLog.getId();
            reshelvedLog.getItemCode();
            reshelvedLog.getQuantity();
            reshelvedLog.getStoreType();
            reshelvedLog.getReshelvedAt();
        }

        // Verify state unchanged
        assertEquals(originalId, reshelvedLog.getId());
        assertEquals(originalItemCode, reshelvedLog.getItemCode());
        assertEquals(originalQuantity, reshelvedLog.getQuantity());
        assertEquals(originalStoreType, reshelvedLog.getStoreType());
        assertEquals(originalDateTime, reshelvedLog.getReshelvedAt());
    }

    @Test
    @DisplayName("Should handle setter with same value multiple times")
    void testSetterWithSameValueMultipleTimes() {
        for (int i = 0; i < 5; i++) {
            reshelvedLog.setId(1);
            reshelvedLog.setItemCode("ITM001");
            reshelvedLog.setQuantity(50);
            reshelvedLog.setStoreType("MAIN_STORE");
            reshelvedLog.setReshelvedAt(testDateTime);
        }

        assertEquals(1, reshelvedLog.getId());
        assertEquals("ITM001", reshelvedLog.getItemCode());
        assertEquals(50, reshelvedLog.getQuantity());
        assertEquals("MAIN_STORE", reshelvedLog.getStoreType());
        assertEquals(testDateTime, reshelvedLog.getReshelvedAt());
    }
}
