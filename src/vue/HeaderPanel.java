package vue;

import javax.swing.*;
import java.awt.*;

public class HeaderPanel extends JPanel {

    private final JLabel titre;

    public HeaderPanel() {

        setPreferredSize(new Dimension(0, 70));

        setBackground(Color.WHITE);

        setLayout(new BorderLayout());

        titre = new JLabel("Gestion des prêts");

        titre.setFont(new Font("Segoe UI", Font.BOLD, 22));

        titre.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        add(titre, BorderLayout.WEST);

        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 233, 240)));

    }

    public void setTitre(String texte) {
        titre.setText(texte);
    }

}
