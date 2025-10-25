package session;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class UserSession {

    private static String currentUsername;
    private static int userCredits = 0;
    private static Label creditsLabel; // רפרנס לתווית ב-UI (אם נרצה לעדכן אותה ישירות)

    // --- ניהול משתמש ---
    public static void setUsername(String username) {
        currentUsername = username;
    }

    public static String getUsername() {
        return currentUsername;
    }

    public static void clear() {
        currentUsername = null;
        userCredits = 0;
    }

    // --- קרדיטים ---
    public static int getUserCredits() {
        return userCredits;
    }

    public static void setUserCredits(int inputUserCredits) {
        userCredits = inputUserCredits;
        updateCreditsLabel();
    }

    public static void addCredits(int amount) {
        userCredits += amount;
        updateCreditsLabel();
    }

    public static void deductCredits(int amount) {
        userCredits = Math.max(0, userCredits - amount);
        updateCreditsLabel();
    }

    // --- ניהול תווית ב-UI ---
    public static void bindCreditsLabel(Label label) {
        creditsLabel = label;
        updateCreditsLabel();
    }

    public static void updateCreditsLabel() {
        if (creditsLabel != null) {
            Platform.runLater(() ->
                    creditsLabel.setText("Available Credits: " + userCredits)
            );
        }
    }
}
