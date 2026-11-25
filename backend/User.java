/**
 * Represents a registered supervisor/admin user of the monitoring system.
 * Demonstrates ENCAPSULATION: fields are private, accessed only via
 * getters/setters.
 */
public class User {

    private String name;
    private String username;
    private String password; // in production: store a hash, never plaintext

    public User(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{name='" + name + "', username='" + username + "'}";
    }
}
