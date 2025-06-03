package syos.ui.views;

import syos.dto.StockBatchDTO;
import syos.service.StockBatchReportService;
import syos.service.StockBatchReportService.BatchFilter;
import syos.service.StockBatchReportService.BatchSummary;

import java.util.List;
import java.util.Scanner;

/**
 * Stock Batch Report UI.
 * Extends AbstractReportUI following Template Method pattern.
 * Shows batch-wise stock details with status and expiry information.
 */
public class StockBatchReportUI extends AbstractReportUI<StockBatchDTO> {

    private final StockBatchReportService service = new StockBatchReportService();
    private BatchFilter batchFilter;
    private String specificItemCode;
    private boolean showSummary;

    public StockBatchReportUI(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected boolean gatherReportCriteria() {
        try {
            showReportOptions();
            batchFilter = promptBatchFilter();

            // Check if user chose to go back
            if (batchFilter == null) {
                return false; // User chose to go back to reports menu
            }

            clearScreen();

            if (batchFilter != BatchFilter.ALL) {
                specificItemCode = promptSpecificItem();
                clearScreen();
            }

            showSummary = true; // Always show summary
            return true;
        } catch (Exception e) {
            System.out.println("Error gathering criteria: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected String getReportTitle() {
        return "Stock Batch Report";
    }

    /**
     * Shows available report options to the user.
     * KISS principle: Clear, simple information display.
     */
    private void showReportOptions() {
        System.out.println("=== STOCK BATCH REPORT OPTIONS ===");

    }

    /**
     * Prompts user to select batch filter criteria.
     * Strategy pattern: Different filtering strategies.
     */
    private BatchFilter promptBatchFilter() {
        while (true) {
            System.out.println("Select Batch Filter:");
            System.out.println("  1. All Batches");
            System.out.println("  2. Active Batches Only");
            System.out.println("  3. Expiring Soon (≤ 30 days)");
            System.out.println("  4. Expired Batches");
            System.out.println("  5. Depleted Batches");
            System.out.println("  0. Back to Reports Menu"); // Added this line
            System.out.print("Choice (0-5): ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    return BatchFilter.ALL;
                case "2":
                    return BatchFilter.ACTIVE_ONLY;
                case "3":
                    return BatchFilter.EXPIRING_SOON;
                case "4":
                    return BatchFilter.EXPIRED;
                case "5":
                    return BatchFilter.DEPLETED;
                case "0":
                    return null; // Return null to indicate user wants to go back
                default:
                    System.out.println("[Invalid] Enter 0-5.");
            }
        }
    }

    /**
     * Prompts for specific item code (optional).
     * YAGNI principle: Simple optional filtering.
     */
    private String promptSpecificItem() {
        System.out.print("Enter specific item code (or press Enter for all items): ");
        String itemCode = scanner.nextLine().trim();
        return itemCode.isEmpty() ? null : itemCode.toUpperCase();
    }

    @Override
    protected List<StockBatchDTO> fetchReportItems() {
        if (specificItemCode != null) {
            return service.getBatchesForItem(specificItemCode);
        }
        return service.getStockBatches(batchFilter);
    }

    @Override
    protected void renderReport(List<StockBatchDTO> batches) {
        // Show summary first if requested
        if (showSummary) {
            renderSummary();
            System.out.println("");
        }

        renderBatchDetails(batches);
    }

    /**
     * Renders batch summary statistics.
     * SRP: Focused on summary rendering only.
     */
    private void renderSummary() {
        BatchSummary summary = service.getBatchSummary();

        System.out.println("============================== BATCH SUMMARY ==============================");
        System.out.printf("Total Batches: %-8d | Active: %-8d | Expiring Soon: %-8d%n",
                summary.getTotalBatches(), summary.getActiveBatches(), summary.getExpiringSoon());
        System.out.printf("Expired: %-13d | Depleted: %-6d%n",
                summary.getExpired(), summary.getDepleted());
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("Total Stock: %-10d | Available: %-9d | Used: %-10d%n",
                summary.getTotalStock(), summary.getAvailableStock(), summary.getUsedStock());
        System.out.println("===============================================================================");
    }

    /**
     * Renders detailed batch information.
     * SRP: Focused on batch details rendering only.
     */
    private void renderBatchDetails(List<StockBatchDTO> batches) {
        System.out
                .println(
                        "======================================= STOCK BATCH DETAILS =======================================");
        System.out.println("Filter: " + getBatchFilterDescription());
        if (specificItemCode != null) {
            System.out.println("Item Code: " + specificItemCode);
        }
        System.out.println(
                "----------------------------------------------------------------------------------------------------");
        System.out.printf("%-6s %-10s %-20s %-12s %-12s %-8s %-9s %-8s %-10s%n",
                "S.No", "Code", "Name", "Purchase", "Expiry", "Total", "Available", "Used", "Price");
        System.out.println(
                "----------------------------------------------------------------------------------------------------");

        int serialNo = 1;
        for (StockBatchDTO batch : batches) {
            int availableQty = Math.max(0, batch.getAvailableQuantity()); // Prevent negative numbers
            String itemName = batch.getItemName() != null
                    ? (batch.getItemName().length() > 20 ? batch.getItemName().substring(0, 17) + "..."
                            : batch.getItemName())
                    : "-";

            System.out.printf("%-6d %-10s %-20s %-12s %-12s %-8d %-9d %-8d Rs.%-7.2f%n",
                    serialNo++,
                    batch.getItemCode(),
                    itemName,
                    batch.getPurchaseDate(),
                    batch.getExpiryDate(),
                    batch.getTotalQuantity(),
                    availableQty,
                    batch.getUsedQuantity(),
                    batch.getSellingPrice());
        }

        System.out.println(
                "----------------------------------------------------------------------------------------------------");
        System.out.printf("Total Batches Displayed: %d%n", batches.size());

        if (!batches.isEmpty()) {
            int totalStock = batches.stream().mapToInt(StockBatchDTO::getTotalQuantity).sum();
            int availableStock = batches.stream().mapToInt(b -> Math.max(0, b.getAvailableQuantity())).sum();
            int usedStock = batches.stream().mapToInt(StockBatchDTO::getUsedQuantity).sum();

            System.out.printf("Total Stock in View: %d | Available: %d | Used: %d%n",
                    totalStock, availableStock, usedStock);
        }

        System.out.println(
                "====================================================================================================");
    }

    /**
     * Gets description of selected batch filter.
     * KISS principle: Simple string mapping.
     */
    private String getBatchFilterDescription() {
        return switch (batchFilter) {
            case ALL -> "All Batches";
            case ACTIVE_ONLY -> "Active Batches Only";
            case EXPIRING_SOON -> "Expiring Soon (≤ 30 days)";
            case EXPIRED -> "Expired Batches";
            case DEPLETED -> "Depleted Batches";
        };
    }

    @Override
    protected void showNoDataMessage() {
        System.out
                .println(
                        "======================================= STOCK BATCH DETAILS =======================================");
        System.out.println("Filter: " + getBatchFilterDescription());
        if (specificItemCode != null) {
            System.out.println("Item Code: " + specificItemCode);
        }
        System.out.println("");
        System.out.println("No batches found matching the selected criteria.");
        System.out.println(
                "====================================================================================================");
        waitForEnter();
    }
}
