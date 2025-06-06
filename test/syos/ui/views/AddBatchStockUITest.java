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
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import syos.data.BatchGateway;
import syos.data.ItemGateway;
import syos.data.OnlineItemGateway;
import syos.dto.BatchDTO;
import syos.dto.ItemDTO;
import syos.dto.OnlineInventoryDTO;
import syos.service.BatchService;
import syos.service.BatchService.StoreType;
import syos.util.ConsoleUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

@DisplayName("AddBatchStockUI Tests - Complete Coverage")
class AddBatchStockUITest {

    @Mock
    private BatchService mockBatchService;

    @Mock
    private Scanner mockScanner;

    private AddBatchStockUI addBatchStockUI;
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

    private ItemDTO createMockItem(String code, String name, double price) {
        return new ItemDTO(code, name, price, 100);
    }

    private OnlineInventoryDTO createMockOnlineItem(String code, String name, double price, int quantity) {
        return new OnlineInventoryDTO(code, name, price, quantity);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        @Test
        @DisplayName("Should create AddBatchStockUI instance successfully with scanner")
        void shouldCreateInstanceSuccessfullyWithScanner() {
            // Given
            Scanner scanner = createScanner("test input");

            // When
            addBatchStockUI = new AddBatchStockUI(scanner);

            // Then
            assertNotNull(addBatchStockUI);
        }

        @Test
        @DisplayName("Should handle null scanner in constructor")
        void shouldHandleNullScannerInConstructor() {
            // When & Then
            assertDoesNotThrow(() -> new AddBatchStockUI(null));
        }

        @Test
        @DisplayName("Should create instance with empty input")
        void shouldCreateInstanceWithEmptyInput() {
            // Given
            Scanner scanner = createScanner("");

            // When & Then
            assertDoesNotThrow(() -> new AddBatchStockUI(scanner));
        }
    }

    @Nested
    @DisplayName("Start Method Tests - Store Type Selection")
    class StoreTypeSelectionTests {

