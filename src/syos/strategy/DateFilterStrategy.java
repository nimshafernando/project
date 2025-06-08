package syos.strategy;

public interface DateFilterStrategy {
    void apply(StringBuilder sql, String columnName);
}
