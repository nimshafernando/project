package syos.service;

import syos.dto.StockBatchDTO;
import syos.interfaces.IReportService;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service class responsible for stock batch reporting.
 * Follows SRP - handles only stock batch business logic.
 * Uses Gateway pattern for data access abstraction.
 * Implements IReportService interface for consistent reporting interface.
 */
public class StockBatchReportService implements IReportService<StockBatchDTO> {

    public enum BatchFilter {
        ALL, ACTIVE_ONLY, EXPIRING_SOON, EXPIRED, DEPLETED
    }

    /**
     * Fetches all stock batches with optional filtering.
     */
    public List<StockBatchDTO> getStockBatches(BatchFilter filter) {
        List<StockBatchDTO> batches = fetchAllBatches();

        return switch (filter) {
            case ACTIVE_ONLY -> batches.stream()
                    .filter(b -> "ACTIVE".equals(b.getStatus()))
                    .toList();
            case EXPIRING_SOON -> batches.stream()
                    .filter(b -> "EXPIRING_SOON".equals(b.getStatus()))
                    .toList();
            case EXPIRED -> batches.stream()
                    .filter(b -> "EXPIRED".equals(b.getStatus()))
                    .toList();
            case DEPLETED -> batches.stream()
                    .filter(b -> "DEPLETED".equals(b.getStatus()))
                    .toList();
            case ALL -> batches;
        };
    }

    /**
     * Fetches batches for a specific item code.
     */
    public List<StockBatchDTO> getBatchesForItem(String itemCode) {
        return fetchAllBatches().stream()
                .filter(b -> b.getItemCode().equals(itemCode))
                .toList();
    }

    /**
     * Fetches summary statistics for stock batches.
     */
    public BatchSummary getBatchSummary() {
        List<StockBatchDTO> allBatches = fetchAllBatches();

        int totalBatches = allBatches.size();
        int activeBatches = (int) allBatches.stream().filter(b -> "ACTIVE".equals(b.getStatus())).count();
        int expiringSoon = (int) allBatches.stream().filter(b -> "EXPIRING_SOON".equals(b.getStatus())).count();
        int expired = (int) allBatches.stream().filter(b -> "EXPIRED".equals(b.getStatus())).count();
        int depleted = (int) allBatches.stream().filter(b -> "DEPLETED".equals(b.getStatus())).count();

        int totalStock = allBatches.stream().mapToInt(StockBatchDTO::getTotalQuantity).sum();
        int availableStock = allBatches.stream().mapToInt(StockBatchDTO::getAvailableQuantity).sum();
        int usedStock = allBatches.stream().mapToInt(StockBatchDTO::getUsedQuantity).sum();

        return new BatchSummary(totalBatches, activeBatches, expiringSoon, expired, depleted,
                totalStock, availableStock, usedStock);
    }

    private List<StockBatchDTO> fetchAllBatches() {
        List<StockBatchDTO> batches = new ArrayList<>();
        String sql = """
                SELECT item_code, name, quantity, used_quantity,
                       purchase_date, expiry_date, selling_price
                FROM batches
                ORDER BY expiry_date ASC, item_code, purchase_date
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                batches.add(new StockBatchDTO(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getInt("used_quantity"),
                        rs.getDate("purchase_date").toLocalDate(),
                        rs.getDate("expiry_date").toLocalDate(),
                        rs.getDouble("selling_price")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching stock batches: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error getting database connection: " + e.getMessage(), e);
        }

        return batches;
    }

    /**
     * Inner class for batch summary statistics.
     * Follows SRP - encapsulates summary data only.
     */
    public static class BatchSummary {
        private final int totalBatches;
        private final int activeBatches;
        private final int expiringSoon;
        private final int expired;
        private final int depleted;
        private final int totalStock;
        private final int availableStock;
        private final int usedStock;

        public BatchSummary(int totalBatches, int activeBatches, int expiringSoon, int expired,
                int depleted, int totalStock, int availableStock, int usedStock) {
            this.totalBatches = totalBatches;
            this.activeBatches = activeBatches;
            this.expiringSoon = expiringSoon;
            this.expired = expired;
            this.depleted = depleted;
            this.totalStock = totalStock;
            this.availableStock = availableStock;
            this.usedStock = usedStock;
        }

        // Getters
        public int getTotalBatches() {
            return totalBatches;
        }

        public int getActiveBatches() {
            return activeBatches;
        }

        public int getExpiringSoon() {
            return expiringSoon;
        }

        public int getExpired() {
            return expired;
        }

        public int getDepleted() {
            return depleted;
        }

        public int getTotalStock() {
            return totalStock;
        }

        public int getAvailableStock() {
            return availableStock;
        }

        public int getUsedStock() {
            return usedStock;
        }
    }

    // IReportService interface implementation

    @Override
    public List<StockBatchDTO> generateReport() {
        return getStockBatches(BatchFilter.ALL);
    }

    @Override
    public List<StockBatchDTO> generateReport(Object... filters) {
        if (filters.length > 0 && filters[0] instanceof BatchFilter) {
            BatchFilter filter = (BatchFilter) filters[0];
            return getStockBatches(filter);
        }
        return generateReport();
    }

    @Override
    public String getReportName() {
        return "Stock Batch Report";
    }

    @Override
    public boolean isDataAvailable() {
        return !getStockBatches(BatchFilter.ALL).isEmpty();
    }

    @Override
    public String getReportTitle() {
        return "SYOS STOCK BATCH REPORT";
    }

    @Override
    public List<String> getColumnHeaders() {
        return Arrays.asList("Item Code", "Item Name", "Total Qty", "Used Qty", "Available", "Expiry Date", "Status");
    }

    @Override
    public List<List<String>> getReportData() {
        List<List<String>> reportData = new ArrayList<>();
        List<StockBatchDTO> batches = generateReport();

        for (StockBatchDTO batch : batches) {
            reportData.add(Arrays.asList(
                    batch.getItemCode(),
                    batch.getItemName(),
                    String.valueOf(batch.getTotalQuantity()),
                    String.valueOf(batch.getUsedQuantity()),
                    String.valueOf(batch.getAvailableQuantity()),
                    batch.getExpiryDate().toString(),
                    batch.getStatus()));
        }

        return reportData;
    }

    @Override
    public String getFormattedRow(StockBatchDTO batch) {
        return String.format("%-10s %-20s %8d %8d %8d %-12s %-10s",
                batch.getItemCode(),
                truncateName(batch.getItemName(), 20),
                batch.getTotalQuantity(),
                batch.getUsedQuantity(),
                batch.getAvailableQuantity(),
                batch.getExpiryDate().toString(),
                batch.getStatus());
    }

    /**
     * Truncates item names for better table formatting.
     */
    private String truncateName(String name, int maxLength) {
        if (name.length() <= maxLength) {
            return name;
        }
        return name.substring(0, maxLength - 3) + "...";
    }
}
