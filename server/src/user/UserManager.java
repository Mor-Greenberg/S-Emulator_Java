package user;

import dto.UserRunEntryDTO;

import java.util.*;

public class UserManager {
    private static final UserManager instance = new UserManager();
    public static UserManager getInstance() { return instance; }

    private final Map<String, User> users = new HashMap<>();
    private final Map<String, List<UserRunEntryDTO>> userHistories = new HashMap<>();


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
        userHistories.computeIfAbsent(username, k -> new ArrayList<>()).add(entry);
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

    public void deductCredits(String username, int amount) {
        getUser(username).deductCredits(amount);
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

    public void trackExecution(String username, String programName, int creditsUsed) {
        getUser(username).trackExecution(programName, creditsUsed);
    }

    public Map<String, User> getAllUsers() { return users; }

    public boolean userExists(String username) { return users.containsKey(username); }
}
