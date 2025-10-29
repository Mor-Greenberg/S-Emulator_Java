package session;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Window;
import logic.architecture.ArchitectureData;
import okhttp3.*;
import util.HttpClientUtil;

import java.io.IOException;
import java.util.Optional;

public class UserSession {

    private static String currentUsername;
    private static int userCredits = 0;
    private static Label creditsLabel;
    private ArchitectureData lastArchitecture;

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
    private static final UserSession INSTANCE = new UserSession();
    public static UserSession getInstance() { return INSTANCE; }

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

        int before = userCredits;
        userCredits = Math.max(0, userCredits - amount);

        System.out.println("[CLIENT] 💳 Deducted " + amount +
                " credits | Before=" + before + " | After=" + userCredits);

        updateCreditsLabel();

        try {
            RequestBody body = RequestBody.create(
                    String.valueOf(-amount),
                    MediaType.parse("text/plain")
            );

            Request request = new Request.Builder()
                    .url("http://localhost:8080/S-Emulator/credits")
                    .post(body)
                    .build();

            try (Response response = HttpClientUtil.getClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("[CLIENT]  Failed to update credits on server. Code: " + response.code());
                } else {
                    System.out.println("[CLIENT]  Server credits updated successfully by: -" + amount);
                }
            }

        } catch (Exception e) {
            System.err.println("[CLIENT]  Exception while updating credits: " + e.getMessage());
        }
    }


    public static void updateCreditsLabel() {
        if (creditsLabel != null) {
            Platform.runLater(() ->
                    creditsLabel.setText("Available Credits: " + userCredits)
            );
        }
    }
    public static boolean confirmReusePreviousArchitecture(Window owner, String archText, String mode) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Architecture");
        alert.setHeaderText(mode + " Mode");
        alert.setContentText("Continue with the previous architecture: " + archText + "?");

        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(ok, cancel);

        if (owner != null)
            alert.initOwner(owner);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ok;
    }


    public ArchitectureData getLastArchitecture() {
        return  lastArchitecture;
    }
    public void setLastArchitecture(ArchitectureData lastArchitecture) {
        this.lastArchitecture = lastArchitecture;
    }

    public static void refreshCreditsFromServerAsync() {
        try {
            Request req = new Request.Builder()
                    .url("http://localhost:8080/S-Emulator/credits")
                    .get()
                    .build();

            HttpClientUtil.getClient().newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    System.err.println("Failed to refresh credits: " + e.getMessage());
                }
                @Override public void onResponse(Call call, Response resp) throws IOException {
                    try (resp) {
                        if (!resp.isSuccessful()) return;
                        String txt = resp.body().string().trim();
                        try {
                            int serverCredits = Integer.parseInt(txt);
                            setUserCredits(serverCredits);   // זה גם יעדכן את ה־Label
                        } catch (NumberFormatException ignore) {}
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Exception while refreshing credits: " + e.getMessage());
        }
    }
}
