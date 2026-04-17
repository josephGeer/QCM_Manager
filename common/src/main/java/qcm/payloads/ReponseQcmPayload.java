package qcm.payloads;

public class ReponseQcmPayload {
    private int qcmId;
    private int questionIndex;
    private int optionIndex;

    public ReponseQcmPayload(int qcmId, int questionIndex, int optionIndex) {
        this.qcmId = qcmId;
        this.questionIndex = questionIndex;
        this.optionIndex = optionIndex;
    }

    public int getQcmId() { return qcmId; }
    public int getQuestionIndex() { return questionIndex; }
    public int getOptionIndex() { return optionIndex; }
}