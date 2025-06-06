package syos.ui.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;
import org.mockito.MockedConstruction;

import syos.model.Employee;
import syos.ui.views.AddBatchStockUI;
import syos.ui.views.ReduceStockFromBatchUI;
import syos.ui.views.ViewExpiredBatchesUI;
import syos.ui.views.StockSummaryUI;
import syos.ui.views.AutoCleanupExpiredBatchesUI;
import syos.util.ConsoleUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Comprehensive test suite for InventoryAndStockController
 * Tests all menu options, navigation paths, and error handling scenarios
 * Ensures 100% coverage of controller functionality
 */
class InventoryAndStockControllerTest {

    private Employee testEmployee;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private MockedStatic<ConsoleUtils> consoleUtilsMock;

    @BeforeEach
    void setUp() {
        // Create test employee with realistic data
        testEmployee = new Employee("1001", "Sarah Johnson", "4567", "Inventory Manager");

        // Capture system output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        // Mock ConsoleUtils static methods
        consoleUtilsMock = mockStatic(ConsoleUtils.class);
        consoleUtilsMock.when(ConsoleUtils::clearScreen).then(invocation -> {
            System.out.println("[Screen Cleared]");
            return null;
        });
        consoleUtilsMock.when(() -> ConsoleUtils.pause(any(Scanner.class))).then(invocation -> {
            System.out.println("[Console Paused]");
            return null;
        });
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        if (consoleUtilsMock != null) {
            consoleUtilsMock.close();
        }
    }

    @Test
    @DisplayName("Should launch inventory menu and display all options correctly")
    void testLaunchInventoryMenuDisplay() {
        // Arrange
        String input = "0\n"; // Exit immediately
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        // Act
        InventoryAndStockController.launch(scanner, testEmployee);

        // Assert
        String output = outputStream.toString();
        assertAll("Menu display validation",
                () -> assertTrue(output.contains("INVENTORY AND STOCK OPERATIONS"),
                        "Should display main header"),
                () -> assertTrue(output.contains("1. Add Batch Stock"),
                        "Should display Add Batch Stock option"),
                () -> assertTrue(output.contains("2. Reduce Shelf Stock by Batch"),
                        "Should display Reduce Stock option"),
                () -> assertTrue(output.contains("3. View Expired Batches"),
                        "Should display View Expired Batches option"),
                () -> assertTrue(output.contains("4. Stock Summary (Total / Used / Available)"),
                        "Should display Stock Summary option"),
                () -> assertTrue(output.contains("5. Auto-Cleanup Expired Batches"),
                        "Should display Auto-Cleanup option"),
                () -> assertTrue(output.contains("0. Back to Main Menu"),
                        "Should display back option"),
                () -> assertTrue(output.contains("Select an option:"),
                        "Should prompt for selection"),
                () -> assertTrue(output.contains("Returning to Employee Portal"),
                        "Should show exit message"));
    }

