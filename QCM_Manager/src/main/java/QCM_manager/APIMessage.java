package QCM_manager;

import qcm.payloads.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import com.google.gson.JsonSyntaxException;
import qcm.Request;


public class APIMessage {
    private final Gson gson = new Gson();
    private final UserService userService;
    private QcmService qcmService;


    public APIMessage(UserService userService, QcmService qcmService) {
        this.userService = userService;
        this.qcmService = qcmService;
    }

    public String traiteMessage(String jsonString) {
        try {
            Request req = gson.fromJson(jsonString, Request.class);

            if (req == null || req.getAction() == null) {
                return createErrorResponse("Action manquante");
            }

            String action = req.getAction();

            //Coeur de l'API
            switch (action) {
                case "REGISTER":
                    return handleRegister(req);
                case "LOGIN":
                    return handleLogin(req);
                case "DEMANDE_LIST_QCM":
                    return handleDemandeListQCM(req);
                case "DEMANDE_EXEC_QCM":
                    return handleDemandeExecutionQCM(req);
                case "DEMANDE_PROFIL":
                    return handleProfil(req);
                case "REPONSE_QCM":
                    return handleReponseQcm(req);
                case "DELETE_USER":
                    return handleDeleteUser(req);
                default:
                    return createErrorResponse("Action inconnue : " + action);
            }

        } catch (JsonSyntaxException e) {
            return createErrorResponse("JSON malformé");
        } catch (Exception e) {
            return createErrorResponse("Erreur serveur : " + e.getMessage());
        }
    }

    private String handleRegister(Request req) {
        try {
            RegisterPayload data = gson.fromJson(gson.toJsonTree(req.getPayload()),RegisterPayload.class);

            if (data.getUsername() == null || data.getUsername().isEmpty()) {
                return createErrorResponse("Username vide");
            }
            if (data.getPassword() == null || data.getPassword().isEmpty()) {
                return createErrorResponse("Password vide");
            }

            Boolean success = userService.registerUser(data.getUsername(), data.getEmail(),req.getClientId(),data.getPassword());

            if (success) {
                JsonObject response = new JsonObject();
                response.addProperty("status", "OK");
                response.addProperty("action", "REGISTER_SUCCESS");
                response.addProperty("message", "Inscription validée");
                response.addProperty("username", data.getUsername());
                return response.toString();
            } else {
                return createErrorResponse("Utilisateur déjà existant");
            }

        } catch (Exception e) {
            return createErrorResponse("Erreur lors de l'inscription : " + e.getMessage());
        }
    }

    private String handleLogin(Request req) {
        try {
            LoginPayload data = gson.fromJson(gson.toJsonTree(req.getPayload()),LoginPayload.class);
            if (data.getUsername() == null || data.getUsername().isEmpty()) {
                return createErrorResponse("Username manquant");
            }
            if (data.getPassword() == null || data.getPassword().isEmpty()) {
                return createErrorResponse("Password manquant");
            }

            String sessionId = userService.loginUser(data.getUsername(), data.getPassword());
            if (sessionId != null) {
                JsonObject response = new JsonObject();
                response.addProperty("status", "OK");
                response.addProperty("action", "LOGIN_SUCCESS");
                response.addProperty("message", "Connexion réussie");
                response.addProperty("sessionId", sessionId);
                return response.toString();
            } else {
                return createErrorResponse("Identifiants invalides");
            }

        } catch (Exception e) {
            return createErrorResponse("Erreur lors de la connexion : " + e.getMessage());
        }
    }

    private String handleDemandeListQCM(Request req) {
        if (!userService.isSessionActive(req.getSessionId())) {
             return createErrorResponse("Session invalide ou expirée");
        }

        JsonObject response = new JsonObject();
        response.addProperty("status", "OK");
        response.addProperty("action", "QCM_LIST");

        JsonArray listeQcmArray = new JsonArray();

        List<Qcm> tousLesQcms = qcmService.getAllQcms();

        if (tousLesQcms != null) {
            for (Qcm q : tousLesQcms) {
                JsonObject qcmJson = new JsonObject();
                qcmJson.addProperty("qcmId", q.qcmId);
                qcmJson.addProperty("titre", q.titre);
                listeQcmArray.add(qcmJson);
            }
        }

        response.add("payload", listeQcmArray);

        return response.toString();
    }

    private String handleDemandeExecutionQCM(Request req) {
        if (!userService.isSessionActive(req.getSessionId())) return createErrorResponse("Session invalide");
        userService.initSessionScore(req.getSessionId());

        try {
            DemandeQcmPayload payload = gson.fromJson(gson.toJsonTree(req.getPayload()), DemandeQcmPayload.class);

            Qcm leQcm = qcmService.getQcmById(payload.getQcmId());

            if (leQcm == null) return createErrorResponse("QCM introuvable");
            Question premiereQuestion = leQcm.getQuestionByNumero(1);
            if (premiereQuestion == null) return createErrorResponse("QCM vide (pas de questions)");
            return createQuestion(premiereQuestion, leQcm.qcmId);

        } catch (Exception e) {
            return createErrorResponse("Erreur chargement QCM");
        }
    }

