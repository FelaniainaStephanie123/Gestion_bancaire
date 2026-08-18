package service;

import java.util.ArrayList;
import java.util.List;

public final class ClientNumeroCompteGenerator {

    private static final String PREFIX = "CLI";

    private ClientNumeroCompteGenerator() {
    }

    public static String genererNumeroCompte(List<String> comptesExistants) {
        List<String> comptes = comptesExistants == null ? new ArrayList<>() : comptesExistants;
        int maxNumero = 0;

        for (String compte : comptes) {
            if (compte == null || compte.trim().isEmpty()) {
                continue;
            }

            String compteValide = compte.trim().toUpperCase();
            if (!compteValide.startsWith(PREFIX)) {
                continue;
            }

            String suffixe = compteValide.substring(PREFIX.length());
            if (!suffixe.matches("\\d+")) {
                continue;
            }

            int numero = Integer.parseInt(suffixe);
            if (numero > maxNumero) {
                maxNumero = numero;
            }
        }

        return String.format("%s%03d", PREFIX, maxNumero + 1);
    }
}
