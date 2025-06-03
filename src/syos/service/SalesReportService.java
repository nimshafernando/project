package syos.service;

import syos.dto.ReportItemDTO;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class SalesReportService {

    // Store types used in report filtering
    public enum StoreType {
        IN_STORE, ONLINE, COMBINED
    }

    // Transaction types used in report filtering
    public enum TransactionType {
        IN_STORE,
        RESERVATION_PAY_IN_STORE,
        RESERVATION_COD,
        RESERVATION,
        ALL
    }

    // Main method to fetch report results
    public List<ReportItemDTO> getSalesReport(LocalDate date, StoreType storeType, TransactionType transactionType) {
        Map<String, ReportItemDTO> reportMap = new LinkedHashMap<>();

        if (storeType == StoreType.IN_STORE || storeType == StoreType.COMBINED) {
            if (transactionType == TransactionType.IN_STORE || transactionType == TransactionType.ALL) {
                fetchInStoreSales(date, reportMap);
            }
        }

        if (storeType == StoreType.ONLINE || storeType == StoreType.COMBINED) {
            fetchOnlineSales(date, transactionType, reportMap);
        }

        return new ArrayList<>(reportMap.values());
    }

    // Fetches in-store sales based on the bill_items table
    private void fetchInStoreSales(LocalDate date, Map<String, ReportItemDTO> reportMap) {
        String sql = """
                    SELECT bi.item_code, bi.item_name,
                           SUM(bi.quantity) AS total_qty,
                           SUM(bi.quantity * bi.price_per_unit) AS total_revenue
                    FROM bills b
                    JOIN bill_items bi ON b.id = bi.bill_id
                    WHERE DATE(b.time) = ?
                    GROUP BY bi.item_code, bi.item_name
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String itemName = rs.getString("item_name");
                // Append store type for combined reports
                String displayName = itemName + " (In-Store)";

                addOrMerge(reportMap, new ReportItemDTO(
                        rs.getString("item_code"),
                        displayName,
                        rs.getInt("total_qty"),
                        rs.getDouble("total_revenue")));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching in-store bill data: " + e.getMessage());
        }
    }

    // Fetches online sales based on the online_bill_items + online_inventory
    private void fetchOnlineSales(LocalDate date, TransactionType type, Map<String, ReportItemDTO> reportMap) {
        StringBuilder sql = new StringBuilder("""
                    SELECT obi.item_code, oi.name,
                           SUM(obi.quantity) AS total_qty,
                           SUM(obi.quantity * obi.price) AS total_revenue
                    FROM online_bills b
                    JOIN online_bill_items obi ON b.id = obi.bill_id
                    JOIN online_inventory oi ON obi.item_code = oi.item_code
                    WHERE b.date = ?
                """);

        // Filter based on transaction type
        switch (type) {
            case RESERVATION_PAY_IN_STORE -> sql.append(" AND b.payment_method = 'Pay in Store'");
            case RESERVATION_COD -> sql.append(" AND b.payment_method = 'Cash on Delivery'");
            case RESERVATION -> sql.append(" AND b.payment_method IN ('Pay in Store', 'Cash on Delivery')");
            case IN_STORE -> {
                return; // skip, not applicable for online
            }
            case ALL -> {
                // no filter
            }
        }

        sql.append(" GROUP BY obi.item_code, oi.name");

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String itemName = rs.getString("name");
                // Append store type for combined reports
                String displayName = itemName + " (Online)";

                addOrMerge(reportMap, new ReportItemDTO(
                        rs.getString("item_code"),
                        displayName,
                        rs.getInt("total_qty"),
                        rs.getDouble("total_revenue")));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching online bill data: " + e.getMessage());
        }
    }

    // Merges quantities and revenue of duplicate items
    private void addOrMerge(Map<String, ReportItemDTO> map, ReportItemDTO newItem) {
        map.merge(newItem.getCode(), newItem, ReportItemDTO::merge);
    }
}
