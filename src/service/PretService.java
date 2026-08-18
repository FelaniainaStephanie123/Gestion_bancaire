package service;

import dao.PretDAO;
import dao.SituationPretDAO;
import modele.Pret;
import modele.SituationPret;

import java.math.BigDecimal;
import java.util.List;

public class PretService {
    
    private final PretDAO pretDAO;
    private final SituationPretDAO situationPretDAO;

    public PretService() {
        this.pretDAO = new PretDAO();
        this.situationPretDAO = new SituationPretDAO();
    }

    /**
     * Création d'un nouveau prêt
     */
    public boolean creerPret(Pret pret) {

        if (pret == null) {
            System.out.println("Erreur : prêt inexistant.");
            return false;
        }

        if (pret.getMontantPrete() == null ||
            pret.getMontantPrete().doubleValue() <= 0) {

            System.out.println("Erreur : montant du prêt invalide.");
            return false;
        }

        if (pret.getNumCompte() == null || pret.getNumCompte().isEmpty()) {

            System.out.println("Erreur : numéro de compte manquant.");
            return false;
        }

        if (pret.getDatePret() == null) {

            System.out.println("Erreur : date du prêt manquante.");
            return false;
        }

        if (pret.getDateEcheance() != null &&
            pret.getDateEcheance().isBefore(pret.getDatePret())) {

            System.out.println("Erreur : échéance avant la date du prêt.");
            return false;
        }

        // Vérification : Empêcher un nouveau prêt si un prêt est déjà en cours (reste à payer > 0)
        List<SituationPret> situations = situationsDesPrets();
        for (SituationPret s : situations) {
            if (s.getNumCompte() != null && s.getNumCompte().equals(pret.getNumCompte())) {
                if (s.getResteAPayer() != null && s.getResteAPayer().compareTo(BigDecimal.ZERO) > 0) {
                    System.out.println("Erreur : le client a déjà un prêt en cours non soldé.");
                    return false;
                }
            }
        }

        return pretDAO.ajouter(pret);
    }

    /**
     * Recherche d'un prêt par son numéro
     */
    public Pret chercherPret(String numPret) {

        if (numPret == null || numPret.isEmpty()) {
            return null;
        }

        return pretDAO.rechercherParId(numPret);
    }

    public boolean modifierPret(Pret pret) {

        if (pret == null) {
            System.out.println("Erreur : prêt inexistant.");
            return false;
        }

        if (pret.getNumPret() == null || pret.getNumPret().isEmpty()) {
            System.out.println("Erreur : numéro du prêt manquant.");
            return false;
        }

        if (pret.getMontantPrete() == null ||
            pret.getMontantPrete().doubleValue() <= 0) {

            System.out.println("Erreur : montant du prêt invalide.");
            return false;
        }

        if (pret.getDatePret() == null) {
            System.out.println("Erreur : date du prêt manquante.");
            return false;
        }

        if (pret.getDateEcheance() != null &&
            pret.getDateEcheance().isBefore(pret.getDatePret())) {

            System.out.println("Erreur : échéance avant la date du prêt.");
            return false;
        }

        return pretDAO.modifier(pret);
    }

    public boolean supprimerPret(String numPret) {

        if (numPret == null || numPret.isEmpty()) {
            System.out.println("Erreur : numéro du prêt manquant.");
            return false;
        }

        return pretDAO.supprimer(numPret);
    }

    public List<Pret> tousLesPrets() {
        return pretDAO.listerTous();
    }

    /**
     * Liste toutes les situations des prêts
     */
    public List<SituationPret> situationsDesPrets() {
        return situationPretDAO.listerSituations();
    }
}