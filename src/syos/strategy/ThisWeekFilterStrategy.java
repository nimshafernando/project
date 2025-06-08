package syos.strategy;

public class ThisWeekFilterStrategy implements DateFilterStrategy {
    @Override
    public void apply(StringBuilder sql, String columnName) {
        sql.append(columnName).append(" BETWEEN DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) AND CURDATE()");
    }
}
