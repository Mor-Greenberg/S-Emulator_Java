package gui.reRun;

import gui.ExecutionRunner;
import logic.history.RunHistoryEntry;
import logic.program.Program;

import static utils.Utils.showError;

public class ReRunService {

    public static void prepareReRun(Program program) {
        if (program == null) {
            showError("No program loaded.");
            return;
        }

        // לוקחים את ההרצה האחרונה מהיסטוריה
        RunHistoryEntry last = ExecutionRunner.getHistory().isEmpty() ? null :
                ExecutionRunner.getHistory().get(ExecutionRunner.getHistory().size() - 1);

        if (last != null) {
            // מזריקים דרגה וקלטים להרצה הבאה
            ExecutionRunner.setPrefilledDegree(last.getLastDegree());
            ExecutionRunner.setPrefilledInputs(last.getInputsMap());
        } else {
            showError("No previous run found for Re-Run.");
        }
    }
}
