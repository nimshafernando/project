package syos.service;

import syos.dto.BillHistoryDTO;
import syos.interfaces.IReportService;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class responsible for bill history reporting.
 * Follows SRP - handles only bill history business logic.
 * Uses Gateway pattern for data access abstraction.
 */
public class BillHistoryReportService implements IReportService<BillHistoryDTO> {

    /**
     * Enum to filter bill history by store type.
     * Follows Strategy pattern for different filtering approaches.
     */
    public enum StoreFilter {
        IN_STORE, ONLINE, ALL
    }

    /**
     * Enum to filter bill history by date range.
     * Follows Strategy pattern for different time filtering approaches.
     */
    public enum DateFilter {
        TODAY, THIS_WEEK, THIS_MONTH, ALL_TIME, CUSTOM_RANGE
    }

    /**
     * Enum to filter online bills by payment method.
     * Strategy pattern for payment method filtering.
     */
    public enum PaymentMethodFilter {
        ALL_PAYMENT_METHODS, CASH_ON_DELIVERY, PAY_IN_STORE
    }

    @Override
    public List<BillHistoryDTO> generateReport() {
        // Default to all bills, all time
        return getBillHistory(StoreFilter.ALL);
    }

    @Override
    public List<BillHistoryDTO> generateReport(Object... filters) {
        if (filters.length >= 1 && filters[0] instanceof StoreFilter) {
            StoreFilter storeFilter = (StoreFilter) filters[0];
            if (filters.length >= 4 && filters[1] instanceof DateFilter) {
                DateFilter dateFilter = (DateFilter) filters[1];
                LocalDate startDate = (LocalDate) filters[2];
                LocalDate endDate = (LocalDate) filters[3];
                return getBillHistory(storeFilter, dateFilter, startDate, endDate,
                        PaymentMethodFilter.ALL_PAYMENT_METHODS);
            }
            return getBillHistory(storeFilter);
        }
        return generateReport();
    }

    @Override
    public String getReportName() {
        return "Bill History Report";
    }

    /**
     * Fetches bill history with optional filtering.
     * Supports both in-store and online transactions.
     */
    public List<BillHistoryDTO> getBillHistory(StoreFilter storeFilter) {
        return getBillHistory(storeFilter, DateFilter.ALL_TIME, null, null, PaymentMethodFilter.ALL_PAYMENT_METHODS);
    }

    /**
     * Fetches bill history with date range filtering.
     */
    public List<BillHistoryDTO> getBillHistory(StoreFilter storeFilter, DateFilter dateFilter,
            LocalDate startDate, LocalDate endDate, PaymentMethodFilter paymentMethodFilter) {
        List<BillHistoryDTO> history = new ArrayList<>();

        if (storeFilter == StoreFilter.IN_STORE || storeFilter == StoreFilter.ALL) {
            history.addAll(fetchInStoreBills(dateFilter, startDate, endDate));
        }

        if (storeFilter == StoreFilter.ONLINE || storeFilter == StoreFilter.ALL) {
            history.addAll(fetchOnlineBills(dateFilter, startDate, endDate, paymentMethodFilter));
        }

        // Sort by date descending, then by bill ID
        history.sort((a, b) -> {
            int dateComparison = b.getDateTime().compareTo(a.getDateTime());
            return dateComparison != 0 ? dateComparison : Integer.compare(b.getBillId(), a.getBillId());
        });

        return history;
    }

    /**
     * Fetches in-store bill history.
     * DRY principle: Reusable SQL query building.
     */
    private List<BillHistoryDTO> fetchInStoreBills(DateFilter dateFilter, LocalDate startDate, LocalDate endDate) {
        List<BillHistoryDTO> bills = new ArrayList<>();
        String sql = buildInStoreBillQuery(dateFilter);

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            setDateParameters(stmt, dateFilter, startDate, endDate, 1);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate date = rs.getDate("date").toLocalDate();
                java.sql.Time time = rs.getTime("time");
                java.time.LocalDateTime dateTime = date.atTime(time.toLocalTime());

                bills.add(new BillHistoryDTO(
                        rs.getInt("id"),
                        "BILL-" + rs.getInt("id"),
                        dateTime,
                        rs.getDouble("total"),
                        0.0, // Discount
                        "IN_STORE",
                        "CASH",
                        null, // No customer info for in-store
                        rs.getString("employee_name"), // Add employee name
                        0)); // Item count
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching in-store bills: " + e.getMessage(), e);
        }

