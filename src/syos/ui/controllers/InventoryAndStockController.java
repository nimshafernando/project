package syos.ui.controllers;

import syos.ui.views.AddBatchStockUI;
import syos.ui.views.ReduceStockFromBatchUI;
import syos.ui.views.ViewExpiredBatchesUI;
import syos.ui.views.StockSummaryUI;
import syos.ui.views.AutoCleanupExpiredBatchesUI;
import syos.model.Employee;
import syos.util.ConsoleUtils;
import java.util.Scanner;

/**
 * Inventory and Stock Controller
 * Handles inventory management operations
 * Implements SOLID principles: SRP (inventory operations),
 * OCP (extensible inventory features)
 */
public class InventoryAndStockController {

    public static void launch(Scanner scanner, Employee employee) {
        while (true) {
            ConsoleUtils.clearScreen();
            System.out.println("-------------------------------------------");
            System.out.println("      INVENTORY AND STOCK OPERATIONS");
            System.out.println("-------------------------------------------");
            System.out.println("1. Add Batch Stock");
            System.out.println("2. Reduce Shelf Stock by Batch");
            System.out.println("3. View Expired Batches");
            System.out.println("4. Stock Summary (Total / Used / Available)");
            System.out.println("5. Auto-Cleanup Expired Batches");
            System.out.println("0. Back to Main Menu");
            System.out.println("-------------------------------------------");
            System.out.print("Select an option: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {
                    AddBatchStockUI addBatchUI = new AddBatchStockUI(scanner);
                    addBatchUI.start();
                }
                case "2" -> {
                    ReduceStockFromBatchUI reduceStockUI = new ReduceStockFromBatchUI(scanner);
                    reduceStockUI.start();
                }
                case "3" -> {
                    ViewExpiredBatchesUI viewExpiredUI = new ViewExpiredBatchesUI(scanner);
                    viewExpiredUI.start();
                }
                case "4" -> {
                    StockSummaryUI stockSummaryUI = new StockSummaryUI(scanner);
                    stockSummaryUI.start();
                }
                case "5" -> {
                    AutoCleanupExpiredBatchesUI autoCleanupUI = new AutoCleanupExpiredBatchesUI(scanner);
                    autoCleanupUI.start();
                }
                case "0" -> {
                    System.out.println("\nReturning to Employee Portal...");
                    ConsoleUtils.pause(scanner);
                    return;
                }
                default -> {
                    System.out.println("\nInvalid option. Please select again.");
                    ConsoleUtils.pause(scanner);
                }
            }
        }
    }
}
