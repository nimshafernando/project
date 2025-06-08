package syos.strategy;

public class TodayFilterStrategy implements DateFilterStrategy {
    @Override
    public void apply(StringBuilder sql, String columnName) {
        sql.append(columnName).append(" = CURDATE()");
    }
}
