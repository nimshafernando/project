package syos.strategy;

public class NoFilterStrategy implements DateFilterStrategy {
    @Override
    public void apply(StringBuilder sql, String columnName) {
        // No condition
    }
}
