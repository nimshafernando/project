package syos.ui.views;

import syos.data.BatchGateway;
import syos.data.ItemGateway;
import syos.service.BatchService;
import syos.util.ConsoleUtils;
import java.util.Scanner;

/**
 * Simple UI for auto-cleanup of expired batches.
 */
public class AutoCleanupExpiredBatchesUI {

    private final Scanner scanner;
    private final BatchService batchService;

    public AutoCleanupExpiredBatchesUI(Scanner scanner) {
        this.scanner = scanner;
        this.batchService = new BatchService(new BatchGateway(), new ItemGateway());
    }

    public void start() {
        ConsoleUtils.clearScreen();
        System.out.println("=== AUTO CLEANUP EXPIRED BATCHES ===");
        System.out.println();

        try {
            System.out.print("Run auto cleanup of expired batches? (y/n): ");
            String confirm = scanner.nextLine().trim();

            if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
                System.out.println("\nRunning auto cleanup...");
                batchService.autoCleanupExpiredBatches();
                System.out.println("Cleanup completed!");
            } else {
                System.out.println("Cleanup cancelled.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println();
        ConsoleUtils.pause(scanner);
    }
}
