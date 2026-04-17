package QCM_manager;

import java.util.ArrayList;
import java.util.List;

public class Qcm {
    int qcmId;
    String titre;
    List<Question> questions;

    public Qcm(int qcmId, String titre) {
        this.qcmId = qcmId;
        this.titre = titre;
        this.questions = new ArrayList<>();
    }

    public void addQuestion(Question q) {
        this.questions.add(q);
    }

    public Question getQuestionByNumero(int numero) {
        if (numero > 0 && numero <= questions.size()) {
            return questions.get(numero - 1);
        }
        return null;
    }
}