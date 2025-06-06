package syos.ui.views;

import syos.service.StockBatchReportService;
import syos.service.StockBatchReportService.BatchSummary;
import syos.util.ConsoleUtils;

import java.util.Scanner;

/**
 * Console UI for displaying a summary of stock batch information.
 */
public class StockSummaryUI {

    private final Scanner scanner;
    private final StockBatchReportService reportService;

    public StockSummaryUI(Scanner scanner) {
        this.scanner = scanner;
        this.reportService = new StockBatchReportService();
    }

    // Constructor for dependency injection (testing)
    public StockSummaryUI(Scanner scanner, StockBatchReportService reportService) {
        this.scanner = scanner;
        this.reportService = reportService;
    }

    public void start() {
        ConsoleUtils.clearScreen();
        System.out.println("=====================================================");
        System.out.println("                  STOCK SUMMARY REPORT               ");
        System.out.println("=====================================================\n");

        try {
            BatchSummary summary = reportService.getBatchSummary();

            if (summary != null) {
                // Header
                System.out.printf("%-25s : %s%n", "Total Batches", summary.getTotalBatches());
                System.out.printf("%-25s : %s%n", "Active Batches", summary.getActiveBatches());
                System.out.printf("%-25s : %s%n", "Expired Batches", summary.getExpired());
                System.out.println("-----------------------------------------------------");
                System.out.printf("%-25s : %,d units%n", "Total Stock", summary.getTotalStock());
                System.out.printf("%-25s : %,d units%n", "Available Stock", summary.getAvailableStock());
                System.out.printf("%-25s : %,d units%n", "Used Stock", summary.getUsedStock());

                if (summary.getTotalStock() > 0) {
                    double usageRate = (double) summary.getUsedStock() / summary.getTotalStock() * 100;
                    System.out.printf("%-25s : %.1f%%%n", "Usage Rate", usageRate);
                }
            } else {
                System.out.println("Error: Unable to retrieve stock summary data.");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("\n=====================================================");
        ConsoleUtils.pause(scanner);
    }
}
