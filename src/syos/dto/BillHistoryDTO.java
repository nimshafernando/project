package syos.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for bill history information.
 * Follows SRP and immutability principles.
 */
public class BillHistoryDTO {
    private final int billId;
    private final String serialNumber;
    private final LocalDateTime dateTime;
    private final double totalAmount;
    private final double discount;
    private final String storeType;
    private final String paymentMethod;
    private final String customerInfo;
    private final String employeeName; // Add this field
    private final int itemCount;

    public BillHistoryDTO(int billId, String serialNumber, LocalDateTime dateTime,
            double totalAmount, double discount, String storeType,
            String paymentMethod, String customerInfo, String employeeName, int itemCount) {
        this.billId = billId;
        this.serialNumber = serialNumber;
        this.dateTime = dateTime;
        this.totalAmount = totalAmount;
        this.discount = discount;
        this.storeType = storeType;
        this.paymentMethod = paymentMethod;
        this.customerInfo = customerInfo;
        this.employeeName = employeeName;
        this.itemCount = itemCount;
    }

    public int getBillId() {
        return billId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getDiscount() {
        return discount;
    }

    public String getStoreType() {
        return storeType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getCustomerInfo() {
        return customerInfo;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public int getItemCount() {
        return itemCount;
    }

    public double getTotalBeforeDiscount() {
        return totalAmount + discount;
    }
}
