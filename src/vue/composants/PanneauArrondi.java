package vue.composants;

import javax.swing.*;
import java.awt.*;

/**
 * Panneau blanc (ou coloré) à coins arrondis, utilisé comme "carte"
 * pour englober un tableau, un formulaire ou une statistique.
 */
public class PanneauArrondi extends JPanel {

    private Color couleurFond = Color.WHITE;
    private int rayon = 25;

    public PanneauArrondi() {
        setOpaque(false);
    }

    public PanneauArrondi(Color couleurFond) {
        this.couleurFond = couleurFond;
        setOpaque(false);
    }

    public void setCouleurFond(Color couleurFond) {
        this.couleurFond = couleurFond;
        repaint();
    }

    public void setRayon(int rayon) {
        this.rayon = rayon;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(couleurFond);

        g2.fillRoundRect(
                0,
                0,
                getWidth(),
                getHeight(),
                rayon,
                rayon
        );

        g2.dispose();

    }

}
