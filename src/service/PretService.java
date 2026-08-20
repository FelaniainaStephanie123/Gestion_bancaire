package service;

import dao.PretDAO;
import dao.SituationPretDAO;
import modele.EmailNotification;
import modele.Pret;
import modele.SituationPret;
import util.ConnexionBD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;

public class PretService {
    
    private final PretDAO pretDAO;
    private final SituationPretDAO situationPretDAO;

    public PretService() {
        this.pretDAO = new PretDAO();
        this.situationPretDAO = new SituationPretDAO();
    }

    /**
     * Création d'un nouveau prêt
     */
    public boolean creerPret(Pret pret) {

        if (pret == null) {
            System.out.println("Erreur : prêt inexistant.");
            return false;
        }

        if (pret.getMontantPrete() == null ||
            pret.getMontantPrete().doubleValue() <= 0) {

            System.out.println("Erreur : montant du prêt invalide.");
            return false;
        }

        if (pret.getNumCompte() == null || pret.getNumCompte().isEmpty()) {

            System.out.println("Erreur : numéro de compte manquant.");
            return false;
        }

        if (pret.getDatePret() == null) {

            System.out.println("Erreur : date du prêt manquante.");
            return false;
        }

        if (pret.getDateEcheance() != null &&
            pret.getDateEcheance().isBefore(pret.getDatePret())) {

            System.out.println("Erreur : échéance avant la date du prêt.");
            return false;
        }

        List<SituationPret> situations = situationsDesPrets();
        for (SituationPret s : situations) {
            if (s.getNumCompte() != null && s.getNumCompte().equals(pret.getNumCompte())) {
                if (s.getResteAPayer() != null && s.getResteAPayer().compareTo(BigDecimal.ZERO) > 0) {
                    System.out.println("Erreur : le client a déjà un prêt en cours non soldé.");
                    return false;
                }
            }
        }

        // 1. Enregistrer le prêt
        boolean pretAjoute = pretDAO.ajouter(pret);

        // 2. Programmer la notification
        if (pretAjoute) {
            NotificationService notificationService = new NotificationService();
            notificationService.programmerNotificationPret(pret);
        }

        return pretAjoute;
    }

    /**
     * Recherche de l'historique des notifications e-mail envoyées
     */
    public List<EmailNotification> trouverHistorique(LocalDate debut, LocalDate fin) {
        List<EmailNotification> historique = new ArrayList<>();
        
        String sql = "SELECT id, destinataire, sujet, contenu, date_envoi, envoyee, envoyee_le "
            + "FROM notification_email WHERE envoyee = TRUE "
            + "AND (?::timestamp IS NULL OR envoyee_le >= ?::timestamp) "
            + "AND (?::timestamp IS NULL OR envoyee_le < ?::timestamp) "
            + "ORDER BY envoyee_le DESC NULLS LAST, id DESC";

        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
             
            Timestamp debutTimestamp = (debut == null) ? null : Timestamp.valueOf(debut.atStartOfDay());
            Timestamp finTimestamp = (fin == null) ? null : Timestamp.valueOf(fin.plusDays(1).atStartOfDay());
            
            ps.setTimestamp(1, debutTimestamp);
            ps.setTimestamp(2, debutTimestamp);
            ps.setTimestamp(3, finTimestamp);
            ps.setTimestamp(4, finTimestamp);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmailNotification notification = new EmailNotification();
                    notification.setId(rs.getLong("id"));
                    notification.setDestinataire(rs.getString("destinataire"));
                    notification.setSujet(rs.getString("sujet"));
                    notification.setContenu(rs.getString("contenu"));
                    notification.setDateEnvoi(rs.getTimestamp("date_envoi").toLocalDateTime());
                    notification.setEnvoyee(rs.getBoolean("envoyee"));
                    if (rs.getTimestamp("envoyee_le") != null) {
                        notification.setEnvoyeeLe(rs.getTimestamp("envoyee_le").toLocalDateTime());
                    }
                    historique.add(notification);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historique;
    }

    public List<EmailNotification> trouverHistorique() {
        return trouverHistorique(null, null);
    }

    /**
     * Recherche d'un prêt par son numéro
     */
    public Pret chercherPret(String numPret) {

        if (numPret == null || numPret.isEmpty()) {
            return null;
        }

        return pretDAO.rechercherParId(numPret);
    }

    public boolean modifierPret(Pret pret) {

        if (pret == null) {
            System.out.println("Erreur : prêt inexistant.");
            return false;
        }

        if (pret.getNumPret() == null || pret.getNumPret().isEmpty()) {
            System.out.println("Erreur : numéro du prêt manquant.");
            return false;
        }

        if (pret.getMontantPrete() == null ||
            pret.getMontantPrete().doubleValue() <= 0) {

            System.out.println("Erreur : montant du prêt invalide.");
            return false;
        }

        if (pret.getDatePret() == null) {
            System.out.println("Erreur : date du prêt manquante.");
            return false;
        }

        if (pret.getDateEcheance() != null &&
            pret.getDateEcheance().isBefore(pret.getDatePret())) {

            System.out.println("Erreur : échéance avant la date du prêt.");
            return false;
        }

        return pretDAO.modifier(pret);
    }

    public boolean supprimerPret(String numPret) {

        if (numPret == null || numPret.isEmpty()) {
            System.out.println("Erreur : numéro du prêt manquant.");
            return false;
        }

        return pretDAO.supprimer(numPret);
    }

    public List<Pret> tousLesPrets() {
        return pretDAO.listerTous();
    }

    /**
     * Liste toutes les situations des prêts
     */
    public List<SituationPret> situationsDesPrets() {
        return situationPretDAO.listerSituations();
    }
    public List<Pret> getPretsFiltres(LocalDate dateDebut, LocalDate dateFin, String motCle) {
    return pretDAO.listerParPeriode(dateDebut, dateFin, motCle);
}
}