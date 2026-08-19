package vue;

import javax.swing.*;
import modele.Agent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Menu de navigation latéral avec image de fond proportionnelle, voile sombre 
 * et texte optimisé pour une lisibilité maximale.
 */
public class Sidebar extends JPanel {

    private static final Color FOND_SURVOL = new Color(28, 46, 92, 200);
    private static final Color FOND_ACTIF = new Color(41, 84, 209, 220);
    private static final Color TEXTE = new Color(198, 207, 232);

    private final Map<String, JPanel> boutonsParCle = new LinkedHashMap<>();
    private String cleActive;
    private Image backgroundImage;

    public Sidebar(Consumer<String> surNavigation, Agent agent) {
        setPreferredSize(new Dimension(240, 0));
        setLayout(new BorderLayout());

        // Chargement de l'image de fond depuis le dossier /ressources/
        java.net.URL bgUrl = getClass().getResource("/ressources/f31.png"); 
        if (bgUrl != null) {
            backgroundImage = new ImageIcon(bgUrl).getImage();
        }

        // 1. En-tête : Logo BankSys (avec icône bank.png à gauche) + Sous-titre
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(35, 20, 35, 20));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        java.net.URL bankIconUrl = getClass().getResource("/ressources/bank.png");
        if (bankIconUrl != null) {
            ImageIcon bankIcon = new ImageIcon(bankIconUrl);
            Image imgBank = bankIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(imgBank));
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            titlePanel.add(iconLabel);
        }

        JLabel titre = new JLabel("BankSys");
        titre.setForeground(Color.WHITE);
        titre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titlePanel.add(titre);

        JLabel subLogo = new JLabel("Système de Gestion Bancaire");
        subLogo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLogo.setForeground(new Color(170, 190, 225));
        subLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titlePanel);
        header.add(Box.createVerticalStrut(3));
        header.add(subLogo);
        add(header, BorderLayout.NORTH);

        // 2. Menu central
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        ajouterEntree(menu, "CLIENTS", "customer", "Gestion des clients", surNavigation);
        menu.add(Box.createVerticalStrut(12));
        ajouterEntree(menu, "VIREMENTS", "transfer", "Virements", surNavigation);
        menu.add(Box.createVerticalStrut(12));
        ajouterEntree(menu, "PRETS", "loan", "Gestion des prêts", surNavigation);
        menu.add(Box.createVerticalStrut(12));
        ajouterEntree(menu, "REMBOURSEMENTS", "information", "Remboursements", surNavigation);
        menu.add(Box.createVerticalStrut(12));
        ajouterEntree(menu, "SITUATION", "up", "Situation des prêts", surNavigation);
        menu.add(Box.createVerticalStrut(12));
        ajouterEntree(menu, "NOTIFICATIONS", "notification", "Notifications email", surNavigation);

        if (agent != null && "ADMIN".equals(agent.getRole())) {
            menu.add(Box.createVerticalStrut(12));
            ajouterEntree(menu, "BENEFICE", "dashboard", "Bénéfice banque", surNavigation);
        }

        add(menu, BorderLayout.CENTER);

        // 3. Pied de page : Déconnexion
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 12, 20, 12));

        JPanel decoPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getBackground() == FOND_SURVOL) {
                    g2.setColor(FOND_SURVOL);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        decoPanel.setOpaque(false);
        decoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        decoPanel.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 10));
        decoPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel decoLabel;
        java.net.URL logoutUrl = getClass().getResource("/ressources/loan (1).png"); 
        if (logoutUrl != null) {
            ImageIcon logoutIcon = new ImageIcon(logoutUrl);
            Image imgDeco = logoutIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            decoLabel = new JLabel("Déconnexion", new ImageIcon(imgDeco), JLabel.LEFT);
            decoLabel.setIconTextGap(12);
        } else {
            decoLabel = new JLabel("⮜ Déconnexion");
        }
        decoLabel.setForeground(new Color(239, 68, 68));
        decoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        decoPanel.add(decoLabel, BorderLayout.WEST);

        decoPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                decoPanel.setBackground(FOND_SURVOL);
                decoPanel.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                decoPanel.setBackground(new Color(0,0,0,0));
                decoPanel.repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                surNavigation.accept("LOGOUT");
            }
        });

        footer.add(decoPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        activer("CLIENTS");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Active le rendu haute qualité pour l'image et le texte
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if (backgroundImage != null) {
            int imgWidth = backgroundImage.getWidth(this);
            int imgHeight = backgroundImage.getHeight(this);

            if (imgWidth > 0 && imgHeight > 0) {
                // Maintien des proportions de l'image (Cover)
                double panelRatio = (double) panelWidth / panelHeight;
                double imgRatio = (double) imgWidth / imgHeight;

                int drawWidth, drawHeight, x, y;

                if (panelRatio > imgRatio) {
                    drawWidth = panelWidth;
                    drawHeight = (int) (panelWidth / imgRatio);
                    x = 0;
                    y = (panelHeight - drawHeight) / 2;
                } else {
                    drawWidth = (int) (panelHeight * imgRatio);
                    drawHeight = panelHeight;
                    x = (panelWidth - drawWidth) / 2;
                    y = 0;
                }

                g2.drawImage(backgroundImage, x, y, drawWidth, drawHeight, this);
            }
        } else {
            g2.setColor(new Color(18, 33, 73));
            g2.fillRect(0, 0, panelWidth, panelHeight);
        }

        // Voile sombre ajusté à 210 pour faire bien ressortir le texte par-dessus l'image
        g2.setColor(new Color(10, 18, 35, 210));
        g2.fillRect(0, 0, panelWidth, panelHeight);

        g2.dispose();
    }

    private void ajouterEntree(JPanel menu, String cle, String nomIcone, String libelle, Consumer<String> surNavigation) {
        JPanel entree = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                
                if (cle.equals(cleActive)) {
                    g2.setColor(FOND_ACTIF);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } else if (getBackground().getAlpha() > 0) {
                    g2.setColor(FOND_SURVOL);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        entree.setOpaque(false);
        entree.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        entree.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 10));
        entree.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel label;
        java.net.URL imgUrl = getClass().getResource("/ressources/" + nomIcone + ".png");
        if (imgUrl != null) {
            ImageIcon iconeOriginale = new ImageIcon(imgUrl);
            Image imgRedimensionnee = iconeOriginale.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            label = new JLabel(libelle, new ImageIcon(imgRedimensionnee), JLabel.LEFT);
            label.setIconTextGap(12);
        } else {
            label = new JLabel(libelle);
        }

        label.setForeground(TEXTE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        entree.add(label, BorderLayout.WEST);

        entree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!cle.equals(cleActive)) {
                    entree.setBackground(FOND_SURVOL);
                    entree.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!cle.equals(cleActive)) {
                    entree.setBackground(new Color(0, 0, 0, 0));
                    entree.repaint();
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

            panel.setBackground(actif ? FOND_ACTIF : new Color(0, 0, 0, 0));
            panel.repaint();

            JLabel label = (JLabel) panel.getComponent(0);
            label.setForeground(actif ? Color.WHITE : TEXTE);
            label.setFont(label.getFont().deriveFont(actif ? Font.BOLD : Font.PLAIN));
        }
    }
}