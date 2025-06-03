package syos.ui.views;

import syos.data.BatchGateway;
import syos.data.ItemGateway;
import syos.data.OnlineItemGateway;
import syos.dto.BatchDTO;
import syos.dto.ItemDTO;
import syos.service.BatchService;
import syos.service.BatchService.StoreType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import static syos.util.ConsoleUtils.clearScreen;
import static syos.util.ConsoleUtils.pause;

public class AddBatchStockUI {

    private final Scanner scanner;

    public AddBatchStockUI(Scanner scanner) {
        this.scanner = scanner;
    }

    public void start() {
        BatchGateway batchGateway = new BatchGateway();
        ItemGateway itemGateway = new ItemGateway();
        OnlineItemGateway onlineItemGateway = new OnlineItemGateway();
        BatchService batchService = new BatchService(batchGateway, itemGateway);

        clearScreen();
        System.out.println("-------------------------------------------");
        System.out.println("           ADD NEW BATCH STOCK");
        System.out.println("-------------------------------------------");

        // Store type selection
        System.out.println("Select Store Type:");
        System.out.println("1. In-Store Inventory");
        System.out.println("2. Online Store Inventory");
        System.out.println("0. Cancel");
        System.out.print("Choose store type (1-2): ");

        String storeChoice = scanner.nextLine().trim();
        StoreType storeType;
        ItemGateway targetGateway = null;
        OnlineItemGateway onlineTargetGateway = null;

        switch (storeChoice) {
            case "1" -> {
                storeType = StoreType.IN_STORE;
                targetGateway = itemGateway;
            }
            case "2" -> {
                storeType = StoreType.ONLINE;
                onlineTargetGateway = onlineItemGateway;
            }
            case "0" -> {
                return;
            }
            default -> {
                System.out.println("Invalid choice. Please try again.");
                pause(scanner);
                return;
            }
        }

        clearScreen();
        System.out.println("-------------------------------------------");
        System.out.printf("ADDING BATCH FOR %s INVENTORY%n",
                storeType == StoreType.IN_STORE ? "IN-STORE" : "ONLINE");
        System.out.println("-------------------------------------------");

        System.out.print("Enter item code: ");
        String code = scanner.nextLine().trim().toUpperCase();

        ItemDTO existingItem = (storeType == StoreType.IN_STORE)
                ? targetGateway.getItemByCode(code)
                : onlineTargetGateway.getItemByCode(code);

        String itemName;
        double sellingPrice;

        if (existingItem != null) {
            itemName = existingItem.getName();
            System.out.printf("Existing item found: %s\n", itemName);
            System.out.printf("Current selling price: Rs. %.2f\n", existingItem.getPrice());

            System.out.print("Enter new selling price (or press Enter to keep current): ");
            String priceInput = scanner.nextLine().trim();
            sellingPrice = priceInput.isEmpty() ? existingItem.getPrice()
                    : parsePriceOrDefault(priceInput, existingItem.getPrice());
        } else {
            System.out.println("New item detected. Please provide details:");
            System.out.print("Enter item name: ");
            itemName = scanner.nextLine().trim();

            System.out.print("Enter selling price: ");
            sellingPrice = parsePriceOrDefault(scanner.nextLine().trim(), -1);
            if (sellingPrice < 0) {
                pause(scanner);
                return;
            }
        }

        System.out.print("Enter quantity: ");
        int quantity = parseIntOrCancel(scanner.nextLine().trim());
        if (quantity <= 0) {
            System.out.println("Quantity must be positive.");
            pause(scanner);
            return;
        }

        System.out.print("Enter purchase date (YYYY-MM-DD): ");
        LocalDate purchaseDate = parseDateOrCancel(scanner.nextLine().trim());
        if (purchaseDate == null) {
            pause(scanner);
            return;
        }

        System.out.print("Enter expiry date (YYYY-MM-DD): ");
        LocalDate expiryDate = parseDateOrCancel(scanner.nextLine().trim());
        if (expiryDate == null) {
            pause(scanner);
            return;
        }

        BatchDTO batch = new BatchDTO(code, itemName, sellingPrice, quantity, purchaseDate, expiryDate);

        try {
            boolean success = batchService.addNewBatch(batch, storeType);
            clearScreen();
            if (success) {
                System.out.println("===============================================");
                System.out.println("              BATCH ADDED SUCCESSFULLY         ");
                System.out.println("===============================================");
                if (existingItem == null) {
                    System.out.printf("New item created in %s inventory!\n",
                            storeType == StoreType.IN_STORE ? "in-store" : "online");
                } else if (existingItem.getPrice() != sellingPrice) {
                    System.out.println("Item price updated!");
                }

                System.out.printf("%-18s : %s\n", "Store Type",
                        storeType == StoreType.IN_STORE ? "In-Store" : "Online");
                System.out.printf("%-18s : %s (%s)\n", "Item", itemName, code);
                System.out.printf("%-18s : %d\n", "Quantity", quantity);
                System.out.printf("%-18s : Rs. %.2f\n", "Selling Price", sellingPrice);
                System.out.printf("%-18s : %s\n", "Purchase Date", purchaseDate);
                System.out.printf("%-18s : %s\n", "Expiry Date", expiryDate);
                System.out.println("===============================================\n");

            } else {
                System.out.println("\n✗ Failed to add batch. Please check input.");
            }
        } catch (RuntimeException e) {
            System.out.println("\nError: " + e.getMessage());
        }

        pause(scanner);
    }

    private double parsePriceOrDefault(String input, double defaultValue) {
        try {
            double price = Double.parseDouble(input);
            return (price > 0) ? price : defaultValue;
        } catch (NumberFormatException e) {
            System.out.println("Invalid price format.");
            return defaultValue;
        }
    }

    private int parseIntOrCancel(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity format.");
            return -1;
        }
    }

    private LocalDate parseDateOrCancel(String input) {
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Use YYYY-MM-DD.");
            return null;
        }
    }
}
