package test;

import service.ClientNumeroCompteGenerator;

import java.util.Arrays;
import java.util.List;

public class ClientNumeroCompteGeneratorTest {
    public static void main(String[] args) {
        List<String> comptesExistants = Arrays.asList("ACC001", "ACC002", "ACC010");
        String numero = ClientNumeroCompteGenerator.genererNumeroCompte(comptesExistants);

        if (!"CLI001".equals(numero)) {
            throw new AssertionError("Numéro de compte attendu : CLI001, obtenu : " + numero);
        }

        System.out.println("Test OK : " + numero);
    }
}
