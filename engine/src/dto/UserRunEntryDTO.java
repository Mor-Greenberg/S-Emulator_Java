package dto;

import logic.architecture.ArchitectureData;

public class UserRunEntryDTO {
    private String username;
    private int runId;
    private String runType;      // "Program" or "Function"
    private String programName;
    private ArchitectureData architecture;
    private int degree;
    private long yValue;
    private int cycles;

    public UserRunEntryDTO(String username, int runId, String runType, String programName,
                           ArchitectureData architecture, int degree, long yValue, int cycles) {
        this.username = username;
        this.runId = runId;
        this.runType = runType;
        this.programName = programName;
        this.architecture = architecture;
        this.degree = degree;
        this.yValue = yValue;
        this.cycles = cycles;
    }

    public UserRunEntryDTO() {} // Empty constructor for Gson

    // --- Getters ---
    public String getUsername() { return username; }
    public int getRunId() { return runId; }
    public String getRunType() { return runType; }
    public String getProgramName() { return programName; }
    public ArchitectureData getArchitecture() { return architecture; }
    public int getDegree() { return degree; }
    public long getYValue() { return yValue; }
    public int getCycles() { return cycles; }

    @Override
    public String toString() {
        return "UserRunEntryDTO{" +
                "username='" + username + '\'' +
                ", runId=" + runId +
                ", runType='" + runType + '\'' +
                ", programName='" + programName + '\'' +
                ", architecture=" + architecture +
                ", degree=" + degree +
                ", yValue=" + yValue +
                ", cycles=" + cycles +
                '}';
    }
}
