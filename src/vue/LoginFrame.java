package vue;

import dao.AgentDAO;
import modele.Agent;
import vue.composants.BoutonArrondi;
import vue.composants.ChampMotDePasseArrondi;
import vue.composants.ChampTexteArrondi;

import javax.swing.*;
import java.awt.*;

/**
 * Écran de connexion — design deux colonnes inspiré du showcase BankSys.
 * Colonne gauche : branding avec gradient navy + illustration
 * Colonne droite : formulaire de connexion
 */
public class LoginFrame extends JFrame {

    private final AgentDAO agentDAO = new AgentDAO();

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color WHITE         = Color.WHITE;
    private static final Color TEXT_MUTED    = new Color(120, 126, 148);
    private static final Color ERROR_CLR     = new Color(214,  69,  69);
    private static final Color BG_RIGHT      = new Color(247, 249, 253);
    private static final Color BLUE_ACCENT = new Color(59, 130, 246);

    public LoginFrame() {
        setTitle("BankSys — Connexion");
        setSize(860, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new GridLayout(1, 2, 0, 0));

        add(buildPanneauGauche());
        add(buildPanneauDroit());

        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PANNEAU GAUCHE — Branding avec gradient et image
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildPanneauGauche() {
        JPanel panneau = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // 1. Dessiner l'image de fond redimensionnée
                java.net.URL imgUrl = getClass().getResource("/ressources/f31.png");
                if (imgUrl != null) {
                    ImageIcon icon = new ImageIcon(imgUrl);
                    Image img = icon.getImage();
                    
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int imgWidth = icon.getIconWidth();
                    int imgHeight = icon.getIconHeight();
                    
                    double scale = Math.max((double) panelWidth / imgWidth, (double) panelHeight / imgHeight);
                    int scaledWidth = (int) (imgWidth * scale);
                    int scaledHeight = (int) (imgHeight * scale);
                    int x = (panelWidth - scaledWidth) / 2;
                    int y = (panelHeight - scaledHeight) / 2;
                    
                    g2.drawImage(img, x, y, scaledWidth, scaledHeight, this);
                }

                // 2. LE VOILE SOMBRE : Assombrit l'image pour faire ressortir le texte blanc
                g2.setColor(new Color(15, 23, 42, 170)); // Bleu-gris foncé avec transparence (170/255)
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        JPanel sep = new JPanel();
        sep.setPreferredSize(new Dimension(180, 1));
        sep.setBackground(new Color(255, 255, 255, 100));
        sep.setOpaque(true);

        JLabel slogan = new JLabel("<html><div style='text-align:center;'>"
            + "Votre confiance,<br>notre priorité</div></html>");
        slogan.setFont(new Font("Segoe UI", Font.BOLD, 20));
        slogan.setForeground(WHITE);
        slogan.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel sousSlogan = new JLabel("<html><div style='text-align:center;line-height:1.5'>"
            + "Une gestion simple, sécurisée et efficace<br>de vos opérations bancaires."
            + "</div></html>");
        sousSlogan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sousSlogan.setForeground(new Color(180, 200, 240));
        sousSlogan.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
        JLabel badgeTxt = new JLabel("● Système de Gestion Bancaire");
        badgeTxt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        badgeTxt.setForeground(new Color(147, 197, 253));
        badge.add(badgeTxt);
        badge.setPreferredSize(new Dimension(220, 26));

        gbc.insets = new Insets(0, 32, 18, 32);
        panneau.add(sep, gbc);
        gbc.insets = new Insets(0, 32, 10, 32);
        panneau.add(slogan, gbc);
        gbc.insets = new Insets(0, 32, 20, 32);
        panneau.add(sousSlogan, gbc);
        gbc.insets = new Insets(0, 32, 0, 32);
        panneau.add(badge, gbc);

        return panneau;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PANNEAU DROIT — Formulaire
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildPanneauDroit() {
        JPanel panneau = new JPanel(new GridBagLayout());
        panneau.setBackground(BG_RIGHT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.gridy  = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Carte blanche centrale
        JPanel carte = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Ombre portée simulée
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(4, 6, getWidth() - 4, getHeight() - 4, 20, 20);
                // Fond blanc
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);
                g2.dispose();
            }
        };
        carte.setOpaque(false);
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 40));
        carte.setPreferredSize(new Dimension(320, 380));

        // En-tête formulaire
        JLabel titre = new JLabel("Connexion");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titre.setForeground(new Color(15, 23, 60));
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sousTitre = new JLabel("Bienvenue, veuillez vous connecter");
        sousTitre.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sousTitre.setForeground(TEXT_MUTED);
        sousTitre.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Séparateur fin sous le titre
        JPanel ligneTitre = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(226, 232, 240));
                g2.fillRect(0, 0, getWidth(), 1);
                g2.setColor(BLUE_ACCENT);
                g2.fillRect(0, 0, 40, 2);
                g2.dispose();
            }
        };
        ligneTitre.setOpaque(false);
        ligneTitre.setPreferredSize(new Dimension(Integer.MAX_VALUE, 3));
        ligneTitre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
        ligneTitre.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Champs
        JLabel labelId = fieldLabel("Identifiant");
        ChampTexteArrondi champIdentifiant = new ChampTexteArrondi("ex: admin");
        champIdentifiant.setAlignmentX(Component.LEFT_ALIGNMENT);
        champIdentifiant.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel labelMdp = fieldLabel("Mot de passe");
        ChampMotDePasseArrondi champMotDePasse = new ChampMotDePasseArrondi("••••••••");
        champMotDePasse.setAlignmentX(Component.LEFT_ALIGNMENT);
        champMotDePasse.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        // Message d'erreur
        JLabel messageErreur = new JLabel(" ");
        messageErreur.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        messageErreur.setForeground(ERROR_CLR);
        messageErreur.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Bouton connexion
        BoutonArrondi boutonConnexion = new BoutonArrondi("Se connecter");
        boutonConnexion.setAlignmentX(Component.LEFT_ALIGNMENT);
        boutonConnexion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        boutonConnexion.setPreferredSize(new Dimension(Integer.MAX_VALUE, 44));

        // Logique de connexion
        Runnable tenterConnexion = () -> {
            String identifiant = champIdentifiant.getText().trim();
            String motDePasse  = new String(champMotDePasse.getPassword());

            if (identifiant.isEmpty() || motDePasse.isEmpty()) {
                messageErreur.setText("⚠ Renseigne l'identifiant et le mot de passe.");
                return;
            }
            try {
                Agent agent = agentDAO.authentifier(identifiant, motDePasse);
                if (agent != null) {
                    dispose();
                    SwingUtilities.invokeLater(() -> new MainFrame(agent));
                } else {
                    messageErreur.setText("⚠ Identifiant ou mot de passe incorrect.");
                    champMotDePasse.setText("");
                }
            } catch (Exception ex) {
                messageErreur.setText("⚠ Connexion impossible à la base de données.");
                ex.printStackTrace();
            }
        };

        boutonConnexion.addActionListener(e -> tenterConnexion.run());
        champMotDePasse.addActionListener(e -> tenterConnexion.run());
        champIdentifiant.addActionListener(e -> champMotDePasse.requestFocus());

        // Assemblage carte
        carte.add(titre);
        carte.add(Box.createVerticalStrut(4));
        carte.add(sousTitre);
        carte.add(Box.createVerticalStrut(10));
        carte.add(ligneTitre);
        carte.add(Box.createVerticalStrut(22));
        carte.add(labelId);
        carte.add(Box.createVerticalStrut(6));
        carte.add(champIdentifiant);
        carte.add(Box.createVerticalStrut(16));
        carte.add(labelMdp);
        carte.add(Box.createVerticalStrut(6));
        carte.add(champMotDePasse);
        carte.add(Box.createVerticalStrut(8));
        carte.add(messageErreur);
        carte.add(Box.createVerticalStrut(4));
        carte.add(boutonConnexion);

        gbc.insets = new Insets(0, 30, 0, 30);
        panneau.add(carte, gbc);

        return panneau;
    }

    private JLabel fieldLabel(String texte) {
        JLabel lbl = new JLabel(texte);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(50, 60, 90));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
}