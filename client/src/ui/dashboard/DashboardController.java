package ui.dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import dto.ProgramStatsDTO;
import dto.UserStats;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import jaxbV2.jaxb.v2.SProgram;
import okhttp3.*;
import session.UserSession;
import util.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static utils.UiUtils.showAlert;

public class DashboardController {
    private SProgram loadedProgram;
    private SProgram mainProgram;

    @FXML
    public Label xmlPathLabel;

    @FXML
    public Label statusLabel;

    @FXML
    private ListView<String> usersListView;

    private final OkHttpClient client = HttpClientUtil.getClient();

    @FXML
    public Label userNameField;
    @FXML private TableView<ProgramStatsDTO> programsTable;
    @FXML private TableColumn<ProgramStatsDTO, String> programNameColumn;
    @FXML private TableColumn<ProgramStatsDTO, String> uploaderColumn;
    @FXML private TableColumn<ProgramStatsDTO, Integer> instructionCountColumn;
    @FXML private TableColumn<ProgramStatsDTO, Integer> maxDegreeColumn;
    @FXML private TableColumn<ProgramStatsDTO, Integer> runCountColumn;
    @FXML private TableColumn<ProgramStatsDTO, Double> avgCreditsColumn;


    @FXML
    private Label creditsLabel;
    @FXML private TableView<UserStats> usersTable;
    @FXML private TableColumn<UserStats, String> nameColumn;
    @FXML private TableColumn<UserStats, Integer> mainProgramsColumn;
    @FXML private TableColumn<UserStats, Integer> contributedFunctionsColumn;
    @FXML private TableColumn<UserStats, Integer> currentCreditsColumn;
    @FXML private TableColumn<UserStats, Integer> usedCreditsColumn;
    @FXML private TableColumn<UserStats, Integer> executionCountColumn;

    private String currentUserName;


    @FXML
    public void initialize() {
        String username = UserSession.getUsername();
        userNameField.setText(username);
        fetchUsers();
        loadCreditsFromServer();
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        mainProgramsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getMainPrograms()).asObject());
        contributedFunctionsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getContributedFunctions()).asObject());
        currentCreditsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getCurrentCredits()).asObject());
        usedCreditsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getUsedCredits()).asObject());
        executionCountColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getExecutionCount()).asObject());

        programNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProgramName()));
        uploaderColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUploaderName()));
        instructionCountColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getInstructionCount()).asObject());
        maxDegreeColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getMaxExpansionLevel()).asObject());
        runCountColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getRunCount()).asObject());
        avgCreditsColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getAverageCredits()).asObject());

        fetchProgramsFromServer();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchUsers();
            }
        }, 0, 1000);
    }

    private void fetchUsers() {
        Request request = new Request.Builder()
                .url("http://localhost:8080/S-Emulator/get-users")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    Type listType = new TypeToken<List<UserStats>>() {}.getType();
                    List<UserStats> users = new Gson().fromJson(json, listType);


                    Platform.runLater(() -> {
                        usersTable.getItems().setAll(users); // במקום usersListView
                    });

                }
            }
        });
    }

    @FXML
    private void loadFilePressed(ActionEvent event) throws IOException {
        LoadFile loadFile = new LoadFile();
        loadFile.loadProgram(event, this);
    }

    @FXML
    private Button loadFileButton;

    @FXML
    private void executeProgramPressed(ActionEvent event) {
        // פעולה כלשהי
    }

    @FXML
    private void executeFunctionPressed(ActionEvent event) {
        // פעולה כלשהי
    }

    private void updateCreditsLabel(int credits) {
        System.out.println("Updating label to: Available Credits: " + credits);
        Platform.runLater(() -> creditsLabel.setText("Available Credits: " + credits));
    }


    private void loadCreditsFromServer() {
        System.out.println("loadCreditsFromServer called");

        Request request = new Request.Builder()
                .url("http://localhost:8080/S-Emulator/credits")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("Failed to load credits: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int statusCode = response.code();
                System.out.println("Server responded with status code: " + statusCode);

                String responseBody = response.body() != null ? response.body().string() : null;
                System.out.println("Response body: " + responseBody);

                if (!response.isSuccessful()) {
                    System.err.println("Server returned error status: " + statusCode);
                    return;
                }

                if (responseBody == null || responseBody.isBlank()) {
                    System.err.println("Empty response body, but status was OK");
                    return;
                }

                try {
                    JsonElement element = JsonParser.parseString(responseBody);
                    if (!element.isJsonObject()) {
                        System.err.println("Response is not a JSON object: " + responseBody);
                        return;
                    }

                    JsonObject obj = element.getAsJsonObject();
                    int credits = obj.get("credits").getAsInt();

                    UserSession.setUserCredits(credits);
                    Platform.runLater(() -> creditsLabel.setText("Available Credits: " + credits));
                    UserSession.setUserCredits(credits);
                    updateCreditsLabel(credits);

                } catch (Exception e) {
                    System.err.println("Failed to parse JSON: " + e.getMessage());
                    System.err.println("Raw response: " + responseBody);
                }

            }
        });
    }


    @FXML
    private void handleChargeCredits() {
        TextInputDialog dialog = new TextInputDialog("50");
        dialog.setTitle("Charge Credits");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter amount to charge:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int amountToAdd = Integer.parseInt(input);
                if (amountToAdd <= 0) {
                    Platform.runLater(() ->
                            showAlert("Invalid Amount, Please enter a positive number.")
                    );
                    return;
                }

                RequestBody body = RequestBody.create(
                        String.valueOf(amountToAdd),
                        MediaType.parse("text/plain; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url("http://localhost:8080/S-Emulator/credits")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        System.err.println("Failed to charge credits: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String json = response.body().string();
                        if (json == null || json.isBlank()) return;
                        JsonElement element = JsonParser.parseString(json);
                        if (!element.isJsonObject()) return;
                        JsonObject obj = element.getAsJsonObject();
                        int updatedCredits = obj.get("credits").getAsInt();

                        UserSession.setUserCredits(updatedCredits);
                        updateCreditsLabel(updatedCredits);
                        Platform.runLater(() -> statusLabel.setText("Charged " + amountToAdd + " credits"));
                    }
                });
            } catch (NumberFormatException e) {
                showAlert("Invalid Input, Please enter a valid number.");
            }
        });
    }

    private void deductCredits(int amount) {
        RequestBody body = RequestBody.create(
                String.valueOf(-amount),
                MediaType.parse("text/plain; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("http://localhost:8080/S-Emulator/credits")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (json == null || json.isBlank()) {
                    System.err.println("Empty response from server");
                    return;
                }
                JsonElement element = JsonParser.parseString(json);
                if (!element.isJsonObject()) {
                    System.err.println("Response is not a JSON object: " + json);
                    return;
                }
                JsonObject obj = element.getAsJsonObject();

                int updatedCredits = obj.get("credits").getAsInt();

                UserSession.setUserCredits(updatedCredits);
                updateCreditsLabel(updatedCredits);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("Failed to deduct credits: " + e.getMessage());
            }
        });
    }
    void fetchProgramsFromServer() {

        Request request = new Request.Builder()
                .url("http://localhost:8080/S-Emulator/api/programs")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                Type listType = new TypeToken<List<ProgramStatsDTO>>(){}.getType();
                List<ProgramStatsDTO> programs = new Gson().fromJson(json, listType);

                Platform.runLater(() -> programsTable.getItems().setAll(programs));
            }
        });
    }
    public String getCurrentUserName() {
        return currentUserName;
    }


}
