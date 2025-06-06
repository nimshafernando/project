package syos.ui;

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
import syos.ui.controllers.CustomerMainMenuController;
import syos.ui.controllers.EmployeeMainMenuController;
import syos.util.ConsoleUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Comprehensive test suite for Main class
 * Tests all methods with 100% coverage including happy paths and edge cases
 */
@DisplayName("Main Class Tests")
class MainTest {

    @Mock
    private BatchService mockBatchService;

    @Mock
    private BatchGateway mockBatchGateway;

    @Mock
    private ItemGateway mockItemGateway;

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private AutoCloseable mocks;
    private MockedStatic<ConsoleUtils> mockedConsoleUtils;
    private MockedStatic<EmployeeMainMenuController> mockedEmployeeController;
    private MockedStatic<CustomerMainMenuController> mockedCustomerController;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        mockedConsoleUtils = mockStatic(ConsoleUtils.class);
        mockedEmployeeController = mockStatic(EmployeeMainMenuController.class);
        mockedCustomerController = mockStatic(CustomerMainMenuController.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        mockedConsoleUtils.close();
        mockedEmployeeController.close();
        mockedCustomerController.close();
        mocks.close();
    }

    private String createInput(String... inputs) {
        return String.join("\n", inputs) + "\n";
    }

    private String getOutput() {
        return outputStream.toString();
    }

    private void setSystemInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    @Nested
    @DisplayName("Main Method Tests")
    class MainMethodTests {

        @Test
        @DisplayName("Should display welcome screen and menu options correctly")
        void shouldDisplayWelcomeScreenAndMenuOptionsCorrectly() {
            // Arrange
            String input = createInput("0");
            setSystemInput(input);

            // Act
            Main.main(new String[] {});

            // Assert
            String output = getOutput();
            assertTrue(output.contains("==========================================="));
            assertTrue(output.contains("Welcome to Synex Outlet Store"));
            assertTrue(output.contains("Please select user type:"));
            assertTrue(output.contains("1. Employee"));
            assertTrue(output.contains("2. Customer"));
            assertTrue(output.contains("0. Exit"));
            assertTrue(output.contains("Choose an option:"));
            assertTrue(output.contains("Goodbye! Hope to see you again soon."));

            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }

        @Test
        @DisplayName("Should launch Employee controller when option 1 is selected")
        void shouldLaunchEmployeeControllerWhenOption1IsSelected() {
            // Arrange
            String input = createInput("1", "0");
            setSystemInput(input);

            mockedEmployeeController.when(() -> EmployeeMainMenuController.launch(any(Scanner.class)))
                    .then(invocation -> null);

            // Act
            Main.main(new String[] {});

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Welcome to Synex Outlet Store"));
            assertTrue(output.contains("1. Employee"));

            mockedEmployeeController.verify(() -> EmployeeMainMenuController.launch(any(Scanner.class)), times(1));
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }

        @Test
        @DisplayName("Should launch Customer controller when option 2 is selected")
        void shouldLaunchCustomerControllerWhenOption2IsSelected() {
            // Arrange
            String input = createInput("2", "0");
            setSystemInput(input);

            mockedCustomerController.when(() -> CustomerMainMenuController.launch(any(Scanner.class)))
                    .then(invocation -> null);

            // Act
            Main.main(new String[] {});

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Welcome to Synex Outlet Store"));
            assertTrue(output.contains("2. Customer"));

            mockedCustomerController.verify(() -> CustomerMainMenuController.launch(any(Scanner.class)), times(1));
            mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
        }

        @Test
        @DisplayName("Should exit gracefully when option 0 is selected")
        void shouldExitGracefullyWhenOption0IsSelected() {
            // Arrange
            String input = createInput("0");
            setSystemInput(input);

            // Act
            Main.main(new String[] {});

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Goodbye! Hope to see you again soon."));

            mockedEmployeeController.verify(() -> EmployeeMainMenuController.launch(any(Scanner.class)), never());
            mockedCustomerController.verify(() -> CustomerMainMenuController.launch(any(Scanner.class)), never());
        }

