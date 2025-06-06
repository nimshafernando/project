package syos.ui.views;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import syos.data.BatchGateway;
import syos.data.ItemGateway;
import syos.service.BatchService;
import syos.util.ConsoleUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Comprehensive test suite for AutoCleanupExpiredBatchesUI
 * Tests all methods with 100% coverage including happy paths and edge cases
 */
@DisplayName("AutoCleanupExpiredBatchesUI Tests")
class AutoCleanupExpiredBatchesUITest {

    @Mock
    private BatchService mockBatchService;

    @Mock
    private BatchGateway mockBatchGateway;

    @Mock
    private ItemGateway mockItemGateway;

    private AutoCleanupExpiredBatchesUI cleanupUI;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private AutoCloseable mocks;
    private MockedStatic<ConsoleUtils> mockedConsoleUtils;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        mockedConsoleUtils = mockStatic(ConsoleUtils.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        mockedConsoleUtils.close();
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
        @DisplayName("Should create AutoCleanupExpiredBatchesUI with valid scanner")
        void shouldCreateAutoCleanupExpiredBatchesUIWithValidScanner() {
            // Arrange
            Scanner scanner = createScanner("n\n");

            // Act
            AutoCleanupExpiredBatchesUI ui = new AutoCleanupExpiredBatchesUI(scanner);

            // Assert
            assertNotNull(ui);
        }

        @Test
        @DisplayName("Should create AutoCleanupExpiredBatchesUI with null scanner")
        void shouldCreateAutoCleanupExpiredBatchesUIWithNullScanner() {
            // Arrange & Act
            AutoCleanupExpiredBatchesUI ui = new AutoCleanupExpiredBatchesUI(null);

            // Assert
            assertNotNull(ui);
        }
    }

    @Nested
    @DisplayName("Start Method Tests")
    class StartMethodTests {

        @Test
        @DisplayName("Should display header and prompt correctly")
        void shouldDisplayHeaderAndPromptCorrectly() {
            // Arrange
            Scanner scanner = createScanner("n\n");
            cleanupUI = new AutoCleanupExpiredBatchesUI(scanner);

            // Act
            cleanupUI.start();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("=== AUTO CLEANUP EXPIRED BATCHES ==="));
            assertTrue(output.contains("Run auto cleanup of expired batches? (y/n):"));
            assertTrue(output.contains("Cleanup cancelled."));

            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
            mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }

        @ParameterizedTest
        @ValueSource(strings = { "y", "Y", "yes", "YES", "Yes", "yEs" })
        @DisplayName("Should run cleanup when user confirms with various yes inputs")
        void shouldRunCleanupWhenUserConfirmsWithVariousYesInputs(String confirmInput) {
            // Arrange
            Scanner scanner = createScanner(confirmInput + "\n");
            cleanupUI = new AutoCleanupExpiredBatchesUI(scanner);

            // Use reflection to inject mock BatchService
            try {
                java.lang.reflect.Field batchServiceField = AutoCleanupExpiredBatchesUI.class
                        .getDeclaredField("batchService");
                batchServiceField.setAccessible(true);
                batchServiceField.set(cleanupUI, mockBatchService);

                doNothing().when(mockBatchService).autoCleanupExpiredBatches();
            } catch (Exception e) {
                fail("Failed to inject mock BatchService: " + e.getMessage());
            }

            // Act
            cleanupUI.start();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Running auto cleanup..."));
            assertTrue(output.contains("Cleanup completed!"));

            verify(mockBatchService).autoCleanupExpiredBatches();
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
            mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }

        @ParameterizedTest
        @ValueSource(strings = { "n", "N", "no", "NO", "No", "nO" })
        @DisplayName("Should cancel cleanup when user declines with various no inputs")
        void shouldCancelCleanupWhenUserDeclinesWithVariousNoInputs(String declineInput) {
            // Arrange
            Scanner scanner = createScanner(declineInput + "\n");
            cleanupUI = new AutoCleanupExpiredBatchesUI(scanner);

            // Use reflection to inject mock BatchService
            try {
                java.lang.reflect.Field batchServiceField = AutoCleanupExpiredBatchesUI.class
                        .getDeclaredField("batchService");
                batchServiceField.setAccessible(true);
                batchServiceField.set(cleanupUI, mockBatchService);
            } catch (Exception e) {
                fail("Failed to inject mock BatchService: " + e.getMessage());
            }

            // Act
            cleanupUI.start();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Cleanup cancelled."));
            assertFalse(output.contains("Running auto cleanup..."));
            assertFalse(output.contains("Cleanup completed!"));

            verify(mockBatchService, never()).autoCleanupExpiredBatches();
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
            mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }

