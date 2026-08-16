package modele;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Client {

    private String numCompte;
    private String nom;
    private String prenoms;
    private String tel;
    private String mail;
    private BigDecimal soldeActuel;
    private LocalDateTime dateCreation;

    public Client() {
    }

    public Client(String numCompte, String nom, String prenoms, String tel, String mail, BigDecimal soldeActuel) {
        this.numCompte = numCompte;
        this.nom = nom;
        this.prenoms = prenoms;
        this.tel = tel;
        this.mail = mail;
        this.soldeActuel = soldeActuel;
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

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public BigDecimal getSoldeActuel() {
        return soldeActuel;
    }

    public void setSoldeActuel(BigDecimal soldeActuel) {
        this.soldeActuel = soldeActuel;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getNomComplet() {
        return nom + " " + prenoms;
    }

    @Override
    public String toString() {
        return numCompte + " - " + getNomComplet();
    }
}
