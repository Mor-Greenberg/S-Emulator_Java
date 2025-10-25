package ui.dashboard;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.UserRunEntryDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.*;
import session.UserSession;
import util.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;


public class UserHistory {
    public static void showUserHistoryPopup(String username) {
        String url = "http://localhost:8080/S-Emulator/api/user-history?username=" + username;
        Request request = new Request.Builder().url(url).build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.err.println("Failed to get history: " + response.code());
                    return;
                }
                String json = response.body().string();
                Type listType = new TypeToken<List<UserRunEntryDTO>>(){}.getType();
                List<UserRunEntryDTO> runs = new Gson().fromJson(json, listType);
                Platform.runLater(() -> openHistoryStage(username, runs));
            }

            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
    }
    private static void openHistoryStage(String username, List<UserRunEntryDTO> runs) {
        Stage popup = new Stage();
        popup.setTitle("Run History - " + username);

        TableView<UserRunEntryDTO> table = new TableView<>();
        table.getColumns().addAll(
                createColumn("Run #", "runId", 60),
                createColumn("Type", "runType", 100),
                createColumn("Program", "programName", 150),
                createColumn("Architecture", "architecture", 100),
                createColumn("Degree", "degree", 80),
                createColumn("Y value", "yValue", 100),
                createColumn("Cycles", "cycles", 80)
        );

        table.setItems(FXCollections.observableList(runs));

        VBox root = new VBox(10, table);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 700, 400);
        popup.setScene(scene);
        popup.show();
    }

    private static <T> TableColumn<UserRunEntryDTO, T> createColumn(String title, String property, int width) {
        TableColumn<UserRunEntryDTO, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        return col;
    }

    public static void sendRunToServer(UserRunEntryDTO dto) {
        String username = UserSession.getUsername();
        RequestBody body = RequestBody.create(
                new Gson().toJson(dto),
                MediaType.parse("application/json; charset=utf-8")
        );


        Request request = new Request.Builder()
                .url("http://localhost:8080/S-Emulator/api/add-run?username=" + username)
                .post(body)
                .build();

        HttpClientUtil.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                System.out.println("Run saved for " + username);
            }
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("Failed to save run: " + e.getMessage());
            }
        });
    }


}
