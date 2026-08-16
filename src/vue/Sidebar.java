package vue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Menu de navigation latéral. Chaque entrée déclenche l'affichage
 * du panel correspondant dans le MainPanel via le callback fourni.
 */
public class Sidebar extends JPanel {

    private static final Color FOND = new Color(18, 33, 73);
    private static final Color FOND_SURVOL = new Color(28, 46, 92);
    private static final Color FOND_ACTIF = new Color(41, 84, 209);
    private static final Color TEXTE = new Color(198, 207, 232);

    private final Map<String, JPanel> boutonsParCle = new LinkedHashMap<>();
    private String cleActive;

    public Sidebar(Consumer<String> surNavigation) {

        setPreferredSize(new Dimension(230, 0));
        setBackground(FOND);
        setLayout(new BorderLayout());

        JLabel titre = new JLabel("BankSys");
        titre.setForeground(Color.WHITE);
        titre.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titre.setHorizontalAlignment(SwingConstants.CENTER);
        titre.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));
        add(titre, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

        ajouterEntree(menu, "CLIENTS", "Gestion des clients", surNavigation);
        ajouterEntree(menu, "VIREMENTS", "Virements", surNavigation);
        ajouterEntree(menu, "PRETS", "Gestion des prêts", surNavigation);
        ajouterEntree(menu, "REMBOURSEMENTS", "Remboursements", surNavigation);
        ajouterEntree(menu, "SITUATION", "Situation des prêts", surNavigation);
        ajouterEntree(menu, "BENEFICE", "Bénéfice banque", surNavigation);
        ajouterEntree(menu, "NOTIFICATIONS", "Notifications email", surNavigation);

        add(menu, BorderLayout.CENTER);

        activer("CLIENTS");
    }

    private void ajouterEntree(JPanel menu, String cle, String libelle, Consumer<String> surNavigation) {

        JPanel entree = new JPanel(new BorderLayout());
        entree.setOpaque(true);
        entree.setBackground(FOND);
        entree.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        entree.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 10));

        JLabel label = new JLabel(libelle);
        label.setForeground(TEXTE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        entree.add(label, BorderLayout.WEST);

        entree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!cle.equals(cleActive)) {
                    entree.setBackground(FOND_SURVOL);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!cle.equals(cleActive)) {
                    entree.setBackground(FOND);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                activer(cle);
                surNavigation.accept(cle);
            }
        });

        boutonsParCle.put(cle, entree);
        menu.add(entree);
    }

    private void activer(String cle) {

        cleActive = cle;

        for (Map.Entry<String, JPanel> entry : boutonsParCle.entrySet()) {

            JPanel panel = entry.getValue();
            boolean actif = entry.getKey().equals(cle);

            panel.setBackground(actif ? FOND_ACTIF : FOND);

            JLabel label = (JLabel) panel.getComponent(0);
            label.setForeground(actif ? Color.WHITE : TEXTE);
            label.setFont(label.getFont().deriveFont(actif ? Font.BOLD : Font.PLAIN));
        }
    }

}
