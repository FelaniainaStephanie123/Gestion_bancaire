import java.util.Scanner;

public class Login {
    public static boolean verifierConnexion() {
        try (Scanner scanner = new Scanner(System.in)) {
        
        System.out.println("==================================");
        System.out.println("   AUTHENTIFICATION - BANQUE      ");
        System.out.println("==================================");
        
        System.out.print("Entrez votre identifiant : ");
        String utilisateur = scanner.nextLine();
        
        System.out.print("Entrez votre mot de passe : ");
        String motDePasse = scanner.nextLine();
        
        if (utilisateur.equals("admin") && motDePasse.equals("admin123")) {
            System.out.println("\n[Succès] Connexion établie ! Bienvenue.\n");
            return true;
        } else {
            System.out.println("\n[Erreur] Identifiant ou mot de passe incorrect.\n");
            return false;
        }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}