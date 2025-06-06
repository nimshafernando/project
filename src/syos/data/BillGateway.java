package syos.data;

import syos.interfaces.BillDataAccess;
import syos.interfaces.DatabaseConnectionProvider;
import syos.model.Bill;
import syos.model.CartItem;
import syos.util.BillStorage;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * BillGateway implementing BillDataAccess interface
 * Follows SOLID principles for bill data operations
 */
public class BillGateway implements BillDataAccess {

    private final DatabaseConnectionProvider connectionProvider;

    // Constructor injection for DIP compliance
    public BillGateway(DatabaseConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    // Default constructor for backward compatibility
    public BillGateway() {
        this.connectionProvider = DatabaseConnection.getInstance();
    }

    @Override
    public boolean saveBill(Bill bill) {
        try (Connection conn = connectionProvider.getPoolConnection()) {
            if (conn == null)
                throw new SQLException("Connection returned null.");

            // Format bill serial using BillStorage logic
            String serialFormatted = BillStorage.getFormattedSerial(bill);
            LocalDate billDate = bill.getDate().toLocalDate();
            LocalTime billTime = bill.getDate().toLocalTime();

            // Insert bill record
            String billSql = "INSERT INTO bills (bill_serial, date, time, total, discount, cash_tendered, change_due, employee_name) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement billStmt = conn.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS);
            billStmt.setString(1, serialFormatted);
            billStmt.setDate(2, Date.valueOf(billDate));
            billStmt.setTime(3, Time.valueOf(billTime));
            billStmt.setDouble(4, bill.getTotal());
            billStmt.setDouble(5, bill.getDiscount());
            billStmt.setDouble(6, bill.getCashTendered());
            billStmt.setDouble(7, bill.getChange());
            billStmt.setString(8, bill.getEmployeeName());

            int rowsAffected = billStmt.executeUpdate();
            if (rowsAffected == 0)
                throw new SQLException("Failed to insert bill.");

            ResultSet generatedKeys = billStmt.getGeneratedKeys();
            if (!generatedKeys.next())
                throw new SQLException("Failed to retrieve bill ID.");
            int billId = generatedKeys.getInt(1);

            // Only insert bill items if present
            List<CartItem> items = bill.getItems();
            if (items != null && !items.isEmpty()) {
                String itemSql = "INSERT INTO bill_items (bill_id, item_code, item_name, quantity, price_per_unit, total_price) "
                        + "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement itemStmt = conn.prepareStatement(itemSql);

                for (CartItem item : items) {
                    itemStmt.setInt(1, billId);
                    itemStmt.setString(2, item.getItem().getCode());
                    itemStmt.setString(3, item.getItem().getName());
                    itemStmt.setInt(4, item.getQuantity());
                    itemStmt.setDouble(5, item.getItem().getPrice());
                    itemStmt.setDouble(6, item.getTotalPrice());
                    itemStmt.addBatch();
                }

                itemStmt.executeBatch();
            }

            return true;

        } catch (Exception e) {
            System.out.println("Failed to store bill in MySQL:");
            e.printStackTrace();
            return false;
        }
    }
}
