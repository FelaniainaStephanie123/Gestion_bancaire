package dao;

import modele.Benefice;
import util.ConnexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class BeneficeDAO {


    public Benefice calculerBenefice() {


        Benefice benefice = null;


        String sql = "SELECT * FROM v_benefice_banque";


        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {


            if (rs.next()) {


                benefice = new Benefice();


                benefice.setNombrePrets(
                    rs.getLong("nombre_prets")
                );


                benefice.setTotalPrete(
                    rs.getBigDecimal("total_prete")
                );


                benefice.setBeneficeTotal(
                    rs.getBigDecimal("benefice_total")
                );

            }


        } catch (SQLException e) {

            e.printStackTrace();

        }


        return benefice;
    }

}