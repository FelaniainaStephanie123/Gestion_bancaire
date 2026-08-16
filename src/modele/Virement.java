package modele;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Virement {

    private String numVirement;
    private String numCompteEnvoyeur;
    private String numCompteBeneficiaire;
    private BigDecimal montant;
    private LocalDateTime dateTransfert;

    public Virement() {
    }

    public Virement(String numVirement, String numCompteEnvoyeur, String numCompteBeneficiaire, BigDecimal montant) {
        this.numVirement = numVirement;
        this.numCompteEnvoyeur = numCompteEnvoyeur;
        this.numCompteBeneficiaire = numCompteBeneficiaire;
        this.montant = montant;
    }

    public String getNumVirement() {
        return numVirement;
    }

    public void setNumVirement(String numVirement) {
        this.numVirement = numVirement;
    }

    public String getNumCompteEnvoyeur() {
        return numCompteEnvoyeur;
    }

    public void setNumCompteEnvoyeur(String numCompteEnvoyeur) {
        this.numCompteEnvoyeur = numCompteEnvoyeur;
    }

    public String getNumCompteBeneficiaire() {
        return numCompteBeneficiaire;
    }

    public void setNumCompteBeneficiaire(String numCompteBeneficiaire) {
        this.numCompteBeneficiaire = numCompteBeneficiaire;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public LocalDateTime getDateTransfert() {
        return dateTransfert;
    }

    public void setDateTransfert(LocalDateTime dateTransfert) {
        this.dateTransfert = dateTransfert;
    }

    @Override
    public String toString() {
        return numVirement + " : " + numCompteEnvoyeur + " -> " + numCompteBeneficiaire + " (" + montant + " Ar)";
    }
}
