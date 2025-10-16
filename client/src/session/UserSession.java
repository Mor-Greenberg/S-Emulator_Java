package session;

public class UserSession {

    private static String currentUsername;

    public static void setUsername(String username) {
        currentUsername = username;
    }

    public static String getUsername() {
        return currentUsername;
    }

    public static void clear() {
        currentUsername = null;
    }
}
