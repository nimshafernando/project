package syos.dto;

public class OnlineUserDTO {
    private String username;
    private String password;

    public OnlineUserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
