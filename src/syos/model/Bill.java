package syos.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Bill model with State pattern implementation
 * Manages bill lifecycle states and their corresponding behaviors
 * Implements SOLID principles: SRP (bill data and state management),
 * OCP (extensible states), LSP (state substitutability), ISP (state-specific
 * interfaces),
 * DIP (depends on state abstractions)
 */
public class Bill {
    private int id;
    private int serialNumber;
    private String serial;
    private LocalDateTime date;
    private List<CartItem> items;
    private double total;
    private double discount;
    private double cashTendered;
    private double change;
    private String source;
    private String username; // for online bills
    private String paymentMethod;
    private String employeeName; // Add this field

    // State pattern implementation
    private BillState currentState;
    private LocalDateTime lastStateChange;
    private String stateChangeReason;

    /**
     * State pattern interface for bill lifecycle management
     */
    public interface BillState {
        void process(Bill bill);

        void refund(Bill bill);

        void cancel(Bill bill);

        boolean canModify();

        boolean canRefund();

        boolean canCancel();

        String getStateName();

        String getStateDescription();
    }

    /**
     * Created state - bill just created, can be modified
     */
    public static class CreatedState implements BillState {
        @Override
        public void process(Bill bill) {
            bill.setState(new ProcessedState(), "Bill processed and finalized");
        }

        @Override
        public void refund(Bill bill) {
            throw new IllegalStateException("Cannot refund a bill that hasn't been processed");
        }

        @Override
        public void cancel(Bill bill) {
            bill.setState(new CancelledState(), "Bill cancelled before processing");
        }

        @Override
        public boolean canModify() {
            return true;
        }

        @Override
        public boolean canRefund() {
            return false;
        }

        @Override
        public boolean canCancel() {
            return true;
        }

        @Override
        public String getStateName() {
            return "CREATED";
        }

        @Override
        public String getStateDescription() {
            return "Bill created and pending processing";
        }
    }

    /**
     * Processed state - bill completed, can be refunded
     */
    public static class ProcessedState implements BillState {
        @Override
        public void process(Bill bill) {
            throw new IllegalStateException("Bill is already processed");
        }

        @Override
        public void refund(Bill bill) {
            bill.setState(new RefundedState(), "Bill refunded to customer");
        }

        @Override
        public void cancel(Bill bill) {
            throw new IllegalStateException("Cannot cancel a processed bill, use refund instead");
        }

        @Override
        public boolean canModify() {
            return false;
        }

        @Override
        public boolean canRefund() {
            return true;
        }

        @Override
        public boolean canCancel() {
            return false;
        }

        @Override
        public String getStateName() {
            return "PROCESSED";
        }

        @Override
        public String getStateDescription() {
            return "Bill processed and payment completed";
        }
    }

    /**
     * Refunded state - bill has been refunded, final state
     */
    public static class RefundedState implements BillState {
        @Override
        public void process(Bill bill) {
            throw new IllegalStateException("Cannot process a refunded bill");
        }

        @Override
        public void refund(Bill bill) {
            throw new IllegalStateException("Bill is already refunded");
        }

        @Override
        public void cancel(Bill bill) {
            throw new IllegalStateException("Cannot cancel a refunded bill");
        }

        @Override
        public boolean canModify() {
            return false;
        }

        @Override
        public boolean canRefund() {
            return false;
        }

        @Override
        public boolean canCancel() {
            return false;
        }

        @Override
        public String getStateName() {
            return "REFUNDED";
        }

        @Override
        public String getStateDescription() {
            return "Bill refunded to customer";
        }
    }

    /**
     * Cancelled state - bill cancelled before processing, final state
     */
    public static class CancelledState implements BillState {
        @Override
        public void process(Bill bill) {
            throw new IllegalStateException("Cannot process a cancelled bill");
        }

        @Override
        public void refund(Bill bill) {
            throw new IllegalStateException("Cannot refund a cancelled bill");
        }

        @Override
        public void cancel(Bill bill) {
            throw new IllegalStateException("Bill is already cancelled");
        }

        @Override
        public boolean canModify() {
            return false;
        }

        @Override
        public boolean canRefund() {
            return false;
        }

        @Override
        public boolean canCancel() {
            return false;
        }

        @Override
        public String getStateName() {
            return "CANCELLED";
        }

