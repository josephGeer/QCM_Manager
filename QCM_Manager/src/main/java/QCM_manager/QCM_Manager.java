package QCM_manager;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.nio.charset.StandardCharsets;
import qcm.Request;
import qcm.payloads.*;

public class QCM_Manager {
    private static final String BROKER_URL = "tcp://10.11.34.22:1883";
    private static final String SERVER_TOPIC = "server/qcm";
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        try {
            UserService userService = new UserService(true);
            QcmService qcmService = new QcmService();

            APIMessage api = new APIMessage(userService, qcmService);

            MqttClient serverClient = new MqttClient(BROKER_URL, "QCM_Manager_Server");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            serverClient.connect(options);

            serverClient.subscribe(SERVER_TOPIC, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("Requête reçue : " + payload);

                Request reqObj = null;
                try {
                    reqObj = gson.fromJson(payload, Request.class);
                } catch (Exception e) {
                    System.err.println("JSON invalide reçu, impossible de répondre");
                    return;
                }

                String responseJson = api.traiteMessage(payload);

                if (reqObj != null && reqObj.getClientId() != null) {
                    String responseTopic = reqObj.getClientId() + "/res";
                    MqttMessage mqttResponse = new MqttMessage(responseJson.getBytes());
                    serverClient.publish(responseTopic, mqttResponse);

                    System.out.println("Réponse envoyée vers " + responseTopic + " : " + responseJson);
                } else {
                    System.err.println("Pas de ClientId, impossible de répondre.");
                }
            });

            System.out.println("Serveur QCM prêt sur " + SERVER_TOPIC);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}