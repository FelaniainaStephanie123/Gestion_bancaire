package vue.panels;

import dao.RenduDAO;
import modele.Rendu;
import service.RenduService;
import vue.composants.BoutonArrondi;
import vue.composants.ChampTexteArrondi;
import vue.composants.PanneauArrondi;
import vue.composants.StyleTableau;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Écran "Gestion des remboursements" : tableau + formulaire d'ajout/modification,
 * branché sur RenduService (le service calcule déjà la situation : payé en
 * totalité ou partiellement).
 */
public class RenduPanel extends JPanel {

    private static final Color FOND = new Color(245, 247, 252);
    private static final String[] COLONNES = {
            "N° Remboursement", "N° Prêt", "Montant payé", "Situation", "Date"
    };

    private final RenduService renduService = new RenduService();
    private final RenduDAO renduDAO = new RenduDAO(); // Ajouté pour générer le prochain N° de remboursement

    private DefaultTableModel modeleTable;
    private JTable table;
    private ChampTexteArrondi champRecherche;
    private DatePicker champDateDebut;
    private DatePicker champDateFin;
    private List<Rendu> renduAffiches;

    public RenduPanel() {

        setBackground(FOND);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(construireBarreOutils(), BorderLayout.NORTH);
        add(construireTableau(), BorderLayout.CENTER);
        add(construireBarreActions(), BorderLayout.SOUTH);

        rafraichir();
    }

    private JPanel construireBarreOutils() {

        JPanel barre = new JPanel(new BorderLayout(15, 0));
        barre.setOpaque(false);

        champRecherche = new ChampTexteArrondi("Rechercher un n° de prêt ou de remboursement...");
        champRecherche.setPreferredSize(new Dimension(320, 40));
        champRecherche.addActionListener(e -> rafraichir());

        champDateDebut = creerChampDateFiltre();
        champDateFin = creerChampDateFiltre();

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        gauche.setOpaque(false);
        gauche.add(champRecherche);
        gauche.add(new JLabel("Du"));
        gauche.add(champDateDebut);
        gauche.add(new JLabel("au"));
        gauche.add(champDateFin);

       BoutonArrondi boutonNouveau = new BoutonArrondi("+ Nouveau remboursement");
boutonNouveau.addActionListener(e -> ouvrirFormulaire(null));

        barre.add(gauche, BorderLayout.WEST);
        barre.add(boutonNouveau, BorderLayout.EAST);

        return barre;
    }

    private DatePicker creerChampDateFiltre() {
        DatePickerSettings parametresDate = new DatePickerSettings();
        parametresDate.setFormatForDatesCommonEra("dd/MM/yyyy");
        DatePicker champ = new DatePicker(parametresDate);
        champ.setDate(LocalDate.now());
        champ.addDateChangeListener(e -> rafraichir());
        return champ;
    }

