package vue.composants;

import javax.swing.*;
import java.awt.*;

/**
 * Champ de texte à coins arrondis avec un léger padding intérieur
 * et un texte d'indication (placeholder) affiché quand le champ est vide.
 */
public class ChampTexteArrondi extends JTextField {

    private static final Color BORDURE = new Color(223, 227, 236);
    private static final Color BORDURE_FOCUS = new Color(41, 84, 209);
    private static final Color COULEUR_PLACEHOLDER = new Color(160, 165, 178);

    private final String placeholder;
    private int rayon = 14;

    public ChampTexteArrondi() {
        this("");
    }

    public ChampTexteArrondi(String placeholder) {
        super();
        this.placeholder = placeholder;

        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setBackground(Color.WHITE);

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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, rayon, rayon);

        g2.dispose();

        super.paintComponent(g);

        if (placeholder != null && !placeholder.isEmpty() && getText().isEmpty() && !isFocusOwner()) {
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
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.4f));
        g2.setColor(isFocusOwner() ? BORDURE_FOCUS : BORDURE);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, rayon, rayon);
        g2.dispose();
    }
}
