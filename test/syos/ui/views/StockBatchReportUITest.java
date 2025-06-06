package syos.ui.views;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import syos.dto.StockBatchDTO;
import syos.service.StockBatchReportService;
import syos.service.StockBatchReportService.BatchFilter;
import syos.service.StockBatchReportService.BatchSummary;
import syos.ui.views.StockBatchReportUI;
import syos.util.ConsoleUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("StockBatchReportUI Tests")
class StockBatchReportUITest {

    @Mock
    private StockBatchReportService mockService;

    private StockBatchReportUI stockBatchReportUI;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        // Capture system output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        mocks.close();
    }

    private Scanner createScanner(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    private String getOutput() {
        return outputStream.toString();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with valid scanner")
        void testConstructor_WithValidScanner() {
            // Arrange & Act
            Scanner validScanner = createScanner("test");
            StockBatchReportUI ui = new StockBatchReportUI(validScanner);

            // Assert
            assertNotNull(ui);
        }

        @Test
        @DisplayName("Should handle null scanner")
        void testConstructor_WithNullScanner() {
            // Act & Assert
            assertDoesNotThrow(() -> new StockBatchReportUI(null));
        }
    }

    @Nested
    @DisplayName("Gather Report Criteria Tests")
    class GatherReportCriteriaTests {

        @Test
        @DisplayName("Should gather criteria for all batches filter")
        void testGatherReportCriteria_AllBatchesFilter() {
            // Arrange
            Scanner scanner = createScanner("1\n"); // All Batches
            stockBatchReportUI = new StockBatchReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = stockBatchReportUI.gatherReportCriteria();

                // Assert
                assertTrue(result);
                String output = getOutput();
                assertTrue(output.contains("STOCK BATCH REPORT OPTIONS"));
                assertTrue(output.contains("Select Batch Filter:"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen());
            }
        }

        @Test
        @DisplayName("Should gather criteria for active only filter")
        void testGatherReportCriteria_ActiveOnlyFilter() {
            // Arrange
            Scanner scanner = createScanner("2\n\n"); // Active Only + empty item code
            stockBatchReportUI = new StockBatchReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = stockBatchReportUI.gatherReportCriteria();

                // Assert
                assertTrue(result);
                String output = getOutput();
                assertTrue(output.contains("Active Batches Only"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(2));
            }
        }

        @Test
        @DisplayName("Should gather criteria for expiring soon filter")
        void testGatherReportCriteria_ExpiringSoonFilter() {
            // Arrange
            Scanner scanner = createScanner("3\nITEM001\n"); // Expiring Soon + specific item
            stockBatchReportUI = new StockBatchReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = stockBatchReportUI.gatherReportCriteria();

                // Assert
                assertTrue(result);
                String output = getOutput();
                assertTrue(output.contains("Expiring Soon"));
                assertTrue(output.contains("Enter specific item code"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(2));
            }
        }

        @Test
        @DisplayName("Should gather criteria for expired filter")
        void testGatherReportCriteria_ExpiredFilter() {
            // Arrange
            Scanner scanner = createScanner("4\nitem002\n"); // Expired + lowercase item code
            stockBatchReportUI = new StockBatchReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = stockBatchReportUI.gatherReportCriteria();

                // Assert
                assertTrue(result);
                String output = getOutput();
                assertTrue(output.contains("Expired Batches"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(2));
            }
        }

        @Test
        @DisplayName("Should gather criteria for depleted filter")
        void testGatherReportCriteria_DepletedFilter() {
            // Arrange
            Scanner scanner = createScanner("5\n   ITEM003   \n"); // Depleted + item with spaces
            stockBatchReportUI = new StockBatchReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = stockBatchReportUI.gatherReportCriteria();

                // Assert
                assertTrue(result);
                String output = getOutput();
                assertTrue(output.contains("Depleted Batches"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(2));
            }
        }

        @Test
        @DisplayName("Should return false when user chooses to go back")
        void testGatherReportCriteria_UserGoesBack() {
            // Arrange
            Scanner scanner = createScanner("0\n"); // Back to menu
            stockBatchReportUI = new StockBatchReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = stockBatchReportUI.gatherReportCriteria();

                // Assert
                assertFalse(result);
            }
        }

        @ParameterizedTest
        @ValueSource(strings = { "invalid", "6", "-1", "abc", "", "999" })
        @DisplayName("Should handle invalid filter choices")
        void testGatherReportCriteria_InvalidChoices(String invalidChoice) {
            // Arrange
            Scanner scanner = createScanner(invalidChoice + "\n1\n"); // Invalid choice then valid
            stockBatchReportUI = new StockBatchReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = stockBatchReportUI.gatherReportCriteria();

                // Assert
                assertTrue(result);
                String output = getOutput();
                assertTrue(output.contains("[Invalid] Enter 0-5."));
            }
        }

        @Test
        @DisplayName("Should handle specific item code prompt for non-all filters")
        void testGatherReportCriteria_WithSpecificItem() {
            // Arrange
            Scanner scanner = createScanner("2\nITEM001\n"); // Active only + specific item
            stockBatchReportUI = new StockBatchReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = stockBatchReportUI.gatherReportCriteria();

                // Assert
                assertTrue(result);
                String output = getOutput();
                assertTrue(output.contains("Enter specific item code"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(2));
            }
        }

        @Test
        @DisplayName("Should handle empty item code input")
        void testGatherReportCriteria_EmptyItemCode() {
            // Arrange
            Scanner scanner = createScanner("2\n\n"); // Active only + empty item code
            stockBatchReportUI = new StockBatchReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = stockBatchReportUI.gatherReportCriteria();

                // Assert
                assertTrue(result);
                String output = getOutput();
                assertTrue(output.contains("Enter specific item code"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(2));
            }
        }

        @Test
        @DisplayName("Should handle exception during criteria gathering")
        void testGatherReportCriteria_WithException() {
            // Arrange
            Scanner scanner = createScanner("1\n");
            stockBatchReportUI = new StockBatchReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                mockedConsoleUtils.when(() -> ConsoleUtils.clearScreen())
                        .thenThrow(new RuntimeException("Test exception"));

                // Act
                boolean result = stockBatchReportUI.gatherReportCriteria();

                // Assert
                assertFalse(result);
                String output = getOutput();
                assertTrue(output.contains("Error gathering criteria"));
            }
        }
    }

    @Nested
    @DisplayName("Fetch Report Items Tests")
    class FetchReportItemsTests {

        @Test
        @DisplayName("Should fetch all batches when filter is ALL")
        void testFetchReportItems_AllBatches() {
            // Arrange
            Scanner scanner = createScanner("1\n"); // All batches
            stockBatchReportUI = new StockBatchReportUI(scanner);

            List<StockBatchDTO> mockBatches = Arrays.asList(
                    createMockBatch("FOOD001", "Organic Apples", 125.99, 150, 80),
                    createMockBatch("ELEC002", "Wireless Mouse", 845.50, 75, 60));

            try (MockedConstruction<StockBatchReportService> mockedConstruction = mockConstruction(
                    StockBatchReportService.class, (mock, context) -> {
                        when(mock.getStockBatches(BatchFilter.ALL)).thenReturn(mockBatches);
                    })) {

                stockBatchReportUI.gatherReportCriteria();

                // Act
                List<StockBatchDTO> result = stockBatchReportUI.fetchReportItems();

                // Assert
                assertEquals(2, result.size());
                assertEquals("FOOD001", result.get(0).getItemCode());
                assertEquals("ELEC002", result.get(1).getItemCode());
            }
        }

        @Test
        @DisplayName("Should fetch batches for specific item when item code provided")
        void testFetchReportItems_SpecificItem() {
            // Arrange
            Scanner scanner = createScanner("2\nFOOD001\n"); // Active only + specific item
            stockBatchReportUI = new StockBatchReportUI(scanner);

            List<StockBatchDTO> mockBatches = Arrays.asList(
                    createMockBatch("FOOD001", "Organic Apples", 135.99, 50, 40));

            try (MockedConstruction<StockBatchReportService> mockedConstruction = mockConstruction(
                    StockBatchReportService.class, (mock, context) -> {
                        when(mock.getBatchesForItem("FOOD001")).thenReturn(mockBatches);
                    })) {

                stockBatchReportUI.gatherReportCriteria();

                // Act
                List<StockBatchDTO> result = stockBatchReportUI.fetchReportItems();

                // Assert
                assertEquals(1, result.size());
                assertEquals("FOOD001", result.get(0).getItemCode());
            }
        }

        @Test
        @DisplayName("Should fetch empty list when no batches found")
        void testFetchReportItems_EmptyList() {
            // Arrange
            Scanner scanner = createScanner("4\n"); // Expired batches
            stockBatchReportUI = new StockBatchReportUI(scanner);

            List<StockBatchDTO> emptyBatches = Collections.emptyList();

            try (MockedConstruction<StockBatchReportService> mockedConstruction = mockConstruction(
                    StockBatchReportService.class, (mock, context) -> {
                        when(mock.getStockBatches(BatchFilter.EXPIRED)).thenReturn(emptyBatches);
                    })) {

                stockBatchReportUI.gatherReportCriteria();

                // Act
                List<StockBatchDTO> result = stockBatchReportUI.fetchReportItems();

                // Assert
                assertTrue(result.isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("Render Report Tests")
    class RenderReportTests {

        @Test
        @DisplayName("Should render report with summary and batch details")
        void testRenderReport_WithBatches() {
            // Arrange
            Scanner scanner = createScanner("1\n");
            stockBatchReportUI = new StockBatchReportUI(scanner);

            List<StockBatchDTO> batches = Arrays.asList(
                    createMockBatch("FOOD001", "Organic Apples", 125.99, 100, 80),
                    createMockBatch("ELEC002", "Wireless Mouse", 845.50, 75, 60));

            BatchSummary mockSummary = createMockBatchSummary();

            try (MockedConstruction<StockBatchReportService> mockedConstruction = mockConstruction(
                    StockBatchReportService.class, (mock, context) -> {
                        when(mock.getBatchSummary()).thenReturn(mockSummary);
                    })) {

                stockBatchReportUI.gatherReportCriteria();

                // Act
                stockBatchReportUI.renderReport(batches);

                // Assert
                String output = getOutput();
                assertTrue(output.contains("BATCH SUMMARY"));
                assertTrue(output.contains("Total Batches: 100"));
                assertTrue(output.contains("Active: 80"));
                assertTrue(output.contains("STOCK BATCH DETAILS"));
                assertTrue(output.contains("FOOD001"));
                assertTrue(output.contains("Organic Apples"));
                assertTrue(output.contains("125.99"));
                assertTrue(output.contains("ELEC002"));
                assertTrue(output.contains("Wireless Mouse"));
                assertTrue(output.contains("845.50"));
                assertTrue(output.contains("Total Batches Displayed: 2"));
            }
        }

        @Test
        @DisplayName("Should render report with empty batch list")
        void testRenderReport_WithEmptyBatches() {
            // Arrange
            Scanner scanner = createScanner("1\n");
            stockBatchReportUI = new StockBatchReportUI(scanner);

            List<StockBatchDTO> emptyBatches = Collections.emptyList();
            BatchSummary mockSummary = createMockBatchSummary();

            try (MockedConstruction<StockBatchReportService> mockedConstruction = mockConstruction(
                    StockBatchReportService.class, (mock, context) -> {
                        when(mock.getBatchSummary()).thenReturn(mockSummary);
                    })) {

                stockBatchReportUI.gatherReportCriteria();

                // Act
                stockBatchReportUI.renderReport(emptyBatches);

                // Assert
                String output = getOutput();
                assertTrue(output.contains("BATCH SUMMARY"));
                assertTrue(output.contains("STOCK BATCH DETAILS"));
                assertTrue(output.contains("Total Batches Displayed: 0"));
            }
        }

        @Test
        @DisplayName("Should handle long item names with truncation")
        void testRenderReport_WithLongItemNames() {
            // Arrange
            Scanner scanner = createScanner("1\n");
            stockBatchReportUI = new StockBatchReportUI(scanner);

            List<StockBatchDTO> batches = Arrays.asList(
                    createMockBatch("LONGITEM001",
                            "This is a very long item name that should be truncated when displayed", 1515.99, 50, 30));

            BatchSummary mockSummary = createMockBatchSummary();

            try (MockedConstruction<StockBatchReportService> mockedConstruction = mockConstruction(
                    StockBatchReportService.class, (mock, context) -> {
                        when(mock.getBatchSummary()).thenReturn(mockSummary);
                    })) {

                stockBatchReportUI.gatherReportCriteria();

                // Act
                stockBatchReportUI.renderReport(batches);

                // Assert
                String output = getOutput();
                assertTrue(output.contains("LONGITEM001"));
                assertTrue(output.contains("...") || output.contains("This is a very long"));
            }
        }

        @Test
        @DisplayName("Should handle negative available quantities")
        void testRenderReport_WithNegativeQuantities() {
            // Arrange
            Scanner scanner = createScanner("1\n");
            stockBatchReportUI = new StockBatchReportUI(scanner);

            StockBatchDTO negativeBatch = createMockBatch("NEG001", "Overused Item", 520.00, 50, 60); // Used > Total
            List<StockBatchDTO> batches = Arrays.asList(negativeBatch);

            BatchSummary mockSummary = createMockBatchSummary();

            try (MockedConstruction<StockBatchReportService> mockedConstruction = mockConstruction(
                    StockBatchReportService.class, (mock, context) -> {
                        when(mock.getBatchSummary()).thenReturn(mockSummary);
                    })) {

                stockBatchReportUI.gatherReportCriteria();

                // Act
                stockBatchReportUI.renderReport(batches);

                // Assert
                String output = getOutput();
                assertTrue(output.contains("NEG001"));
                assertTrue(output.contains("Overused Item"));
            }
        }

        @Test
        @DisplayName("Should handle null item names")
        void testRenderReport_WithNullItemNames() {
            // Arrange
            Scanner scanner = createScanner("1\n");
            stockBatchReportUI = new StockBatchReportUI(scanner);

            StockBatchDTO nullNameBatch = createMockBatch("NULL001", null, 99.99, 25, 10);
            List<StockBatchDTO> batches = Arrays.asList(nullNameBatch);

            BatchSummary mockSummary = createMockBatchSummary();

            try (MockedConstruction<StockBatchReportService> mockedConstruction = mockConstruction(
                    StockBatchReportService.class, (mock, context) -> {
                        when(mock.getBatchSummary()).thenReturn(mockSummary);
                    })) {

                stockBatchReportUI.gatherReportCriteria();

                // Act
                stockBatchReportUI.renderReport(batches);

                // Assert
                String output = getOutput();
                assertTrue(output.contains("NULL001"));
                assertTrue(output.contains("-")); // Should display "-" for null name
            }
        }
    }

    @Nested
    @DisplayName("Show No Data Message Tests")
    class ShowNoDataMessageTests {

        @Test
        @DisplayName("Should show no data message for all batches filter")
        void testShowNoDataMessage_AllBatches() {
            // Arrange
            Scanner scanner = createScanner("1\n\n"); // All batches + enter to continue
            stockBatchReportUI = new StockBatchReportUI(scanner);
            stockBatchReportUI.gatherReportCriteria();

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                stockBatchReportUI.showNoDataMessage();

                // Assert
                String output = getOutput();
                assertTrue(output.contains("STOCK BATCH DETAILS"));
                assertTrue(output.contains("Filter: All Batches"));
                assertTrue(output.contains("No batches found matching the selected criteria"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)));
            }
        }

        @Test
        @DisplayName("Should show no data message with specific item code")
        void testShowNoDataMessage_WithSpecificItem() {
            // Arrange
            Scanner scanner = createScanner("2\nITEM999\n\n"); // Active only + specific item + enter
            stockBatchReportUI = new StockBatchReportUI(scanner);
            stockBatchReportUI.gatherReportCriteria();

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                stockBatchReportUI.showNoDataMessage();

                // Assert
                String output = getOutput();
                assertTrue(output.contains("STOCK BATCH DETAILS"));
                assertTrue(output.contains("Filter: Active Batches Only"));
                assertTrue(output.contains("Item Code: ITEM999"));
                assertTrue(output.contains("No batches found matching the selected criteria"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)));
            }
        }

        @Test
        @DisplayName("Should show no data message for expired filter")
        void testShowNoDataMessage_ExpiredFilter() {
            // Arrange
            Scanner scanner = createScanner("4\nOLD001\n\n"); // Expired + specific item + enter
            stockBatchReportUI = new StockBatchReportUI(scanner);
            stockBatchReportUI.gatherReportCriteria();

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                stockBatchReportUI.showNoDataMessage();

                // Assert
                String output = getOutput();
                assertTrue(output.contains("STOCK BATCH DETAILS"));
                assertTrue(output.contains("Filter: Expired Batches"));
                assertTrue(output.contains("Item Code: OLD001"));
                assertTrue(output.contains("No batches found matching the selected criteria"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)));
            }
        }

        @Test
        @DisplayName("Should show no data message for depleted filter")
        void testShowNoDataMessage_DepletedFilter() {
            // Arrange
            Scanner scanner = createScanner("5\nEMPTY001\n\n"); // Depleted + specific item + enter
            stockBatchReportUI = new StockBatchReportUI(scanner);
            stockBatchReportUI.gatherReportCriteria();

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                stockBatchReportUI.showNoDataMessage();

                // Assert
                String output = getOutput();
                assertTrue(output.contains("STOCK BATCH DETAILS"));
                assertTrue(output.contains("Filter: Depleted Batches"));
                assertTrue(output.contains("Item Code: EMPTY001"));
                assertTrue(output.contains("No batches found matching the selected criteria"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)));
            }
        }
    }

    @Nested
    @DisplayName("Get Report Title Tests")
    class GetReportTitleTests {

        @Test
        @DisplayName("Should return correct report title")
        void testGetReportTitle() {
            // Arrange
            Scanner scanner = createScanner("test");
            stockBatchReportUI = new StockBatchReportUI(scanner);

            // Act
            String title = stockBatchReportUI.getReportTitle();

            // Assert
            assertEquals("Stock Batch Report", title);
        }
    }

    @Nested
    @DisplayName("Display Method Tests")
    class DisplayMethodTests {

        @Test
        @DisplayName("Should complete full display workflow successfully")
        void testDisplay_SuccessfulWorkflow() {
            // Arrange
            Scanner scanner = createScanner("1\n\n"); // All batches + enter to continue
            stockBatchReportUI = new StockBatchReportUI(scanner);

            List<StockBatchDTO> mockBatches = Arrays.asList(
                    createMockBatch("DISPLAY001", "Display Item", 99.99, 30, 20));

            BatchSummary mockSummary = createMockBatchSummary();

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<StockBatchReportService> mockedConstruction = mockConstruction(
                            StockBatchReportService.class, (mock, context) -> {
                                when(mock.getStockBatches(any())).thenReturn(mockBatches);
                                when(mock.getBatchSummary()).thenReturn(mockSummary);
                            })) {

                // Act
                stockBatchReportUI.display();

                // Assert
                String output = getOutput();
                assertTrue(output.contains("STOCK BATCH DETAILS"));
                assertTrue(output.contains("DISPLAY001"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(1));
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeast(1));
            }
        }

        @Test
        @DisplayName("Should handle user going back during criteria gathering")
        void testDisplay_UserGoesBack() {
            // Arrange
            Scanner scanner = createScanner("0\n\n"); // Go back + enter
            stockBatchReportUI = new StockBatchReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                stockBatchReportUI.display();

                // Assert
                String output = getOutput();
                assertTrue(output.contains("Returning to reports menu"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(1));
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)));
            }
        }
    }

    // Helper methods
    private StockBatchDTO createMockBatch(String code, String name, double price, int totalQty, int usedQty) {
        StockBatchDTO batch = mock(StockBatchDTO.class);
        when(batch.getItemCode()).thenReturn(code);
        when(batch.getItemName()).thenReturn(name);
        when(batch.getSellingPrice()).thenReturn(price);
        when(batch.getTotalQuantity()).thenReturn(totalQty);
        when(batch.getUsedQuantity()).thenReturn(usedQty);
        when(batch.getAvailableQuantity()).thenReturn(totalQty - usedQty);
        when(batch.getPurchaseDate()).thenReturn(LocalDate.now().minusDays(30));
        when(batch.getExpiryDate()).thenReturn(LocalDate.now().plusMonths(6));
        return batch;
    }

    private BatchSummary createMockBatchSummary() {
        BatchSummary summary = mock(BatchSummary.class);
        when(summary.getTotalBatches()).thenReturn(100);
        when(summary.getActiveBatches()).thenReturn(80);
        when(summary.getExpiringSoon()).thenReturn(15);
        when(summary.getExpired()).thenReturn(5);
        when(summary.getDepleted()).thenReturn(10);
        when(summary.getTotalStock()).thenReturn(5000);
        when(summary.getAvailableStock()).thenReturn(4000);
        when(summary.getUsedStock()).thenReturn(1000);
        return summary;
    }
}
