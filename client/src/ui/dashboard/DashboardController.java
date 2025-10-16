package ui.dashboard;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import jaxbV2.jaxb.v2.SProgram;
import okhttp3.*;
import session.UserSession;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class DashboardController {
    private SProgram loadedProgram;
    private SProgram mainProgram;

    @FXML
    public Label xmlPathLabel;

    @FXML
    public Label statusLabel;
    @FXML
    private ListView<String> usersListView;

    private final OkHttpClient client = new OkHttpClient();

    @FXML
    public Label userNameField;


    @FXML
    public void initialize() {
        String username = UserSession.getUsername();
        userNameField.setText(username);
        fetchUsers();

        // רענון כל 5 שניות
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchUsers();
            }
        }, 0, 5000);
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
                    Type listType = new TypeToken<List<String>>() {}.getType();
                    List<String> users = new Gson().fromJson(json, listType);

                    javafx.application.Platform.runLater(() -> {
                        usersListView.getItems().setAll(users);
                    });
                }
            }
        });
    }


    @FXML
    private void loadFilePressed(ActionEvent event) throws IOException {
      LoadFile loadFile= new LoadFile();
      loadFile.loadProgram(event,this);

    }

    @FXML
    private Button loadFileButton;

    @FXML
    private void executeProgramPressed(ActionEvent event) {
        // פעולה כלשהי
    }

    @FXML
    private void executeFunctionPressed(ActionEvent event) {
        // TODO: implement function execution
    }

    @FXML
    private void chargeCreditsPressed() {
    }
}
