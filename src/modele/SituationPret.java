package modele;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SituationPret {

    private String numPret;
    private String numCompte;

    private String nom;
    private String prenoms;

    private BigDecimal montantPrete;
    private BigDecimal montantARendre;
    private BigDecimal totalPaye;
    private BigDecimal resteAPayer;

    private LocalDate datePret;
    private LocalDate dateEcheance;

    private String situationActuelle;


    public SituationPret() {
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


    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


    public String getPrenoms() {
        return prenoms;
    }

    public void setPrenoms(String prenoms) {
        this.prenoms = prenoms;
    }


    public BigDecimal getMontantPrete() {
        return montantPrete;
    }

    public void setMontantPrete(BigDecimal montantPrete) {
        this.montantPrete = montantPrete;
    }


    public BigDecimal getMontantARendre() {
        return montantARendre;
    }

    public void setMontantARendre(BigDecimal montantARendre) {
        this.montantARendre = montantARendre;
    }


    public BigDecimal getTotalPaye() {
        return totalPaye;
    }

    public void setTotalPaye(BigDecimal totalPaye) {
        this.totalPaye = totalPaye;
    }


    public BigDecimal getResteAPayer() {
        return resteAPayer;
    }

    public void setResteAPayer(BigDecimal resteAPayer) {
        this.resteAPayer = resteAPayer;
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


    public String getSituationActuelle() {
        return situationActuelle;
    }

    public void setSituationActuelle(String situationActuelle) {
        this.situationActuelle = situationActuelle;
    }


    @Override
    public String toString() {

        return "Pret " + numPret +
                " | Client : " + nom + " " + prenoms +
                " | Reste : " + resteAPayer +
                " Ar | Situation : " + situationActuelle;
    }
}