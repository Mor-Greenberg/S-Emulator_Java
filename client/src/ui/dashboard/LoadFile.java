package ui.dashboard;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.event.ActionEvent;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static utils.UiUtils.*;

public class LoadFile {

    public void loadProgram(ActionEvent event, Object controller) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));

        Window window = ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile != null) {
            if (controller instanceof DashboardController dashboardController) {
                Platform.runLater(() -> {
                    dashboardController.xmlPathLabel.setText(selectedFile.getName());
                    dashboardController.statusLabel.setText("Uploading...");
                });
            }

            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());

                RequestBody requestBody = RequestBody.create(
                        fileContent,
                        MediaType.parse("application/xml")
                );

                Request request = new Request.Builder()
                        .url("http://localhost:8080/S-Emulator/load-program")
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Platform.runLater(() -> {
                            showError("Failed to upload file: " + e.getMessage());
                            if (controller instanceof DashboardController dc)
                                dc.statusLabel.setText("Upload failed");
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseText = response.body() != null ? response.body().string() : "(no response)";
                        Platform.runLater(() -> {
                            if (response.isSuccessful()) {
                                showInfo("Server response:\n" + responseText);
                                if (controller instanceof DashboardController dc)
                                    dc.statusLabel.setText("File uploaded successfully");
                            } else {
                                showError("Server returned error " + response.code() + ":\n" + responseText);
                                if (controller instanceof DashboardController dc)
                                    dc.statusLabel.setText("Validation error");
                            }
                        });
                    }
                });

            } catch (IOException e) {
                Platform.runLater(() -> {
                    showAlert("Error reading file: " + e.getMessage());
                    if (controller instanceof DashboardController dc)
                        dc.statusLabel.setText("Error reading file");
                });
            }
        }
    }
}
