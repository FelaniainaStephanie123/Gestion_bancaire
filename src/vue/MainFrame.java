package vue;

import modele.Agent;
import javax.swing.*;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {

    public MainFrame() {
        this(null);
    }

    public MainFrame(Agent agentConnecte) {

        String titre = "BankSys - Gestion bancaire";
        if (agentConnecte != null && agentConnecte.getNomComplet() != null) {
            titre += "  (connecté : " + agentConnecte.getNomComplet() + ")";
        }
        setTitle(titre);

        setSize(1400, 800);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        try {
            MainPanel mainPanel = new MainPanel();
Sidebar sidebar = new Sidebar(mainPanel::afficherPanel, agentConnecte);
            add(sidebar, BorderLayout.WEST);
            add(mainPanel, BorderLayout.CENTER);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement de l'interface principale.\nVérifiez la base de données et les dépendances Java.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        setVisible(true);
    }

}

