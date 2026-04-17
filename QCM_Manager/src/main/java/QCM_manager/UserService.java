package QCM_manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class UserService {
    private final String FILE_PATH = "users.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private List<User> users;
    private final Map<String, String> activeSessions = new HashMap<>();
    private final Map<String, Integer> currentSessionScores = new HashMap<>();

    public UserService() {
        this.users = loadUsers();
    }

    public UserService(boolean loadFromFile) {
        if (loadFromFile) {
            this.users = loadUsers();
        } else {
            this.users = new ArrayList<>();
        }
    }

    public Boolean registerUser(String username, String email, String userId, String password) {

        if (findUser(username) != null) {
            return false;
        }

        String hashedPassword = hashPassword(password);
        User newUser = new User(username,email, userId, hashedPassword);

        users.add(newUser);
        return saveToFile();
    }

    public String loginUser(String username, String password) {
        User user = findUser(username);
        if (user != null && user.password.equals(hashPassword(password))) {

            String sessionId = UUID.randomUUID().toString();
            activeSessions.put(sessionId, username);

            return sessionId;
        }
        return null;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors du hachage", e);
        }
    }

    //sert à récupérer tous les utilisateurs
    private List<User> loadUsers() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();
        try (Reader reader = new FileReader(file)) {
            //retourne liste de type User grace au TypeToken
            return gson.fromJson(reader, new TypeToken<List<User>>(){}.getType());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public boolean saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(users, writer);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean deleteUser(String sessionId) {
        User userToDelete = getUserBySession(sessionId);

        if (userToDelete == null) {
            return false;
        }
        this.users.remove(userToDelete);
        this.activeSessions.remove(sessionId);
        saveToFile();

        return true;
    }

    public User findUser(String username) {
        return users.stream().filter(u -> u.username.equals(username)).findFirst().orElse(null);
    }

    public boolean isSessionActive(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    public User getUserBySession(String sessionId) {
        String username = activeSessions.get(sessionId);

        if (username == null) return null;
        return findUser(username);
    }

    public void initSessionScore(String sessionId) {
        currentSessionScores.put(sessionId, 0);
    }

    public void incrementScore(String sessionId) {
        currentSessionScores.put(sessionId, currentSessionScores.getOrDefault(sessionId, 0) + 1);
    }

    public int getSessionScore(String sessionId) {
        return currentSessionScores.getOrDefault(sessionId, 0);
    }

    public void clearSessionScore(String sessionId) {
        currentSessionScores.remove(sessionId);
    }

    public double calculerMoyenneQcm(int qcmId) {
        int totalScore = 0;
        int nombreParticipants = 0;

        for (User u : this.users) {
            if (u.getScores().containsKey(qcmId)) {
                totalScore += u.getScores().get(qcmId);
                nombreParticipants++;
            }
        }

        if (nombreParticipants == 0) return 0.0;
        return (double) totalScore / nombreParticipants;
    }
}