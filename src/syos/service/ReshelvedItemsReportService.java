// syos/service/ReshelvedItemsReportService.java
package syos.service;

import syos.dto.ReshelvedItemDTO;
import syos.interfaces.IReportService;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data Gateway & SRP: retrieves items reshelved from batches.
 * Implements IReportService interface for consistent reporting interface.
 */
public class ReshelvedItemsReportService implements IReportService<ReshelvedItemDTO> {
    /**
     * Fetches total quantity per item reshelved from batches on the given date.
     * These are the items that were moved from batch storage to store shelves.
     */
    public List<ReshelvedItemDTO> getReshelvedItemsForInStore(LocalDate date) {
        // Updated SQL to query reshelved_log table instead of bills
        String sql = """
                    SELECT rl.item_code,
                           i.name,
                           SUM(rl.quantity) AS qty
                      FROM reshelved_log rl
                      JOIN items i ON rl.item_code = i.code
                     WHERE DATE(rl.reshelved_at) = ?
                       AND rl.store_type = 'INSTORE'
                     GROUP BY rl.item_code, i.name
                     ORDER BY i.name
                """;

        List<ReshelvedItemDTO> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ReshelvedItemDTO(
                            rs.getString("item_code"),
                            rs.getString("name"),
                            rs.getInt("qty")));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error fetching reshelved items from log: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Error getting database connection: " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Fetches total quantity per item reshelved from batches within a date range
     * for in-store.
     */
    public List<ReshelvedItemDTO> getReshelvedItemsForInStore(LocalDate startDate, LocalDate endDate) {
        String sql = """
                    SELECT rl.item_code,
                           i.name,
                           SUM(rl.quantity) AS qty
                      FROM reshelved_log rl
                      JOIN items i ON rl.item_code = i.code
                     WHERE DATE(rl.reshelved_at) BETWEEN ? AND ?
                       AND rl.store_type = 'INSTORE'
                     GROUP BY rl.item_code, i.name
                     ORDER BY i.name
                """;
        List<ReshelvedItemDTO> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ReshelvedItemDTO(
                            rs.getString("item_code"),
                            rs.getString("name"),
                            rs.getInt("qty")));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error fetching reshelved items from log: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Error getting database connection: " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Fetches total quantity per item reshelved from batches for online store on
     * the given date.
     */
    public List<ReshelvedItemDTO> getReshelvedItemsForOnline(LocalDate date) {
        String sql = """
                    SELECT rl.item_code,
                           oi.name,
                           SUM(rl.quantity) AS qty
                      FROM reshelved_log rl
                      JOIN online_inventory oi ON rl.item_code = oi.item_code
                     WHERE DATE(rl.reshelved_at) = ?
                       AND rl.store_type = 'ONLINE'
                     GROUP BY rl.item_code, oi.name                     ORDER BY oi.name
                """;

        List<ReshelvedItemDTO> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ReshelvedItemDTO(
                            rs.getString("item_code"),
                            rs.getString("name"),
                            rs.getInt("qty")));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error fetching online reshelved items from log: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Error getting database connection: " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Fetches total quantity per item reshelved from batches for online store
     * within a date range.
     */
    public List<ReshelvedItemDTO> getReshelvedItemsForOnline(LocalDate startDate, LocalDate endDate) {
        String sql = """
                    SELECT rl.item_code,
                           oi.name,
                           SUM(rl.quantity) AS qty
                      FROM reshelved_log rl
                      JOIN online_inventory oi ON rl.item_code = oi.item_code
                     WHERE DATE(rl.reshelved_at) BETWEEN ? AND ?
                       AND rl.store_type = 'ONLINE'
                     GROUP BY rl.item_code, oi.name
                     ORDER BY oi.name
                """;

        List<ReshelvedItemDTO> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ReshelvedItemDTO(
                            rs.getString("item_code"),
                            rs.getString("name"),
                            rs.getInt("qty")));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error fetching online reshelved items from log: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Error getting database connection: " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Get all reshelved items (both in-store and online) for a given date
     */
    public List<ReshelvedItemDTO> getAllReshelvedItems(LocalDate date) {
        String sql = """
                    SELECT rl.item_code,
                           CASE
                               WHEN rl.store_type = 'INSTORE' THEN i.name
                               WHEN rl.store_type = 'ONLINE' THEN oi.name
                               ELSE COALESCE(i.name, oi.name)
                           END as name,
                           SUM(rl.quantity) AS qty,
                           rl.store_type
                      FROM reshelved_log rl
                      LEFT JOIN items i ON rl.item_code = i.code AND rl.store_type = 'INSTORE'
                      LEFT JOIN online_inventory oi ON rl.item_code = oi.item_code AND rl.store_type = 'ONLINE'
                     WHERE DATE(rl.reshelved_at) = ?
                     GROUP BY rl.item_code, rl.store_type, CASE
                                                              WHEN rl.store_type = 'INSTORE' THEN i.name
                                                              WHEN rl.store_type = 'ONLINE' THEN oi.name
                                                              ELSE COALESCE(i.name, oi.name)
                                                          END
                     ORDER BY rl.store_type, CASE
                                                 WHEN rl.store_type = 'INSTORE' THEN i.name
                                                 WHEN rl.store_type = 'ONLINE' THEN oi.name
                                                 ELSE COALESCE(i.name, oi.name)
                                             END
                """;

        List<ReshelvedItemDTO> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String storeType = rs.getString("store_type");
                    String storeLabel = "INSTORE".equals(storeType) ? "In-Store" : "Online";
                    String itemName = rs.getString("name") + " (" + storeLabel + ")";
                    list.add(new ReshelvedItemDTO(
                            rs.getString("item_code"),
                            itemName,
                            rs.getInt("qty")));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error fetching all reshelved items from log: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Error getting database connection: " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Get all reshelved items (both in-store and online) within a date range
     */
    public List<ReshelvedItemDTO> getAllReshelvedItemsRange(LocalDate startDate, LocalDate endDate) {
        String sql = """
                    SELECT rl.item_code,
                           CASE
                               WHEN rl.store_type = 'INSTORE' THEN i.name
                               WHEN rl.store_type = 'ONLINE' THEN oi.name
                               ELSE COALESCE(i.name, oi.name)
                           END as name,
                           SUM(rl.quantity) AS qty,
                           rl.store_type
                      FROM reshelved_log rl
                      LEFT JOIN items i ON rl.item_code = i.code AND rl.store_type = 'INSTORE'
                      LEFT JOIN online_inventory oi ON rl.item_code = oi.item_code AND rl.store_type = 'ONLINE'
                     WHERE DATE(rl.reshelved_at) BETWEEN ? AND ?
                     GROUP BY rl.item_code, rl.store_type, CASE
                                                              WHEN rl.store_type = 'INSTORE' THEN i.name
                                                              WHEN rl.store_type = 'ONLINE' THEN oi.name
                                                              ELSE COALESCE(i.name, oi.name)
                                                          END
                     ORDER BY rl.store_type, CASE
                                                 WHEN rl.store_type = 'INSTORE' THEN i.name
                                                 WHEN rl.store_type = 'ONLINE' THEN oi.name
                                                 ELSE COALESCE(i.name, oi.name)
                                             END
                """;

        List<ReshelvedItemDTO> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String storeType = rs.getString("store_type");
                    String storeLabel = "INSTORE".equals(storeType) ? "In-Store" : "Online";
                    String itemName = rs.getString("name") + " (" + storeLabel + ")";
                    list.add(new ReshelvedItemDTO(
                            rs.getString("item_code"),
                            itemName,
                            rs.getInt("qty")));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error fetching all reshelved items from log: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Error getting database connection: " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Alias methods to match UI expectations
     */
    public List<ReshelvedItemDTO> getReshelvedItemsForInStoreRange(LocalDate startDate, LocalDate endDate) {
        return getReshelvedItemsForInStore(startDate, endDate);
    }

    public List<ReshelvedItemDTO> getReshelvedItemsForOnlineRange(LocalDate startDate, LocalDate endDate) {
        return getReshelvedItemsForOnline(startDate, endDate);
    }

    // IReportService interface implementation

    @Override
    public List<ReshelvedItemDTO> generateReport() {
        return getReshelvedItemsForInStore(LocalDate.now());
    }

    @Override
    public List<ReshelvedItemDTO> generateReport(Object... filters) {
        if (filters.length > 0 && filters[0] instanceof LocalDate) {
            LocalDate date = (LocalDate) filters[0];
            return getReshelvedItemsForInStore(date);
        }
        return generateReport();
    }

    @Override
    public String getReportName() {
        return "Reshelved Items Report";
    }

    @Override
    public boolean isDataAvailable() {
        return !generateReport().isEmpty();
    }

    @Override
    public String getReportTitle() {
        return "SYOS RESHELVED ITEMS REPORT";
    }

    @Override
    public List<String> getColumnHeaders() {
        return Arrays.asList("Item Code", "Item Name", "Quantity Reshelved");
    }

    @Override
    public List<List<String>> getReportData() {
        List<List<String>> reportData = new ArrayList<>();
        List<ReshelvedItemDTO> items = generateReport();

        for (ReshelvedItemDTO item : items) {
            reportData.add(Arrays.asList(
                    item.getCode(),
                    item.getName(),
                    String.valueOf(item.getQuantity())));
        }

        return reportData;
    }

    @Override
    public String getFormattedRow(ReshelvedItemDTO item) {
        return String.format("%-10s %-30s %8d",
                item.getCode(),
                truncateName(item.getName(), 30),
                item.getQuantity());
    }

    /**
     * Truncates item names for better table formatting.
     * YAGNI principle: Simple truncation without complex formatting.
     */
    private String truncateName(String name, int maxLength) {
        if (name.length() <= maxLength) {
            return name;
        }
        return name.substring(0, maxLength - 3) + "...";
    }
}
