package vue.panels;

import dao.EmailNotificationDAO;
import modele.EmailNotification;
import vue.composants.StyleTableau;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * Écran "Notification par email" : affichage de l'historique des emails envoyés.
 */
public class NotificationPanel extends JPanel {

    private static final Color FOND = new Color(245, 247, 252);

    private final EmailNotificationDAO emailNotificationDAO = new EmailNotificationDAO();
    private DefaultTableModel modeleHistorique;
    private JTable tableHistorique;
    private DatePicker champDateDebut;
    private DatePicker champDateFin;

    public NotificationPanel() {
        setBackground(FOND);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(construireHistorique(), BorderLayout.CENTER);

        // Actualisation automatique toutes les minutes
        new Timer(60_000, e -> rafraichirHistorique()).start();
    }

    private JPanel construireHistorique() {
        JPanel historique = new JPanel(new BorderLayout(0, 8));
        historique.setOpaque(false);

        JLabel titre = new JLabel("Historique des emails envoyés");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 16));

        champDateDebut = creerChampDateFiltre();
        champDateFin = creerChampDateFiltre();

        JPanel filtresDate = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtresDate.setOpaque(false);
        filtresDate.add(new JLabel("Du"));
        filtresDate.add(champDateDebut);
        filtresDate.add(new JLabel("au"));
        filtresDate.add(champDateFin);

        JPanel entete = new JPanel(new BorderLayout());
        entete.setOpaque(false);
        entete.add(titre, BorderLayout.WEST);
        entete.add(filtresDate, BorderLayout.EAST);

        String[] colonnes = {"Client", "Sujet", "Date d'envoi", "Statut"};
        modeleHistorique = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableHistorique = new JTable(modeleHistorique);
        StyleTableau.appliquer(tableHistorique);
        tableHistorique.setRowHeight(30);
        tableHistorique.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableHistorique.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHistorique.setAutoCreateRowSorter(true);
        tableHistorique.getColumnModel().getColumn(0).setPreferredWidth(220);
        tableHistorique.getColumnModel().getColumn(1).setPreferredWidth(320);
        tableHistorique.getColumnModel().getColumn(2).setPreferredWidth(150);
        tableHistorique.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane defilement = new JScrollPane(tableHistorique);
        defilement.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 240)));

        historique.add(entete, BorderLayout.NORTH);
        historique.add(defilement, BorderLayout.CENTER);
        
        rafraichirHistorique();
        return historique;
    }

    private DatePicker creerChampDateFiltre() {
        DatePickerSettings parametresDate = new DatePickerSettings();
        parametresDate.setFormatForDatesCommonEra("dd/MM/yyyy");
        DatePicker champ = new DatePicker(parametresDate);
        // Les champs restent vides pour afficher tout l'historique au départ.
        champ.addDateChangeListener(e -> rafraichirHistorique());
        return champ;
    }

    private void rafraichirHistorique() {
        if (modeleHistorique == null) {
            return;
        }
        modeleHistorique.setRowCount(0);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDate dateDebut = champDateDebut == null ? null : champDateDebut.getDate();
        LocalDate dateFin = champDateFin == null ? null : champDateFin.getDate();

        if (dateDebut != null && dateFin != null && dateDebut.isAfter(dateFin)) {
            LocalDate dateTemporaire = dateDebut;
            dateDebut = dateFin;
            dateFin = dateTemporaire;
        }

        List<EmailNotification> historique = emailNotificationDAO.trouverHistorique();
        for (EmailNotification email : historique) {
            LocalDate dateNotification = email.getEnvoyeeLe() != null
                    ? email.getEnvoyeeLe().toLocalDate()
                    : email.getDateEnvoi() == null ? null : email.getDateEnvoi().toLocalDate();
            if (!dansIntervalle(dateNotification, dateDebut, dateFin)) {
                continue;
            }

            String date = email.getEnvoyeeLe() == null ? "-" : email.getEnvoyeeLe().format(format);
            modeleHistorique.addRow(new Object[]{
                    email.getDestinataire(), email.getSujet(), date, "Envoyé"
            });
        }
    }

    private boolean dansIntervalle(LocalDate date, LocalDate debut, LocalDate fin) {
        return date != null
                && (debut == null || !date.isBefore(debut))
                && (fin == null || !date.isAfter(fin));
    }
}