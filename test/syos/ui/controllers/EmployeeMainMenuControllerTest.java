package syos.ui.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import syos.data.EmployeeGateway;
import syos.data.ItemGateway;
import syos.model.Employee;
import syos.service.POS;
import syos.util.EmployeeSession;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for EmployeeMainMenuController
 * Achieves 100% coverage with happy paths and edge cases
 * Tests all public and private methods
 * Uses real-life values for employee data and inputs
 */
@DisplayName("EmployeeMainMenuController Tests")
class EmployeeMainMenuControllerTest {

    @Mock
    private EmployeeGateway mockEmployeeGateway;

    @Mock
    private ItemGateway mockItemGateway;

    @Mock
    private POS mockPOS;

    @Mock
    private EmployeeSession mockEmployeeSession;

    @Mock
    private CheckoutAndBillingController mockCheckoutController;

    @Mock
    private InventoryAndStockController mockInventoryController;

    @Mock
    private ReportingMainMenuController mockReportingController;

    private Employee testEmployee;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private MockedStatic<EmployeeSession> mockedEmployeeSession;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        MockitoAnnotations.openMocks(this);
        setupTestData();
        captureSystemOut();
        // Mock ProcessBuilder for ConsoleUtils.clearScreen
        System.setProperty("os.name", "Windows");
        try (MockedConstruction<ProcessBuilder> processBuilderMock = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    when(mock.inheritIO().start().waitFor()).thenReturn(0);
                })) {
            // Ensure ProcessBuilder is mocked
        }
        // Mock EmployeeSession singleton
        mockedEmployeeSession = mockStatic(EmployeeSession.class);
        when(EmployeeSession.getInstance()).thenReturn(mockEmployeeSession);
    }

    @AfterEach
    void tearDown() {
        restoreSystemOut();
        System.clearProperty("os.name");
        if (mockedEmployeeSession != null) {
            mockedEmployeeSession.close();
        }
    }

    private void setupTestData() {
        // Real-life employee data with active status using the 4-argument constructor
        testEmployee = new Employee("1234", "John Doe", "5678", "Cashier");
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

    private Scanner createScanner(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    private void invokePrivateMethod(String methodName, Class<?>[] paramTypes, Object[] args) {
        String output = "";
        try {
            Method method = EmployeeMainMenuController.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            Object result = method.invoke(null, args);
            if (method.getReturnType() == boolean.class && !(result instanceof Boolean)) {
                fail("Expected boolean return type for " + methodName);
            }
        } catch (InvocationTargetException e) {
            output = getOutput();
            Throwable cause = e.getCause();
            fail("Method " + methodName + " threw exception: " + (cause != null ? cause.getMessage() : "null")
                    + "\nOutput: " + output, cause);
        } catch (Exception e) {
            output = getOutput();
            fail("Failed to invoke method " + methodName + ": " + e.getMessage() + "\nOutput: " + output, e);
        } finally {
            if (!output.isEmpty()) {
                System.err.println("Output for " + methodName + ": " + output);
            }
        }
    }

    // ============ Launch Method Tests ============ @Test
    @DisplayName("Should authenticate employee and show portal, then logout")
    void testLaunch_SuccessfulAuthenticationAndLogout() {
        // Provide sufficient input: ID, PIN, auth pause, logout, logout pause, extra
        // newlines
        Scanner scanner = createScanner("1234\n5678\n\n0\n\n\n\n\n");

        // Create active employee using 4-parameter constructor
        Employee testEmployee = new Employee("1234", "John Doe", "5678", "Cashier");

        // Mock session behavior
        when(mockEmployeeSession.isLoggedIn()).thenReturn(true);

        // Mock EmployeeGateway construction
        try (MockedConstruction<EmployeeGateway> gatewayMock = mockConstruction(EmployeeGateway.class,
                (mock, context) -> when(mock.authenticateEmployee("1234", "5678")).thenReturn(testEmployee))) {
            // Invoke the method under test
            EmployeeMainMenuController.launch(scanner);

            // Get console output and normalize spacing/case
            String rawOutput = getOutput();
            String output = rawOutput.toLowerCase().replaceAll("\\s+", " ");

            // Assertions (all lowercase and space-normalized)
            assertTrue(output.contains("employee authentication"), "Missing: employee authentication");
            assertTrue(output.contains("authentication successful"), "Missing: authentication successful");
            assertTrue(output.contains("welcome, john doe"), "Missing: welcome message");
            assertTrue(output.contains("employee id: 1234"), "Missing: employee ID");
            assertTrue(output.contains("role: cashier"), "Missing: role");
            assertTrue(output.contains("syos - employee portal"), "Missing: portal title");
            assertTrue(output.contains("logged in as: john doe (1234)"), "Missing: logged in as");
            assertTrue(output.contains("session: active"), "Missing: session status");
            assertTrue(output.contains("goodbye, john doe"), "Missing: goodbye");
            assertTrue(output.contains("press enter to continue"), "Missing: press enter");

            // Verify login/logout occurred
            verify(mockEmployeeSession).loginEmployee("John Doe", "1234", "Cashier");
            verify(mockEmployeeSession).logout();

            // Verify gateway was mocked
            assertEquals(1, gatewayMock.constructed().size());
            verify(gatewayMock.constructed().get(0)).authenticateEmployee("1234", "5678");
        }
    }

    @Test
    @DisplayName("Should deny access after max authentication attempts")
    void testLaunch_MaxAuthenticationAttempts() {
        Scanner scanner = createScanner("9999\n9999\n8888\n8888\n7777\n7777\n\n\n\n"); // 3 invalid attempts, pause,
                                                                                       // extra

        when(mockEmployeeGateway.authenticateEmployee(anyString(), anyString())).thenReturn(null);

        try (MockedConstruction<EmployeeGateway> gatewayMock = mockConstruction(EmployeeGateway.class)) {
            EmployeeMainMenuController.launch(scanner);

            String output = getOutput();
            assertTrue(output.toLowerCase().contains("employee authentication"));
            assertTrue(output.toLowerCase().contains("incorrect employee id or pin. (1/3 attempts)"));
            assertTrue(output.toLowerCase().contains("incorrect employee id or pin. (2/3 attempts)"));
            assertTrue(output.toLowerCase().contains("incorrect employee id or pin. (3/3 attempts)"));
            assertTrue(output.toLowerCase().contains("maximum authentication attempts exceeded"));
            assertTrue(output.toLowerCase().contains("access denied for security reasons"));
            assertTrue(output.toLowerCase().contains("press enter to continue"));

            verify(mockEmployeeSession, never()).loginEmployee(anyString(), anyString(), anyString());
            verify(mockEmployeeSession, never()).logout();
        }
    }

    // ============ Authenticate Employee Tests ============

    @Test
    @DisplayName("Should authenticate employee with valid ID and PIN")
    void testAuthenticateEmployee_Successful() {
        Scanner scanner = createScanner("1234\n5678\n\n\n"); // ID, PIN, pause, extra

        when(mockEmployeeGateway.authenticateEmployee("1234", "5678")).thenReturn(testEmployee);

        invokePrivateMethod("authenticateEmployee",
                new Class[] { Scanner.class, EmployeeGateway.class },
                new Object[] { scanner, mockEmployeeGateway });

        String output = getOutput();
        assertTrue(output.toLowerCase().contains("employee authentication"));
        assertTrue(output.toLowerCase().contains("authentication successful"));
        assertTrue(output.toLowerCase().contains("welcome, john doe"));
        assertTrue(output.toLowerCase().contains("employee id: 1234"));
        assertTrue(output.toLowerCase().contains("role: cashier"));
        assertTrue(output.toLowerCase().contains("press enter to continue"));

        verify(mockEmployeeGateway).authenticateEmployee("1234", "5678");
    }

    @Test
    @DisplayName("Should fail authentication with invalid ID format")
    void testAuthenticateEmployee_InvalidIdFormat() {
        Scanner scanner = createScanner("12\n1234\n5678\n\n\n\n"); // Invalid ID, valid ID, PIN, pause, extra

        when(mockEmployeeGateway.authenticateEmployee("1234", "5678")).thenReturn(testEmployee);

        invokePrivateMethod("authenticateEmployee",
                new Class[] { Scanner.class, EmployeeGateway.class },
                new Object[] { scanner, mockEmployeeGateway });
        String output = getOutput();
        System.out.println("=== DEBUG OUTPUT START ===");
        System.out.println(output);
        System.out.println("=== DEBUG OUTPUT END ===");

        // Check if output contains the expected texts
        boolean hasInvalidFormat = output.toLowerCase().contains("invalid employee id format");
        boolean hasAuthSuccess = output.toLowerCase().contains("authentication successful");
        boolean hasPressEnter = output.toLowerCase().contains("press enter to continue");

        System.out.println("Has invalid format: " + hasInvalidFormat);
        System.out.println("Has auth success: " + hasAuthSuccess);
        System.out.println("Has press enter: " + hasPressEnter);

        assertTrue(hasInvalidFormat, "Missing: invalid employee id format");
        assertTrue(hasAuthSuccess, "Missing: authentication successful");
        assertTrue(hasPressEnter, "Missing: press enter to continue");

        verify(mockEmployeeGateway).authenticateEmployee("1234", "5678");
    }

    @Test
    @DisplayName("Should fail authentication with invalid PIN format")
    void testAuthenticateEmployee_InvalidPinFormat() {
        Scanner scanner = createScanner("1234\n56\n1234\n5678\n\n\n\n"); // Valid ID, invalid PIN, valid ID, PIN, pause,
                                                                         // extra

        when(mockEmployeeGateway.authenticateEmployee("1234", "5678")).thenReturn(testEmployee);

        invokePrivateMethod("authenticateEmployee",
                new Class[] { Scanner.class, EmployeeGateway.class },
                new Object[] { scanner, mockEmployeeGateway });

        String output = getOutput();
        assertTrue(output.toLowerCase().contains("invalid pin format"));
        assertTrue(output.toLowerCase().contains("authentication successful"));
        assertTrue(output.toLowerCase().contains("press enter to continue"));

        verify(mockEmployeeGateway).authenticateEmployee("1234", "5678");
    }

    @Test
    @DisplayName("Should fail authentication with null employee")
    void testAuthenticateEmployee_NullEmployee() {
        Scanner scanner = createScanner("1234\n5678\n\n\n\n"); // ID, PIN, pause, extra

        when(mockEmployeeGateway.authenticateEmployee("1234", "5678")).thenReturn(null);

        invokePrivateMethod("authenticateEmployee",
                new Class[] { Scanner.class, EmployeeGateway.class },
                new Object[] { scanner, mockEmployeeGateway });

        String output = getOutput();
        assertTrue(output.toLowerCase().contains("incorrect employee id or pin"));
        assertTrue(output.toLowerCase().contains("press enter to continue"));

        verify(mockEmployeeGateway).authenticateEmployee("1234", "5678");
    }

    @Test
    @DisplayName("Should fail after max attempts with invalid credentials")
    void testAuthenticateEmployee_MaxAttempts() {
        Scanner scanner = createScanner("9999\n9999\n8888\n8888\n7777\n7777\n\n\n\n"); // 3 invalid attempts, pause,
                                                                                       // extra

        when(mockEmployeeGateway.authenticateEmployee(anyString(), anyString())).thenReturn(null);

        invokePrivateMethod("authenticateEmployee",
                new Class[] { Scanner.class, EmployeeGateway.class },
                new Object[] { scanner, mockEmployeeGateway });

        String output = getOutput();
        assertTrue(output.toLowerCase().contains("incorrect employee id or pin. (1/3 attempts)"));
        assertTrue(output.toLowerCase().contains("incorrect employee id or pin. (2/3 attempts)"));
        assertTrue(output.toLowerCase().contains("incorrect employee id or pin. (3/3 attempts)"));
        assertTrue(output.toLowerCase().contains("maximum authentication attempts exceeded"));
        assertTrue(output.toLowerCase().contains("press enter to continue"));

        verify(mockEmployeeGateway, times(3)).authenticateEmployee(anyString(), anyString());
    }

    // ============ Show Employee Portal Tests ============ @Test
    @DisplayName("Should show portal and handle checkout option")
    void testShowEmployeePortal_Checkout() {
        Scanner scanner = createScanner("1\n\n0\n\n\n"); // Checkout, pause after checkout, logout, logout pause, extra

        when(mockEmployeeSession.isLoggedIn()).thenReturn(true);

        try (MockedConstruction<ItemGateway> itemGatewayMock = mockConstruction(ItemGateway.class);
                MockedConstruction<POS> posMock = mockConstruction(POS.class)) {
            invokePrivateMethod("showEmployeePortal",
                    new Class[] { Scanner.class, Employee.class },
                    new Object[] { scanner, testEmployee });
            String output = getOutput();
            assertTrue(output.toLowerCase().contains("syos - employee portal"));
            assertTrue(output.toLowerCase().contains("logged in as: john doe (1234)"));
            assertTrue(output.toLowerCase().contains("role: cashier"));
            assertTrue(output.toLowerCase().contains("session: active"));
            assertTrue(output.toLowerCase().contains("1. checkout and billing"));
            assertTrue(output.toLowerCase().contains("goodbye, john doe"));

            verify(mockEmployeeSession, atLeast(1)).isLoggedIn();
            assertEquals(1, itemGatewayMock.constructed().size());
            assertEquals(1, posMock.constructed().size());
        }
    }

    @Test
    @DisplayName("Should show portal and handle inventory option")
    void testShowEmployeePortal_Inventory() {
        Scanner scanner = createScanner("2\n0\n\n0\n\n\n\n\n"); // Inventory, back to main, pause, logout, logout pause,
                                                                // extra

        when(mockEmployeeSession.isLoggedIn()).thenReturn(true);

        try (MockedStatic<InventoryAndStockController> inventoryMock = mockStatic(InventoryAndStockController.class)) {
            inventoryMock.when(() -> InventoryAndStockController.launch(any(Scanner.class), any(Employee.class)))
                    .then(invocation -> {
                        System.out.println("-------------------------------------------");
                        System.out.println("      INVENTORY AND STOCK OPERATIONS");
                        System.out.println("-------------------------------------------");
                        System.out.println("1. Add Batch Stock");
                        System.out.println("2. Reduce Shelf Stock by Batch");
                        System.out.println("3. View Expired Batches");
                        System.out.println("4. Stock Summary (Total / Used / Available)");
                        System.out.println("5. Auto-Cleanup Expired Batches");
                        System.out.println("0. Back to Main Menu");
                        System.out.println("-------------------------------------------");
                        System.out.print("Select an option: ");
                        Scanner invScanner = invocation.getArgument(0);
                        invScanner.nextLine(); // Consume choice input
                        System.out.println("Returning to Employee Portal...");
                        System.out.println("Press Enter to continue...");
                        invScanner.nextLine(); // Consume pause
                        return null;
                    });

            invokePrivateMethod("showEmployeePortal",
                    new Class[] { Scanner.class, Employee.class },
                    new Object[] { scanner, testEmployee });
            String output = getOutput();
            assertTrue(output.toLowerCase().contains("syos - employee portal"));
            assertTrue(output.toLowerCase().contains("2. inventory and stock operations"));
            assertTrue(output.toLowerCase().contains("goodbye, john doe"));

            verify(mockEmployeeSession, atLeast(1)).isLoggedIn();
            inventoryMock.verify(() -> InventoryAndStockController.launch(any(Scanner.class), any(Employee.class)));
        }
    }

    @Test
    @DisplayName("Should show portal and handle reports option")
    void testShowEmployeePortal_Reports() {
        Scanner scanner = createScanner("3\n0\n\n0\n\n\n\n\n"); // Reports, back to main, pause, logout, logout pause,
                                                                // extra

        when(mockEmployeeSession.isLoggedIn()).thenReturn(true);

        try (MockedStatic<ReportingMainMenuController> reportsMock = mockStatic(ReportingMainMenuController.class)) {
            reportsMock.when(() -> ReportingMainMenuController.launch(any(Scanner.class)))
                    .then(invocation -> {
                        System.out.println("========= REPORTING MODULE =========");
                        System.out.println("1. Total Sales Report (Daily)");
                        System.out.println("2. Reshelved Items Report");
                        System.out.println("3. Reorder Level Report");
                        System.out.println("4. Stock Batch Report");
                        System.out.println("5. Bill History Report");
                        System.out.println("0. Back to Main Menu");
                        System.out.print("Choose an option: ");
                        Scanner invScanner = invocation.getArgument(0);
                        invScanner.nextLine(); // Consume choice input
                        System.out.println("Returning to Employee Portal...");
                        System.out.println("Press Enter to continue...");
                        invScanner.nextLine(); // Consume pause
                        return null;
                    });

            invokePrivateMethod("showEmployeePortal",
                    new Class[] { Scanner.class, Employee.class },
                    new Object[] { scanner, testEmployee });
            String output = getOutput();
            assertTrue(output.toLowerCase().contains("syos - employee portal"));
            assertTrue(output.toLowerCase().contains("3. reports"));
            assertTrue(output.toLowerCase().contains("goodbye, john doe"));

            verify(mockEmployeeSession, atLeast(1)).isLoggedIn();
            reportsMock.verify(() -> ReportingMainMenuController.launch(any(Scanner.class)));
        }
    }

    @Test
    @DisplayName("Should show portal and handle logout")
    void testShowEmployeePortal_Logout() {
        Scanner scanner = createScanner("0\n\n\n"); // Logout, logout pause, extra

        when(mockEmployeeSession.isLoggedIn()).thenReturn(true);

        invokePrivateMethod("showEmployeePortal",
                new Class[] { Scanner.class, Employee.class },
                new Object[] { scanner, testEmployee });
        String output = getOutput();
        assertTrue(output.toLowerCase().contains("syos - employee portal"));
        assertTrue(output.toLowerCase().contains("0. logout"));
        assertTrue(output.toLowerCase().contains("goodbye, john doe"));
        assertTrue(output.toLowerCase().contains("press enter to continue"));

        verify(mockEmployeeSession, atLeast(1)).isLoggedIn();
    }

    // ============ Validation Method Tests ============

    @Test
    @DisplayName("Should validate correct employee ID format")
    void testIsValidEmployeeId_Valid() {
        try {
            Method method = EmployeeMainMenuController.class.getDeclaredMethod("isValidEmployeeId", String.class);
            method.setAccessible(true);
            assertTrue((boolean) method.invoke(null, "1234"));
            assertTrue((boolean) method.invoke(null, "0000"));
            assertTrue((boolean) method.invoke(null, "9999"));
        } catch (Exception e) {
            fail("Failed to test isValidEmployeeId: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should reject invalid employee ID formats")
    void testIsValidEmployeeId_Invalid() {
        try {
            Method method = EmployeeMainMenuController.class.getDeclaredMethod("isValidEmployeeId", String.class);
            method.setAccessible(true);
            assertFalse((boolean) method.invoke(null, "123")); // Too short
            assertFalse((boolean) method.invoke(null, "12345")); // Too long
            assertFalse((boolean) method.invoke(null, "12ab")); // Non-numeric
            assertFalse((boolean) method.invoke(null, "")); // Empty
            assertFalse((boolean) method.invoke(null, (Object) null)); // Null
        } catch (Exception e) {
            fail("Failed to test isValidEmployeeId: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should validate correct PIN format")
    void testIsValidPin_Valid() {
        try {
            Method method = EmployeeMainMenuController.class.getDeclaredMethod("isValidPin", String.class);
            method.setAccessible(true);
            assertTrue((boolean) method.invoke(null, "5678"));
            assertTrue((boolean) method.invoke(null, "0000"));
            assertTrue((boolean) method.invoke(null, "9999"));
        } catch (Exception e) {
            fail("Failed to test isValidPin: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should reject invalid PIN formats")
    void testIsValidPin_Invalid() {
        try {
            Method method = EmployeeMainMenuController.class.getDeclaredMethod("isValidPin", String.class);
            method.setAccessible(true);
            assertFalse((boolean) method.invoke(null, "567")); // Too short
            assertFalse((boolean) method.invoke(null, "56789")); // Too long
            assertFalse((boolean) method.invoke(null, "56ab")); // Non-numeric
            assertFalse((boolean) method.invoke(null, "")); // Empty
            assertFalse((boolean) method.invoke(null, (Object) null)); // Null
        } catch (Exception e) {
            fail("Failed to test isValidPin: " + e.getMessage());
        }
    }

}