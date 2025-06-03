package syos.service;

import syos.data.OnlineBillGateway;
import syos.data.OnlineItemGateway;
import syos.dto.ItemDTO;
import syos.model.Bill;
import syos.model.CartItem;
import syos.model.OnlineUser;
import syos.util.BillStorage;
import syos.util.OnlineBillPrinter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OnlinePOS {
    private List<CartItem> cartItems;
    private OnlineItemGateway itemGateway;

    public OnlinePOS() {
        this.cartItems = new ArrayList<>();
        this.itemGateway = new OnlineItemGateway();
    }

    public void addToCart(ItemDTO item, int quantity) {
        if (item == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid item or quantity.");
        }
        if (quantity > item.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock for item: " + item.getName());
        }

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem cartItem = cartItems.get(i);
            if (cartItem.getItem().getCode().equals(item.getCode())) {
                cartItems.set(i, new CartItem(cartItem.getItem(), cartItem.getQuantity() + quantity));
                return;
            }
        }
        cartItems.add(new CartItem(item, quantity));
    }

    public double getCartTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public Bill checkout(String paymentMethod, OnlineUser user) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User is not logged in.");
            }

            if (cartItems.isEmpty()) {
                throw new IllegalArgumentException("Cart is empty.");
            }

            for (CartItem cartItem : cartItems) {
                ItemDTO currentItem = itemGateway.getItemByCode(cartItem.getItem().getCode());

                if (currentItem == null) {
                    throw new IllegalArgumentException("Item not found: " + cartItem.getItem().getName());
                }

                if (currentItem.getQuantity() < cartItem.getQuantity()) {
                    throw new IllegalArgumentException("Insufficient stock for item: " + cartItem.getItem().getName());
                }
            }

            double total = getCartTotal();
            int serial = BillStorage.getNextSerialForToday(LocalDateTime.now().toLocalDate(), true);

            Bill bill = new Bill(serial, LocalDateTime.now(), new ArrayList<>(cartItems), total, 0.0, 0.0, 0.0);
            bill.setSource("online");
            bill.setUsername(user.getUsername());
            bill.setPaymentMethod(paymentMethod);

            OnlineBillGateway gateway = new OnlineBillGateway();
            boolean saved = gateway.saveOnlineBill(bill, user.getUsername(), paymentMethod);

            if (!saved) {
                throw new RuntimeException("Failed to save online bill to database.");
            }

            boolean stockReduced = itemGateway.reduceStockBatch(cartItems);

            if (!stockReduced) {
                throw new RuntimeException("Failed to reduce stock for items.");
            }

            OnlineBillPrinter.print(bill, paymentMethod, user.getUsername());
            clearCart();

            return bill;

        } catch (Exception e) {
            throw e;
        }
    }

    public List<Bill> getUserBills(String username) {
        try {
            return new OnlineBillGateway().getBillsByUsername(username);
        } catch (Exception e) {
            throw e;
        }
    }

    public Bill getDetailedBill(int billId) {
        try {
            OnlineBillGateway gateway = new OnlineBillGateway();
            List<CartItem> items = gateway.getItemsForBill(billId);

            if (items.isEmpty()) {
                return null;
            }

            Bill bill = new Bill();
            bill.setId(billId);
            bill.setItems(items);

            double total = 0;
            for (CartItem item : items) {
                total += item.getQuantity() * item.getItem().getPrice();
            }
            bill.setTotal(total);

            return bill;

        } catch (Exception e) {
            throw e;
        }
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public void clearCart() {
        cartItems.clear();
    }
}
