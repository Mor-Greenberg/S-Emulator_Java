package dto;

public class ProgramStatsDTO {
    private String programName;
    private String uploaderName;
    private int instructionCount;
    private int maxExpansionLevel;
    private int runCount;
    private double averageCredits;

    public ProgramStatsDTO(String programName, String uploaderName, int instructionCount,
                           int maxExpansionLevel, int runCount, double averageCredits) {
        this.programName = programName;
        this.uploaderName = uploaderName;
        this.instructionCount = instructionCount;
        this.maxExpansionLevel = maxExpansionLevel;
        this.runCount = runCount;
        this.averageCredits = averageCredits;
    }

    public String getProgramName() {
        return programName;
    }
    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getUploaderName() {
        return uploaderName;
    }
    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public int getInstructionCount() {
        return instructionCount;
    }
    public void setInstructionCount(int instructionCount) {
        this.instructionCount = instructionCount;
    }

    public int getMaxExpansionLevel() {
        return maxExpansionLevel;
    }
    public void setMaxExpansionLevel(int maxExpansionLevel) {
        this.maxExpansionLevel = maxExpansionLevel;
    }

    public int getRunCount() {
        return runCount;
    }
    public void setRunCount(int runCount) {
        this.runCount = runCount;
    }

    public double getAverageCredits() {
        return averageCredits;
    }
    public void setAverageCredits(double averageCredits) {
        this.averageCredits = averageCredits;
    }
}
