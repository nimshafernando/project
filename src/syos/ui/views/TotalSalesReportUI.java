package syos.ui.views;

import syos.dto.ReportItemDTO;
import syos.service.SalesReportService;
import syos.service.SalesReportService.StoreType;
import syos.service.SalesReportService.TransactionType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Scanner;

/**
 * Concrete Report UI for "Total Sales (Daily)".
 * Extends AbstractReportUI with report-specific criteria and rendering.
 */
public class TotalSalesReportUI extends AbstractReportUI<ReportItemDTO> {

    private final SalesReportService service = new SalesReportService();
    private LocalDate startDate;
    private LocalDate endDate;
    private StoreType storeType;
    private TransactionType txnType;

    public TotalSalesReportUI(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected boolean gatherReportCriteria() {
        // Allow user to exit at date prompt
        if (!promptDateRange()) {
            return false; // User chose to exit
        }

        clearScreen();
        storeType = promptStoreType();
        if (storeType == null) {
            return false; // User chose to exit
        }

        clearScreen();
        txnType = promptTransactionType(storeType);
        if (txnType == null) {
            return false; // User chose to exit
        }

        clearScreen();
        return true;
    }

    @Override
    protected String getReportTitle() {
        return "SYOS DAILY SALES REPORT";
    }

    /**
     * Prompts for date range with predefined options
     * Returns false if user wants to exit
     */
    private boolean promptDateRange() {
        while (true) {
            System.out.println("Select Date Range:");
            System.out.println("  1. Today");
            System.out.println("  2. This Week");
            System.out.println("  3. This Month");
            System.out.println("  4. Specific Date Range");
            System.out.println("  0. Back to previous menu");
            System.out.print("Choice (0-4): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    startDate = LocalDate.now();
                    endDate = LocalDate.now();
                    return true;
                case "2":
                    startDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                    endDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
                    return true;
                case "3":
                    startDate = LocalDate.now().withDayOfMonth(1);
                    endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
                    return true;
                case "4":
                    return promptSpecificDateRange();
                case "0":
                case "":
                    return false; // User wants to go back
                default:
                    System.out.println("[Invalid] Enter 0-4.");
            }
        }
    }

    /**
     * Prompts for specific date range
     * Returns false if user wants to go back
     */
    private boolean promptSpecificDateRange() {
        while (true) {
            System.out.print("Enter start date (yyyy-mm-dd) or press Enter to go back: ");
            String startInput = scanner.nextLine().trim();

            if (startInput.isEmpty()) {
                return false; // User wants to go back
            }

            try {
                startDate = LocalDate.parse(startInput);
            } catch (DateTimeParseException e) {
                System.out.println("[Invalid] Use format yyyy-mm-dd (e.g., 2024-01-15).");
                continue;
            }

            System.out.print("Enter end date (yyyy-mm-dd) or press Enter to go back: ");
            String endInput = scanner.nextLine().trim();

            if (endInput.isEmpty()) {
                return false; // User wants to go back
            }

            try {
                endDate = LocalDate.parse(endInput);
                if (endDate.isBefore(startDate)) {
                    System.out.println("[Invalid] End date cannot be before start date.");
                    continue;
                }
                return true;
            } catch (DateTimeParseException e) {
                System.out.println("[Invalid] Use format yyyy-mm-dd (e.g., 2024-01-15).");
            }
        }
    }

    /**
     * Prompts for store type with option to go back
     * Returns null if user wants to exit
     */
    private StoreType promptStoreType() {
        while (true) {
            System.out.println("Select Store Type:");
            System.out.println("  1. In-Store");
            System.out.println("  2. Online");
            System.out.println("  3. Combined");
            System.out.println("  0. Back to previous menu");
            System.out.print("Choice (0-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    return StoreType.IN_STORE;
                case "2":
                    return StoreType.ONLINE;
                case "3":
                    return StoreType.COMBINED;
                case "0":
                case "":
                    return null; // User wants to go back
                default:
                    System.out.println("[Invalid] Enter 0-3.");
            }
        }
    }

    /**
     * Prompts for transaction type with option to go back
     * Returns null if user wants to exit
     */
    private TransactionType promptTransactionType(StoreType st) {
        if (st == StoreType.IN_STORE) {
            System.out.println("Transaction Type: In-Store (cash only)");
            return TransactionType.IN_STORE;
        }

        while (true) {
            if (st == StoreType.ONLINE) {
                System.out.println("Select Transaction Type:");
                System.out.println("  1. Pay in Store");
                System.out.println("  2. Cash on Delivery");
                System.out.println("  3. All Online Orders");
                System.out.println("  0. Back to previous menu");
                System.out.print("Choice (0-3): ");

                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        return TransactionType.RESERVATION_PAY_IN_STORE;
                    case "2":
                        return TransactionType.RESERVATION_COD;
                    case "3":
                        return TransactionType.ALL;
                    case "0":
                    case "":
                        return null; // User wants to go back
                    default:
                        System.out.println("[Invalid] Enter 0-3.");
                }
            } else { // COMBINED
                System.out.println("Select Transaction Type:");
                System.out.println("  1. Cash");
                System.out.println("  2. Pay in Store");
                System.out.println("  3. Cash on Delivery");
                System.out.println("  4. All Transactions");
                System.out.println("  0. Back to previous menu");
                System.out.print("Choice (0-4): ");

                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        return TransactionType.IN_STORE;
                    case "2":
                        return TransactionType.RESERVATION_PAY_IN_STORE;
                    case "3":
                        return TransactionType.RESERVATION_COD;
                    case "4":
                        return TransactionType.ALL;
                    case "0":
                    case "":
                        return null; // User wants to go back
                    default:
                        System.out.println("[Invalid] Enter 0-4.");
                }
            }
        }
    }

    @Override
    protected List<ReportItemDTO> fetchReportItems() {
        // Note: You'll need to update your SalesReportService to accept date range
        // For now, using startDate as the single date parameter
        return service.getSalesReport(startDate, storeType, txnType);
    }

    @Override
    protected void renderReport(List<ReportItemDTO> items) {
        int totalQty = 0;
        double totalRev = 0;

        System.out.println("=======================================================");
        System.out.println("                 SYOS DAILY SALES REPORT               ");
        System.out.println("=======================================================");

        if (startDate.equals(endDate)) {
            System.out.println("Date       : " + startDate);
        } else {
            System.out.println("Date Range : " + startDate + " to " + endDate);
        }

        System.out.println("Store Type : " + storeType);
        System.out.println("Txn Type   : " + txnType);
        System.out.println("-------------------------------------------------------");

        // Adjust column headers based on store type
        if (storeType == StoreType.COMBINED) {
            System.out.printf("%-10s %-25s %-10s %7s %12s%n", "Code", "Name", "Store", "Qty", "Revenue");
        } else {
            System.out.printf("%-10s %-30s %7s %12s%n", "Code", "Name", "Qty", "Revenue");
        }
        System.out.println("-------------------------------------------------------");

        for (ReportItemDTO dto : items) {
            totalQty += dto.getQuantity();
            totalRev += dto.getRevenue();

            if (storeType == StoreType.COMBINED) {
                // Extract store type from item name if it contains store info
                String storeName = extractStoreType(dto.getName());
                String itemName = cleanItemName(dto.getName());
                System.out.printf("%-10s %-25s %-10s %7d %12.2f%n",
                        dto.getCode(),
                        truncateName(itemName, 25),
                        storeName,
                        dto.getQuantity(),
                        dto.getRevenue());
            } else {
                System.out.printf("%-10s %-30s %7d %12.2f%n",
                        dto.getCode(), dto.getName(), dto.getQuantity(), dto.getRevenue());
            }
        }

        System.out.println("-------------------------------------------------------");
        System.out.printf("Total Quantity: %-5d | Total Revenue: Rs. %.2f%n", totalQty, totalRev);
        System.out.println("=======================================================");
    }

    /**
     * Extracts store type from item name for combined reports
     */
    private String extractStoreType(String itemName) {
        if (itemName.contains("(In-Store)")) {
            return "In-Store";
        } else if (itemName.contains("(Online)")) {
            return "Online";
        }
        return "Unknown";
    }

    /**
     * Removes store type suffix from item name
     */
    private String cleanItemName(String itemName) {
        if (itemName.contains("(In-Store)")) {
            return itemName.replace(" (In-Store)", "").trim();
        } else if (itemName.contains("(Online)")) {
            return itemName.replace(" (Online)", "").trim();
        }
        return itemName;
    }

    /**
     * Truncates item names for better table formatting
     */
    private String truncateName(String name, int maxLength) {
        if (name.length() <= maxLength) {
            return name;
        }
        return name.substring(0, maxLength - 3) + "...";
    }
}
