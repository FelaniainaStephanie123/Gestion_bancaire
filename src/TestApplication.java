import dao.*;
import modele.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Classe de test pour démontrer les fonctionnalités du projet BankSys
 */
public class TestApplication {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   TEST DU PROJET BANKSYS");
        System.out.println("========================================\n");

        // Test 1: CRUD Clients
        testClientsOperations();

        // Test 2: Gestion des Prêts
        testPretOperations();

        // Test 3: Gestion des Virements
        testVirementOperations();

        System.out.println("\n========================================");
        System.out.println("   TESTS TERMINÉS");
        System.out.println("========================================");
    }

    /**
     * Test des opérations CRUD sur les Clients
     */
    public static void testClientsOperations() {
        System.out.println("\n--- TEST 1: GESTION DES CLIENTS ---\n");

        ClientDAO clientDAO = new ClientDAO();

        // Créer des clients de test
        Client client1 = new Client(
            "ACC001",
            "DUPONT",
            "Jean",
            "0612345678",
            "jean.dupont@email.com",
            new BigDecimal("50000.00")
        );

        Client client2 = new Client(
            "ACC002",
            "MARTIN",
            "Marie",
            "0687654321",
            "marie.martin@email.com",
            new BigDecimal("75000.00")
        );

        Client client3 = new Client(
            "ACC003",
            "BERNARD",
            "Pierre",
            "0611111111",
            "pierre.bernard@email.com",
            new BigDecimal("100000.00")
        );

        // 1. Ajouter des clients
        System.out.println("✓ Ajout de 3 clients...");
        clientDAO.ajouter(client1);
        clientDAO.ajouter(client2);
        clientDAO.ajouter(client3);
        System.out.println("✓ Clients ajoutés avec succès!\n");

        // 2. Lister tous les clients
        System.out.println("✓ Affichage de tous les clients:");
        List<Client> tousClients = clientDAO.listerTous();
        afficherClients(tousClients);

        // 3. Rechercher un client par ID
        System.out.println("\n✓ Recherche du client par ID 'ACC001':");
        Client clientTrouve = clientDAO.rechercherParId("ACC001");
        if (clientTrouve != null) {
            System.out.println("  → Trouvé: " + clientTrouve.getNom() + " " + clientTrouve.getPrenoms());
        }

        // 4. Rechercher avec critère (LIKE)
        System.out.println("\n✓ Recherche des clients avec 'MARTIN' dans le nom:");
        List<Client> clientsRecherches = clientDAO.rechercher("MARTIN");
        afficherClients(clientsRecherches);

        // 5. Modifier un client
        System.out.println("\n✓ Modification du client ACC002:");
        client2.setPrenoms("Marie-Claire");
        client2.setTel("0699999999");
        clientDAO.modifier(client2);
        System.out.println("  → Client modifié!");

        // 6. Afficher à nouveau pour vérifier
        System.out.println("\n✓ Vérification après modification:");
        Client clientModifie = clientDAO.rechercherParId("ACC002");
        if (clientModifie != null) {
            System.out.println("  → Nom: " + clientModifie.getNom() + " " + clientModifie.getPrenoms());
            System.out.println("  → Téléphone: " + clientModifie.getTel());
        }

        // 7. Supprimer un client
        System.out.println("\n✓ Suppression du client ACC003...");
        clientDAO.supprimer("ACC003");
        System.out.println("  → Client supprimé!");

        // 8. Afficher la liste finale
        System.out.println("\n✓ Liste finale des clients:");
        tousClients = clientDAO.listerTous();
        afficherClients(tousClients);
    }

    /**
     * Test des opérations sur les Prêts
     */
    public static void testPretOperations() {
        System.out.println("\n--- TEST 2: GESTION DES PRÊTS ---\n");

        PretDAO pretDAO = new PretDAO();
        ClientDAO clientDAO = new ClientDAO();

        // Vérifier qu'il y a au moins un client
        Client client = clientDAO.rechercherParId("ACC001");
        
        if (client != null) {
            // Créer un prêt
            Pret pret = new Pret(
                "ACC001",           // num_compte du client
                new BigDecimal("20000.00"),  // montant
                12,                  // durée en mois
                7.5,                 // taux d'intérêt
                "En cours"           // statut
            );

            System.out.println("✓ Création d'un prêt pour le client ACC001:");
            System.out.println("  → Montant: 20000.00 MGA");
            System.out.println("  → Durée: 12 mois");
            System.out.println("  → Taux: 7.5%");

            if (pretDAO.ajouter(pret)) {
                System.out.println("  → Prêt ajouté avec succès!");

                // Lister tous les prêts
                System.out.println("\n✓ Liste de tous les prêts:");
                List<Pret> prets = pretDAO.listerTous();
                afficherPrets(prets);
            } else {
                System.out.println("  ⚠ Erreur lors de l'ajout du prêt");
            }
        } else {
            System.out.println("⚠ Aucun client trouvé pour le test");
        }
    }

    /**
     * Test des opérations sur les Virements
     */
    public static void testVirementOperations() {
        System.out.println("\n--- TEST 3: GESTION DES VIREMENTS ---\n");

        VirementDAO virementDAO = new VirementDAO();
        ClientDAO clientDAO = new ClientDAO();

        // Vérifier qu'il y a au moins un client
        Client client = clientDAO.rechercherParId("ACC001");
        
        if (client != null) {
            // Créer un virement
            Virement virement = new Virement(
                "ACC001",                    // num_compte origine
                "ACC002",                    // num_compte destination
                new BigDecimal("5000.00"),   // montant
                "Paiement facture"           // motif
            );

            System.out.println("✓ Création d'un virement:");
            System.out.println("  → De: ACC001");
            System.out.println("  → Vers: ACC002");
            System.out.println("  → Montant: 5000.00 MGA");
            System.out.println("  → Motif: Paiement facture");

            if (virementDAO.ajouter(virement)) {
                System.out.println("  → Virement ajouté avec succès!");

                // Lister tous les virements
                System.out.println("\n✓ Liste de tous les virements:");
                List<Virement> virements = virementDAO.listerTous();
                afficherVirements(virements);
            } else {
                System.out.println("  ⚠ Erreur lors de l'ajout du virement");
            }
        } else {
            System.out.println("⚠ Aucun client trouvé pour le test");
        }
    }

    // ============ MÉTHODES UTILITAIRES ============

    private static void afficherClients(List<Client> clients) {
        if (clients.isEmpty()) {
            System.out.println("  Aucun client trouvé");
            return;
        }
        System.out.println("  ┌─────────────────────────────────────────────────────────────────┐");
        for (Client c : clients) {
            System.out.printf("  │ ID: %-10s | %s %s | Tel: %s%n", 
                c.getNumCompte(), c.getNom(), c.getPrenoms(), c.getTel());
            System.out.printf("  │   Email: %-20s | Solde: %.2f MGA%n", 
                c.getMail(), c.getSoldeActuel());
        }
        System.out.println("  └─────────────────────────────────────────────────────────────────┘");
    }

    private static void afficherPrets(List<Pret> prets) {
        if (prets.isEmpty()) {
            System.out.println("  Aucun prêt trouvé");
            return;
        }
        System.out.println("  ┌─────────────────────────────────────────────────────────────────┐");
        for (Pret p : prets) {
            System.out.printf("  │ Client: %-15s | Montant: %.2f MGA%n", 
                p.getNumCompte(), p.getMontant());
            System.out.printf("  │   Durée: %d mois | Taux: %.2f%% | Statut: %s%n", 
                p.getDuree(), p.getTauxInteret(), p.getStatut());
        }
        System.out.println("  └─────────────────────────────────────────────────────────────────┘");
    }

    private static void afficherVirements(List<Virement> virements) {
        if (virements.isEmpty()) {
            System.out.println("  Aucun virement trouvé");
            return;
        }
        System.out.println("  ┌─────────────────────────────────────────────────────────────────┐");
        for (Virement v : virements) {
            System.out.printf("  │ De: %-10s → Vers: %-10s | Montant: %.2f MGA%n", 
                v.getNumCompteOrigine(), v.getNumCompteDestination(), v.getMontant());
            System.out.printf("  │   Motif: %s%n", v.getMotif());
        }
        System.out.println("  └─────────────────────────────────────────────────────────────────┘");
    }
}
