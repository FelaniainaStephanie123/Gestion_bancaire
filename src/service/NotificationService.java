package service;

import dao.NotificationDAO;
import modele.NotificationPret;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import service.EmailService;

public class NotificationService {

    private final EmailService emailService;
    private final NotificationDAO notificationDAO;


    public NotificationService() {

        this.notificationDAO = new NotificationDAO();
        this.emailService = new EmailService();
    }



    public String genererNotification(String numPret) {


        NotificationPret notification =
                notificationDAO.trouverPretEnCours(numPret);



        if(notification == null){

            return "Aucun prêt trouvé.";

        }



        long joursRestants = 0;


        if(notification.getDateEcheance() != null){

            joursRestants =
                ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    notification.getDateEcheance()
                );

        }



        String message =

            "Bonjour " 
            + notification.getNom()
            + " "
            + notification.getPrenoms()
            + ",\n\n"

            + "Votre prêt numéro "
            + notification.getNumPret()
            + " est toujours en cours.\n\n"

            + "Montant restant à payer : "
            + notification.getResteAPayer()
            + " Ar\n"

            + "Nombre de jours restants : "
            + joursRestants
            + " jours.\n\n"

            + "Merci de régulariser votre situation.";



        return message;

    }
public boolean envoyerNotification(String numPret) {


    NotificationPret notification =
            notificationDAO.trouverPretEnCours(numPret);


    if(notification == null){

        return false;

    }


    String message = genererNotification(numPret);


    return emailService.envoyerMail(
            notification.getMail(),
            "Rappel concernant votre prêt " + numPret,
            message
    );

}
}