package vue.composants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Bouton rectangulaire à coins arrondis, dans le style visuel de BankSys.
 *
 * Deux styles disponibles :
 *  - PLEIN   : fond coloré, texte blanc (ex: "Ajouter", "Envoyer")
 *  - CONTOUR : fond blanc, bordure et texte colorés (ex: "Annuler")
 */
public class BoutonArrondi extends JButton {

    public enum Style { PLEIN, CONTOUR }

    private static final Color BLEU_PRINCIPAL = new Color(41, 84, 209);
    private static final Color BLEU_SURVOL = new Color(33, 68, 173);
    private static final Color ROUGE_PRINCIPAL = new Color(214, 69, 69);
    private static final Color ROUGE_SURVOL = new Color(184, 55, 55);

    private final Style style;
    private final Color couleurBase;
    private final Color couleurSurvol;
    private final Color couleurTexte;
    private int rayon = 18;
    private boolean survole = false;

    public BoutonArrondi(String texte) {
        this(texte, Style.PLEIN, BLEU_PRINCIPAL, BLEU_SURVOL, Color.WHITE);
    }

    public BoutonArrondi(String texte, Style style) {
        this(texte, style,
                style == Style.PLEIN ? BLEU_PRINCIPAL : Color.WHITE,
                style == Style.PLEIN ? BLEU_SURVOL : new Color(240, 243, 250),
                style == Style.PLEIN ? Color.WHITE : BLEU_PRINCIPAL);
    }

    public BoutonArrondi(String texte, Style style, Color couleurBase, Color couleurSurvol) {
        this(texte, style, couleurBase, couleurSurvol,
                style == Style.PLEIN ? Color.WHITE : BLEU_PRINCIPAL);
    }

    public BoutonArrondi(String texte, Style style, Color couleurBase, Color couleurSurvol, Color couleurTexte) {
        super(texte);
        this.style = style;
        this.couleurBase = couleurBase;
        this.couleurSurvol = couleurSurvol;
        this.couleurTexte = couleurTexte;

        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setForeground(couleurTexte);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                survole = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                survole = false;
                repaint();
            }
        });
    }

    /** Bouton rouge, utile pour les actions "Supprimer". */
    public static BoutonArrondi boutonDanger(String texte) {
        return new BoutonArrondi(texte, Style.CONTOUR, Color.WHITE, new Color(255, 235, 235), ROUGE_PRINCIPAL);
    }

    public void setRayon(int rayon) {
        this.rayon = rayon;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(survole ? couleurSurvol : couleurBase);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), rayon, rayon);

        if (style == Style.CONTOUR) {
            g2.setColor(BLEU_PRINCIPAL);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, rayon, rayon);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
