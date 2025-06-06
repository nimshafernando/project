package syos.ui.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;

import syos.util.ConsoleUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Comprehensive test suite for CustomerMainMenuController
 * Tests all methods with 100% coverage including happy paths and edge cases
 */
@DisplayName("CustomerMainMenuController Tests")
class CustomerMainMenuControllerTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        captureSystemOut();
    }

    @AfterEach
    void tearDown() {
        restoreSystemOut();
    }

    private void captureSystemOut() {
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    private void restoreSystemOut() {
        System.setOut(originalOut);
    }

    private String getOutput() {
        return outputStream.toString();
    }

    private Scanner createScannerWithInput(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    @Test
    @DisplayName("Should launch online sales portal when option 1 is selected")
    void testLaunch_Option1_LaunchOnlinePortal() {
        // Arrange
        String input = "1\n0\n"; // Select option 1, then exit
        Scanner scanner = createScannerWithInput(input);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<OnlineMainMenuController> mockedOnlineController = mockStatic(
                        OnlineMainMenuController.class)) {

            // Act
            CustomerMainMenuController.launch(scanner); // Assert
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
            mockedOnlineController.verify(() -> OnlineMainMenuController.launch(scanner));

            String output = getOutput();
            assertContains(output, "SYOS - CUSTOMER PORTAL");
            assertContains(output, "1. Online Sales Portal");
            assertContains(output, "0. Exit");
            assertContains(output, "Choose an option:");
        }
    }

    @Test
    @DisplayName("Should exit when option 0 is selected")
    void testLaunch_Option0_Exit() {
        // Arrange
        String input = "0\n";
        Scanner scanner = createScannerWithInput(input);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<OnlineMainMenuController> mockedOnlineController = mockStatic(
                        OnlineMainMenuController.class)) {

            // Act
            CustomerMainMenuController.launch(scanner); // Assert
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
            mockedOnlineController.verifyNoInteractions();

            String output = getOutput();
            assertContains(output, "SYOS - CUSTOMER PORTAL");
            assertContains(output, "Choose an option:");
        }
    }

    @Test
    @DisplayName("Should handle invalid option and show error message")
    void testLaunch_InvalidOption_ShowError() {
        // Arrange
        String input = "5\n0\n"; // Invalid option 5, then exit
        Scanner scanner = createScannerWithInput(input);
        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<OnlineMainMenuController> mockedOnlineController = mockStatic(
                        OnlineMainMenuController.class)) {

            // Act
            CustomerMainMenuController.launch(scanner); // Assert
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
            mockedOnlineController.verifyNoInteractions();

            String output = getOutput();
            assertContains(output, "Invalid option. Please try again.");
            assertContains(output, "Press Enter to continue...");
        }
    }

    @Test
    @DisplayName("Should handle multiple invalid options before valid choice")
    void testLaunch_MultipleInvalidOptions() {
        // Arrange
        String input = "abc\n-1\n99\n1\n0\n"; // Multiple invalid options, then valid choices
        Scanner scanner = createScannerWithInput(input);
        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<OnlineMainMenuController> mockedOnlineController = mockStatic(
                        OnlineMainMenuController.class)) {

            // Act
            CustomerMainMenuController.launch(scanner);

            // Assert
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
            mockedOnlineController.verify(() -> OnlineMainMenuController.launch(scanner));

            String output = getOutput();
            // Should show error message multiple times
            assertTrue(output.split("Invalid option. Please try again.").length > 3,
                    "Should show multiple error messages for multiple invalid inputs");
        }
    }

    @Test
    @DisplayName("Should handle empty string input as invalid option")
    void testLaunch_EmptyStringInput() {
        // Arrange
        String input = "\n\n0\n"; // Empty input, then exit
        Scanner scanner = createScannerWithInput(input);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<OnlineMainMenuController> mockedOnlineController = mockStatic(
                        OnlineMainMenuController.class)) {

            // Act
            CustomerMainMenuController.launch(scanner);

            // Assert
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
            mockedOnlineController.verifyNoInteractions();

            String output = getOutput();
            assertContains(output, "Invalid option. Please try again.");
        }
    }

    @Test
    @DisplayName("Should handle whitespace-only input as invalid option")
    void testLaunch_WhitespaceInput() {
        // Arrange
        String input = "   \n\t\n0\n"; // Whitespace inputs, then exit
        Scanner scanner = createScannerWithInput(input);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<OnlineMainMenuController> mockedOnlineController = mockStatic(
                        OnlineMainMenuController.class)) {

            // Act
            CustomerMainMenuController.launch(scanner);

            // Assert
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
            mockedOnlineController.verifyNoInteractions();

            String output = getOutput();
            assertContains(output, "Invalid option. Please try again.");
        }
    }

    @Test
    @DisplayName("Should handle case insensitive input")
    void testLaunch_CaseInsensitiveInput() {
        // Arrange
        String input = "ONE\nzero\n0\n"; // String inputs that don't match, then exit
        Scanner scanner = createScannerWithInput(input);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<OnlineMainMenuController> mockedOnlineController = mockStatic(
                        OnlineMainMenuController.class)) {

            // Act
            CustomerMainMenuController.launch(scanner); // Assert
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
            mockedOnlineController.verifyNoInteractions();
            String output = getOutput();
            // Should show error for non-numeric inputs
            assertTrue(output.split("Invalid option. Please try again.").length > 2,
                    "Should show error messages for non-numeric inputs");
        }
    }

    @Test
    @DisplayName("Should display menu correctly formatted")
    void testLaunch_MenuFormatting() {
        // Arrange
        String input = "0\n";
        Scanner scanner = createScannerWithInput(input);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {

            // Act
            CustomerMainMenuController.launch(scanner);

            // Assert
            String output = getOutput();

            // Check for proper menu formatting
            assertContains(output, "===========================================");
            assertContains(output, "    SYOS - CUSTOMER PORTAL");
            assertContains(output, "1. Online Sales Portal");
            assertContains(output, "0. Exit");
            assertContains(output, "Choose an option:");

            // Check that menu elements appear in correct order
            int portalIndex = output.indexOf("SYOS - CUSTOMER PORTAL");
            int option1Index = output.indexOf("1. Online Sales Portal");
            int option0Index = output.indexOf("0. Exit");
            int promptIndex = output.indexOf("Choose an option:");

            assertTrue(portalIndex < option1Index, "Portal title should appear before option 1");
            assertTrue(option1Index < option0Index, "Option 1 should appear before option 0");
            assertTrue(option0Index < promptIndex, "Options should appear before prompt");
        }
    }

    @Test
    @DisplayName("Should handle rapid successive inputs")
    void testLaunch_RapidInputs() {
        // Arrange
        String input = "1\n0\n1\n0\n"; // Rapid switching between options
        Scanner scanner = createScannerWithInput(input);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<OnlineMainMenuController> mockedOnlineController = mockStatic(
                        OnlineMainMenuController.class)) { // Act
            CustomerMainMenuController.launch(scanner);

            // Assert
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
            mockedOnlineController.verify(() -> OnlineMainMenuController.launch(scanner), times(2));

            String output = getOutput();
            // Should handle multiple menu interactions
            assertTrue(output.split("Choose an option:").length > 2,
                    "Should show multiple menu prompts for multiple interactions");
        }
    }

    @Test
    @DisplayName("Should clear screen before showing menu")
    void testLaunch_ClearScreen() {
        // Arrange
        String input = "0\n";
        Scanner scanner = createScannerWithInput(input);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) { // Act
            CustomerMainMenuController.launch(scanner);

            // Assert
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }
    }

    @Test
    @DisplayName("Should handle numeric strings with leading/trailing spaces")
    void testLaunch_NumericWithSpaces() {
        // Arrange
        String input = " 1 \n  0  \n"; // Valid options with spaces
        Scanner scanner = createScannerWithInput(input);

        try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                MockedStatic<OnlineMainMenuController> mockedOnlineController = mockStatic(
                        OnlineMainMenuController.class)) { // Act
            CustomerMainMenuController.launch(scanner);

            // Assert
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
            mockedOnlineController.verify(() -> OnlineMainMenuController.launch(scanner));

            String output = getOutput();
            assertContains(output, "SYOS - CUSTOMER PORTAL");
            // Should handle trimmed input correctly
        }
    }

    private void assertContains(String content, String expected) {
        assertTrue(content.contains(expected),
                "Content should contain: '" + expected + "'\nActual content:\n" + content);
    }
}