        @ParameterizedTest
        @ValueSource(strings = { "maybe", "sure", "ok", "1", "true", "", "   ", "cancel", "quit" })
        @DisplayName("Should cancel cleanup for invalid or ambiguous inputs")
        void shouldCancelCleanupForInvalidOrAmbiguousInputs(String invalidInput) {
            // Arrange
            Scanner scanner = createScanner(invalidInput + "\n");
            cleanupUI = new AutoCleanupExpiredBatchesUI(scanner);

            // Use reflection to inject mock BatchService
            try {
                java.lang.reflect.Field batchServiceField = AutoCleanupExpiredBatchesUI.class
                        .getDeclaredField("batchService");
                batchServiceField.setAccessible(true);
                batchServiceField.set(cleanupUI, mockBatchService);
            } catch (Exception e) {
                fail("Failed to inject mock BatchService: " + e.getMessage());
            }

            // Act
            cleanupUI.start();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Cleanup cancelled."));
            assertFalse(output.contains("Running auto cleanup..."));
            assertFalse(output.contains("Cleanup completed!"));

            verify(mockBatchService, never()).autoCleanupExpiredBatches();
        }

        @Test
        @DisplayName("Should handle batch service exception gracefully")
        void shouldHandleBatchServiceExceptionGracefully() {
            // Arrange
            Scanner scanner = createScanner("y\n");
            cleanupUI = new AutoCleanupExpiredBatchesUI(scanner);

            // Use reflection to inject mock BatchService
            try {
                java.lang.reflect.Field batchServiceField = AutoCleanupExpiredBatchesUI.class
                        .getDeclaredField("batchService");
                batchServiceField.setAccessible(true);
                batchServiceField.set(cleanupUI, mockBatchService);

                doThrow(new RuntimeException("Database connection failed")).when(mockBatchService)
                        .autoCleanupExpiredBatches();
            } catch (Exception e) {
                fail("Failed to inject mock BatchService: " + e.getMessage());
            }

            // Act
            cleanupUI.start();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Running auto cleanup..."));
            assertTrue(output.contains("Error: Database connection failed"));
            assertFalse(output.contains("Cleanup completed!"));

            verify(mockBatchService).autoCleanupExpiredBatches();
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
            mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }

        @Test
        @DisplayName("Should handle scanner input exception gracefully")
        void shouldHandleScannerInputExceptionGracefully() {
            // Arrange
            Scanner mockScanner = mock(Scanner.class);
            cleanupUI = new AutoCleanupExpiredBatchesUI(mockScanner);

            when(mockScanner.nextLine()).thenThrow(new RuntimeException("Scanner closed unexpectedly"));

            // Act & Assert - Should not throw exception
            assertDoesNotThrow(() -> cleanupUI.start());

            String output = getOutput();
            assertTrue(output.contains("Error: Scanner closed unexpectedly"));

            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
        }

        @Test
        @DisplayName("Should handle empty input gracefully")
        void shouldHandleEmptyInputGracefully() {
            // Arrange
            Scanner scanner = createScanner("\n");
            cleanupUI = new AutoCleanupExpiredBatchesUI(scanner);

            // Use reflection to inject mock BatchService
            try {
                java.lang.reflect.Field batchServiceField = AutoCleanupExpiredBatchesUI.class
                        .getDeclaredField("batchService");
                batchServiceField.setAccessible(true);
                batchServiceField.set(cleanupUI, mockBatchService);
            } catch (Exception e) {
                fail("Failed to inject mock BatchService: " + e.getMessage());
            }

            // Act
            cleanupUI.start();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Cleanup cancelled."));
            verify(mockBatchService, never()).autoCleanupExpiredBatches();
        }

        @Test
        @DisplayName("Should handle whitespace input gracefully")
        void shouldHandleWhitespaceInputGracefully() {
            // Arrange
            Scanner scanner = createScanner("   \t   \n");
            cleanupUI = new AutoCleanupExpiredBatchesUI(scanner);

            // Use reflection to inject mock BatchService
            try {
                java.lang.reflect.Field batchServiceField = AutoCleanupExpiredBatchesUI.class
                        .getDeclaredField("batchService");
                batchServiceField.setAccessible(true);
                batchServiceField.set(cleanupUI, mockBatchService);
            } catch (Exception e) {
                fail("Failed to inject mock BatchService: " + e.getMessage());
            }

            // Act
            cleanupUI.start();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Cleanup cancelled."));
            verify(mockBatchService, never()).autoCleanupExpiredBatches();
        }

