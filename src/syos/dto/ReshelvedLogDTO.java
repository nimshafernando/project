package syos.dto;

import java.time.LocalDateTime;

public class ReshelvedLogDTO {
    private int id;
    private String itemCode;
    private int quantity;
    private String storeType;
    private LocalDateTime reshelvedAt;

    public ReshelvedLogDTO(int id, String itemCode, int quantity, String storeType, LocalDateTime reshelvedAt) {
        this.id = id;
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.storeType = storeType;
        this.reshelvedAt = reshelvedAt;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public LocalDateTime getReshelvedAt() {
        return reshelvedAt;
    }

    public void setReshelvedAt(LocalDateTime reshelvedAt) {
        this.reshelvedAt = reshelvedAt;
    }
}
