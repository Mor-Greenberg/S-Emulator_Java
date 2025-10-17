package serverProgram;
public class ProgramStats {
    private String programName;
    private String uploaderName;
    private int instructionCount;
    private int maxExpansionLevel;
    private int runCount;
    private double averageCredits;

    public ProgramStats(String programName, String userName, int instructionCount, int maxLevel, int runCount, double avgCredits) {
        this.programName = programName;
        this.uploaderName = userName;
        this.instructionCount = instructionCount;
        this.maxExpansionLevel = maxLevel;
        this.runCount = runCount;
        this.averageCredits = avgCredits;
    }
    public ProgramStats() {}

    public String getProgramName() {
        return programName;
    }
    public void setProgramName(String programName) {
        this.programName = programName;
    }
    public String getUploaderName() {
        return uploaderName;
    }
    public void setUploaderName(String userName) {
        this.uploaderName = userName;
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
