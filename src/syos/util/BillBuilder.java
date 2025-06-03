package syos.util;

import syos.model.Bill;
import syos.model.CartItem;
import syos.dto.ItemDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Enhanced BillBuilder with comprehensive Builder pattern implementation
 * Provides fluent interface, validation, and multiple build options
 * Implements SOLID principles: SRP (bill construction), OCP (extensible
 * builders),
 * DIP (depends on abstractions), ISP (specific builder interfaces)
 */
public class BillBuilder {
    private int serialNumber;
    private String serial;
    private LocalDateTime date;
    private List<CartItem> items = new ArrayList<>();
    private double total;
    private double discount;
    private double cashTendered;
    private double change;
    private String source;
    private String username;
    private String paymentMethod;

    // Builder state validation
    private boolean hasSerialNumber = false;
    private boolean hasDate = false;
    private boolean hasItems = false;
    private boolean hasTotals = false;

    /**
     * Default constructor with sensible defaults
     */
    public BillBuilder() {
        this.date = LocalDateTime.now();
        this.source = "store";
        this.paymentMethod = "cash";
        this.hasDate = true;
    }

    /**
     * Static factory method for creating builder
     * Provides clean entry point following common Builder pattern practices
     */
    public static BillBuilder newBill() {
        return new BillBuilder();
    }

    /**
     * Static factory for store bills
     */
    public static BillBuilder newStoreBill() {
        return new BillBuilder().setSource("store");
    }

    /**
     * Static factory for online bills
     */
    public static BillBuilder newOnlineBill() {
        return new BillBuilder().setSource("online").setPaymentMethod("online");
    }

    /**
     * Static factory for bill from existing bill (copy builder)
     */
    public static BillBuilder fromBill(Bill existingBill) {
        BillBuilder builder = new BillBuilder();

        if (existingBill.getSerial() != null) {
            builder.setSerialString(existingBill.getSerial());
        } else {
            builder.setSerialNumber(existingBill.getSerialNumber());
        }

        return builder
                .setDate(existingBill.getDate())
                .setItems(new ArrayList<>(existingBill.getItems()))
                .setTotal(existingBill.getTotal())
                .setDiscount(existingBill.getDiscount())
                .setCashTendered(existingBill.getCashTendered())
                .setChange(existingBill.getChange())
                .setSource(existingBill.getSource())
                .setUsername(existingBill.getUsername())
                .setPaymentMethod(existingBill.getPaymentMethod());
    }

    // === Fluent Builder Methods ===

    public BillBuilder setSerialNumber(int serialNumber) {
        if (serialNumber <= 0) {
            throw new IllegalArgumentException("Serial number must be positive");
        }
        this.serialNumber = serialNumber;
        this.hasSerialNumber = true;
        return this;
    }

    public BillBuilder setSerialString(String serialString) {
        if (serialString == null || serialString.trim().isEmpty()) {
            throw new IllegalArgumentException("Serial string cannot be null or empty");
        }
        this.serial = serialString.trim();
        this.hasSerialNumber = true;
        return this;
    }

    public BillBuilder setDate(LocalDateTime date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        this.date = date;
        this.hasDate = true;
        return this;
    }

    public BillBuilder setDateNow() {
        this.date = LocalDateTime.now();
        this.hasDate = true;
        return this;
    }

    public BillBuilder setItems(List<CartItem> items) {
        if (items == null) {
            throw new IllegalArgumentException("Items list cannot be null");
        }
        this.items = new ArrayList<>(items); // Defensive copy
        this.hasItems = !this.items.isEmpty();
        return this;
    }