        @Override
        public String getStateDescription() {
            return "Bill cancelled before processing";
        }
    }

    // === Constructors ===

    /**
     * Constructor for in-store bills
     */
    public Bill(int serialNumber, LocalDateTime date, List<CartItem> items, double total, double discount,
            double cashTendered, double change) {
        this.serialNumber = serialNumber;
        this.date = date;
        this.items = items;
        this.total = total;
        this.discount = discount;
        this.cashTendered = cashTendered;
        this.change = change;
        this.source = "store";

        // Initialize with Created state
        this.currentState = new CreatedState();
        this.lastStateChange = LocalDateTime.now();
        this.stateChangeReason = "Bill created";
    }

    /**
     * Constructor for online bills with string serial
     */
    public Bill(String serial, LocalDateTime date, List<CartItem> items, double total, double discount,
            double cashTendered, double change) {
        this.serial = serial;
        this.date = date;
        this.items = items;
        this.total = total;
        this.discount = discount;
        this.cashTendered = cashTendered;
        this.change = change;
        this.source = "online";

        // Initialize with Created state
        this.currentState = new CreatedState();
        this.lastStateChange = LocalDateTime.now();
        this.stateChangeReason = "Bill created";
    }

    /**
     * Empty constructor for flexibility
     */
    public Bill() {
        this.currentState = new CreatedState();
        this.lastStateChange = LocalDateTime.now();
        this.stateChangeReason = "Bill initialized";
    }

    /**
     * Constructor for in-store bills with employee name
     */
    public Bill(int serialNumber, LocalDateTime date, List<CartItem> items, double total, double discount,
            double cashTendered, double change, String employeeName) {
        this.serialNumber = serialNumber;
        this.date = date;
        this.items = items;
        this.total = total;
        this.discount = discount;
        this.cashTendered = cashTendered;
        this.change = change;
        this.employeeName = employeeName;
        this.source = "store";

        // Initialize with Created state
        this.currentState = new CreatedState();
        this.lastStateChange = LocalDateTime.now();
        this.stateChangeReason = "Bill created";
    }

    // === State Management Methods ===

    /**
     * Internal method to change state
     */
    private void setState(BillState newState, String reason) {
        this.currentState = newState;
        this.lastStateChange = LocalDateTime.now();
        this.stateChangeReason = reason;
    }

    /**
     * Process the bill (moves to processed state)
     */
    public void processBill() {
        currentState.process(this);
    }

    /**
     * Refund the bill (moves to refunded state)
     */
    public void refundBill() {
        currentState.refund(this);
    }

    /**
     * Cancel the bill (moves to cancelled state)
     */
    public void cancelBill() {
        currentState.cancel(this);
    }

    /**
     * Get current state information
     */
    public String getCurrentStateName() {
        return currentState.getStateName();
    }

    public String getCurrentStateDescription() {
        return currentState.getStateDescription();
    }

    public LocalDateTime getLastStateChange() {
        return lastStateChange;
    }

    public String getStateChangeReason() {
        return stateChangeReason;
    }

    /**
     * Check if bill can be modified
     */
    public boolean canModify() {
        return currentState.canModify();
    }

    /**
     * Check if bill can be refunded
     */
    public boolean canRefund() {
        return currentState.canRefund();
    }

    /**
     * Check if bill can be cancelled
     */
    public boolean canCancel() {
        return currentState.canCancel();
    }

    /**
     * Override setters to respect state constraints
     */
    public void setTotal(double total) {
        if (!canModify()) {
            throw new IllegalStateException("Cannot modify bill in " + getCurrentStateName() + " state");
        }
        this.total = total;
    }

    public void setItems(List<CartItem> items) {
        if (!canModify()) {
            throw new IllegalStateException("Cannot modify bill in " + getCurrentStateName() + " state");
        }
        this.items = items;
    }

    public void setDiscount(double discount) {
        if (!canModify()) {
            throw new IllegalStateException("Cannot modify bill in " + getCurrentStateName() + " state");
        }
        this.discount = discount;
    }

    // === Standard Getters and Setters ===
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotal() {
        return total;
    }

    public double getDiscount() {
        return discount;
    }

    public double getCashTendered() {
        return cashTendered;
    }

    public void setCashTendered(double cashTendered) {
        this.cashTendered = cashTendered;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Add getter and setter for employeeName
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
}
