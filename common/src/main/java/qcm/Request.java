package qcm;
//permet d'alléger toutes les requêtes
public class Request {
    private String clientId;
    private String sessionId;
    private String action;
    private Object payload;

    public Request(String clientId,String sessionId, String action, Object payload) {
        this.clientId = clientId;
        this.sessionId = sessionId;
        this.action = action;
        this.payload = payload;
    }

    public String getClientId() { return clientId; }
    public String getSessionId() { return sessionId; }
    public String getAction() { return action; }
    public Object getPayload() { return payload; }

    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

}