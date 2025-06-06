package syos.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReshelvedItemDTO Test Suite")
class ReshelvedItemDTOTest {

    private ReshelvedItemDTO reshelvedItemDTO;

    @BeforeEach
    void setUp() {
        // Setup is done in individual test methods
    }

    // Constructor tests - Various item types
    @Test
    @DisplayName("Should create ReshelvedItemDTO for grocery items")
    void testConstructor_GroceryItems() {
        reshelvedItemDTO = new ReshelvedItemDTO("GROC001", "Organic Bananas", 25);

        assertEquals("GROC001", reshelvedItemDTO.getCode());
        assertEquals("Organic Bananas", reshelvedItemDTO.getName());
        assertEquals(25, reshelvedItemDTO.getQuantity());
    }

    @Test
    @DisplayName("Should create ReshelvedItemDTO for electronics")
    void testConstructor_Electronics() {
        reshelvedItemDTO = new ReshelvedItemDTO("ELEC002", "Wireless Headphones", 5);

        assertEquals("ELEC002", reshelvedItemDTO.getCode());
        assertEquals("Wireless Headphones", reshelvedItemDTO.getName());
        assertEquals(5, reshelvedItemDTO.getQuantity());
    }

    @Test
    @DisplayName("Should create ReshelvedItemDTO for household items")
    void testConstructor_HouseholdItems() {
        reshelvedItemDTO = new ReshelvedItemDTO("HOME003", "Laundry Detergent 2L", 12);

        assertEquals("HOME003", reshelvedItemDTO.getCode());
        assertEquals("Laundry Detergent 2L", reshelvedItemDTO.getName());
        assertEquals(12, reshelvedItemDTO.getQuantity());
    }

    @Test
    @DisplayName("Should create ReshelvedItemDTO for clothing items")
    void testConstructor_ClothingItems() {
        reshelvedItemDTO = new ReshelvedItemDTO("CLOTH004", "Men's Cotton T-Shirt Size L", 8);

        assertEquals("CLOTH004", reshelvedItemDTO.getCode());
        assertEquals("Men's Cotton T-Shirt Size L", reshelvedItemDTO.getName());
        assertEquals(8, reshelvedItemDTO.getQuantity());
    }

    @Test
    @DisplayName("Should create ReshelvedItemDTO for books and media")
    void testConstructor_BooksAndMedia() {
        reshelvedItemDTO = new ReshelvedItemDTO("BOOK005", "Programming Guide: Java Edition", 3);

        assertEquals("BOOK005", reshelvedItemDTO.getCode());
        assertEquals("Programming Guide: Java Edition", reshelvedItemDTO.getName());
        assertEquals(3, reshelvedItemDTO.getQuantity());
    }

    // Constructor with edge cases
    @Test
    @DisplayName("Should handle null values in constructor")
    void testConstructor_NullValues() {
        reshelvedItemDTO = new ReshelvedItemDTO(null, null, 0);

        assertNull(reshelvedItemDTO.getCode());
        assertNull(reshelvedItemDTO.getName());
        assertEquals(0, reshelvedItemDTO.getQuantity());
    }

    @Test
    @DisplayName("Should handle empty strings in constructor")
    void testConstructor_EmptyStrings() {
        reshelvedItemDTO = new ReshelvedItemDTO("", "", 1);

        assertEquals("", reshelvedItemDTO.getCode());
        assertEquals("", reshelvedItemDTO.getName());
        assertEquals(1, reshelvedItemDTO.getQuantity());
    }

    @Test
    @DisplayName("Should handle negative quantity in constructor")
    void testConstructor_NegativeQuantity() {
        reshelvedItemDTO = new ReshelvedItemDTO("NEG001", "Negative Item", -5);

        assertEquals("NEG001", reshelvedItemDTO.getCode());
        assertEquals("Negative Item", reshelvedItemDTO.getName());
        assertEquals(-5, reshelvedItemDTO.getQuantity());
    }

