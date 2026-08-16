package vue.panels;

import modele.Rendu;
import service.RenduService;
import vue.composants.BoutonArrondi;
import vue.composants.ChampTexteArrondi;
import vue.composants.PanneauArrondi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class RenduPanel extends JPanel {

    private static final Color FOND = new Color(245, 247, 252);
    private static final String[] COLONNES = {
            "N° Remb.", "N° Prêt", "Situation", "Montant payé", "Date"
    };

    private final RenduService renduService = new RenduService();
    private DefaultTableModel modeleTable;
    private JTable table;
    private ChampTexteArrondi champRecherche;

    public RenduPanel() {
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

        champRecherche = new ChampTexteArrondi("Rechercher par n° de prêt ou n° de remboursement...");
        champRecherche.setPreferredSize(new Dimension(360, 40));
        champRecherche.addActionListener(e -> rafraichir());

        BoutonArrondi boutonNouveau = new BoutonArrondi("+ Nouveau remboursement");
        boutonNouveau.addActionListener(e -> ouvrirFormulaire(null));

        JPanel gauche = new JPanel(new BorderLayout());
        gauche.setOpaque(false);
        gauche.add(champRecherche, BorderLayout.CENTER);

        barre.add(gauche, BorderLayout.WEST);
        barre.add(boutonNouveau, BorderLayout.EAST);
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
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(235, 239, 249));
        table.setSelectionBackground(new Color(223, 233, 255));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(235, 237, 242));

        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);

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
        List<Rendu> tous = renduService.tousLesRemboursements();
        String motCle = champRecherche == null ? "" : champRecherche.getText().trim().toLowerCase();

        modeleTable.setRowCount(0);
        for (Rendu r : tous) {
            String numRendu = r.getNumRendu() == null ? "" : r.getNumRendu();
            String numPret = r.getNumPret() == null ? "" : r.getNumPret();
            String situation = r.getSituation() == null ? "" : r.getSituation();

            boolean correspond = motCle.isEmpty()
                    || numRendu.toLowerCase().contains(motCle)
                    || numPret.toLowerCase().contains(motCle)
                    || situation.toLowerCase().contains(motCle);

            if (correspond) {
                modeleTable.addRow(new Object[] {
                        numRendu,
                        numPret,
                        situation,
                        r.getMontantPaye(),
                        r.getDateRendu()
                });
            }
        }
    }

    private void ouvrirFormulaire(Rendu remboursementExistant) {
        boolean modification = remboursementExistant != null;

        JDialog dialogue = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                modification ? "Modifier le remboursement" : "Nouveau remboursement",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialogue.setSize(420, 360);
        dialogue.setLocationRelativeTo(this);
        dialogue.getContentPane().setBackground(Color.WHITE);

        JPanel contenu = new JPanel();
        contenu.setBackground(Color.WHITE);
        contenu.setLayout(new BoxLayout(contenu, BoxLayout.Y_AXIS));
        contenu.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        ChampTexteArrondi champNumPret = new ChampTexteArrondi("Ex: P010");
        ChampTexteArrondi champMontant = new ChampTexteArrondi("Montant payé (Ar)");
        ChampTexteArrondi champDate = new ChampTexteArrondi("AAAA-MM-JJ");

        if (modification) {
            champNumPret.setText(remboursementExistant.getNumPret());
            champMontant.setText(String.valueOf(remboursementExistant.getMontantPaye()));
            champDate.setText(String.valueOf(remboursementExistant.getDateRendu()));
        }

        contenu.add(champLabelise("N° du prêt", champNumPret));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Montant payé (Ar)", champMontant));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Date du remboursement", champDate));
        contenu.add(Box.createVerticalStrut(24));

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        boutons.setOpaque(false);
        boutons.setAlignmentX(Component.LEFT_ALIGNMENT);

        BoutonArrondi boutonAnnuler = new BoutonArrondi("Annuler", BoutonArrondi.Style.CONTOUR);
        boutonAnnuler.addActionListener(e -> dialogue.dispose());

        BoutonArrondi boutonEnregistrer = new BoutonArrondi(modification ? "Enregistrer" : "Ajouter");
        boutonEnregistrer.addActionListener(e -> {
            try {
                String valeurDate = champDate.getText().trim();
                LocalDate dateRendu = parseDate(valeurDate);

                Rendu rendu = new Rendu();
                rendu.setNumPret(champNumPret.getText().trim());
                rendu.setMontantPaye(new BigDecimal(champMontant.getText().trim()));
                rendu.setDateRendu(dateRendu);

                boolean succes = renduService.ajouterRemboursement(rendu);
                if (succes) {
                    dialogue.dispose();
                    rafraichir();
                } else {
                    JOptionPane.showMessageDialog(dialogue,
                            "Impossible d'enregistrer le remboursement.",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialogue,
                        "Montant invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialogue,
                        "Date invalide, format attendu : yyyy-MM-dd.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialogue,
                        ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
            }
        });

        boutons.add(boutonAnnuler);
        boutons.add(boutonEnregistrer);
        contenu.add(boutons);

        dialogue.add(contenu);
        dialogue.setVisible(true);
    }

    private JPanel champLabelise(String libelle, JComponent champ) {
        JPanel conteneur = new JPanel();
        conteneur.setOpaque(false);
        conteneur.setLayout(new BoxLayout(conteneur, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(libelle);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(31, 41, 55));

        conteneur.add(label);
        conteneur.add(Box.createVerticalStrut(6));
        conteneur.add(champ);
        return conteneur;
    }

    private LocalDate parseDate(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            throw new IllegalArgumentException("Date de remboursement manquante.");
        }
        return LocalDate.parse(texte.trim());
    }
}
