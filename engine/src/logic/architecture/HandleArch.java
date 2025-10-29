package logic.architecture;

import javafx.scene.control.ChoiceDialog;
import session.UserSession;
import utils.UiUtils;

import java.util.Optional;

import static logic.execution.ExecutionRunner.architecture;

public class HandleArch {
    public static boolean ensureArchitectureSelected() {
        if (architecture != null)
            return true;

        ChoiceDialog<ArchitectureData> dialog = new ChoiceDialog<>(ArchitectureData.I, ArchitectureData.values());
        dialog.setTitle("Select Architecture");
        dialog.setHeaderText("Please choose an execution architecture:");
        dialog.setContentText("Available architectures:");
        Optional<ArchitectureData> choice = dialog.showAndWait();

        if (choice.isEmpty()) {
            UiUtils.showError("No architecture selected — execution cancelled.");
            return false;
        }

        ArchitectureData selected = choice.get();
        int cost = selected.getCreditsCost();
        int userCredits = UserSession.getUserCredits();

        if (userCredits < cost) {
            UiUtils.showError("Not enough credits for " + selected.name());
            return false;
        }

        architecture = selected;
        UiUtils.showAlert("Architecture '" + selected.name() + "' selected.\nCost: " + cost + " credits.");
        return true;
    }

}
