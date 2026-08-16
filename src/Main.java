import dao.ClientDAO;
import modele.Client;
import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        // 1. Étape de l'interface de Login
        if (Login.verifierConnexion()) {
            ClientDAO clientDao = new ClientDAO();

            // 2. Test du CRUD Clients
            Client client1 = new Client("200543", "RAKOTO", "Bernard", "0341234567", "rakoto@mail.com", new BigDecimal(15000000));
            Client client2 = new Client("202908", "RANDRIA", "Barthelemy", "0339876543", "randria@mail.com", new BigDecimal(500000));
            
            clientDao.ajouter(client1);
            clientDao.ajouter(client2);

            // Afficher tous les clients
            System.out.println("\n=== TOUS LES CLIENTS ===");
            clientDao.listerTous().forEach(c -> System.out.println(c));

            // Rechercher un client avec %LIKE%
            System.out.println("\n=== Recherche du client 'RAK' ===");
            clientDao.rechercher("RAK").forEach(c -> System.out.println(c));

            // Modifier un client
            System.out.println("\n=== Modification du client ===");
            Client clientModifie = new Client("202908", "RANDRIA", "Barthelemy Modifié", "0330000000", "newmail@mail.com", new BigDecimal(500000));
            clientDao.modifier(clientModifie);

            // Afficher à nouveau pour voir la modification
            System.out.println("\n=== CLIENTS APRÈS MODIFICATION ===");
            clientDao.listerTous().forEach(c -> System.out.println(c));
        }
    }
}