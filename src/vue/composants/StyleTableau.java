package vue.composants;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class StyleTableau {

    private static final Color FOND_LIGNE = Color.WHITE;
    private static final Color FOND_LIGNE_ALTERNEE = new Color(247, 249, 253);
    private static final Color FOND_SURVOL = new Color(235, 242, 255);
    private static final Color FOND_SELECTION = new Color(211, 226, 255);
    private static final Color TEXTE = new Color(42, 48, 61);
    private static final Color BORDURE = new Color(228, 232, 240);
    private static final Color FOND_ENTETE = new Color(31, 52, 91);

    private StyleTableau() {
    }

    public static void appliquer(JTable table) {
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(TEXTE);
        table.setBackground(FOND_LIGNE);
        table.setSelectionBackground(FOND_SELECTION);
        table.setSelectionForeground(TEXTE);
        table.setGridColor(BORDURE);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setRowMargin(0);

        JTableHeader entete = table.getTableHeader();
        entete.setFont(new Font("Segoe UI", Font.BOLD, 13));
        entete.setForeground(Color.WHITE);
        entete.setBackground(FOND_ENTETE);
        entete.setOpaque(true);
        entete.setPreferredSize(new Dimension(entete.getPreferredSize().width, 42));
        entete.setReorderingAllowed(false);
        entete.setBorder(BorderFactory.createEmptyBorder());
        entete.setDefaultRenderer(new RenduEntete());

        table.setDefaultRenderer(Object.class, new RenduLigne());
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int ligne = table.rowAtPoint(e.getPoint());
                Object ancienneLigne = table.getClientProperty("ligneSurvolee");
                if (!(ancienneLigne instanceof Integer) || ligne != (Integer) ancienneLigne) {
                    table.putClientProperty("ligneSurvolee", ligne);
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                table.putClientProperty("ligneSurvolee", -1);
                table.repaint();
            }
        });
    }

    private static final class RenduEntete extends javax.swing.table.DefaultTableCellRenderer {
        RenduEntete() {
            setHorizontalAlignment(SwingConstants.LEFT);
            setVerticalAlignment(SwingConstants.CENTER);
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, hasFocus, row, column);
            setBackground(FOND_ENTETE);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            return this;
        }
    }

    private static final class RenduLigne extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean hasFocus, int row, int column) {
            Component composant = super.getTableCellRendererComponent(
                    table, value, selected, hasFocus, row, column);
            int ligneSurvolee = table.getClientProperty("ligneSurvolee") instanceof Integer
                    ? (Integer) table.getClientProperty("ligneSurvolee") : -1;
            if (selected) {
                composant.setBackground(FOND_SELECTION);
            } else if (row == ligneSurvolee) {
                composant.setBackground(FOND_SURVOL);
            } else {
                composant.setBackground(row % 2 == 0 ? FOND_LIGNE : FOND_LIGNE_ALTERNEE);
            }
            composant.setForeground(TEXTE);
            return composant;
        }
    }
}
