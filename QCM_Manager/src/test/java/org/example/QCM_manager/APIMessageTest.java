package QCM_manager;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

import qcm.payloads.*;
import qcm.*;

@DisplayName("Tests de l'API Message")
public class APIMessageTest {

    private UserService userService;
    private QcmService qcmService;
    private APIMessage api;
    private Gson gson;

    @BeforeEach
    public void setUp() {
        this.userService = new UserService(false);
        this.qcmService = new QcmService();

        createSaveQCM("test_qcms.json");
        this.api = new APIMessage(userService, qcmService);
    }

    private String createJsonRequest(String action, String sessionId, Object payload) {
        Request req = new Request("test-client", sessionId, action, payload);
        return gson.toJson(req);
    }

    public void createSaveQCM(String filename) {
        Qcm monQcm = new Qcm(1, "QCM Test");
        for (int i = 1; i <= 20; i++) {
            List<QcmOption> mesOptions = new ArrayList<>();
            mesOptions.add(new QcmOption(1, "A"));
            mesOptions.add(new QcmOption(2, "B"));
            mesOptions.add(new QcmOption(3, "C"));
            Question q = new Question(i, "Q"+i, mesOptions, 1);
            monQcm.addQuestion(q);
        }
        List<Qcm> listeQcms = List.of(monQcm);

        try (FileWriter writer = new FileWriter(filename)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(listeQcms, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.qcmService = new QcmService(filename);
        this.gson = new Gson();
    }

    @Test
    @DisplayName("Test Register")
    public void testRegisterValide() {
        RegisterPayload payload = new RegisterPayload("testeur","fake@mail.fr", "pass123");

        String jsonInput = createJsonRequest("REGISTER", null, payload);
        String reponse = api.traiteMessage(jsonInput);
        assertTrue(reponse.contains("OK"), "Le statut devrait être OK");
    }

    @Test
    @DisplayName("Test Login")
    public void testLoginValide() {
        api.traiteMessage(createJsonRequest("REGISTER", null, new RegisterPayload("testeur", "fake@mail.fr", "pass")));

        LoginPayload loginPayload = new LoginPayload("testeur", "pass");
        String reponse = api.traiteMessage(createJsonRequest("LOGIN", null, loginPayload));

        assertTrue(reponse.contains("OK"), "Le statut devrait être OK");
        assertTrue(reponse.contains("sessionId"), "La réponse doit contenir sessionId");
    }

    @Test
    @DisplayName("Test ListeQCM")
    public void testListeQCM() {
        api.traiteMessage(createJsonRequest("REGISTER", null, new RegisterPayload("c1", "fake@mail.fr", "pass")));
        String reponseLogin = api.traiteMessage(createJsonRequest("LOGIN", null, new LoginPayload("c1", "pass")));
        String sessionId = JsonParser.parseString(reponseLogin).getAsJsonObject().get("sessionId").getAsString();

        String reponseQCM = api.traiteMessage(createJsonRequest("DEMANDE_LIST_QCM", sessionId, null));
        JsonObject jsonResponse = JsonParser.parseString(reponseQCM).getAsJsonObject();

        assertFalse(jsonResponse.has("erreur"));
        assertTrue(jsonResponse.has("payload"));
    }

    @Test
    @DisplayName("Test Execution QCM")
    public void testExecutionQCM() {
        api.traiteMessage(createJsonRequest("REGISTER", null, new RegisterPayload("testeur", "fake@mail.fr", "pass")));
        String reponseLogin = api.traiteMessage(createJsonRequest("LOGIN", null, new LoginPayload("testeur", "pass")));
        String sessionId = JsonParser.parseString(reponseLogin).getAsJsonObject().get("sessionId").getAsString();

        DemandeQcmPayload payload = new DemandeQcmPayload(1);
        String reponse = api.traiteMessage(createJsonRequest("DEMANDE_EXEC_QCM", sessionId, payload));

        JsonObject jsonResponse = JsonParser.parseString(reponse).getAsJsonObject();
        assertTrue(jsonResponse.getAsJsonObject("payload").has("enonce"));
    }

    @Test
    @DisplayName("Test Cycle complet QCM")
    public void testCorrectionQCMComplet() {
        api.traiteMessage(createJsonRequest("REGISTER", null, new RegisterPayload("testeur", "fake@mail.fr", "pass")));
        String reponseLogin = api.traiteMessage(createJsonRequest("LOGIN", null, new LoginPayload("testeur", "pass")));
        String sessionId = JsonParser.parseString(reponseLogin).getAsJsonObject().get("sessionId").getAsString();

        for (int i = 1; i <= 20; i++) {
            ReponseQcmPayload repPayload = new ReponseQcmPayload(1, i, 1);
            String reponseAPI = api.traiteMessage(createJsonRequest("REPONSE_QCM", sessionId, repPayload));
            JsonObject root = JsonParser.parseString(reponseAPI).getAsJsonObject();

            if (i < 20) {
                assertEquals("QCM_QUESTION", root.get("action").getAsString());
            } else {
                assertEquals("SCORE", root.get("action").getAsString());
            }
        }
    }

    @Test
    @DisplayName("Test Demande Profil")
    public void testDemandeProfil() {
        api.traiteMessage(createJsonRequest("REGISTER", null, new RegisterPayload("user1", "fake@mail.fr", "pass")));
        String resLog = api.traiteMessage(createJsonRequest("LOGIN", null, new LoginPayload("user1", "pass")));
        String sessionId = JsonParser.parseString(resLog).getAsJsonObject().get("sessionId").getAsString();

        String reponse = api.traiteMessage(createJsonRequest("DEMANDE_PROFIL", sessionId, null));
        assertTrue(reponse.contains("user1"));
    }

    @Test
    @DisplayName("Test Suppression user")
    public void testDeleteUser(){
        api.traiteMessage(createJsonRequest("REGISTER", null, new RegisterPayload("user1", "fake@mail.fr", "pass")));
        String res = api.traiteMessage(createJsonRequest("LOGIN", null, new LoginPayload("user1", "pass")));

        String sessionId = JsonParser.parseString(res).getAsJsonObject().get("sessionId").getAsString();
        String resDelete = api.traiteMessage(createJsonRequest("DELETE_USER", sessionId, null));

        assertTrue(resDelete.contains("DELETE_USER_SUCCESS"), "L'API doit retourner un succès");
    }

    @Test
    @DisplayName("Login password incorrect")
    public void testLoginMotdePasseIncorrect() {
        api.traiteMessage(createJsonRequest("REGISTER", null, new RegisterPayload("testeur", "fake@mail.fr", "password123")));

        LoginPayload badLogin = new LoginPayload("testeur", "mauvaisPassword");
        String response = api.traiteMessage(createJsonRequest("LOGIN", null, badLogin));
        assertTrue(response.contains("ERROR"));
    }

    @Test
    @DisplayName("Rejeter Action Inconnue")
    public void testActionInconnue() {
        String response = api.traiteMessage(createJsonRequest("ACTION_INEXISTANTE", null, null));
        assertTrue(response.contains("ERROR"));
    }
}