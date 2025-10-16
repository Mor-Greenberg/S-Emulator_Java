package ui.login;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import okhttp3.*;
import session.UserSession;
import ui.dashboard.DashboardController;
import util.HttpClientUtil;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private Label errorLabel;

    private final OkHttpClient client = HttpClientUtil.getClient();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            errorLabel.setText("Please enter a username.");
            return;
        }

        // Prepare HTTP request
        RequestBody body = RequestBody.create(
                username,
                MediaType.parse("text/plain; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("http://localhost:8080/S-Emulator/login")
                .post(body)
                .build();

        // Send async HTTP request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> errorLabel.setText("Connection failed."));
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string().trim();

                if (result.equals("OK")) {
                    Platform.runLater(() -> {
                        UserSession.setUsername(username);
                        goToDashboard(username);
                    });

                } else {
                    Platform.runLater(() ->
                            errorLabel.setText("Username already exists. Try another.")
                    );
                }
            }
        });
    }

    private void goToDashboard(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/dashboard/S-Emulator-Dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            DashboardController controller = loader.getController();
            controller.userNameField.setText(username);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("S-Emulator Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load dashboard.");
        }
    }
    @FXML
    private Button loginButton;

    @FXML
    public void initialize() {
        loginButton.setDefaultButton(true);
    }

}
