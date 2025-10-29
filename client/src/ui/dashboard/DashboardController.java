package ui.dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import dto.FunctionDTO;
import dto.ProgramStatsDTO;
import dto.UserStatsDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import jaxbV2.jaxb.v2.SProgram;
import logic.execution.ExecutionContextImpl;
import logic.execution.ExecutionRunner;
import logic.program.Program;
import logic.xml.XmlLoader;
import okhttp3.*;
import session.UserSession;
import ui.executionBoard.ExecutionBoardController;
import util.HttpClientUtil;
import utils.UiUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static utils.UiUtils.showAlert;

public class DashboardController {
    private SProgram loadedProgram;
    private SProgram mainProgram;

    @FXML
    public Label xmlPathLabel;

    @FXML
    public Label statusLabel;

    @FXML Button executeFunctionButton;
    @FXML
    private ListView<String> usersListView;

    private final OkHttpClient client = HttpClientUtil.getClient();

    final ObservableList<FunctionDTO> functionList = FXCollections.observableArrayList();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    FunctionDTO selectedFunction;

    @FXML
    public Label userNameField;
    @FXML
    TableView<ProgramStatsDTO> programsTable;
    @FXML
    TableColumn<ProgramStatsDTO, String> programNameColumn;
    @FXML
    TableColumn<ProgramStatsDTO, String> uploaderColumn;
    @FXML
    TableColumn<ProgramStatsDTO, Integer> instructionCountColumn;
    @FXML
    TableColumn<ProgramStatsDTO, Integer> maxDegreeColumn;
    @FXML
    TableColumn<ProgramStatsDTO, Integer> runCountColumn;
    @FXML
    TableColumn<ProgramStatsDTO, Double> avgCreditsColumn;

    // --- Functions Table ---
    @FXML
    TableView<FunctionDTO> functionsTable;
    @FXML
    TableColumn<FunctionDTO, String> colFunctionName;
    @FXML
    TableColumn<FunctionDTO, String> colProgramName;
    @FXML
    TableColumn<FunctionDTO, String> colUploader;
    @FXML
    TableColumn<FunctionDTO, Number> colNumInstructions;
    @FXML
    TableColumn<FunctionDTO, Number> colMaxDegree;



    @FXML
    Label creditsLabel;
    @FXML
    TableView<UserStatsDTO> usersTable;
    @FXML
    TableColumn<UserStatsDTO, String> nameColumn;
    @FXML
    TableColumn<UserStatsDTO, Integer> mainProgramsColumn;
    @FXML
    TableColumn<UserStatsDTO, Integer> contributedFunctionsColumn;
    @FXML
    TableColumn<UserStatsDTO, Integer> currentCreditsColumn;
    @FXML
    TableColumn<UserStatsDTO, Integer> usedCreditsColumn;
    @FXML
    TableColumn<UserStatsDTO, Integer> executionCountColumn;

    private String currentUserName;
    @FXML
    public Button executeProgramButton;
    ProgramStatsDTO selectedProgram;


    public static void refreshProgramsFromServer() {
        if (instance != null) {
            Platform.runLater(() -> {
                instance.fetchProgramsFromServer();
            });
        } else {
            System.err.println("⚠ DashboardController instance is null");
        }
    }

    private static DashboardController instance;

    public static DashboardController getInstance() {
        return instance ;
    }


    @FXML
    public void initialize() {
        instance = this;
        InitDashboardHelper.initialize(this);
    }
    public void initAfterLogin() {
        if (userSession != null) {
            int credits = userSession.getUserCredits();
            creditsLabel.setText("Available Credits: " + credits);
            fetchProgramsFromServer();
            fetchUsers();
        }
    }

    private UserSession userSession;

    public void setUserSession(UserSession session) {
        this.userSession = session;
        userNameField.setText(session.getUsername());
    }
    public UserSession getUserSession() {
        return userSession;
    }




