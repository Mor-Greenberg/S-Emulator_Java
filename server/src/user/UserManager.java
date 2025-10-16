package user;

import java.util.HashMap;
import java.util.Map;

public class UserManager {

    private final Map<String, User> users = new HashMap<>();

    public void addUser(String username) {
        users.putIfAbsent(username, new User(username));
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public int getCredits(String username) {
        User user = users.get(username);
        return (user != null) ? (int) user.getCredits() : 0;
    }

    public void addCredits(String username, int amount) {
        User user = users.get(username);
        if (user != null) {
            user.addCredits(amount);
        }
    }

    public void deductCredits(String username, int amount) {
        User user = users.get(username);
        if (user != null) {
            user.deductCredits(amount);
        }
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public Map<String, User> getAllUsers() {
        return users;
    }
}
