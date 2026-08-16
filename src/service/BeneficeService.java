package service;

import dao.BeneficeDAO;
import modele.Benefice;


public class BeneficeService {


    private final BeneficeDAO beneficeDAO;


    public BeneficeService() {

        this.beneficeDAO = new BeneficeDAO();

    }


    /**
     * Retourne le bénéfice total de la banque
     */
    public Benefice obtenirBenefice() {

        return beneficeDAO.calculerBenefice();

    }

}