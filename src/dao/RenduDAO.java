package dao;

import modele.Rendu;
import util.ConnexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RenduDAO implements DAO<Rendu, String> {

    @Override
    public boolean ajouter(Rendu objet) {
      String sql = "INSERT INTO rendre (num_pret, situation, montant_paye, date_rendu) VALUES (?, ?, ?, ?)";

    try (Connection cn = ConnexionBD.getConnexion();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        ps.setString(1, objet.getNumPret());
        ps.setString(2, objet.getSituation());
        ps.setBigDecimal(3, objet.getMontantPaye());
        ps.setDate(4, java.sql.Date.valueOf(objet.getDateRendu()));

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
    }

    @Override
    public boolean modifier(Rendu objet) {
        String sql = "UPDATE rendre SET num_pret = ?, situation = ?, montant_paye = ?, date_rendu = ? WHERE num_rendu = ?";

    try (Connection cn = ConnexionBD.getConnexion();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        ps.setString(1, objet.getNumPret());
        ps.setString(2, objet.getSituation());
        ps.setBigDecimal(3, objet.getMontantPaye());
        ps.setDate(4, java.sql.Date.valueOf(objet.getDateRendu()));
        ps.setString(5, objet.getNumRendu());

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
    }

    @Override
    public boolean supprimer(String id) {
      String sql = "DELETE FROM rendre WHERE num_rendu = ?";

    try (Connection cn = ConnexionBD.getConnexion();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        ps.setString(1, id);

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
    }

    @Override
    public Rendu rechercherParId(String id) {
          String sql = "SELECT * FROM rendre WHERE num_rendu = ?";

    try (Connection cn = ConnexionBD.getConnexion();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        ps.setString(1, id);

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
    public List<Rendu> listerTous() {
         List<Rendu> liste = new ArrayList<>();

    String sql = "SELECT * FROM rendre ORDER BY date_rendu DESC";

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
private Rendu construireDepuisResultSet(ResultSet rs) throws SQLException {

    Rendu r = new Rendu();

    r.setNumRendu(rs.getString("num_rendu"));
    r.setNumPret(rs.getString("num_pret"));
    r.setSituation(rs.getString("situation"));
    r.setMontantPaye(rs.getBigDecimal("montant_paye"));
    r.setDateRendu(rs.getDate("date_rendu").toLocalDate());

    return r;
}
}