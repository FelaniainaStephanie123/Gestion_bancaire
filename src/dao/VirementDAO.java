package dao;

import modele.Virement;
import util.ConnexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table virement.
 *
 * Particularité : à l'INSERT, les triggers PostgreSQL
 * (trg_avant_virement / trg_apres_virement) vérifient le solde
 * et mettent à jour les 2 comptes automatiquement. On récupère donc
 * le message d'erreur exact du trigger (ex: "Solde insuffisant")
 * via ajouterAvecMessage(), plus précis que le simple booléen du CRUD.
 */
public class VirementDAO implements DAO<Virement, String> {

    @Override
    public boolean ajouter(Virement v) {
        return ajouterAvecMessage(v) == null;
    }

    /**
     * Insère le virement (déclenche les triggers en base).
     * @return null si tout s'est bien passé, sinon le message d'erreur à afficher à l'utilisateur.
     */
    public String ajouterAvecMessage(Virement v) {
        String sql = "INSERT INTO virement (num_virement, num_compte_envoyeur, num_compte_beneficiaire, montant) VALUES (?, ?, ?, ?)";
        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, v.getNumVirement());
            ps.setString(2, v.getNumCompteEnvoyeur());
            ps.setString(3, v.getNumCompteBeneficiaire());
            ps.setBigDecimal(4, v.getMontant());

            ps.executeUpdate();
            return null;

        } catch (SQLException e) {
            // Le trigger PostgreSQL renvoie un message clair (RAISE EXCEPTION ...)
            return e.getMessage();
        }
    }

    @Override
    public boolean modifier(Virement v) {
        // NB : dans une vraie banque on ne modifie jamais un virement déjà exécuté
        // (les soldes ont déjà bougé). On l'autorise ici uniquement sur la date/traçabilité,
        // pas sur le montant ni les comptes, pour respecter l'exigence CRUD du barème.
        String sql = "UPDATE virement SET date_transfert = ? WHERE num_virement = ?";
        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(v.getDateTransfert()));
            ps.setString(2, v.getNumVirement());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean supprimer(String numVirement) {
        String sql = "DELETE FROM virement WHERE num_virement = ?";
        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numVirement);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Virement rechercherParId(String numVirement) {
        String sql = "SELECT * FROM virement WHERE num_virement = ?";
        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numVirement);
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
    public List<Virement> listerTous() {
        List<Virement> liste = new ArrayList<>();
        String sql = "SELECT * FROM virement ORDER BY date_transfert DESC";
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
     * Recherche par n° de virement ou n° de compte (envoyeur/bénéficiaire), avec LIKE.
     */
    public List<Virement> rechercher(String motCle) {
        List<Virement> liste = new ArrayList<>();
       String sql = "SELECT * FROM virement WHERE LOWER(num_virement) LIKE LOWER(?) " +
                 "OR LOWER(num_compte_envoyeur) LIKE LOWER(?) " +
                 "OR LOWER(num_compte_beneficiaire) LIKE LOWER(?) ORDER BY date_transfert DESC";
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

    private Virement construireDepuisResultSet(ResultSet rs) throws SQLException {
        Virement v = new Virement();
        v.setNumVirement(rs.getString("num_virement"));
        v.setNumCompteEnvoyeur(rs.getString("num_compte_envoyeur"));
        v.setNumCompteBeneficiaire(rs.getString("num_compte_beneficiaire"));
        v.setMontant(rs.getBigDecimal("montant"));
        Timestamp ts = rs.getTimestamp("date_transfert");
        if (ts != null) {
            v.setDateTransfert(ts.toLocalDateTime());
        }
        return v;
    }
}
