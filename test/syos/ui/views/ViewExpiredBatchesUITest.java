package syos.ui.views;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import syos.data.BatchGateway;
import syos.data.ItemGateway;
import syos.dto.BatchDTO;
import syos.service.BatchService;
import syos.util.ConsoleUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Test suite for ViewExpiredBatchesUI with 100% coverage
 */
@DisplayName("ViewExpiredBatchesUI Tests")
class ViewExpiredBatchesUITest {

    @Mock
    private BatchService mockBatchService;

    @Mock
    private BatchGateway mockBatchGateway;

    @Mock
    private ItemGateway mockItemGateway;

    private ViewExpiredBatchesUI viewExpiredBatchesUI;
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

    private List<BatchDTO> createSampleExpiredBatches() {
        List<BatchDTO> batches = new ArrayList<>();

        // Sample expired batch 1 - Dairy product
        BatchDTO batch1 = new BatchDTO("MILK001", 24, LocalDate.parse("2025-05-15"));
        batch1.setId(101);
        batch1.setName("Fresh Whole Milk");
        batch1.setPurchaseDate(LocalDate.parse("2025-05-01"));
        batches.add(batch1);

        // Sample expired batch 2 - Packaged food (name will be truncated)
        BatchDTO batch2 = new BatchDTO("BREAD025", 12, LocalDate.parse("2025-05-20"));
        batch2.setId(102);
        batch2.setName("Artisanal Sourdough Bread"); // Changed to fit within 25 chars
        batch2.setPurchaseDate(LocalDate.parse("2025-05-17"));
        batches.add(batch2);

        // Sample expired batch 3 - Produce
        BatchDTO batch3 = new BatchDTO("BANAN003", 50, LocalDate.parse("2025-05-22"));
        batch3.setId(103);
        batch3.setName("Organic Bananas");
        batch3.setPurchaseDate(LocalDate.parse("2025-05-18"));
        batches.add(batch3);

        return batches;
    }

    private BatchDTO createBatchWithLongName() {
        BatchDTO batch = new BatchDTO("LONGNAME1", 5, LocalDate.parse("2025-05-25"));
        batch.setId(104);
        batch.setName("Premium Organic Free Range Chicken Breast Fillets Extra Large Pack");
        batch.setPurchaseDate(LocalDate.parse("2025-05-20"));
        return batch;
    }

