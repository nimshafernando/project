package syos.dto;

public class OnlineUserDTO {
    private int id;
    private String username;
    private String password;
    private String contactNumber;
    private String address;

    // Constructor for login/basic usage
    public OnlineUserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Constructor for full user details
    public OnlineUserDTO(int id, String username, String password, String contactNumber, String address) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.contactNumber = contactNumber;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getAddress() {
        return address;
    }
}
