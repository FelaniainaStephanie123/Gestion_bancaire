package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnexionBD {

    private static final String URL = "jdbc:postgresql://localhost:5432/banque_bdd?charSet=UTF-8";
    private static final String UTILISATEUR = "postgres";
    private static final String MOT_DE_PASSE = "2006";

    private ConnexionBD() {
    }

    public static Connection getConnexion() {

        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, UTILISATEUR, MOT_DE_PASSE);

        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL introuvable");
            e.printStackTrace();

        } catch (SQLException e) {
            System.err.println("Erreur de connexion PostgreSQL");
            e.printStackTrace();
        }

        return null;
    }

    public static void fermer() {
        // Les connexions sont maintenant fermees par chaque DAO.
    }
}