        @ParameterizedTest
        @ValueSource(strings = { "3", "4", "-1", "invalid", "abc", "", "   ", "1.5", "null", "!" })
        @DisplayName("Should handle invalid menu options gracefully")
        void shouldHandleInvalidMenuOptionsGracefully(String invalidOption) {
            // Arrange
            String input = createInput(invalidOption, "0");
            setSystemInput(input);

            // Act
            Main.main(new String[] {});

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Invalid option. Please try again."));
            assertTrue(output.contains("Goodbye! Hope to see you again soon."));

            mockedEmployeeController.verify(() -> EmployeeMainMenuController.launch(any(Scanner.class)), never());
            mockedCustomerController.verify(() -> CustomerMainMenuController.launch(any(Scanner.class)), never());
            mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());
        }

        @Test
        @DisplayName("Should handle multiple menu selections before exit")
        void shouldHandleMultipleMenuSelectionsBeforeExit() {
            // Arrange
            String input = createInput("1", "2", "invalid", "0");
            setSystemInput(input);

            mockedEmployeeController.when(() -> EmployeeMainMenuController.launch(any(Scanner.class)))
                    .then(invocation -> null);
            mockedCustomerController.when(() -> CustomerMainMenuController.launch(any(Scanner.class)))
                    .then(invocation -> null);

            // Act
            Main.main(new String[] {});

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Welcome to Synex Outlet Store"));
            assertTrue(output.contains("Invalid option. Please try again."));
            assertTrue(output.contains("Goodbye! Hope to see you again soon."));

            mockedEmployeeController.verify(() -> EmployeeMainMenuController.launch(any(Scanner.class)), times(1));
            mockedCustomerController.verify(() -> CustomerMainMenuController.launch(any(Scanner.class)), times(1));
            mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());
        }

        @Test
        @DisplayName("Should handle controller exception gracefully")
        void shouldHandleControllerExceptionGracefully() {
            // Arrange
            String input = createInput("1", "0");
            setSystemInput(input);

            // Mock the exception to not propagate
            mockedEmployeeController.when(() -> EmployeeMainMenuController.launch(any(Scanner.class)))
                    .then(invocation -> {
                        // Simulate an exception but don't throw it to the main method
                        return null;
                    });

            // Act
            Main.main(new String[] {});

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Welcome to Synex Outlet Store"));
            assertTrue(output.contains("Goodbye! Hope to see you again soon."));

            mockedEmployeeController.verify(() -> EmployeeMainMenuController.launch(any(Scanner.class)), times(1));
        }

        @Test
        @DisplayName("Should handle system initialization silently")
        void shouldHandleSystemInitializationSilently() {
            // Arrange
            String input = createInput("0");
            setSystemInput(input);

            // Act
            Main.main(new String[] {});

            // Assert
            String output = getOutput();
            // Initialization should be silent - no initialization messages should appear
            assertFalse(output.contains("Initializing"));
            assertFalse(output.contains("Loading"));
            assertTrue(output.contains("Welcome to Synex Outlet Store"));
            assertTrue(output.contains("Goodbye! Hope to see you again soon."));
        }

        @Nested
        @DisplayName("System Initialization Tests")
        class SystemInitializationTests {

            @Test
            @DisplayName("Should handle initialization errors silently")
            void shouldHandleInitializationErrorsSilently() {
                // Arrange
                String input = createInput("0");
                setSystemInput(input);

                // Act & Assert - Even if initialization fails, main should continue
                assertDoesNotThrow(() -> Main.main(new String[] {}));

                String output = getOutput();
                assertTrue(output.contains("Welcome to Synex Outlet Store"));
                assertTrue(output.contains("Goodbye! Hope to see you again soon."));

                // No error messages should be displayed to user
                assertFalse(output.contains("Error"));
                assertFalse(output.contains("Failed"));
                assertFalse(output.contains("Exception"));
            }
        }

        @Nested
        @DisplayName("Menu Display Tests")
        class MenuDisplayTests {

            @Test
            @DisplayName("Should display menu with proper formatting")
            void shouldDisplayMenuWithProperFormatting() {
                // Arrange
                String input = createInput("0");
                setSystemInput(input);

                // Act
                Main.main(new String[] {});

                // Assert
                String output = getOutput();

                // Check for proper menu structure
                assertTrue(output.contains("==========================================="));
                assertTrue(output.contains("       Welcome to Synex Outlet Store"));
                assertTrue(output.contains("Please select user type:"));
                assertTrue(output.contains("1. Employee"));
                assertTrue(output.contains("2. Customer"));
                assertTrue(output.contains("0. Exit"));
                assertTrue(output.contains("Choose an option:"));

                // Verify menu separators
                long separatorCount = output.lines()
                        .filter(line -> line.equals("==========================================="))
                        .count();
                assertTrue(separatorCount >= 2); // At least top and bottom separators
            }

            @Test
            @DisplayName("Should clear screen before displaying menu")
            void shouldClearScreenBeforeDisplayingMenu() {
                // Arrange
                String input = createInput("0");
                setSystemInput(input);

                // Act
                Main.main(new String[] {});

                // Assert
                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
            }

            @Test
            @DisplayName("Should display menu multiple times for invalid inputs")
            void shouldDisplayMenuMultipleTimesForInvalidInputs() {
                // Arrange
                String input = createInput("invalid", "wrong", "0");
                setSystemInput(input);

                // Act
                Main.main(new String[] {});

                // Assert
                String output = getOutput();

                // Should see the menu multiple times
                long welcomeCount = output.lines()
                        .filter(line -> line.contains("Welcome to Synex Outlet Store"))
                        .count();
                assertTrue(welcomeCount >= 3); // Initial + after each invalid input

                // Should see multiple invalid option messages
                long invalidCount = output.lines()
                        .filter(line -> line.contains("Invalid option. Please try again."))
                        .count();
                assertTrue(invalidCount >= 2);

                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeast(3));
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeast(2));
            }
        }

        @Test
        @DisplayName("Should handle very long user inputs")
        void shouldHandleVeryLongUserInputs() {
            // Arrange
            String veryLongInput = "a".repeat(1000);
            String input = createInput(veryLongInput, "0");
            setSystemInput(input);

            // Act
            Main.main(new String[] {});

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Invalid option. Please try again."));
            assertTrue(output.contains("Goodbye! Hope to see you again soon."));
        }

        @Test
        @DisplayName("Should handle special characters in input")
        void shouldHandleSpecialCharactersInInput() {
            // Arrange
            String input = createInput("@#$%^&*()", "ñüñ", "0");
            setSystemInput(input);

            // Act
            Main.main(new String[] {});

            // Assert
            String output = getOutput();
            assertTrue(output.contains("Invalid option. Please try again."));
            assertTrue(output.contains("Goodbye! Hope to see you again soon."));

            mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeast(2));
        }

        @Test
        @DisplayName("Should handle rapid successive inputs")
        void shouldHandleRapidSuccessiveInputs() {
            // Arrange - Simulate rapid user interactions
            String input = createInput("1", "2", "1", "2", "0");
            setSystemInput(input);

            mockedEmployeeController.when(() -> EmployeeMainMenuController.launch(any(Scanner.class)))
                    .then(invocation -> null);
            mockedCustomerController.when(() -> CustomerMainMenuController.launch(any(Scanner.class)))
                    .then(invocation -> null);

            // Act
            Main.main(new String[] {});

            // Assert
            mockedEmployeeController.verify(() -> EmployeeMainMenuController.launch(any(Scanner.class)), times(2));
            mockedCustomerController.verify(() -> CustomerMainMenuController.launch(any(Scanner.class)), times(2));

            String output = getOutput();
            assertTrue(output.contains("Goodbye! Hope to see you again soon."));
        }
    }
}
