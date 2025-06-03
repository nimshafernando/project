package syos.service;

import syos.dto.ReportItemDTO;
import syos.interfaces.IReportService;
import syos.service.SalesReportService.StoreType;
import syos.service.SalesReportService.TransactionType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Total Sales Report Service implementing IReportService interface
 * Follows Single Responsibility Principle for total sales reporting
 * Integrates with existing SalesReportService for data access
 */
public class TotalSalesReportService implements IReportService<ReportItemDTO> {

    private final SalesReportService salesService;

    public TotalSalesReportService() {
        this.salesService = new SalesReportService();
    }

    @Override
    public List<ReportItemDTO> generateReport() {
        // Default to today's report for all stores and transactions
        return salesService.getSalesReport(
                LocalDate.now(),
                StoreType.COMBINED,
                TransactionType.ALL);
    }

    @Override
    public List<ReportItemDTO> generateReport(Object... filters) {
        if (filters.length >= 3) {
            LocalDate date = (LocalDate) filters[0];
            StoreType storeType = (StoreType) filters[1];
            TransactionType transactionType = (TransactionType) filters[2];

            return salesService.getSalesReport(date, storeType, transactionType);
        }
        return generateReport();
    }

    @Override
    public String getReportName() {
        return "Total Sales Report";
    }

    @Override
    public boolean isDataAvailable() {
        try {
            List<ReportItemDTO> report = generateReport();
            return report != null && !report.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getReportTitle() {
        return "SYOS TOTAL SALES REPORT";
    }

    @Override
    public List<String> getColumnHeaders() {
        return Arrays.asList("Item Code", "Item Name", "Quantity Sold", "Total Revenue");
    }

    @Override
    public List<List<String>> getReportData() {
        List<List<String>> reportData = new ArrayList<>();
        List<ReportItemDTO> items = generateReport();

        for (ReportItemDTO item : items) {
            reportData.add(Arrays.asList(
                    item.getCode(),
                    item.getName(),
                    String.valueOf(item.getQuantity()),
                    String.format("%.2f", item.getRevenue())));
        }

        return reportData;
    }

    @Override
    public String getFormattedRow(ReportItemDTO item) {
        return String.format("%-10s %-25s %8d %12.2f",
                item.getCode(),
                truncateName(item.getName(), 25),
                item.getQuantity(),
                item.getRevenue());
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
