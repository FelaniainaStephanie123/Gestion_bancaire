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
    String sqlPret = "INSERT INTO preter (num_pret, num_compte, montant_prete, taux_interet, date_pret, date_echeance) VALUES (?, ?, ?, ?, ?, ?)";
    String sqlUpdateSolde = "UPDATE client SET solde_actuel = solde_actuel + ? WHERE num_compte = ?";

    Connection cn = null;
    try {
        cn = ConnexionBD.getConnexion();
        // 1. Désactiver l'autocommit pour gérer la transaction manuellement
        cn.setAutoCommit(false);

        // 2. Exécuter l'insertion du prêt
        try (PreparedStatement psPret = cn.prepareStatement(sqlPret)) {
            psPret.setString(1, objet.getNumPret());
            psPret.setString(2, objet.getNumCompte());
            psPret.setBigDecimal(3, objet.getMontantPrete());
            psPret.setBigDecimal(4, objet.getTauxInteret());
            psPret.setDate(5, java.sql.Date.valueOf(objet.getDatePret()));
            psPret.setDate(6, java.sql.Date.valueOf(objet.getDateEcheance()));
            psPret.executeUpdate();
        }

        // 3. Exécuter la mise à jour du solde
        try (PreparedStatement psSolde = cn.prepareStatement(sqlUpdateSolde)) {
            psSolde.setBigDecimal(1, objet.getMontantPrete());
            psSolde.setString(2, objet.getNumCompte());
            psSolde.executeUpdate();
        }

        // 4. Valider la transaction
        cn.commit();
        return true;

    } catch (SQLException e) {
        // En cas d'erreur, annuler tout (Rollback)
        if (cn != null) {
            try { cn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
        e.printStackTrace();
        return false;
    } finally {
        // Fermer la connexion
        if (cn != null) {
            try { 
                cn.setAutoCommit(true); // Remettre en auto-commit
                cn.close(); 
            } catch (SQLException e) { e.printStackTrace(); }
        }
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