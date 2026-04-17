package qcm.payloads;

public class LoginPayload {
    private String username;
    private String password;

    public LoginPayload(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}