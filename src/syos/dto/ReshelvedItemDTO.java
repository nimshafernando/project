package syos.dto;

/**
 * Data Transfer Object for items to be reshelved.
 */
public class ReshelvedItemDTO {
    private final String code;
    private final String name;
    private final int quantity;

    public ReshelvedItemDTO(String code, String name, int quantity) {
        this.code = code;
        this.name = name;
        this.quantity = quantity;
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

    /**
     * Merge two DTOs with the same code/name.
     * (Not strictly needed if SQL groups correctly, but keeps OCP.)
     */
    public static ReshelvedItemDTO merge(ReshelvedItemDTO a, ReshelvedItemDTO b) {
        return new ReshelvedItemDTO(a.code, a.name, a.quantity + b.quantity);
    }
}
