package syos.ui.views;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import syos.interfaces.IReportService;
import syos.util.ConsoleUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AbstractReportUI Tests")
class AbstractReportUITest {

    private TestableAbstractReportUI testableReportUI;
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
            testableReportUI = new TestableAbstractReportUI(validScanner);

            // Assert
            assertNotNull(testableReportUI);
            assertEquals("Test Report", testableReportUI.getReportTitle());
        }

        @Test
        @DisplayName("Should handle null scanner")
        void testConstructor_WithNullScanner() {
            // Act & Assert
            assertDoesNotThrow(() -> new TestableAbstractReportUI(null));
        }
    }

    @Nested
    @DisplayName("Display Method Tests")
    class DisplayMethodTests {

        @Test
        @DisplayName("Should complete successful workflow")
        void testDisplay_SuccessfulWorkflow() {
            // Arrange
            Scanner scanner = createScanner("\n"); // Enter to continue
            testableReportUI = new TestableAbstractReportUI(scanner);
            testableReportUI.setGatherCriteriaResult(true);
            testableReportUI.setMockData(Arrays.asList("Item1", "Item2", "Item3"));

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                testableReportUI.display();

                // Assert
                String output = getOutput();
                assertTrue(output.contains("Rendering report with 3 items:"));
                assertTrue(output.contains("- Item1"));
                assertTrue(output.contains("- Item2"));
                assertTrue(output.contains("- Item3"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(1));
            }
        }

        @Test
        @DisplayName("Should handle user cancellation during criteria gathering")
        void testDisplay_UserCancellation() {
            // Arrange
            Scanner scanner = createScanner("\n"); // Enter to continue
            testableReportUI = new TestableAbstractReportUI(scanner);
            testableReportUI.setGatherCriteriaResult(false); // User cancels

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                testableReportUI.display();

                // Assert
                String output = getOutput();
                assertTrue(output.contains("Returning to reports menu"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(1));
            }
        }

        @Test
        @DisplayName("Should handle empty results")
        void testDisplay_EmptyResults() {
            // Arrange
            Scanner scanner = createScanner("\n"); // Enter to continue
            testableReportUI = new TestableAbstractReportUI(scanner);
            testableReportUI.setGatherCriteriaResult(true);
            testableReportUI.setMockData(Collections.emptyList());

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                testableReportUI.display();

                // Assert
                String output = getOutput();
                assertTrue(output.contains("No data found matching your criteria"));
                assertTrue(output.contains("No records found for the selected criteria"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(1));
            }
        }

        @Test
        @DisplayName("Should handle null results")
        void testDisplay_NullResults() {
            // Arrange
            Scanner scanner = createScanner("\n"); // Enter to continue
            testableReportUI = new TestableAbstractReportUI(scanner);
            testableReportUI.setGatherCriteriaResult(true);
            testableReportUI.setMockData(null);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                testableReportUI.display();

                // Assert
                String output = getOutput();
                assertTrue(output.contains("No data found matching your criteria"));
                assertTrue(output.contains("No records found for the selected criteria"));

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(1));
            }
        }

    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should clear screen")
        void testClearScreen() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // Act
                testableReportUI.clearScreen();

                // Assert
                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen());
            }
        }

        @Test
        @DisplayName("Should display report header")
        void testDisplayReportHeader() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act
            testableReportUI.displayReportHeader();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Test Report"));
            assertTrue(output.contains("Generated:"));
            assertTrue(output.contains("========================================"));
        }

        @Test
        @DisplayName("Should wait for enter")
        void testWaitForEnter() {
            // Arrange
            Scanner scanner = createScanner("\n"); // Enter
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act
            testableReportUI.waitForEnter();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Press Enter to continue"));
        }

        @Test
        @DisplayName("Should wait for enter with NoSuchElementException")
        void testWaitForEnter_WithException() {
            // Arrange
            Scanner scanner = mock(Scanner.class);
            when(scanner.nextLine()).thenThrow(new NoSuchElementException());
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act
            testableReportUI.waitForEnter();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Press Enter to continue"));
            assertTrue(output.contains("[System] Input unavailable, continuing"));
        }

        @Test
        @DisplayName("Should format numbers correctly")
        void testFormatNumber() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act & Assert
            assertEquals("1,000", testableReportUI.formatNumber(1000));
            assertEquals("1,234,567", testableReportUI.formatNumber(1234567));
            assertEquals("0", testableReportUI.formatNumber(0));
        }

        @Test
        @DisplayName("Should format currency correctly")
        void testFormatCurrency() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act & Assert
            assertEquals("Rs. 100.00", testableReportUI.formatCurrency(100.0));
            assertEquals("Rs. 1234.56", testableReportUI.formatCurrency(1234.56));
            assertEquals("Rs. 0.00", testableReportUI.formatCurrency(0.0));
        }

        @Test
        @DisplayName("Should get current timestamp")
        void testGetCurrentTimestamp() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act
            String timestamp = testableReportUI.getCurrentTimestamp();

            // Assert
            assertNotNull(timestamp);
            assertFalse(timestamp.isEmpty());
            assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
        }

        @Test
        @DisplayName("Should check success status correctly")
        void testWasSuccessful() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act & Assert
            assertTrue(testableReportUI.wasSuccessful()); // Default state should be successful

            // Simulate error
            testableReportUI.setHasError(true);
            assertFalse(testableReportUI.wasSuccessful());
        }

        @Test
        @DisplayName("Should get execution stats")
        void testGetExecutionStats() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Test with no execution data
            String stats = testableReportUI.getExecutionStats();
            assertTrue(stats.contains("No execution data available"));

            // Test with execution data
            testableReportUI.setExecutionTimes();
            testableReportUI.setRecordCount(10);
            stats = testableReportUI.getExecutionStats();
            assertTrue(stats.contains("Records: 10"));
            assertTrue(stats.contains("Duration:"));
            assertTrue(stats.contains("Success"));
        }

        @Test
        @DisplayName("Should prompt repeat correctly")
        void testPromptRepeat() {
            // Test with "y" response
            Scanner scanner = createScanner("y\n");
            testableReportUI = new TestableAbstractReportUI(scanner);
            assertTrue(testableReportUI.promptRepeat());

            // Test with "no" response
            scanner = createScanner("no\n");
            testableReportUI = new TestableAbstractReportUI(scanner);
            assertFalse(testableReportUI.promptRepeat());

            // Test with exception
            scanner = mock(Scanner.class);
            when(scanner.nextLine()).thenThrow(new NoSuchElementException());
            testableReportUI = new TestableAbstractReportUI(scanner);
            assertFalse(testableReportUI.promptRepeat());
        }

        @Test
        @DisplayName("Should pause for user")
        void testPauseForUser() {
            // Arrange
            Scanner scanner = createScanner("\n");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act
            testableReportUI.pauseForUser();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Press Enter to continue"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle empty results")
        void testHandleEmptyResults() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act
            testableReportUI.handleEmptyResults();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("No data found matching your criteria"));
            assertTrue(output.contains("Try adjusting your search parameters"));
        }

        @Test
        @DisplayName("Should show error message")
        void testShowError() {
            // Arrange
            Scanner scanner = createScanner("\n"); // Enter to continue
            testableReportUI = new TestableAbstractReportUI(scanner);
            Exception testException = new RuntimeException("Test error message");

            // Act
            testableReportUI.showError(testException);

            // Assert
            String output = getOutput();
            assertTrue(output.contains("[Error] Test error message"));
            assertTrue(output.contains("Press Enter to return to reports menu"));
        }

        @Test
        @DisplayName("Should show error with scanner exception")
        void testShowError_WithScannerException() {
            // Arrange
            Scanner scanner = mock(Scanner.class);
            when(scanner.nextLine()).thenThrow(new NoSuchElementException());
            testableReportUI = new TestableAbstractReportUI(scanner);
            Exception testException = new RuntimeException("Test error message");

            // Act
            testableReportUI.showError(testException);

            // Assert
            String output = getOutput();
            assertTrue(output.contains("[Error] Test error message"));
            assertTrue(output.contains("[System] Input unavailable, continuing"));
        }

        @Test
        @DisplayName("Should handle validation failure")
        void testHandleValidationFailure() {
            // Arrange
            Scanner scanner = createScanner("\n"); // Enter to continue
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act
            testableReportUI.handleValidationFailure();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Invalid criteria provided"));
            assertTrue(output.contains("Please try again"));
        }

        @Test
        @DisplayName("Should handle cancellation")
        void testHandleCancellation() {
            // Arrange
            Scanner scanner = createScanner("\n"); // Enter to continue
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act
            testableReportUI.handleCancellation();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Report generation cancelled"));
        }
    }

    @Nested
    @DisplayName("Report Header and Footer Tests")
    class ReportHeaderFooterTests {

        @Test
        @DisplayName("Should render report header with record count")
        void testRenderReportHeader() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);
            List<String> data = Arrays.asList("Item1", "Item2");

            // Act
            testableReportUI.renderReportHeader(data);

            // Assert
            String output = getOutput();
            assertTrue(output.contains("--- Report Results ---"));
            assertTrue(output.contains("Total Records: 0")); // Initial count
        }

        @Test
        @DisplayName("Should render report footer with execution time")
        void testRenderReportFooter() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);
            testableReportUI.setExecutionTimes(); // Set start and end times
            List<String> data = Arrays.asList("Item1", "Item2");

            // Act
            testableReportUI.renderReportFooter(data);

            // Assert
            String output = getOutput();
            assertTrue(output.contains("--- End of Report ---"));
            assertTrue(output.contains("Execution time:"));
            assertTrue(output.contains("ms"));
        }

        @Test
        @DisplayName("Should show processing message")
        void testShowProcessingMessage() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act
            testableReportUI.showProcessingMessage();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Generating report, please wait"));
        }

        @Test
        @DisplayName("Should show no data message")
        void testShowNoDataMessage() {
            // Arrange
            Scanner scanner = createScanner("\n");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act
            testableReportUI.showNoDataMessage();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("No records found for the selected criteria"));
            assertTrue(output.contains("Try selecting a different date or criteria"));
        }
    }

    @Nested
    @DisplayName("Hook Method Tests")
    class HookMethodTests {

        @Test
        @DisplayName("Should initialize report")
        void testInitializeReport() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act
            testableReportUI.initializeReport();

            // Assert - No exception should be thrown
            assertDoesNotThrow(() -> testableReportUI.initializeReport());
        }

        @Test
        @DisplayName("Should execute before report execution hook")
        void testBeforeReportExecution() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act & Assert - No exception should be thrown
            assertDoesNotThrow(() -> testableReportUI.beforeReportExecution());
        }

        @Test
        @DisplayName("Should execute after report execution hook")
        void testAfterReportExecution() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);
            List<String> data = Arrays.asList("Item1", "Item2");

            // Act & Assert - No exception should be thrown
            assertDoesNotThrow(() -> testableReportUI.afterReportExecution(data));
        }

        @Test
        @DisplayName("Should validate criteria")
        void testValidateCriteria() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act & Assert
            assertTrue(testableReportUI.validateCriteria()); // Default should be true
        }

        @Test
        @DisplayName("Should process report data")
        void testProcessReportData() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);
            List<String> data = Arrays.asList("Item1", "Item2");

            // Act
            List<String> result = testableReportUI.processReportData(data);

            // Assert
            assertEquals(data, result); // Default should return same data
        }

        @Test
        @DisplayName("Should validate results")
        void testValidateResults() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Test with valid data
            List<String> data = Arrays.asList("Item1", "Item2");
            assertTrue(testableReportUI.validateResults(data));

            // Test with empty data
            assertFalse(testableReportUI.validateResults(Collections.emptyList()));

            // Test with null data
            assertFalse(testableReportUI.validateResults(null));
        }

        @Test
        @DisplayName("Should offer export options")
        void testOfferExportOptions() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);
            List<String> data = Arrays.asList("Item1", "Item2");

            // Act & Assert - No exception should be thrown
            assertDoesNotThrow(() -> testableReportUI.offerExportOptions(data));
        }

        @Test
        @DisplayName("Should cleanup report")
        void testCleanupReport() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act & Assert - No exception should be thrown
            assertDoesNotThrow(() -> testableReportUI.cleanupReport());
        }
    }

    @Nested
    @DisplayName("Report Service Integration Tests")
    class ReportServiceIntegrationTests {

        @Test
        @DisplayName("Should return null report service by default")
        void testGetReportService_Default() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act & Assert
            assertNull(testableReportUI.getReportService());
        }

        @Test
        @DisplayName("Should execute report service when available")
        void testExecuteReportService_WithService() throws Exception {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            IReportService<String> mockService = mock(IReportService.class);
            List<String> expectedData = Arrays.asList("Service Item 1", "Service Item 2");
            when(mockService.generateReport()).thenReturn(expectedData);

            testableReportUI.setMockReportService(mockService);

            // Act
            List<String> result = testableReportUI.executeReportService();

            // Assert
            assertEquals(expectedData, result);
            verify(mockService).generateReport();
        }

        @Test
        @DisplayName("Should throw exception when no service available")
        void testExecuteReportService_NoService() {
            // Arrange
            Scanner scanner = createScanner("test");
            testableReportUI = new TestableAbstractReportUI(scanner);

            // Act & Assert
            UnsupportedOperationException exception = assertThrows(
                    UnsupportedOperationException.class,
                    () -> testableReportUI.executeReportService());
            assertTrue(exception.getMessage().contains("No report service available"));
        }
    }

    // Testable concrete implementation of AbstractReportUI for testing
    private static class TestableAbstractReportUI extends AbstractReportUI<String> {
        private boolean gatherCriteriaResult = true;
        private List<String> mockData = Arrays.asList("Default Item");
        private boolean throwException = false;
        private boolean hasError = false;
        private IReportService<String> mockReportService;

        public TestableAbstractReportUI(Scanner scanner) {
            super(scanner);
        }

        @Override
        protected String getReportTitle() {
            return "Test Report";
        }

        @Override
        protected boolean gatherReportCriteria() {
            return gatherCriteriaResult;
        }

        @Override
        protected List<String> fetchReportItems() throws Exception {
            if (throwException) {
                throw new RuntimeException("Test exception");
            }
            return mockData;
        }

        @Override
        protected void renderReport(List<String> items) {
            System.out.println("Rendering report with " + items.size() + " items:");
            for (String item : items) {
                System.out.println("- " + item);
            }
        }

        @Override
        protected IReportService<String> getReportService() {
            return mockReportService;
        }

        // Test helper methods
        public void setGatherCriteriaResult(boolean result) {
            this.gatherCriteriaResult = result;
        }

        public void setMockData(List<String> data) {
            this.mockData = data;
        }

        public void setThrowException(boolean throwException) {
            this.throwException = throwException;
        }

        public void setHasError(boolean hasError) {
            this.hasError = hasError;
            if (hasError) {
                this.lastError = new RuntimeException("Test error");
            } else {
                this.lastError = null;
            }
        }

        public void setExecutionTimes() {
            this.executionStartTime = LocalDateTime.now().minusSeconds(1);
            this.executionEndTime = LocalDateTime.now();
        }

        public void setRecordCount(int count) {
            this.recordCount = count;
        }

        public void setMockReportService(IReportService<String> service) {
            this.mockReportService = service;
        }

        // Expose protected methods for testing
        @Override
        public void clearScreen() {
            super.clearScreen();
        }

        @Override
        public void displayReportHeader() {
            super.displayReportHeader();
        }

        @Override
        public void waitForEnter() {
            super.waitForEnter();
        }

        @Override
        public String formatNumber(Number number) {
            return super.formatNumber(number);
        }

        @Override
        public String formatCurrency(double amount) {
            return super.formatCurrency(amount);
        }

        @Override
        public String getCurrentTimestamp() {
            return super.getCurrentTimestamp();
        }

        @Override
        public boolean wasSuccessful() {
            return super.wasSuccessful();
        }

        @Override
        public String getExecutionStats() {
            return super.getExecutionStats();
        }

        @Override
        public boolean promptRepeat() {
            return super.promptRepeat();
        }

        @Override
        public void pauseForUser() {
            super.pauseForUser();
        }

        @Override
        public void handleEmptyResults() {
            super.handleEmptyResults();
        }

        @Override
        public void handleFetchError(Exception e) {
            super.handleFetchError(e);
        }

        @Override
        public void handleRenderError(Exception e) {
            super.handleRenderError(e);
        }

        @Override
        public void showError(Exception e) {
            super.showError(e);
        }

        @Override
        public void handleValidationFailure() {
            super.handleValidationFailure();
        }

        @Override
        public void handleCancellation() {
            super.handleCancellation();
        }

        @Override
        public void renderReportHeader(List<String> reportData) {
            super.renderReportHeader(reportData);
        }

        @Override
        public void renderReportFooter(List<String> reportData) {
            super.renderReportFooter(reportData);
        }

        @Override
        public void showProcessingMessage() {
            super.showProcessingMessage();
        }

        @Override
        public void showNoDataMessage() {
            super.showNoDataMessage();
        }

        // Hook methods
        @Override
        public void initializeReport() {
            super.initializeReport();
        }

        @Override
        public void beforeReportExecution() {
            super.beforeReportExecution();
        }

        @Override
        public void afterReportExecution(List<String> reportData) {
            super.afterReportExecution(reportData);
        }

        @Override
        public boolean validateCriteria() {
            return super.validateCriteria();
        }

        @Override
        public List<String> processReportData(List<String> rawData) {
            return super.processReportData(rawData);
        }

        @Override
        public boolean validateResults(List<String> reportData) {
            return super.validateResults(reportData);
        }

        @Override
        public void offerExportOptions(List<String> reportData) {
            super.offerExportOptions(reportData);
        }

        @Override
        public void cleanupReport() {
            super.cleanupReport();
        }

        @Override
        public List<String> executeReportService() throws Exception {
            return super.executeReportService();
        }
    }
}
