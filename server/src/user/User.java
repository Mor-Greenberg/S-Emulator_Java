package user;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class User {
    private final String username;
    private int credits;
    private int usedCredits = 0;
    private int mainProgramsUploaded = 0;
    private int contributedFunctions = 0;
    private int executionCount = 0;

    public User(String username) {
        this.username = username;
        this.credits = 0;
    }

    public static UserManager getManager() {
        return UserManager.getInstance();
    }

    public String getUsername() { return username; }

    public int getCredits() { return credits; }

    public int getUsedCredits() { return usedCredits; }

    public int getMainProgramsUploaded() { return mainProgramsUploaded; }

    public int getContributedFunctions() { return contributedFunctions; }

    public int getExecutionCount() { return executionCount; }

    public void addCredits(int amount) {
        this.credits += amount;
    }

    public void deductCredits(int amount) {
        this.credits -= amount;
        this.usedCredits += amount;
    }

    public void incrementMainProgramsUploaded() {
        mainProgramsUploaded++;
    }

    public void incrementContributedFunctions() {
        contributedFunctions++;
    }

    public void incrementExecutionCount() {
        executionCount++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return Objects.equals(username, other.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return username;
    }
    private final Map<String, Integer> programRunCounts = new HashMap<>();
    private final Map<String, Integer> programUsedCredits = new HashMap<>();

    public void trackExecution(String programName, int creditsUsed) {
        programRunCounts.merge(programName, 1, Integer::sum);
        programUsedCredits.merge(programName, creditsUsed, Integer::sum);
        executionCount++;
        deductCredits(creditsUsed); // כבר מעדכן usedCredits כולל
    }

    public int getRunCountForProgram(String programName) {
        return programRunCounts.getOrDefault(programName, 0);
    }

    public double getAverageCreditsForProgram(String programName) {
        int runs = getRunCountForProgram(programName);
        if (runs == 0) return 0;
        return (double) programUsedCredits.getOrDefault(programName, 0) / runs;
    }

}
