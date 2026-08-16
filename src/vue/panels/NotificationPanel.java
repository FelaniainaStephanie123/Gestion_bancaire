package vue.panels;

import service.NotificationService;
import vue.composants.BoutonArrondi;
import vue.composants.ChampTexteArrondi;
import vue.composants.PanneauArrondi;

import javax.swing.*;
import java.awt.*;

/**
 * Écran "Notification par email" : on saisit un n° de prêt, on prévisualise
 * le message de rappel puis on l'envoie au client concerné (NotificationService
 * + EmailService, déjà fonctionnels côté backend).
 */
public class NotificationPanel extends JPanel {

    private static final Color FOND = new Color(245, 247, 252);

    private final NotificationService notificationService = new NotificationService();

    private ChampTexteArrondi champNumPret;
    private JTextArea zoneApercu;
    private JLabel statut;

    public NotificationPanel() {

        setBackground(FOND);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        PanneauArrondi panneau = new PanneauArrondi();
        panneau.setLayout(new BorderLayout(0, 20));
        panneau.setBorder(BorderFactory.createEmptyBorder(26, 30, 26, 30));

        panneau.add(construireFormulaireRecherche(), BorderLayout.NORTH);
        panneau.add(construireApercu(), BorderLayout.CENTER);
        panneau.add(construireBarreActions(), BorderLayout.SOUTH);

        add(panneau, BorderLayout.CENTER);
    }

    private JPanel construireFormulaireRecherche() {

        JPanel ligne = new JPanel(new BorderLayout(15, 0));
        ligne.setOpaque(false);

        JLabel label = new JLabel("N° de prêt :");
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));

        champNumPret = new ChampTexteArrondi("Ex: P010");
        champNumPret.setPreferredSize(new Dimension(0, 40));

        BoutonArrondi boutonApercu = new BoutonArrondi("Générer l'aperçu", BoutonArrondi.Style.CONTOUR);
        boutonApercu.addActionListener(e -> genererApercu());

        JPanel milieu = new JPanel(new BorderLayout(15, 0));
        milieu.setOpaque(false);
        milieu.add(champNumPret, BorderLayout.CENTER);
        milieu.add(boutonApercu, BorderLayout.EAST);

        ligne.add(label, BorderLayout.WEST);
        ligne.add(milieu, BorderLayout.CENTER);

        return ligne;
    }

    private JScrollPane construireApercu() {

        zoneApercu = new JTextArea();
        zoneApercu.setEditable(false);
        zoneApercu.setLineWrap(true);
        zoneApercu.setWrapStyleWord(true);
        zoneApercu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        zoneApercu.setBackground(new Color(249, 250, 253));
        zoneApercu.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        zoneApercu.setText("Saisissez un n° de prêt puis cliquez sur \"Générer l'aperçu\" pour voir le message qui sera envoyé au client.");

        JScrollPane defilement = new JScrollPane(zoneApercu);
        defilement.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 240)));
        return defilement;
    }

    private JPanel construireBarreActions() {

        JPanel barre = new JPanel(new BorderLayout());
        barre.setOpaque(false);

        statut = new JLabel(" ");
        statut.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        BoutonArrondi boutonEnvoyer = new BoutonArrondi("Envoyer par email");
        boutonEnvoyer.addActionListener(e -> envoyerNotification());

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        droite.setOpaque(false);
        droite.add(boutonEnvoyer);

        barre.add(statut, BorderLayout.WEST);
        barre.add(droite, BorderLayout.EAST);

        return barre;
    }

    private void genererApercu() {

        String numPret = champNumPret.getText().trim();

        if (numPret.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Saisissez un numéro de prêt.");
            return;
        }

        String message = notificationService.genererNotification(numPret);
        zoneApercu.setText(message);
        statut.setText(" ");
    }

    private void envoyerNotification() {

        String numPret = champNumPret.getText().trim();

        if (numPret.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Saisissez un numéro de prêt.");
            return;
        }

        statut.setText("Envoi en cours...");
        statut.setForeground(new Color(90, 96, 110));

        // Envoi sur un thread séparé pour ne pas geler l'interface pendant la connexion SMTP.
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return notificationService.envoyerNotification(numPret);
            }

            @Override
            protected void done() {
                try {
                    boolean succes = get();
                    if (succes) {
                        statut.setText("Email envoyé avec succès.");
                        statut.setForeground(new Color(46, 139, 87));
                    } else {
                        statut.setText("Échec de l'envoi (prêt introuvable ou erreur SMTP).");
                        statut.setForeground(new Color(184, 55, 55));
                    }
                } catch (Exception ex) {
                    statut.setText("Erreur inattendue lors de l'envoi.");
                    statut.setForeground(new Color(184, 55, 55));
                }
            }
        }.execute();
    }

}
