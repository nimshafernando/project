package syos.ui.views;

import syos.dto.ReorderItemDTO;
import syos.service.ReorderLevelReportService;

import java.util.List;
import java.util.Scanner;

/**
 * Reorder Level Report UI.
 * Extends AbstractReportUI following Template Method pattern.
 * Shows items with stock below custom threshold value.
 */
public class ReorderLevelReportUI extends AbstractReportUI<ReorderItemDTO> {

    private StoreFilter storeFilter;
    private int thresholdValue;

    public enum StoreFilter {
        IN_STORE_ONLY, ONLINE_ONLY, BOTH_STORES
    }

    public ReorderLevelReportUI(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected String getReportTitle() {
        return "SYOS REORDER LEVEL REPORT";
    }

    @Override
    protected boolean gatherReportCriteria() {
        try {
            // First get store filter
            storeFilter = promptStoreFilter();
            if (storeFilter == null) {
                return false; // User chose to exit
            }

            // Then get threshold value
            thresholdValue = promptThresholdValue();
            if (thresholdValue == -1) {
                return false; // User chose to exit
            }

            clearScreen();
            return true;
        } catch (Exception e) {
            System.out.println("Error gathering criteria: " + e.getMessage());
            return false;
        }
    }

    /**
     * Prompts user to select which store inventory to check.
     * Returns null if user wants to exit
     */
    private StoreFilter promptStoreFilter() {
        while (true) {
            System.out.println("Select Store Type for Reorder Analysis:");
            System.out.println("  1. In-Store Inventory Only");
            System.out.println("  2. Online Inventory Only");
            System.out.println("  3. Both Store Inventories");
            System.out.println("  0. Back to reports menu");
            System.out.print("Choice (0-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    return StoreFilter.IN_STORE_ONLY;
                case "2":
                    return StoreFilter.ONLINE_ONLY;
                case "3":
                    return StoreFilter.BOTH_STORES;
                case "0":
                case "":
                    return null; // User wants to go back
                default:
                    System.out.println("[Invalid] Enter 0-3.");
            }
        }
    }

    /**
     * Prompts user to enter threshold value for reorder level.
     * Returns -1 if user wants to exit
     */
    private int promptThresholdValue() {
        while (true) {
            System.out.println("\nReorder Threshold Configuration:");
            System.out.println("Items with stock at or below this threshold will be shown.");
            System.out.println("(Default threshold is 50 units)");
            System.out.print("Enter threshold value (1-999) or 0 to go back: ");

            String input = scanner.nextLine().trim();

            // Allow user to go back
            if (input.equals("0") || input.isEmpty()) {
                return -1;
            }

            try {
                int threshold = Integer.parseInt(input);
                if (threshold >= 1 && threshold <= 999) {
                    return threshold;
                } else {
                    System.out.println("[Invalid] Threshold must be between 1 and 999.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[Invalid] Please enter a valid number.");
            }
        }
    }

    @Override
    protected List<ReorderItemDTO> fetchReportItems() {
        ReorderLevelReportService service = new ReorderLevelReportService();
        return switch (storeFilter) {
            case IN_STORE_ONLY -> service.getInStoreReorderItems(thresholdValue);
            case ONLINE_ONLY -> service.getOnlineReorderItems(thresholdValue);
            case BOTH_STORES -> service.getItemsBelowReorderLevel(thresholdValue);
        };
    }

    @Override
    protected void renderReport(List<ReorderItemDTO> items) {
        double totalValue = items.stream()
                .mapToDouble(item -> item.getShortfall() * item.getPrice())
                .sum();
        int totalShortfall = items.stream()
                .mapToInt(ReorderItemDTO::getShortfall)
                .sum();

        System.out.println("==========================================================");
        System.out.println("                SYOS REORDER LEVEL REPORT                ");
        System.out.println("==========================================================");
        System.out.println("Store Type: " + getStoreTypeDescription());
        System.out.println("Threshold : Items with stock ≤ " + thresholdValue + " units");
        System.out.println("----------------------------------------------------------");

        // Updated column headers (removed "Reorder" column)
        System.out.printf("%-10s %-25s %-8s %-8s %-10s %-8s%n",
                "Code", "Name", "Current", "Shortfall", "Price", "Store");
        System.out.println("----------------------------------------------------------");

        for (ReorderItemDTO item : items) {
            // Updated row format (removed reorder level column)
            System.out.printf("%-10s %-25s %-8d %-8d %-10.2f %-8s%n",
                    item.getCode(),
                    truncateName(item.getName(), 25),
                    item.getCurrentStock(),
                    item.getShortfall(),
                    item.getPrice(),
                    item.getStoreType());
        }

        System.out.println("----------------------------------------------------------");
        System.out.printf("Total Items Requiring Reorder: %d%n", items.size());
        System.out.printf("Total Units Shortfall: %d%n", totalShortfall);
        System.out.printf("Total Estimated Reorder Value: Rs. %.2f%n", totalValue);
        System.out.println("==========================================================");
    }

    /**
     * Gets description of selected store type.
     * KISS principle: Simple string mapping.
     */
    private String getStoreTypeDescription() {
        return switch (storeFilter) {
            case IN_STORE_ONLY -> "In-Store Inventory Only";
            case ONLINE_ONLY -> "Online Inventory Only";
            case BOTH_STORES -> "Both In-Store and Online";
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

    @Override
    protected void showNoDataMessage() {
        System.out.println("==========================================================");
        System.out.println("                SYOS REORDER LEVEL REPORT                ");
        System.out.println("==========================================================");
        System.out.println("Store Type: " + getStoreTypeDescription());
        System.out.println("Threshold : Items with stock ≤ " + thresholdValue + " units");
        System.out.println("");
        System.out.println("✓ Great news! All items are sufficiently stocked.");
        System.out.println("  No items are below the threshold of " + thresholdValue + " units.");
        System.out.println("==========================================================");
        waitForEnter();
    }
}
