package dao;

import modele.SituationPret;
import util.ConnexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class SituationPretDAO {


    public List<SituationPret> listerSituations() {

        List<SituationPret> liste = new ArrayList<>();

        String sql = "SELECT * FROM v_situation_prets";


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



    public List<SituationPret> rechercherParSituation(String situation) {

        List<SituationPret> liste = new ArrayList<>();

        String sql = 
            "SELECT * FROM v_situation_prets WHERE situation_actuelle = ?";


        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {


            ps.setString(1, situation);


            try(ResultSet rs = ps.executeQuery()) {


                while(rs.next()) {

                    liste.add(construireDepuisResultSet(rs));

                }

            }


        } catch(SQLException e) {

            e.printStackTrace();

        }


        return liste;
    }




    private SituationPret construireDepuisResultSet(ResultSet rs) throws SQLException {


        SituationPret sp = new SituationPret();


        sp.setNumPret(rs.getString("num_pret"));

        sp.setNumCompte(rs.getString("num_compte"));


        sp.setNom(rs.getString("nom"));

        sp.setPrenoms(rs.getString("prenoms"));


        sp.setMontantPrete(
            rs.getBigDecimal("montant_prete")
        );


        sp.setMontantARendre(
            rs.getBigDecimal("montant_a_rendre")
        );


        sp.setTotalPaye(
            rs.getBigDecimal("total_paye")
        );


        sp.setResteAPayer(
            rs.getBigDecimal("reste_a_payer")
        );


        sp.setDatePret(
            rs.getDate("date_pret").toLocalDate()
        );


        if(rs.getDate("date_echeance") != null) {

            sp.setDateEcheance(
                rs.getDate("date_echeance").toLocalDate()
            );

        }


        sp.setSituationActuelle(
            rs.getString("situation_actuelle")
        );


        return sp;
    }
    public SituationPret trouverParPret(String numPret) {

    SituationPret situation = null;

    String sql = "SELECT * FROM v_situation_prets WHERE num_pret = ?";


    try (Connection cn = ConnexionBD.getConnexion();
         PreparedStatement ps = cn.prepareStatement(sql)) {


        ps.setString(1, numPret);


        try (ResultSet rs = ps.executeQuery()) {


            if (rs.next()) {

                situation = construireDepuisResultSet(rs);

            }

        }


    } catch (SQLException e) {

        e.printStackTrace();

    }


    return situation;
}

}