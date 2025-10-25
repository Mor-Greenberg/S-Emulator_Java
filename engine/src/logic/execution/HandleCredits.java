package logic.execution;

import javafx.application.Platform;
import logic.architecture.ArchitectureData;
import logic.program.Program;
import session.UserSession;
import ui.executionBoard.ExecutionBoardController;
import utils.UiUtils;

/**
 * Centralized credit management for program execution.
 * Handles:
 *  - Initial cost (architecture charge)
 *  - Per-cycle deduction
 *  - Out-of-credit handling and UI updates
 */
public class HandleCredits {

    private static int remainingCredits;

    public static boolean prepareExecution(Program program, String architectureName) {
        System.out.println("Architecture name received: " + architectureName);

        ExecutionBoardController ctrl = ExecutionBoardController.getInstance();
        if (ctrl == null) return false;

        int userCredits = ctrl.getUserCredits();
        ArchitectureData archType = ArchitectureData.valueOf(architectureName);
        int architectureCost = archType.getCreditsCost();

        // Estimate program cost (average)
        int estimatedCycles = program.calculateCycles();
        int totalRequired = estimatedCycles + architectureCost;

        if (userCredits < totalRequired) {
            UiUtils.showError(
                    "Not enough credits to run this program.\n" +
                            "Required: " + totalRequired + ", Available: " + userCredits
            );
            return false;
        }

        // Deduct architecture cost upfront
        remainingCredits = userCredits - architectureCost;
        Platform.runLater(() -> ctrl.setUserCredits(remainingCredits));

        System.out.println("Architecture cost charged: " + architectureCost +
                " | Remaining credits: " + remainingCredits);

        return true;
    }


    public static boolean consumeCycles(String programName, int cyclesUsed) {
        ExecutionBoardController ctrl = ExecutionBoardController.getInstance();
        if (ctrl == null) return false;

        remainingCredits -= cyclesUsed;
        Platform.runLater(() -> ctrl.setUserCredits(remainingCredits));

        // Out of credits → stop execution and return to dashboard
        if (remainingCredits <= 0) {
            Platform.runLater(() -> {
                UiUtils.showError("You have run out of credits. Execution stopped.");
                ctrl.onBackToDashboard();
            });
            return false;
        }

        return true;
    }

    public static void finalizeRun(String programName, int totalCyclesUsed) {
        ExecutionBoardController ctrl = ExecutionBoardController.getInstance();
        if (ctrl == null) return;

        Platform.runLater(() -> {
            ctrl.setUserCredits(remainingCredits);
            System.out.println("Run of " + programName + " completed. " +
                    "Total cycles: " + totalCyclesUsed +
                    " | Remaining credits: " + remainingCredits);
        });

        UserSession.setUserCredits(remainingCredits);
    }

    public static int getRemainingCredits() {
        return remainingCredits;
    }
}
