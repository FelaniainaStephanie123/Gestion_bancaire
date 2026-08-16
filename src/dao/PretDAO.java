package dao;
import modele.Pret;
import util.ConnexionBD;

// import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PretDAO implements DAO<Pret, String> {
@Override
public boolean ajouter(Pret objet) {
      String sql = "INSERT INTO preter (num_pret, num_compte, montant_prete, taux_interet, date_pret, date_echeance) VALUES (?, ?, ?, ?, ?, ?)";
Pret p = objet;
    try (Connection cn = ConnexionBD.getConnexion();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        ps.setString(1, p.getNumPret());
        ps.setString(2, p.getNumCompte());
        ps.setBigDecimal(3, p.getMontantPrete());
        ps.setBigDecimal(4, p.getTauxInteret());
        ps.setDate(5, java.sql.Date.valueOf(p.getDatePret()));
        ps.setDate(6, java.sql.Date.valueOf(p.getDateEcheance()));

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

@Override
public boolean modifier(Pret objet) {
     String sql = "UPDATE preter SET num_compte = ?, montant_prete = ?, taux_interet = ?, date_pret = ?, date_echeance = ? WHERE num_pret = ?";

    try (Connection cn = ConnexionBD.getConnexion();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        ps.setString(1, objet.getNumCompte());
        ps.setBigDecimal(2, objet.getMontantPrete());
        ps.setBigDecimal(3, objet.getTauxInteret());
        ps.setDate(4, java.sql.Date.valueOf(objet.getDatePret()));
        ps.setDate(5, java.sql.Date.valueOf(objet.getDateEcheance()));
        ps.setString(6, objet.getNumPret());

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

@Override
public boolean supprimer(String id) {
   String sql = "DELETE FROM preter WHERE num_pret = ?";

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
public Pret rechercherParId(String id) {
    String sql = "SELECT * FROM preter WHERE num_pret = ?";

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
public List<Pret> listerTous() {
  List<Pret> liste = new ArrayList<>();

    String sql = "SELECT * FROM preter ORDER BY date_pret DESC";

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
private Pret construireDepuisResultSet(ResultSet rs) throws SQLException {

    Pret p = new Pret();

    p.setNumPret(rs.getString("num_pret"));
    p.setNumCompte(rs.getString("num_compte"));
    p.setMontantPrete(rs.getBigDecimal("montant_prete"));
    p.setTauxInteret(rs.getBigDecimal("taux_interet"));
    p.setMontantARendre(rs.getBigDecimal("montant_a_rendre"));
    p.setDatePret(rs.getDate("date_pret").toLocalDate());

    if (rs.getDate("date_echeance") != null) {
        p.setDateEcheance(rs.getDate("date_echeance").toLocalDate());
    }

    return p;
}
}