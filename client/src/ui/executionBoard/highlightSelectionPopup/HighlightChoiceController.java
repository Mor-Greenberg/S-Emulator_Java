package ui.executionBoard.highlightSelectionPopup;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.util.List;

public class HighlightChoiceController {
    @FXML
    private ComboBox<String> comboBox;

    private String selectedChoice;


    private List<String> choicesToSet = null;


    @FXML
    public void initialize() {
        if (choicesToSet != null) {
            comboBox.setItems(FXCollections.observableArrayList(choicesToSet));
        }
    }

    public void setChoices(List<String> choices) {
        if (comboBox != null) {
            comboBox.setItems(FXCollections.observableArrayList(choices));
        } else {
            choicesToSet = choices;
        }
    }



    public String getSelectedChoice() {
        return selectedChoice;
    }

    @FXML
    private void onOkButtonClicked() {
        selectedChoice = comboBox.getValue();
        ((Stage) comboBox.getScene().getWindow()).close();
    }
}
