package service;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;


public class EmailService {


    private final String expediteur = "nambinintsoaranto67@gmail.com";
    private final String motDePasse = "icxz luvs qjox cmdt";


    public boolean envoyerMail(String destinataire, String sujet, String contenu) {


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


            message.setSubject(sujet);


            message.setText(contenu);



            Transport.send(message);


            return true;



        } catch (MessagingException e) {


            e.printStackTrace();

            return false;

        }

    }

}