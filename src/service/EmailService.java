package service;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import java.util.Properties;


public class EmailService {


    private final String expediteur = System.getenv("BANKSYS_EMAIL");
    private final String motDePasse = System.getenv("BANKSYS_EMAIL_PASSWORD");


    public boolean envoyerMail(String destinataire, String sujet, String contenu) {

        if (expediteur == null || expediteur.isBlank()
                || motDePasse == null || motDePasse.isBlank()) {
            System.err.println("Email non envoye : configurez BANKSYS_EMAIL et BANKSYS_EMAIL_PASSWORD.");
            return false;
        }


        Properties properties = new Properties();


        properties.put(
            "mail.smtp.host",
            "smtp.gmail.com"
        );

        properties.put(
            "mail.smtp.port",
            "587"
        );

        properties.put(
            "mail.smtp.auth",
            "true"
        );

        properties.put(
            "mail.smtp.starttls.enable",
            "true"
        );


        Session session = Session.getInstance(
            properties,
            new Authenticator() {

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {

                    return new PasswordAuthentication(
                        expediteur,
                        motDePasse
                    );

                }
            }
        );


        try {


            Message message = new MimeMessage(session);


            message.setFrom(
                new InternetAddress(expediteur)
            );


            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(destinataire)
            );


                try {
                    message.setSubject(MimeUtility.encodeText(
                            sujet, StandardCharsets.UTF_8.name(), "B"));
                } catch (UnsupportedEncodingException e) {
                    message.setSubject(sujet);
                }


            message.setContent(contenu, "text/plain; charset=UTF-8");



            Transport.send(message);


            return true;



        } catch (AuthenticationFailedException e) {
            System.err.println("Email non envoye : identifiants Gmail refuses. Utilisez un mot de passe d'application.");
            return false;
        } catch (MessagingException e) {


            e.printStackTrace();

            return false;

        }

    }

}