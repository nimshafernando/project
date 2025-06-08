package syos.strategy;

public class ThisMonthFilterStrategy implements DateFilterStrategy {
    @Override
    public void apply(StringBuilder sql, String columnName) {
        sql.append("YEAR(").append(columnName).append(") = YEAR(CURDATE()) AND MONTH(")
                .append(columnName).append(") = MONTH(CURDATE())");
    }
}
