package vue;

import dao.AgentDAO;
import modele.Agent;
import vue.composants.BoutonArrondi;
import vue.composants.ChampMotDePasseArrondi;
import vue.composants.ChampTexteArrondi;

import javax.swing.*;
import java.awt.*;

/**
 * Premier écran affiché au lancement de l'application.
 * Tant que l'agent n'est pas authentifié, il n'accède pas à MainFrame
 * (gestion des clients / virements).
 */
public class LoginFrame extends JFrame {

    private final AgentDAO agentDAO = new AgentDAO();

    public LoginFrame() {

        setTitle("BankSys - Connexion");
        setSize(420, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel conteneur = new JPanel();
        conteneur.setBackground(Color.WHITE);
        conteneur.setLayout(new BoxLayout(conteneur, BoxLayout.Y_AXIS));
        conteneur.setBorder(BorderFactory.createEmptyBorder(50, 40, 40, 40));

        JLabel logo = new JLabel("BankSys");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logo.setForeground(new Color(41, 84, 209));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sousTitre = new JLabel("Espace agent bancaire");
        sousTitre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sousTitre.setForeground(new Color(120, 126, 138));
        sousTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        ChampTexteArrondi champIdentifiant = new ChampTexteArrondi("Identifiant");
        champIdentifiant.setAlignmentX(Component.CENTER_ALIGNMENT);
        champIdentifiant.setMaximumSize(new Dimension(320, 42));
        champIdentifiant.setPreferredSize(new Dimension(320, 42));

        ChampMotDePasseArrondi champMotDePasse = new ChampMotDePasseArrondi("Mot de passe");
        champMotDePasse.setAlignmentX(Component.CENTER_ALIGNMENT);
        champMotDePasse.setMaximumSize(new Dimension(320, 42));
        champMotDePasse.setPreferredSize(new Dimension(320, 42));

        JLabel messageErreur = new JLabel(" ");
        messageErreur.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageErreur.setForeground(new Color(214, 69, 69));
        messageErreur.setAlignmentX(Component.CENTER_ALIGNMENT);

        BoutonArrondi boutonConnexion = new BoutonArrondi("Se connecter");
        boutonConnexion.setAlignmentX(Component.CENTER_ALIGNMENT);

        Runnable tenterConnexion = () -> {
            String identifiant = champIdentifiant.getText().trim();
            String motDePasse = new String(champMotDePasse.getPassword());

            if (identifiant.isEmpty() || motDePasse.isEmpty()) {
                messageErreur.setText("Renseigne l'identifiant et le mot de passe.");
                return;
            }

            try {
                Agent agent = agentDAO.authentifier(identifiant, motDePasse);

                if (agent != null) {
                    dispose();
                    SwingUtilities.invokeLater(() -> new MainFrame(agent));
                } else {
                    messageErreur.setText("Identifiant ou mot de passe incorrect.");
                    champMotDePasse.setText("");
                }
            } catch (Exception ex) {
                messageErreur.setText("Connexion impossible à la base de données.");
                ex.printStackTrace();
            }
        };

        boutonConnexion.addActionListener(e -> tenterConnexion.run());
        // Permet aussi de valider avec la touche Entrée depuis le champ mot de passe
        champMotDePasse.addActionListener(e -> tenterConnexion.run());

        conteneur.add(logo);
        conteneur.add(Box.createVerticalStrut(4));
        conteneur.add(sousTitre);
        conteneur.add(Box.createVerticalStrut(35));
        conteneur.add(champIdentifiant);
        conteneur.add(Box.createVerticalStrut(16));
        conteneur.add(champMotDePasse);
        conteneur.add(Box.createVerticalStrut(10));
        conteneur.add(messageErreur);
        conteneur.add(Box.createVerticalStrut(10));
        conteneur.add(boutonConnexion);

        add(conteneur);
        setVisible(true);
    }
}
