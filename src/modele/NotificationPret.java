package modele;

import java.math.BigDecimal;
import java.time.LocalDate;


public class NotificationPret {


    private String mail;
    private String nom;
    private String prenoms;

    private String numPret;

    private BigDecimal resteAPayer;

    private LocalDate dateEcheance;



    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
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


    public String getNumPret() {
        return numPret;
    }

    public void setNumPret(String numPret) {
        this.numPret = numPret;
    }


    public BigDecimal getResteAPayer() {
        return resteAPayer;
    }

    public void setResteAPayer(BigDecimal resteAPayer) {
        this.resteAPayer = resteAPayer;
    }


    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }



    @Override
    public String toString() {

        return nom + " " + prenoms
                + " | Pret : " + numPret
                + " | Reste : " + resteAPayer
                + " | Échéance : " + dateEcheance;
    }

}