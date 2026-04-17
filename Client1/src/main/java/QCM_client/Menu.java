package QCM_client;

import java.util.Scanner;

public class Menu {
    private final Client client;
    private final Scanner scanner = new Scanner(System.in);

    public Menu(Client client) {
        this.client = client;
    }

    public void lancer() {
        boolean continuer = true;

        while (continuer) {
            afficherMenu();
            String choix = scanner.nextLine();

            switch (choix) {
                case "r":
                    demanderInfosEtAction("REGISTER");
                    break;
                case "l":
                    demanderInfosEtAction("LOGIN");
                    break;
                case "1":
                    client.demanderProfil();
                    break;
                case "2":
                    client.demanderListeQCM();
                    break;
                case "3":
                    System.out.println("Entrez l'ID du QCM :");
                    String idStr = scanner.nextLine();
                    try {
                        int id = Integer.parseInt(idStr);
                        lancerCycleQCM(id);
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalide");
                    }
                    break;
                case "4":
                    System.out.println("supression du compte ... ");
                    client.supprimerUser();
                    break;
                case "0":
                    continuer = false;
                    System.out.println("Fermeture de l'application...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Choix invalide.");
            }
        }
    }

    private void afficherMenu() {
        System.out.println("\n--- BIENVENUE SUR L'APPLICATION QCM ---");
        System.out.println("[r] S'inscrire (Register)");
        System.out.println("[l] Se connecter (Login)");
        System.out.println("-------------------------");
        System.out.println("1. Afficher mon profil");
        System.out.println("2. Liste des QCM");
        System.out.println("3. Demander un QCM");
        System.out.println("4. Supprimer son compte");
        System.out.println("0. Quitter");
        System.out.print("\nVotre choix : ");
    }

    private void demanderInfosEtAction(String type) {

        if (type.equals("REGISTER")) {
            System.out.print("Nom d'utilisateur : ");
            String user = scanner.nextLine();
            System.out.print("email : ");
            String email = scanner.nextLine();
            System.out.print("Mot de passe : ");
            String pass = scanner.nextLine();
            client.requeteRegister(user, email, pass);
        } else {
            System.out.print("Nom d'utilisateur : ");
            String user = scanner.nextLine();
            System.out.print("Mot de passe : ");
            String pass = scanner.nextLine();
            client.requeteLogin(user, pass);
        }
    }

    public void lancerCycleQCM(int qcmId) {
        client.demanderQCM(qcmId);

        while (client.isQcmEnCours()) {

            String repStr = scanner.nextLine();
            if (repStr.isEmpty()) continue;

            try {
                int rep = Integer.parseInt(repStr);
                client.envoyerReponse(qcmId, rep);

            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un numéro valide.");
            }
        }
    }
}