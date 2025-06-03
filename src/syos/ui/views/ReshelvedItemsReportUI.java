package syos.ui.views;

import syos.dto.ReshelvedItemDTO;
import syos.service.ReshelvedItemsReportService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Reshelved Items Report UI with Store Type Selection.
 * Leverages the Template Method in AbstractReportUI.
 * Shows items moved from batch storage to store shelves.
 */
public class ReshelvedItemsReportUI extends AbstractReportUI<ReshelvedItemDTO> {

    private final ReshelvedItemsReportService service = new ReshelvedItemsReportService();
    private LocalDate startDate;
    private LocalDate endDate;
    private StoreFilter storeFilter;
    private DateRangeFilter dateRangeFilter;

    public enum StoreFilter {
        IN_STORE_ONLY, ONLINE_ONLY, COMBINED
    }

    public enum DateRangeFilter {
        TODAY, THIS_WEEK, THIS_MONTH, SPECIFIC_DATE_RANGE
    }

    public ReshelvedItemsReportUI(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected String getReportTitle() {
        return "SYOS RESHELVED ITEMS REPORT";
    }

    @Override
    protected boolean gatherReportCriteria() {
        try {
            // First prompt for store filter
            storeFilter = promptStoreFilter();
            if (storeFilter == null) {
                return false; // User chose to exit
            }

            clearScreen();

            // Then prompt for date range filter
            dateRangeFilter = promptDateRangeFilter();
            if (dateRangeFilter == null) {
                return false; // User chose to exit
            }

            clearScreen();

            // Set dates based on filter selection
            if (!setDatesFromFilter()) {
                return false; // User cancelled during specific date input
            }

            clearScreen();
            return true;
        } catch (Exception e) {
            System.out.println("Error gathering criteria: " + e.getMessage());
            return false;
        }
    }

    /**
     * Prompts user to select date range filter.
     * Returns null if user wants to go back.
     */
    private DateRangeFilter promptDateRangeFilter() {
        while (true) {
            System.out.println("Select Date Range:");
            System.out.println("  1. Today");
            System.out.println("  2. This Week");
            System.out.println("  3. This Month");
            System.out.println("  4. Specific Date Range");
            System.out.println("  0. Back to Reports Menu");
            System.out.print("Choice (0-4): ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    return DateRangeFilter.TODAY;
                case "2":
                    return DateRangeFilter.THIS_WEEK;
                case "3":
                    return DateRangeFilter.THIS_MONTH;
                case "4":
                    return DateRangeFilter.SPECIFIC_DATE_RANGE;
                case "0":
                case "":
                    return null; // Return null to indicate user wants to go back
                default:
                    System.out.println("[Invalid] Enter 0-4.");
            }
        }
    }

    /**
     * Sets start and end dates based on the selected filter.
     * Returns false if user cancels during specific date input.
     */
    private boolean setDatesFromFilter() {
        LocalDate today = LocalDate.now();

        switch (dateRangeFilter) {
            case TODAY:
                startDate = today;
                endDate = today;
                break;

            case THIS_WEEK:
                // Start from Monday of current week
                startDate = today.minusDays(today.getDayOfWeek().getValue() - 1);
                endDate = today;
                break;

            case THIS_MONTH:
                // Start from first day of current month
                startDate = today.withDayOfMonth(1);
                endDate = today;
                break;

            case SPECIFIC_DATE_RANGE:
                System.out.println("Enter Start Date:");
                startDate = promptDate();
                if (startDate == null) {
                    return false; // User cancelled
                }

                System.out.println("Enter End Date:");
                endDate = promptDate();
                if (endDate == null) {
                    return false; // User cancelled
                }

                // Validate date range
                if (endDate.isBefore(startDate)) {
                    System.out.println("End date cannot be before start date!");
                    return false;
                }
                break;
        }

        return true;
    }

    /**
     * Prompt for a valid date (yyyy-MM-dd) with option to go back
     * Returns null if user wants to exit
     */
    private LocalDate promptDate() {
        while (true) {
            System.out.print("Enter date (yyyy-MM-dd) or press Enter to go back: ");
            String input = scanner.nextLine().trim();

            // Allow user to go back by pressing Enter
            if (input.isEmpty()) {
                return null;
            }

            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("[Invalid] Use format yyyy-MM-dd (e.g., 2025-06-03).");
            }
        }
    }

