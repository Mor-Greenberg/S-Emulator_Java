package session;

public class UserSession {

    private static String currentUsername;
    private static int userCredits = 0 ;

    public static void setUsername(String username) {
        currentUsername = username;
    }

    public static String getUsername() {
        return currentUsername;
    }

    public static void clear() {
        currentUsername = null;
    }
    public static int getUserCredits() {
        return userCredits;
    }
    public static void setUserCredits(int inputUserCredits) {
        userCredits = inputUserCredits;
    }
    public static void addCredits(int amount) {
        userCredits += amount;

    }


}
