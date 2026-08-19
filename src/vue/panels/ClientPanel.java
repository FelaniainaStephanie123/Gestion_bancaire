package vue.panels;

import modele.Client;
import service.ClientService;
import vue.composants.BoutonArrondi;
import vue.composants.ChampTexteArrondi;
import vue.composants.PanneauArrondi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import dao.ClientDAO;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Écran "Gestion des clients" : recherche (LIKE %mot%), tableau
 * et formulaire d'ajout/modification, branché sur ClientService.
 */
public class ClientPanel extends JPanel {

    private static final Color FOND = new Color(245, 247, 252);

    // IMPORTANT : une seule colonne "Actions" (au lieu de "Modifier" + "Supprimer" séparés)
    // pour que les deux boutons soient dessinés ensemble dans LA MÊME colonne.
    private static final String[] COLONNES = {
            "N° Compte", "Nom", "Prénoms", "Téléphone", "Email", "Solde actuel (Ar)", "Actions"
    };

    private static final int INDEX_COLONNE_ACTIONS = COLONNES.length - 1;

    private final ClientService clientService = new ClientService();

    private DefaultTableModel modeleTable;
    private JTable table;
    private ChampTexteArrondi champRecherche;
    private List<Client> clientsAffiches;

    public ClientPanel() {

        setBackground(FOND);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(construireBarreOutils(), BorderLayout.NORTH);
        add(construireTableau(), BorderLayout.CENTER);

        rafraichir();
       addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                rafraichir();
            }
            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
            });
    }

   private JPanel construireBarreOutils() {
    JPanel barre = new JPanel(new BorderLayout(15, 0));
    barre.setOpaque(false);

    // 1. Initialisation du champ
    champRecherche = new ChampTexteArrondi("Rechercher par n° de compte ou nom...");
    champRecherche.setPreferredSize(new Dimension(320, 40));

    // 2. Utilisation d'un DocumentListener simple qui appelle rafraichir()
    champRecherche.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) { rafraichir(); }
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) { rafraichir(); }
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) { rafraichir(); }
    });

    JPanel zoneRecherche = new JPanel(new BorderLayout(10, 0));
    zoneRecherche.setOpaque(false);
    zoneRecherche.add(champRecherche, BorderLayout.CENTER);

    BoutonArrondi boutonNouveau = new BoutonArrondi("+ Nouveau client");
    boutonNouveau.addActionListener(e -> ouvrirFormulaire(null));

    barre.add(zoneRecherche, BorderLayout.WEST);
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

        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);

        // La colonne Actions doit être assez large pour contenir les DEUX boutons.
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

    private Client clientSelectionne() {
        int ligne = table.getSelectedRow();
        if (ligne < 0 || clientsAffiches == null || ligne >= clientsAffiches.size()) {
            return null;
        }
        return clientsAffiches.get(ligne);
    }

    public void rafraichir() {
        String motCle = champRecherche == null ? "" : champRecherche.getText().trim();
        clientsAffiches = clientService.rechercherClients(motCle);

        modeleTable.setRowCount(0);
        for (Client c : clientsAffiches) {
            modeleTable.addRow(new Object[]{
                    c.getNumCompte(),
                    c.getNom(),
                    c.getPrenoms(),
                    c.getTel(),
                    c.getMail(),
                    c.getSoldeActuel(),
                    ""
            });
        }
    }

    /** Ouvre le formulaire d'ajout (clientExistant == null) ou de modification. */
    private void ouvrirFormulaire(Client clientExistant) {

        boolean modification = clientExistant != null;

        JDialog dialogue = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                modification ? "Modifier le client" : "Nouveau client",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialogue.setSize(420, 520);
        dialogue.setLocationRelativeTo(this);
        dialogue.getContentPane().setBackground(Color.WHITE);

        JPanel contenu = new JPanel();
        contenu.setBackground(Color.WHITE);
        contenu.setLayout(new BoxLayout(contenu, BoxLayout.Y_AXIS));
        contenu.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        ChampTexteArrondi champNumCompte = new ChampTexteArrondi("Ex: ACC001");
        ChampTexteArrondi champNom = new ChampTexteArrondi("Nom de famille");
        ChampTexteArrondi champPrenoms = new ChampTexteArrondi("Prénoms");
        ChampTexteArrondi champTel = new ChampTexteArrondi("Numéro de téléphone");
        ChampTexteArrondi champMail = new ChampTexteArrondi("Adresse email");
        ChampTexteArrondi champSolde = new ChampTexteArrondi("Solde initial en Ar");

        if (modification) {
            champNumCompte.setText(clientExistant.getNumCompte());
            champNumCompte.setEditable(false);
            champNom.setText(clientExistant.getNom());
            champPrenoms.setText(clientExistant.getPrenoms());
            champTel.setText(clientExistant.getTel());
            champMail.setText(clientExistant.getMail());
            champSolde.setText(String.valueOf(clientExistant.getSoldeActuel()));
            champSolde.setEditable(false);
            champSolde.setToolTipText("Le solde évolue uniquement via les virements.");
        } else {
            champNumCompte.setText(clientService.generationCompteAuto());
            champNumCompte.setEditable(false);
            champSolde.setText("0");
        }

        if (modification) {
            contenu.add(champLabelise("N° de compte", champNumCompte));
            contenu.add(Box.createVerticalStrut(14));
        }
        contenu.add(champLabelise("Nom", champNom));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Prénoms", champPrenoms));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Téléphone", champTel));

        JLabel indiceTel = new JLabel("Doit commencer par 032, 033, 034 ou 038 et contenir 10 chiffres");
        indiceTel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        indiceTel.setForeground(new Color(128, 0, 32)); // grenat
        indiceTel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenu.add(Box.createVerticalStrut(4));
        contenu.add(indiceTel);

        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Email", champMail));
        contenu.add(Box.createVerticalStrut(14));
        contenu.add(champLabelise("Solde " + (modification ? "actuel" : "initial") + " (Ar)", champSolde));
        contenu.add(Box.createVerticalStrut(24));

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        boutons.setOpaque(false);
        boutons.setAlignmentX(Component.LEFT_ALIGNMENT);

        BoutonArrondi boutonAnnuler = new BoutonArrondi("Annuler", BoutonArrondi.Style.CONTOUR);
        boutonAnnuler.addActionListener(e -> dialogue.dispose());

        BoutonArrondi boutonEnregistrer = new BoutonArrondi(modification ? "Enregistrer" : "Ajouter");
        boutonEnregistrer.addActionListener(e -> {
            try {
                Client client = new Client();
                client.setNumCompte(modification ? champNumCompte.getText().trim() : clientService.generationCompteAuto());
                client.setNom(champNom.getText().trim());
                client.setPrenoms(champPrenoms.getText().trim());

                String telephone = champTel.getText().trim();

                // Validation : doit commencer par 032, 033, 034 ou 038 et contenir 10 chiffres au total.
                if (!telephone.matches("^(032|033|034|038)\\d{7}$")) {
                    JOptionPane.showMessageDialog(dialogue,
                            "<html><font color='#800020'>Numéro de téléphone invalide. Le num de telephone doit commencer par 032, 033, 034 ou 038 et contenir 10 chiffres.</font></html>",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                client.setTel(telephone);
                client.setMail(champMail.getText().trim());

                BigDecimal solde = new BigDecimal(champSolde.getText().trim());

                // Validation : le solde ne peut pas être négatif.
                if (solde.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(dialogue,
                            "<html><font color='#800020'>Le montant doit être positif.</font></html>",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                client.setSoldeActuel(solde);

                boolean succes = modification
                        ? clientService.modifierClient(client)
                        : clientService.creerClient(client);

                if (succes) {
                    dialogue.dispose();
                    rafraichir();
                } else {
                    JOptionPane.showMessageDialog(dialogue,
                            "Impossible d'enregistrer le client (n° de compte déjà utilisé ou champ obligatoire manquant).",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialogue,
                        "Solde invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
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
                Client selectionne = clientsAffiches.get(ligneCourante);
                ouvrirFormulaire(selectionne);
            });

            boutonSupprimer = new BoutonArrondi("Supprimer", BoutonArrondi.Style.PLEIN,
                    new Color(214, 69, 69), new Color(184, 55, 55), Color.WHITE);
            boutonSupprimer.setPreferredSize(new Dimension(90, 30));
            boutonSupprimer.addActionListener(e -> {
                fireEditingStopped();
                Client selectionne = clientsAffiches.get(ligneCourante);
                int confirmation = JOptionPane.showConfirmDialog(
                        ClientPanel.this,
                        "Supprimer le client " + selectionne.getNumCompte() + " (" + selectionne.getNomComplet() + ") ?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirmation == JOptionPane.YES_OPTION) {
                    boolean succes = clientService.supprimerClient(selectionne.getNumCompte());
                    if (!succes) {
                        JOptionPane.showMessageDialog(ClientPanel.this,
                                "Suppression impossible : ce client a peut-être des virements ou prêts liés.",
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
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
        // Dans vue.panels.ClientPanel.java

    }
}