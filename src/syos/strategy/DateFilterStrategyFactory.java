package syos.strategy;

import syos.service.BillHistoryReportService.DateFilter;

public class DateFilterStrategyFactory {
    public static DateFilterStrategy getStrategy(DateFilter filter) {
        return switch (filter) {
            case TODAY -> new TodayFilterStrategy();
            case THIS_WEEK -> new ThisWeekFilterStrategy();
            case THIS_MONTH -> new ThisMonthFilterStrategy();
            case CUSTOM_RANGE, ALL_TIME -> new NoFilterStrategy();
        };
    }
}
