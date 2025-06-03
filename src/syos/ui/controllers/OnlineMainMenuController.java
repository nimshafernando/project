package syos.ui.controllers;

import syos.data.OnlineBillGateway;
import syos.data.OnlineItemGateway;
import syos.data.OnlineUserGateway;
import syos.dto.ItemDTO;
import syos.model.Bill;
import syos.model.CartItem;
import syos.model.OnlineUser;
import syos.service.OnlinePOS;
import syos.ui.views.OnlineCheckoutUI;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class OnlineMainMenuController {

    public static void launch(Scanner scanner) {
        OnlineUserGateway userGateway = new OnlineUserGateway();
        OnlinePOS onlinePOS = new OnlinePOS();

        while (true) {
            clearScreen();
            System.out.println("===========================================");
            System.out.println("         SYOS ONLINE STORE PORTAL");
            System.out.println("===========================================");
            System.out.println("1. Login");
            System.out.println("2. Register New User");
            System.out.println("0. Return to Main Menu");
            System.out.print("Choose an option: ");
            String mainChoice = scanner.nextLine().trim();

            switch (mainChoice) {
                case "1" -> {
                    clearScreen();
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine().trim();

                    if (!userGateway.authenticateUser(username, password)) {
                        System.out.println("Login failed. Invalid credentials.");
                        pause(scanner);
                        break;
                    }

                    OnlineUser user = userGateway.getUserByUsername(username);
                    launchOnlineStore(scanner, onlinePOS, user);
                }

                case "2" -> {
                    System.out.print("Choose a username: ");
                    String newUsername = scanner.nextLine().trim();
                    System.out.print("Choose a password: ");
                    String newPassword = scanner.nextLine().trim();
                    System.out.print("Enter contact number: ");
                    String contact = scanner.nextLine().trim();
                    System.out.print("Enter address: ");
                    String address = scanner.nextLine().trim();

                    OnlineUser newUser = new OnlineUser(newUsername, newPassword, contact, address);
                    boolean registered = userGateway.registerUser(newUser);
                    if (registered) {
                        System.out.println("Registration successful. You can now login.");
                    } else {
                        System.out.println("Username already taken. Please try again.");
                    }
                    pause(scanner);
                }

                case "0" -> {
                    System.out.println("Returning to main menu...");
                    pause(scanner);
                    return;
                }

                default -> {
                    System.out.println("Invalid option. Please try again.");
                    pause(scanner);
                }
            }
        }
    }

    private static void launchOnlineStore(Scanner scanner, OnlinePOS pos, OnlineUser user) {
        OnlineItemGateway itemGateway = new OnlineItemGateway();

        while (true) {
            clearScreen();
            System.out.println("===========================================");
            System.out.println("         WELCOME " + user.getUsername().toUpperCase());
            System.out.println("===========================================");
            System.out.println("1. View All Items");
            System.out.println("2. View Cart");
            System.out.println("3. Checkout");
            System.out.println("4. View Past Bills");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    clearScreen();
                    viewAllItems(itemGateway, pos, scanner);
                }
                case "2" -> {
                    clearScreen();
                    viewCart(pos);
                    pause(scanner);
                }
                case "3" -> {
                    clearScreen();
                    // Create and start OnlineCheckoutUI with scanner
                    OnlineCheckoutUI checkoutUI = new OnlineCheckoutUI(scanner, pos, user);
                    checkoutUI.start();
                    // No need for pause here as OnlineCheckoutUI handles it
                }
                case "4" -> {
                    clearScreen();
                    viewPastBills(user, scanner);
                    pause(scanner);
                }
                case "0" -> {
                    System.out.println("Logging out...");
                    pause(scanner);
                    return;
                }
                default -> {
                    System.out.println("Invalid option. Please try again.");
                    pause(scanner);
                }
            }
        }
    }

    public static void showMainMenu(Scanner scanner, OnlineUser user) {
        OnlineItemGateway gateway = new OnlineItemGateway();
        OnlinePOS pos = new OnlinePOS();

        while (true) {
            clearScreen();
            System.out.println("=======================================");
            System.out.println("         SYOS ONLINE STORE");
            System.out.println("=======================================");
            System.out.printf("Welcome, %s!\n", user.getUsername());
            System.out.println("1. View All Items");
            System.out.println("2. View Cart");
            System.out.println("3. Checkout");
            System.out.println("4. Clear Cart");
            System.out.println("5. View Past Bills");
            System.out.println("0. Logout");
            System.out.println("---------------------------------------");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    clearScreen();
                    viewAllItems(gateway, pos, scanner);
                }
                case "2" -> {
                    clearScreen();
                    viewCart(pos);
                    pause(scanner);
                }
                case "3" -> {
                    clearScreen();
                    // Create and start OnlineCheckoutUI with scanner
                    OnlineCheckoutUI checkoutUI = new OnlineCheckoutUI(scanner, pos, user);
                    checkoutUI.start();
                    // No need for pause here as OnlineCheckoutUI handles it
                }
                case "4" -> {
                    pos.clearCart();
                    System.out.println("Cart cleared!");
                    pause(scanner);
                }
                case "5" -> {
                    clearScreen();
                    viewPastBills(user, scanner);
                    pause(scanner);
                }
                case "0" -> {
                    System.out.println("Goodbye, " + user.getUsername() + "!");
                    return;
                }
                default -> {
                    System.out.println("Invalid option. Please try again.");
                    pause(scanner);
                }
            }
        }
    }

    private static void viewAllItems(OnlineItemGateway gateway, OnlinePOS pos, Scanner scanner) {
        List<ItemDTO> items = gateway.getAllItems();

        if (items.isEmpty()) {
            System.out.println("No items found in online inventory.");
            pause(scanner);
            return;
        }

        while (true) {
            clearScreen();
            System.out.println("====================================================================================");
            System.out.printf("| %-10s | %-35s | %10s | %7s |\n", "Code", "Name", "Price", "Stock");
            System.out.println("====================================================================================");
            for (ItemDTO item : items) {
                System.out.printf("| %-10s | %-35s | %10.2f | %7d |\n",
                        item.getCode(), item.getName(), item.getPrice(), item.getQuantity());
            }
            System.out.println("====================================================================================");

            System.out.println("\nOptions:");
            System.out.println("1. Add item to cart");
            System.out.println("0. Back to main menu");
            System.out.print("Choose option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    addItemToCartFromView(scanner, gateway, pos);
                    // Refresh the item list after adding to cart to show updated stock
                    items = gateway.getAllItems();
                }
                case "0" -> {
                    return;
                }
                default -> {
                    System.out.println("Invalid option. Please try again.");
                    pause(scanner);
                }
            }
        }
    }

    private static void addItemToCartFromView(Scanner scanner, OnlineItemGateway gateway, OnlinePOS pos) {
        System.out.print("Enter item code to add to cart: ");
        String code = scanner.nextLine().trim().toUpperCase();

        ItemDTO item = gateway.getItemByCode(code);
        if (item == null) {
            System.out.println("Item not found.");
            pause(scanner);
            return;
        }

        System.out.printf("Selected: %s - Rs. %.2f (Stock: %d)\n",
                item.getName(), item.getPrice(), item.getQuantity());

        if (item.getQuantity() == 0) {
            System.out.println("Item is out of stock.");
            pause(scanner);
            return;
        }

        System.out.print("Enter quantity: ");
        int qty;
        try {
            qty = Integer.parseInt(scanner.nextLine().trim());
            if (qty <= 0) {
                System.out.println("Quantity must be positive.");
                pause(scanner);
                return;
            }
            if (qty > item.getQuantity()) {
                System.out.println("Insufficient stock. Available: " + item.getQuantity());
                pause(scanner);
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity format.");
            pause(scanner);
            return;
        }

        // Add to cart (stock reduction happens at checkout)
        pos.addToCart(item, qty);

        System.out.printf("Added %d x %s to cart. Subtotal: Rs. %.2f\n",
                qty, item.getName(), item.getPrice() * qty);
        System.out.printf("Cart total: Rs. %.2f\n", pos.getCartTotal());

        pause(scanner); // Always pause at the end
    }

    private static void viewCart(OnlinePOS pos) {
        List<CartItem> cartItems = pos.getCartItems();

        if (cartItems.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }

        System.out.println(
                "===============================================================================================");
        System.out.printf("| %-10s | %-35s | %7s | %10s | %10s |\n", "Code", "Name", "Qty", "Price", "Subtotal");
        System.out.println(
                "===============================================================================================");

        double total = 0;
        for (CartItem item : cartItems) {
            double subtotal = item.getQuantity() * item.getItem().getPrice();
            total += subtotal;

            System.out.printf("| %-10s | %-35s | %7d | %10.2f | %10.2f |\n",
                    item.getItem().getCode(),
                    item.getItem().getName(),
                    item.getQuantity(),
                    item.getItem().getPrice(),
                    subtotal);
        }

        System.out.println(
                "===============================================================================================");
        System.out.printf("| %-67s | TOTAL: %10.2f |\n", "", total);
        System.out.println(
                "===============================================================================================");
    }

    private static void viewPastBills(OnlineUser user, Scanner scanner) {
        OnlineBillGateway gateway = new OnlineBillGateway();
        List<Bill> bills = gateway.getBillsByUsername(user.getUsername());

        if (bills.isEmpty()) {
            System.out.println("You have no past bills.");
            return;
        }

        System.out.println("===============================================================");
        System.out.println("                         YOUR PAST BILLS");
        System.out.println("===============================================================");
        System.out.printf("%-5s %-20s %-20s %-12s%n", "ID", "Date & Time", "Payment", "Total (Rs.)");
        System.out.println("---------------------------------------------------------------");

        for (Bill bill : bills) {
            // Convert payment method to abbreviations
            String paymentMethod = bill.getPaymentMethod() != null ? bill.getPaymentMethod() : "N/A";
            if (paymentMethod.equalsIgnoreCase("Cash on Delivery")) {
                paymentMethod = "COD";
            } else if (paymentMethod.equalsIgnoreCase("Pay in Store")) {
                paymentMethod = "PIS";
            }

            System.out.printf("%-5d %-20s %-20s %-12.2f%n",
                    bill.getId(),
                    bill.getDate().toString().replace("T", " "),
                    paymentMethod,
                    bill.getTotal());
        }

        System.out.println("===============================================================");
        System.out.print("Enter Bill ID to view details (or 0 to go back): ");

        try {
            int selectedBillId = Integer.parseInt(scanner.nextLine().trim());

            if (selectedBillId == 0) {
                return;
            }

            Bill selectedBill = null;
            for (Bill bill : bills) {
                if (bill.getId() == selectedBillId) {
                    selectedBill = bill;
                    break;
                }
            }

            if (selectedBill == null) {
                System.out.println("Invalid Bill ID.");
                return;
            }

            List<CartItem> items = gateway.getItemsForBill(selectedBillId);
            selectedBill.setItems(items);

            displayDetailedBill(selectedBill);

        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid Bill ID.");
        } catch (Exception e) {
            System.out.println("Error retrieving bill details: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void displayDetailedBill(Bill bill) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                          BILL DETAILS");
        System.out.println("=".repeat(70));
        System.out.println("Bill ID      : " + bill.getId());
        System.out.println("Date & Time  : " + bill.getDate().toString().replace("T", " "));
        System.out.println("Payment      : " + (bill.getPaymentMethod() != null ? bill.getPaymentMethod() : "N/A"));
        System.out.println("-".repeat(70));
        System.out.printf("%-12s %-30s %7s %10s %10s%n", "Code", "Item Name", "Qty", "Price", "Subtotal");
        System.out.println("-".repeat(70));

        double totalAmount = 0;
        for (CartItem item : bill.getItems()) {
            double subtotal = item.getQuantity() * item.getItem().getPrice();
            totalAmount += subtotal;
            System.out.printf("%-12s %-30s %7d %10.2f %10.2f%n",
                    item.getItem().getCode(),
                    truncateString(item.getItem().getName(), 30),
                    item.getQuantity(),
                    item.getItem().getPrice(),
                    subtotal);
        }

        System.out.println("-".repeat(70));
        System.out.printf("%59s Rs. %10.2f%n", "TOTAL:", totalAmount);
        System.out.println("=".repeat(70));
    }

    // Helper method to truncate long item names for table
    private static String truncateString(String str, int maxLength) {
        if (str == null)
            return "";
        if (str.length() <= maxLength)
            return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (IOException | InterruptedException ex) {
            // Fallback: print empty lines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    /**
     * Pauses execution and waits for user to press Enter
     * Uses Scanner to avoid input buffer issues
     */
    private static void pause(Scanner scanner) {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}