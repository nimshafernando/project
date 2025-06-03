package syos.dto;

public class ReportItemDTO {
    private final String code;
    private final String name;
    private final int quantity;
    private final double revenue;

    public ReportItemDTO(String code, String name, int quantity, double revenue) {
        this.code = code;
        this.name = name;
        this.quantity = quantity;
        this.revenue = revenue;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getRevenue() {
        return revenue;
    }

    public static ReportItemDTO merge(ReportItemDTO a, ReportItemDTO b) {
        return new ReportItemDTO(
                a.code,
                a.name,
                a.quantity + b.quantity,
                a.revenue + b.revenue);
    }
}
