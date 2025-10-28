package user;

import dto.UserRunEntryDTO;

import java.util.*;

public class UserManager {
    private static final UserManager instance = new UserManager();
    public static UserManager getInstance() { return instance; }
    private final Map<String, User> users = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, List<UserRunEntryDTO>> userHistories = new java.util.concurrent.ConcurrentHashMap<>();


    public UserManager() {}

    public void addUser(String username) {
        if (username == null || username.isBlank()) return;
        users.putIfAbsent(username, new User(username));
        userHistories.putIfAbsent(username, new ArrayList<>());
    }

    public User getUser(String username) {
        if (username == null || username.isBlank()) return null;
        return users.computeIfAbsent(username, User::new);
    }
    public void addRun(String username, UserRunEntryDTO entry) {
        userHistories
                .computeIfAbsent(username, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(entry);

        User user = users.computeIfAbsent(username, User::new);
        user.incrementExecutionCount();

        int usedCredits = entry.getCycles() + entry.getArchitecture().getCreditsCost();
        user.tryDeductCredits(usedCredits);
    }



    public List<UserRunEntryDTO> getUserHistory(String username) {
        return userHistories.getOrDefault(username, Collections.emptyList());
    }

    public void incrementExecutionCount(String username) {
        User user = getUser(username);
        if (user != null) {
            user.incrementExecutionCount();
        }
    }

    public void addCredits(String username, int amount) {
        getUser(username).addCredits(amount);
    }

    public boolean deductCredits(String username, int amount) {
        return getUser(username).tryDeductCredits(amount);
    }

    public int getCredits(String username) {
        return getUser(username).getCredits();
    }

    public void incrementMainProgramsUploaded(String username) {
        getUser(username).incrementMainProgramsUploaded();
    }

    public void incrementContributedFunctions(String username) {
        getUser(username).incrementContributedFunctions();
    }

    public boolean trackExecution(String username, String programName, int creditsUsed) {
        User user = getUser(username);
        if (user == null) return false;
        return user.trackExecutionIfEnough(programName, creditsUsed);
    }



    public Map<String, User> getAllUsers() { return users; }

    public boolean userExists(String username) { return users.containsKey(username); }
    public void recordExecution(String username, String programName, int creditsUsed) {
        User u = getUser(username);
        if (u != null) {
            u.recordExecution(programName, creditsUsed);
        }
    }

}
