package syos.ui.views;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import syos.dto.ReorderItemDTO;
import syos.service.ReorderLevelReportService;
import syos.ui.views.ReorderLevelReportUI.StoreFilter;
import syos.util.ConsoleUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyInt;

@DisplayName("Reorder Level Report UI Tests")
class ReorderLevelReportUITest {

    private ReorderLevelReportUI ui;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        System.setErr(originalErr);
        mocks.close();
    }

    private Scanner createScanner(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    private String getOutput() {
        return outputStream.toString() + errorStream.toString();
    }

    // Helper methods for creating mock data
    private List<ReorderItemDTO> createInStoreItems(int threshold) {
        return Arrays.asList(
                new ReorderItemDTO("INS001", "Low Stock Widget", 5, threshold, 19.99, "IN_STORE"),
                new ReorderItemDTO("INS002", "Almost Out Gadget", 2, threshold, 29.99, "IN_STORE"),
                new ReorderItemDTO("INS003", "Very Long Item Name That Needs To Be Truncated For Display", 10,
                        threshold, 49.99, "IN_STORE"));
    }

    private List<ReorderItemDTO> createOnlineItems(int threshold) {
        return Arrays.asList(
                new ReorderItemDTO("ONL001", "Online Low Stock Item", 8, threshold, 39.99, "ONLINE"),
                new ReorderItemDTO("ONL002", "Online Critical Stock", 1, threshold, 99.99, "ONLINE"));
    }

    private List<ReorderItemDTO> createCombinedItems(int threshold) {
        List<ReorderItemDTO> items = new ArrayList<>();
        items.addAll(createInStoreItems(threshold));
        items.addAll(createOnlineItems(threshold));
        return items;
    }

    @Test
    @DisplayName("Should get correct report title")
    void testGetReportTitle() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner(""));

        // Act & Assert
        assertEquals("SYOS REORDER LEVEL REPORT", ui.getReportTitle());
    }

    @Test
    @DisplayName("Should exit report when selecting back option in store filter")
    void testExitWhenBackSelectedInStoreFilter() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("0\n"));

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class)) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Select Store Type for Reorder Analysis:"));
            assertTrue(output.contains("Returning to reports menu..."));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should exit when empty input given in store filter")
    void testExitWhenEmptyInputInStoreFilter() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("\n"));

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class)) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Select Store Type for Reorder Analysis:"));
            assertTrue(output.contains("Returning to reports menu..."));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should handle invalid store filter option")
    void testInvalidStoreFilterOption() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("9\n0\n"));

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class)) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("[Invalid] Enter 0-3."));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should exit when back option selected in threshold value")
    void testExitWhenBackSelectedInThreshold() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("1\n0\n"));

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class)) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Enter threshold value"));
            assertTrue(output.contains("Returning to reports menu..."));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should exit when empty input given in threshold value")
    void testExitWhenEmptyInputInThreshold() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("1\n\n"));

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class)) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Enter threshold value"));
            assertTrue(output.contains("Returning to reports menu..."));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should handle invalid threshold value")
    void testInvalidThresholdValue() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("1\nabc\n0\n"));

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class)) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("[Invalid] Please enter a valid number."));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should handle out of range threshold value")
    void testOutOfRangeThresholdValue() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("1\n1000\n0\n"));

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class)) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("[Invalid] Threshold must be between 1 and 999."));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should handle negative threshold value")
    void testNegativeThresholdValue() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("1\n-10\n0\n"));

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class)) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("[Invalid] Threshold must be between 1 and 999."));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should handle zero threshold value")
    void testZeroThresholdValue() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("1\n0\n"));

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class)) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Returning to reports menu..."));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should display in-store inventory report")
    void testInStoreInventoryReport() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("1\n20\n\n"));

        try (
                MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ReorderLevelReportService> mockedService = mockConstruction(
                        ReorderLevelReportService.class,
                        (mock, context) -> when(mock.getInStoreReorderItems(20)).thenReturn(createInStoreItems(20)))) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("SYOS REORDER LEVEL REPORT"));
            assertTrue(output.contains("Store Type: In-Store Inventory Only"));
            assertTrue(output.contains("Threshold : Items with stock ≤ 20 units"));
            assertTrue(output.contains("INS001"));
            assertTrue(output.contains("Low Stock Widget"));
            assertTrue(output.contains("19.99")); // Verify truncation works - maxLength is 25, so truncated as 22 chars
            // + "..."
            assertTrue(output.contains("Very Long Item Name Th..."));

            // Verify the summary information is correct
            assertTrue(output.contains("Total Items Requiring Reorder: 3"));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should display online inventory report")
    void testOnlineInventoryReport() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("2\n15\n\n"));

        try (
                MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ReorderLevelReportService> mockedService = mockConstruction(
                        ReorderLevelReportService.class,
                        (mock, context) -> when(mock.getOnlineReorderItems(15)).thenReturn(createOnlineItems(15)))) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("SYOS REORDER LEVEL REPORT"));
            assertTrue(output.contains("Store Type: Online Inventory Only"));
            assertTrue(output.contains("Threshold : Items with stock ≤ 15 units"));
            assertTrue(output.contains("ONL001"));
            assertTrue(output.contains("Online Low Stock Item"));
            assertTrue(output.contains("39.99"));
            assertTrue(output.contains("ONL002"));
            assertTrue(output.contains("Online Critical Stock"));
            assertTrue(output.contains("99.99"));
            // Verify the summary information is correct
            assertTrue(output.contains("Total Items Requiring Reorder: 2"));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should display both stores inventory report")
    void testBothStoresInventoryReport() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("3\n25\n\n"));

        try (
                MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ReorderLevelReportService> mockedService = mockConstruction(
                        ReorderLevelReportService.class,
                        (mock, context) -> when(mock.getItemsBelowReorderLevel(25))
                                .thenReturn(createCombinedItems(25)))) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("SYOS REORDER LEVEL REPORT"));
            assertTrue(output.contains("Store Type: Both In-Store and Online"));
            assertTrue(output.contains("Threshold : Items with stock ≤ 25 units"));
            // Check both in-store and online items are included
            assertTrue(output.contains("INS001"));
            assertTrue(output.contains("ONL001"));
            // Verify the summary information is correct
            assertTrue(output.contains("Total Items Requiring Reorder: 5"));
            assertTrue(output.contains("Total Units Shortfall:"));
            assertTrue(output.contains("Total Estimated Reorder Value:"));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should handle empty report results for in-store")
    void testEmptyReportResultsInStore() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("1\n30\n"));

        try (
                MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ReorderLevelReportService> mockedService = mockConstruction(
                        ReorderLevelReportService.class,
                        (mock, context) -> when(mock.getInStoreReorderItems(30)).thenReturn(Collections.emptyList()))) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("SYOS REORDER LEVEL REPORT"));
            assertTrue(output.contains("Store Type: In-Store Inventory Only"));
            assertTrue(output.contains("Great news! All items are sufficiently stocked."));
            assertTrue(output.contains("No items are below the threshold of 30 units."));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should handle empty report results for online")
    void testEmptyReportResultsOnline() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("2\n10\n"));

        try (
                MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ReorderLevelReportService> mockedService = mockConstruction(
                        ReorderLevelReportService.class,
                        (mock, context) -> when(mock.getOnlineReorderItems(10)).thenReturn(Collections.emptyList()))) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("SYOS REORDER LEVEL REPORT"));
            assertTrue(output.contains("Store Type: Online Inventory Only"));
            assertTrue(output.contains("Great news! All items are sufficiently stocked."));
            assertTrue(output.contains("No items are below the threshold of 10 units."));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should handle empty report results for both stores")
    void testEmptyReportResultsBothStores() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("3\n5\n"));

        try (
                MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ReorderLevelReportService> mockedService = mockConstruction(
                        ReorderLevelReportService.class,
                        (mock, context) -> when(mock.getItemsBelowReorderLevel(5))
                                .thenReturn(Collections.emptyList()))) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("SYOS REORDER LEVEL REPORT"));
            assertTrue(output.contains("Store Type: Both In-Store and Online"));
            assertTrue(output.contains("Great news! All items are sufficiently stocked."));
            assertTrue(output.contains("No items are below the threshold of 5 units."));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should handle exception during report generation")
    void testExceptionDuringReportGeneration() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("1\n25\n\n"));

        try (
                MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ReorderLevelReportService> mockedService = mockConstruction(
                        ReorderLevelReportService.class,
                        (mock, context) -> when(mock.getInStoreReorderItems(25))
                                .thenThrow(new RuntimeException("Database connection error")))) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Error fetching report data: Database connection error"));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should calculate correct shortfall and total values")
    void testShortfallAndTotalCalculations() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("1\n50\n"));

        List<ReorderItemDTO> testItems = Arrays.asList(
                // Current stock is 5, threshold/reorder level is 50, so shortfall is 45
                new ReorderItemDTO("TEST001", "Low Stock Item", 5, 50, 10.00, "IN_STORE"),
                // Current stock is 10, threshold/reorder level is 50, so shortfall is 40
                new ReorderItemDTO("TEST002", "Medium Stock Item", 10, 50, 20.00, "IN_STORE"));

        try (
                MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ReorderLevelReportService> mockedService = mockConstruction(
                        ReorderLevelReportService.class,
                        (mock, context) -> when(mock.getInStoreReorderItems(50)).thenReturn(testItems))) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Total Units Shortfall: 85")); // 45 + 40
            assertTrue(output.contains("Total Estimated Reorder Value: Rs. 1250.00")); // (45*10.00) + (40*20.00) = 450
                                                                                       // + 800 = 1250
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should correctly handle store type descriptions")
    void testStoreTypeDescriptions() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner(""));

        // Test In-Store description
        ReorderLevelReportUI inStoreUI = new ReorderLevelReportUI(createScanner("1\n20\n"));
        try (
                MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ReorderLevelReportService> mockedService = mockConstruction(
                        ReorderLevelReportService.class,
                        (mock, context) -> when(mock.getInStoreReorderItems(20)).thenReturn(createInStoreItems(20)))) {
            inStoreUI.display();
            String output = getOutput();
            assertTrue(output.contains("Store Type: In-Store Inventory Only"));
        }
        outputStream.reset();

        // Test Online description
        ReorderLevelReportUI onlineUI = new ReorderLevelReportUI(createScanner("2\n20\n"));
        try (
                MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ReorderLevelReportService> mockedService = mockConstruction(
                        ReorderLevelReportService.class,
                        (mock, context) -> when(mock.getOnlineReorderItems(20)).thenReturn(createOnlineItems(20)))) {
            onlineUI.display();
            String output = getOutput();
            assertTrue(output.contains("Store Type: Online Inventory Only"));
        }
        outputStream.reset();

        // Test Both Stores description
        ReorderLevelReportUI bothUI = new ReorderLevelReportUI(createScanner("3\n20\n"));
        try (
                MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ReorderLevelReportService> mockedService = mockConstruction(
                        ReorderLevelReportService.class,
                        (mock, context) -> when(mock.getItemsBelowReorderLevel(20))
                                .thenReturn(createCombinedItems(20)))) {
            bothUI.display();
            String output = getOutput();
            assertTrue(output.contains("Store Type: Both In-Store and Online"));
        }
    }

    @Test
    @DisplayName("Should handle minimum threshold value (1)")
    void testMinimumThresholdValue() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("1\n1\n"));

        try (
                MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ReorderLevelReportService> mockedService = mockConstruction(
                        ReorderLevelReportService.class,
                        (mock, context) -> when(mock.getInStoreReorderItems(1)).thenReturn(
                                Arrays.asList(new ReorderItemDTO("CRITICAL", "Critical Stock Item", 0, 1, 15.99,
                                        "IN_STORE"))))) {
            // Act
            ui.display();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Threshold : Items with stock ≤ 1 units"));
            assertTrue(output.contains("CRITICAL"));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should handle maximum threshold value (999)")
    void testMaximumThresholdValue() {
        // Arrange
        ui = new ReorderLevelReportUI(createScanner("2\n999\n"));

        try (
                MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ReorderLevelReportService> mockedService = mockConstruction(
                        ReorderLevelReportService.class,
                        (mock, context) -> when(mock.getOnlineReorderItems(999)).thenReturn(createOnlineItems(999)))) {
            // Act
            ui.display();
            // Assert
            String output = getOutput();
            assertTrue(output.contains("Threshold : Items with stock ≤ 999 units"));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should handle missing scanner gracefully")
    void testMissingScannerHandling() {
        // Arrange & Act
        assertDoesNotThrow(() -> {
            ReorderLevelReportUI ui = new ReorderLevelReportUI(null);
        });
    }

}