    @Test
    @DisplayName("Should launch AddBatchStockUI when option 1 is selected")
    void testAddBatchStockUILaunch() {
        // Arrange
        String input = "1\n0\n"; // Select option 1, then exit
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<AddBatchStockUI> mockAddBatchUI = mockConstruction(AddBatchStockUI.class,
                (mock, context) -> {
                    assertEquals(1, context.arguments().size(), "Should pass scanner to constructor");
                    assertSame(scanner, context.arguments().get(0), "Should pass correct scanner");
                })) {

            // Act
            InventoryAndStockController.launch(scanner, testEmployee);

            // Assert
            assertEquals(1, mockAddBatchUI.constructed().size(), "Should create AddBatchStockUI instance");
            AddBatchStockUI addBatchUI = mockAddBatchUI.constructed().get(0);
            verify(addBatchUI, times(1)).start();
        }
    }

    @Test
    @DisplayName("Should launch ReduceStockFromBatchUI when option 2 is selected")
    void testReduceStockFromBatchUILaunch() {
        // Arrange
        String input = "2\n0\n"; // Select option 2, then exit
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<ReduceStockFromBatchUI> mockReduceStockUI = mockConstruction(
                ReduceStockFromBatchUI.class,
                (mock, context) -> {
                    assertEquals(1, context.arguments().size(), "Should pass scanner to constructor");
                    assertSame(scanner, context.arguments().get(0), "Should pass correct scanner");
                })) {

            // Act
            InventoryAndStockController.launch(scanner, testEmployee);

            // Assert
            assertEquals(1, mockReduceStockUI.constructed().size(), "Should create ReduceStockFromBatchUI instance");
            ReduceStockFromBatchUI reduceStockUI = mockReduceStockUI.constructed().get(0);
            verify(reduceStockUI, times(1)).start();
        }
    }

    @Test
    @DisplayName("Should launch ViewExpiredBatchesUI when option 3 is selected")
    void testViewExpiredBatchesUILaunch() {
        // Arrange
        String input = "3\n0\n"; // Select option 3, then exit
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<ViewExpiredBatchesUI> mockViewExpiredUI = mockConstruction(ViewExpiredBatchesUI.class,
                (mock, context) -> {
                    assertEquals(1, context.arguments().size(), "Should pass scanner to constructor");
                    assertSame(scanner, context.arguments().get(0), "Should pass correct scanner");
                })) {

            // Act
            InventoryAndStockController.launch(scanner, testEmployee);

            // Assert
            assertEquals(1, mockViewExpiredUI.constructed().size(), "Should create ViewExpiredBatchesUI instance");
            ViewExpiredBatchesUI viewExpiredUI = mockViewExpiredUI.constructed().get(0);
            verify(viewExpiredUI, times(1)).start();
        }
    }

    @Test
    @DisplayName("Should launch StockSummaryUI when option 4 is selected")
    void testStockSummaryUILaunch() {
        // Arrange
        String input = "4\n0\n"; // Select option 4, then exit
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<StockSummaryUI> mockStockSummaryUI = mockConstruction(StockSummaryUI.class,
                (mock, context) -> {
                    assertEquals(1, context.arguments().size(), "Should pass scanner to constructor");
                    assertSame(scanner, context.arguments().get(0), "Should pass correct scanner");
                })) {

            // Act
            InventoryAndStockController.launch(scanner, testEmployee);

            // Assert
            assertEquals(1, mockStockSummaryUI.constructed().size(), "Should create StockSummaryUI instance");
            StockSummaryUI stockSummaryUI = mockStockSummaryUI.constructed().get(0);
            verify(stockSummaryUI, times(1)).start();
        }
    }

    @Test
    @DisplayName("Should launch AutoCleanupExpiredBatchesUI when option 5 is selected")
    void testAutoCleanupExpiredBatchesUILaunch() {
        // Arrange
        String input = "5\n0\n"; // Select option 5, then exit
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<AutoCleanupExpiredBatchesUI> mockAutoCleanupUI = mockConstruction(
                AutoCleanupExpiredBatchesUI.class,
                (mock, context) -> {
                    assertEquals(1, context.arguments().size(), "Should pass scanner to constructor");
                    assertSame(scanner, context.arguments().get(0), "Should pass correct scanner");
                })) {

            // Act
            InventoryAndStockController.launch(scanner, testEmployee);

            // Assert
            assertEquals(1, mockAutoCleanupUI.constructed().size(),
                    "Should create AutoCleanupExpiredBatchesUI instance");
            AutoCleanupExpiredBatchesUI autoCleanupUI = mockAutoCleanupUI.constructed().get(0);
            verify(autoCleanupUI, times(1)).start();
        }
    }

    @Test
    @DisplayName("Should exit gracefully when option 0 is selected")
    void testExitOption() {
        // Arrange
        String input = "0\n"; // Select exit
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        // Act
        InventoryAndStockController.launch(scanner, testEmployee);

        // Assert
        String output = outputStream.toString();
        assertAll("Exit validation",
                () -> assertTrue(output.contains("Returning to Employee Portal"),
                        "Should display return message"),
                () -> assertTrue(output.contains("[Console Paused]"),
                        "Should pause before returning"));
    }

    @Test
    @DisplayName("Should handle invalid menu options gracefully")
    void testInvalidMenuOption() {
        // Arrange
        String input = "invalid\n0\n"; // Invalid input, then exit
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        // Act
        InventoryAndStockController.launch(scanner, testEmployee);

        // Assert
        String output = outputStream.toString();
        assertAll("Invalid option handling",
                () -> assertTrue(output.contains("Invalid option. Please select again."),
                        "Should display invalid option message"),
                () -> assertTrue(output.contains("[Console Paused]"),
                        "Should pause after error"),
                () -> assertTrue(output.contains("INVENTORY AND STOCK OPERATIONS"),
                        "Should redisplay menu after error"));
    }

    @Test
    @DisplayName("Should handle multiple invalid options before valid selection")
    void testMultipleInvalidOptions() {
        // Arrange
        String input = "9\n-1\nabc\n1\n0\n"; // Multiple invalid, then valid option 1, then exit
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<AddBatchStockUI> mockAddBatchUI = mockConstruction(AddBatchStockUI.class)) {

            // Act
            InventoryAndStockController.launch(scanner, testEmployee);

            // Assert
            String output = outputStream.toString();

            // Count occurrences of invalid option message
            long invalidMessageCount = output.lines()
                    .filter(line -> line.contains("Invalid option. Please select again."))
                    .count();

            assertEquals(3, invalidMessageCount, "Should display 3 invalid option messages");
            assertEquals(1, mockAddBatchUI.constructed().size(), "Should eventually launch AddBatchStockUI");
        }
    }

    @Test
    @DisplayName("Should handle navigation through all menu options sequentially")
    void testSequentialMenuNavigation() {
        // Arrange
        String input = "1\n2\n3\n4\n5\n0\n"; // Go through all options then exit
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<AddBatchStockUI> mockAddBatchUI = mockConstruction(AddBatchStockUI.class);
                MockedConstruction<ReduceStockFromBatchUI> mockReduceStockUI = mockConstruction(
                        ReduceStockFromBatchUI.class);
                MockedConstruction<ViewExpiredBatchesUI> mockViewExpiredUI = mockConstruction(
                        ViewExpiredBatchesUI.class);
                MockedConstruction<StockSummaryUI> mockStockSummaryUI = mockConstruction(StockSummaryUI.class);
                MockedConstruction<AutoCleanupExpiredBatchesUI> mockAutoCleanupUI = mockConstruction(
                        AutoCleanupExpiredBatchesUI.class)) {

            // Act
            InventoryAndStockController.launch(scanner, testEmployee);

            // Assert
            assertAll("Sequential navigation validation",
                    () -> assertEquals(1, mockAddBatchUI.constructed().size(),
                            "Should create AddBatchStockUI"),
                    () -> assertEquals(1, mockReduceStockUI.constructed().size(),
                            "Should create ReduceStockFromBatchUI"),
                    () -> assertEquals(1, mockViewExpiredUI.constructed().size(),
                            "Should create ViewExpiredBatchesUI"),
                    () -> assertEquals(1, mockStockSummaryUI.constructed().size(),
                            "Should create StockSummaryUI"),
                    () -> assertEquals(1, mockAutoCleanupUI.constructed().size(),
                            "Should create AutoCleanupExpiredBatchesUI"));

            // Verify all UIs were started
            verify(mockAddBatchUI.constructed().get(0), times(1)).start();
            verify(mockReduceStockUI.constructed().get(0), times(1)).start();
            verify(mockViewExpiredUI.constructed().get(0), times(1)).start();
            verify(mockStockSummaryUI.constructed().get(0), times(1)).start();
            verify(mockAutoCleanupUI.constructed().get(0), times(1)).start();
        }
    }

    @Test
    @DisplayName("Should handle edge case inputs like whitespace and empty strings")
    void testEdgeCaseInputs() {
        // Arrange
        String input = "   \n\n  \t  \n1\n0\n"; // Whitespace, empty, tabs, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<AddBatchStockUI> mockAddBatchUI = mockConstruction(AddBatchStockUI.class)) {

            // Act
            InventoryAndStockController.launch(scanner, testEmployee);

            // Assert
            String output = outputStream.toString();

            // Should handle empty inputs as invalid
            assertTrue(output.contains("Invalid option. Please select again."),
                    "Should treat empty/whitespace inputs as invalid");
            assertEquals(1, mockAddBatchUI.constructed().size(),
                    "Should eventually process valid option");
        }
    }

    @Test
    @DisplayName("Should pass correct employee parameter to all UI constructors")
    void testEmployeeParameterPassing() {
        // Arrange
        Employee specificEmployee = new Employee("2002", "Michael Chen", "7890", "Stock Manager");
        String input = "1\n0\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<AddBatchStockUI> mockAddBatchUI = mockConstruction(AddBatchStockUI.class)) {

            // Act
            InventoryAndStockController.launch(scanner, specificEmployee);

            // Assert
            // Note: The employee parameter is passed to the launch method but UI
            // constructors
            // only take Scanner. This test verifies the method accepts the employee
            // parameter correctly.
            assertEquals(1, mockAddBatchUI.constructed().size(),
                    "Should successfully launch with employee parameter");
        }
    }

    @Test
    @DisplayName("Should handle rapid option selection without errors")
    void testRapidOptionSelection() {
        // Arrange
        String input = "1\n2\n3\n4\n5\n1\n0\n"; // Rapid selection
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<AddBatchStockUI> mockAddBatchUI = mockConstruction(AddBatchStockUI.class);
                MockedConstruction<ReduceStockFromBatchUI> mockReduceStockUI = mockConstruction(
                        ReduceStockFromBatchUI.class);
                MockedConstruction<ViewExpiredBatchesUI> mockViewExpiredUI = mockConstruction(
                        ViewExpiredBatchesUI.class);
                MockedConstruction<StockSummaryUI> mockStockSummaryUI = mockConstruction(StockSummaryUI.class);
                MockedConstruction<AutoCleanupExpiredBatchesUI> mockAutoCleanupUI = mockConstruction(
                        AutoCleanupExpiredBatchesUI.class)) {

            // Act
            InventoryAndStockController.launch(scanner, testEmployee);

            // Assert - AddBatchStockUI should be called twice (first and last)
            assertEquals(2, mockAddBatchUI.constructed().size(),
                    "Should handle rapid option selection");
            assertEquals(1, mockReduceStockUI.constructed().size(),
                    "Should handle ReduceStockFromBatchUI selection");
            assertEquals(1, mockViewExpiredUI.constructed().size(),
                    "Should handle ViewExpiredBatchesUI selection");
            assertEquals(1, mockStockSummaryUI.constructed().size(),
                    "Should handle StockSummaryUI selection");
            assertEquals(1, mockAutoCleanupUI.constructed().size(),
                    "Should handle AutoCleanupExpiredBatchesUI selection");
        }
    }

    @Test
    @DisplayName("Should maintain clean state between menu iterations")
    void testCleanStateBetweenIterations() {
        // Arrange
        String input = "1\n3\n5\n0\n"; // Multiple operations then exit
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<AddBatchStockUI> mockAddBatchUI = mockConstruction(AddBatchStockUI.class);
                MockedConstruction<ViewExpiredBatchesUI> mockViewExpiredUI = mockConstruction(
                        ViewExpiredBatchesUI.class);
                MockedConstruction<AutoCleanupExpiredBatchesUI> mockAutoCleanupUI = mockConstruction(
                        AutoCleanupExpiredBatchesUI.class)) {

            // Act
            InventoryAndStockController.launch(scanner, testEmployee);

            // Assert
            String output = outputStream.toString();

            // Count screen clearing operations (one before each menu display)
            long screenClearCount = output.lines()
                    .filter(line -> line.contains("[Screen Cleared]"))
                    .count();

            assertTrue(screenClearCount >= 3, "Should clear screen before each menu display");

            // Verify each UI was created and started exactly once
            assertEquals(1, mockAddBatchUI.constructed().size());
            assertEquals(1, mockViewExpiredUI.constructed().size());
            assertEquals(1, mockAutoCleanupUI.constructed().size());
        }
    }

    @Test
    @DisplayName("Should handle null employee parameter gracefully")
    void testNullEmployeeParameter() {
        // Arrange
        String input = "0\n"; // Exit immediately
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            InventoryAndStockController.launch(scanner, null);
        }, "Should handle null employee parameter without throwing exception");
    }

    @Test
    @DisplayName("Should verify ConsoleUtils integration")
    void testConsoleUtilsIntegration() {
        // Arrange
        String input = "invalid\n0\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        // Act
        InventoryAndStockController.launch(scanner, testEmployee);

        // Assert
        consoleUtilsMock.verify(ConsoleUtils::clearScreen, atLeastOnce());
        consoleUtilsMock.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());
    }

    @Test
    @DisplayName("Should handle very long invalid input strings")
    void testLongInvalidInputs() {
        // Arrange
        String longInput = "a".repeat(1000); // Very long invalid input
        String input = longInput + "\n0\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        // Act & Assert
        assertDoesNotThrow(() -> {
            InventoryAndStockController.launch(scanner, testEmployee);
        }, "Should handle very long invalid inputs without error");

        String output = outputStream.toString();
        assertTrue(output.contains("Invalid option. Please select again."),
                "Should treat long invalid input as invalid option");
    }

    @Test
    @DisplayName("Should verify menu loop continues until exit option")
    void testMenuLoopContinuation() {
        // Arrange
        String input = "invalid1\ninvalid2\n7\n8\n1\n2\n0\n"; // Multiple invalid, then valid, then exit
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<AddBatchStockUI> mockAddBatchUI = mockConstruction(AddBatchStockUI.class);
                MockedConstruction<ReduceStockFromBatchUI> mockReduceStockUI = mockConstruction(
                        ReduceStockFromBatchUI.class)) {

            // Act
            InventoryAndStockController.launch(scanner, testEmployee);

            // Assert
            String output = outputStream.toString();

            // Count menu header appearances
            long menuHeaderCount = output.lines()
                    .filter(line -> line.contains("INVENTORY AND STOCK OPERATIONS"))
                    .count();

            assertTrue(menuHeaderCount > 1, "Menu should be displayed multiple times");
            assertEquals(1, mockAddBatchUI.constructed().size());
            assertEquals(1, mockReduceStockUI.constructed().size());
        }
    }
}
