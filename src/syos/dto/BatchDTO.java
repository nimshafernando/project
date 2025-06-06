package syos.dto;

import java.time.LocalDate;

public class BatchDTO {
    private int id;
    private String itemCode;
    private String name;
    private double sellingPrice;
    private int quantity;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    private int usedQuantity;

    // Constructor for new batches
    public BatchDTO(String itemCode, String name, double sellingPrice, int quantity,
            LocalDate purchaseDate, LocalDate expiryDate) {
        this.itemCode = itemCode;
        this.name = name;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.usedQuantity = 0;
    }

    // Constructor for existing batches (from database)
    public BatchDTO(int id, String itemCode, String name, double sellingPrice, int quantity,
            LocalDate purchaseDate, LocalDate expiryDate, int usedQuantity) {
        this.id = id;
        this.itemCode = itemCode;
        this.name = name;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.usedQuantity = usedQuantity;
    }

    // Constructor for simple batch creation (itemCode, quantity, expiryDate)
    public BatchDTO(String itemCode, int quantity, LocalDate expiryDate) {
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.purchaseDate = LocalDate.now(); // Set to current date
        this.usedQuantity = 0;
        this.name = ""; // Default empty name
        this.sellingPrice = 0.0; // Default price
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(int usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    public String getItemName() {
        return name;
    } // For compatibility

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        BatchDTO batchDTO = (BatchDTO) obj;

        return id == batchDTO.id &&
                Double.compare(batchDTO.sellingPrice, sellingPrice) == 0 &&
                quantity == batchDTO.quantity &&
                usedQuantity == batchDTO.usedQuantity &&
                java.util.Objects.equals(itemCode, batchDTO.itemCode) &&
                java.util.Objects.equals(name, batchDTO.name) &&
                java.util.Objects.equals(purchaseDate, batchDTO.purchaseDate) &&
                java.util.Objects.equals(expiryDate, batchDTO.expiryDate);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, itemCode, name, sellingPrice, quantity, purchaseDate, expiryDate,
                usedQuantity);
    }

    @Override
    public String toString() {
        return "BatchDTO{" +
                "id=" + id +
                ", itemCode='" + itemCode + '\'' +
                ", name='" + name + '\'' +
                ", sellingPrice=" + sellingPrice +
                ", quantity=" + quantity +
                ", purchaseDate=" + purchaseDate +
                ", expiryDate=" + expiryDate +
                ", usedQuantity=" + usedQuantity +
                '}';
    }
}
