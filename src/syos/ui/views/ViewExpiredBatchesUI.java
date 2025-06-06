package syos.ui.views;

import syos.data.BatchGateway;
import syos.data.ItemGateway;
import syos.dto.BatchDTO;
import syos.service.BatchService;

import java.util.List;
import java.util.Scanner;

import static syos.util.ConsoleUtils.clearScreen;
import static syos.util.ConsoleUtils.pause;

public class ViewExpiredBatchesUI {

    private final Scanner scanner;
    private final BatchService batchService;

    public ViewExpiredBatchesUI(Scanner scanner) {
        this.scanner = scanner;
        this.batchService = new BatchService(new BatchGateway(), new ItemGateway());
    }

    public void start() {
        clearScreen();
        System.out.println("===============================================================");
        System.out.println("                      ARCHIVED EXPIRED BATCHES                 ");
        System.out.println("===============================================================\n");

        List<BatchDTO> archivedExpiredBatches = batchService.getArchivedExpiredBatches();

        if (archivedExpiredBatches.isEmpty()) {
            System.out.println("No expired batches found in archive.");
            pause(scanner);
            return;
        }

        System.out.printf("%-5s %-12s %-25s %-10s %-12s %-12s%n",
                "No.", "Item Code", "Item Name", "Quantity", "Expiry", "Purchased");
        System.out.println("--------------------------------------------------------------------------");

        int index = 1;
        for (BatchDTO batch : archivedExpiredBatches) {
            System.out.printf("%-5d %-12s %-25s %-10d %-12s %-12s%n",
                    index++,
                    batch.getItemCode(),
                    truncate(batch.getName(), 25),
                    batch.getQuantity(),
                    batch.getExpiryDate(),
                    batch.getPurchaseDate());
        }

        System.out.println("\nNote: These batches have been automatically archived.");
        System.out.print("Press 'c' to clear all archived records, or press Enter to return: ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("c")) {
            System.out.print("Are you sure you want to permanently delete all archived expired batches? (yes/no): ");
            String confirmation = scanner.nextLine().trim();

            if (confirmation.equalsIgnoreCase("yes") || confirmation.equalsIgnoreCase("y")) {
                if (batchService.clearArchivedExpiredBatches()) {
                    System.out.println("All archived expired batches have been permanently deleted.");
                } else {
                    System.out.println("Failed to clear archived batches.");
                }
            } else {
                System.out.println("Operation cancelled.");
            }
        }

        pause(scanner);
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
