package syos.util;

import syos.model.Bill;
import syos.model.CartItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class OnlineBillPrinter {

    public static void print(Bill bill, String paymentMethod, String username) {
        String folderPath = "bills/online";
        File dir = new File(folderPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filename = String.format("%s/OnlineBill_%s.txt", folderPath, BillStorage.getFormattedSerial(bill));

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("==============================================================\n");
            writer.write("                      SYOS ONLINE ORDER BILL                  \n");
            writer.write("==============================================================\n");
            writer.write("Serial No    : " + BillStorage.getFormattedSerial(bill) + "\n");
            writer.write("Username     : " + username + "\n");
            writer.write("Date/Time    : " + bill.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    + "\n");
            writer.write("Payment Mode : " + paymentMethod.toUpperCase() + "\n");
            writer.write("--------------------------------------------------------------\n");
            writer.write(String.format("%-10s %-35s %5s %7s %9s\n", "Code", "Name", "Qty", "Price", "Subtotal"));
            writer.write("--------------------------------------------------------------\n");

            double total = 0;
            for (CartItem item : bill.getItems()) {
                double subtotal = item.getQuantity() * item.getItem().getPrice();
                total += subtotal;
                writer.write(String.format("%-10s %-35s %5d %7.2f %9.2f\n",
                        item.getItem().getCode(),
                        item.getItem().getName(),
                        item.getQuantity(),
                        item.getItem().getPrice(),
                        subtotal));
            }

            writer.write("--------------------------------------------------------------\n");
            writer.write(String.format("%52s TOTAL : Rs. %,10.2f\n", "", total));
            writer.write("--------------------------------------------------------------\n");
            writer.write("                 THIS IS A RESERVATION SLIP                   \n");
            writer.write("         Please present it when collecting your items.        \n");
            writer.write("==============================================================\n");

        } catch (IOException e) {
            System.out.println("ERROR: Failed to write online bill to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
