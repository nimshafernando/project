package syos.util;

import syos.model.Bill;
import syos.model.CartItem;

public class BillPrinter {

    public static void print(Bill bill) {
        System.out.println("\n===== SYOS BILL =====");
        System.out.println("Bill No: " + bill.getSerialNumber());
        System.out.println("Date: " + bill.getDate());
        System.out.println("----------------------");

        for (CartItem item : bill.getItems()) {
            System.out.printf("%s x %d = Rs. %.2f\n",
                    item.getItem().getName(),
                    item.getQuantity(),
                    item.getTotalPrice());
        }

        System.out.println("----------------------");
        System.out.printf("Total: Rs. %.2f\n", bill.getTotal());
        System.out.printf("Discount: Rs. %.2f\n", bill.getDiscount());
        System.out.printf("Cash Tendered: Rs. %.2f\n", bill.getCashTendered());
        System.out.printf("Change: Rs. %.2f\n", bill.getChange());
        System.out.println("======================");
    }
}