    void fetchUsers() {
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
                    Type listType = new TypeToken<List<UserStatsDTO>>() {}.getType();
                    List<UserStatsDTO> users = new Gson().fromJson(json, listType);


                    Platform.runLater(() -> {
                        usersTable.getItems().setAll(users);
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
    void executeProgramPressed(ActionEvent event) {
        if (selectedProgram == null) {
            UiUtils.showError("Select a program first.");
            return;
        }

        String programName = selectedProgram.getProgramName();
        String username = userSession.getUsername();

        Program localProgram = ExecutionContextImpl.getGlobalProgramMap().get(programName);
        if (localProgram != null) {
            openExecutionBoard(localProgram);
            return;
        }

        String url = "http://localhost:8080/S-Emulator/load-program?name=" + programName;
        Request request = new Request.Builder().url(url).get().build();

        OkHttpClient client = HttpClientUtil.getClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> UiUtils.showError("Server error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        Platform.runLater(() ->
                                UiUtils.showError("Program unavailable on server (HTTP " + response.code() + ")."));
                        return;
                    }

                    String xml = response.body().string();

                    try {
                        Program program = safeLoadProgram(xml, username);
                        ExecutionContextImpl.loadProgram(program, xml);

                        System.out.println(" Loaded program from server: " + program.getName());
                        Platform.runLater(() -> openExecutionBoard(program));

                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() ->
                                UiUtils.showError("Failed to parse XML: " + e.getMessage()));
                    }
                }
            }
        });
    }

    private void updateCreditsLabel(int credits) {
        Platform.runLater(() -> creditsLabel.setText("Available Credits: " + credits));
    }


    void loadCreditsFromServer() {
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

                String responseBody = response.body() != null ? response.body().string() : null;

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

                    userSession.setUserCredits(credits);
                    Platform.runLater(() -> creditsLabel.setText("Available Credits: " + credits));
                    userSession.setUserCredits(credits);
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
            controller.setUserSession(this.userSession);
            controller.initAfterSession();


            // get controller

            controller.setLoadedProgram(program);
            controller.setOriginalInstructions(program.getInstructions());
            controller.setUserCredits(userSession.getUserCredits());
            controller.architecture = null;

            Stage stage = new Stage();
            stage.setTitle("Execution Board – " + program.getName());
            stage.setScene(new Scene(root));
            stage.setOnHidden(event -> {
                refreshCreditsFromSession();
                fetchUsers();
                fetchProgramsFromServer();
            });

            stage.show();

            Stage currentStage = (Stage) executeProgramButton.getScene().getWindow(); // או userNameField
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            UiUtils.showError("Failed to open Execution Board: " + e.getMessage());
        }
    }



    @FXML
    private void handleChargeCredits() {
        TextInputDialog dialog = new TextInputDialog("5000");
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

                        userSession.setUserCredits(updatedCredits);
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

                userSession.setUserCredits(updatedCredits);
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
            Program program = XmlLoader.fromXmlString(xml,userSession.getUsername());
            ExecutionContextImpl.loadProgram(program);
            return program;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @FXML
    private void onExecuteFunctionClicked(ActionEvent event) {
        if (selectedFunction == null) {
            UiUtils.showError("Select a function first.");
            return;
        }

        String functionName = selectedFunction.getFunctionName();

        Program localFunction = ExecutionContextImpl.getGlobalProgramMap().get(functionName);
        if (localFunction != null) {
            openExecutionBoard(localFunction);
            return;
        }

        String url = "http://localhost:8080/S-Emulator/request-program?name=" + functionName;
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
                            UiUtils.showError("Function unavailable on server."));
                    return;
                }

                String xml = response.body().string();

                try {
                    Program functionProgram = logic.xml.XmlLoader.fromXmlString(xml,userSession.getUsername());

                    ExecutionContextImpl.loadProgram(functionProgram, xml);

                    Platform.runLater(() -> openExecutionBoard(functionProgram));

                } catch (Exception e) {
                    Platform.runLater(() ->
                            UiUtils.showError("Failed to load function: " + e.getMessage()));
                    e.printStackTrace();
                }
            }
        });
    }

    private long lastKnownUpdate = 0;

    void startFunctionsAutoRefresh() {
        OkHttpClient client = HttpClientUtil.getClient();
        Timer timer = new Timer(true);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Request req = new Request.Builder()
                        .url("http://localhost:8080/S-Emulator/functions/last-update")
                        .get().build();

                try (Response res = client.newCall(req).execute()) {
                    if (res.isSuccessful() && res.body() != null) {
                        long serverTime = new Gson().fromJson(res.body().string(), Map.class)
                                .get("lastUpdate") instanceof Double d ? d.longValue() : 0L;

                        if (serverTime > lastKnownUpdate) {
                            lastKnownUpdate = serverTime;
                            Platform.runLater(() -> updateFunctionsTableFromServer());
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Auto-refresh failed: " + e.getMessage());
                }
            }
        }, 0, 2000);
    }

    public void updateFunctionsTableFromServer() {
        String url = "http://localhost:8080/S-Emulator/functions";

        OkHttpClient client = HttpClientUtil.getClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() ->
                        UiUtils.showError("Failed to fetch functions: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Platform.runLater(() ->
                            UiUtils.showError("Server returned error while fetching functions"));
                    return;
                }

                String json = response.body().string();
                response.close();

                FunctionDTO[] funcs = new Gson().fromJson(json, FunctionDTO[].class);

                Platform.runLater(() -> {
                    functionList.clear();
                    functionList.addAll(Arrays.asList(funcs));
                });
            }
        });
    }

    @FXML
    private void onClose() {
        scheduler.shutdownNow();
    }
    public void updateFunctionsTableFromLoadedProgram() {
        Request request = new Request.Builder()
                .url("http://localhost:8080/S-Emulator/functions")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("Failed to fetch functions: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;

                String json = response.body().string();
                Type listType = new TypeToken<List<FunctionDTO>>() {}.getType();
                List<FunctionDTO> allFunctions = new Gson().fromJson(json, listType);

                String currentProgramName = xmlPathLabel.getText().replace(".xml", "").toLowerCase();


                List<FunctionDTO> programFunctions = allFunctions.stream()
                        .filter(f -> f.getProgramName() != null && f.getProgramName().equalsIgnoreCase(currentProgramName))
                        .toList();


                Platform.runLater(() -> functionsTable.getItems().setAll(programFunctions));
            }
        });
    }
    public void fetchFunctionsFromServer() {
        String url = "http://localhost:8080/S-Emulator/functions";
        Request request = new Request.Builder().url(url).get().build();

        HttpClientUtil.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> UiUtils.showError("Server error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Platform.runLater(() -> UiUtils.showError("Failed to load functions from server"));
                    return;
                }

                String json = response.body().string();
                FunctionDTO[] functions = new Gson().fromJson(json, FunctionDTO[].class);

                Platform.runLater(() -> {
                    functionList.clear();
                    functionList.addAll(Arrays.asList(functions));
                });
            }
        });
    }
    UserStatsDTO selectedUser;

    @FXML
    private void onShowHistoryClicked() {
        UserStatsDTO selected = selectedUser;
        String username;

        if (selected != null) {
            username = selected.getName();
        } else {
            username = userSession.getUsername();
        }

        UserHistory.showUserHistoryPopup(username);
    }

    @FXML
    private void onUnselectUserClicked() {
        usersTable.getSelectionModel().clearSelection();
        selectedUser = null;

        usersTable.refresh();


    }
    private Program safeLoadProgram(String xml, String username) throws Exception {
        try {
            return XmlLoader.fromXmlString(xml, username);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (!msg.contains("undefined function")) throw e;

            List<String> missingFunctions = Arrays.stream(msg.split("\n"))
                    .filter(line -> line.contains("undefined function"))
                    .map(line -> line.substring(line.lastIndexOf(":") + 1).trim())
                    .distinct()
                    .toList();


            for (String funcName : missingFunctions) {
                try {
                    String funcUrl = "http://localhost:8080/S-Emulator/load-program?name=" + funcName;
                    Request req = new Request.Builder().url(funcUrl).get().build();

                    try (Response res = HttpClientUtil.getClient().newCall(req).execute()) {
                        if (res.isSuccessful() && res.body() != null) {
                            String funcXml = res.body().string();

                            try {
                                Program funcProg = XmlLoader.fromXmlString(funcXml, username);
                                ExecutionContextImpl.loadProgram(funcProg, funcXml);
                                System.out.println(" Loaded missing dependency: " + funcName);
                            } catch (Exception innerEx) {
                                System.out.println(" Failed to map function " + funcName + ": " + innerEx.getMessage());
                            }
                        } else {
                            System.out.println(" Could not load " + funcName + " from server. HTTP " + res.code());
                        }
                    }
                } catch (Exception ex) {
                    System.out.println(" Error fetching " + funcName + ": " + ex.getMessage());
                }
            }

            System.out.println("Retrying XML load after fetching missing dependencies...");
            return XmlLoader.fromXmlString(xml, username,true);
        }
    }



    public static void refreshUserHistory() {
        if (instance != null) {
            Platform.runLater(() -> {
                try {
                    if (instance != null && instance.userSession != null) {
                        ui.dashboard.UserHistory.refreshUserHistory(instance.userSession.getUsername());
                    }
                    instance.fetchProgramsFromServer();
                    instance.fetchUsers();
                    instance.refreshCreditsFromSession();
                } catch (Exception e) {
                    System.err.println("Failed to refresh user history: " + e.getMessage());
                }
            });
        } else {
            System.err.println("DashboardController instance is null (cannot refresh history)");
        }
    }

    public void refreshCreditsFromSession() {
        int credits = userSession.getUserCredits();
        Platform.runLater(() -> creditsLabel.setText("Available Credits: " + credits));
    }



}
