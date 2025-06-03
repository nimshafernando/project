package syos.ui.views;

import syos.dto.BillHistoryDTO;
import syos.service.BillHistoryReportService;
import syos.service.BillHistoryReportService.DateFilter;
import syos.service.BillHistoryReportService.StoreFilter;
import syos.service.BillHistoryReportService.PaymentMethodFilter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Bill History Report UI.
 * Extends AbstractReportUI following Template Method pattern.
 * Shows comprehensive transaction history across all customer bills.
 */
public class BillHistoryReportUI extends AbstractReportUI<BillHistoryDTO> {

    private final BillHistoryReportService service = new BillHistoryReportService();
    private StoreFilter storeFilter;
    private DateFilter dateFilter;
    private PaymentMethodFilter paymentMethodFilter; // Add this field
    private LocalDate startDate;
    private LocalDate endDate;

    public BillHistoryReportUI(Scanner scanner) {
        super(scanner);
    }

    /**
     * Prompts user to select store type filter.
     * Strategy pattern: Different store filtering strategies.
     */
    private StoreFilter promptStoreFilter() {
        while (true) {
            System.out.println("Select Store Type:");
            System.out.println("  1. In-Store Transactions Only");
            System.out.println("  2. Online Transactions Only");
            System.out.println("  3. All Transactions");
            System.out.println("  0. Back to Reports Menu"); // Added this line
            System.out.print("Choice (0-3): ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    return StoreFilter.IN_STORE;
                case "2":
                    return StoreFilter.ONLINE;
                case "3":
                    return StoreFilter.ALL;
                case "0":
                    return null; // Return null to indicate user wants to go back
                default:
                    System.out.println("[Invalid] Enter 0-3.");
            }
        }
    }

    /**
     * Prompts user to select date range filter.
     * Strategy pattern: Different date filtering strategies.
     */
    private DateFilter promptDateFilter() {
        while (true) {
            System.out.println("Select Date Range:");
            System.out.println("  1. Today Only");
            System.out.println("  2. This Week");
            System.out.println("  3. This Month");
            System.out.println("  4. All Time");
            System.out.println("  5. Custom Range");
            System.out.println("  0. Back to Previous Menu"); // Added this line
            System.out.print("Choice (0-5): ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    return DateFilter.TODAY;
                case "2":
                    return DateFilter.THIS_WEEK;
                case "3":
                    return DateFilter.THIS_MONTH;
                case "4":
                    return DateFilter.ALL_TIME;
                case "5":
                    return DateFilter.CUSTOM_RANGE;
                case "0":
                    return null; // Return null to indicate user wants to go back
                default:
                    System.out.println("[Invalid] Enter 0-5.");
            }
        }
    }

