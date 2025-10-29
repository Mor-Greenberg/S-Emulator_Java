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

    private final Object lock = new Object();

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
        synchronized (lock) {
            credits += amount;

        }
    }

    public boolean tryDeductCredits(int amount) {
        synchronized (lock) {
            if (credits < amount) {
                return false;
            }
            credits -= amount;
            usedCredits += amount;
            System.out.println("[SERVER] 💰 tryDeductCredits(" + amount + ") | Before=" + credits + " | After=" + (credits - amount) + " | User=" + username);

            return true;

        }
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


    public int getRunCountForProgram(String programName) {
        return programRunCounts.getOrDefault(programName, 0);
    }

    public double getAverageCreditsForProgram(String programName) {
        int runs = getRunCountForProgram(programName);
        if (runs == 0) return 0;
        return (double) programUsedCredits.getOrDefault(programName, 0) / runs;
    }
    public void recordExecution(String programName, int creditsUsed) {
        synchronized (lock) {
            programRunCounts.merge(programName, 1, Integer::sum);
            programUsedCredits.merge(programName, creditsUsed, Integer::sum);
            executionCount++;
            System.out.println("[DEBUG] recordExecution called for " + username + " creditsUsed=" + creditsUsed);
        }
    }


    public void addUsedCredits(int usedCreditsIn) {
        this.usedCredits += usedCreditsIn;
    }

}
