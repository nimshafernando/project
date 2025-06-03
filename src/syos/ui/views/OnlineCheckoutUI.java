package syos.ui.views;

import syos.model.CartItem;
import syos.model.OnlineUser;
import syos.service.OnlinePOS;

import java.util.List;
import java.util.Scanner;

public class OnlineCheckoutUI {

    private final OnlinePOS pos;
    private final OnlineUser user;
    private final Scanner scanner;

    public OnlineCheckoutUI(Scanner scanner, OnlinePOS pos, OnlineUser user) {
        this.scanner = scanner;
        this.pos = pos;
        this.user = user;
    }

    public void start() {
        try {
            List<CartItem> cartItems = pos.getCartItems();

            if (cartItems == null || cartItems.isEmpty()) {
                System.out.println("Your cart is empty.");
                pause();
                return;
            }

            displayCart(cartItems);

            String paymentMethod = selectPaymentMethod();
            if (paymentMethod == null) {
                return; // User chose invalid option
            }

            processCheckout(paymentMethod);

        } catch (Exception e) {
            System.out.println("\nAn error occurred during checkout: " + e.getMessage());
            e.printStackTrace();
        }

        pause();
    }

    private void displayCart(List<CartItem> cartItems) {
        System.out.println("====================================================================================");
        System.out.printf("| %-10s | %-35s | %10s | %8s | %12s |\n", "Code", "Name", "Price", "Quantity",
                "Subtotal");
        System.out.println("====================================================================================");

        double total = 0;
        for (CartItem item : cartItems) {
            if (item != null && item.getItem() != null) {
                double subtotal = item.getTotalPrice();
                total += subtotal;

                System.out.printf("| %-10s | %-35s | %10.2f | %8d | %12.2f |\n",
                        item.getItem().getCode(),
                        item.getItem().getName(),
                        item.getItem().getPrice(),
                        item.getQuantity(),
                        subtotal);
            }
        }

        System.out.println("====================================================================================");
        System.out.printf("| %-67s | TOTAL: %10.2f |\n", "", total);
        System.out.println("====================================================================================");
    }

    private String selectPaymentMethod() {
        System.out.println("\nSelect Payment Method:");
        System.out.println("1. Pay in Store");
        System.out.println("2. Cash on Delivery");
        System.out.print("Enter choice: ");

        // Ensure we read a fresh line
        String choice = "";
        try {
            choice = scanner.nextLine().trim();
        } catch (Exception e) {
            System.out.println("Error reading input: " + e.getMessage());
            pause();
            return null;
        }

        switch (choice) {
            case "1":
                return "Pay in Store";
            case "2":
                return "Cash on Delivery";
            default:
                System.out.println("Invalid choice: '" + choice + "'");
                pause();
                return null;
        }
    }

    private void processCheckout(String paymentMethod) {
        try {
            pos.checkout(paymentMethod, user); // This will handle OnlineBillPrinter

            // Remove saveBillToFile() call here - let OnlineBillPrinter handle it
            // Don't call: saveBillToFile(bill, paymentMethod, user.getUsername());

            System.out.println("\nCheckout successful. Your bill has been generated.");

        } catch (Exception e) {
            System.out.println("\nCheckout failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void pause() {
        try {
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
        } catch (Exception e) {
            // Ignore any exception during pause
        }
    }
}