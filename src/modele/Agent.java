package modele;

public class Agent {

    private int idAgent;
    private String nomUtilisateur;
    private String nomComplet;
    private String role; // Ajout du rôle

    public Agent() {
    }

    public Agent(int idAgent, String nomUtilisateur, String nomComplet, String role) {
        this.idAgent = idAgent;
        this.nomUtilisateur = nomUtilisateur;
        this.nomComplet = nomComplet;
        this.role = role;
    }

    public int getIdAgent() {
        return idAgent;
    }

    public void setIdAgent(int idAgent) {
        this.idAgent = idAgent;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}