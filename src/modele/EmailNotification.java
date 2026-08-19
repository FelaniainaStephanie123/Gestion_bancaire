package modele;

import java.time.LocalDateTime;

public class EmailNotification {

    private long id;
    private String destinataire;
    private String sujet;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private boolean envoyee;
    private LocalDateTime envoyeeLe;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(String destinataire) {
        this.destinataire = destinataire;
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public boolean isEnvoyee() {
        return envoyee;
    }

    public void setEnvoyee(boolean envoyee) {
        this.envoyee = envoyee;
    }

    public LocalDateTime getEnvoyeeLe() {
        return envoyeeLe;
    }

    public void setEnvoyeeLe(LocalDateTime envoyeeLe) {
        this.envoyeeLe = envoyeeLe;
    }
}
