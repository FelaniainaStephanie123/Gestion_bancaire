package dao;

import modele.Agent;
import util.ConnexionBD;
import util.HachageMotDePasse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AgentDAO {

    /**
     * Vérifie l'identifiant + mot de passe.
     * @return l'Agent si les identifiants sont corrects, sinon null.
     */
    public Agent authentifier(String nomUtilisateur, String motDePasseEnClair) {

        String motDePasseHache = HachageMotDePasse.hacher(motDePasseEnClair);

        // La requête sélectionnera tous les champs y compris 'role'
        String sql = "SELECT * FROM agent WHERE nom_utilisateur = ? AND mot_de_passe = ?";

        try (Connection cn = ConnexionBD.getConnexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, nomUtilisateur);
            ps.setString(2, motDePasseHache);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Agent(
                            rs.getInt("id_agent"),
                            rs.getString("nom_utilisateur"),
                            rs.getString("nom_complet"),
                            rs.getString("role") // Récupération du rôle depuis la BDD
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // identifiants incorrects
    }
}