    /**
     * Prompts for a single date with validation.
     * DRY principle: Reusable date input method.
     */
    private LocalDate promptDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("[Invalid] Use format yyyy-MM-dd (e.g., 2024-01-15).");
            }
        }
    }

    /**
     * Prompts user to select payment method filter for online transactions.
     * Strategy pattern: Different payment method filtering strategies.
     */
    private PaymentMethodFilter promptPaymentMethodFilter() {
        while (true) {
            System.out.println("Select Payment Method Filter:");
            System.out.println("  1. All Payment Methods");
            System.out.println("  2. Cash on Delivery Only");
            System.out.println("  3. Pay in Store Only");
            System.out.println("  0. Back to Previous Menu");
            System.out.print("Choice (0-3): ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    return PaymentMethodFilter.ALL_PAYMENT_METHODS;
                case "2":
                    return PaymentMethodFilter.CASH_ON_DELIVERY;
                case "3":
                    return PaymentMethodFilter.PAY_IN_STORE;
                case "0":
                    return null; // Return null to indicate user wants to go back
                default:
                    System.out.println("[Invalid] Enter 0-3.");
            }
        }
    }

    @Override
    protected boolean gatherReportCriteria() {
        try {
            // Get store filter
            storeFilter = promptStoreFilter();
            if (storeFilter == null) {
                return false; // User chose to go back to reports menu
            }

            // Get date filter
            dateFilter = promptDateFilter();
            if (dateFilter == null) {
                return false; // User chose to go back
            }

            // Handle custom date range
            if (dateFilter == DateFilter.CUSTOM_RANGE) {
                startDate = promptDate("Enter start date (yyyy-MM-dd): ");
                if (startDate == null) {
                    return false; // User chose to exit
                }

                endDate = promptDate("Enter end date (yyyy-MM-dd): ");
                if (endDate == null) {
                    return false; // User chose to exit
                }
            }

            // Get payment method filter if online transactions selected
            if (storeFilter == StoreFilter.ONLINE) {
                paymentMethodFilter = promptPaymentMethodFilter();
                if (paymentMethodFilter == null) {
                    return false; // User chose to go back
                }
            } else {
                // Default to all payment methods for other store filters
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

    /**
     * Shows information about the bill history report.
     * KISS principle: Clear information display.
     */

    @Override
    protected List<BillHistoryDTO> fetchReportItems() {
        return service.getBillHistory(storeFilter, dateFilter, startDate, endDate, paymentMethodFilter);
    }

    /**
     * Gets description of selected payment method filter.
     */
    private String getPaymentMethodFilterDescription() {
        if (storeFilter != StoreFilter.ONLINE) {
            return ""; // Don't show payment method filter for non-online transactions
        }

        return switch (paymentMethodFilter) {
            case ALL_PAYMENT_METHODS -> "All Payment Methods";
            case CASH_ON_DELIVERY -> "Cash on Delivery Only";
            case PAY_IN_STORE -> "Pay in Store Only";
        };
    }

    /**
     * Renders summary statistics for the bill history.
     * SRP: Focused on summary rendering only.
     */
    private void renderSummaryStatistics(List<BillHistoryDTO> bills) {
        Map<String, List<BillHistoryDTO>> billsByStore = bills.stream()
                .collect(Collectors.groupingBy(BillHistoryDTO::getStoreType));

        double totalRevenue = bills.stream().mapToDouble(BillHistoryDTO::getTotalAmount).sum();
        int totalBills = bills.size();

        System.out.println("=============================== TRANSACTION SUMMARY ===============================");
        System.out.println("Date Range: " + getDateRangeDescription());
        System.out.println("Store Filter: " + getStoreFilterDescription());

        // Show payment method filter only for online transactions
        if (storeFilter == StoreFilter.ONLINE && paymentMethodFilter != PaymentMethodFilter.ALL_PAYMENT_METHODS) {
            System.out.println("Payment Method Filter: " + getPaymentMethodFilterDescription());
        }

        System.out.println("------------------------------------------------------------------------------------");

        // Store-wise breakdown
        if (billsByStore.containsKey("IN_STORE")) {
            List<BillHistoryDTO> inStoreBills = billsByStore.get("IN_STORE");
            double inStoreRevenue = inStoreBills.stream().mapToDouble(BillHistoryDTO::getTotalAmount).sum();
            System.out.printf("In-Store Transactions: %-5d | Revenue: Rs. %10.2f%n",
                    inStoreBills.size(), inStoreRevenue);
        }

        if (billsByStore.containsKey("ONLINE")) {
            List<BillHistoryDTO> onlineBills = billsByStore.get("ONLINE");
            double onlineRevenue = onlineBills.stream().mapToDouble(BillHistoryDTO::getTotalAmount).sum();
            System.out.printf("Online Transactions: %-7d | Revenue: Rs. %10.2f%n",
                    onlineBills.size(), onlineRevenue);

            // Payment method breakdown for online (if showing all payment methods)
            if (paymentMethodFilter == PaymentMethodFilter.ALL_PAYMENT_METHODS) {
                Map<String, Long> paymentMethods = onlineBills.stream()
                        .collect(Collectors.groupingBy(BillHistoryDTO::getPaymentMethod, Collectors.counting()));

                System.out.print("  Payment Methods: ");
                paymentMethods.forEach((method, count) -> System.out.printf("%s (%d) ", method, count));
                System.out.println();
            } else {
                // Show specific payment method count
                String filterDescription = getPaymentMethodFilterDescription();
                System.out.printf("  Filtered by: %s (%d transactions)%n", filterDescription, onlineBills.size());
            }
        }

        System.out.println("------------------------------------------------------------------------------------");
        System.out.printf("TOTAL TRANSACTIONS: %-7d | TOTAL REVENUE: Rs. %10.2f%n", totalBills, totalRevenue);

        if (totalBills > 0) {
            System.out.printf("AVERAGE TRANSACTION VALUE: Rs. %.2f%n", totalRevenue / totalBills);
        }

        System.out.println("====================================================================================");
    }

    /**
     * Renders detailed bill information.
     * SRP: Focused on bill details rendering only.
     */
    private void renderBillDetails(List<BillHistoryDTO> bills) {
        System.out.println(
                "==================================== TRANSACTION DETAILS ====================================");
        System.out.printf("%-8s %-12s %-19s %-10s %-15s %-15s %-8s%n",
                "Bill ID", "Date", "Timestamp", "Amount", "Payment", "Customer/Employee", "Store");
        System.out.println(
                "------------------------------------------------------------------------------------------------");
        for (BillHistoryDTO bill : bills) {
            // Show employee name for in-store, customer name for online
            String customerOrEmployee;
            if ("IN_STORE".equals(bill.getStoreType())) {
                customerOrEmployee = bill.getEmployeeName() != null ? bill.getEmployeeName() : "N/A";
            } else {
                customerOrEmployee = bill.getCustomerInfo() != null ? bill.getCustomerInfo() : "N/A";
            }

            System.out.printf("%-8d %-12s %-19s %-10.2f %-15s %-15s %-8s%n",
                    bill.getBillId(),
                    bill.getDateTime().toLocalDate(),
                    formatTimestamp(bill.getDateTime()),
                    bill.getTotalAmount(),
                    truncateText(bill.getPaymentMethod(), 15),
                    truncateText(customerOrEmployee, 15),
                    bill.getStoreType());
        }

        System.out.println(
                "------------------------------------------------------------------------------------------------");
        System.out.printf("Total Bills Displayed: %d%n", bills.size());
        System.out.println(
                "================================================================================================");
    }

    /**
     * Formats timestamp for display.
     * KISS principle: Simple time formatting.
     */
    private String formatTimestamp(java.time.LocalDateTime timestamp) {
        if (timestamp == null)
            return "N/A";
        return timestamp.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * Truncates text for better table formatting.
     * YAGNI principle: Simple text truncation.
     */
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text != null ? text : "N/A";
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Gets description of selected date range.
     * KISS principle: Simple string mapping.
     */
    private String getDateRangeDescription() {
        return switch (dateFilter) {
            case TODAY -> "Today (" + LocalDate.now() + ")";
            case THIS_WEEK -> "This Week";
            case THIS_MONTH -> "This Month";
            case ALL_TIME -> "All Time";
            case CUSTOM_RANGE -> startDate + " to " + endDate;
        };
    }

    /**
     * Gets description of selected store filter.
     * KISS principle: Simple string mapping.
     */
    private String getStoreFilterDescription() {
        return switch (storeFilter) {
            case IN_STORE -> "In-Store Only";
            case ONLINE -> "Online Only";
            case ALL -> "All Stores";
        };
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

        System.out.println("");
        System.out.println("No transactions found for the selected criteria.");
        System.out.println("====================================================================================");
        waitForEnter();
    }
}