    /**
     * Prompts user to select store type filter.
     * Returns null if user wants to go back.
     */
    private StoreFilter promptStoreFilter() {
        while (true) {
            System.out.println("Select Store Type:");
            System.out.println("  1. In-Store Reshelving Only");
            System.out.println("  2. Online Reshelving Only");
            System.out.println("  3. Combined (Both In-Store and Online)");
            System.out.println("  0. Back to Reports Menu");
            System.out.print("Choice (0-3): ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    return StoreFilter.IN_STORE_ONLY;
                case "2":
                    return StoreFilter.ONLINE_ONLY;
                case "3":
                    return StoreFilter.COMBINED;
                case "0":
                case "":
                    return null; // Return null to indicate user wants to go back
                default:
                    System.out.println("[Invalid] Enter 0-3.");
            }
        }
    }

    @Override
    protected List<ReshelvedItemDTO> fetchReportItems() {
        return switch (storeFilter) {
            case IN_STORE_ONLY -> service.getReshelvedItemsForInStoreRange(startDate, endDate);
            case ONLINE_ONLY -> service.getReshelvedItemsForOnlineRange(startDate, endDate);
            case COMBINED -> service.getAllReshelvedItemsRange(startDate, endDate);
        };
    }

    @Override
    protected void renderReport(List<ReshelvedItemDTO> items) {
        int total = 0;
        System.out.println("==============================================");
        System.out.println("      SYOS DAILY RESHELVED ITEMS REPORT      ");
        System.out.println("        (Items Moved from Batch to Shelf)    ");
        System.out.println("==============================================");
        System.out.println("Date Range: " + formatDateRange());
        System.out.println("Store Type: " + getStoreTypeDescription());
        System.out.println("----------------------------------------------");

        // Adjust column widths based on whether it's combined report
        if (storeFilter == StoreFilter.COMBINED) {
            System.out.printf("%-10s %-40s %8s%n", "Code", "Name", "Quantity");
        } else {
            System.out.printf("%-10s %-30s %8s%n", "Code", "Name", "Quantity");
        }
        System.out.println("----------------------------------------------");

        if (items.isEmpty()) {
            System.out.println("No reshelving activities found for the selected criteria.");
        } else {
            for (ReshelvedItemDTO dto : items) {
                if (storeFilter == StoreFilter.COMBINED) {
                    System.out.printf("%-10s %-40s %8d%n",
                            dto.getCode(),
                            truncateName(dto.getName(), 40),
                            dto.getQuantity());
                } else {
                    System.out.printf("%-10s %-30s %8d%n",
                            dto.getCode(),
                            truncateName(dto.getName(), 30),
                            dto.getQuantity());
                }
                total += dto.getQuantity();
            }
        }

        System.out.println("----------------------------------------------");
        System.out.printf("Total Items Reshelved from Batches: %d%n", total);
        System.out.println("==============================================");
    }

    /**
     * Formats the date range for display.
     */
    private String formatDateRange() {
        if (startDate.equals(endDate)) {
            return startDate.toString();
        } else {
            return startDate + " to " + endDate;
        }
    }

    /**
     * Gets description of selected store type.
     * KISS principle: Simple string mapping.
     */
    private String getStoreTypeDescription() {
        return switch (storeFilter) {
            case IN_STORE_ONLY -> "In-Store Reshelving Only";
            case ONLINE_ONLY -> "Online Reshelving Only";
            case COMBINED -> "Combined (In-Store + Online)";
        };
    }

    /**
     * Truncates item names for better table formatting.
     * YAGNI principle: Simple truncation without complex formatting.
     */
    private String truncateName(String name, int maxLength) {
        if (name.length() <= maxLength) {
            return name;
        }
        return name.substring(0, maxLength - 3) + "...";
    }
}
