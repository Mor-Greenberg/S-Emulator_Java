package user;

import java.util.Objects;

public class User {
    private final String username;
    private int credits;

    public User(String username) {
        this.username = username;
        this.credits = 0;
    }

    public String getUsername() { return username; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
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

    public void addCredits(int amount) {
        this.credits += amount;
    }

    public Object getCredits() {
        return this.credits;
    }

    public void deductCredits(int amount) {
        this.credits -= amount;
    }
}
