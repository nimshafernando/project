package syos.ui.controllers;

import syos.model.Employee;
import syos.model.Bill;
import syos.model.CartItem;
import syos.dto.ItemDTO;
import syos.service.POS;
import syos.data.ItemGateway;
import syos.data.BillGateway;
import syos.util.BillStorage;
import syos.util.ConsoleUtils;
import syos.util.EmployeeSession;

import java.util.List;
import java.util.Scanner;

public class CheckoutAndBillingController {

    public static void launch(Scanner scanner, ItemGateway gateway, POS pos, Employee employee) {
        while (true) {
            ConsoleUtils.clearScreen();
            showEmployeeHeader();

            System.out.println("===========================================");
            System.out.println("         CHECKOUT AND BILLING MODULE");
            System.out.println("===========================================");
            System.out.printf("Cashier: %s (%s)\n", employee.getDisplayName(), employee.getEmployeeId());
            System.out.println("-------------------------------------------");

            // ─── Step 0: Display Available Items ──────────────────────────────────────
            try {
                List<ItemDTO> allItems = gateway.getAllItems();
                if (allItems.isEmpty()) {
                    System.out.println("No items available in inventory.");
                    ConsoleUtils.pause(scanner);
                    return;
                }

                System.out.println("\nAVAILABLE ITEMS:");
                System.out.println("===========================================");
                System.out.printf("%-10s %-30s %-10s %-10s\n", "CODE", "NAME", "PRICE (Rs.)", "STOCK");
                System.out.println("------------------------------------------------------------------");
                for (ItemDTO item : allItems) {
                    System.out.printf("%-10s %-30s %-10.2f %-10d\n",
                            item.getCode(),
                            truncateString(item.getName(), 30),
                            item.getPrice(),
                            item.getQuantity());
                }
                System.out.println("------------------------------------------------------------------");
            } catch (Exception e) {
                System.out.println("Error loading items: " + e.getMessage());
                ConsoleUtils.pause(scanner);
                return;
            }

            // ─── Step 1: Scan Items ───────────────────────────────────────────────────
            while (true) {
                System.out.print("\nEnter item code (or type 'checkout' to proceed): ");
                String code = scanner.nextLine().trim();
                if (code.isEmpty())
                    return; // Silent return to main menu
                if (code.equalsIgnoreCase("checkout"))
                    break;

                ItemDTO item = gateway.getItemByCode(code);
                if (item == null) {
                    System.out.println("Item not found in inventory.");
                    continue;
                }

                System.out.print("Enter quantity: ");
                int quantity;
                try {
                    quantity = Integer.parseInt(scanner.nextLine());
                    if (quantity <= 0) {
                        System.out.println("Quantity must be a positive integer.");
                        continue;
                    }
                    if (quantity > item.getQuantity()) {
                        System.out.printf("Only %d units available in stock.\n", item.getQuantity());
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid quantity. Please enter a valid number.");
                    continue;
                }

                pos.addToCart(item, quantity);
                System.out.printf("Added %d x %s at Rs. %.2f each\n",
                        quantity, item.getName(), item.getPrice());
                System.out.printf("Current total: Rs. %.2f\n", pos.getCartTotal());
            }

            if (pos.getCartItems().isEmpty()) {
                System.out.println("\nNo items in cart. Cancelling transaction.");
                ConsoleUtils.pause(scanner);
                return;
            }

            // ─── Step 2: Apply Discount ──────────────────────────────────────────────
            double totalBeforeDiscount = pos.getCartTotal();
            System.out.printf("\nTotal before discount: Rs. %.2f\n", totalBeforeDiscount);

            double discount;
            try {
                System.out.print("Enter discount (e.g. 10 or 10%): ");
                String discountInput = scanner.nextLine().trim();
                if (discountInput.endsWith("%")) {
                    double percent = Double.parseDouble(discountInput.replace("%", ""));
                    discount = totalBeforeDiscount * (percent / 100.0);
                } else {
                    discount = Double.parseDouble(discountInput);
                }
            } catch (Exception e) {
                System.out.println("Invalid discount format. Assuming no discount.");
                discount = 0.0;
            }

            double totalAfterDiscount = totalBeforeDiscount - discount;
            System.out.printf("Discount applied: Rs. %.2f\n", discount);
            System.out.printf("Total after discount: Rs. %.2f\n", totalAfterDiscount);

            // ─── Step 3: Payment ─────────────────────────────────────────────────────
            System.out.print("Enter cash tendered: ");
            double cash;
            try {
                cash = Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid cash amount entered. Cancelling transaction.");
                return;
            }

            // ─── Step 4: Finalize ────────────────────────────────────────────────────
            try {
                Bill bill = pos.checkout(cash, discount);

                BillGateway billGateway = new BillGateway();
                billGateway.saveBill(bill);
                BillStorage.save(bill);

                for (CartItem cartItem : bill.getItems()) {
                    gateway.reduceStock(cartItem.getItem().getCode(), cartItem.getQuantity());
                }

                System.out.println("\n-------------------------------------------");
                System.out.println("Transaction completed successfully.");
                System.out.println("Thank you for shopping at SYOS.");
                System.out.println("-------------------------------------------");
                ConsoleUtils.pause(scanner);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
                ConsoleUtils.pause(scanner);
            }
        }
    }

    private static String truncateString(String str, int maxLength) {
        if (str == null)
            return "";
        return str.length() <= maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }

    private static void showEmployeeHeader() {
        EmployeeSession session = EmployeeSession.getInstance();
        if (session.isLoggedIn()) {
            System.out.println("+---------------------------------------------+");
            System.out.printf("| Cashier: %-35s |\n", session.getCurrentEmployeeName());
            System.out.printf("| ID: %-39s |\n", session.getEmployeeId());
            System.out.println("+---------------------------------------------+");

        }
    }
}
