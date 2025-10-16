package ui.executionBoard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import session.UserSession;



public class ExecutionBoardController {
    @FXML
    public Label userNameField;
    @FXML
    public void initialize() {
        String username = UserSession.getUsername();
        userNameField.setText(username);

    }

}
