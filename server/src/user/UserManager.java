package user;

import dto.UserRunEntryDTO;
import java.util.*;

public class UserManager {
    private static final UserManager instance = new UserManager();
    public static UserManager getInstance() { return instance; }

    private final Map<String, User> users = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, List<UserRunEntryDTO>> userHistories = new java.util.concurrent.ConcurrentHashMap<>();

    private UserManager() {}

    public void addUser(String username) {
        if (username == null || username.isBlank()) return;
        users.putIfAbsent(username, new User(username));
        userHistories.putIfAbsent(username, Collections.synchronizedList(new ArrayList<>()));
    }

    public User getUser(String username) {
        if (username == null || username.isBlank()) return null;
        return users.computeIfAbsent(username, User::new);
    }

    public void addRun(String username, UserRunEntryDTO entry) {
        userHistories.computeIfAbsent(username, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(entry);


    }

    public List<UserRunEntryDTO> getUserHistory(String username) {
        return userHistories.getOrDefault(username, Collections.emptyList());
    }

    // ---------------- Credit and statistics management ----------------

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

    public void recordExecution(String username, String programName, int creditsUsed) {
        User u = getUser(username);
        if (u != null) {
            boolean ok = u.tryDeductCredits(creditsUsed);
            if (!ok) {
                System.out.println(" User " + username + " has insufficient credits to deduct " + creditsUsed);
                return;
            }

            u.recordExecution(programName, creditsUsed);
            u.incrementExecutionCount();


        }
    }



    public Map<String, User> getAllUsers() {
        return users;
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }
}
