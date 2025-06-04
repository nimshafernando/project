package syos.ui;

import syos.ui.controllers.EmployeeMainMenuController;
import syos.util.ConsoleUtils;
import syos.ui.controllers.CustomerMainMenuController;

import java.util.Scanner;

/**
 * Main entry point - simplified to user type selection only
 * Following Single Responsibility Principle
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Silent system initialization
            initializeSystemSilently();

            while (true) {
                ConsoleUtils.clearScreen();
                System.out.println("===========================================");
                System.out.println("       Welcome to Synex Outlet Store");
                System.out.println("===========================================");
                System.out.println("Please select user type:");
                System.out.println("1. Employee");
                System.out.println("2. Customer");
                System.out.println("0. Exit");
                System.out.println("===========================================");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> EmployeeMainMenuController.launch(scanner);
                    case "2" -> CustomerMainMenuController.launch(scanner);
                    case "0" -> {
                        System.out.println("Goodbye! Hope to see you again soon.");
                        return;
                    }
                    default -> {
                        System.out.println("Invalid option. Please try again.");
                        ConsoleUtils.pause(scanner);
                    }
                }
            }
        } finally {
            scanner.close();
        }
    }

    private static void initializeSystemSilently() {
        // Silent initialization - no output to console
        try {
            syos.data.ItemGateway itemGateway = new syos.data.ItemGateway();
            syos.data.BatchGateway batchGateway = new syos.data.BatchGateway();
            syos.service.BatchService batchService = new syos.service.BatchService(batchGateway, itemGateway);

            // Clean up expired batches silently
            batchService.autoCleanupExpiredBatches();
        } catch (Exception e) {
            // Silent error handling - log to file if needed but don't display
        }
    }
}
