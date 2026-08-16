package service;

import dao.ClientDAO;
import dao.VirementDAO;
import modele.Client;
import modele.Virement;

import java.util.List;

public class VirementService {

    private final VirementDAO virementDAO;
    private final ClientDAO clientDAO;

    public VirementService() {
        this.virementDAO = new VirementDAO();
        this.clientDAO = new ClientDAO();
    }

    /**
     * Effectue un virement : valide les données côté Java (retour rapide et lisible),
     * puis laisse les triggers PostgreSQL faire la vérification finale du solde
     * et la mise à jour des 2 comptes de façon atomique.
     *
     * @return null si succès, sinon le message d'erreur à afficher.
     */
    public String effectuerVirement(Virement virement) {

        if (virement == null) {
            return "Virement invalide.";
        }

        if (virement.getNumCompteEnvoyeur() == null || virement.getNumCompteEnvoyeur().isEmpty()) {
            return "Le compte envoyeur est obligatoire.";
        }

        if (virement.getNumCompteBeneficiaire() == null || virement.getNumCompteBeneficiaire().isEmpty()) {
            return "Le compte bénéficiaire est obligatoire.";
        }

        if (virement.getNumCompteEnvoyeur().equals(virement.getNumCompteBeneficiaire())) {
            return "Le compte envoyeur et le bénéficiaire doivent être différents.";
        }

        if (virement.getMontant() == null || virement.getMontant().doubleValue() <= 0) {
            return "Le montant doit être supérieur à 0.";
        }

        Client envoyeur = clientDAO.rechercherParId(virement.getNumCompteEnvoyeur());
        if (envoyeur == null) {
            return "Le compte envoyeur n'existe pas.";
        }

        Client beneficiaire = clientDAO.rechercherParId(virement.getNumCompteBeneficiaire());
        if (beneficiaire == null) {
            return "Le compte bénéficiaire n'existe pas.";
        }

        if (envoyeur.getSoldeActuel().compareTo(virement.getMontant()) < 0) {
            return "Solde insuffisant pour effectuer ce virement.";
        }

        if (virement.getNumVirement() == null || virement.getNumVirement().isEmpty()) {
            virement.setNumVirement(genererProchainNumero());
        }

        // L'insertion déclenche les triggers PostgreSQL (vérif finale + mise à jour des soldes)
        return virementDAO.ajouterAvecMessage(virement);
    }

    public boolean modifierVirement(Virement virement) {
        if (virement == null || virement.getNumVirement() == null || virement.getNumVirement().isEmpty()) {
            return false;
        }
        return virementDAO.modifier(virement);
    }

    public boolean supprimerVirement(String numVirement) {
        if (numVirement == null || numVirement.isEmpty()) {
            return false;
        }
        return virementDAO.supprimer(numVirement);
    }

    public Virement chercherVirement(String numVirement) {
        if (numVirement == null || numVirement.isEmpty()) {
            return null;
        }
        return virementDAO.rechercherParId(numVirement);
    }

    public List<Virement> tousLesVirements() {
        return virementDAO.listerTous();
    }

    public List<Virement> rechercherVirements(String motCle) {
        if (motCle == null || motCle.isBlank()) {
            return virementDAO.listerTous();
        }
        return virementDAO.rechercher(motCle.trim());
    }

    /** Récupère le solde et l'identité à jour d'un compte (utile pour l'avis PDF). */
    public Client infosCompte(String numCompte) {
        return clientDAO.rechercherParId(numCompte);
    }

    private String genererProchainNumero() {
        int nombre = virementDAO.listerTous().size() + 1;
        return String.format("V%03d", nombre);
    }
}
