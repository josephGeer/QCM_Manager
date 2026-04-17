package qcm.payloads;

import java.util.List;

public class QuestionPayload {
    private int qcmId;
    private int questionIndex;
    private String enonce;
    private List<String> options;

    public QuestionPayload(int qcmId, int questionIndex, String enonce, List<String> options) {
        this.qcmId = qcmId;
        this.questionIndex = questionIndex;
        this.enonce = enonce;
        this.options = options;
    }
    public int getQuestionIndex() { return questionIndex; }
    public String getEnonce() { return enonce; }
}