package syos.ui.views;

import syos.dto.ReportItemDTO;
import syos.service.SalesReportService;
import syos.service.SalesReportService.StoreType;
import syos.service.SalesReportService.TransactionType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class TotalSalesReportUI extends AbstractReportUI<ReportItemDTO> {

    private final SalesReportService service;
    private LocalDate startDate;
    private LocalDate endDate;
    private StoreType storeType;
    private TransactionType txnType;
    private List<ReportItemDTO> lastFetchedItems; // Track fetched items to distinguish null vs empty

    public TotalSalesReportUI(Scanner scanner) {
        super(scanner);
        this.service = new SalesReportService();
    }

    public TotalSalesReportUI(Scanner scanner, SalesReportService service) {
        super(scanner);
        this.service = service;
    }

    @Override
    protected boolean gatherReportCriteria() {
        if (!promptDateRange())
            return false;
        clearScreen();

        storeType = promptStoreType();
        if (storeType == null)
            return false;
        clearScreen();

        txnType = promptTransactionType(storeType);
        if (txnType == null)
            return false;
        clearScreen();

        return true;
    }

    @Override
    protected String getReportTitle() {
        return "SYOS DAILY SALES REPORT";
    }

    @Override
    protected List<ReportItemDTO> fetchReportItems() {
        lastFetchedItems = service.getSalesReport(startDate, storeType, txnType);
        return lastFetchedItems;
    }

    @Override
    protected void showNoDataMessage() {
        // Check if we had null items (error case) vs empty items (zero totals case)
        if (lastFetchedItems == null) {
            // Null case: show error message as expected by tests
            System.out.println("\n[Info] No data available for the selected criteria.");
            System.out.println("This could be due to a system error or database issue.");
            waitForEnter();
        } else if (lastFetchedItems.isEmpty()) {
            // Empty case: render report with zero totals
            renderReport(lastFetchedItems);
            waitForEnter();
        } else {
            // Fallback to default behavior
            super.showNoDataMessage();
        }
    }

    @Override
    protected void renderReport(List<ReportItemDTO> items) {
        int totalQty = 0;
        double totalRev = 0;

        printHeader();
        printTableHeader();

        for (ReportItemDTO dto : items) {
            totalQty += dto.getQuantity();
            totalRev += dto.getRevenue();

            if (storeType == StoreType.COMBINED) {
                String storeName = extractStoreType(dto.getName());
                String itemName = cleanItemName(dto.getName());
                System.out.printf("%-10s %-25s %-10s %7d %12.2f%n",
                        dto.getCode(),
                        truncateName(itemName, 25),
                        storeName,
                        dto.getQuantity(),
                        dto.getRevenue());
            } else {
                System.out.printf("%-10s %-30s %7d %12.2f%n",
                        dto.getCode(), dto.getName(), dto.getQuantity(), dto.getRevenue());
            }
        }

        System.out.println("-------------------------------------------------------");
        System.out.printf("Total Quantity: %-5d | Total Revenue: Rs. %.2f%n", totalQty, totalRev);
        System.out.println("=======================================================");
    }

    private void printHeader() {
        System.out.println("=======================================================");
        System.out.println("                 SYOS DAILY SALES REPORT               ");
        System.out.println("=======================================================");

        System.out.println(startDate.equals(endDate)
                ? "Date       : " + startDate
                : "Date Range : " + startDate + " to " + endDate);

        System.out.println("Store Type : " + storeType);
        System.out.println("Txn Type   : " + txnType);
        System.out.println("-------------------------------------------------------");
    }

    private void printTableHeader() {
        if (storeType == StoreType.COMBINED) {
            System.out.printf("%-10s %-25s %-10s %7s %12s%n", "Code", "Name", "Store", "Qty", "Revenue");
        } else {
            System.out.printf("%-10s %-30s %7s %12s%n", "Code", "Name", "Qty", "Revenue");
        }
        System.out.println("-------------------------------------------------------");
    }

    private boolean promptDateRange() {
        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1", () -> startDate = endDate = LocalDate.now());
        options.put("2", () -> {
            startDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            endDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
        });
        options.put("3", () -> {
            startDate = LocalDate.now().withDayOfMonth(1);
            endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        });
        options.put("4", () -> {
            if (!promptSpecificDateRange())
                throw new RuntimeException("exit");
        });
        options.put("0", () -> {
            throw new RuntimeException("exit");
        });

        while (true) {
            System.out.println("Select Date Range:");
            System.out.println("  1. Today\n  2. This Week\n  3. This Month\n  4. Specific Date Range\n  0. Back");
            System.out.print("Choice (0-4): ");

            String input = scanner.nextLine().trim();
            if (options.containsKey(input)) {
                try {
                    options.get(input).run();
                    return true;
                } catch (RuntimeException e) {
                    return false;
                }
            } else {
                System.out.println("[Invalid] Enter 0–4.");
            }
        }
    }

    private boolean promptSpecificDateRange() {
        try {
            System.out.print("Enter start date (yyyy-mm-dd): ");
            startDate = LocalDate.parse(scanner.nextLine().trim());

            System.out.print("Enter end date (yyyy-mm-dd): ");
            endDate = LocalDate.parse(scanner.nextLine().trim());

            if (endDate.isBefore(startDate)) {
                System.out.println("[Invalid] End date before start date.");
                return false;
            }
            return true;
        } catch (DateTimeParseException e) {
            System.out.println("[Invalid] Use yyyy-mm-dd format.");
            return false;
        }
    }

    private StoreType promptStoreType() {
        Map<String, StoreType> options = new LinkedHashMap<>();
        options.put("1", StoreType.IN_STORE);
        options.put("2", StoreType.ONLINE);
        options.put("3", StoreType.COMBINED);
        options.put("0", null);
        return promptOption("Store Type", options);
    }

    private TransactionType promptTransactionType(StoreType st) {
        if (st == StoreType.IN_STORE)
            return TransactionType.IN_STORE;

        Map<String, TransactionType> options = new LinkedHashMap<>();
        switch (st) {
            case ONLINE -> {
                options.put("1", TransactionType.RESERVATION_PAY_IN_STORE);
                options.put("2", TransactionType.RESERVATION_COD);
                options.put("3", TransactionType.ALL);
                options.put("0", null);
                return promptOption("Online Transaction Type", options);
            }
            case COMBINED -> {
                options.put("1", TransactionType.IN_STORE);
                options.put("2", TransactionType.RESERVATION_PAY_IN_STORE);
                options.put("3", TransactionType.RESERVATION_COD);
                options.put("4", TransactionType.ALL);
                options.put("0", null);
                return promptOption("Combined Transaction Type", options);
            }
            default -> {
                return null;
            }
        }
    }

    private <T> T promptOption(String label, Map<String, T> options) {
        while (true) {
            System.out.println("Select " + label + ":");
            options.forEach((k, v) -> System.out.println("  " + k + ". " + (v != null ? v : "Back")));
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            if (options.containsKey(choice))
                return options.get(choice);
            System.out.println("[Invalid] Try again.");
        }
    }

    private String extractStoreType(String itemName) {
        if (itemName.contains("(In-Store)"))
            return "In-Store";
        if (itemName.contains("(Online)"))
            return "Online";
        return "Unknown";
    }

    private String cleanItemName(String itemName) {
        return itemName.replace(" (In-Store)", "").replace(" (Online)", "").trim();
    }

    private String truncateName(String name, int maxLength) {
        return name.length() <= maxLength ? name : name.substring(0, maxLength - 3) + "...";
    }
}
