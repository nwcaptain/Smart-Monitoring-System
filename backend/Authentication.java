import java.util.HashMap;
import java.util.Map;

/**
 * Handles registration and login. Kept separate from User (single
 * responsibility) and supports multiple registered users, matching the
 * Registration/Login screens in the frontend.
 */
public class Authentication {

    private final Map<String, User> registeredUsers = new HashMap<>();

    public Authentication() {
        // Seed with the default admin credentials used in the frontend demo
        // (username: admin / password: admin123)
        registeredUsers.put("admin", new User("Administrator", "admin", "admin123"));
    }

    /**
     * Registers a new user. Returns false if the username is already taken
     * or the passwords do not match (mirrors the "Confirm Password" field).
     */
    public boolean register(String name, String username, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            return false;
        }
        if (registeredUsers.containsKey(username)) {
            System.out.println("Username already exists.");
            return false;
        }
        registeredUsers.put(username, new User(name, username, password));
        System.out.println("Registration successful.");
        return true;
    }

    /**
     * Validates credentials. Returns the logged-in User on success, or null
     * on failure (invalid username or password).
     */
    public User login(String username, String password) {
        User user = registeredUsers.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
}
