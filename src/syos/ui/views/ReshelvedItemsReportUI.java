package syos.ui.views;

import syos.dto.ReshelvedItemDTO;
import syos.service.ReshelvedItemsReportService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class ReshelvedItemsReportUI extends AbstractReportUI<ReshelvedItemDTO> {

    private final ReshelvedItemsReportService service = new ReshelvedItemsReportService();
    private LocalDate startDate;
    private LocalDate endDate;
    private StoreFilter storeFilter;
    private DateRangeFilter dateRangeFilter;

    public enum StoreFilter {
        IN_STORE_ONLY, ONLINE_ONLY, COMBINED
    }

    public enum DateRangeFilter {
        TODAY, THIS_WEEK, THIS_MONTH, SPECIFIC_DATE_RANGE
    }

    public ReshelvedItemsReportUI(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected String getReportTitle() {
        return "SYOS RESHELVED ITEMS REPORT";
    }

    @Override
    protected boolean gatherReportCriteria() {
        try {
            System.out.println("DEBUG: Starting gatherReportCriteria");
            Map<String, StoreFilter> storeOptions = new LinkedHashMap<>();
            storeOptions.put("1", StoreFilter.IN_STORE_ONLY);
            storeOptions.put("2", StoreFilter.ONLINE_ONLY);
            storeOptions.put("3", StoreFilter.COMBINED);
            storeOptions.put("0", null);
            storeFilter = promptFilter("Store Type", storeOptions);
            System.out.println("DEBUG: Selected storeFilter: " + storeFilter);
            if (storeFilter == null)
                return false;

            clearScreen();

            Map<String, DateRangeFilter> dateOptions = new LinkedHashMap<>();
            dateOptions.put("1", DateRangeFilter.TODAY);
            dateOptions.put("2", DateRangeFilter.THIS_WEEK);
            dateOptions.put("3", DateRangeFilter.THIS_MONTH);
            dateOptions.put("4", DateRangeFilter.SPECIFIC_DATE_RANGE);
            dateOptions.put("0", null);
            dateRangeFilter = promptFilter("Date Range", dateOptions);
            System.out.println("DEBUG: Selected dateRangeFilter: " + dateRangeFilter);
            if (dateRangeFilter == null)
                return false;

            clearScreen();

            System.out.println("DEBUG: About to call setDatesFromFilter()");
            boolean dateResult = setDatesFromFilter();
            System.out.println("DEBUG: setDatesFromFilter() returned: " + dateResult);
            if (!dateResult)
                return false;

            clearScreen();
            System.out.println("DEBUG: gatherReportCriteria returning true");
            return true;

        } catch (Exception e) {
            System.out.println("Error gathering criteria: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private <T> T promptFilter(String label, Map<String, T> options) {
        while (true) {
            System.out.println("Select " + label + ":");
            options.forEach((key, value) -> System.out
                    .println("  " + key + ". " + (value != null ? value.toString().replace("_", " ") : "Back")));
            System.out.print("Choice: ");
            String input = scanner.nextLine().trim();
            if (options.containsKey(input))
                return options.get(input);
            System.out.println("[Invalid] Please enter a valid option.");
        }
    }

    private boolean setDatesFromFilter() {
        LocalDate today = LocalDate.now();
        System.out.println("DEBUG: setDatesFromFilter called with dateRangeFilter: " + dateRangeFilter);

        return switch (dateRangeFilter) {
            case TODAY -> {
                System.out.println("DEBUG: Processing TODAY case");
                startDate = endDate = today;
                yield true;
            }
            case THIS_WEEK -> {
                System.out.println("DEBUG: Processing THIS_WEEK case");
                startDate = today.minusDays(today.getDayOfWeek().getValue() - 1);
                endDate = today;
                yield true;
            }
            case THIS_MONTH -> {
                System.out.println("DEBUG: Processing THIS_MONTH case");
                startDate = today.withDayOfMonth(1);
                endDate = today;
                yield true;
            }
            case SPECIFIC_DATE_RANGE -> {
                System.out.println("DEBUG: Processing SPECIFIC_DATE_RANGE case");
                yield promptSpecificDateRange();
            }
            default -> {
                System.out.println("DEBUG: Unexpected dateRangeFilter value: " + dateRangeFilter);
                throw new IllegalStateException("Unexpected value: " + dateRangeFilter);
            }
        };
    }

    private boolean promptSpecificDateRange() {
        System.out.println("Enter Start Date:");
        startDate = promptDate();
        if (startDate == null)
            return false;

        System.out.println("Enter End Date:");
        endDate = promptDate();
        if (endDate == null || endDate.isBefore(startDate)) {
            System.out.println("End date cannot be before start date!");
            return false;
        }

        return true;
    }

    private LocalDate promptDate() {
        while (true) {
            System.out.print("Enter date (yyyy-MM-dd) or press Enter to go back: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty())
                return null;

            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("[Invalid] Use format yyyy-MM-dd (e.g., 2025-06-03).");
            }
        }
    }

    @Override
    protected List<ReshelvedItemDTO> fetchReportItems() {
        if (storeFilter == null || startDate == null || endDate == null) {
            throw new IllegalStateException("Cannot fetch data: storeFilter, startDate, or endDate is null.");
        }

        return switch (storeFilter) {
            case IN_STORE_ONLY -> service.getReshelvedItemsForInStoreRange(startDate, endDate);
            case ONLINE_ONLY -> service.getReshelvedItemsForOnlineRange(startDate, endDate);
            case COMBINED -> service.getAllReshelvedItemsRange(startDate, endDate);
        };
    }

    @Override
    protected void renderReport(List<ReshelvedItemDTO> items) {
        System.out.println("==============================================");
        System.out.println("      SYOS DAILY RESHELVED ITEMS REPORT      ");
        System.out.println("        (Items Moved from Batch to Shelf)    ");
        System.out.println("==============================================");
        System.out.println("Date Range: " + formatDateRange());
        System.out.println("Store Type: " + getStoreTypeDescription());
        System.out.println("----------------------------------------------");

        printTableHeader();

        if (items.isEmpty()) {
            System.out.println("No reshelving activities found for the selected criteria.");
        } else {
            int total = 0;
            for (ReshelvedItemDTO dto : items) {
                printRow(dto);
                total += dto.getQuantity();
            }
            System.out.println("----------------------------------------------");
            System.out.printf("Total Items Reshelved from Batches: %d%n", total);
        }

        System.out.println("==============================================");
    }

    private void printTableHeader() {
        if (storeFilter == StoreFilter.COMBINED) {
            System.out.printf("%-10s %-40s %8s%n", "Code", "Name", "Quantity");
        } else {
            System.out.printf("%-10s %-30s %8s%n", "Code", "Name", "Quantity");
        }
        System.out.println("----------------------------------------------");
    }

    private void printRow(ReshelvedItemDTO dto) {
        if (storeFilter == StoreFilter.COMBINED) {
            System.out.printf("%-10s %-40s %8d%n",
                    dto.getCode(), truncateName(dto.getName(), 40), dto.getQuantity());
        } else {
            System.out.printf("%-10s %-30s %8d%n",
                    dto.getCode(), truncateName(dto.getName(), 30), dto.getQuantity());
        }
    }

    private String formatDateRange() {
        return startDate.equals(endDate)
                ? startDate.toString()
                : startDate + " to " + endDate;
    }

    private String getStoreTypeDescription() {
        return switch (storeFilter) {
            case IN_STORE_ONLY -> "In-Store Reshelving Only";
            case ONLINE_ONLY -> "Online Reshelving Only";
            case COMBINED -> "Combined (In-Store + Online)";
        };
    }

    private String truncateName(String name, int maxLength) {
        return name.length() <= maxLength ? name : name.substring(0, maxLength - 3) + "...";
    }
}
