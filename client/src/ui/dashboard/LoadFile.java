package ui.dashboard;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import okhttp3.*;
import session.UserSession;
import util.HttpClientUtil;
import logic.xml.XmlLoader;
import logic.program.Program;
import dto.ProgramStatsDTO;
import utils.UiUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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
            String xml = Files.readString(selectedFile.toPath(), StandardCharsets.UTF_8);
            Program program = XmlLoader.fromXmlString(xml);

            program.setUploaderName(UserSession.getUsername());

            logic.execution.ExecutionContextImpl.loadProgram(program, xml);

            Platform.runLater(() ->
                    controller.xmlPathLabel.setText(selectedFile.getName())
            );

            RequestBody xmlBody = RequestBody.create(xml, MediaType.parse("application/xml; charset=utf-8"));
            Request xmlReq = new Request.Builder()
                    .url("http://localhost:8080/S-Emulator/request-program?name=" + program.getName())
                    .post(xmlBody)
                    .build();

            client.newCall(xmlReq).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() ->
                            UiUtils.showError("❌ Server error: " + e.getMessage())
                    );
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try (response) { // סוגר אוטומטית את ה־Response
                        if (!response.isSuccessful()) {
                            Platform.runLater(() ->
                                    UiUtils.showError("❌ Server returned: " + response.code())
                            );
                            return;
                        }

                        // יצירת DTO עם נתונים ל־ProgramStatsServlet
                        ProgramStatsDTO dto = new ProgramStatsDTO(
                                program.getName(),
                                UserSession.getUsername(),
                                program.getInstructions().size(),
                                program.calculateMaxDegree(),
                                program.getRunCount(),
                                0.0 // ✅ לא ניגשים ל־UserManager בצד הלקוח!
                        );

                        RequestBody statsBody = RequestBody.create(
                                new Gson().toJson(dto),
                                MediaType.parse("application/json; charset=utf-8")
                        );

                        Request statsReq = new Request.Builder()
                                .url("http://localhost:8080/S-Emulator/api/programs")
                                .post(statsBody)
                                .build();

                        client.newCall(statsReq).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                System.err.println("❌ Failed to update shared table: " + e.getMessage());
                            }

                            @Override
                            public void onResponse(Call call, Response res) {
                                res.close();
                                Platform.runLater(() -> {
                                    controller.statusLabel.setText(
                                            "✅ Program uploaded successfully: " + program.getName());
                                    controller.fetchProgramsFromServer();
                                    controller.fetchFunctionsFromServer();
                                    controller.updateFunctionsTableFromLoadedProgram();



                                    // 🕓 דחייה קצרה כדי לאפשר לשרת לעדכן את המפה לפני הקריאה
                                    java.util.concurrent.Executors.newSingleThreadScheduledExecutor()
                                            .schedule(() -> Platform.runLater(() -> {
                                                System.out.println("🔁 Refreshing functions after upload...");
                                                controller.updateFunctionsTableFromLoadedProgram();
                                            }), 1, java.util.concurrent.TimeUnit.SECONDS);
                                });
                            }

                        });
                    }
                }
            });

        } catch (Exception e) {
            UiUtils.showError("Failed to load program: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
