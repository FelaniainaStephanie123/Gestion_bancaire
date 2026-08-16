package vue;

import vue.panels.BeneficePanel;
import vue.panels.ClientPanel;
import vue.panels.NotificationPanel;
import vue.panels.PretPanel;
import vue.panels.RenduPanel;
import vue.panels.SituationPanel;
import vue.panels.VirementPanel;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Zone principale : un en-tête fixe (HeaderPanel) et, en dessous,
 * un CardLayout qui bascule entre les différents écrans métier.
 */
public class MainPanel extends JPanel {

    private static final Map<String, String> TITRES = new LinkedHashMap<>();
    static {
        TITRES.put("CLIENTS", "Gestion des clients");
        TITRES.put("VIREMENTS", "Gestion des virements");
        TITRES.put("PRETS", "Gestion des prêts");
        TITRES.put("REMBOURSEMENTS", "Gestion des remboursements");
        TITRES.put("SITUATION", "Liste des prêts par situation");
        TITRES.put("BENEFICE", "Bénéfice accordé à la banque");
        TITRES.put("NOTIFICATIONS", "Notification par email");
    }

    private final HeaderPanel headerPanel;
    private final JPanel conteneurCartes;
    private final CardLayout cardLayout;

    public MainPanel() {

        setLayout(new BorderLayout());

        setBackground(new Color(245, 247, 252));

        headerPanel = new HeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        conteneurCartes = new JPanel(cardLayout);
        conteneurCartes.setOpaque(false);

        conteneurCartes.add(new ClientPanel(), "CLIENTS");
        conteneurCartes.add(new VirementPanel(), "VIREMENTS");
        conteneurCartes.add(new PretPanel(), "PRETS");
        conteneurCartes.add(new RenduPanel(), "REMBOURSEMENTS");
        conteneurCartes.add(new SituationPanel(), "SITUATION");
        conteneurCartes.add(new BeneficePanel(), "BENEFICE");
        conteneurCartes.add(new NotificationPanel(), "NOTIFICATIONS");

        add(conteneurCartes, BorderLayout.CENTER);
    }

    /** Appelé par le Sidebar quand l'utilisateur clique sur une entrée du menu. */
    public void afficherPanel(String cle) {
        cardLayout.show(conteneurCartes, cle);
        headerPanel.setTitre(TITRES.getOrDefault(cle, ""));
    }

}
