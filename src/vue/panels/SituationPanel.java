package vue.panels;

import modele.SituationPret;
import service.PretService;
import vue.composants.ChampTexteArrondi;
import vue.composants.PanneauArrondi;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Écran "Liste des prêts par situation" (barème : liste des prêts et leur
 * situation de remboursement), branché sur PretService.situationsDesPrets()
 * qui lit la vue SQL v_situation_prets.
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
        champRecherche.addActionListener(e -> rafraichir());

        barre.add(champRecherche, BorderLayout.WEST);
        return barre;
    }

    private JScrollPane construireTableau() {

        modeleTable = new DefaultTableModel(COLONNES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(modeleTable);
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

    private void rafraichir() {

        List<SituationPret> tous = pretService.situationsDesPrets();
        for (SituationPret s : tous) {
    System.out.println("Valeur reçue de la BDD : [" + s.getSituationActuelle() + "]"); // <--- Ajoute ça
    
    String client = (s.getNom() == null ? "" : s.getNom()) + " " + (s.getPrenoms() == null ? "" : s.getPrenoms());
    // ... le reste du code
        }
        String motCle = champRecherche == null ? "" : champRecherche.getText().trim().toLowerCase();

        modeleTable.setRowCount(0);

        for (SituationPret s : tous) {

            String client = (s.getNom() == null ? "" : s.getNom()) + " " + (s.getPrenoms() == null ? "" : s.getPrenoms());

            boolean correspond = motCle.isEmpty()
                    || (s.getNumPret() != null && s.getNumPret().toLowerCase().contains(motCle))
                    || client.toLowerCase().contains(motCle)
                    || (s.getSituationActuelle() != null && s.getSituationActuelle().toLowerCase().contains(motCle));

            if (!correspond) {
                continue;
            }

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

    /** Colore le libellé de situation, comme les badges de la maquette. */
    private static class RenduSituationCellule extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            String texte = value == null ? "" : value.toString();
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setHorizontalAlignment(SwingConstants.CENTER);

            if (texte.toLowerCase().contains("retard")) {
                label.setForeground(new Color(184, 55, 55));
            } else if (texte.toLowerCase().contains("paye") || texte.toLowerCase().contains("payé") || texte.toLowerCase().contains("solde")) {
                label.setForeground(new Color(46, 139, 87));
            } else {
                label.setForeground(new Color(41, 84, 209));
            }

            return label;
        }
    }

}
