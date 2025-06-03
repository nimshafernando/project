package syos.ui.controllers;

import syos.util.ConsoleUtils;
import java.util.Scanner;

/**
 * Customer Main Menu Controller
 * Handles customer portal navigation
 * Implements SOLID principles: SRP (customer menu management),
 * OCP (extensible menu options)
 */
public class CustomerMainMenuController {

    public static void launch(Scanner scanner) {
        while (true) {
            ConsoleUtils.clearScreen();
            System.out.println("===========================================");
            System.out.println("    SYOS - CUSTOMER PORTAL");
            System.out.println("===========================================");
            System.out.println("1. Online Sales Portal");
            System.out.println("0. Exit");
            System.out.println("===========================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> OnlineMainMenuController.launch(scanner);
                case "0" -> {
                    return; // Go back to user type selection
                }
                default -> {
                    System.out.println("Invalid option. Please try again.");
                    System.out.print("Press Enter to continue...");
                    scanner.nextLine();
                }
            }
        }
    }
}
