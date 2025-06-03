package syos.ui.views;

import syos.data.BatchGateway;
import syos.data.ItemGateway;
import syos.data.OnlineItemGateway;
import syos.dto.ItemDTO;
import syos.service.BatchService;
import syos.service.OnlineBatchService;

import java.util.List;
import java.util.Scanner;

import static syos.util.ConsoleUtils.clearScreen;
import static syos.util.ConsoleUtils.pause;

public class ReduceStockFromBatchUI {

    private final Scanner scanner;

    public ReduceStockFromBatchUI(Scanner scanner) {
        this.scanner = scanner;
    }

    public void start() {
        BatchGateway batchGateway = new BatchGateway();
        ItemGateway itemGateway = new ItemGateway();
        OnlineItemGateway onlineItemGateway = new OnlineItemGateway();
        BatchService batchService = new BatchService(batchGateway, itemGateway);
        OnlineBatchService onlineBatchService = new OnlineBatchService(batchGateway, onlineItemGateway);

        clearScreen();
        System.out.println("-------------------------------------------");
        System.out.println("       RESTOCK FROM BATCH");
        System.out.println("-------------------------------------------");
        System.out.println("1. Restock In-Store Inventory");
        System.out.println("2. Restock Online Inventory");
        System.out.println("0. Back");
        System.out.println("-------------------------------------------");
        System.out.print("Choose destination: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> restockInStoreFromBatch(scanner, batchService);
            case "2" -> restockOnlineFromBatch(scanner, onlineBatchService);
            case "0" -> {
                return;
            }
            default -> {
                System.out.println("Invalid option. Please try again.");
                pause(scanner);
            }
        }
    }

    private void restockInStoreFromBatch(Scanner scanner, BatchService batchService) {
        clearScreen();
        System.out.println("-------------------------------------------");
        System.out.println("    RESTOCK IN-STORE FROM BATCH");
        System.out.println("-------------------------------------------");

        ItemGateway itemGateway = new ItemGateway();
        List<ItemDTO> inStoreItems = itemGateway.getAllItems();

        System.out.println("\nCURRENT IN-STORE INVENTORY:");
        System.out.println("==============================================================");
        if (inStoreItems.isEmpty()) {
            System.out.println("No items in in-store inventory.");
        } else {
            System.out.printf("%-12s %-30s %-12s %-10s%n", "Code", "Name", "Price", "Stock");
            System.out.println("--------------------------------------------------------------");
            for (ItemDTO item : inStoreItems) {
                System.out.printf("%-12s %-30s Rs.%-9.2f %-10d%n",
                        item.getCode(),
                        truncateString(item.getName(), 30),
                        item.getPrice(),
                        item.getQuantity());
            }
        }
        System.out.println("==============================================================");

        System.out.print("\nEnter item code to restock from batch: ");
        String itemCode = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter quantity to move to in-store shelf: ");
        try {
            int qtyToMove = Integer.parseInt(scanner.nextLine().trim());

            if (qtyToMove <= 0) {
                System.out.println("Quantity must be positive.");
                pause(scanner);
                return;
            }

            boolean success = batchService.restockItem(itemCode, qtyToMove);
            if (success) {
                System.out.println("\n Stock moved to in-store inventory successfully.");
            } else {
                System.out.println("\n Not enough quantity in batches to fulfill the request.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity format.");
        }

        pause(scanner);
    }

    private void restockOnlineFromBatch(Scanner scanner, OnlineBatchService onlineBatchService) {
        clearScreen();
        System.out.println("-------------------------------------------");
        System.out.println("     RESTOCK ONLINE FROM BATCH");
        System.out.println("-------------------------------------------");

        OnlineItemGateway onlineItemGateway = new OnlineItemGateway();
        List<ItemDTO> onlineItems = onlineItemGateway.getAllItems();

        System.out.println("\nCURRENT ONLINE INVENTORY:");
        System.out.println("==============================================================");
        if (onlineItems.isEmpty()) {
            System.out.println("No items in online inventory.");
        } else {
            System.out.printf("%-12s %-30s %-12s %-10s%n", "Code", "Name", "Price", "Stock");
            System.out.println("--------------------------------------------------------------");
            for (ItemDTO item : onlineItems) {
                System.out.printf("%-12s %-30s Rs.%-9.2f %-10d%n",
                        item.getCode(),
                        truncateString(item.getName(), 30),
                        item.getPrice(),
                        item.getQuantity());
            }
        }
        System.out.println("==============================================================");

        System.out.print("\nEnter item code to restock from batch: ");
        String itemCode = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter quantity to move to online inventory: ");
        try {
            int qtyToMove = Integer.parseInt(scanner.nextLine().trim());

            if (qtyToMove <= 0) {
                System.out.println("Quantity must be positive.");
                pause(scanner);
                return;
            }

            boolean success = onlineBatchService.restockOnlineItem(itemCode, qtyToMove);
            if (success) {
                System.out.println("\n✓ Stock moved to online inventory successfully.");
            } else {
                System.out.println("\n✗ Not enough quantity in batches to fulfill the request.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity format.");
        }

        pause(scanner);
    }

    private String truncateString(String input, int maxLength) {
        return (input.length() <= maxLength) ? input : input.substring(0, maxLength - 3) + "...";
    }
}
