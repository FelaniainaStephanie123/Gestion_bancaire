package modele;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Rendu {

    public static final String SITUATION_TOUT_PAYE = "TOUT_PAYE";
    public static final String SITUATION_PAYE_UNE_PART = "PAYE_PARTIEL";

    private String numRendu;
    private String numPret;
    private String situation;
    private BigDecimal montantPaye;
    private LocalDate dateRendu;

    public Rendu() {
    }

    public Rendu(String numRendu, String numPret, String situation, BigDecimal montantPaye, LocalDate dateRendu) {
        this.numRendu = numRendu;
        this.numPret = numPret;
        this.situation = situation;
        this.montantPaye = montantPaye;
        this.dateRendu = dateRendu;
    }

    public String getNumRendu() {
        return numRendu;
    }

    public void setNumRendu(String numRendu) {
        this.numRendu = numRendu;
    }

    public String getNumPret() {
        return numPret;
    }

    public void setNumPret(String numPret) {
        this.numPret = numPret;
    }

    public String getSituation() {
        return situation;
    }

    public void setSituation(String situation) {
        this.situation = situation;
    }

    public BigDecimal getMontantPaye() {
        return montantPaye;
    }

    public void setMontantPaye(BigDecimal montantPaye) {
        this.montantPaye = montantPaye;
    }

    public LocalDate getDateRendu() {
        return dateRendu;
    }

    public void setDateRendu(LocalDate dateRendu) {
        this.dateRendu = dateRendu;
    }

    @Override
    public String toString() {
        return numRendu + " - prêt " + numPret + " : " + montantPaye + " Ar (" + situation + ")";
    }
}