    @Test
    @DisplayName("Should handle zero quantity in constructor")
    void testConstructor_ZeroQuantity() {
        reshelvedItemDTO = new ReshelvedItemDTO("ZERO001", "Zero Quantity Item", 0);

        assertEquals("ZERO001", reshelvedItemDTO.getCode());
        assertEquals("Zero Quantity Item", reshelvedItemDTO.getName());
        assertEquals(0, reshelvedItemDTO.getQuantity());
    }

    @Test
    @DisplayName("Should handle maximum integer quantity in constructor")
    void testConstructor_MaxQuantity() {
        reshelvedItemDTO = new ReshelvedItemDTO("MAX001", "Max Quantity Item", Integer.MAX_VALUE);

        assertEquals("MAX001", reshelvedItemDTO.getCode());
        assertEquals("Max Quantity Item", reshelvedItemDTO.getName());
        assertEquals(Integer.MAX_VALUE, reshelvedItemDTO.getQuantity());
    }

    // Getter tests
    @Test
    @DisplayName("Should return correct code from getter")
    void testGetCode() {
        reshelvedItemDTO = new ReshelvedItemDTO("CODE123", "Test Item", 10);
        assertEquals("CODE123", reshelvedItemDTO.getCode());
    }

    @Test
    @DisplayName("Should return correct name from getter")
    void testGetName() {
        reshelvedItemDTO = new ReshelvedItemDTO("ITEM001", "Detailed Product Name", 10);
        assertEquals("Detailed Product Name", reshelvedItemDTO.getName());
    }

    @Test
    @DisplayName("Should return correct quantity from getter")
    void testGetQuantity() {
        reshelvedItemDTO = new ReshelvedItemDTO("QTY001", "Quantity Test", 42);
        assertEquals(42, reshelvedItemDTO.getQuantity());
    }

