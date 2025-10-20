package dto;

public class FunctionDTO {
    private String functionName;
    private String programName;
    private String uploader;
    private int numInstructions;
    private int maxDegree;

    public FunctionDTO(String functionName, String programName, String uploader, int numInstructions, int maxDegree) {
        this.functionName = functionName;
        this.programName = programName;
        this.uploader = uploader;
        this.numInstructions = numInstructions;
        this.maxDegree = maxDegree;
    }
    public FunctionDTO() {}

    public String getFunctionName() {
        return functionName;
    }
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
    public String getProgramName() {
        return programName;
    }
    public void setProgramName(String programName) {
        this.programName = programName;
    }
    public String getUploader() {
        return uploader;
    }
    public void setUploader(String uploader) {
        this.uploader = uploader;
    }
    public int getNumInstructions() {
        return numInstructions;
    }
    public void setNumInstructions(int numInstructions) {
        this.numInstructions = numInstructions;
    }
    public int getMaxDegree() {
        return maxDegree;
    }
    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }
}
