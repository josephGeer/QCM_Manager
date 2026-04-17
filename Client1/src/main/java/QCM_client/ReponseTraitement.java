package QCM_client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;

public class ReponseTraitement {
    private final Gson gson = new Gson();
    private final Client client;

    public ReponseTraitement(Client client) {
        this.client = client;
    }

    public void traiter(String jsonBrut) {
        try {
            JsonObject jsonObject = gson.fromJson(jsonBrut, JsonObject.class);

            if (!jsonObject.has("action")) return;

            String action = jsonObject.get("action").getAsString();

            switch (action) {
                case "REGISTER_SUCCESS":
                    System.out.println("Register success");
                    break;

                case "LOGIN_SUCCESS":
                    if(jsonObject.has("sessionId")) {
                        client.setSessionId(jsonObject.get("sessionId").getAsString());
                    }
                    System.out.println("Opération réussie !");
                    break;

                case "QCM_LIST":
                    gererListeQcm(jsonObject);
                    break;

                case "PROFILE":
                    gererProfil(jsonObject);
                    break;

                case "QCM_QUESTION":
                    gererAffichageQuestion(jsonObject.getAsJsonObject("payload"));
                    break;

                case "SCORE":
                    afficherScore(jsonObject);
                    break;

                case "DELETE_USER_SUCCESS":
                    System.out.println(jsonObject.get("message").getAsString());
                    client.deconnexion();
                    break;

                case "ERROR":
                    System.err.println("Erreur du serveur : " + jsonObject.get("message").getAsString());
                    client.setQcmEnCours(false);
                    break;

                default:
                    System.out.println("Action ignorée ou inconnue : " + action);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du message : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void gererListeQcm(JsonObject jsonObject) {
        System.out.println("\n--- Liste des QCM disponibles ---");
        JsonArray liste = jsonObject.getAsJsonArray("payload");
        if (liste.size() == 0) {
            System.out.println("Aucun QCM disponible.");
        } else {
            for (JsonElement elem : liste) {
                JsonObject qcm = elem.getAsJsonObject();

                int id = qcm.get("qcmId").getAsInt();
                String titre = qcm.get("titre").getAsString();

                System.out.println("- QCM " + id + " : " + titre);
            }
        }
    }

    private void gererProfil(JsonObject jsonObject) {
        JsonObject profil = jsonObject.getAsJsonObject("payload");
        System.out.println("\n---------------------------------");
        System.out.println("       PROFIL UTILISATEUR        ");
        System.out.println("---------------------------------");

        String username = profil.get("username").getAsString();
        System.out.println("Nom : " + username);

        System.out.println("\n--- Historique des Scores ---");

        if (profil.has("historique_scores")) {
            com.google.gson.JsonArray scores = profil.getAsJsonArray("historique_scores");

            if (scores.size() == 0) {
                System.out.println("Aucun QCM effectué pour le moment.");
            } else {
                for (com.google.gson.JsonElement elem : scores) {
                    JsonObject s = elem.getAsJsonObject();

                    int idQcm = s.get("qcmId").getAsInt();
                    int score = s.get("score").getAsInt();
                    String titre = s.get("titre").getAsString();
                    double moyenne = s.get("moyenne").getAsDouble();

                System.out.printf("• %s : %d pts (Moyenne globale : %.2f)%n", titre, score, moyenne);                }
            }
        }
        System.out.println("---------------------------------");
    }

    private void gererAffichageQuestion(JsonObject payload) {
        int index = payload.get("questionIndex").getAsInt();
        client.setCurrentQuestionIndex(index);

        String texte = payload.get("enonce").getAsString();

        System.out.println("\n--- QUESTION N°" + index + " ---");
        System.out.println(texte);

        if (payload.has("options")) {
            com.google.gson.JsonArray options = payload.getAsJsonArray("options");
            int numeroOption = 1;

            for (JsonElement opt : options) {
                System.out.println("   [" + numeroOption + "] " + opt.getAsString());
                numeroOption++;
            }
        }
        System.out.println("\nEntrez le numéro de votre réponse :");
    }

    private void afficherScore(JsonObject jsonObject) {
        JsonObject data = jsonObject.getAsJsonObject("payload");
        int score = data.get("score").getAsInt();
        int total = data.get("totalQuestions").getAsInt();

        System.out.println("\n=================================");
        System.out.println("       RÉSULTAT DU QCM           ");
        System.out.println("       Votre score : " + score + " / " + total);
        System.out.println("=================================");

        client.setQcmEnCours(false);
    }
}