        @Test
        @DisplayName("Should successfully add batch stock for in-store inventory")
        void shouldSuccessfullyAddBatchStockForInStore() {
            // Given
            String input = "1\nAPPLE001\nApple iPhone 14\n999.99\n50\n2024-01-01\n2024-12-31\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("APPLE001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), eq(StoreType.IN_STORE)))
                                    .thenReturn(true))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class),
                        eq(StoreType.IN_STORE));

                String output = getOutput();
                assertTrue(output.contains("ADD NEW BATCH STOCK") || output.contains("ADDING BATCH"));
            }
        }

        @Test
        @DisplayName("Should successfully add batch stock for online inventory")
        void shouldSuccessfullyAddBatchStockForOnline() {
            // Given
            String input = "2\nLAPTOP001\nGaming Laptop\n1499.99\n25\n2024-01-01\n2025-06-15\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class);
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("LAPTOP001")).thenReturn(null));
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), eq(StoreType.ONLINE)))
                                    .thenReturn(true))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class), eq(StoreType.ONLINE));

                String output = getOutput();
                assertTrue(output.contains("ADD NEW BATCH STOCK") || output.contains("ADDING BATCH"));
            }
        }

        @Test
        @DisplayName("Should handle cancel operation")
        void shouldHandleCancelOperation() {
            // Given
            String input = "0\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then - should return without any batch operations
                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
            }
        }

        @ParameterizedTest
        @ValueSource(strings = { "3", "99", "invalid", "", "-1", "abc" })
        @DisplayName("Should handle invalid store type selection")
        void shouldHandleInvalidStoreTypeSelection(String invalidChoice) {
            // Given
            String input = invalidChoice + "\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());

                String output = getOutput();
                assertTrue(output.contains("Invalid choice") || output.contains("Please try again"));
            }
        }
    }

    @Nested
    @DisplayName("Item Input and Validation Tests")
    class ItemInputValidationTests {

        @Test
        @DisplayName("Should handle existing item in store - keep current price")
        void shouldHandleExistingItemInStoreKeepCurrentPrice() {
            // Given
            String input = "1\nEXIST001\n\n30\n2024-01-01\n2024-12-31\n"; // Empty price input to keep current
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            ItemDTO existingItem = createMockItem("EXIST001", "Existing Item", 25.99);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("EXIST001")).thenReturn(existingItem));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), any(StoreType.class)))
                                    .thenReturn(true))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class), any(StoreType.class));

                String output = getOutput();
                assertTrue(output.contains("Existing item found") || output.contains("Current selling price"));
            }
        }

        @Test
        @DisplayName("Should handle existing item in store - update price")
        void shouldHandleExistingItemInStoreUpdatePrice() {
            // Given
            String input = "1\nEXIST001\n35.99\n30\n2024-01-01\n2024-12-31\n"; // New price
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            ItemDTO existingItem = createMockItem("EXIST001", "Existing Item", 25.99);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("EXIST001")).thenReturn(existingItem));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), any(StoreType.class)))
                                    .thenReturn(true))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class), any(StoreType.class));

                String output = getOutput();
                assertTrue(output.contains("BATCH ADDED SUCCESSFULLY") && output.contains("Item price updated"));
            }
        }

        @Test
        @DisplayName("Should handle existing online item")
        void shouldHandleExistingOnlineItem() {
            // Given
            String input = "2\nONLINE001\n\n40\n2024-01-01\n2025-01-15\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            ItemDTO existingOnlineItem = createMockItem("ONLINE001", "Online Item", 15.99);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class);
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("ONLINE001")).thenReturn(existingOnlineItem));
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), any(StoreType.class)))
                                    .thenReturn(true))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class), any(StoreType.class));
            }
        }
    }

    @Nested
    @DisplayName("Price Input Validation Tests")
    class PriceInputValidationTests {

        @Test
        @DisplayName("Should handle invalid price input - non-numeric")
        void shouldHandleInvalidPriceInputNonNumeric() {
            // Given
            String input = "1\nTEST001\nTest Item\ninvalid\n19.99\n10\n2024-01-01\n2024-12-31\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TEST001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), any(StoreType.class)))
                                    .thenReturn(true))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                String output = getOutput();
                assertTrue(output.contains("Invalid price format") || output.contains("BATCH ADDED SUCCESSFULLY"));
            }
        }

        @Test
        @DisplayName("Should handle negative price input")
        void shouldHandleNegativePriceInput() {
            // Given
            String input = "1\nTEST001\nTest Item\n-10.99\n"; // No recovery input needed as it returns
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TEST001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class)) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());
            }
        }

        @Test
        @DisplayName("Should handle zero price input")
        void shouldHandleZeroPriceInput() {
            // Given
            String input = "1\nTEST001\nTest Item\n0\n"; // No recovery input needed as it returns
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TEST001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class)) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());
            }
        }

        @Test
        @DisplayName("Should handle very large price input")
        void shouldHandleVeryLargePriceInput() {
            // Given
            String input = "1\nTEST001\nTest Item\n999999.99\n10\n2024-01-01\n2024-12-31\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TEST001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), any(StoreType.class)))
                                    .thenReturn(true))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class), any(StoreType.class));
            }
        }
    }

    @Nested
    @DisplayName("Quantity Input Validation Tests")
    class QuantityInputValidationTests {

        @Test
        @DisplayName("Should handle invalid quantity input - non-numeric")
        void shouldHandleInvalidQuantityInputNonNumeric() {
            // Given
            String input = "1\nTEST001\nTest Item\n15.99\ninvalid\n"; // No recovery input
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TEST001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class)) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                String output = getOutput();
                assertTrue(output.contains("Invalid quantity format") || output.contains("Quantity must be positive"));
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());
            }
        }

        @Test
        @DisplayName("Should handle negative quantity input")
        void shouldHandleNegativeQuantityInput() {
            // Given
            String input = "1\nTEST001\nTest Item\n15.99\n-5\n"; // No recovery input
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TEST001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class)) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                String output = getOutput();
                assertTrue(output.contains("Quantity must be positive"));
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());
            }
        }

        @Test
        @DisplayName("Should handle zero quantity input")
        void shouldHandleZeroQuantityInput() {
            // Given
            String input = "1\nTEST001\nTest Item\n15.99\n0\n"; // No recovery input
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TEST001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class)) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                String output = getOutput();
                assertTrue(output.contains("Quantity must be positive"));
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());
            }
        }

        @Test
        @DisplayName("Should handle very large quantity input")
        void shouldHandleVeryLargeQuantityInput() {
            // Given
            String input = "1\nTEST001\nTest Item\n15.99\n999999\n2024-01-01\n2024-12-31\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TEST001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), any(StoreType.class)))
                                    .thenReturn(true))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class), any(StoreType.class));
            }
        }
    }

    @Nested
    @DisplayName("Date Input Validation Tests")
    class DateInputValidationTests {

        @Test
        @DisplayName("Should handle invalid date format")
        void shouldHandleInvalidDateFormat() {
            // Given
            String input = "1\nTEST001\nTest Item\n15.99\n10\ninvalid-date\n"; // No recovery input
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TEST001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class)) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                String output = getOutput();
                assertTrue(output.contains("Invalid date format"));
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());
            }
        }

        @Test
        @DisplayName("Should handle valid future expiry date")
        void shouldHandleValidFutureExpiryDate() {
            // Given
            LocalDate futureDate = LocalDate.now().plusYears(1);
            String input = String.format("1\nTEST001\nTest Item\n15.99\n10\n2024-01-01\n%s\n", futureDate.toString());
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TEST001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), any(StoreType.class)))
                                    .thenReturn(true))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class), any(StoreType.class));
            }
        }

        @Test
        @DisplayName("Should handle empty date input")
        void shouldHandleEmptyDateInput() {
            // Given
            String input = "1\nTEST001\nTest Item\n15.99\n10\n\n"; // Empty date input
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TEST001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class)) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                String output = getOutput();
                assertTrue(output.contains("Invalid date format"));
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());
            }
        }
    }

    @Nested
    @DisplayName("Service Integration Tests")
    class ServiceIntegrationTests {

        @Test
        @DisplayName("Should handle service failure gracefully")
        void shouldHandleServiceFailure() {
            // Given
            String input = "1\nTEST001\nTest Item\n50.0\n10\n2024-01-01\n2024-12-31\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TEST001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), eq(StoreType.IN_STORE)))
                                    .thenReturn(false))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then - should attempt the operation and handle failure
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class),
                        eq(StoreType.IN_STORE));
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());

                String output = getOutput();
                assertTrue(output.contains("Failed to add batch"));
            }
        }

        @Test
        @DisplayName("Should handle service exception gracefully")
        void shouldHandleServiceException() {
            // Given
            String input = "1\nTEST001\nTest Item\n50.0\n10\n2024-01-01\n2024-12-31\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TEST001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), eq(StoreType.IN_STORE)))
                                    .thenThrow(new RuntimeException("Database connection failed")))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class),
                        eq(StoreType.IN_STORE));
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());

                String output = getOutput();
                assertTrue(output.contains("Error:") && output.contains("Database connection failed"));
            }
        }

        @Test
        @DisplayName("Should handle successful batch creation with new item")
        void shouldHandleSuccessfulBatchCreationWithNewItem() {
            // Given
            String input = "1\nNEW001\nNew Item\n29.99\n15\n2024-01-01\n2024-12-31\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("NEW001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), eq(StoreType.IN_STORE)))
                                    .thenReturn(true))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class),
                        eq(StoreType.IN_STORE));

                String output = getOutput();
                assertTrue(output.contains("BATCH ADDED SUCCESSFULLY") && output.contains("New item created"));
            }
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle item code with whitespace")
        void shouldHandleItemCodeWithWhitespace() {
            // Given
            String input = "1\n  trim001  \nTrim Item\n19.99\n5\n2024-01-01\n2024-12-31\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("TRIM001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), any(StoreType.class)))
                                    .thenReturn(true))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class), any(StoreType.class));
            }
        }

        @Test
        @DisplayName("Should handle item name with special characters")
        void shouldHandleItemNameWithSpecialCharacters() {
            // Given
            String input = "1\nSPECIAL001\nSpecial Item & Co. (Ltd.)\n99.99\n1\n2024-01-01\n2024-12-31\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("SPECIAL001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), any(StoreType.class)))
                                    .thenReturn(true))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class), any(StoreType.class));

                String output = getOutput();
                assertTrue(output.contains("BATCH ADDED SUCCESSFULLY"));
            }
        }

        @Test
        @DisplayName("Should handle decimal price with multiple decimal places")
        void shouldHandleDecimalPriceWithMultipleDecimalPlaces() {
            // Given
            String input = "1\nDECIMAL001\nDecimal Item\n19.9999\n5\n2024-01-01\n2024-12-31\n";
            Scanner scanner = createScanner(input);
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class);
                    MockedConstruction<BatchGateway> mockedBatchGateway = mockConstruction(BatchGateway.class);
                    MockedConstruction<ItemGateway> mockedItemGateway = mockConstruction(ItemGateway.class,
                            (mock, context) -> when(mock.getItemByCode("DECIMAL001")).thenReturn(null));
                    MockedConstruction<OnlineItemGateway> mockedOnlineItemGateway = mockConstruction(
                            OnlineItemGateway.class);
                    MockedConstruction<BatchService> mockedBatchService = mockConstruction(BatchService.class,
                            (mock, context) -> when(mock.addNewBatch(any(BatchDTO.class), any(StoreType.class)))
                                    .thenReturn(true))) {

                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                verify(mockedBatchService.constructed().get(0)).addNewBatch(any(BatchDTO.class), any(StoreType.class));
            }
        }
    }
}
