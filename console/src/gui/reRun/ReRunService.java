package gui.reRun;

import logic.execution.ExecutionRunner;
import logic.history.RunHistoryEntry;
import logic.program.Program;

import static ui.guiUtils.UiUtils.showError;


public class ReRunService {

    public static void prepareReRun(Program program) {
        if (program == null) {
            showError("No program loaded.");
            return;
        }

        RunHistoryEntry last = ExecutionRunner.getHistory().isEmpty() ? null :
                ExecutionRunner.getHistory().get(ExecutionRunner.getHistory().size() - 1);

        if (last != null) {
            ExecutionRunner.setPrefilledDegree(last.getLastDegree());
            ExecutionRunner.setPrefilledInputs(last.getInputsMap());
        } else {
            showError("No previous run found for Re-Run.");
        }
    }
}
