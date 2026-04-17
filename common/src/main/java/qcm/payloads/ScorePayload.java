package qcm.payloads;

public class ScorePayload {
    private int score;
    private int totalQuestions;

    public ScorePayload(int score, int totalQuestions) {
        this.score = score;
        this.totalQuestions = totalQuestions;
    }

    public int getScore() { return score; }
    public int getTotalQuestions() { return totalQuestions; }
}