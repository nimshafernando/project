package syos.ui.controllers;

import syos.interfaces.IReportService;
import syos.service.*;
import syos.ui.views.BillHistoryReportUI;
import syos.ui.views.ReorderLevelReportUI;
import syos.ui.views.ReshelvedItemsReportUI;
import syos.ui.views.StockBatchReportUI;
import syos.ui.views.TotalSalesReportUI;
import syos.util.ConsoleUtils;

import java.util.List;
import java.util.Scanner;

/**
 * ReportingMainMenuController with integrated Factory pattern
 * Centralizes report service creation and UI management
 * Implements SOLID principles: SRP (reporting menu management),
 * OCP (extensible report types), DIP (depends on abstractions)
 */
public class ReportingMainMenuController {

    /**
     * Enum for report types - used by Factory pattern
     * Provides type safety and extensibility
     */
    public enum ReportType {
        TOTAL_SALES("Total Sales Report (Daily)", "1"),
        RESHELVED_ITEMS("Reshelved Items Report", "2"),
        REORDER_LEVEL("Reorder Level Report", "3"),
        STOCK_BATCH("Stock Batch Report", "4"),
        BILL_HISTORY("Bill History Report", "5");

        private final String displayName;
        private final String menuOption;

        ReportType(String displayName, String menuOption) {
            this.displayName = displayName;
            this.menuOption = menuOption;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getMenuOption() {
            return menuOption;
        }

        /**
         * Get ReportType by menu choice
         */
        public static ReportType fromChoice(String choice) {
            for (ReportType type : values()) {
                if (type.menuOption.equals(choice)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * Factory pattern implementation for report services
     * Centralizes object creation and provides abstraction
     */
    public static class ReportServiceFactory {

        /**
         * Create report service based on type
         * 
         * @param type Report type to create
         * @return IReportService implementation
         */
        public static IReportService<?> createReportService(ReportType type) {
            return switch (type) {
                case TOTAL_SALES -> new TotalSalesReportService();
                case RESHELVED_ITEMS -> new ReshelvedItemsReportService();
                case REORDER_LEVEL -> new ReorderLevelReportService();
                case STOCK_BATCH -> new StockBatchReportService();
                case BILL_HISTORY -> new BillHistoryReportService();
            };
        }

        /**
         * Create report service by string identifier
         * Useful for configuration-driven report creation
         */
        public static IReportService<?> createReportService(String serviceType) {
            try {
                ReportType type = ReportType.valueOf(serviceType.toUpperCase());
                return createReportService(type);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown report service type: " + serviceType);
            }
        }

        /**
         * Get available report types
         */
        public static ReportType[] getAvailableReports() {
            return ReportType.values();
        }

        /**
         * Check if report type is available
         */
        public static boolean isReportTypeAvailable(String type) {
            try {
                ReportType.valueOf(type.toUpperCase());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    /**
     * Factory pattern for UI creation
     * Centralizes UI object creation based on report type
     */
    public static class ReportUIFactory {

        /**
         * Create appropriate UI for report type
         */
        public static void displayReport(ReportType type, Scanner scanner) {
            switch (type) {
                case TOTAL_SALES -> new TotalSalesReportUI(scanner).display();
                case RESHELVED_ITEMS -> new ReshelvedItemsReportUI(scanner).display();
                case REORDER_LEVEL -> new ReorderLevelReportUI(scanner).display();
                case STOCK_BATCH -> new StockBatchReportUI(scanner).display();
                case BILL_HISTORY -> new BillHistoryReportUI(scanner).display();
                default -> throw new IllegalArgumentException("Unknown report type: " + type);
            }
        }

        /**
         * Get report service and display generic report
         * Alternative approach using service abstraction
         */
        public static void displayReportGeneric(ReportType type, Scanner scanner) {
            try {
                IReportService<?> service = ReportServiceFactory.createReportService(type);

                if (service.isDataAvailable()) {
                    System.out.println("=== " + service.getReportTitle() + " ===");

                    List<String> headers = service.getColumnHeaders();
                    List<List<String>> data = service.getReportData();

                    // Display headers
                    for (String header : headers) {
                        System.out.printf("%-15s", header);
                    }
                    System.out.println();

                    // Display data
                    for (List<String> row : data) {
                        for (String cell : row) {
                            System.out.printf("%-15s", cell);
                        }
                        System.out.println();
                    }
                } else {
                    System.out.println("No data available for " + type.getDisplayName());
                }

                System.out.print("\nPress Enter to continue...");
                scanner.nextLine(); // Fixed: use scanner.nextLine() instead

            } catch (Exception e) {
                System.err.println("Error generating report: " + e.getMessage());
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine(); // Fixed: use scanner.nextLine() instead
            }
        }
    }

    /**
     * Launch the reporting menu with Factory pattern integration
     */
    public static void launch(Scanner scanner) {
        while (true) {
            ConsoleUtils.clearScreen();
            displayMenu();

            String choice = scanner.nextLine().trim();

            if ("0".equals(choice)) {
                return; // Back to main menu
            }

            // Use Factory pattern to handle report selection
            ReportType reportType = ReportType.fromChoice(choice);

            if (reportType != null) {
                try {
                    // Use Factory to display appropriate report
                    ReportUIFactory.displayReport(reportType, scanner);
                } catch (Exception e) {
                    System.err.println("Error displaying report: " + e.getMessage());
                    System.out.print("Press Enter to continue...");
                    scanner.nextLine(); // Fixed: use scanner.nextLine() instead
                }
            } else {
                System.out.println("[Invalid] Enter 0-5.");
                System.out.print("Press Enter to continue...");
                scanner.nextLine(); // Fixed: use scanner.nextLine() instead
            }
        }
    }

    /**
     * Display menu using Factory pattern for dynamic menu generation
     */
    private static void displayMenu() {
        System.out.println("========= REPORTING MODULE =========");

        // Dynamic menu generation using Factory pattern
        for (ReportType type : ReportServiceFactory.getAvailableReports()) {
            System.out.println(type.getMenuOption() + ". " + type.getDisplayName());
        }

        System.out.println("0. Back to Main Menu");
        System.out.print("Choose an option: ");
    }

    /**
     * Alternative launch method with configuration-driven reports
     * Demonstrates extensibility of Factory pattern
     */
    public static void launchConfigurable(Scanner scanner, String[] enabledReports) {
        while (true) {
            ConsoleUtils.clearScreen();
            System.out.println("========= REPORTING MODULE (Configurable) =========");

            int optionNumber = 1;
            ReportType[] availableTypes = new ReportType[enabledReports.length];

            // Display only enabled reports
            for (int i = 0; i < enabledReports.length; i++) {
                if (ReportServiceFactory.isReportTypeAvailable(enabledReports[i])) {
                    ReportType type = ReportType.valueOf(enabledReports[i].toUpperCase());
                    availableTypes[optionNumber - 1] = type;
                    System.out.println(optionNumber + ". " + type.getDisplayName());
                    optionNumber++;
                }
            }

            System.out.println("0. Back to Main Menu");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            if ("0".equals(choice)) {
                return;
            }

            try {
                int index = Integer.parseInt(choice) - 1;
                if (index >= 0 && index < availableTypes.length && availableTypes[index] != null) {
                    ReportUIFactory.displayReport(availableTypes[index], scanner);
                } else {
                    System.out.println("[Invalid] Please enter a valid option.");
                    scanner.nextLine();
                }
            } catch (NumberFormatException e) {
                System.out.println("[Invalid] Please enter a number.");
                scanner.nextLine();
            }
        }
    }
}
