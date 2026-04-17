package qcm.payloads;

public class DemandeQcmPayload {
    private int qcmId;

    public DemandeQcmPayload(int qcmId){
        this.qcmId = qcmId;
    }

    public int getQcmId() { return qcmId; }
}