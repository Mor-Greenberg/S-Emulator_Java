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
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import jaxbV2.jaxb.v2.SProgram;
import logic.execution.ExecutionContextImpl;
import logic.program.Program;
import logic.xml.XmlLoader;
import okhttp3.*;
import session.UserSession;
import ui.executionBoard.ExecutionBoardController;
import util.HttpClientUtil;
import util.ProgramFetcher;
import utils.UiUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static utils.UiUtils.showAlert;
import static utils.UiUtils.showError;

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
    private Button executeProgramButton;
    private ProgramStatsDTO selectedProgram;


    @FXML
    public void initialize() {
        executeProgramButton.setDisable(true);
        programsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        programsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedProgram = newSelection;
                executeProgramButton.setDisable(false);
            }
        });


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
                fetchProgramsFromServer();
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
    private void addProgramToTable(ProgramStatsDTO programStats) {
        programsTable.getItems().add(programStats);
    }




    @FXML
    private void loadFilePressed(ActionEvent event) throws IOException {
        LoadFile loadFile = new LoadFile();
        loadFile.loadProgram(event, this);
    }

    @FXML
    private Button loadFileButton;
    @FXML
    void executeProgramPressed(ActionEvent event) {
        if (selectedProgram == null) {
            UiUtils.showError("Select a program first.");
            return;
        }

        String programName = selectedProgram.getProgramName();

        // 1️⃣ קודם ננסה להביא את התוכנית מהמפה המקומית
        Program localProgram = ExecutionContextImpl.getGlobalProgramMap().get(programName);
        if (localProgram != null) {
            openExecutionBoard(localProgram);
            return;
        }

        // 2️⃣ אם לא קיימת מקומית – נבקש אותה מהשרת
        String url = "http://localhost:8080/S-Emulator/request-program?name=" + programName;
        Request request = new Request.Builder().url(url).get().build();

        OkHttpClient client = HttpClientUtil.getClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> UiUtils.showError("Server error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Platform.runLater(() ->
                            UiUtils.showError("❌ Program unavailable on server."));
                    return;
                }

                String xml = response.body().string();

                try {
                    // 3️⃣ המרה מ־XML ל־Program
                    Program program = logic.xml.XmlLoader.fromXmlString(xml);

                    // 4️⃣ שמירה רק בזיכרון המקומי
                    ExecutionContextImpl.loadProgram(program, xml);
                    System.out.println("♻️ Loaded shared program from server: " + program.getName());

                    // 5️⃣ פתיחת מסך ההרצה
                    Platform.runLater(() -> openExecutionBoard(program));

                } catch (Exception e) {
                    Platform.runLater(() ->
                            UiUtils.showError("Failed to load program: " + e.getMessage()));
                    e.printStackTrace();
                }
            }
        });
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
    private void openExecutionBoard(Program program) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/executionBoard/S-Emulator-Execution.fxml"));
            Parent root = loader.load();

            ExecutionBoardController controller = loader.getController();
            controller.setLoadedProgram(program);
            controller.setProgramStats(selectedProgram);
            controller.setUserCredits(UserSession.getUserCredits());

            Stage stage = new Stage();
            stage.setTitle("Execution Board - " + program.getName());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            UiUtils.showError("Failed to open execution screen: " + e.getMessage());
            e.printStackTrace();
        }
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
    private Program fetchProgramFromUploader(String uploader, String programName) {
        String url = "http://localhost:8080/S-Emulator/uploaders/"
                + uploader + "/" + programName + ".xml";

        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;
            String xml = response.body().string();
            Program program = XmlLoader.fromXmlString(xml);
            ExecutionContextImpl.loadProgram(program);
            return program;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



}
