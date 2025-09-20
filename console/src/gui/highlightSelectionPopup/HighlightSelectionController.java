package gui.highlightSelectionPopup;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;


public class HighlightSelectionController {
    @FXML
    private Button labelButton;

    @FXML
    private Button variableButton;
    @FXML
    private Button clearButton;


    private HighlightChoiceListener listener;

    public void setListener(HighlightChoiceListener listener) {
        this.listener = listener;
    }


    @FXML
    void onLabelClicked() {
        if (listener != null) {
            listener.onLabelChosen();
        }
        ((Stage) labelButton.getScene().getWindow()).close();
    }


    @FXML
    void onVariableClicked() {
        if (listener != null) {
            listener.onVariableChosen();
        }
        ((Stage) variableButton.getScene().getWindow()).close();
    }


    @FXML
    void onClearHighlight() {
        if (listener != null) {
            listener.onClearHighlight();
        }
        ((Stage) clearButton.getScene().getWindow()).close();
    }




}
