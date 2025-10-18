package dto;


import user.User;

public class UserStatsDTO {
    private final String name;
    private final int mainPrograms;
    private final int contributedFunctions;
    private final int currentCredits;
    private final int usedCredits;
    private final int executionCount;

    public UserStatsDTO(User user) {
        this.name = user.getUsername();
        this.mainPrograms = user.getMainProgramsUploaded();
        this.contributedFunctions = user.getContributedFunctions();
        this.currentCredits = user.getCredits();
        this.usedCredits = user.getUsedCredits();
        this.executionCount = user.getExecutionCount();
    }

    public String getName() { return name; }
    public int getMainPrograms() { return mainPrograms; }
    public int getContributedFunctions() { return contributedFunctions; }
    public int getCurrentCredits() { return currentCredits; }
    public int getUsedCredits() { return usedCredits; }
    public int getExecutionCount() { return executionCount; }
}

