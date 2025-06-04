package syos.ui.views;

import syos.dto.BillHistoryDTO;
import syos.service.BillHistoryReportService;
import syos.service.BillHistoryReportService.DateFilter;
import syos.service.BillHistoryReportService.StoreFilter;
import syos.service.BillHistoryReportService.PaymentMethodFilter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class BillHistoryReportUI extends AbstractReportUI<BillHistoryDTO> {

    private final BillHistoryReportService service = new BillHistoryReportService();
    private StoreFilter storeFilter;
    private DateFilter dateFilter;
    private PaymentMethodFilter paymentMethodFilter;
    private LocalDate startDate;
    private LocalDate endDate;

    public BillHistoryReportUI(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected boolean gatherReportCriteria() {
        try {
            storeFilter = promptStoreFilter();
            if (storeFilter == null)
                return false;

            dateFilter = promptDateFilter();
            if (dateFilter == null)
                return false;

            if (dateFilter == DateFilter.CUSTOM_RANGE) {
                startDate = promptDate("Enter start date (yyyy-MM-dd): ");
                endDate = promptDate("Enter end date (yyyy-MM-dd): ");
            }

            if (storeFilter == StoreFilter.ONLINE) {
                paymentMethodFilter = promptPaymentMethodFilter();
                if (paymentMethodFilter == null)
                    return false;
            } else {
                paymentMethodFilter = PaymentMethodFilter.ALL_PAYMENT_METHODS;
            }

            clearScreen();
            return true;
        } catch (Exception e) {
            System.out.println("Error gathering criteria: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected String getReportTitle() {
        return "Bill History Report";
    }

    @Override
    protected List<BillHistoryDTO> fetchReportItems() {
        return service.getBillHistory(storeFilter, dateFilter, startDate, endDate, paymentMethodFilter);
    }

    @Override
    protected void renderReport(List<BillHistoryDTO> bills) {
        if (bills.isEmpty()) {
            showNoDataMessage();
            return;
        }
        clearScreen();
        renderSummaryStatistics(bills);
        System.out.println();
        renderBillDetails(bills);
        waitForEnter();
    }

    @Override
    protected void showNoDataMessage() {
        System.out.println("=============================== TRANSACTION SUMMARY ===============================");
        System.out.println("Date Range: " + getDateRangeDescription());
        System.out.println("Store Filter: " + getStoreFilterDescription());
        if (storeFilter == StoreFilter.ONLINE && paymentMethodFilter != PaymentMethodFilter.ALL_PAYMENT_METHODS) {
            System.out.println("Payment Method Filter: " + getPaymentMethodFilterDescription());
        }
        System.out.println("\nNo transactions found for the selected criteria.");
        System.out.println("====================================================================================");
        waitForEnter();
    }

    // Prompt Methods (safe)
    private StoreFilter promptStoreFilter() {
        Map<String, StoreFilter> options = new LinkedHashMap<>();
        options.put("1", StoreFilter.IN_STORE);
        options.put("2", StoreFilter.ONLINE);
        options.put("3", StoreFilter.ALL);
        options.put("0", null);
        return promptFilter("Store Type", options);
    }

    private DateFilter promptDateFilter() {
        Map<String, DateFilter> options = new LinkedHashMap<>();
        options.put("1", DateFilter.TODAY);
        options.put("2", DateFilter.THIS_WEEK);
        options.put("3", DateFilter.THIS_MONTH);
        options.put("4", DateFilter.ALL_TIME);
        options.put("5", DateFilter.CUSTOM_RANGE);
        options.put("0", null);
        return promptFilter("Date Range", options);
    }

    private PaymentMethodFilter promptPaymentMethodFilter() {
        Map<String, PaymentMethodFilter> options = new LinkedHashMap<>();
        options.put("1", PaymentMethodFilter.ALL_PAYMENT_METHODS);
        options.put("2", PaymentMethodFilter.CASH_ON_DELIVERY);
        options.put("3", PaymentMethodFilter.PAY_IN_STORE);
        options.put("0", null);
        return promptFilter("Payment Method Filter", options);
    }

    private <T> T promptFilter(String title, Map<String, T> options) {
        while (true) {
            System.out.println("Select " + title + ":");
            options.forEach((key, val) -> System.out
                    .println("  " + key + ". " + (val != null ? val.toString().replace("_", " ") : "Back")));
            System.out.print("Choice: ");
            String input = scanner.nextLine().trim();
            if (options.containsKey(input))
                return options.get(input);
            System.out.println("[Invalid] Please enter a valid option.");
        }
    }

    private LocalDate promptDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(scanner.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("[Invalid] Format must be yyyy-MM-dd");
            }
        }
    }

    private String getDateRangeDescription() {
        return switch (dateFilter) {
            case TODAY -> "Today (" + LocalDate.now() + ")";
            case THIS_WEEK -> "This Week";
            case THIS_MONTH -> "This Month";
            case ALL_TIME -> "All Time";
            case CUSTOM_RANGE -> startDate + " to " + endDate;
        };
    }

    private String getStoreFilterDescription() {
        return switch (storeFilter) {
            case IN_STORE -> "In-Store Only";
            case ONLINE -> "Online Only";
            case ALL -> "All Stores";
        };
    }

    private String getPaymentMethodFilterDescription() {
        return switch (paymentMethodFilter) {
            case ALL_PAYMENT_METHODS -> "All Payment Methods";
            case CASH_ON_DELIVERY -> "Cash on Delivery Only";
            case PAY_IN_STORE -> "Pay in Store Only";
        };
    }

    private void renderSummaryStatistics(List<BillHistoryDTO> bills) {
        Map<String, List<BillHistoryDTO>> billsByStore = bills.stream()
                .collect(Collectors.groupingBy(BillHistoryDTO::getStoreType));

        double totalRevenue = bills.stream().mapToDouble(BillHistoryDTO::getTotalAmount).sum();
        int totalBills = bills.size();

        System.out.println("=============================== TRANSACTION SUMMARY ===============================");
        System.out.println("Date Range: " + getDateRangeDescription());
        System.out.println("Store Filter: " + getStoreFilterDescription());
        if (storeFilter == StoreFilter.ONLINE && paymentMethodFilter != PaymentMethodFilter.ALL_PAYMENT_METHODS) {
            System.out.println("Payment Method Filter: " + getPaymentMethodFilterDescription());
        }

        System.out.println("------------------------------------------------------------------------------------");
        if (billsByStore.containsKey("IN_STORE"))
            renderStoreSummary("In-Store", billsByStore.get("IN_STORE"));
        if (billsByStore.containsKey("ONLINE"))
            renderOnlineSummary(billsByStore.get("ONLINE"));

        System.out.println("------------------------------------------------------------------------------------");
        System.out.printf("TOTAL TRANSACTIONS: %-7d | TOTAL REVENUE: Rs. %10.2f%n", totalBills, totalRevenue);

        if (totalBills > 0)
            System.out.printf("AVERAGE TRANSACTION VALUE: Rs. %.2f%n", totalRevenue / totalBills);

        System.out.println("====================================================================================");
    }

    private void renderStoreSummary(String label, List<BillHistoryDTO> bills) {
        double revenue = bills.stream().mapToDouble(BillHistoryDTO::getTotalAmount).sum();
        System.out.printf("%s Transactions: %-5d | Revenue: Rs. %10.2f%n", label, bills.size(), revenue);
    }

    private void renderOnlineSummary(List<BillHistoryDTO> onlineBills) {
        renderStoreSummary("Online", onlineBills);

        if (paymentMethodFilter == PaymentMethodFilter.ALL_PAYMENT_METHODS) {
            Map<String, Long> methodCounts = onlineBills.stream()
                    .collect(Collectors.groupingBy(BillHistoryDTO::getPaymentMethod, Collectors.counting()));
            System.out.print("  Payment Methods: ");
            methodCounts.forEach((method, count) -> System.out.printf("%s (%d) ", method, count));
            System.out.println();
        } else {
            System.out.printf("  Filtered by: %s (%d transactions)%n",
                    getPaymentMethodFilterDescription(), onlineBills.size());
        }
    }

    private void renderBillDetails(List<BillHistoryDTO> bills) {
        System.out.println(
                "==================================== TRANSACTION DETAILS ====================================");
        System.out.printf("%-8s %-12s %-19s %-10s %-15s %-15s %-8s%n",
                "Bill ID", "Date", "Timestamp", "Amount", "Payment", "Customer/Employee", "Store");
        System.out.println(
                "------------------------------------------------------------------------------------------------");

        for (BillHistoryDTO bill : bills) {
            String user = bill.getStoreType().equals("IN_STORE")
                    ? Optional.ofNullable(bill.getEmployeeName()).orElse("N/A")
                    : Optional.ofNullable(bill.getCustomerInfo()).orElse("N/A");

            System.out.printf("%-8d %-12s %-19s %-10.2f %-15s %-15s %-8s%n",
                    bill.getBillId(),
                    bill.getDateTime().toLocalDate(),
                    formatTimestamp(bill.getDateTime()),
                    bill.getTotalAmount(),
                    truncateText(bill.getPaymentMethod(), 15),
                    truncateText(user, 15),
                    bill.getStoreType());
        }

        System.out.println(
                "------------------------------------------------------------------------------------------------");
        System.out.printf("Total Bills Displayed: %d%n", bills.size());
        System.out.println(
                "================================================================================================");
    }

    private String formatTimestamp(java.time.LocalDateTime timestamp) {
        return timestamp == null ? "N/A"
                : timestamp.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private String truncateText(String text, int maxLength) {
        return (text == null || text.length() <= maxLength)
                ? Optional.ofNullable(text).orElse("N/A")
                : text.substring(0, maxLength - 3) + "...";
    }
}
