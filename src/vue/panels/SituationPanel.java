package vue.panels;

import modele.SituationPret;
import service.PretService;
import vue.composants.ChampTexteArrondi;
import vue.composants.PanneauArrondi;
import vue.composants.StyleTableau;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;

/**
 * Écran "Liste des prêts par situation"
 */
public class SituationPanel extends JPanel {

    private static final Color FOND = new Color(245, 247, 252);
    private static final String[] COLONNES = {
            "N° Prêt", "Client", "Montant prêté", "Montant à rendre",
            "Total payé", "Reste à payer", "Échéance", "Situation"
    };

    private final PretService pretService = new PretService();

    private DefaultTableModel modeleTable;
    private JTable table;
    private ChampTexteArrondi champRecherche;
    private JComboBox<String> filtreSituation;
    private DatePicker champDateDebut;
    private DatePicker champDateFin;

    private static final String TOUTES_SITUATIONS = "Toutes";

    public SituationPanel() {
        setBackground(FOND);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(construireBarreOutils(), BorderLayout.NORTH);
        add(construireTableau(), BorderLayout.CENTER);

        rafraichir();
    }

    private JPanel construireBarreOutils() {
        JPanel barre = new JPanel(new BorderLayout(15, 0));
        barre.setOpaque(false);

        champRecherche = new ChampTexteArrondi("Filtrer par n° de prêt, client ou situation...");
        champRecherche.setPreferredSize(new Dimension(360, 40));
        
        champRecherche.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { rafraichir(); }
            @Override public void removeUpdate(DocumentEvent e) { rafraichir(); }
            @Override public void changedUpdate(DocumentEvent e) { rafraichir(); }
        });

        champDateDebut = creerChampDateFiltre();
        champDateFin = creerChampDateFiltre();
        filtreSituation = new JComboBox<>(new String[]{
            TOUTES_SITUATIONS,
            "Non remboursé",
            "Payé une part",
            "Tout payé"
        });
        filtreSituation.setPreferredSize(new Dimension(150, 40));
        filtreSituation.addActionListener(e -> rafraichir());

        JPanel filtres = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtres.setOpaque(false);
        filtres.add(champRecherche);
        filtres.add(new JLabel("Situation"));
        filtres.add(filtreSituation);
        filtres.add(new JLabel("Du"));
        filtres.add(champDateDebut);
        filtres.add(new JLabel("au"));
        filtres.add(champDateFin);

        barre.add(filtres, BorderLayout.WEST);
        return barre;
    }

    private DatePicker creerChampDateFiltre() {
        DatePickerSettings parametresDate = new DatePickerSettings();
        parametresDate.setFormatForDatesCommonEra("dd/MM/yyyy");
        DatePicker champ = new DatePicker(parametresDate);
        
        // Rafraîchit dès qu'une date est choisie ou supprimée
        champ.addDateChangeListener(e -> rafraichir());
        return champ;
    }

    private JScrollPane construireTableau() {
        modeleTable = new DefaultTableModel(COLONNES, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(modeleTable);
        StyleTableau.appliquer(table);
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(235, 239, 249));
        table.setSelectionBackground(new Color(223, 233, 255));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(235, 237, 242));

        table.getColumnModel().getColumn(COLONNES.length - 1).setCellRenderer(new RenduSituationCellule());

        PanneauArrondi panneau = new PanneauArrondi();
        panneau.setLayout(new BorderLayout());
        panneau.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane defilement = new JScrollPane(table);
        defilement.setBorder(BorderFactory.createEmptyBorder());
        panneau.add(defilement, BorderLayout.CENTER);

        JScrollPane conteneur = new JScrollPane(panneau);
        conteneur.setBorder(BorderFactory.createEmptyBorder());
        conteneur.getVerticalScrollBar().setUnitIncrement(16);
        return conteneur;
    }

    public void rafraichir() {
        List<SituationPret> tous = pretService.situationsDesPrets();
        String motCle = champRecherche == null ? "" : champRecherche.getText().trim().toLowerCase();
        String situationChoisie = filtreSituation == null
            ? TOUTES_SITUATIONS : (String) filtreSituation.getSelectedItem();
        LocalDate dateDebut = champDateDebut == null ? null : champDateDebut.getDate();
        LocalDate dateFin = champDateFin == null ? null : champDateFin.getDate();

        if (dateDebut != null && dateFin != null && dateDebut.isAfter(dateFin)) {
            LocalDate dateTemporaire = dateDebut;
            dateDebut = dateFin;
            dateFin = dateTemporaire;
        }

        modeleTable.setRowCount(0);

        for (SituationPret s : tous) {
            String client = (s.getNom() == null ? "" : s.getNom()) + " " + (s.getPrenoms() == null ? "" : s.getPrenoms());

            // 1. Filtrage texte
            boolean matchTexte = motCle.isEmpty()
                    || (s.getNumPret() != null && s.getNumPret().toLowerCase().contains(motCle))
                    || client.toLowerCase().contains(motCle)
                    || (s.getSituationActuelle() != null && s.getSituationActuelle().toLowerCase().contains(motCle));

                boolean matchSituation = TOUTES_SITUATIONS.equals(situationChoisie)
                    || normaliser(situationChoisie).equals(normaliser(s.getSituationActuelle()));

            boolean matchDate = dansIntervalle(s.getDatePret(), dateDebut, dateFin)
                    || dansIntervalle(s.getDateEcheance(), dateDebut, dateFin);

                if (matchTexte && matchSituation && matchDate) {
                modeleTable.addRow(new Object[]{
                        s.getNumPret(),
                        client.trim(),
                        s.getMontantPrete(),
                        s.getMontantARendre(),
                        s.getTotalPaye(),
                        s.getResteAPayer(),
                        s.getDateEcheance(),
                        s.getSituationActuelle()
                });
            }
        }
    }

    private boolean dansIntervalle(LocalDate date, LocalDate debut, LocalDate fin) {
        return date != null
                && (debut == null || !date.isBefore(debut))
                && (fin == null || !date.isAfter(fin));
    }

    private String normaliser(String texte) {
        if (texte == null) {
            return "";
        }
        return Normalizer.normalize(texte, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase();
    }

    private static class RenduSituationCellule extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String texte = value == null ? "" : value.toString().toLowerCase();
            
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setHorizontalAlignment(SwingConstants.CENTER);

            if (texte.contains("retard")) label.setForeground(new Color(184, 55, 55));
            else if (texte.contains("paye") || texte.contains("solde")) label.setForeground(new Color(46, 139, 87));
            else label.setForeground(new Color(41, 84, 209));

            return label;
        }
    }
}