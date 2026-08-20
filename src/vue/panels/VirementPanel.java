package vue.panels;

import modele.Client;
import modele.Virement;
import pdf.AvisVirementPDF;
import service.VirementService;
import vue.composants.BoutonArrondi;
import vue.composants.ChampTexteArrondi;
import vue.composants.PanneauArrondi;
import vue.composants.StyleTableau;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Écran "Gestion des virements" : formulaire pour effectuer un virement
 * (le solde envoyeur/bénéficiaire est mis à jour par les triggers PostgreSQL),
 * tableau historique, recherche LIKE, et génération de l'avis de virement en PDF.
 */
public class VirementPanel extends JPanel {

    private static final Color FOND = new Color(245, 247, 252);
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] COLONNES = {
            "N° Virement", "Compte envoyeur", "Compte bénéficiaire", "Montant (Ar)", "Date"
    };

    private final VirementService virementService = new VirementService();

    private DefaultTableModel modeleTable;
    private JTable table;
    private ChampTexteArrondi champRecherche;
    private DatePicker dateDebut;
    private DatePicker dateFin;
    private List<Virement> virementsAffiches;

    public VirementPanel() {

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

        champRecherche = new ChampTexteArrondi("Rechercher par n° de virement ou compte...");
        champRecherche.setPreferredSize(new Dimension(320, 40));
        champRecherche.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { rafraichir(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { rafraichir(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { rafraichir(); }
        });

        DatePickerSettings parametresDateDebut = new DatePickerSettings();
        parametresDateDebut.setFormatForDatesCommonEra("dd/MM/yyyy");
        DatePickerSettings parametresDateFin = new DatePickerSettings();
        parametresDateFin.setFormatForDatesCommonEra("dd/MM/yyyy");
        dateDebut = new DatePicker(parametresDateDebut);
        dateFin = new DatePicker(parametresDateFin);
        LocalDate aujourdHui = LocalDate.now();
        dateDebut.setDate(aujourdHui);
        dateFin.setDate(aujourdHui);
        dateDebut.addDateChangeListener(e -> rafraichir());
        dateFin.addDateChangeListener(e -> rafraichir());

        JPanel filtres = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtres.setOpaque(false);
        filtres.add(champRecherche);
        filtres.add(new JLabel("Du"));
        filtres.add(dateDebut);
        filtres.add(new JLabel("au"));
        filtres.add(dateFin);

        BoutonArrondi boutonNouveau = new BoutonArrondi("+ Effectuer un virement");
        boutonNouveau.addActionListener(e -> ouvrirFormulaireVirement());

        barre.add(filtres, BorderLayout.CENTER);
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

        BoutonArrondi boutonAvis = new BoutonArrondi("Générer l'avis PDF", BoutonArrondi.Style.CONTOUR);
        boutonAvis.addActionListener(e -> {
            Virement selectionne = virementSelectionne();
            if (selectionne == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un virement dans le tableau.");
                return;
            }
            genererAvisPdf(selectionne);
        });

        BoutonArrondi boutonModifierDate = new BoutonArrondi("", BoutonArrondi.Style.CONTOUR);
        boutonModifierDate.definirIcone("edit", "Corriger la date");
        boutonModifierDate.setPreferredSize(new Dimension(42, 36));
        boutonModifierDate.addActionListener(e -> {
            Virement selectionne = virementSelectionne();
            if (selectionne == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un virement dans le tableau.");
                return;
            }
            ouvrirFormulaireCorrection(selectionne);
        });

        BoutonArrondi boutonSupprimer = BoutonArrondi.boutonDanger("");
        boutonSupprimer.definirIcone("delete", "Supprimer");
        boutonSupprimer.setPreferredSize(new Dimension(42, 36));
        boutonSupprimer.addActionListener(e -> {
            Virement selectionne = virementSelectionne();
            if (selectionne == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un virement dans le tableau.");
                return;
            }
            int confirmation = JOptionPane.showConfirmDialog(
                    this,
                    "Supprimer le virement " + selectionne.getNumVirement() + " ?\n"
                            + "Attention : les soldes des 2 comptes ne seront PAS recrédités/redébités automatiquement,\n"
                            + "seul l'enregistrement de l'historique sera supprimé.",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirmation == JOptionPane.YES_OPTION) {
                boolean succes = virementService.supprimerVirement(selectionne.getNumVirement());
                if (!succes) {
                    JOptionPane.showMessageDialog(this,
                            "Suppression impossible.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
                rafraichir();
            }
        });

        barre.add(boutonAvis);
        barre.add(boutonModifierDate);
        barre.add(boutonSupprimer);
        return barre;
    }

    /** Petit formulaire pour corriger uniquement la date d'un virement (montant/comptes non modifiables : les soldes ont déjà bougé). */
    private void ouvrirFormulaireCorrection(Virement virement) {

        JDialog dialogue = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Corriger la date du virement " + virement.getNumVirement(),
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialogue.setSize(380, 260);
        dialogue.setLocationRelativeTo(this);
        dialogue.getContentPane().setBackground(Color.WHITE);

        JPanel contenu = new JPanel();
        contenu.setBackground(Color.WHITE);
        contenu.setLayout(new BoxLayout(contenu, BoxLayout.Y_AXIS));
        contenu.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        JLabel info = new JLabel("<html>Le montant et les comptes ne sont pas modifiables<br>"
                + "(les soldes ont déjà été débités/crédités).<br>Seule la date peut être corrigée.</html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(new Color(120, 126, 138));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenu.add(info);
        contenu.add(Box.createVerticalStrut(16));

        ChampTexteArrondi champDate = new ChampTexteArrondi("AAAA-MM-JJ HH:MM");
        champDate.setText(virement.getDateTransfert().toLocalDate() + " "
            + String.format("%02d:%02d", virement.getDateTransfert().getHour(), virement.getDateTransfert().getMinute()));
        contenu.add(champLabelise("Date du virement", champDate));
        contenu.add(Box.createVerticalStrut(20));

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        boutons.setOpaque(false);
        boutons.setAlignmentX(Component.LEFT_ALIGNMENT);

        BoutonArrondi boutonAnnuler = new BoutonArrondi("Annuler", BoutonArrondi.Style.CONTOUR);
        boutonAnnuler.addActionListener(e -> dialogue.dispose());

        BoutonArrondi boutonEnregistrer = new BoutonArrondi("Enregistrer");
        boutonEnregistrer.addActionListener(e -> {
            try {
                String texte = champDate.getText().trim().replace(" ", "T");
                virement.setDateTransfert(java.time.LocalDateTime.parse(texte));
                boolean succes = virementService.modifierVirement(virement);
                if (succes) {
                    dialogue.dispose();
                    rafraichir();
                } else {
                    JOptionPane.showMessageDialog(dialogue, "Échec de l'enregistrement.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogue,
                        "Format de date invalide. Utilisez AAAA-MM-JJ HH:MM.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        boutons.add(boutonAnnuler);
        boutons.add(boutonEnregistrer);
        contenu.add(boutons);

        dialogue.add(contenu);
        dialogue.setVisible(true);
    }

    private Virement virementSelectionne() {
        int ligne = table.getSelectedRow();
        if (ligne < 0 || virementsAffiches == null || ligne >= virementsAffiches.size()) {
            return null;
        }
        return virementsAffiches.get(ligne);
    }

    private void rafraichir() {

        String motCle = champRecherche == null ? "" : champRecherche.getText().trim();
        List<Virement> virements = virementService.rechercherVirements(motCle);
        LocalDate debut = dateDebut == null ? null : dateDebut.getDate();
        LocalDate fin = dateFin == null ? null : dateFin.getDate();
        virementsAffiches = new ArrayList<>();

        for (Virement virement : virements) {
            LocalDate dateVirement = virement.getDateTransfert() == null
                    ? null : virement.getDateTransfert().toLocalDate();
            if (dateVirement != null
                    && (debut == null || !dateVirement.isBefore(debut))
                    && (fin == null || !dateVirement.isAfter(fin))) {
                virementsAffiches.add(virement);
            }
        }

        modeleTable.setRowCount(0);
        for (Virement v : virementsAffiches) {
            modeleTable.addRow(new Object[]{
                    v.getNumVirement(),
                    v.getNumCompteEnvoyeur(),
                    v.getNumCompteBeneficiaire(),
                    v.getMontant(),
                    v.getDateTransfert() == null ? "" : v.getDateTransfert().format(FORMAT_DATE)
            });
        }
    }

    /** Formulaire "Effectuer un virement". Propose de générer l'avis PDF juste après. */
    private void ouvrirFormulaireVirement() {

        JDialog dialogue = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Effectuer un virement",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialogue.setSize(420, 420);
        dialogue.setLocationRelativeTo(this);
        dialogue.getContentPane().setBackground(Color.WHITE);

        JPanel contenu = new JPanel();
        contenu.setBackground(Color.WHITE);
        contenu.setLayout(new BoxLayout(contenu, BoxLayout.Y_AXIS));
        contenu.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        ChampTexteArrondi champEnvoyeur = new ChampTexteArrondi("N° de compte de l'envoyeur");
        ChampTexteArrondi champBeneficiaire = new ChampTexteArrondi("N° de compte du bénéficiaire");
        ChampTexteArrondi champMontant = new ChampTexteArrondi("Montant en Ar");

        contenu.add(champLabelise("Compte envoyeur", champEnvoyeur));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Compte bénéficiaire", champBeneficiaire));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Montant (Ar)", champMontant));
        contenu.add(Box.createVerticalStrut(24));

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        boutons.setOpaque(false);
        boutons.setAlignmentX(Component.LEFT_ALIGNMENT);

        BoutonArrondi boutonAnnuler = new BoutonArrondi("Annuler", BoutonArrondi.Style.CONTOUR);
        boutonAnnuler.addActionListener(e -> dialogue.dispose());

        BoutonArrondi boutonValider = new BoutonArrondi("Effectuer le virement");
        boutonValider.addActionListener(e -> {
            try {
                Virement virement = new Virement();
                virement.setNumCompteEnvoyeur(champEnvoyeur.getText().trim());
                virement.setNumCompteBeneficiaire(champBeneficiaire.getText().trim());
                virement.setMontant(new BigDecimal(champMontant.getText().trim()));

                String erreur = virementService.effectuerVirement(virement);

                if (erreur == null) {
                    dialogue.dispose();
                    rafraichir();

                    // Recharge le virement complet (date, n° généré) depuis la base pour l'avis
                    Virement virementEnregistre = virementService.chercherVirement(virement.getNumVirement());

                    int reponse = JOptionPane.showConfirmDialog(this,
                            "Virement " + virement.getNumVirement() + " effectué avec succès.\n"
                                    + "Voulez-vous générer l'avis de virement en PDF maintenant ?",
                            "Virement effectué", JOptionPane.YES_NO_OPTION);
                    if (reponse == JOptionPane.YES_OPTION) {
                        genererAvisPdf(virementEnregistre);
                    }
                } else {
                    JOptionPane.showMessageDialog(dialogue, erreur, "Virement refusé", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialogue,
                        "Montant invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        boutons.add(boutonAnnuler);
        boutons.add(boutonValider);
        contenu.add(boutons);

        dialogue.add(contenu);
        dialogue.setVisible(true);
    }

    /** Demande où enregistrer le PDF, le génère, puis propose de l'ouvrir. */
    private void genererAvisPdf(Virement virement) {

        if (virement == null) {
            return;
        }

        Client envoyeur = virementService.infosCompte(virement.getNumCompteEnvoyeur());
        Client beneficiaire = virementService.infosCompte(virement.getNumCompteBeneficiaire());

        if (envoyeur == null || beneficiaire == null) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de récupérer les informations des comptes.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser selecteur = new JFileChooser();
        selecteur.setSelectedFile(new File("avis_virement_" + virement.getNumVirement() + ".pdf"));
        int choix = selecteur.showSaveDialog(this);

        if (choix != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String chemin = selecteur.getSelectedFile().getAbsolutePath();
        if (!chemin.toLowerCase().endsWith(".pdf")) {
            chemin += ".pdf";
        }

        try {
            AvisVirementPDF.genererAvis(virement, envoyeur, beneficiaire, chemin);

            int ouvrir = JOptionPane.showConfirmDialog(this,
                    "Avis de virement généré : " + chemin + "\nVoulez-vous l'ouvrir maintenant ?",
                    "PDF généré", JOptionPane.YES_NO_OPTION);

            if (ouvrir == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(chemin));
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la génération du PDF : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
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
