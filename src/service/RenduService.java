package service;

import dao.RenduDAO;
import dao.SituationPretDAO;
import modele.Rendu;
import modele.SituationPret;
import java.util.List;
import java.math.BigDecimal;
public class RenduService {

    private final RenduDAO renduDAO;
private final SituationPretDAO situationPretDAO;

    public RenduService() {
        this.renduDAO = new RenduDAO();
        this.situationPretDAO = new SituationPretDAO();
    }


    
    public boolean ajouterRemboursement(Rendu rendu) {


    if(rendu == null){

        System.out.println("Erreur : remboursement inexistant.");
        return false;

    }


    if(rendu.getMontantPaye() == null ||
       rendu.getMontantPaye().compareTo(BigDecimal.ZERO) <= 0){

        System.out.println("Erreur : montant invalide.");
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
if(rendu.getNumPret() == null ||
   rendu.getNumPret().isEmpty()){

    System.out.println("Erreur : numéro du prêt manquant.");
    return false;

}
if(rendu.getDateRendu() == null){

    System.out.println("Erreur : date de remboursement manquante.");
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


    return renduDAO.ajouter(rendu);

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