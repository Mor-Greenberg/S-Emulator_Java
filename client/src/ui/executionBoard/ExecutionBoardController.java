package ui.executionBoard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import okhttp3.OkHttpClient;
import session.UserSession;
import util.HttpClientUtil;


public class ExecutionBoardController {
    @FXML
    public Label userNameField;
    private final OkHttpClient client = HttpClientUtil.getClient();

    @FXML
    public void initialize() {
        String username = UserSession.getUsername();
        userNameField.setText(username);

    }

}
