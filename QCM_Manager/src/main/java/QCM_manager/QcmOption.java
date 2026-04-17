package QCM_manager;

public class QcmOption {
    int idx;
    String text;

    public QcmOption(int idx, String text) {
        this.idx = idx;
        this.text = text;
    }

    public int getIdx(){return idx;}
    public String getText(){return text;}
}