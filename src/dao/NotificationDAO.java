package dao;


import modele.NotificationPret;
import util.ConnexionBD;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



public class NotificationDAO {


    public NotificationPret trouverPretEnCours(String numPret) {


        NotificationPret notification = null;


        String sql =
        """
        SELECT 
            c.mail,
            c.nom,
            c.prenoms,
            p.num_pret,
            v.reste_a_payer,
            v.date_echeance

        FROM v_situation_prets v

        JOIN client c 
            ON c.num_compte = v.num_compte

        JOIN preter p
            ON p.num_pret = v.num_pret

        WHERE p.num_pret = ?
        """;



        try(Connection cn = ConnexionBD.getConnexion();
            PreparedStatement ps = cn.prepareStatement(sql)){


            ps.setString(1, numPret);



            try(ResultSet rs = ps.executeQuery()){


                if(rs.next()){


                    notification = new NotificationPret();


                    notification.setMail(
                        rs.getString("mail")
                    );


                    notification.setNom(
                        rs.getString("nom")
                    );


                    notification.setPrenoms(
                        rs.getString("prenoms")
                    );


                    notification.setNumPret(
                        rs.getString("num_pret")
                    );


                    notification.setResteAPayer(
                        rs.getBigDecimal("reste_a_payer")
                    );


                    if(rs.getDate("date_echeance") != null){

                        notification.setDateEcheance(
                            rs.getDate("date_echeance").toLocalDate()
                        );

                    }

                }

            }


        }catch(SQLException e){

            e.printStackTrace();

        }


        return notification;

    }

}