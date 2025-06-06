package syos.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test coverage for ConsoleUtils utility class
 * Tests screen clearing and pause functionality with various scenarios
 */
class ConsoleUtilsTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @Mock
    private Scanner mockScanner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    @DisplayName("Should clear screen by printing ANSI escape codes")
    void testClearScreen() {
        // Act
        ConsoleUtils.clearScreen();

        // Assert
        String output = outputStreamCaptor.toString();
        assertEquals("\033[H\033[2J", output);
    }

    @Test
    @DisplayName("Should clear screen multiple times correctly")
    void testClearScreenMultipleTimes() {
        // Act
        ConsoleUtils.clearScreen();
        ConsoleUtils.clearScreen();
        ConsoleUtils.clearScreen();

        // Assert
        String output = outputStreamCaptor.toString();
        String expectedOutput = "\033[H\033[2J\033[H\033[2J\033[H\033[2J";
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Should pause with message and wait for Enter")
    void testPauseWithMockScanner() {
        // Arrange
        when(mockScanner.nextLine()).thenReturn("");

        // Act
        ConsoleUtils.pause(mockScanner);

        // Assert
        String output = outputStreamCaptor.toString();
        assertEquals("Press Enter to continue...", output);
        verify(mockScanner).nextLine();
    }

    @Test
    @DisplayName("Should pause and handle Enter key press")
    void testPauseWithRealScanner() {
        // Arrange
        String enterKeyInput = "\n";
        System.setIn(new ByteArrayInputStream(enterKeyInput.getBytes()));
        Scanner realScanner = new Scanner(System.in);

        // Act
        ConsoleUtils.pause(realScanner);

        // Assert
        String output = outputStreamCaptor.toString();
        assertEquals("Press Enter to continue...", output);

        realScanner.close();
    }

    @Test
    @DisplayName("Should pause and handle Enter with additional text")
    void testPauseWithAdditionalTextInput() {
        // Arrange
        String inputWithText = "some text\n";
        System.setIn(new ByteArrayInputStream(inputWithText.getBytes()));
        Scanner realScanner = new Scanner(System.in);

        // Act
        ConsoleUtils.pause(realScanner);

        // Assert
        String output = outputStreamCaptor.toString();
        assertEquals("Press Enter to continue...", output);

        realScanner.close();
    }

    @Test
    @DisplayName("Should pause and handle multiple Enter presses")
    void testPauseWithMultipleEnterPresses() {
        // Arrange - Mock scanner to return empty string (simulating Enter press)
        when(mockScanner.nextLine()).thenReturn("");

        // Act - Call pause multiple times
        ConsoleUtils.pause(mockScanner);
        ConsoleUtils.pause(mockScanner);
        ConsoleUtils.pause(mockScanner);

        // Assert
        String output = outputStreamCaptor.toString();
        String expectedOutput = "Press Enter to continue...Press Enter to continue...Press Enter to continue...";
        assertEquals(expectedOutput, output);
        verify(mockScanner, times(3)).nextLine();
    }

    @Test
    @DisplayName("Should handle pause with whitespace input")
    void testPauseWithWhitespaceInput() {
        // Arrange
        when(mockScanner.nextLine()).thenReturn("   ");

        // Act
        ConsoleUtils.pause(mockScanner);

        // Assert
        String output = outputStreamCaptor.toString();
        assertEquals("Press Enter to continue...", output);
        verify(mockScanner).nextLine();
    }

    @Test
    @DisplayName("Should work with clearScreen and pause in sequence")
    void testClearScreenAndPauseSequence() {
        // Arrange
        when(mockScanner.nextLine()).thenReturn("");

        // Act
        ConsoleUtils.clearScreen();
        ConsoleUtils.pause(mockScanner);

        // Assert
        String output = outputStreamCaptor.toString();
        assertEquals("\033[H\033[2JPress Enter to continue...", output);
        verify(mockScanner).nextLine();
    }

    @Test
    @DisplayName("Should handle different scanner states")
    void testPauseWithDifferentScannerInputs() {
        // Test with empty string
        when(mockScanner.nextLine()).thenReturn("");
        ConsoleUtils.pause(mockScanner);

        // Test with spaces
        when(mockScanner.nextLine()).thenReturn("   ");
        ConsoleUtils.pause(mockScanner);

        // Test with actual text
        when(mockScanner.nextLine()).thenReturn("test input");
        ConsoleUtils.pause(mockScanner);

        // Assert all calls were made
        verify(mockScanner, times(3)).nextLine();

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Press Enter to continue..."));

        // Count occurrences of the prompt
        String prompt = "Press Enter to continue...";
        int count = (output.length() - output.replace(prompt, "").length()) / prompt.length();
        assertEquals(3, count);
    }

    @Test
    @DisplayName("Should work correctly in real console simulation scenario")
    void testRealConsoleSimulation() {
        // Arrange - Simulate typical console usage
        String simulatedUserInput = "\n\n\n"; // User presses Enter 3 times
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));
        Scanner realScanner = new Scanner(System.in);

        // Act - Simulate a typical console session
        ConsoleUtils.clearScreen();
        ConsoleUtils.pause(realScanner);
        ConsoleUtils.clearScreen();
        ConsoleUtils.pause(realScanner);
        ConsoleUtils.clearScreen();
        ConsoleUtils.pause(realScanner);

        // Assert
        String output = outputStreamCaptor.toString();

        // Should contain 3 clear commands
        int clearCount = (output.length() - output.replace("\033[H\033[2J", "").length()) / "\033[H\033[2J".length();
        assertEquals(3, clearCount);

        // Should contain 3 pause messages
        String prompt = "Press Enter to continue...";
        int promptCount = (output.length() - output.replace(prompt, "").length()) / prompt.length();
        assertEquals(3, promptCount);

        realScanner.close();
    }

    @Test
    @DisplayName("Should handle edge case with very long input")
    void testPauseWithVeryLongInput() {
        // Arrange - Create a very long string
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("a");
        }

        when(mockScanner.nextLine()).thenReturn(longInput.toString());

        // Act
        ConsoleUtils.pause(mockScanner);

        // Assert
        String output = outputStreamCaptor.toString();
        assertEquals("Press Enter to continue...", output);
        verify(mockScanner).nextLine();
    }

    @Test
    @DisplayName("Should maintain console state after operations")
    void testConsoleStateAfterOperations() {
        // Arrange
        when(mockScanner.nextLine()).thenReturn("");

        // Act - Perform multiple operations
        ConsoleUtils.clearScreen();
        String outputAfterClear = outputStreamCaptor.toString();

        ConsoleUtils.pause(mockScanner);
        String outputAfterPause = outputStreamCaptor.toString();

        // Assert - Verify state progression
        assertEquals("\033[H\033[2J", outputAfterClear);
        assertEquals("\033[H\033[2JPress Enter to continue...", outputAfterPause);

        // Verify the console utilities don't affect system state
        assertNotNull(System.out);
        assertNotNull(System.in);
    }
}