    private String handleReponseQcm(Request req) {
        String sessionId = req.getSessionId();
        if (!userService.isSessionActive(sessionId)) return createErrorResponse("Session invalide");

        try {
            ReponseQcmPayload clientReponse = gson.fromJson(gson.toJsonTree(req.getPayload()), ReponseQcmPayload.class);

            Qcm leQcm = qcmService.getQcmById(clientReponse.getQcmId());
            if (leQcm == null) return createErrorResponse("QCM introuvable");

            Question questionPosee = leQcm.getQuestionByNumero(clientReponse.getQuestionIndex());
            if (questionPosee == null) return createErrorResponse("Question introuvable");


            if (clientReponse.getOptionIndex() == questionPosee.reponseCorrecte) {
                userService.incrementScore(sessionId);
            }

            //vérification si il y a une prochaine question
            int nextIndex = clientReponse.getQuestionIndex() + 1;
            Question nextQuestion = leQcm.getQuestionByNumero(nextIndex);

            if (nextQuestion != null) {
                return createQuestion(nextQuestion, leQcm.qcmId);
            } else {
                return finQcm(sessionId, leQcm.qcmId, leQcm.questions.size());
            }

        } catch (Exception e) {
            return createErrorResponse("Erreur réponse");
        }
    }

    private String createQuestion(Question q, int qcmId) {
        List<String> optionsText = new ArrayList<>();
        for (QcmOption opt : q.options) {
            optionsText.add(opt.text);
        }

        QuestionPayload payloadObj = new QuestionPayload(qcmId,q.numero,q.enonce,optionsText);

        JsonObject response = new JsonObject();
        response.addProperty("status", "OK");
        response.addProperty("action", "QCM_QUESTION");

        response.add("payload", gson.toJsonTree(payloadObj));

        return response.toString();
    }

    //envoie le score lorsque le qcm est finis
    private String finQcm(String sessionId, int qcmId, int totalQuestions) {
        int finalScore = userService.getSessionScore(sessionId);
        User user = userService.getUserBySession(sessionId);

        if (user != null) {
            user.addScore(qcmId, finalScore);
            userService.saveToFile();
        }
        userService.clearSessionScore(sessionId);

        ScorePayload payloadObj = new ScorePayload(finalScore, totalQuestions);

        JsonObject response = new JsonObject();
        response.addProperty("status", "OK");
        response.addProperty("action", "SCORE");

        response.add("payload", gson.toJsonTree(payloadObj));

        return response.toString();
    }

    private String handleProfil(Request req) {
        if (!userService.isSessionActive(req.getSessionId())) {
            return createErrorResponse("Session invalide");
        }
        User user = userService.getUserBySession(req.getSessionId());

        JsonObject response = new JsonObject();
        response.addProperty("status", "OK");
        response.addProperty("action", "PROFILE");

        JsonObject profilPayload = new JsonObject();

        if (user != null) {
            profilPayload.addProperty("username", user.getUsername());
            JsonArray scoresArray = new JsonArray();

            for (Map.Entry<Integer, Integer> entry : user.getScores().entrySet()) {
                JsonObject scoreItem = new JsonObject();
                int qcmId = entry.getKey();
                scoreItem.addProperty("qcmId", qcmId);
                scoreItem.addProperty("score", entry.getValue());
                Qcm qcm = qcmService.getQcmById(qcmId);
                scoreItem.addProperty("titre", qcm.titre);

                double moyenne = userService.calculerMoyenneQcm(qcmId);
                scoreItem.addProperty("moyenne", moyenne);

                scoresArray.add(scoreItem);
            }


            profilPayload.add("historique_scores", scoresArray);

        } else {
            profilPayload.addProperty("username", "Inconnu");
            profilPayload.add("historique_scores", new com.google.gson.JsonArray());
        }

        response.add("payload", profilPayload);
        return response.toString();
    }

    private String handleDeleteUser(Request req) {
        if (!userService.isSessionActive(req.getSessionId())) {
            return createErrorResponse("Session invalide, impossible de supprimer le compte.");
        }
        boolean success = userService.deleteUser(req.getSessionId());

        JsonObject response = new JsonObject();
        if (success) {
            response.addProperty("status", "OK");
            response.addProperty("action", "DELETE_USER_SUCCESS");
            response.addProperty("message", "Compte supprimé avec succès. Au revoir !");
        } else {
            return createErrorResponse("Erreur lors de la suppression du compte.");
        }
        return response.toString();
    }

    private String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "ERROR");
        response.addProperty("message", message);
        return response.toString();
    }
}