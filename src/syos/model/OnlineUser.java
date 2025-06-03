package syos.model;

/**
 * OnlineUser model for managing online customer data
 * Implements SOLID principles: SRP (user data management),
 * OCP (extensible via inheritance), LSP (substitutable),
 * ISP (focused interface), DIP (no direct dependencies)
 */
public class OnlineUser {
    private int id;
    private String username;
    private String password;
    private String email;
    private String contactNumber;
    private String address;

    // === Essential Constructors ===

    /**
     * Full constructor (used when retrieving from database)
     */
    public OnlineUser(int id, String username, String email, String contactNumber, String address) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.contactNumber = contactNumber;
        this.address = address;
    }

    /**
     * Login constructor (most commonly used)
     */
    public OnlineUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Registration constructor
     */
    public OnlineUser(String username, String password, String contactNumber, String address) {
        this.username = username;
        this.password = password;
        this.contactNumber = contactNumber;
        this.address = address;
    }

    /**
     * Username-only constructor (for bill tagging)
     */
    public OnlineUser(String username) {
        this.username = username;
    }

    // === Core Getters ===
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username != null ? username : "";
    }

    public String getPassword() {
        return password != null ? password : "";
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public String getContactNumber() {
        return contactNumber != null ? contactNumber : "";
    }

    public String getAddress() {
        return address != null ? address : "";
    }

    // === Core Setters ===
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // === Essential Business Logic ===

    /**
     * Validate user credentials
     */
    public boolean isPasswordMatch(String inputPassword) {
        return this.password != null &&
                inputPassword != null &&
                this.password.equals(inputPassword);
    }

    /**
     * Check if user has minimum required data
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
                password != null && !password.trim().isEmpty();
    }

    // === Utility Methods ===

    @Override
    public String toString() {
        return String.format("OnlineUser{id=%d, username='%s', email='%s'}",
                id, getUsername(), getEmail());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        OnlineUser that = (OnlineUser) obj;
        return id == that.id &&
                getUsername().equals(that.getUsername());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, getUsername());
    }
}
