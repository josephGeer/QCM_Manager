package QCM_client;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import qcm.payloads.*;
import qcm.Request;

public class Client {
    private static final String BROKER_URL = "tcp://10.11.34.22:1883";
    private static final String SERVER_TOPIC = "server/qcm";

    private String sessionId = "";
    private final String clientId = "client-" + UUID.randomUUID().toString().substring(0, 8);
    private final String reponseTopic = clientId + "/res";

    private final ReponseTraitement traitement = new ReponseTraitement(this);
    private final Gson gson = new Gson();
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    private MqttClient mqttClient;

    private volatile boolean qcmEnCours = false;
    private int currentQuestionIndex = 1;

    public void setCurrentQuestionIndex(int index) { this.currentQuestionIndex = index; }
    public int getCurrentQuestionIndex() { return this.currentQuestionIndex; }

    public void attendreReponse() {
        try {
            String reponseJson = responseQueue.take();
            traitement.traiter(reponseJson);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setQcmEnCours(boolean enCours) {
        this.qcmEnCours = enCours;
    }

    public boolean isQcmEnCours() {
        return this.qcmEnCours;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
        System.out.println("[SYSTEM] Session mise à jour : " + sessionId);
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
        Menu menu = new Menu(client);
        menu.lancer();
    }

    //Communication avec le broker MQTT
    public void start() {
        try {
            mqttClient = new MqttClient(BROKER_URL, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.connect(options);

            System.out.println("Connecté au broker MQTT.");

            //Abonnement au topic de réponse
            mqttClient.subscribe(reponseTopic, (topic, message) -> {
                String reponseJson = new String(message.getPayload());
                responseQueue.offer(reponseJson);
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //Envoie des requêtes
    public void requeteRegister(String username,String email, String password) {
        RegisterPayload payload = new RegisterPayload(username, email, password);
        envoyerRequete("REGISTER", payload);
    }

    public void requeteLogin(String username, String password) {
        LoginPayload payload = new LoginPayload(username, password);
        envoyerRequete("LOGIN", payload);
    }

    public void demanderProfil() {
        envoyerRequete("DEMANDE_PROFIL", null);
    }

    public void supprimerUser() {
        envoyerRequete("DELETE_USER", null);
    }

    public void deconnexion() {
        this.sessionId = "";
        System.out.println("Déconnexion en raison de la supression du compte");
    }

    public void demanderListeQCM() {
        envoyerRequete("DEMANDE_LIST_QCM", null);
    }

    public void demanderQCM(int qcmId) {
        this.qcmEnCours = true;

        responseQueue.clear();

        DemandeQcmPayload payload = new DemandeQcmPayload(qcmId);
        envoyerRequete("DEMANDE_EXEC_QCM", payload);
    }

    public void envoyerReponse(int qcmId, int option) {
        if (!qcmEnCours) {
            return;
        }
        ReponseQcmPayload payload = new ReponseQcmPayload(qcmId, this.currentQuestionIndex, option);
        envoyerRequete("REPONSE_QCM", payload);
    }

    private void envoyerRequete(String action, Object payloadObject) {
        try {
            if (mqttClient == null || !mqttClient.isConnected()) return;

            Request request = new Request(clientId, sessionId, action, payloadObject);

            String jsonFinal = gson.toJson(request);
            mqttClient.publish("server/qcm", new MqttMessage(jsonFinal.getBytes()));
            this.attendreReponse();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}