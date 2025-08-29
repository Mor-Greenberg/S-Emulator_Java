package logic.history;

import logic.Variable.Variable;
import java.util.HashMap;
import java.util.Map;

public class RunHistoryEntry {
    private final int runNumber;
    private final int expansionDegree; // ✅ חדש
    private final Map<Variable, Long> inputs;
    private final long resultY;
    private final int totalCycles;

    public RunHistoryEntry(int runNumber, int expansionDegree, Map<Variable, Long> inputs, long resultY, int totalCycles) {
        this.runNumber = runNumber;
        this.expansionDegree = expansionDegree; // ✅ חדש
        this.inputs = new HashMap<>(inputs);    // הגנה על immutability
        this.resultY = resultY;
        this.totalCycles = totalCycles;
    }

    public int getRunNumber() {
        return runNumber;
    }

    public int getExpansionDegree() {
        return expansionDegree;
    }

    public Map<Variable, Long> getInputs() {
        return inputs;
    }

    public long getResultY() {
        return resultY;
    }

    public int getTotalCycles() {
        return totalCycles;
    }
}
