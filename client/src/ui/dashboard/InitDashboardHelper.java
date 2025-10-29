package ui.dashboard;

import dto.FunctionDTO;
import dto.ProgramStatsDTO;
import dto.UserStatsDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import session.UserSession;

import java.util.Timer;
import java.util.TimerTask;

import static ui.dashboard.UserHistory.showUserHistoryPopup;


public class InitDashboardHelper {

    public static void initialize(DashboardController controller) {
        controller.usersTable.setRowFactory(tv -> {
            TableRow<UserStatsDTO> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    controller.selectedUser = row.getItem();
                    controller.usersTable.refresh();
                }
            });

            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                updateRowStyle(row, controller);
            });

            row.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                updateRowStyle(row, controller);
            });

            return row;
        });


        // --- Disable both buttons initially ---
        controller.executeProgramButton.setDisable(true);
        controller.executeFunctionButton.setDisable(true);

        // Programs Table
        controller.programsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        controller.programsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                controller.selectedProgram = newSelection;
                controller.executeProgramButton.setDisable(false);
            } else {
                controller.selectedProgram = null;
                controller.executeProgramButton.setDisable(true);
            }
        });

        controller.programsTable.setRowFactory(tv -> {
            TableRow<ProgramStatsDTO> row = new TableRow<>();
            row.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
                if (isHovered && !row.isEmpty() && !row.getItem().equals(controller.selectedProgram)) {
                    row.setStyle("-fx-background-color: derive(#87CEFA, 30%);");
                } else if (!row.isEmpty() && !row.getItem().equals(controller.selectedProgram)) {
                    row.setStyle("");
                }
            });
            return row;
        });

        // Functions table
        controller.functionsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        controller.functionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                controller.selectedFunction = newSelection;
                controller.executeFunctionButton.setDisable(false);
            } else {
                controller.selectedFunction = null;
                controller.executeFunctionButton.setDisable(true);
            }
        });

        controller.functionsTable.setRowFactory(tv -> {
            TableRow<FunctionDTO> row = new TableRow<>();
            row.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
                if (isHovered && !row.isEmpty() && !row.getItem().equals(controller.selectedFunction)) {
                    row.setStyle("-fx-background-color: derive(#87CEFA, 30%);");
                } else if (!row.isEmpty() && !row.getItem().equals(controller.selectedFunction)) {
                    row.setStyle("");
                }
            });
            return row;
        });

        // Present UserName
        String username = UserSession.getUsername();
        controller.userNameField.setText(username);



        controller.fetchUsers();
        int credits = UserSession.getUserCredits();
        controller.creditsLabel.setText("Available Credits: " + credits);
        controller.fetchProgramsFromServer();
        controller.programsTable.refresh();


        // User's Table
        controller.nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        controller.mainProgramsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getMainPrograms()).asObject());
        controller.contributedFunctionsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getContributedFunctions()).asObject());
        controller.currentCreditsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getCurrentCredits()).asObject());
        controller.usedCreditsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getUsedCredits()).asObject());
        controller.executionCountColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getExecutionCount()).asObject());

        // Program's table columns
        controller.programNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProgramName()));
        controller.uploaderColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUploaderName()));
        controller.instructionCountColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getInstructionCount()).asObject());
        controller.maxDegreeColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getMaxExpansionLevel()).asObject());
        controller.runCountColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getRunCount()).asObject());
        controller.avgCreditsColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getAverageCredits()).asObject());

        controller.avgCreditsColumn.setCellValueFactory(
                cell -> new SimpleDoubleProperty(cell.getValue().getAverageCredits()).asObject()
        );
        controller.avgCreditsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", value));
                }
            }
        });


        // Function's table columns
        controller.colFunctionName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFunctionName()));
        controller.colProgramName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProgramName()));
        controller.colUploader.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUploader()));
        controller.colNumInstructions.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNumInstructions()));
        controller.colMaxDegree.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getMaxDegree()));

        controller.functionsTable.setItems(controller.functionList);
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (controller.usersTable.getScene() == null) {
                    cancel();
                    return;
                }
                Platform.runLater(() -> {
                    controller.fetchUsers();
                    controller.fetchProgramsFromServer();
                    controller.fetchFunctionsFromServer();
                });
            }
        }, 3000, 3000);

    }
    private static void updateRowStyle(TableRow<UserStatsDTO> row, DashboardController controller) {
        UserStatsDTO current = row.getItem();

        if (current == null) {
            row.setStyle("");
        } else if (current.equals(controller.selectedUser)) {
            row.setStyle("-fx-background-color: #87CEFA; -fx-font-weight: bold;");
        } else {
            row.setStyle("");
        }
    }

}
