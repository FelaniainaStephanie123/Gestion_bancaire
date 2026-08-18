package service;

import dao.ClientDAO;
import modele.Client;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ClientService {

    private final ClientDAO clientDAO;

    public ClientService() {
        this.clientDAO = new ClientDAO();
    }

    /**
     * Création d'un nouveau client.
     */
    public boolean creerClient(Client client) {

        if (client == null) {
            System.out.println("Erreur : client inexistant.");
            return false;
        }

        if (client.getNom() == null || client.getNom().isEmpty()) {
            System.out.println("Erreur : nom manquant.");
            return false;
        }

        List<String> comptesExistants = clientDAO.listerTous().stream()
                .map(Client::getNumCompte)
                .collect(Collectors.toList());

        String numeroCompte = ClientNumeroCompteGenerator.genererNumeroCompte(comptesExistants);
        client.setNumCompte(numeroCompte);

        if (clientDAO.rechercherParId(client.getNumCompte()) != null) {
            System.out.println("Erreur : ce numéro de compte existe déjà.");
            return false;
        }

        if (client.getSoldeActuel() == null) {
            client.setSoldeActuel(BigDecimal.ZERO);
        }

        if (client.getSoldeActuel().doubleValue() < 0) {
            System.out.println("Erreur : le solde initial ne peut pas être négatif.");
            return false;
        }

        return clientDAO.ajouter(client);
    }

    public boolean modifierClient(Client client) {

        if (client == null || client.getNumCompte() == null || client.getNumCompte().isEmpty()) {
            System.out.println("Erreur : client invalide.");
            return false;
        }

        if (client.getNom() == null || client.getNom().isEmpty()) {
            System.out.println("Erreur : nom manquant.");
            return false;
        }

        return clientDAO.modifier(client);
    }

    public boolean supprimerClient(String numCompte) {

        if (numCompte == null || numCompte.isEmpty()) {
            System.out.println("Erreur : numéro de compte manquant.");
            return false;
        }

        return clientDAO.supprimer(numCompte);
    }

    public Client chercherClient(String numCompte) {

        if (numCompte == null || numCompte.isEmpty()) {
            return null;
        }

        return clientDAO.rechercherParId(numCompte);
    }

    public String generationCompteAuto() {
        List<String> comptesExistants = clientDAO.listerTous().stream()
                .map(Client::getNumCompte)
                .collect(Collectors.toList());
        return ClientNumeroCompteGenerator.genererNumeroCompte(comptesExistants);
    }

    public List<Client> tousLesClients() {
        return clientDAO.listerTous();
    }

    /**
     * Recherche par numéro de compte OU nom (LIKE %mot%).
     */
    public List<Client> rechercherClients(String motCle) {

        if (motCle == null || motCle.isBlank()) {
            return clientDAO.listerTous();
        }

        return clientDAO.rechercher(motCle.trim());
    }
}
