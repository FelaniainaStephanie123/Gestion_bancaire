package vue.panels;

import modele.Benefice;
import service.BeneficeService;
import vue.composants.BoutonArrondi;
import vue.composants.PanneauArrondi;

import javax.swing.*;
import java.awt.*;

/**
 * Écran "Bénéfice accordé à la banque" : 3 cartes de synthèse
 * (nombre de prêts, total prêté, bénéfice total), branché sur BeneficeService
 * qui lit la vue SQL v_benefice_banque.
 */
public class BeneficePanel extends JPanel {

    private static final Color FOND = new Color(245, 247, 252);
    private static final Color NAVY = new Color(18, 33, 73);

    private final BeneficeService beneficeService = new BeneficeService();

    private JLabel valeurNombrePrets;
    private JLabel valeurTotalPrete;
    private JLabel valeurBenefice;

    public BeneficePanel() {

        setBackground(FOND);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel cartes = new JPanel(new GridLayout(1, 3, 20, 0));
        cartes.setOpaque(false);

        PanneauArrondi carte1 = construireCarte("Nombre de prêts");
        valeurNombrePrets = dernierLabel(carte1);

        PanneauArrondi carte2 = construireCarte("Total prêté (Ar)");
        valeurTotalPrete = dernierLabel(carte2);

        PanneauArrondi carte3 = construireCarte("Bénéfice accordé à la banque (Ar)");
        valeurBenefice = dernierLabel(carte3);

        cartes.add(carte1);
        cartes.add(carte2);
        cartes.add(carte3);

        JPanel haut = new JPanel(new BorderLayout());
        haut.setOpaque(false);
        haut.add(cartes, BorderLayout.CENTER);

        BoutonArrondi boutonActualiser = new BoutonArrondi("Actualiser");
        boutonActualiser.addActionListener(e -> rafraichir());

        JPanel barreActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        barreActions.setOpaque(false);
        barreActions.add(boutonActualiser);

        add(haut, BorderLayout.NORTH);
        add(barreActions, BorderLayout.SOUTH);

        rafraichir();
    }

    private PanneauArrondi construireCarte(String libelle) {

        PanneauArrondi carte = new PanneauArrondi(NAVY);
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBorder(BorderFactory.createEmptyBorder(26, 24, 26, 24));
        carte.setPreferredSize(new Dimension(0, 150));

        JLabel titre = new JLabel(libelle);
        titre.setForeground(new Color(198, 207, 232));
        titre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valeur = new JLabel("—");
        valeur.setForeground(Color.WHITE);
        valeur.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valeur.setAlignmentX(Component.LEFT_ALIGNMENT);

        carte.add(titre);
        carte.add(Box.createVerticalStrut(14));
        carte.add(valeur);

        return carte;
    }

    private JLabel dernierLabel(PanneauArrondi carte) {
        return (JLabel) carte.getComponent(carte.getComponentCount() - 1);
    }

    private void rafraichir() {

        Benefice benefice = beneficeService.obtenirBenefice();

        if (benefice == null) {
            valeurNombrePrets.setText("—");
            valeurTotalPrete.setText("—");
            valeurBenefice.setText("—");
            return;
        }

        valeurNombrePrets.setText(String.valueOf(benefice.getNombrePrets()));
        valeurTotalPrete.setText(formater(benefice.getTotalPrete()));
        valeurBenefice.setText(formater(benefice.getBeneficeTotal()));
    }

    private String formater(java.math.BigDecimal montant) {
        return montant == null ? "0" : String.format("%,.0f", montant).replace(',', ' ');
    }

}
