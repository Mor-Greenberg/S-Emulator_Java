package session;

import javafx.application.Platform;
import javafx.scene.control.Label;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import util.HttpClientUtil;

public class UserSession {

    private static String currentUsername;
    private static int userCredits = 0;
    private static Label creditsLabel;

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
        if (amount <= 0) return;

        userCredits = Math.max(0, userCredits - amount);
        updateCreditsLabel();

        try {
            RequestBody body = RequestBody.create(
                    String.valueOf(-amount),
                    MediaType.parse("text/plain")
            );

            Request request = new Request.Builder()
                    .url("http://localhost:8080/S-Emulator/credits") // 👈 אם יש לך BASE_URL אחר, שימי אותו כאן
                    .post(body)
                    .build();

            try (Response response = HttpClientUtil.getClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("Failed to update credits on server. Code: " + response.code());
                } else {
                    System.out.println("Server credits updated successfully by: -" + amount);
                }
            }

        } catch (Exception e) {
            System.err.println("Exception while updating credits: " + e.getMessage());
        }
    }
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
