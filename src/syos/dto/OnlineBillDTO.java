package syos.dto;

import java.time.LocalTime;
import java.util.List;
import syos.model.CartItem;

public class OnlineBillDTO {
    private String serial;
    private String username;
    private double total;
    private double discount;
    private double cash;
    private double change;
    private LocalTime time;
    private List<CartItem> items;

    public OnlineBillDTO(String serial, String username, double total, double discount,
            double cash, double change, LocalTime time, List<CartItem> items) {
        this.serial = serial;
        this.username = username;
        this.total = total;
        this.discount = discount;
        this.cash = cash;
        this.change = change;
        this.time = time;
        this.items = items;
    }

    public String getSerial() {
        return serial;
    }

    public String getUsername() {
        return username;
    }

    public double getTotal() {
        return total;
    }

    public double getDiscount() {
        return discount;
    }

    public double getCash() {
        return cash;
    }

    public double getChange() {
        return change;
    }

    public LocalTime getTime() {
        return time;
    }

    public List<CartItem> getItems() {
        return items;
    }
}