    private JScrollPane construireTableau() {

        modeleTable = new DefaultTableModel(COLONNES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
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

    private JPanel construireBarreActions() {

        JPanel barre = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        barre.setOpaque(false);

        BoutonArrondi boutonModifier = new BoutonArrondi("", BoutonArrondi.Style.CONTOUR);
        boutonModifier.definirIcone("edit", "Modifier");
        boutonModifier.setPreferredSize(new Dimension(42, 36));
        boutonModifier.addActionListener(e -> {
            Rendu selectionne = renduSelectionne();
            if (selectionne != null) {
                ouvrirFormulaire(selectionne);
            } else {
                JOptionPane.showMessageDialog(this, "Sélectionnez un remboursement dans le tableau.");
            }
        });

        BoutonArrondi boutonSupprimer = BoutonArrondi.boutonDanger("");
        boutonSupprimer.definirIcone("delete", "Supprimer");
        boutonSupprimer.setPreferredSize(new Dimension(42, 36));
        boutonSupprimer.addActionListener(e -> {
            Rendu selectionne = renduSelectionne();
            if (selectionne == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un remboursement dans le tableau.");
                return;
            }
            int confirmation = JOptionPane.showConfirmDialog(
                    this,
                    "Supprimer le remboursement " + selectionne.getNumRendu() + " ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirmation == JOptionPane.YES_OPTION) {
                renduService.supprimerRemboursement(selectionne.getNumRendu());
                rafraichir();
            }
        });

        barre.add(boutonModifier);
        barre.add(boutonSupprimer);
        return barre;
    }

    private Rendu renduSelectionne() {
        int ligne = table.getSelectedRow();
        if (ligne < 0 || renduAffiches == null || ligne >= renduAffiches.size()) {
            return null;
        }
        return renduAffiches.get(ligne);
    }

    public void rafraichir() {

        List<Rendu> tous = renduService.tousLesRemboursements();
        String motCle = champRecherche == null ? "" : champRecherche.getText().trim().toLowerCase();
        LocalDate dateDebut = champDateDebut == null ? null : champDateDebut.getDate();
        LocalDate dateFin = champDateFin == null ? null : champDateFin.getDate();

        renduAffiches = tous.stream()
            .filter(r -> {
                boolean matchTexte = motCle.isEmpty()
                    || (r.getNumRendu() != null && r.getNumRendu().toLowerCase().contains(motCle))
                    || (r.getNumPret() != null && r.getNumPret().toLowerCase().contains(motCle));
                boolean matchDate = r.getDateRendu() != null
                    && (dateDebut == null || !r.getDateRendu().isBefore(dateDebut))
                    && (dateFin == null || !r.getDateRendu().isAfter(dateFin));
                return matchTexte && matchDate;
            })
                .toList();

        modeleTable.setRowCount(0);
        for (Rendu r : renduAffiches) {
            modeleTable.addRow(new Object[]{
                    r.getNumRendu(),
                    r.getNumPret(),
                    r.getMontantPaye(),
                    libelleSituation(r.getSituation()),
                    r.getDateRendu()
            });
        }
    }

    private String libelleSituation(String situation) {
        if (Rendu.SITUATION_TOUT_PAYE.equals(situation)) {
            return "Payé en totalité";
        }
        if (Rendu.SITUATION_PAYE_UNE_PART.equals(situation)) {
            return "Payé partiellement";
        }
        return situation == null ? "" : situation;
    }

    private void ouvrirFormulaire(Rendu renduExistant) {

        boolean modification = renduExistant != null;

        JDialog dialogue = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                modification ? "Modifier le remboursement" : "Nouveau remboursement",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialogue.setSize(420, 400);
        dialogue.setLocationRelativeTo(this);
        dialogue.getContentPane().setBackground(Color.WHITE);

        JPanel contenu = new JPanel();
        contenu.setBackground(Color.WHITE);
        contenu.setLayout(new BoxLayout(contenu, BoxLayout.Y_AXIS));
        contenu.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        ChampTexteArrondi champNumRendu = new ChampTexteArrondi("Ex: R010");
        ChampTexteArrondi champNumPret = new ChampTexteArrondi("");
        ChampTexteArrondi champMontant = new ChampTexteArrondi("Montant versé en Ar");
        ChampTexteArrondi champDate = new ChampTexteArrondi("AAAA-MM-JJ");

        if (modification) {
            champNumRendu.setText(renduExistant.getNumRendu());
            champNumRendu.setEditable(false);
            champNumPret.setText(renduExistant.getNumPret());
            champMontant.setText(String.valueOf(renduExistant.getMontantPaye()));
            champDate.setText(String.valueOf(renduExistant.getDateRendu()));
        } else {
            // Génération automatique du numéro de remboursement (ex: R01, R02...)
            champNumRendu.setText(renduDAO.genererProchainNumRendu());
            champNumRendu.setEditable(false);
            champDate.setText(String.valueOf(LocalDate.now()));
        }

        contenu.add(champLabelise("N° du remboursement", champNumRendu));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("N° du prêt", champNumPret));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Montant versé (Ar)", champMontant));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Date du versement", champDate));
        contenu.add(Box.createVerticalStrut(24));

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        boutons.setOpaque(false);
        boutons.setAlignmentX(Component.LEFT_ALIGNMENT);

        BoutonArrondi boutonAnnuler = new BoutonArrondi("Annuler", BoutonArrondi.Style.CONTOUR);
        boutonAnnuler.addActionListener(e -> dialogue.dispose());

        BoutonArrondi boutonEnregistrer = new BoutonArrondi(modification ? "Enregistrer" : "Ajouter");
        boutonEnregistrer.addActionListener(e -> {
            try {
                Rendu rendu = new Rendu();
                rendu.setNumRendu(champNumRendu.getText().trim());
                rendu.setNumPret(champNumPret.getText().trim());
                rendu.setMontantPaye(new BigDecimal(champMontant.getText().trim()));
                rendu.setDateRendu(LocalDate.parse(champDate.getText().trim()));

                boolean succes = modification
                        ? renduService.modifierRemboursement(rendu)
                        : renduService.ajouterRemboursement(rendu);

                if (succes) {
                    dialogue.dispose();
                    rafraichir();
                } else {
                    JOptionPane.showMessageDialog(dialogue,
                            "Impossible d'enregistrer le remboursement (prêt introuvable, montant "
                                    + "invalide ou dépassant le reste à payer).",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialogue,
                        "Montant invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialogue,
                        "Date invalide, format attendu AAAA-MM-JJ.", "Erreur", JOptionPane.ERROR_MESSAGE);
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
        conteneur.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(libelle);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(90, 96, 110));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        champ.setAlignmentX(Component.LEFT_ALIGNMENT);
        champ.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        champ.setPreferredSize(new Dimension(340, 40));

        conteneur.add(label);
        conteneur.add(Box.createVerticalStrut(4));
        conteneur.add(champ);

        return conteneur;
    }
}