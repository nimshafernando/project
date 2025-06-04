package syos.data;

import syos.dto.ItemDTO;
import syos.interfaces.OnlineBillDataAccess;
import syos.interfaces.DatabaseConnectionProvider;
import syos.model.Bill;
import syos.model.CartItem;
import syos.util.BillStorage;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OnlineBillGateway implementing OnlineBillDataAccess interface
 * Follows SOLID principles for online bill data operations
 */
public class OnlineBillGateway implements OnlineBillDataAccess {

    private final DatabaseConnectionProvider connectionProvider;

    // Constructor injection for DIP compliance
    public OnlineBillGateway(DatabaseConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    } // Default constructor for backward compatibility

    public OnlineBillGateway() {
        this.connectionProvider = DatabaseConnection.getInstance();
    }

    @Override
    public boolean saveOnlineBill(Bill bill, String username, String paymentMethod) {
        Connection conn = null;

        try {
            conn = connectionProvider.getPoolConnection();
            if (conn == null)
                return false;

            conn.setAutoCommit(false);
            int billId = insertBillAndGetId(conn, bill, username, paymentMethod);
            if (billId <= 0) {
                conn.rollback();
                return false;
            }

            boolean itemsSaved = saveOnlineBillItems(conn, billId, bill);
            if (itemsSaved) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            System.out.println("Error saving online bill: " + e.getMessage());
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private int insertBillAndGetId(Connection conn, Bill bill, String username, String paymentMethod)
            throws Exception {

        String sql = "INSERT INTO online_bills (serial_number, username, time, total, source, payment_method, date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, BillStorage.getFormattedSerial(bill));
            stmt.setString(2, username);
            stmt.setTime(3, Time.valueOf(bill.getDate().toLocalTime())); // time after username
            stmt.setDouble(4, bill.getTotal());
            stmt.setString(5, "online"); // source = 'online'
            stmt.setString(6, paymentMethod);
            stmt.setDate(7, Date.valueOf(bill.getDate().toLocalDate())); // date at the end

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0)
                throw new Exception("Creating bill failed.");

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new Exception("No ID obtained for bill.");
                }
            }
        }
    }

    private boolean saveOnlineBillItems(Connection conn, int billId, Bill bill) throws Exception {
        String sql = "INSERT INTO online_bill_items (bill_id, item_code, quantity, price) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CartItem item : bill.getItems()) {
                stmt.setInt(1, billId);
                stmt.setString(2, item.getItem().getCode());
                stmt.setInt(3, item.getQuantity());
                stmt.setDouble(4, item.getItem().getPrice());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            for (int result : results) {
                if (result == Statement.EXECUTE_FAILED) {
                    return false;
                }
            }

            return results.length == bill.getItems().size();
        }
    }

    @Override
    public List<Bill> getBillsByUsername(String username) {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT id, serial_number, payment_method, total, date FROM online_bills WHERE username = ? ORDER BY date DESC";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Bill bill = new Bill();
                    bill.setId(rs.getInt("id"));
                    bill.setSerial(rs.getString("serial_number").replaceAll("[^0-9]", ""));
                    bill.setPaymentMethod(rs.getString("payment_method"));
                    bill.setTotal(rs.getDouble("total"));
                    bill.setDate(rs.getTimestamp("date").toLocalDateTime());
                    bills.add(bill);
                }
            }

        } catch (Exception e) {
            System.out.println("Error getting bills by username: " + e.getMessage());
        }

        return bills;
    }

    @Override
    public List<CartItem> getItemsForBill(int billId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT obi.item_code, obi.quantity, obi.price, oi.name " +
                "FROM online_bill_items obi " +
                "JOIN online_inventory oi ON obi.item_code = oi.item_code " +
                "WHERE obi.bill_id = ?";

        try (Connection conn = connectionProvider.getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, billId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ItemDTO itemDTO = new ItemDTO(
                            rs.getString("item_code"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"));
                    CartItem cartItem = new CartItem(itemDTO, rs.getInt("quantity"));
                    items.add(cartItem);
                }
            }

        } catch (Exception e) {
            System.out.println("Error getting items for bill: " + e.getMessage());
        }

        return items;
    }
}
