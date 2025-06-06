package syos.service;

import syos.dto.BillHistoryDTO;
import syos.interfaces.IReportService;
import syos.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class responsible for bill history reporting.
 * Follows SRP - handles only bill history business logic.
 * Uses Gateway pattern for data access abstraction.
 */
public class BillHistoryReportService implements IReportService<BillHistoryDTO> {

    public enum StoreFilter {
        IN_STORE, ONLINE, ALL
    }

    public enum DateFilter {
        TODAY, THIS_WEEK, THIS_MONTH, ALL_TIME, CUSTOM_RANGE
    }

    public enum PaymentMethodFilter {
        ALL_PAYMENT_METHODS, CASH_ON_DELIVERY, PAY_IN_STORE
    }

    @Override
    public List<BillHistoryDTO> generateReport() {
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

    public List<BillHistoryDTO> getBillHistory(StoreFilter storeFilter) {
        return getBillHistory(storeFilter, DateFilter.ALL_TIME, null, null, PaymentMethodFilter.ALL_PAYMENT_METHODS);
    }

    public List<BillHistoryDTO> getBillHistory(StoreFilter storeFilter, DateFilter dateFilter) {
        return getBillHistory(storeFilter, dateFilter, null, null, PaymentMethodFilter.ALL_PAYMENT_METHODS);
    }

    public List<BillHistoryDTO> getBillHistory(StoreFilter storeFilter, DateFilter dateFilter, LocalDate startDate,
            LocalDate endDate) {
        return getBillHistory(storeFilter, dateFilter, startDate, endDate, PaymentMethodFilter.ALL_PAYMENT_METHODS);
    }

    public List<BillHistoryDTO> getBillHistory(StoreFilter storeFilter, DateFilter dateFilter,
            PaymentMethodFilter paymentMethodFilter) {
        return getBillHistory(storeFilter, dateFilter, null, null, paymentMethodFilter);
    }

    public List<BillHistoryDTO> getBillHistory(StoreFilter storeFilter, DateFilter dateFilter, LocalDate startDate,
            LocalDate endDate, PaymentMethodFilter paymentMethodFilter) {
        List<BillHistoryDTO> history = new ArrayList<>();

        if (storeFilter == StoreFilter.IN_STORE || storeFilter == StoreFilter.ALL) {
            history.addAll(fetchInStoreBills(dateFilter, startDate, endDate));
        }

        if (storeFilter == StoreFilter.ONLINE || storeFilter == StoreFilter.ALL) {
            history.addAll(fetchOnlineBills(dateFilter, startDate, endDate, paymentMethodFilter));
        }

        history.sort((a, b) -> {
            int dateComparison = b.getDateTime().compareTo(a.getDateTime());
            return dateComparison != 0 ? dateComparison : Integer.compare(b.getBillId(), a.getBillId());
        });

        return history;
    }

    private List<BillHistoryDTO> fetchInStoreBills(DateFilter dateFilter, LocalDate startDate, LocalDate endDate) {
        List<BillHistoryDTO> bills = new ArrayList<>();
        String sql = buildInStoreBillQuery(dateFilter);

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            setDateParameters(stmt, dateFilter, startDate, endDate, 1);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("date");
                    if (sqlDate == null) {
                        continue;
                    }
                    LocalDate date = sqlDate.toLocalDate();
                    Time time = rs.getTime("time");
                    LocalDateTime dateTime = time != null ? date.atTime(time.toLocalTime()) : date.atStartOfDay();

                    bills.add(new BillHistoryDTO(
                            rs.getInt("id"),
                            "BILL-" + rs.getInt("id"),
                            dateTime,
                            rs.getDouble("total"),
                            0.0,
                            "IN_STORE",
                            "CASH",
                            null,
                            rs.getString("employee_name"),
                            0));
                }
            }
        } catch (Exception e) {
            // Log the error if needed, but return empty list to handle gracefully
            return new ArrayList<>();
        }

        return bills;
    }

    private String buildInStoreBillQuery(DateFilter dateFilter) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, date, time, total, employee_name FROM bills");

        if (dateFilter != DateFilter.ALL_TIME) {
            sql.append(" WHERE ");
            appendDateCondition(sql, dateFilter, "date");
        }

        sql.append(" ORDER BY date DESC, time DESC");
        return sql.toString();
    }

    private List<BillHistoryDTO> fetchOnlineBills(DateFilter dateFilter, LocalDate startDate, LocalDate endDate,
            PaymentMethodFilter paymentMethodFilter) {
        List<BillHistoryDTO> bills = new ArrayList<>();
        String sql = buildOnlineBillQuery(dateFilter, paymentMethodFilter);

        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = setDateParameters(stmt, dateFilter, startDate, endDate, 1);
            setPaymentMethodParameters(stmt, paymentMethodFilter, paramIndex);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("date");
                    if (sqlDate == null) {
                        continue;
                    }
                    LocalDate date = sqlDate.toLocalDate();
                    Time time = rs.getTime("time");
                    LocalDateTime dateTime = time != null ? date.atTime(time.toLocalTime()) : date.atStartOfDay();

                    bills.add(new BillHistoryDTO(
                            rs.getInt("id"),
                            rs.getString("serial_number") != null ? rs.getString("serial_number")
                                    : "ONLINE-" + rs.getInt("id"),
                            dateTime,
                            rs.getDouble("total"),
                            0.0,
                            "ONLINE",
                            rs.getString("payment_method"),
                            rs.getString("username"),
                            null,
                            0));
                }
            }
        } catch (Exception e) {
            // Log the error if needed, but return empty list to handle gracefully
            return new ArrayList<>();
        }

        return bills;
    }

    private String buildOnlineBillQuery(DateFilter dateFilter, PaymentMethodFilter paymentMethodFilter) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, date, time, total, payment_method, username, serial_number FROM online_bills");

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

    private void appendPaymentMethodCondition(StringBuilder sql, PaymentMethodFilter paymentMethodFilter) {
        switch (paymentMethodFilter) {
            case CASH_ON_DELIVERY -> sql.append("payment_method = ?");
            case PAY_IN_STORE -> sql.append("payment_method = ?");
            case ALL_PAYMENT_METHODS -> {
                // No condition
            }
        }
    }

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
                return startIndex;
            }
        }
        return startIndex;
    }

    private void appendDateCondition(StringBuilder sql, DateFilter dateFilter, String dateColumn) {
        switch (dateFilter) {
            case TODAY -> sql.append(dateColumn).append(" = CURDATE()");
            case THIS_WEEK -> sql.append(dateColumn)
                    .append(" BETWEEN DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) AND CURDATE()");
            case THIS_MONTH -> sql.append("YEAR(").append(dateColumn).append(") = YEAR(CURDATE()) AND MONTH(")
                    .append(dateColumn).append(") = MONTH(CURDATE())");
            case CUSTOM_RANGE -> sql.append(dateColumn).append(" BETWEEN ? AND ?");
            case ALL_TIME -> {
                // No condition
            }
        }
    }

    @Override
    public boolean isDataAvailable() {
        try (Connection conn = DatabaseConnection.getInstance().getPoolConnection();
                PreparedStatement stmt = conn
                        .prepareStatement("SELECT 1 FROM bills UNION SELECT 1 FROM online_bills LIMIT 1");
                ResultSet rs = stmt.executeQuery()) {
            return rs.next();
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
                        bill.getPaymentMethod() != null ? bill.getPaymentMethod() : "N/A",
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
                item.getPaymentMethod() != null ? item.getPaymentMethod() : "N/A",
                item.getCustomerInfo() != null ? item.getCustomerInfo() : "N/A");
    }

    private int setDateParameters(PreparedStatement stmt, DateFilter dateFilter, LocalDate startDate, LocalDate endDate,
            int startIndex) throws SQLException {
        if (dateFilter == DateFilter.CUSTOM_RANGE) {
            if (startDate != null && endDate != null) {
                stmt.setDate(startIndex, Date.valueOf(startDate));
                stmt.setDate(startIndex + 1, Date.valueOf(endDate));
                return startIndex + 2;
            } else {
                throw new IllegalArgumentException("Start and end dates are required for custom range filter");
            }
        }
        return startIndex;
    }
}