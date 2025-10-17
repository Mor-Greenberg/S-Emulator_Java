package user;
public class UserStats {
    private final String name;
    private final int mainPrograms;
    private final int contributedFunctions;
    private final int currentCredits;
    private final int usedCredits;
    private final int executionCount;

    public UserStats(String name, int mainPrograms, int contributedFunctions, int currentCredits, int usedCredits, int executionCount) {
        this.name = name;
        this.mainPrograms = mainPrograms;
        this.contributedFunctions = contributedFunctions;
        this.currentCredits = currentCredits;
        this.usedCredits = usedCredits;
        this.executionCount = executionCount;
    }

    public String getName() { return name; }
    public int getMainPrograms() { return mainPrograms; }
    public int getContributedFunctions() { return contributedFunctions; }
    public int getCurrentCredits() { return currentCredits; }
    public int getUsedCredits() { return usedCredits; }
    public int getExecutionCount() { return executionCount; }
}
