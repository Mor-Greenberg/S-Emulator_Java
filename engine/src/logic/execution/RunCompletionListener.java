package logic.execution;

import dto.UserRunEntryDTO;

public interface RunCompletionListener {
    void onRunCompleted(UserRunEntryDTO runEntry);
}
