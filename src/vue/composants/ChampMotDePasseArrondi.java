package vue.composants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/**
 * Variante de ChampTexteArrondi pour les mots de passe :
 * mêmes coins arrondis, mais les caractères tapés sont masqués (•).
 */
public class ChampMotDePasseArrondi extends JPasswordField {

    private static final Color BORDURE = new Color(223, 227, 236);
    private static final Color BORDURE_FOCUS = new Color(41, 84, 209);
    private static final Color COULEUR_PLACEHOLDER = new Color(160, 165, 178);

    private final String placeholder;
    private final JButton boutonVisibilite;
    private boolean motDePasseVisible;
    private int rayon = 14;

    public ChampMotDePasseArrondi(String placeholder) {
        super();
        this.placeholder = placeholder;
        setEchoChar('•');
        setLayout(null);

        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 44));
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setBackground(Color.WHITE);

        boutonVisibilite = new JButton();
        boutonVisibilite.setToolTipText("Afficher le mot de passe");
        boutonVisibilite.setFocusable(false);
        boutonVisibilite.setBorderPainted(false);
        boutonVisibilite.setContentAreaFilled(false);
        boutonVisibilite.setOpaque(false);
        boutonVisibilite.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boutonVisibilite.addActionListener(e -> basculerVisibilite());
        add(boutonVisibilite);

        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                repaint();
            }
        });
    }

    public void setRayon(int rayon) {
        this.rayon = rayon;
    }

    private void basculerVisibilite() {
        motDePasseVisible = !motDePasseVisible;
        setEchoChar(motDePasseVisible ? (char) 0 : '•');
        boutonVisibilite.setToolTipText(motDePasseVisible
                ? "Masquer le mot de passe" : "Afficher le mot de passe");
        boutonVisibilite.repaint();
        requestFocusInWindow();
    }

    @Override
    public void doLayout() {
        int taille = 32;
        boutonVisibilite.setBounds(getWidth() - taille - 6, (getHeight() - taille) / 2, taille, taille);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, rayon, rayon);

        g2.dispose();

        super.paintComponent(g);

        if (placeholder != null && !placeholder.isEmpty() && getPassword().length == 0 && !isFocusOwner()) {
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g3.setColor(COULEUR_PLACEHOLDER);
            g3.setFont(getFont());
            FontMetrics fm = g3.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g3.drawString(placeholder, getInsets().left, y);
            g3.dispose();
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int centreX = boutonVisibilite.getX() + boutonVisibilite.getWidth() / 2;
        int centreY = boutonVisibilite.getY() + boutonVisibilite.getHeight() / 2;
        int largeur = 17;
        int hauteur = 11;
        g2.setColor(new Color(105, 112, 128));
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Ellipse2D.Double(centreX - largeur / 2.0, centreY - hauteur / 2.0, largeur, hauteur));
        g2.fill(new Ellipse2D.Double(centreX - 2, centreY - 2, 4, 4));
        if (motDePasseVisible) {
            g2.draw(new Line2D.Double(centreX - 9, centreY - 8, centreX + 9, centreY + 8));
        }
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.4f));
        g2.setColor(isFocusOwner() ? BORDURE_FOCUS : BORDURE);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, rayon, rayon);
        g2.dispose();
    }
}
