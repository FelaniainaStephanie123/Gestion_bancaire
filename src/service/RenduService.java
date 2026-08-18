package service;

import dao.RenduDAO;
import dao.SituationPretDAO;
import modele.Rendu;
import modele.SituationPret;
import java.util.List;
import java.math.BigDecimal;
import dao.ClientDAO;
import dao.PretDAO;
import modele.Client;
import modele.Pret;
public class RenduService {

    private final RenduDAO renduDAO;
private final SituationPretDAO situationPretDAO;

    public RenduService() {
        this.renduDAO = new RenduDAO();
        this.situationPretDAO = new SituationPretDAO();
    }


    
   public boolean ajouterRemboursement(Rendu rendu) {

        if (rendu == null) {
            System.out.println("Erreur : remboursement inexistant.");
            return false;
        }

        if (rendu.getNumPret() == null || rendu.getNumPret().isEmpty()) {
            System.out.println("Erreur : numéro du prêt manquant.");
            return false;
        }

        if (rendu.getMontantPaye() == null ||
           rendu.getMontantPaye().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Erreur : montant invalide.");
            return false;
        }

        SituationPret situation = situationPretDAO.trouverParPret(rendu.getNumPret());

        if (situation == null) {
            System.out.println("Erreur : prêt introuvable.");
            return false;
        }

        if (rendu.getMontantPaye().compareTo(situation.getResteAPayer()) > 0) {
            System.out.println("Erreur : le remboursement dépasse la dette restante.");
            return false;
        }

        if (rendu.getDateRendu() == null) {
            System.out.println("Erreur : date de remboursement manquante.");
            return false;
        }

        BigDecimal nouveauReste = situation.getResteAPayer().subtract(rendu.getMontantPaye());

        if (nouveauReste.compareTo(BigDecimal.ZERO) == 0) {
            rendu.setSituation(Rendu.SITUATION_TOUT_PAYE);
        } else {
            rendu.setSituation(Rendu.SITUATION_PAYE_UNE_PART);
        }

        // return renduDAO.ajouter(rendu);
        // 1. Enregistrer le remboursement
        boolean renduAjoutre = renduDAO.ajouter(rendu);

        // 2. Extraire (soustraire) le montant payé du solde du client
        if (renduAjoutre) {
            PretDAO pretDAO = new PretDAO();
            ClientDAO clientDAO = new ClientDAO();

            Pret pret = pretDAO.rechercherParId(rendu.getNumPret());
            if (pret != null) {
                System.out.println("DEBUG - Prêt trouvé, NumCompte associé : " + pret.getNumCompte());
                Client client = clientDAO.rechercherParId(pret.getNumCompte());
                
                if (client != null) {
                    System.out.println("DEBUG - Client trouvé, Ancien solde : " + client.getSoldeActuel());
                    BigDecimal soldeActuel = client.getSoldeActuel();
                    if (soldeActuel == null) {
                        soldeActuel = BigDecimal.ZERO;
                    }

                    BigDecimal nouveauSolde = soldeActuel.subtract(rendu.getMontantPaye());
                    client.setSoldeActuel(nouveauSolde);
                    
                    boolean modifie = clientDAO.modifier(client);
                    System.out.println("DEBUG - Modification du client en base : " + modifie + " (Nouveau solde : " + nouveauSolde + ")");
                } else {
                    System.out.println("DEBUG - ERREUR : Client introuvable pour le compte ID: " + pret.getNumCompte());
                }
            } else {
                System.out.println("DEBUG - ERREUR : Prêt introuvable pour l'ID: " + rendu.getNumPret());
            }
        }

        return renduAjoutre;
    }

public boolean modifierRemboursement(Rendu rendu) {

    if(rendu == null){

        System.out.println("Erreur : remboursement inexistant.");
        return false;

    }

    if(rendu.getNumRendu() == null ||
       rendu.getNumRendu().isEmpty()){

        System.out.println("Erreur : numéro du remboursement manquant.");
        return false;

    }

    if(rendu.getNumPret() == null ||
       rendu.getNumPret().isEmpty()){

        System.out.println("Erreur : numéro du prêt manquant.");
        return false;

    }

    if(rendu.getMontantPaye() == null ||
       rendu.getMontantPaye().compareTo(BigDecimal.ZERO) <= 0){

        System.out.println("Erreur : montant invalide.");
        return false;

    }

    if(rendu.getDateRendu() == null){

        System.out.println("Erreur : date de remboursement manquante.");
        return false;

    }

    SituationPret situation =
            situationPretDAO.trouverParPret(rendu.getNumPret());

    if(situation == null){

        System.out.println("Erreur : prêt introuvable.");
        return false;

    }

    if(rendu.getMontantPaye()
            .compareTo(situation.getResteAPayer()) > 0){

        System.out.println(
            "Erreur : le remboursement dépasse la dette restante."
        );

        return false;

    }

    BigDecimal nouveauReste =
            situation.getResteAPayer()
            .subtract(rendu.getMontantPaye());

    if(nouveauReste.compareTo(BigDecimal.ZERO) == 0){

        rendu.setSituation(Rendu.SITUATION_TOUT_PAYE);

    }else{

        rendu.setSituation(Rendu.SITUATION_PAYE_UNE_PART);

    }

    return renduDAO.modifier(rendu);

}
public boolean supprimerRemboursement(String numRendu) {

    if (numRendu == null || numRendu.isEmpty()) {

        System.out.println("Erreur : numéro du remboursement manquant.");
        return false;

    }

    return renduDAO.supprimer(numRendu);

}


    /**
     * Chercher un remboursement
     */
    public Rendu chercherRendu(String numRendu) {

        if (numRendu == null || numRendu.isEmpty()) {
            return null;
        }

        return renduDAO.rechercherParId(numRendu);
    }


    /**
     * Liste des remboursements
     */
    public List<Rendu> tousLesRemboursements() {

        return renduDAO.listerTous();
    }
}