        @Test
        @DisplayName("Should handle successful cleanup with specific business scenarios")
        void shouldHandleSuccessfulCleanupWithSpecificBusinessScenarios() {
            // Arrange
            Scanner scanner = createScanner("YES\n");
            cleanupUI = new AutoCleanupExpiredBatchesUI(scanner);

            // Use reflection to inject mock BatchService
            try {
                java.lang.reflect.Field batchServiceField = AutoCleanupExpiredBatchesUI.class
                        .getDeclaredField("batchService");
                batchServiceField.setAccessible(true);
                batchServiceField.set(cleanupUI, mockBatchService);

                // Simulate successful cleanup of multiple expired batches
                doNothing().when(mockBatchService).autoCleanupExpiredBatches();
            } catch (Exception e) {
                fail("Failed to inject mock BatchService: " + e.getMessage());
            }

            // Act
            cleanupUI.start();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("=== AUTO CLEANUP EXPIRED BATCHES ==="));
            assertTrue(output.contains("Run auto cleanup of expired batches? (y/n):"));
            assertTrue(output.contains("Running auto cleanup..."));
            assertTrue(output.contains("Cleanup completed!"));

            verify(mockBatchService, times(1)).autoCleanupExpiredBatches();
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
            mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }

        @Test
        @DisplayName("Should handle complex error scenarios during cleanup")
        void shouldHandleComplexErrorScenariosDuringCleanup() {
            // Arrange
            Scanner scanner = createScanner("y\n");
            cleanupUI = new AutoCleanupExpiredBatchesUI(scanner);

            // Use reflection to inject mock BatchService
            try {
                java.lang.reflect.Field batchServiceField = AutoCleanupExpiredBatchesUI.class
                        .getDeclaredField("batchService");
                batchServiceField.setAccessible(true);
                batchServiceField.set(cleanupUI, mockBatchService);

                // Simulate specific business error
                doThrow(new IllegalStateException("Batch cleanup failed due to concurrent access"))
                        .when(mockBatchService).autoCleanupExpiredBatches();
            } catch (Exception e) {
                fail("Failed to inject mock BatchService: " + e.getMessage());
            }

            // Act
            cleanupUI.start();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Running auto cleanup..."));
            assertTrue(output.contains("Error: Batch cleanup failed due to concurrent access"));
            assertFalse(output.contains("Cleanup completed!"));

            verify(mockBatchService).autoCleanupExpiredBatches();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should integrate properly with ConsoleUtils")
        void shouldIntegrateProperlyWithConsoleUtils() {
            // Arrange
            Scanner scanner = createScanner("n\n");
            cleanupUI = new AutoCleanupExpiredBatchesUI(scanner);

            // Act
            cleanupUI.start();

            // Assert
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
            mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }

        @Test
        @DisplayName("Should handle multiple consecutive runs")
        void shouldHandleMultipleConsecutiveRuns() {
            // Arrange
            Scanner scanner1 = createScanner("y\n");
            Scanner scanner2 = createScanner("n\n");

            AutoCleanupExpiredBatchesUI ui1 = new AutoCleanupExpiredBatchesUI(scanner1);
            AutoCleanupExpiredBatchesUI ui2 = new AutoCleanupExpiredBatchesUI(scanner2);

            // Use reflection to inject mock BatchService for both instances
            try {
                java.lang.reflect.Field batchServiceField = AutoCleanupExpiredBatchesUI.class
                        .getDeclaredField("batchService");
                batchServiceField.setAccessible(true);
                batchServiceField.set(ui1, mockBatchService);
                batchServiceField.set(ui2, mockBatchService);

                doNothing().when(mockBatchService).autoCleanupExpiredBatches();
            } catch (Exception e) {
                fail("Failed to inject mock BatchService: " + e.getMessage());
            }

            // Act
            ui1.start();
            ui2.start();

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Cleanup completed!"));
            assertTrue(output.contains("Cleanup cancelled."));

            verify(mockBatchService, times(1)).autoCleanupExpiredBatches();
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(2));
            mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(2));
        }
    }
}
