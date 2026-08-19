package service;

import dao.ClientDAO;
import dao.EmailNotificationDAO;
import dao.NotificationDAO;
import modele.Client;
import modele.EmailNotification;
import modele.NotificationPret;
import modele.Pret;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class NotificationService {

    private static final long DELAI_TEST_SECONDES = 10;
    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread thread = new Thread(runnable, "notification-email");
                    thread.setDaemon(true);
                    return thread;
                }
            });
    private static boolean traitementDemarre;

    private final EmailService emailService;
    private final NotificationDAO notificationDAO;
    private final EmailNotificationDAO emailNotificationDAO;

    public NotificationService() {
        this.emailService = new EmailService();
        this.notificationDAO = new NotificationDAO();
        this.emailNotificationDAO = new EmailNotificationDAO();
        demarrerTraitementAutomatique();
    }

    private synchronized void demarrerTraitementAutomatique() {
        if (traitementDemarre) {
            return;
        }
        traitementDemarre = true;
        SCHEDULER.scheduleAtFixedRate(this::traiterNotificationsEnAttente, 0, 1, TimeUnit.SECONDS);
    }

    private void traiterNotificationsEnAttente() {
        List<EmailNotification> notifications = emailNotificationDAO.trouverEnAttente(LocalDateTime.now());
        for (EmailNotification notification : notifications) {
            boolean envoye = emailService.envoyerMail(
                    notification.getDestinataire(),
                    notification.getSujet(),
                    notification.getContenu());
            if (envoye) {
                emailNotificationDAO.marquerEnvoyee(notification.getId());
            }
        }
    }

    public boolean programmerNotificationPret(Pret pret) {
        if (pret == null || pret.getNumPret() == null || pret.getNumPret().isBlank()) {
            return false;
        }

        Client client = new ClientDAO().rechercherParId(pret.getNumCompte());
        if (client == null || client.getMail() == null || client.getMail().isBlank()) {
            System.out.println("Aucun email client pour le pret " + pret.getNumPret());
            return false;
        }

        LocalDate datePret = pret.getDatePret() == null ? LocalDate.now() : pret.getDatePret();
        LocalDate dateLimite = datePret.plusDays(15);
        String nom = client.getNomComplet();
        String sujet = "Confirmation de votre pret n°" + pret.getNumPret();
        String message = "Bonjour " + nom + ",\n\n"
                + "Votre pret numero " + pret.getNumPret() + " a bien ete accorde.\n"
                + "La date limite de remboursement est le " + dateLimite + ".\n\n"
                + "Montant prete : " + pret.getMontantPrete() + " Ar\n\n"
                + "Cordialement,\nLa Banque.";

        boolean programme = emailNotificationDAO.ajouter(
                client.getMail(),
                sujet,
                message,
                LocalDateTime.now().plusSeconds(DELAI_TEST_SECONDES));

        String sujetRappel = "Rappel de remboursement - pret n°" + pret.getNumPret();
        String messageRappel = "Bonjour " + nom + ",\n\n"
                + "Le remboursement de votre pret n°" + pret.getNumPret()
                + " est attendu au plus tard le " + dateLimite + ".\n\n"
                + "Merci de regulariser votre situation.\n\n"
                + "Cordialement,\nLa Banque.";
        long joursAvantRappel = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), dateLimite));
        emailNotificationDAO.ajouter(
                client.getMail(),
                sujetRappel,
                messageRappel,
                LocalDateTime.now().plusDays(joursAvantRappel));

        return programme;
    }

    public String genererNotification(String numPret) {
        NotificationPret notification = notificationDAO.trouverPretEnCours(numPret);
        if (notification == null) {
            return "Aucun pret trouve.";
        }

        long joursRestants = notification.getDateEcheance() == null ? 0
                : ChronoUnit.DAYS.between(LocalDate.now(), notification.getDateEcheance());
        return "Bonjour " + notification.getNom() + " " + notification.getPrenoms() + ",\n\n"
                + "Votre pret numero " + notification.getNumPret() + " est toujours en cours.\n\n"
                + "Montant restant a payer : " + notification.getResteAPayer() + " Ar\n"
                + "Nombre de jours restants : " + joursRestants + " jours.\n\n"
                + "Merci de regulariser votre situation.";
    }

    public boolean envoyerNotification(String numPret) {
        NotificationPret notification = notificationDAO.trouverPretEnCours(numPret);
        if (notification == null) {
            return false;
        }

        String sujet = "Rappel concernant votre pret " + numPret;
        String message = genererNotification(numPret);
        boolean succes = emailService.envoyerMail(notification.getMail(), sujet, message);
        if (succes) {
            emailNotificationDAO.enregistrerEnvoyee(notification.getMail(), sujet, message);
        }
        return succes;
    }
}
