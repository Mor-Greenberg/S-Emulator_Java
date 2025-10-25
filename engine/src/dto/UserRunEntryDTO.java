package dto;

import logic.architecture.ArchitectureData;

public class UserRunEntryDTO {
    private int runId;
    private String runType;      // "Program" or "Function"
    private String programName;
    private ArchitectureData architecture;
    private int degree;
    private long yValue;
    private int cycles;

    public UserRunEntryDTO(int runId, String runType, String programName,
                           ArchitectureData architecture, int degree, long yValue, int cycles) {
        this.runId = runId;
        this.runType = runType;
        this.programName = programName;
        this.architecture = architecture;
        this.degree = degree;
        this.yValue = yValue;
        this.cycles = cycles;
    }

    public int getRunId() { return runId; }
    public String getRunType() { return runType; }
    public String getProgramName() { return programName; }
    public ArchitectureData getArchitecture() { return architecture; }
    public int getDegree() { return degree; }
    public long getYValue() { return yValue; }
    public int getCycles() { return cycles; }
}
