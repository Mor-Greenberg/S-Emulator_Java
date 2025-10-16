package ui.Dashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import jaxbV2.jaxb.v2.SProgram;

import java.io.IOException;


public class DashboardController {
    private SProgram loadedProgram;
    private SProgram mainProgram;

    @FXML
    public Label xmlPathLabel;

    @FXML
    public Label statusLabel;

    @FXML
    public void initialize() {
        // כאן תכניסי לוגיקה שתקרה כשהמסך נטען
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
