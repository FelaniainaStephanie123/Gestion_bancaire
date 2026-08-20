package dao;

import modele.Client;
import util.ConnexionBD;

// import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Exemple d'implémentation concrète de DAO<T, ID> pour Client.
 * Sert de modèle pour VirementDAO, PretDAO et RenduDAO.
 */
public class ClientDAO implements DAO<Client, String> {

    @Override
    public boolean ajouter(Client c) {
        String sql = "INSERT INTO client (num_compte, nom, prenoms, tel, mail, solde_actuel) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNumCompte());
            ps.setString(2, c.getNom());
            ps.setString(3, c.getPrenoms());
            ps.setString(4, c.getTel());
            ps.setString(5, c.getMail());
            ps.setBigDecimal(6, c.getSoldeActuel());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean modifier(Client c) {
        String sql = "UPDATE client SET nom = ?, prenoms = ?, tel = ?, mail = ?, solde_actuel = ? WHERE num_compte = ?";
        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getPrenoms());
            ps.setString(3, c.getTel());
            ps.setString(4, c.getMail());
            ps.setBigDecimal(5, c.getSoldeActuel());
            ps.setString(6, c.getNumCompte());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean supprimer(String numCompte) {
        return supprimerAvecMessage(numCompte) == null;
    }

    public String supprimerAvecMessage(String numCompte) {
        String sql = "UPDATE client SET actif = FALSE WHERE num_compte = ? AND actif = TRUE";
        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numCompte);
            return ps.executeUpdate() > 0 ? null : "Client introuvable ou déjà désactivé.";
        } catch (SQLException e) {
            return "Erreur lors de la suppression du client.";
        }
    }

    public boolean possedePretNonSolde(String numCompte) {
        String sql = "SELECT EXISTS (SELECT 1 FROM v_situation_prets "
                + "WHERE num_compte = ? AND reste_a_payer > 0)";
        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numCompte);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            return true;
        }
    }

    @Override
    public Client rechercherParId(String numCompte) {
        String sql = "SELECT * FROM client WHERE num_compte = ? AND actif = TRUE";
        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numCompte);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construireDepuisResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Client> listerTous() {
        List<Client> liste = new ArrayList<>();
        String sql = "SELECT * FROM client WHERE actif = TRUE ORDER BY num_compte DESC";
        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(construireDepuisResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    /**
     * Recherche par numéro de compte OU nom, avec LIKE (point 2 du barème).
     */
   public List<Client> rechercher(String motCle) {
        List<Client> liste = new ArrayList<>();
        String sql = "SELECT * FROM client WHERE actif = TRUE AND (LOWER(num_compte) LIKE LOWER(?) OR LOWER(nom) LIKE LOWER(?) OR LOWER(prenoms) LIKE LOWER(?)) ORDER BY nom";
        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            String motif = "%" + motCle + "%";
            ps.setString(1, motif);
            ps.setString(2, motif);
            ps.setString(3, motif);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(construireDepuisResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    private Client construireDepuisResultSet(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setNumCompte(rs.getString("num_compte"));
        c.setNom(rs.getString("nom"));
        c.setPrenoms(rs.getString("prenoms"));
        c.setTel(rs.getString("tel"));
        c.setMail(rs.getString("mail"));
        c.setSoldeActuel(rs.getBigDecimal("solde_actuel"));
        return c;
    }
}
