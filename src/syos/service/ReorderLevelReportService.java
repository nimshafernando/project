package syos.service;

import syos.dto.ReorderItemDTO;
import syos.interfaces.IReportService;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service class responsible for reorder level reporting.
 * Follows SRP - handles only reorder level business logic.
 * Uses Gateway pattern for data access abstraction.
 * Implements IReportService interface for consistent reporting interface.
 */
public class ReorderLevelReportService implements IReportService<ReorderItemDTO> {

    public enum StoreFilter {
        IN_STORE_ONLY, ONLINE_ONLY, BOTH_STORES
    }

    /**
     * Fetches items below reorder level from both in-store and online inventories.
     * Uses custom threshold value.
     */
    public List<ReorderItemDTO> getItemsBelowReorderLevel(int threshold) {
        List<ReorderItemDTO> reorderItems = new ArrayList<>();

        // Fetch from in-store inventory
        reorderItems.addAll(fetchInStoreReorderItems(threshold));

        // Fetch from online inventory
        reorderItems.addAll(fetchOnlineReorderItems(threshold));

        return reorderItems;
    }

    /**
     * Fetches items below reorder level from both inventories using default
     * threshold.
     */
    public List<ReorderItemDTO> getItemsBelowReorderLevel() {
        return getItemsBelowReorderLevel(50); // Default threshold
    }

    /**
     * Fetches items below reorder level from in-store inventory only.
     */
    public List<ReorderItemDTO> getInStoreReorderItems(int threshold) {
        return fetchInStoreReorderItems(threshold);
    }

    /**
     * Fetches items below reorder level from in-store inventory with default
     * threshold.
     */
    public List<ReorderItemDTO> getInStoreReorderItems() {
        return fetchInStoreReorderItems(50);
    }

    /**
     * Fetches items below reorder level from online inventory only.
     */
    public List<ReorderItemDTO> getOnlineReorderItems(int threshold) {
        return fetchOnlineReorderItems(threshold);
    }

    /**
     * Fetches items below reorder level from online inventory with default
     * threshold.
     */
    public List<ReorderItemDTO> getOnlineReorderItems() {
        return fetchOnlineReorderItems(50);
    }

    private List<ReorderItemDTO> fetchInStoreReorderItems(int threshold) {
        List<ReorderItemDTO> items = new ArrayList<>();
        String sql = """
                SELECT code, name, price, quantity
                FROM items
                WHERE quantity <= ?
                ORDER BY quantity ASC, name
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, threshold);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int currentStock = rs.getInt("quantity");
                items.add(new ReorderItemDTO(
                        rs.getString("code"),
                        rs.getString("name"),
                        currentStock,
                        threshold, // Use threshold as reorder level
                        rs.getDouble("price"),
                        "IN_STORE"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching in-store reorder items: " + e.getMessage(), e);
        }

        return items;
    }

    private List<ReorderItemDTO> fetchOnlineReorderItems(int threshold) {
        List<ReorderItemDTO> items = new ArrayList<>();
        String sql = """
                SELECT item_code, name, price, quantity
                FROM online_inventory
                WHERE quantity <= ?
                ORDER BY quantity ASC, name
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, threshold);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int currentStock = rs.getInt("quantity");
                items.add(new ReorderItemDTO(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        currentStock,
                        threshold, // Use threshold as reorder level
                        rs.getDouble("price"),
                        "ONLINE"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching online reorder items: " + e.getMessage(), e);
        }
        return items;
    }

    // IReportService interface implementation

    @Override
    public List<ReorderItemDTO> generateReport() {
        return getItemsBelowReorderLevel();
    }

    @Override
    public List<ReorderItemDTO> generateReport(Object... filters) {
        if (filters.length > 0 && filters[0] instanceof StoreFilter) {
            StoreFilter storeFilter = (StoreFilter) filters[0];
            return switch (storeFilter) {
                case IN_STORE_ONLY -> getInStoreReorderItems();
                case ONLINE_ONLY -> getOnlineReorderItems();
                case BOTH_STORES -> getItemsBelowReorderLevel();
            };
        }
        return generateReport();
    }

    @Override
    public String getReportName() {
        return "Reorder Level Report";
    }

    @Override
    public boolean isDataAvailable() {
        return !getItemsBelowReorderLevel().isEmpty();
    }

    @Override
    public String getReportTitle() {
        return "SYOS REORDER LEVEL REPORT";
    }

    @Override
    public List<String> getColumnHeaders() {
        // Updated column headers (removed "Reorder Level")
        return Arrays.asList("Code", "Name", "Current Stock", "Shortfall", "Price", "Store Type");
    }

    @Override
    public List<List<String>> getReportData() {
        List<List<String>> reportData = new ArrayList<>();
        List<ReorderItemDTO> items = generateReport();

        for (ReorderItemDTO item : items) {
            // Updated report data (removed reorder level column)
            reportData.add(Arrays.asList(
                    item.getCode(),
                    item.getName(),
                    String.valueOf(item.getCurrentStock()),
                    String.valueOf(item.getShortfall()),
                    String.format("%.2f", item.getPrice()),
                    item.getStoreType()));
        }

        return reportData;
    }

    @Override
    public String getFormattedRow(ReorderItemDTO item) {
        // Updated formatted row (removed reorder level column)
        return String.format("%-10s %-25s %-8d %-8d %-10.2f %-8s",
                item.getCode(),
                truncateName(item.getName(), 25),
                item.getCurrentStock(),
                item.getShortfall(),
                item.getPrice(),
                item.getStoreType());
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
