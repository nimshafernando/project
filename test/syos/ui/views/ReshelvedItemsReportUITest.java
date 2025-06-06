package syos.ui.views;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import syos.dto.ReshelvedItemDTO;
import syos.service.ReshelvedItemsReportService;
import syos.ui.views.ReshelvedItemsReportUI.StoreFilter;
import syos.ui.views.ReshelvedItemsReportUI.DateRangeFilter;
import syos.util.ConsoleUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ReshelvedItemsReportUI class.
 * Tests all public methods, constructor, and error handling scenarios.
 */
class ReshelvedItemsReportUITest {

    @Mock
    private Scanner scanner;

    @Mock
    private ReshelvedItemsReportService service;
    private ReshelvedItemsReportUI reshelvedItemsReportUI;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Exception e) {
                // Ignore
            }
        }
        if (mocks != null) {
            try {
                mocks.close();
            } catch (Exception e) {
                // Ignore
            }
        }
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
            ReshelvedItemsReportUI ui = new ReshelvedItemsReportUI(validScanner);

            // Assert
            assertNotNull(ui);
        }

        @Test
        @DisplayName("Should handle null scanner")
        void testConstructor_WithNullScanner() {
            // Act & Assert
            assertDoesNotThrow(() -> new ReshelvedItemsReportUI(null));
        }
    }

    @Nested
    @DisplayName("Gather Report Criteria Tests")
    class GatherReportCriteriaTests {

        @Test
        @DisplayName("Should gather criteria for in-store today")
        void testGatherReportCriteria_InStoreToday() {
            // Arrange
            Scanner scanner = createScanner("1\n1\n"); // In-store + Today
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = reshelvedItemsReportUI.gatherReportCriteria();

                // Assert
                assertTrue(result);
                String output = getOutput();
                assertTrue(output.contains("Select Store Type"));
                assertTrue(output.contains("IN_STORE_ONLY"));
                assertTrue(output.contains("Select Date Range"));
                assertTrue(output.contains("TODAY"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(2));
            }
        }

        @Test
        @DisplayName("Should gather criteria for online this week")
        void testGatherReportCriteria_OnlineThisWeek() {
            // Arrange
            Scanner scanner = createScanner("2\n2\n"); // Online + This Week
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = reshelvedItemsReportUI.gatherReportCriteria();

                // Assert
                assertTrue(result);
                String output = getOutput();
                assertTrue(output.contains("ONLINE_ONLY"));
                assertTrue(output.contains("THIS_WEEK"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(2));
            }
        }

        @Test
        @DisplayName("Should gather criteria for combined this month")
        void testGatherReportCriteria_CombinedThisMonth() {
            // Arrange
            Scanner scanner = createScanner("3\n3\n"); // Combined + This Month
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = reshelvedItemsReportUI.gatherReportCriteria();

                // Assert
                assertTrue(result);
                String output = getOutput();
                assertTrue(output.contains("COMBINED"));
                assertTrue(output.contains("THIS_MONTH"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(2));
            }
        }

        @Test
        @DisplayName("Should gather criteria for specific date range")
        void testGatherReportCriteria_SpecificDateRange() {
            // Arrange
            LocalDate startDate = LocalDate.of(2025, 6, 1);
            LocalDate endDate = LocalDate.of(2025, 6, 5);
            Scanner scanner = createScanner("1\n4\n" + startDate + "\n" + endDate + "\n"); // In-store + Specific range
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = reshelvedItemsReportUI.gatherReportCriteria();

                // Assert
                assertTrue(result);
                String output = getOutput();
                assertTrue(output.contains("SPECIFIC_DATE_RANGE"));
                assertTrue(output.contains("Enter Start Date"));
                assertTrue(output.contains("Enter End Date"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(3));
            }
        }

        @Test
        @DisplayName("Should return false when user goes back from store selection")
        void testGatherReportCriteria_UserGoesBackFromStore() {
            // Arrange
            Scanner scanner = createScanner("0\n"); // Back from store selection
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = reshelvedItemsReportUI.gatherReportCriteria();

                // Assert
                assertFalse(result);
            }
        }

        @Test
        @DisplayName("Should return false when user goes back from date selection")
        void testGatherReportCriteria_UserGoesBackFromDate() {
            // Arrange
            Scanner scanner = createScanner("1\n0\n"); // In-store + back from date
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = reshelvedItemsReportUI.gatherReportCriteria();

                // Assert
                assertFalse(result);
            }
        }

        @Test
        @DisplayName("Should handle invalid date range (end before start)")
        void testGatherReportCriteria_InvalidDateRange() {
            // Arrange
            LocalDate startDate = LocalDate.of(2025, 6, 10);
            LocalDate endDate = LocalDate.of(2025, 6, 5); // End before start
            Scanner scanner = createScanner("1\n4\n" + startDate + "\n" + endDate + "\n");
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = reshelvedItemsReportUI.gatherReportCriteria();

                // Assert
                assertFalse(result);
                String output = getOutput();
                assertTrue(output.contains("End date cannot be before start date"));
            }
        }

        @ParameterizedTest
        @ValueSource(strings = { "invalid-date", "2025-13-01", "2025-06-32", "not-a-date" })
        @DisplayName("Should handle invalid date formats")
        void testGatherReportCriteria_InvalidDateFormats(String invalidDate) {
            // Arrange
            Scanner scanner = createScanner("1\n4\n" + invalidDate + "\n2025-06-01\n2025-06-05\n");
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = reshelvedItemsReportUI.gatherReportCriteria();

                // Assert
                assertTrue(result); // Should recover after invalid input
                String output = getOutput();
                assertTrue(output.contains("[Invalid] Use format yyyy-MM-dd"));
            }
        }

        @Test
        @DisplayName("Should return false when user goes back from start date")
        void testGatherReportCriteria_UserGoesBackFromStartDate() {
            // Arrange
            Scanner scanner = createScanner("1\n4\n\n"); // In-store + specific range + empty start date
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = reshelvedItemsReportUI.gatherReportCriteria();

                // Assert
                assertFalse(result);
            }
        }

        @Test
        @DisplayName("Should return false when user goes back from end date")
        void testGatherReportCriteria_UserGoesBackFromEndDate() {
            // Arrange
            Scanner scanner = createScanner("1\n4\n2025-06-01\n\n"); // In-store + specific range + start date + empty
                                                                     // end date
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                boolean result = reshelvedItemsReportUI.gatherReportCriteria();

                // Assert
                assertFalse(result);
            }
        }

        @Test
        @DisplayName("Should handle exception during criteria gathering")
        void testGatherReportCriteria_WithException() {
            // Arrange
            Scanner scanner = createScanner("1\n1\n");
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                mockedConsoleUtils.when(() -> ConsoleUtils.clearScreen())
                        .thenThrow(new RuntimeException("Test exception"));

                // Act
                boolean result = reshelvedItemsReportUI.gatherReportCriteria();

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
        @DisplayName("Should fetch in-store reshelved items")
        void testFetchReportItems_InStoreRange() {
            // Arrange
            Scanner scanner = createScanner("1\n1\n"); // In-store + Today
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);
            reshelvedItemsReportUI.gatherReportCriteria();

            List<ReshelvedItemDTO> mockItems = Arrays.asList(
                    createMockReshelvedItem("ITEM001", "Reshelved Item 1", 25),
                    createMockReshelvedItem("ITEM002", "Reshelved Item 2", 15));

            try (MockedStatic<ReshelvedItemsReportService> mockedService = mockStatic(
                    ReshelvedItemsReportService.class)) {
                ReshelvedItemsReportService serviceInstance = mock(ReshelvedItemsReportService.class);
                mockedService.when(() -> new ReshelvedItemsReportService()).thenReturn(serviceInstance);
                when(serviceInstance.getReshelvedItemsForInStoreRange(any(LocalDate.class), any(LocalDate.class)))
                        .thenReturn(mockItems);

                // Act
                List<ReshelvedItemDTO> result = reshelvedItemsReportUI.fetchReportItems();

                // Assert
                assertEquals(2, result.size());
                assertEquals("ITEM001", result.get(0).getCode());
                assertEquals("ITEM002", result.get(1).getCode());
            }
        }

        @Test
        @DisplayName("Should fetch online reshelved items")
        void testFetchReportItems_OnlineRange() {
            // Arrange
            Scanner scanner = createScanner("2\n2\n"); // Online + This Week
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);
            reshelvedItemsReportUI.gatherReportCriteria();

            List<ReshelvedItemDTO> mockItems = Arrays.asList(
                    createMockReshelvedItem("ONLINE001", "Online Reshelved", 30));

            try (MockedStatic<ReshelvedItemsReportService> mockedService = mockStatic(
                    ReshelvedItemsReportService.class)) {
                ReshelvedItemsReportService serviceInstance = mock(ReshelvedItemsReportService.class);
                mockedService.when(() -> new ReshelvedItemsReportService()).thenReturn(serviceInstance);
                when(serviceInstance.getReshelvedItemsForOnlineRange(any(LocalDate.class), any(LocalDate.class)))
                        .thenReturn(mockItems);

                // Act
                List<ReshelvedItemDTO> result = reshelvedItemsReportUI.fetchReportItems();

                // Assert
                assertEquals(1, result.size());
                assertEquals("ONLINE001", result.get(0).getCode());
            }
        }

        @Test
        @DisplayName("Should fetch combined reshelved items")
        void testFetchReportItems_CombinedRange() {
            // Arrange
            Scanner scanner = createScanner("3\n3\n"); // Combined + This Month
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);
            reshelvedItemsReportUI.gatherReportCriteria();

            List<ReshelvedItemDTO> mockItems = Arrays.asList(
                    createMockReshelvedItem("BOTH001", "Combined Reshelved", 50));

            try (MockedStatic<ReshelvedItemsReportService> mockedService = mockStatic(
                    ReshelvedItemsReportService.class)) {
                ReshelvedItemsReportService serviceInstance = mock(ReshelvedItemsReportService.class);
                mockedService.when(() -> new ReshelvedItemsReportService()).thenReturn(serviceInstance);
                when(serviceInstance.getAllReshelvedItemsRange(any(LocalDate.class), any(LocalDate.class)))
                        .thenReturn(mockItems);

                // Act
                List<ReshelvedItemDTO> result = reshelvedItemsReportUI.fetchReportItems();

                // Assert
                assertEquals(1, result.size());
                assertEquals("BOTH001", result.get(0).getCode());
            }
        }

        @Test
        @DisplayName("Should throw exception when criteria is null")
        void testFetchReportItems_WithNullCriteria() {
            // Arrange
            Scanner scanner = createScanner("test");
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);
            // Don't call gatherReportCriteria to leave criteria null

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> reshelvedItemsReportUI.fetchReportItems());
        }
    }

    @Nested
    @DisplayName("Render Report Tests")
    class RenderReportTests {

        @Test
        @DisplayName("Should render report with reshelved items")
        void testRenderReport_WithItems() {
            // Arrange
            Scanner scanner = createScanner("1\n1\n"); // In-store + Today
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);
            reshelvedItemsReportUI.gatherReportCriteria();

            List<ReshelvedItemDTO> items = Arrays.asList(
                    createMockReshelvedItem("ITEM001", "Reshelved Item 1", 25),
                    createMockReshelvedItem("ITEM002", "Reshelved Item 2", 15),
                    createMockReshelvedItem("ITEM003", "Reshelved Item 3", 10));

            // Act
            reshelvedItemsReportUI.renderReport(items);

            // Assert
            String output = getOutput();
            assertTrue(output.contains("SYOS DAILY RESHELVED ITEMS REPORT"));
            assertTrue(output.contains("(Items Moved from Batch to Shelf)"));
            assertTrue(output.contains("Date Range:"));
            assertTrue(output.contains("Store Type: In-Store Reshelving Only"));
            assertTrue(output.contains("ITEM001"));
            assertTrue(output.contains("Reshelved Item 1"));
            assertTrue(output.contains("25"));
            assertTrue(output.contains("ITEM002"));
            assertTrue(output.contains("Reshelved Item 2"));
            assertTrue(output.contains("15"));
            assertTrue(output.contains("ITEM003"));
            assertTrue(output.contains("Reshelved Item 3"));
            assertTrue(output.contains("10"));
            assertTrue(output.contains("Total Items Reshelved from Batches: 50")); // 25 + 15 + 10
        }

        @Test
        @DisplayName("Should render report with empty items list")
        void testRenderReport_WithEmptyItems() {
            // Arrange
            Scanner scanner = createScanner("2\n2\n"); // Online + This Week
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);
            reshelvedItemsReportUI.gatherReportCriteria();

            List<ReshelvedItemDTO> emptyItems = Collections.emptyList();

            // Act
            reshelvedItemsReportUI.renderReport(emptyItems);

            // Assert
            String output = getOutput();
            assertTrue(output.contains("SYOS DAILY RESHELVED ITEMS REPORT"));
            assertTrue(output.contains("Store Type: Online Reshelving Only"));
            assertTrue(output.contains("No reshelving activities found for the selected criteria"));
        }

        @Test
        @DisplayName("Should render report with combined store filter")
        void testRenderReport_CombinedStoreFilter() {
            // Arrange
            Scanner scanner = createScanner("3\n3\n"); // Combined + This Month
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);
            reshelvedItemsReportUI.gatherReportCriteria();

            List<ReshelvedItemDTO> items = Arrays.asList(
                    createMockReshelvedItem("COMBINED001", "Combined Item", 35));

            // Act
            reshelvedItemsReportUI.renderReport(items);

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Store Type: Combined (In-Store + Online)"));
            assertTrue(output.contains("COMBINED001"));
            assertTrue(output.contains("Combined Item"));
            assertTrue(output.contains("35"));
        }

        @Test
        @DisplayName("Should handle long item names with truncation")
        void testRenderReport_WithLongItemNames() {
            // Arrange
            Scanner scanner = createScanner("1\n1\n"); // In-store + Today
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);
            reshelvedItemsReportUI.gatherReportCriteria();

            List<ReshelvedItemDTO> items = Arrays.asList(
                    createMockReshelvedItem("LONG001",
                            "This is a very long item name that should be truncated for proper display", 20));

            // Act
            reshelvedItemsReportUI.renderReport(items);

            // Assert
            String output = getOutput();
            assertTrue(output.contains("LONG001"));
            assertTrue(output.contains("...") || output.contains("This is a very long"));
        }

        @Test
        @DisplayName("Should format date range correctly")
        void testRenderReport_DateRangeFormatting() {
            // Arrange - Test same date (today)
            Scanner scanner = createScanner("1\n1\n"); // In-store + Today
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);
            reshelvedItemsReportUI.gatherReportCriteria();

            List<ReshelvedItemDTO> items = Arrays.asList(
                    createMockReshelvedItem("DATE001", "Date Test Item", 5));

            // Act
            reshelvedItemsReportUI.renderReport(items);

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Date Range:"));
            // Should show single date for today, not a range
        }

        @Test
        @DisplayName("Should format specific date range correctly")
        void testRenderReport_SpecificDateRange() {
            // Arrange
            Scanner scanner = createScanner("1\n4\n2025-06-01\n2025-06-05\n"); // In-store + Specific range
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);
            reshelvedItemsReportUI.gatherReportCriteria();

            List<ReshelvedItemDTO> items = Arrays.asList(
                    createMockReshelvedItem("RANGE001", "Range Test Item", 8));

            // Act
            reshelvedItemsReportUI.renderReport(items);

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Date Range: 2025-06-01 to 2025-06-05"));
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
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);

            // Act
            String title = reshelvedItemsReportUI.getReportTitle();

            // Assert
            assertEquals("SYOS RESHELVED ITEMS REPORT", title);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large quantities")
        void testRenderReport_WithLargeQuantities() {
            // Arrange
            Scanner scanner = createScanner("1\n1\n");
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);
            reshelvedItemsReportUI.gatherReportCriteria();

            List<ReshelvedItemDTO> items = Arrays.asList(
                    createMockReshelvedItem("LARGE001", "Large Quantity Item", 999999));

            // Act
            reshelvedItemsReportUI.renderReport(items);

            // Assert
            String output = getOutput();
            assertTrue(output.contains("999999"));
            assertTrue(output.contains("Total Items Reshelved from Batches: 999999"));
        }

        @Test
        @DisplayName("Should handle zero quantities")
        void testRenderReport_WithZeroQuantities() {
            // Arrange
            Scanner scanner = createScanner("2\n2\n");
            reshelvedItemsReportUI = new ReshelvedItemsReportUI(scanner);
            reshelvedItemsReportUI.gatherReportCriteria();

            List<ReshelvedItemDTO> items = Arrays.asList(
                    createMockReshelvedItem("ZERO001", "Zero Quantity Item", 0));

            // Act
            reshelvedItemsReportUI.renderReport(items);

            // Assert
            String output = getOutput();
            assertTrue(output.contains("ZERO001"));
            assertTrue(output.contains("0"));
            assertTrue(output.contains("Total Items Reshelved from Batches: 0"));
        }
    }

    // Helper method
    private ReshelvedItemDTO createMockReshelvedItem(String code, String name, int quantity) {
        ReshelvedItemDTO item = mock(ReshelvedItemDTO.class);
        when(item.getCode()).thenReturn(code);
        when(item.getName()).thenReturn(name);
        when(item.getQuantity()).thenReturn(quantity);
        return item;
    }
}
