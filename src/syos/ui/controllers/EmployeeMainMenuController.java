package syos.ui.controllers;

import syos.data.EmployeeGateway;
import syos.data.ItemGateway;
import syos.model.Employee;
import syos.service.POS;
import syos.util.ConsoleUtils;
import syos.util.EmployeeSession; // Add this import
import java.util.Scanner;

/**
 * Employee Main Menu Controller
 * Handles employee authentication and main menu navigation
 * Implements SOLID principles: SRP (employee menu management),
 * OCP (extensible menu options), DIP (depends on abstractions)
 */
public class EmployeeMainMenuController {

    public static void launch(Scanner scanner) {
        EmployeeGateway employeeGateway = new EmployeeGateway();

        while (true) {
            Employee authenticatedEmployee = authenticateEmployee(scanner, employeeGateway);

            if (authenticatedEmployee != null && authenticatedEmployee.isActive()) {
                // Start employee session after successful authentication
                EmployeeSession.getInstance().loginEmployee(
                        authenticatedEmployee.getDisplayName(),
                        authenticatedEmployee.getEmployeeId(),
                        authenticatedEmployee.getRole());

                showEmployeePortal(scanner, authenticatedEmployee);

                // Logout from session when employee logs out
                EmployeeSession.getInstance().logout();
            } else {
                System.out.println("Access denied. Returning to main menu...");
                ConsoleUtils.pause(scanner);
                return; // Return to main menu
            }
        }
    }

    /**
     * Employee authentication method
     */
    private static Employee authenticateEmployee(Scanner scanner, EmployeeGateway employeeGateway) {
        ConsoleUtils.clearScreen();
        System.out.println("===========================================");
        System.out.println("       EMPLOYEE AUTHENTICATION");
        System.out.println("===========================================");

        int attempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Enter Employee ID: ");
            String employeeId = scanner.nextLine().trim();

            if (!isValidEmployeeId(employeeId)) {
                System.out.println("Invalid Employee ID format. Please enter 4 digits.");
                attempts++;
                continue;
            }

            System.out.print("Enter PIN: ");
            String pin = scanner.nextLine().trim();

            if (!isValidPin(pin)) {
                System.out.println("Invalid PIN format. Please enter 4 digits.");
                attempts++;
                continue;
            }

            Employee employee = employeeGateway.authenticateEmployee(employeeId, pin);

            if (employee != null && employee.isActive()) {
                ConsoleUtils.clearScreen();
                System.out.println("===========================================");
                System.out.println("       AUTHENTICATION SUCCESSFUL");
                System.out.println("===========================================");
                System.out.printf("Welcome, %s!\n", employee.getDisplayName());
                System.out.printf("Employee ID: %s\n", employee.getEmployeeId());
                System.out.printf("Role: %s\n", employee.getRole());
                System.out.println("===========================================");
                ConsoleUtils.pause(scanner);
                return employee;
            } else {
                attempts++;
                System.out.printf("Incorrect Employee ID or PIN. (%d/%d attempts)\n",
                        attempts, MAX_ATTEMPTS);
                if (attempts < MAX_ATTEMPTS) {
                    System.out.println("Please try again.\n");
                }
            }
        }

        System.out.println("Maximum authentication attempts exceeded.");
        System.out.println("Access denied for security reasons.");
        ConsoleUtils.pause(scanner);
        return null;
    }

    /**
     * Employee portal with authenticated employee context
     */
    private static void showEmployeePortal(Scanner scanner, Employee employee) {
        while (true) {
            ConsoleUtils.clearScreen();
            System.out.println("===========================================");
            System.out.println("        SYOS - EMPLOYEE PORTAL");
            System.out.println("===========================================");
            System.out.printf("Logged in as: %s (%s)\n",
                    employee.getDisplayName(), employee.getEmployeeId());
            System.out.printf("Role: %s  | Session: %s\n",
                    employee.getRole(),
                    EmployeeSession.getInstance().isLoggedIn() ? "Active" : "Inactive");
            System.out.println("===========================================");
            System.out.println("1. Checkout and Billing");
            System.out.println("2. Inventory and Stock Operations");
            System.out.println("3. Reports");
            System.out.println("0. Logout");
            System.out.println("===========================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    ItemGateway itemGateway = new ItemGateway();
                    POS pos = new POS();
                    CheckoutAndBillingController.launch(scanner, itemGateway, pos, employee);
                }
                case "2" -> InventoryAndStockController.launch(scanner, employee);
                case "3" -> ReportingMainMenuController.launch(scanner);
                case "0" -> {
                    System.out.printf("Goodbye, %s! Logging out...\n", employee.getDisplayName());
                    ConsoleUtils.pause(scanner);
                    return;
                }
                default -> {
                    System.out.println("Invalid option. Please try again.");
                    ConsoleUtils.pause(scanner);
                }
            }
        }
    }

    /**
     * Validate Employee ID format (4 digits)
     */
    private static boolean isValidEmployeeId(String employeeId) {
        return employeeId != null &&
                employeeId.matches("\\d{4}") &&
                employeeId.length() == 4;
    }

    /**
     * Validate PIN format (4 digits)
     */
    private static boolean isValidPin(String pin) {
        return pin != null &&
                pin.matches("\\d{4}") &&
                pin.length() == 4;
    }
}
