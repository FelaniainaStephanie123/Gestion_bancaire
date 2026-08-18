package vue.panels;

import dao.PretDAO;
import modele.Pret;
import service.PretService;
import vue.composants.BoutonArrondi;
import vue.composants.ChampTexteArrondi;
import vue.composants.PanneauArrondi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Écran "Gestion des prêts" : recherche, tableau et formulaire d'ajout/modification,
 * branché sur PretService (backend déjà fonctionnel).
 */
public class PretPanel extends JPanel {

    private static final Color FOND = new Color(245, 247, 252);

    private static final String[] COLONNES = {
            "N° Prêt", "N° Compte", "Montant prêté", "Taux (%)",
            "Montant à rendre", "Date prêt", "Échéance", "Actions"
    };

    private static final int INDEX_COLONNE_ACTIONS = COLONNES.length - 1;

    private final PretService pretService = new PretService();
    private final PretDAO pretDAO = new PretDAO(); // Ajout pour récupérer le prochain ID

    private DefaultTableModel modeleTable;
    private JTable table;
    private ChampTexteArrondi champRecherche;
    private List<Pret> pretsAffiches;

    public PretPanel() {
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

    champRecherche = new ChampTexteArrondi("Rechercher un n° de prêt ou de compte...");
    champRecherche.setPreferredSize(new Dimension(320, 40));

    // Ajout du DocumentListener pour déclencher rafraichir() à chaque frappe
    champRecherche.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) { rafraichir(); }
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) { rafraichir(); }
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) { rafraichir(); }
    });

    JPanel gauche = new JPanel(new BorderLayout());
    gauche.setOpaque(false);
    gauche.add(champRecherche, BorderLayout.CENTER);

    BoutonArrondi boutonNouveau = new BoutonArrondi("+ Nouveau prêt");
    boutonNouveau.addActionListener(e -> ouvrirFormulaire(null));

    barre.add(gauche, BorderLayout.WEST);
    barre.add(boutonNouveau, BorderLayout.EAST);

    return barre;
}

    private JScrollPane construireTableau() {
        modeleTable = new DefaultTableModel(COLONNES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == INDEX_COLONNE_ACTIONS;
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

        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);

        table.getColumnModel().getColumn(INDEX_COLONNE_ACTIONS).setPreferredWidth(230);
        table.getColumnModel().getColumn(INDEX_COLONNE_ACTIONS).setMinWidth(230);

        table.getColumnModel().getColumn(INDEX_COLONNE_ACTIONS).setCellRenderer(new ActionsCellRenderer());
        table.getColumnModel().getColumn(INDEX_COLONNE_ACTIONS).setCellEditor(new ActionsCellEditor());

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
        List<Pret> tous = pretService.tousLesPrets();
        String motCle = champRecherche == null ? "" : champRecherche.getText().trim().toLowerCase();

        pretsAffiches = tous.stream()
                .filter(p -> motCle.isEmpty()
                        || (p.getNumPret() != null && p.getNumPret().toLowerCase().contains(motCle))
                        || (p.getNumCompte() != null && p.getNumCompte().toLowerCase().contains(motCle)))
                .toList();

        modeleTable.setRowCount(0);
        for (Pret p : pretsAffiches) {
            modeleTable.addRow(new Object[]{
                    p.getNumPret(),
                    p.getNumCompte(),
                    p.getMontantPrete(),
                    p.getTauxInteret(),
                    p.getMontantARendre(),
                    p.getDatePret(),
                    p.getDateEcheance(),
                    ""
            });
        }
    }

    /** Ouvre le formulaire d'ajout (pretExistant == null) ou de modification. */
    private void ouvrirFormulaire(Pret pretExistant) {
        boolean modification = pretExistant != null;

        JDialog dialogue = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                modification ? "Modifier le prêt" : "Nouveau prêt",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialogue.setSize(420, 480);
        dialogue.setLocationRelativeTo(this);
        dialogue.getContentPane().setBackground(Color.WHITE);

        JPanel contenu = new JPanel();
        contenu.setBackground(Color.WHITE);
        contenu.setLayout(new BoxLayout(contenu, BoxLayout.Y_AXIS));
        contenu.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        ChampTexteArrondi champNumPret = new ChampTexteArrondi("Ex: P010");
        ChampTexteArrondi champNumCompte = new ChampTexteArrondi("Numéro de compte client");
        ChampTexteArrondi champMontant = new ChampTexteArrondi("Montant en Ar");
        ChampTexteArrondi champTaux = new ChampTexteArrondi("Ex: 10.00");

        LocalDate dateDuJour;
        LocalDate dateEcheanceCalculee;

        if (modification) {
            dateDuJour = pretExistant.getDatePret();
            dateEcheanceCalculee = pretExistant.getDateEcheance();
        } else {
            dateDuJour = LocalDate.now();
            dateEcheanceCalculee = dateDuJour.plusMonths(1);
        }

        ChampTexteArrondi champDatePret = new ChampTexteArrondi("");
        champDatePret.setText(dateDuJour.toString());
        champDatePret.setEnabled(false);

        ChampTexteArrondi champEcheance = new ChampTexteArrondi("");
        champEcheance.setText(dateEcheanceCalculee.toString());
        champEcheance.setEnabled(false);

        if (modification) {
            champNumPret.setText(pretExistant.getNumPret());
            champNumPret.setEditable(false);
            champNumCompte.setText(pretExistant.getNumCompte());
            champMontant.setText(String.valueOf(pretExistant.getMontantPrete()));
            champTaux.setText(String.valueOf(pretExistant.getTauxInteret()));
        } else {
            // Génération automatique du numéro de prêt (ex: P01, P02...)
            champNumPret.setText(pretDAO.genererProchainNumPret());
            champNumPret.setEditable(false); // On bloque l'édition pour garder l'auto-incrémentation propre
            champTaux.setText("10.00");
        }

        contenu.add(champLabelise("N° du prêt", champNumPret));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("N° de compte", champNumCompte));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Montant prêté (Ar)", champMontant));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Taux d'intérêt (%)", champTaux));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Date du prêt", champDatePret));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Date d'échéance", champEcheance));
        contenu.add(Box.createVerticalStrut(24));

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        boutons.setOpaque(false);
        boutons.setAlignmentX(Component.LEFT_ALIGNMENT);

        BoutonArrondi boutonAnnuler = new BoutonArrondi("Annuler", BoutonArrondi.Style.CONTOUR);
        boutonAnnuler.addActionListener(e -> dialogue.dispose());

        BoutonArrondi boutonEnregistrer = new BoutonArrondi(modification ? "Enregistrer" : "Ajouter");
        boutonEnregistrer.addActionListener(e -> {
            try {
                Pret pret = new Pret();
                pret.setNumPret(champNumPret.getText().trim());
                pret.setNumCompte(champNumCompte.getText().trim());
                pret.setMontantPrete(new BigDecimal(champMontant.getText().trim()));
                pret.setTauxInteret(new BigDecimal(champTaux.getText().trim()));
                pret.setDatePret(dateDuJour);
                pret.setDateEcheance(dateEcheanceCalculee);

                boolean succes = modification
                        ? pretService.modifierPret(pret)
                        : pretService.creerPret(pret);

                if (succes) {
                    dialogue.dispose();
                    rafraichir();
                } else {
                    JOptionPane.showMessageDialog(dialogue,
                            "Impossible d'accorder ce prêt : le client possède déjà un prêt en cours non soldé.",
                            "Prêt refusé", JOptionPane.WARNING_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialogue,
                        "Montant ou taux invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        boutons.add(boutonAnnuler);
        boutons.add(boutonEnregistrer);
        contenu.add(boutons);

        dialogue.add(contenu);
        dialogue.setVisible(true);
    }

   private JPanel champLabelise(String libelle, JComponent champ) {
        JPanel panneauChamp = new JPanel();
        panneauChamp.setOpaque(false);
        panneauChamp.setLayout(new BoxLayout(panneauChamp, BoxLayout.Y_AXIS));
        panneauChamp.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(libelle);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(90, 96, 110));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        champ.setAlignmentX(Component.LEFT_ALIGNMENT);
        champ.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        champ.setPreferredSize(new Dimension(340, 40));

        panneauChamp.add(label);
        panneauChamp.add(Box.createVerticalStrut(4));
        panneauChamp.add(champ);

        return panneauChamp;
    }

    private class ActionsCellRenderer extends JPanel implements TableCellRenderer {
        private final BoutonArrondi boutonModifier;
        private final BoutonArrondi boutonSupprimer;

        public ActionsCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
            setOpaque(false);

            boutonModifier = new BoutonArrondi("Modifier", BoutonArrondi.Style.PLEIN,
                    new Color(41, 84, 209), new Color(33, 68, 173), Color.WHITE);
            boutonModifier.setPreferredSize(new Dimension(90, 30));

            boutonSupprimer = new BoutonArrondi("Supprimer", BoutonArrondi.Style.PLEIN,
                    new Color(214, 69, 69), new Color(184, 55, 55), Color.WHITE);
            boutonSupprimer.setPreferredSize(new Dimension(90, 30));

            add(boutonModifier);
            add(boutonSupprimer);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class ActionsCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panneau;
        private final JButton boutonModifier;
        private final JButton boutonSupprimer;
        private int ligneCourante;

        public ActionsCellEditor() {
            panneau = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
            panneau.setOpaque(false);

            boutonModifier = new BoutonArrondi("Modifier", BoutonArrondi.Style.PLEIN,
                    new Color(41, 84, 209), new Color(33, 68, 173), Color.WHITE);
            boutonModifier.setPreferredSize(new Dimension(90, 30));
            boutonModifier.addActionListener(e -> {
                fireEditingStopped();
                Pret selectionne = pretsAffiches.get(ligneCourante);
                ouvrirFormulaire(selectionne);
            });

            boutonSupprimer = new BoutonArrondi("Supprimer", BoutonArrondi.Style.PLEIN,
                    new Color(214, 69, 69), new Color(184, 55, 55), Color.WHITE);
            boutonSupprimer.setPreferredSize(new Dimension(90, 30));
            boutonSupprimer.addActionListener(e -> {
                fireEditingStopped();
                Pret selectionne = pretsAffiches.get(ligneCourante);
                int confirmation = JOptionPane.showConfirmDialog(
                        PretPanel.this,
                        "Supprimer le prêt " + selectionne.getNumPret() + " ?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirmation == JOptionPane.YES_OPTION) {
                    pretService.supprimerPret(selectionne.getNumPret());
                    rafraichir();
                }
            });

            panneau.add(boutonModifier);
            panneau.add(boutonSupprimer);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            ligneCourante = row;
            return panneau;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}