    public BillBuilder addItem(CartItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Cart item cannot be null");
        }
        this.items.add(item);
        this.hasItems = true;
        return this;
    }

    public BillBuilder addItem(ItemDTO itemDTO, int quantity) {
        if (itemDTO == null || quantity <= 0) {
            throw new IllegalArgumentException("ItemDTO cannot be null and quantity must be positive");
        }
        this.items.add(new CartItem(itemDTO, quantity));
        this.hasItems = true;
        return this;
    }

    public BillBuilder clearItems() {
        this.items.clear();
        this.hasItems = false;
        return this;
    }

    public BillBuilder setTotal(double total) {
        if (total < 0) {
            throw new IllegalArgumentException("Total cannot be negative");
        }
        this.total = total;
        this.hasTotals = true;
        return this;
    }

    public BillBuilder calculateTotal() {
        this.total = items.stream()
                .mapToDouble(item -> item.getItem().getPrice() * item.getQuantity())
                .sum() - discount;
        this.hasTotals = true;
        return this;
    }

    public BillBuilder setDiscount(double discount) {
        if (discount < 0) {
            throw new IllegalArgumentException("Discount cannot be negative");
        }
        this.discount = discount;
        return this;
    }

    public BillBuilder setDiscountPercentage(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
        double subtotal = items.stream()
                .mapToDouble(item -> item.getItem().getPrice() * item.getQuantity())
                .sum();
        this.discount = subtotal * (percentage / 100.0);
        return this;
    }

    public BillBuilder setCashTendered(double cashTendered) {
        if (cashTendered < 0) {
            throw new IllegalArgumentException("Cash tendered cannot be negative");
        }
        this.cashTendered = cashTendered;
        return this;
    }

    public BillBuilder setChange(double change) {
        this.change = change;
        return this;
    }

    public BillBuilder calculateChange() {
        this.change = cashTendered - total;
        return this;
    }

    public BillBuilder setSource(String source) {
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Source cannot be null or empty");
        }
        this.source = source.trim();
        return this;
    }

    public BillBuilder setUsername(String username) {
        this.username = username != null ? username.trim() : null;
        return this;
    }

    public BillBuilder setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod != null ? paymentMethod.trim() : "cash";
        return this;
    }

    // === Validation Methods ===

    /**
     * Check if builder has minimum required fields
     */
    public boolean isValid() {
        return hasSerialNumber && hasDate && hasItems && (hasTotals || total > 0) && cashTendered >= total;
    }

    // === Build Methods ===

    /**
     * Build bill with validation
     */
    public Bill build() {
        validateRequiredFields();

        if ("store".equals(source)) {
            // Automatically get employee name from current session
            EmployeeSession session = EmployeeSession.getInstance();
            session.validateSession(); // Throws exception if no one logged in

            String loggedInEmployee = session.getCurrentEmployeeName();

            return new Bill(serialNumber, date, items, total, discount,
                    cashTendered, change, loggedInEmployee);
        } else {
            return new Bill(serial, date, items, total, discount, cashTendered, change);
        }
    }

    /**
     * Build bill without validation (for testing or special cases)
     */
    public Bill buildUnsafe() {
        Bill bill;
        if (serial != null) {
            bill = new Bill(serial, date, items, total, discount, cashTendered, change);
        } else {
            bill = new Bill(serialNumber, date, items, total, discount, cashTendered, change);
        }

        bill.setSource(source);
        bill.setUsername(username);
        bill.setPaymentMethod(paymentMethod);

        return bill;
    }

    /**
     * Build and immediately process bill (convenience method)
     */
    public Bill buildAndProcess() {
        Bill bill = build();
        bill.processBill(); // Uses State pattern from Bill class
        return bill;
    }

    private void validateRequiredFields() {
        if (date == null)
            throw new IllegalStateException("Date is required");
        if (items.isEmpty())
            throw new IllegalStateException("At least one item is required");
        if (total <= 0)
            throw new IllegalStateException("Total must be positive");

        if ("store".equals(source)) {
            EmployeeSession session = EmployeeSession.getInstance();
            if (!session.isLoggedIn()) {
                throw new IllegalStateException(
                        "No employee is currently logged in. Please authenticate through the employee portal.");
            }
        }

        if ("online".equals(source) && (username == null || username.trim().isEmpty())) {
            throw new IllegalStateException("Username is required for online bills");
        }
    }

    // === Utility Methods ===

    /**
     * Reset builder to initial state
     */
    public BillBuilder reset() {
        this.serialNumber = 0;
        this.serial = null;
        this.date = LocalDateTime.now();
        this.items.clear();
        this.total = 0;
        this.discount = 0;
        this.cashTendered = 0;
        this.change = 0;
        this.source = "store";
        this.username = null;
        this.paymentMethod = "cash";

        this.hasSerialNumber = false;
        this.hasDate = true;
        this.hasItems = false;
        this.hasTotals = false;

        return this;
    }

    /**
     * Get summary of current builder state
     */
    public String getSummary() {
        return String.format(
                "BillBuilder[serial=%s, items=%d, total=%.2f, valid=%s]",
                serial != null ? serial : String.valueOf(serialNumber),
                items.size(),
                total,
                isValid());
    }
}
