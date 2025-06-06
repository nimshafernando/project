package syos.ui.views;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import syos.data.BatchGateway;
import syos.data.ItemGateway;
import syos.data.OnlineItemGateway;
import syos.dto.BatchDTO;
import syos.service.BatchService;
import syos.service.BatchService.StoreType;
import syos.util.ConsoleUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

@DisplayName("AddBatchStockUI Tests")
class AddBatchStockUITest {

    @Mock
    private BatchService mockBatchService;

    @Mock
    private Scanner mockScanner;
    private AddBatchStockUI addBatchStockUI;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create AddBatchStockUI instance successfully with scanner")
        void shouldCreateInstanceSuccessfullyWithScanner() {
            // Given
            Scanner scanner = new Scanner("test input");

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
    }

    @Nested
    @DisplayName("Start Method Tests")
    class StartMethodTests {

        @Test
        @DisplayName("Should successfully add batch stock for in-store inventory")
        void shouldSuccessfullyAddBatchStockForInStore() {
            // Given
            String input = "1\nAPPLE001\nApple iPhone 14\n999.99\n50\n2024-12-31\n";
            Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
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
            }
        }

        @Test
        @DisplayName("Should successfully add batch stock for online inventory")
        void shouldSuccessfullyAddBatchStockForOnline() {
            // Given
            String input = "2\nLAPTOP001\nGaming Laptop\n1499.99\n25\n2025-06-15\n";
            Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
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
            }
        }

        @Test
        @DisplayName("Should handle cancel operation")
        void shouldHandleCancelOperation() {
            // Given
            String input = "0\n";
            Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then - should return without any batch operations
                mockedConsoleUtils.verify(() -> ConsoleUtils.clearScreen(), atLeastOnce());
            }
        }

        @ParameterizedTest
        @ValueSource(strings = { "3", "99", "invalid", "" })
        @DisplayName("Should handle invalid store type selection")
        void shouldHandleInvalidStoreTypeSelection(String invalidChoice) {
            // Given
            String input = invalidChoice + "\n";
            Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
            addBatchStockUI = new AddBatchStockUI(scanner);

            try (MockedStatic<ConsoleUtils> mockedConsoleUtils = mockStatic(ConsoleUtils.class)) {
                // When
                assertDoesNotThrow(() -> addBatchStockUI.start());

                // Then
                mockedConsoleUtils.verify(() -> ConsoleUtils.pause(any(Scanner.class)), atLeastOnce());
            }
        }

        @Test
        @DisplayName("Should handle service failure gracefully")
        void shouldHandleServiceFailure() {
            // Given
            String input = "1\nTEST001\nTest Item\n50.0\n10\n2024-12-31\n";
            Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
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
            }
        }
    }
}
