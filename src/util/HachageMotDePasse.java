package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Petit utilitaire pour transformer un mot de passe en "empreinte" (hash) SHA-256.
 *
 * Pourquoi ne jamais stocker le mot de passe en clair ?
 * Si quelqu'un accède un jour à la base de données, il verra seulement
 * une suite de caractères illisible (le hash), jamais le vrai mot de passe.
 * Le hash est à sens unique : impossible de "dé-hasher" pour retrouver le mot de passe.
 * Pour vérifier un mot de passe, on hash ce que l'utilisateur tape et on
 * compare les 2 hashs entre eux.
 */
public class HachageMotDePasse {

    public static String hacher(String motDePasseEnClair) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] octetsHash = digest.digest(motDePasseEnClair.getBytes("UTF-8"));

            StringBuilder hexa = new StringBuilder();
            for (byte octet : octetsHash) {
                String morceau = Integer.toHexString(0xff & octet);
                if (morceau.length() == 1) {
                    hexa.append('0');
                }
                hexa.append(morceau);
            }
            return hexa.toString();

        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }
}
