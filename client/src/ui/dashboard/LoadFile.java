
package ui.dashboard;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import okhttp3.*;
import session.UserSession;
import util.HttpClientUtil;
import dto.ProgramStatsDTO;
import utils.UiUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class LoadFile {

    private final OkHttpClient client = HttpClientUtil.getClient();

    public void loadProgram(ActionEvent event, DashboardController controller) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select XML File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));

        Window window = ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File selectedFile = chooser.showOpenDialog(window);
        if (selectedFile == null) return;

        try {
            String xmlContent = Files.readString(selectedFile.toPath(), StandardCharsets.UTF_8);
            String username = UserSession.getUsername();

            RequestBody xmlBody = RequestBody.create(xmlContent, MediaType.parse("application/xml; charset=utf-8"));
            Request xmlReq = new Request.Builder()
                    .url("http://localhost:8080/S-Emulator/request-program?uploader=" + username)
                    .post(xmlBody)
                    .build();

            Platform.runLater(() -> controller.xmlPathLabel.setText(selectedFile.getName()));

            client.newCall(xmlReq).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() ->
                            UiUtils.showError("Server error: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try (response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Platform.runLater(() ->
                                    UiUtils.showError("Server returned: " + response.code()));
                            return;
                        }

                        String json = response.body().string();

                        if (json.contains("\"error\"")) {
                            String error = new Gson().fromJson(json, java.util.Map.class).get("error").toString();
                            Platform.runLater(() -> UiUtils.showError("Server error: " + error));
                            return;
                        }

                        ProgramStatsDTO dto = new Gson().fromJson(json, ProgramStatsDTO.class);

                        Platform.runLater(() -> {
                            controller.statusLabel.setText("Program uploaded successfully: " + dto.getProgramName());

                            controller.fetchProgramsFromServer();
                            controller.fetchFunctionsFromServer();
                            controller.fetchUsers();

                            java.util.concurrent.Executors.newSingleThreadScheduledExecutor()
                                    .schedule(() -> Platform.runLater(() -> {
                                        controller.fetchProgramsFromServer();
                                        controller.fetchFunctionsFromServer();
                                        controller.fetchUsers();
                                    }), 1, TimeUnit.SECONDS);
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> UiUtils.showError("Failed to parse server response: " + e.getMessage()));
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            UiUtils.showError("Failed to send file: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

