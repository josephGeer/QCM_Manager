package QCM_manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class QcmService {
    private List<Qcm> listeDesQcm;
    private String FILE_PATH;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public QcmService() {
        this.FILE_PATH = "qcms.json";
        this.listeDesQcm = loadQcms();
        if (this.listeDesQcm.isEmpty()) {
            createAndSaveTestQcm();
        }
    }

    public QcmService(String filePath) {
        this.FILE_PATH = filePath;
        this.listeDesQcm = loadQcms();
        if (this.listeDesQcm.isEmpty()) {
            createAndSaveTestQcm();
        }
    }

    public void createAndSaveTestQcm() {
        Qcm grandQcm = new Qcm(1, "Random QCM");

        for (int i = 1; i <= 20; i++) {
            List<QcmOption> options = new ArrayList<>();
            options.add(new QcmOption(1, "Réponse 1"));
            options.add(new QcmOption(2, "Réponse 2"));
            options.add(new QcmOption(3, "Réponse 3"));

            Question q = new Question(i,"Question n°" + i + " : Quelle est la bonne réponse ?",options,1);
            grandQcm.addQuestion(q);
        }
        this.listeDesQcm.add(grandQcm);
        saveToJson();
    }

    public Qcm getQcmById(int idRecherche) {
        return listeDesQcm.stream().filter(q -> q.qcmId == idRecherche).findFirst().orElse(null);
    }

    public List<Qcm> getAllQcms() {
        return listeDesQcm;
    }

    private void saveToJson() {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(listeDesQcm, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private List<Qcm> loadQcms() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();
        try (Reader reader = new FileReader(file)) {
            List<Qcm> loaded = gson.fromJson(reader, new TypeToken<List<Qcm>>(){}.getType());
            return loaded != null ? loaded : new ArrayList<>();
        } catch (IOException e) { return new ArrayList<>(); }
    }
}