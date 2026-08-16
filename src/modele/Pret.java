package modele;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Pret {

    private String numPret;
    private String numCompte;
    private BigDecimal montantPrete;
    private BigDecimal tauxInteret;
    private BigDecimal montantARendre; // calculé côté BDD (colonne générée), en lecture seule
    private LocalDate datePret;
    private LocalDate dateEcheance;

    public Pret() {
    }

    public Pret(String numPret, String numCompte, BigDecimal montantPrete, LocalDate datePret, LocalDate dateEcheance) {
        this.numPret = numPret;
        this.numCompte = numCompte;
        this.montantPrete = montantPrete;
        this.tauxInteret = new BigDecimal("10.00");
        this.datePret = datePret;
        this.dateEcheance = dateEcheance;
    }

    public String getNumPret() {
        return numPret;
    }

    public void setNumPret(String numPret) {
        this.numPret = numPret;
    }

    public String getNumCompte() {
        return numCompte;
    }

    public void setNumCompte(String numCompte) {
        this.numCompte = numCompte;
    }

    public BigDecimal getMontantPrete() {
        return montantPrete;
    }

    public void setMontantPrete(BigDecimal montantPrete) {
        this.montantPrete = montantPrete;
    }

    public BigDecimal getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(BigDecimal tauxInteret) {
        this.tauxInteret = tauxInteret;
    }

    public BigDecimal getMontantARendre() {
        return montantARendre;
    }

    public void setMontantARendre(BigDecimal montantARendre) {
        this.montantARendre = montantARendre;
    }

    public LocalDate getDatePret() {
        return datePret;
    }

    public void setDatePret(LocalDate datePret) {
        this.datePret = datePret;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    @Override
    public String toString() {
        return numPret + " - compte " + numCompte + " : " + montantPrete + " Ar prêtés";
    }
}
