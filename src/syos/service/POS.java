package syos.service;

import syos.dto.ItemDTO;
import syos.model.Bill;
import syos.model.CartItem;
import syos.util.BillBuilder;
import syos.util.BillStorage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * POS (Point of Sale) service with Command pattern implementation
 * Supports undo operations and transaction history management
 * Implements SOLID principles: SRP (POS operations), OCP (extensible commands),
 * LSP (command substitutability), ISP (segregated interfaces), DIP (command
 * abstractions)
 */
public class POS {
    private Cart cart;
    private final Stack<Command> commandHistory;
    private final Stack<Command> undoHistory;
    private static final int MAX_HISTORY_SIZE = 50; // Prevent memory leaks

    // Command pattern interface for all POS operations
    public interface Command {
        void execute();

        void undo();

        String getDescription();

        LocalDateTime getTimestamp();
    }

    /**
     * Command for adding items to cart
     * Implements Command pattern for undoable operations
     */
    private class AddItemCommand implements Command {
        private final ItemDTO item;
        private final int quantity;
        private final LocalDateTime timestamp;

        public AddItemCommand(ItemDTO item, int quantity) {
            this.item = item;
            this.quantity = quantity;
            this.timestamp = LocalDateTime.now();
        }

        @Override
        public void execute() {
            cart.addItem(item, quantity);
        }

        @Override
        public void undo() {
            removeFromCartInternal(item.getCode(), quantity);
        }

        @Override
        public String getDescription() {
            return String.format("Added %d x %s to cart", quantity, item.getName());
        }

        @Override
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Command for removing items from cart
     */
    private class RemoveItemCommand implements Command {
        private final String itemCode;
        private final int quantity;
        private final LocalDateTime timestamp;
        private ItemDTO removedItem; // Store for undo

        public RemoveItemCommand(String itemCode, int quantity) {
            this.itemCode = itemCode;
            this.quantity = quantity;
            this.timestamp = LocalDateTime.now();
        }

        @Override
        public void execute() {
            // Store item details before removal for undo
            Optional<CartItem> cartItem = cart.getItems().stream()
                    .filter(item -> item.getItem().getCode().equals(itemCode))
                    .findFirst();

            if (cartItem.isPresent()) {
                removedItem = cartItem.get().getItem();
                removeFromCartInternal(itemCode, quantity);
            }
        }

        @Override
        public void undo() {
            if (removedItem != null) {
                cart.addItem(removedItem, quantity);
            }
        }

        @Override
        public String getDescription() {
            return String.format("Removed %d x %s from cart", quantity,
                    removedItem != null ? removedItem.getName() : itemCode);
        }

        @Override
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Command for clearing cart
     */
    private class ClearCartCommand implements Command {
        private final List<CartItem> previousItems;
        private final LocalDateTime timestamp;

        public ClearCartCommand() {
            this.previousItems = new ArrayList<>(cart.getItems());
            this.timestamp = LocalDateTime.now();
        }

        @Override
        public void execute() {
            cart.getItems().clear();
        }

        @Override
        public void undo() {
            cart.getItems().clear();
            cart.getItems().addAll(previousItems);
        }

        @Override
        public String getDescription() {
            return String.format("Cleared cart (%d items)", previousItems.size());
        }

        @Override
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    public POS() {
        cart = new Cart();
        commandHistory = new Stack<>();
        undoHistory = new Stack<>();
    }

    public Cart getCart() {
        return cart;
    }

    /**
     * Add item to cart with Command pattern support
     * 
     * @param item     Item to add
     * @param quantity Quantity to add
     */
    public void addToCart(ItemDTO item, int quantity) {
        if (item == null || quantity <= 0) {
            throw new IllegalArgumentException("Item cannot be null and quantity must be positive");
        }

        Command command = new AddItemCommand(item, quantity);
        executeCommand(command);
    }

    public double getCartTotal() {
        return cart.getTotal();
    }

    /**
     * Get cart items for external access
     */
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cart.getItems()); // Return defensive copy
    }

    /**
     * Remove specific quantity of an item from cart with Command pattern
     * 
     * @param itemCode Code of item to remove
     * @param quantity Quantity to remove
     */
    public void removeFromCart(String itemCode, int quantity) {
        if (itemCode == null || itemCode.trim().isEmpty() || quantity <= 0) {
            throw new IllegalArgumentException("Item code cannot be null/empty and quantity must be positive");
        }

        Command command = new RemoveItemCommand(itemCode, quantity);
        executeCommand(command);
    }

    /**
     * Internal method for actual cart item removal
     * Used by commands to avoid recursive command creation
     */
    private void removeFromCartInternal(String itemCode, int quantity) {
        List<CartItem> items = cart.getItems();
        items.removeIf(cartItem -> cartItem.getItem().getCode().equals(itemCode) &&
                cartItem.getQuantity() <= quantity);
    }

    /**
     * Clear all items from cart with Command pattern
     */
    public void clearCart() {
        Command command = new ClearCartCommand();
        executeCommand(command);
    }

    /**
     * Execute command and add to history
     * Implements Command pattern execution with history management
     */
    private void executeCommand(Command command) {
        command.execute();
        commandHistory.push(command);
        undoHistory.clear(); // Clear redo history when new command is executed

        // Limit history size to prevent memory issues (YAGNI principle)
        if (commandHistory.size() > MAX_HISTORY_SIZE) {
            commandHistory.remove(0);
        }
    }

    /**
     * Undo last operation
     * 
     * @return true if undo was successful, false if no operations to undo
     */
    public boolean undoLastOperation() {
        if (commandHistory.isEmpty()) {
            return false;
        }

        Command lastCommand = commandHistory.pop();
        lastCommand.undo();
        undoHistory.push(lastCommand);
        return true;
    }

    /**
     * Redo last undone operation
     * 
     * @return true if redo was successful, false if no operations to redo
     */
    public boolean redoLastOperation() {
        if (undoHistory.isEmpty()) {
            return false;
        }

        Command command = undoHistory.pop();
        command.execute();
        commandHistory.push(command);
        return true;
    }

    /**
     * Get command history for audit trail
     * 
     * @return List of command descriptions with timestamps
     */
    public List<String> getCommandHistory() {
        return commandHistory.stream()
                .map(cmd -> String.format("[%s] %s", cmd.getTimestamp(), cmd.getDescription()))
                .toList();
    }

    /**
     * Check if undo is possible
     */
    public boolean canUndo() {
        return !commandHistory.isEmpty();
    }

    /**
     * Check if redo is possible
     */
    public boolean canRedo() {
        return !undoHistory.isEmpty();
    }

    public Bill checkout(double cashTendered, double discount) {
        double totalBeforeDiscount = cart.getTotal();
        double totalAfterDiscount = totalBeforeDiscount - discount;

        if (totalAfterDiscount < 0) {
            throw new IllegalArgumentException("Discount cannot exceed total amount.");
        }

        double change = cashTendered - totalAfterDiscount;

        if (change < 0) {
            throw new IllegalArgumentException("Cash tendered is less than the amount due.");
        }

        // Get the correct daily serial number from BillStorage
        int serial = BillStorage.getNextSerialForToday(LocalDate.now());

        Bill bill = new BillBuilder()
                .setSerialNumber(serial)
                .setDate(LocalDateTime.now())
                .setItems(cart.getItems())
                .setTotal(totalAfterDiscount)
                .setDiscount(discount)
                .setCashTendered(cashTendered)
                .setChange(change)
                .build();

        // Clear cart after successful checkout
        clearCart();

        return bill;
    }
}