    // Static merge method tests - Same items
    @Test
    @DisplayName("Should merge two ReshelvedItemDTOs with same code - grocery items")
    void testMerge_SameCode_GroceryItems() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("APPLE001", "Red Apples", 10);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("APPLE001", "Red Apples", 15);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("APPLE001", merged.getCode());
        assertEquals("Red Apples", merged.getName());
        assertEquals(25, merged.getQuantity()); // 10 + 15
    }

    @Test
    @DisplayName("Should merge two ReshelvedItemDTOs with same code - electronics")
    void testMerge_SameCode_Electronics() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("PHONE001", "Smartphone", 3);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("PHONE001", "Smartphone", 7);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("PHONE001", merged.getCode());
        assertEquals("Smartphone", merged.getName());
        assertEquals(10, merged.getQuantity()); // 3 + 7
    }

    @Test
    @DisplayName("Should merge two ReshelvedItemDTOs with zero quantities")
    void testMerge_ZeroQuantities() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("ZERO001", "Zero Item", 0);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("ZERO001", "Zero Item", 0);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("ZERO001", merged.getCode());
        assertEquals("Zero Item", merged.getName());
        assertEquals(0, merged.getQuantity()); // 0 + 0
    }

    @Test
    @DisplayName("Should merge ReshelvedItemDTOs with one zero quantity")
    void testMerge_OneZeroQuantity() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("MIXED001", "Mixed Item", 0);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("MIXED001", "Mixed Item", 20);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("MIXED001", merged.getCode());
        assertEquals("Mixed Item", merged.getName());
        assertEquals(20, merged.getQuantity()); // 0 + 20
    }

    @Test
    @DisplayName("Should merge ReshelvedItemDTOs with negative quantities")
    void testMerge_NegativeQuantities() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("NEG001", "Negative Item", -5);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("NEG001", "Negative Item", -3);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("NEG001", merged.getCode());
        assertEquals("Negative Item", merged.getName());
        assertEquals(-8, merged.getQuantity()); // -5 + (-3)
    }

    @Test
    @DisplayName("Should merge ReshelvedItemDTOs with mixed positive and negative quantities")
    void testMerge_MixedPositiveNegative() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("MIX001", "Mixed Item", 15);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("MIX001", "Mixed Item", -5);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("MIX001", merged.getCode());
        assertEquals("Mixed Item", merged.getName());
        assertEquals(10, merged.getQuantity()); // 15 + (-5)
    }

    @Test
    @DisplayName("Should merge ReshelvedItemDTOs with large quantities")
    void testMerge_LargeQuantities() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("LARGE001", "Large Item", 1000000);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("LARGE001", "Large Item", 2000000);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("LARGE001", merged.getCode());
        assertEquals("Large Item", merged.getName());
        assertEquals(3000000, merged.getQuantity()); // 1000000 + 2000000
    }

    @Test
    @DisplayName("Should merge ReshelvedItemDTOs approaching integer overflow")
    void testMerge_ApproachingIntegerOverflow() {
        int halfMax = Integer.MAX_VALUE / 2;
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("OVERFLOW001", "Overflow Item", halfMax);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("OVERFLOW001", "Overflow Item", halfMax);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("OVERFLOW001", merged.getCode());
        assertEquals("Overflow Item", merged.getName());
        assertEquals(halfMax + halfMax, merged.getQuantity()); // Will be close to MAX_VALUE
    }

    // Merge with null values
    @Test
    @DisplayName("Should handle null codes in merge")
    void testMerge_NullCodes() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO(null, "Item", 5);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO(null, "Item", 10);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertNull(merged.getCode());
        assertEquals("Item", merged.getName());
        assertEquals(15, merged.getQuantity());
    }

    @Test
    @DisplayName("Should handle null names in merge")
    void testMerge_NullNames() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("CODE001", null, 8);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("CODE001", null, 12);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("CODE001", merged.getCode());
        assertNull(merged.getName());
        assertEquals(20, merged.getQuantity());
    }

    @Test
    @DisplayName("Should handle empty strings in merge")
    void testMerge_EmptyStrings() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("", "", 3);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("", "", 7);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("", merged.getCode());
        assertEquals("", merged.getName());
        assertEquals(10, merged.getQuantity());
    }

    // Edge cases for string fields

    @Test
    @DisplayName("Should handle very long strings")
    void testConstructor_VeryLongStrings() {
        String longCode = "CODE" + "A".repeat(1000);
        String longName = "Product Name " + "B".repeat(2000);

        reshelvedItemDTO = new ReshelvedItemDTO(longCode, longName, 1);

        assertEquals(longCode, reshelvedItemDTO.getCode());
        assertEquals(longName, reshelvedItemDTO.getName());
        assertEquals(1, reshelvedItemDTO.getQuantity());
    }

    @Test
    @DisplayName("Should handle whitespace-only strings")
    void testConstructor_WhitespaceOnlyStrings() {
        String whitespaceCode = "   ";
        String whitespaceName = "\t\n\r ";

        reshelvedItemDTO = new ReshelvedItemDTO(whitespaceCode, whitespaceName, 2);

        assertEquals(whitespaceCode, reshelvedItemDTO.getCode());
        assertEquals(whitespaceName, reshelvedItemDTO.getName());
        assertEquals(2, reshelvedItemDTO.getQuantity());
    }

    @Test
    @DisplayName("Should handle numeric strings in code and name")
    void testConstructor_NumericStrings() {
        String numericCode = "1234567890";
        String numericName = "999888777";

        reshelvedItemDTO = new ReshelvedItemDTO(numericCode, numericName, 6);

        assertEquals(numericCode, reshelvedItemDTO.getCode());
        assertEquals(numericName, reshelvedItemDTO.getName());
        assertEquals(6, reshelvedItemDTO.getQuantity());
    }

    // Immutability tests
    @Test
    @DisplayName("Should maintain immutability - object state cannot be changed")
    void testImmutability() {
        reshelvedItemDTO = new ReshelvedItemDTO("IMMUTABLE001", "Immutable Item", 15);

        // Store original values
        String originalCode = reshelvedItemDTO.getCode();
        String originalName = reshelvedItemDTO.getName();
        int originalQuantity = reshelvedItemDTO.getQuantity();

        // Verify values haven't changed (immutable)
        assertEquals(originalCode, reshelvedItemDTO.getCode());
        assertEquals(originalName, reshelvedItemDTO.getName());
        assertEquals(originalQuantity, reshelvedItemDTO.getQuantity());
    }

    // Merge method edge cases
    @Test
    @DisplayName("Should merge items where first item has higher quantity")
    void testMerge_FirstItemHigherQuantity() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("HIGH001", "High Quantity Item", 100);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("HIGH001", "High Quantity Item", 1);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("HIGH001", merged.getCode());
        assertEquals("High Quantity Item", merged.getName());
        assertEquals(101, merged.getQuantity());
    }

    @Test
    @DisplayName("Should merge items where second item has higher quantity")
    void testMerge_SecondItemHigherQuantity() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("HIGH002", "High Quantity Item", 2);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("HIGH002", "High Quantity Item", 200);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("HIGH002", merged.getCode());
        assertEquals("High Quantity Item", merged.getName());
        assertEquals(202, merged.getQuantity());
    }

    @Test
    @DisplayName("Should merge items with identical properties")
    void testMerge_IdenticalItems() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("IDENTICAL001", "Identical Item", 10);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("IDENTICAL001", "Identical Item", 10);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("IDENTICAL001", merged.getCode());
        assertEquals("Identical Item", merged.getName());
        assertEquals(20, merged.getQuantity());
    }

    // Test merge method preserves code and name from first item
    @Test
    @DisplayName("Should use first item's code and name in merge result")
    void testMerge_PreservesFirstItemCodeAndName() {
        ReshelvedItemDTO item1 = new ReshelvedItemDTO("FIRST001", "First Item Name", 5);
        ReshelvedItemDTO item2 = new ReshelvedItemDTO("SECOND002", "Second Item Name", 8);

        ReshelvedItemDTO merged = ReshelvedItemDTO.merge(item1, item2);

        assertEquals("FIRST001", merged.getCode()); // From first item
        assertEquals("First Item Name", merged.getName()); // From first item
        assertEquals(13, merged.getQuantity()); // Sum of quantities
    }

    // Comprehensive business scenario tests
    @Test
    @DisplayName("Should handle realistic business scenario - damaged returns")
    void testBusinessScenario_DamagedReturns() {
        ReshelvedItemDTO damaged1 = new ReshelvedItemDTO("LAPTOP001", "Gaming Laptop 15-inch", 2);
        ReshelvedItemDTO damaged2 = new ReshelvedItemDTO("LAPTOP001", "Gaming Laptop 15-inch", 1);

        ReshelvedItemDTO totalDamaged = ReshelvedItemDTO.merge(damaged1, damaged2);

        assertEquals("LAPTOP001", totalDamaged.getCode());
        assertEquals("Gaming Laptop 15-inch", totalDamaged.getName());
        assertEquals(3, totalDamaged.getQuantity());
    }

    @Test
    @DisplayName("Should handle realistic business scenario - seasonal stock")
    void testBusinessScenario_SeasonalStock() {
        ReshelvedItemDTO winter1 = new ReshelvedItemDTO("COAT001", "Winter Coat Size M", 5);
        ReshelvedItemDTO winter2 = new ReshelvedItemDTO("COAT001", "Winter Coat Size M", 3);

        ReshelvedItemDTO totalWinter = ReshelvedItemDTO.merge(winter1, winter2);

        assertEquals("COAT001", totalWinter.getCode());
        assertEquals("Winter Coat Size M", totalWinter.getName());
        assertEquals(8, totalWinter.getQuantity());
    }

    @Test
    @DisplayName("Should handle realistic business scenario - food expiry returns")
    void testBusinessScenario_FoodExpiryReturns() {
        ReshelvedItemDTO dairy1 = new ReshelvedItemDTO("MILK001", "Organic Whole Milk 1L", 12);
        ReshelvedItemDTO dairy2 = new ReshelvedItemDTO("MILK001", "Organic Whole Milk 1L", 8);

        ReshelvedItemDTO totalDairy = ReshelvedItemDTO.merge(dairy1, dairy2);

        assertEquals("MILK001", totalDairy.getCode());
        assertEquals("Organic Whole Milk 1L", totalDairy.getName());
        assertEquals(20, totalDairy.getQuantity());
    }
}
