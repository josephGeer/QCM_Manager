package QCM_manager;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String username;
    public String email;
    public String userId;
    public String password;
    public Map<Integer, Integer> scores = new HashMap<>();

    public User() {}

    public User(String username, String email, String userId, String password) {
        this.username = username;
        this.email = email;
        this.userId = userId;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail(){
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getUserId(){
        return this.userId;
    }

    public Map<Integer, Integer> getScores() {
        if (scores == null) scores = new HashMap<>();
        return scores;
    }

    public void addScore(int qcmId, int score) {
        if (scores == null) scores = new HashMap<>();
        scores.put(qcmId, score);
    }
}