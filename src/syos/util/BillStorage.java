package syos.util;

import syos.model.Bill;
import syos.model.CartItem;

import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BillStorage {

    public static String getFormattedSerial(Bill bill) {
        return String.format("#%d-%s", bill.getSerialNumber(), bill.getDate().toLocalDate());
    }

    public static void save(Bill bill) {
        String folderPath = "bills/store";
        File dir = new File(folderPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String serialTag = getFormattedSerial(bill);
        String filename = String.format("%s/bill_%s.txt", folderPath, serialTag);

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            writer.println("=======================================");
            writer.println("              SYOS GROCERY             ");
            writer.println("=======================================");
            writer.printf("Bill : %s%n", serialTag);
            writer.println("Time   : " + timeFormatter.format(bill.getDate()));

            if (bill.getEmployeeName() != null) {
                writer.println("Employee: " + bill.getEmployeeName());
            }

            writer.println("---------------------------------------");
            writer.printf("%-20s %5s %10s%n", "Item", "Qty", "Total");
            writer.println("---------------------------------------");

            for (CartItem item : bill.getItems()) {
                writer.printf("%-20s %5d %10s%n",
                        item.getItem().getName(),
                        item.getQuantity(),
                        df.format(item.getTotalPrice()));
            }

            writer.println("---------------------------------------");
            double totalBeforeDiscount = bill.getTotal() + bill.getDiscount();
            writer.printf("%-26s %10s%n", "Total before discount:", df.format(totalBeforeDiscount));
            writer.printf("%-26s %10s%n", "Discount applied:", df.format(bill.getDiscount()));
            writer.printf("%-26s %10s%n", "Total after discount:", df.format(bill.getTotal()));
            writer.printf("%-26s %10s%n", "Cash Tendered:", df.format(bill.getCashTendered()));
            writer.printf("%-26s %10s%n", "Change Returned:", df.format(bill.getChange()));
            writer.println("=======================================");
            writer.println("        Thank you for shopping!        ");
            writer.println("=======================================");

        } catch (IOException e) {
            System.out.println("Failed to save bill to file.");
            e.printStackTrace();
        }
    }

    public static int getNextSerialForToday(LocalDate today) {
        return getNextSerialForToday(today, false); // in-store default
    }

    public static int getNextSerialForToday(LocalDate today, boolean isOnline) {
        String fileName = isOnline ? "online_bill_serial.txt" : "bill_serial.txt";
        File tracker = new File(fileName);
        String todayStr = today.toString();
        int serial = 1;

        try {
            if (tracker.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(tracker))) {
                    String dateLine = reader.readLine();
                    String serialLine = reader.readLine();

                    if (dateLine != null && dateLine.equals(todayStr) && serialLine != null) {
                        serial = Integer.parseInt(serialLine.trim()) + 1;
                    }
                }
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(tracker))) {
                writer.println(todayStr);
                writer.println(serial);
            }

        } catch (IOException | NumberFormatException e) {
            System.err.println(
                    "Error handling " + (isOnline ? "online" : "in-store") + " bill serial: " + e.getMessage());
            serial = 1;
        }

        return serial;
    }

    public static void debugSerialStatus() {
        debugSerialStatus(false);
    }

    public static void debugSerialStatus(boolean isOnline) {
        String fileName = isOnline ? "online_bill_serial.txt" : "bill_serial.txt";
        String type = isOnline ? "Online" : "In-store";
        File tracker = new File(fileName);

        if (tracker.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(tracker))) {
                String dateLine = reader.readLine();
                String serialLine = reader.readLine();
                System.out.println(type + " serial file - Date: " + dateLine + ", Serial: " + serialLine);
                System.out.println("Today: " + LocalDate.now());
            } catch (IOException e) {
                System.out.println("Error reading " + type.toLowerCase() + " serial file: " + e.getMessage());
            }
        } else {
            System.out.println(type + " serial file does not exist yet.");
        }
    }
}
