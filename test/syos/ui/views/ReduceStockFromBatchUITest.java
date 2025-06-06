package syos.ui.views;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import syos.data.ItemGateway;
import syos.data.OnlineItemGateway;
import syos.dto.ItemDTO;
import syos.service.BatchService;
import syos.service.OnlineBatchService;
import syos.util.ConsoleUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ReduceStockFromBatchUI Tests")
class ReduceStockFromBatchUITest {

    private ReduceStockFromBatchUI ui;
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

    @Test
    @DisplayName("Should display main menu correctly")
    void startMainMenu() {
        Scanner scanner = createScanner("0\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class)) {
            ui.start();

            String output = getOutput();
            assertTrue(output.contains("RESTOCK FROM BATCH"));
            assertTrue(output.contains("1. Restock In-Store Inventory"));
            assertTrue(output.contains("2. Restock Online Inventory"));
            assertTrue(output.contains("0. Back"));
            assertTrue(output.contains("Choose destination:"));

            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
        }
    }

    @Test
    @DisplayName("Should exit when option 0 selected")
    void startExitOption() {
        Scanner scanner = createScanner("0\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class)) {
            ui.start();

            String output = getOutput();
            assertTrue(output.contains("Choose destination:"));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
        }
    }

    @Test
    @DisplayName("Should handle invalid menu option")
    void startInvalidOption() {
        Scanner scanner = createScanner("99\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class)) {
            ui.start();

            String output = getOutput();
            assertTrue(output.contains("Invalid option. Please try again."));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), times(1));
            mockedUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }
    }

    @Test
    @DisplayName("Should navigate to in-store restock correctly")
    void startInStoreOption() {
        Scanner scanner = createScanner("1\nTEST001\n10\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleItems()));
                MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                        (mock, context) -> when(mock.restockItem("TEST001", 10)).thenReturn(true))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains("RESTOCK IN-STORE FROM BATCH"));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), times(2));
            mockedUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }
    }

    @Test
    @DisplayName("Should navigate to online restock correctly")
    void startOnlineOption() {
        Scanner scanner = createScanner("2\nONL001\n15\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<OnlineItemGateway> mockedOnlineGateway = mockConstruction(OnlineItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleOnlineItems()));
                MockedConstruction<OnlineBatchService> mockedOnlineBatchService = mockConstruction(
                        OnlineBatchService.class,
                        (mock, context) -> when(mock.restockOnlineItem("ONL001", 15)).thenReturn(true))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains("RESTOCK ONLINE FROM BATCH"));
            mockedUtils.verify(() -> ConsoleUtils.clearScreen(), times(2));
            mockedUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }
    }

    @Test
    @DisplayName("Should display empty in-store inventory message")
    void inStoreRestockEmptyInventory() {
        Scanner scanner = createScanner("1\nTEST001\n10\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(Collections.emptyList()));
                MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                        (mock, context) -> when(mock.restockItem("TEST001", 10)).thenReturn(true))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains("No items in in-store inventory."));
            assertTrue(output.contains("Stock moved to in-store inventory successfully."));
        }
    }

    @Test
    @DisplayName("Should handle negative quantity in-store restock")
    void inStoreRestockNegativeQuantity() {
        Scanner scanner = createScanner("1\nTEST001\n-5\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleItems()))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains("Quantity must be positive."));
            mockedUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }
    }

    @Test
    @DisplayName("Should handle zero quantity in-store restock")
    void inStoreRestockZeroQuantity() {
        Scanner scanner = createScanner("1\nTEST001\n0\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleItems()))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains("Quantity must be positive."));
            mockedUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }
    }

    @Test
    @DisplayName("Should handle invalid quantity format in-store")
    void inStoreRestockInvalidFormat() {
        Scanner scanner = createScanner("1\nTEST001\nabc\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleItems()))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains("Invalid quantity format."));
            mockedUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }
    }

    @Test
    @DisplayName("Should handle insufficient batch quantity in-store")
    void inStoreRestockInsufficientBatch() {
        Scanner scanner = createScanner("1\nTEST001\n100\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleItems()));
                MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                        (mock, context) -> when(mock.restockItem("TEST001", 100)).thenReturn(false))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains("Not enough quantity in batches to fulfill the request."));
        }
    }

    @Test
    @DisplayName("Should convert item code to uppercase in-store")
    void inStoreRestockLowerCaseItemCode() {
        Scanner scanner = createScanner("1\ntest001\n10\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleItems()));
                MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                        (mock, context) -> when(mock.restockItem("TEST001", 10)).thenReturn(true))) {

            ui.start();

            // Verify that the service was called with uppercase item code
            verify(mockedBatchService.constructed().get(0)).restockItem("TEST001", 10);
        }
    }

    @Test
    @DisplayName("Should display empty online inventory message")
    void onlineRestockEmptyInventory() {
        Scanner scanner = createScanner("2\nONL001\n10\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<OnlineItemGateway> mockedOnlineGateway = mockConstruction(OnlineItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(Collections.emptyList()));
                MockedConstruction<OnlineBatchService> mockedOnlineBatchService = mockConstruction(
                        OnlineBatchService.class,
                        (mock, context) -> when(mock.restockOnlineItem("ONL001", 10)).thenReturn(true))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains("No items in online inventory."));
            assertTrue(output.contains("✓ Stock moved to online inventory successfully."));
        }
    }

    @Test
    @DisplayName("Should display online inventory with items")
    void onlineRestockWithItems() {
        Scanner scanner = createScanner("2\nONL001\n20\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<OnlineItemGateway> mockedOnlineGateway = mockConstruction(OnlineItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleOnlineItems()));
                MockedConstruction<OnlineBatchService> mockedOnlineBatchService = mockConstruction(
                        OnlineBatchService.class,
                        (mock, context) -> when(mock.restockOnlineItem("ONL001", 20)).thenReturn(true))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains("CURRENT ONLINE INVENTORY:"));
            assertTrue(output.contains("ONL001"));
            assertTrue(output.contains("Laptop Computer"));
            assertTrue(output.contains("✓ Stock moved to online inventory successfully."));
        }
    }

    @Test
    @DisplayName("Should handle negative quantity online restock")
    void onlineRestockNegativeQuantity() {
        Scanner scanner = createScanner("2\nONL001\n-10\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<OnlineItemGateway> mockedOnlineGateway = mockConstruction(OnlineItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleOnlineItems()))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains("Quantity must be positive."));
            mockedUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }
    }

    @Test
    @DisplayName("Should handle zero quantity online restock")
    void onlineRestockZeroQuantity() {
        Scanner scanner = createScanner("2\nONL001\n0\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<OnlineItemGateway> mockedOnlineGateway = mockConstruction(OnlineItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleOnlineItems()))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains("Quantity must be positive."));
            mockedUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }
    }

    @Test
    @DisplayName("Should handle invalid quantity format online")
    void onlineRestockInvalidFormat() {
        Scanner scanner = createScanner("2\nONL001\nxyz\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<OnlineItemGateway> mockedOnlineGateway = mockConstruction(OnlineItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleOnlineItems()))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains("Invalid quantity format."));
            mockedUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), times(1));
        }
    }

    @Test
    @DisplayName("Should handle insufficient batch quantity online")
    void onlineRestockInsufficientBatch() {
        Scanner scanner = createScanner("2\nONL001\n500\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<OnlineItemGateway> mockedOnlineGateway = mockConstruction(OnlineItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleOnlineItems()));
                MockedConstruction<OnlineBatchService> mockedOnlineBatchService = mockConstruction(
                        OnlineBatchService.class,
                        (mock, context) -> when(mock.restockOnlineItem("ONL001", 500)).thenReturn(false))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains("✗ Not enough quantity in batches to fulfill the request."));
        }
    }

    @Test
    @DisplayName("Should convert item code to uppercase online")
    void onlineRestockLowerCaseItemCode() {
        Scanner scanner = createScanner("2\nonl001\n15\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<OnlineItemGateway> mockedOnlineGateway = mockConstruction(OnlineItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleOnlineItems()));
                MockedConstruction<OnlineBatchService> mockedOnlineBatchService = mockConstruction(
                        OnlineBatchService.class,
                        (mock, context) -> when(mock.restockOnlineItem("ONL001", 15)).thenReturn(true))) {

            ui.start();

            verify(mockedOnlineBatchService.constructed().get(0)).restockOnlineItem("ONL001", 15);
        }
    }

    @Test
    @DisplayName("Should handle mixed case item codes correctly")
    void onlineRestockMixedCaseItemCode() {
        Scanner scanner = createScanner("2\nOnL001\n25\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<OnlineItemGateway> mockedOnlineGateway = mockConstruction(OnlineItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleOnlineItems()));
                MockedConstruction<OnlineBatchService> mockedOnlineBatchService = mockConstruction(
                        OnlineBatchService.class,
                        (mock, context) -> when(mock.restockOnlineItem("ONL001", 25)).thenReturn(true))) {

            ui.start();

            verify(mockedOnlineBatchService.constructed().get(0)).restockOnlineItem("ONL001", 25);
        }
    }

    @Test
    @DisplayName("Should truncate long item names correctly")
    void truncateStringMethod() {
        Scanner scanner = createScanner("0\n");
        ui = new ReduceStockFromBatchUI(scanner);

        // Test method through displayed output in inventory
        Scanner testScanner = createScanner("1\nLONG001\n5\n");
        ui = new ReduceStockFromBatchUI(testScanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                        (mock, context) -> {
                            List<ItemDTO> longNameItems = Arrays.asList(
                                    new ItemDTO("LONG001",
                                            "This is a very long product name that definitely exceeds the thirty character limit",
                                            299.99, 25),
                                    new ItemDTO("SHORT", "Short", 10.00, 5));
                            when(mock.getAllItems()).thenReturn(longNameItems);
                        });
                MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                        (mock, context) -> when(mock.restockItem("LONG001", 5)).thenReturn(true))) {
            ui.start();
            String output = getOutput();
            // The truncation method takes the first 27 characters (30-3) and adds "..."
            assertTrue(output.contains("This is a very long product..."));
            assertTrue(output.contains("Short"));
        }
    }

    @Test
    @DisplayName("Should handle large quantities successfully")
    void onlineRestockLargeQuantity() {
        Scanner scanner = createScanner("2\nONL001\n999\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<OnlineItemGateway> mockedOnlineGateway = mockConstruction(OnlineItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleOnlineItems()));
                MockedConstruction<OnlineBatchService> mockedOnlineBatchService = mockConstruction(
                        OnlineBatchService.class,
                        (mock, context) -> when(mock.restockOnlineItem("ONL001", 999)).thenReturn(true))) {

            ui.start();

            String output = getOutput();
            assertTrue(output.contains(" Stock moved to online inventory successfully."));
            verify(mockedOnlineBatchService.constructed().get(0)).restockOnlineItem("ONL001", 999);
        }
    }

    @Test
    @DisplayName("Should handle empty item code input")
    void inStoreRestockEmptyItemCode() {
        Scanner scanner = createScanner("1\n\n10\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleItems()));
                MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                        (mock, context) -> when(mock.restockItem("", 10)).thenReturn(false))) {

            ui.start();

            verify(mockedBatchService.constructed().get(0)).restockItem("", 10);
        }
    }

    @Test
    @DisplayName("Should handle whitespace in item code input")
    void inStoreRestockWhitespaceItemCode() {
        Scanner scanner = createScanner("1\n  TEST001  \n10\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleItems()));
                MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                        (mock, context) -> when(mock.restockItem("TEST001", 10)).thenReturn(true))) {

            ui.start();

            verify(mockedBatchService.constructed().get(0)).restockItem("TEST001", 10);
        }
    }

    @Test
    @DisplayName("Should handle whitespace in quantity input")
    void onlineRestockWhitespaceQuantity() {
        Scanner scanner = createScanner("2\nONL001\n  25  \n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<OnlineItemGateway> mockedOnlineGateway = mockConstruction(OnlineItemGateway.class,
                        (mock, context) -> when(mock.getAllItems()).thenReturn(createSampleOnlineItems()));
                MockedConstruction<OnlineBatchService> mockedOnlineBatchService = mockConstruction(
                        OnlineBatchService.class,
                        (mock, context) -> when(mock.restockOnlineItem("ONL001", 25)).thenReturn(true))) {

            ui.start();

            verify(mockedOnlineBatchService.constructed().get(0)).restockOnlineItem("ONL001", 25);
        }
    }

    // Helper methods to create test data
    private List<ItemDTO> createSampleItems() {
        return Arrays.asList(
                new ItemDTO("TEST001", "Test Product 1", 50.00, 100),
                new ItemDTO("TEST002", "Test Product 2", 75.50, 50),
                new ItemDTO("TEST003", "Test Product 3", 120.00, 25));
    }

    private List<ItemDTO> createSampleOnlineItems() {
        return Arrays.asList(
                new ItemDTO("ONL001", "Laptop Computer", 1299.99, 15),
                new ItemDTO("ONL002", "Wireless Mouse", 29.99, 100),
                new ItemDTO("ONL003", "Keyboard", 89.99, 50),
                new ItemDTO("ONL004", "Monitor", 299.99, 20));
    }

    @Test
    @DisplayName("Debug truncation output")
    void debugTruncation() {
        Scanner scanner = createScanner("1\nLONG001\n5\n");
        ui = new ReduceStockFromBatchUI(scanner);

        try (MockedStatic<ConsoleUtils> mockedUtils = mockStatic(ConsoleUtils.class);
                MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                        (mock, context) -> {
                            List<ItemDTO> longNameItems = Arrays.asList(
                                    new ItemDTO("LONG001",
                                            "Very Long Product Name That Exceeds Thirty Characters Limit", 299.99, 25));
                            when(mock.getAllItems()).thenReturn(longNameItems);
                        });
                MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                        (mock, context) -> when(mock.restockItem("LONG001", 5)).thenReturn(true))) {

            ui.start();

            String output = getOutput();
            System.out.println("DEBUG OUTPUT:");
            System.out.println(output);
            System.out.println("DEBUG END");
        }
    }
}