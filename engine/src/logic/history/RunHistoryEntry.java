package logic.history;

import logic.Variable.Variable;
import java.util.HashMap;
import java.util.Map;

public class RunHistoryEntry {
    private final int runNumber;
    private final int expansionDegree;
    private final Map<Variable, Long> inputs;
    private final long resultY;
    private final int totalCycles;
    public final boolean isDebug;


    public RunHistoryEntry(int runNumber, int expansionDegree, Map<Variable, Long> inputs, long resultY, int totalCycles, boolean isDebug) {
        this.runNumber = runNumber;
        this.expansionDegree = expansionDegree;
        this.inputs = new HashMap<>(inputs);
        this.resultY = resultY;
        this.totalCycles = totalCycles;
        this.isDebug = isDebug;

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

    public boolean isDebug() {
        return isDebug;
    }
    public int getLastDegree(){
        return this.expansionDegree;
    }

    public Map<Variable, Long> getInputsMap() {
        return inputs;
    }
}
