package dto;

public class UserStats {
    private String name;
    private int mainPrograms;
    private int contributedFunctions;
    private int currentCredits;
    private int usedCredits;
    private int executionCount;

    public String getName() {
        return name;
    }

    public int getMainPrograms() {
        return mainPrograms;
    }

    public int getContributedFunctions() {
        return contributedFunctions;
    }

    public int getCurrentCredits() {
        return currentCredits;
    }

    public int getUsedCredits() {
        return usedCredits;
    }

    public int getExecutionCount() {
        return executionCount;
    }
}
