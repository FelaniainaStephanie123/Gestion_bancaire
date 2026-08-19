package dao;

import modele.EmailNotification;
import util.ConnexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmailNotificationDAO {

    public boolean ajouter(String destinataire, String sujet, String contenu, LocalDateTime dateEnvoi) {
        String sql = "INSERT INTO notification_email (destinataire, sujet, contenu, date_envoi) VALUES (?, ?, ?, ?)";

        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, destinataire);
            ps.setString(2, sujet);
            ps.setString(3, contenu);
            ps.setTimestamp(4, Timestamp.valueOf(dateEnvoi));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<EmailNotification> trouverEnAttente(LocalDateTime maintenant) {
        List<EmailNotification> notifications = new ArrayList<>();
        String sql = "SELECT id, destinataire, sujet, contenu, date_envoi "
                + "FROM notification_email WHERE envoyee = FALSE AND date_envoi <= ? ORDER BY date_envoi";

        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(maintenant));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmailNotification notification = new EmailNotification();
                    notification.setId(rs.getLong("id"));
                    notification.setDestinataire(rs.getString("destinataire"));
                    notification.setSujet(rs.getString("sujet"));
                    notification.setContenu(rs.getString("contenu"));
                    notification.setDateEnvoi(rs.getTimestamp("date_envoi").toLocalDateTime());
                    notifications.add(notification);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public boolean marquerEnvoyee(long id) {
        String sql = "UPDATE notification_email SET envoyee = TRUE, envoyee_le = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean enregistrerEnvoyee(String destinataire, String sujet, String contenu) {
        String sql = "INSERT INTO notification_email "
                + "(destinataire, sujet, contenu, date_envoi, envoyee, envoyee_le) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, TRUE, CURRENT_TIMESTAMP)";

        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, destinataire);
            ps.setString(2, sujet);
            ps.setString(3, contenu);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<EmailNotification> trouverHistorique() {
        return trouverHistorique(null, null);
    }

    public List<EmailNotification> trouverHistorique(LocalDate debut, LocalDate fin) {
        List<EmailNotification> historique = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, destinataire, sujet, contenu, date_envoi, envoyee, envoyee_le "
                + "FROM notification_email WHERE envoyee = TRUE");
        List<Timestamp> dates = new ArrayList<>();

        if (debut != null) {
            sql.append(" AND envoyee_le >= ?");
            dates.add(Timestamp.valueOf(debut.atStartOfDay()));
        }
        if (fin != null) {
            sql.append(" AND envoyee_le < ?");
            dates.add(Timestamp.valueOf(fin.plusDays(1).atStartOfDay()));
        }
        sql.append(" ORDER BY envoyee_le DESC NULLS LAST, id DESC");

        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int index = 0; index < dates.size(); index++) {
                ps.setTimestamp(index + 1, dates.get(index));
            }

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
}