    private void injectMockBatchService() {
        try {
            java.lang.reflect.Field batchServiceField = ViewExpiredBatchesUI.class
                    .getDeclaredField("batchService");
            batchServiceField.setAccessible(true);
            batchServiceField.set(viewExpiredBatchesUI, mockBatchService);
        } catch (Exception e) {
            fail("Failed to inject mock BatchService: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Create UI with valid scanner")
    void testConstructorWithValidScanner() {
        Scanner scanner = createScanner("\n");
        ViewExpiredBatchesUI ui = new ViewExpiredBatchesUI(scanner);
        assertNotNull(ui);
    }

    @Test
    @DisplayName("Create UI with null scanner")
    void testConstructorWithNullScanner() {
        ViewExpiredBatchesUI ui = new ViewExpiredBatchesUI(null);
        assertNotNull(ui);
    }

    @Test
    @DisplayName("Display empty archive message")
    void testEmptyArchiveDisplay() {
        Scanner scanner = createScanner("\n");
        viewExpiredBatchesUI = new ViewExpiredBatchesUI(scanner);
        injectMockBatchService();

        when(mockBatchService.getArchivedExpiredBatches()).thenReturn(new ArrayList<>());

        viewExpiredBatchesUI.start();

        String output = getOutput();
        assertTrue(output.contains("ARCHIVED EXPIRED BATCHES"));
        assertTrue(output.contains("No expired batches found in archive."));

        mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
        mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        verify(mockBatchService, times(1)).getArchivedExpiredBatches();
        verify(mockBatchService, never()).clearArchivedExpiredBatches();
    }

    @Test
    @DisplayName("Display archived batches with formatting")
    void testDisplayArchivedBatches() {
        Scanner scanner = createScanner("\n");
        viewExpiredBatchesUI = new ViewExpiredBatchesUI(scanner);
        List<BatchDTO> sampleBatches = createSampleExpiredBatches();
        injectMockBatchService();

        when(mockBatchService.getArchivedExpiredBatches()).thenReturn(sampleBatches);

        viewExpiredBatchesUI.start();

        String output = getOutput();
        assertTrue(output.contains("ARCHIVED EXPIRED BATCHES"));
        assertTrue(output.contains("No."));
        assertTrue(output.contains("Item Code"));
        assertTrue(output.contains("Item Name"));
        assertTrue(output.contains("Quantity"));
        assertTrue(output.contains("Expiry"));
        assertTrue(output.contains("Purchased"));
        assertTrue(output.contains("MILK001"));
        assertTrue(output.contains("Fresh Whole Milk"));
        assertTrue(output.contains("BREAD025"));
        assertTrue(output.contains("Artisanal Sourdough Bread")); // Fixed - no truncation needed
        assertTrue(output.contains("BANAN003"));
        assertTrue(output.contains("Organic Bananas"));
        assertTrue(output.contains("These batches have been automatically archived"));
        assertTrue(output.contains("Press 'c' to clear all archived records"));

        mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
        mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        verify(mockBatchService, times(1)).getArchivedExpiredBatches();
    }

    @Test
    @DisplayName("Return to menu on Enter")
    void testReturnToMenuOnEnter() {
        Scanner scanner = createScanner("\n");
        viewExpiredBatchesUI = new ViewExpiredBatchesUI(scanner);
        List<BatchDTO> sampleBatches = createSampleExpiredBatches();
        injectMockBatchService();

        when(mockBatchService.getArchivedExpiredBatches()).thenReturn(sampleBatches);

        viewExpiredBatchesUI.start();

        String output = getOutput();
        assertTrue(output.contains("ARCHIVED EXPIRED BATCHES"));

        mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
        mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        verify(mockBatchService, times(1)).getArchivedExpiredBatches();
        verify(mockBatchService, never()).clearArchivedExpiredBatches();
    }

    @Test
    @DisplayName("Handle non-clear input gracefully")
    void testHandleNonClearInput() {
        Scanner scanner = createScanner("x\n");
        viewExpiredBatchesUI = new ViewExpiredBatchesUI(scanner);
        List<BatchDTO> sampleBatches = createSampleExpiredBatches();
        injectMockBatchService();

        when(mockBatchService.getArchivedExpiredBatches()).thenReturn(sampleBatches);

        viewExpiredBatchesUI.start();

        String output = getOutput();
        assertTrue(output.contains("ARCHIVED EXPIRED BATCHES"));

        mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
        mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        verify(mockBatchService, times(1)).getArchivedExpiredBatches();
        verify(mockBatchService, never()).clearArchivedExpiredBatches();
    }

    @ParameterizedTest
    @ValueSource(strings = { "c", "C" })
    @DisplayName("Prompt confirmation for clear command")
    void testPromptConfirmationForClear(String clearInput) {
        Scanner scanner = createScanner(clearInput + "\nno\n");
        viewExpiredBatchesUI = new ViewExpiredBatchesUI(scanner);
        List<BatchDTO> sampleBatches = createSampleExpiredBatches();
        injectMockBatchService();

        when(mockBatchService.getArchivedExpiredBatches()).thenReturn(sampleBatches);

        viewExpiredBatchesUI.start();

        String output = getOutput();
        assertTrue(output.contains("Are you sure you want to permanently delete all archived expired batches?"));
        assertTrue(output.contains("Operation cancelled."));

        verify(mockBatchService, times(1)).getArchivedExpiredBatches();
        verify(mockBatchService, never()).clearArchivedExpiredBatches();
    }

    @ParameterizedTest
    @ValueSource(strings = { "yes", "YES", "y", "Y" })
    @DisplayName("Clear archive on confirmation")
    void testClearArchiveOnConfirmation(String confirmInput) {
        Scanner scanner = createScanner("c\n" + confirmInput + "\n");
        viewExpiredBatchesUI = new ViewExpiredBatchesUI(scanner);
        List<BatchDTO> sampleBatches = createSampleExpiredBatches();
        injectMockBatchService();

        when(mockBatchService.getArchivedExpiredBatches()).thenReturn(sampleBatches);
        when(mockBatchService.clearArchivedExpiredBatches()).thenReturn(true);

        viewExpiredBatchesUI.start();

        String output = getOutput();
        assertTrue(output.contains("Are you sure you want to permanently delete all archived expired batches?"));
        assertTrue(output.contains("All archived expired batches have been permanently deleted."));

        verify(mockBatchService, times(1)).getArchivedExpiredBatches();
        verify(mockBatchService, times(1)).clearArchivedExpiredBatches();
    }

    @ParameterizedTest
    @ValueSource(strings = { "no", "NO", "n", "N", "cancel", "nope" })
    @DisplayName("Cancel clear operation on decline")
    void testCancelClearOperation(String declineInput) {
        Scanner scanner = createScanner("c\n" + declineInput + "\n");
        viewExpiredBatchesUI = new ViewExpiredBatchesUI(scanner);
        List<BatchDTO> sampleBatches = createSampleExpiredBatches();
        injectMockBatchService();

        when(mockBatchService.getArchivedExpiredBatches()).thenReturn(sampleBatches);

        viewExpiredBatchesUI.start();

        String output = getOutput();
        assertTrue(output.contains("Are you sure you want to permanently delete all archived expired batches?"));
        assertTrue(output.contains("Operation cancelled."));

        verify(mockBatchService, times(1)).getArchivedExpiredBatches();
        verify(mockBatchService, never()).clearArchivedExpiredBatches();
    }

    @Test
    @DisplayName("Handle clear operation failure")
    void testHandleClearOperationFailure() {
        Scanner scanner = createScanner("c\nyes\n");
        viewExpiredBatchesUI = new ViewExpiredBatchesUI(scanner);
        List<BatchDTO> sampleBatches = createSampleExpiredBatches();
        injectMockBatchService();

        when(mockBatchService.getArchivedExpiredBatches()).thenReturn(sampleBatches);
        when(mockBatchService.clearArchivedExpiredBatches()).thenReturn(false);

        viewExpiredBatchesUI.start();

        String output = getOutput();
        assertTrue(output.contains("Are you sure you want to permanently delete all archived expired batches?"));
        assertTrue(output.contains("Failed to clear archived batches."));

        verify(mockBatchService, times(1)).getArchivedExpiredBatches();
        verify(mockBatchService, times(1)).clearArchivedExpiredBatches();
    }

    @Test
    @DisplayName("Truncate text within limit")
    void testTruncateWithinLimit() throws Exception {
        Scanner scanner = createScanner("\n");
        ViewExpiredBatchesUI ui = new ViewExpiredBatchesUI(scanner);

        java.lang.reflect.Method truncateMethod = ViewExpiredBatchesUI.class
                .getDeclaredMethod("truncate", String.class, int.class);
        truncateMethod.setAccessible(true);

        String result = (String) truncateMethod.invoke(ui, "Short text", 25);
        assertEquals("Short text", result);
    }

    @Test
    @DisplayName("Truncate text at exact limit")
    void testTruncateAtExactLimit() throws Exception {
        Scanner scanner = createScanner("\n");
        ViewExpiredBatchesUI ui = new ViewExpiredBatchesUI(scanner);

        java.lang.reflect.Method truncateMethod = ViewExpiredBatchesUI.class
                .getDeclaredMethod("truncate", String.class, int.class);
        truncateMethod.setAccessible(true);

        String exactLengthText = "Exactly twenty five chars";
        String result = (String) truncateMethod.invoke(ui, exactLengthText, 25);
        assertEquals("Exactly twenty five chars", result);
    }

    @Test
    @DisplayName("Truncate empty string")
    void testTruncateEmptyString() throws Exception {
        Scanner scanner = createScanner("\n");
        ViewExpiredBatchesUI ui = new ViewExpiredBatchesUI(scanner);

        java.lang.reflect.Method truncateMethod = ViewExpiredBatchesUI.class
                .getDeclaredMethod("truncate", String.class, int.class);
        truncateMethod.setAccessible(true);

        String result = (String) truncateMethod.invoke(ui, "", 25);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Truncate single character")
    void testTruncateSingleCharacter() throws Exception {
        Scanner scanner = createScanner("\n");
        ViewExpiredBatchesUI ui = new ViewExpiredBatchesUI(scanner);

        java.lang.reflect.Method truncateMethod = ViewExpiredBatchesUI.class
                .getDeclaredMethod("truncate", String.class, int.class);
        truncateMethod.setAccessible(true);

        String result = (String) truncateMethod.invoke(ui, "A", 25);
        assertEquals("A", result);
    }

    @Test
    @DisplayName("Test display with long name truncation")
    void testDisplayWithLongNameTruncation() {
        Scanner scanner = createScanner("\n");
        viewExpiredBatchesUI = new ViewExpiredBatchesUI(scanner);

        List<BatchDTO> batches = new ArrayList<>();
        BatchDTO longNameBatch = createBatchWithLongName();
        batches.add(longNameBatch);

        injectMockBatchService();
        when(mockBatchService.getArchivedExpiredBatches()).thenReturn(batches);

        viewExpiredBatchesUI.start();

        String output = getOutput();
        assertTrue(output.contains("LONGNAME1"));
        assertTrue(output.contains("Premium Organic Free R...")); // Truncated version

        mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
        mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        verify(mockBatchService, times(1)).getArchivedExpiredBatches();
    }

    @Test
    @DisplayName("Complete clear workflow successfully")
    void testCompleteClearWorkflow() {
        Scanner scanner = createScanner("c\ny\n");
        viewExpiredBatchesUI = new ViewExpiredBatchesUI(scanner);
        List<BatchDTO> sampleBatches = createSampleExpiredBatches();
        injectMockBatchService();

        when(mockBatchService.getArchivedExpiredBatches()).thenReturn(sampleBatches);
        when(mockBatchService.clearArchivedExpiredBatches()).thenReturn(true);

        viewExpiredBatchesUI.start();

        String output = getOutput();
        assertTrue(output.contains("ARCHIVED EXPIRED BATCHES"));
        assertTrue(output.contains("MILK001"));
        assertTrue(output.contains("Fresh Whole Milk"));
        assertTrue(output.contains("Press 'c' to clear all archived records"));
        assertTrue(output.contains("Are you sure you want to permanently delete"));
        assertTrue(output.contains("All archived expired batches have been permanently deleted."));

        mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
        mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        verify(mockBatchService, times(1)).getArchivedExpiredBatches();
        verify(mockBatchService, times(1)).clearArchivedExpiredBatches();
    }

    @Test
    @DisplayName("Handle mixed case clear input")
    void testHandleMixedCaseClearInput() {
        Scanner scanner = createScanner("C\nYES\n");
        viewExpiredBatchesUI = new ViewExpiredBatchesUI(scanner);
        List<BatchDTO> sampleBatches = createSampleExpiredBatches();
        injectMockBatchService();

        when(mockBatchService.getArchivedExpiredBatches()).thenReturn(sampleBatches);
        when(mockBatchService.clearArchivedExpiredBatches()).thenReturn(true);

        viewExpiredBatchesUI.start();

        String output = getOutput();
        assertTrue(output.contains("All archived expired batches have been permanently deleted."));

        verify(mockBatchService, times(1)).getArchivedExpiredBatches();
        verify(mockBatchService, times(1)).clearArchivedExpiredBatches();
    }
}