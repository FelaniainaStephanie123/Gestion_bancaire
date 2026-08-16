package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnexionBD {

    private static final String URL = "jdbc:postgresql://localhost:5432/banque_bdd";
    private static final String UTILISATEUR = "postgres";
    private static final String MOT_DE_PASSE = "2006";

    private static Connection connexion;

    private ConnexionBD() {
    }

    public static Connection getConnexion() {

        try {
            if (connexion == null || connexion.isClosed()) {

                Class.forName("org.postgresql.Driver");

                connexion = DriverManager.getConnection(
                    URL,
                    UTILISATEUR,
                    MOT_DE_PASSE
                );
            }

        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL introuvable");
            e.printStackTrace();

        } catch (SQLException e) {
            System.err.println("Erreur de connexion PostgreSQL");
            e.printStackTrace();
        }

        return connexion;
    }

    public static void fermer() {

        try {
            if (connexion != null && !connexion.isClosed()) {
                connexion.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}