package logic.architecture;

import javafx.scene.control.ChoiceDialog;
import session.UserSession;
import utils.UiUtils;

import java.util.Optional;

public class HandleArch {


    public static ArchitectureData ensureArchitectureSelected(ArchitectureData currentArchitecture,UserSession userSession) {
        if (currentArchitecture != null)
            return currentArchitecture;

        ChoiceDialog<ArchitectureData> dialog = new ChoiceDialog<>(ArchitectureData.I, ArchitectureData.values());
        dialog.setTitle("Select Architecture");
        dialog.setHeaderText("Please choose an execution architecture:");
        dialog.setContentText("Available architectures:");
        Optional<ArchitectureData> choice = dialog.showAndWait();

        if (choice.isEmpty()) {
            UiUtils.showError("No architecture selected — execution cancelled.");
            return null;
        }

        ArchitectureData selected = choice.get();
        int cost = selected.getCreditsCost();
        int userCredits = userSession.getUserCredits();

        if (userCredits < cost) {
            UiUtils.showError("Not enough credits for " + selected.name());
            return null;
        }

        UiUtils.showAlert("Architecture '" + selected.name() + "' selected.\nCost: " + cost + " credits.");
        return selected;
    }
}