        return bills;
    }

    /**
     * Builds SQL query for in-store bills with date filtering.
     */
    private String buildInStoreBillQuery(DateFilter dateFilter) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, date, time, total, employee_name
                FROM bills
                """);

        if (dateFilter != DateFilter.ALL_TIME) {
            sql.append(" WHERE ");
            appendDateCondition(sql, dateFilter, "date");
        }

        sql.append(" ORDER BY date DESC, time DESC");

        return sql.toString();
    }

    /**
     * Fetches online bill history with customer information.
     */
    private List<BillHistoryDTO> fetchOnlineBills(DateFilter dateFilter, LocalDate startDate, LocalDate endDate,
            PaymentMethodFilter paymentMethodFilter) {
        List<BillHistoryDTO> bills = new ArrayList<>();
        String sql = buildOnlineBillQuery(dateFilter, paymentMethodFilter);

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = setDateParameters(stmt, dateFilter, startDate, endDate, 1);
            setPaymentMethodParameters(stmt, paymentMethodFilter, paramIndex);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate date = rs.getDate("date").toLocalDate();
                java.sql.Time time = rs.getTime("time");

                // Handle null time values
                java.time.LocalDateTime dateTime;
                if (time != null) {
                    dateTime = date.atTime(time.toLocalTime());
                } else {
                    dateTime = date.atStartOfDay();
                }

                bills.add(new BillHistoryDTO(
                        rs.getInt("id"),
                        rs.getString("serial_number") != null ? rs.getString("serial_number")
                                : "ONLINE-" + rs.getInt("id"),
                        dateTime,
                        rs.getDouble("total"),
                        0.0, // Discount
                        "ONLINE",
                        rs.getString("payment_method"),
                        rs.getString("username"), // Customer info for online
                        null, // No employee for online bills
                        0)); // Item count
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching online bills: " + e.getMessage(), e);
        }

        return bills;
    }

    /**
     * Enhanced buildOnlineBillQuery with payment method filtering
     */
    private String buildOnlineBillQuery(DateFilter dateFilter, PaymentMethodFilter paymentMethodFilter) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, date, time, total, payment_method, username, serial_number
                FROM online_bills
                """);

        boolean hasDateCondition = (dateFilter != DateFilter.ALL_TIME);
        boolean hasPaymentCondition = (paymentMethodFilter != PaymentMethodFilter.ALL_PAYMENT_METHODS);

        if (hasDateCondition || hasPaymentCondition) {
            sql.append(" WHERE ");

            if (hasDateCondition) {
                appendDateCondition(sql, dateFilter, "date");

                if (hasPaymentCondition) {
                    sql.append(" AND ");
                }
            }

            if (hasPaymentCondition) {
                appendPaymentMethodCondition(sql, paymentMethodFilter);
            }
        }

        sql.append(" ORDER BY date DESC, time DESC");
        return sql.toString();
    }

    /**
     * Appends payment method condition to SQL query
     */
    private void appendPaymentMethodCondition(StringBuilder sql, PaymentMethodFilter paymentMethodFilter) {
        switch (paymentMethodFilter) {
            case CASH_ON_DELIVERY -> sql.append("payment_method = ?");
            case PAY_IN_STORE -> sql.append("payment_method = ?");
            case ALL_PAYMENT_METHODS -> {
                // No condition needed
            }
        }
    }

    /**
     * Sets payment method parameters in prepared statement
     */
    private int setPaymentMethodParameters(PreparedStatement stmt, PaymentMethodFilter paymentMethodFilter,
            int startIndex) throws SQLException {
        switch (paymentMethodFilter) {
            case CASH_ON_DELIVERY -> {
                stmt.setString(startIndex, "Cash on Delivery");
                return startIndex + 1;
            }
            case PAY_IN_STORE -> {
                stmt.setString(startIndex, "Pay in Store");
                return startIndex + 1;
            }
            case ALL_PAYMENT_METHODS -> {
                return startIndex; // No parameters to set
            }
        }
        return startIndex;
    }

    /**
     * Appends date condition to SQL query based on filter type.
     * DRY principle: Reusable date condition logic.
     */
    private void appendDateCondition(StringBuilder sql, DateFilter dateFilter, String dateColumn) {
        switch (dateFilter) {
            case TODAY -> sql.append(dateColumn).append(" = CURRENT_DATE");
            case THIS_WEEK ->
                sql.append(dateColumn).append(" >= DATE_SUB(CURRENT_DATE, INTERVAL WEEKDAY(CURRENT_DATE) DAY)")
                        .append(" AND ").append(dateColumn).append(" <= CURRENT_DATE");
            case THIS_MONTH -> sql.append("MONTH(").append(dateColumn).append(") = MONTH(CURRENT_DATE)")
                    .append(" AND YEAR(").append(dateColumn).append(") = YEAR(CURRENT_DATE)");
            case CUSTOM_RANGE -> sql.append(dateColumn).append(" BETWEEN ? AND ?");
            case ALL_TIME -> {
                /* No condition needed */ }
        }
    }

    @Override
    public boolean isDataAvailable() {
        try {
            List<BillHistoryDTO> report = generateReport();
            return report != null && !report.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getReportTitle() {
        return "Bill History Report - Transaction Overview";
    }

    @Override
    public List<String> getColumnHeaders() {
        return List.of("Bill ID", "Serial Number", "Date & Time", "Total Amount", "Discount",
                "Store Type", "Payment Method", "Customer", "Items Count");
    }

    @Override
    public List<List<String>> getReportData() {
        List<BillHistoryDTO> bills = generateReport();
        return bills.stream()
                .map(bill -> List.of(
                        String.valueOf(bill.getBillId()),
                        bill.getSerialNumber(),
                        bill.getDateTime().toString(),
                        String.format("%.2f", bill.getTotalAmount()),
                        String.format("%.2f", bill.getDiscount()),
                        bill.getStoreType(),
                        bill.getPaymentMethod(),
                        bill.getCustomerInfo() != null ? bill.getCustomerInfo() : "N/A",
                        String.valueOf(bill.getItemCount())))
                .toList();
    }

    @Override
    public String getFormattedRow(BillHistoryDTO item) {
        return String.format(
                "Bill ID: %d | Serial: %s | Date: %s | Total: $%.2f | Type: %s | Payment: %s | Customer: %s",
                item.getBillId(),
                item.getSerialNumber(),
                item.getDateTime().toString(),
                item.getTotalAmount(),
                item.getStoreType(),
                item.getPaymentMethod(),
                item.getCustomerInfo() != null ? item.getCustomerInfo() : "N/A");
    }

    /**
     * Sets date parameters in prepared statement based on filter type.
     * DRY principle: Reusable parameter setting logic.
     */
    private int setDateParameters(PreparedStatement stmt, DateFilter dateFilter,
            LocalDate startDate, LocalDate endDate, int startIndex) throws SQLException {
        if (dateFilter == DateFilter.CUSTOM_RANGE) {
            if (startDate != null && endDate != null) {
                stmt.setDate(startIndex, java.sql.Date.valueOf(startDate));
                stmt.setDate(startIndex + 1, java.sql.Date.valueOf(endDate));
                return startIndex + 2;
            } else {
                throw new IllegalArgumentException("Start and end dates are required for custom range filter");
            }
        }
        return startIndex;
    }
}
