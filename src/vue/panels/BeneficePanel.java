package vue.panels;

import util.ConnexionBD;
import vue.composants.BoutonArrondi;
import vue.composants.PanneauArrondi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;

/**
 * Dashboard Administrateur — Vue synthétique complète.
 *
 * Sections :
 *   1. KPI Cards  (4 indicateurs clés)
 *   2. Graphiques (Line Chart évolution + Donut répartition statuts)
 *   3. Tableaux   (Top 5 prêts + Derniers remboursements)
 */
public class BeneficePanel extends JPanel {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(240, 243, 250);
    private static final Color NAVY        = new Color(18,  33,  73);
    private static final Color NAVY_LIGHT  = new Color(30,  52, 110);
    private static final Color ACCENT      = new Color(59, 130, 246);   // bleu vif
    private static final Color GREEN       = new Color(34, 197, 94);
    private static final Color AMBER       = new Color(251, 191, 36);
    private static final Color RED_SOFT    = new Color(239, 68,  68);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color TEXT_MUTED  = new Color(107, 114, 128);
    private static final Color BORDER_CLR  = new Color(226, 232, 240);

    // ── Widgets KPI ──────────────────────────────────────────────────────────
    private JLabel lblTotalPrete;
    private JLabel lblBenefice;
    private JLabel lblRecouvrement;
    private JLabel lblUtilisateurs;

    // ── Graphiques ───────────────────────────────────────────────────────────
    private LineChartPanel lineChart;
    private DonutChartPanel donutChart;

    // ── Tableaux ─────────────────────────────────────────────────────────────
    private DefaultTableModel modelTop5;
    private DefaultTableModel modelRemboursements;

    public BeneficePanel() {
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Conteneur principal — GridBagLayout pour que tout s'étire en largeur
        JPanel contenu = new JPanel(new GridBagLayout()) {
            // Force le contenu à prendre toute la largeur du viewport
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                Container parent = getParent();
                if (parent != null) d.width = parent.getWidth();
                return d;
            }
        };
        contenu.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets  = new Insets(0, 0, 14, 0);

        gbc.gridy = 0; contenu.add(buildEntete(),    gbc);
        gbc.gridy = 1; contenu.add(buildKpiCards(),  gbc);
        gbc.gridy = 2; contenu.add(buildGraphiques(), gbc);
        gbc.gridy = 3; contenu.add(buildTableaux(),   gbc);

        // Pousseur vertical pour coller le contenu en haut
        gbc.gridy = 4; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contenu.add(Box.createVerticalGlue(), gbc);

        JScrollPane scroll = new JScrollPane(contenu);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        rafraichir();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 0. EN-TÊTE
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildEntete() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);

        JLabel titre = new JLabel("Tableau de bord — Administration");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titre.setForeground(NAVY);

        JLabel soustitre = new JLabel("Vue synthétique en temps réel");
        soustitre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        soustitre.setForeground(TEXT_MUTED);

        JPanel textes = new JPanel(new GridLayout(2, 1, 0, 2));
        textes.setOpaque(false);
        textes.add(titre);
        textes.add(soustitre);

        BoutonArrondi btnActu = new BoutonArrondi("⟳  Actualiser");
        btnActu.addActionListener(e -> rafraichir());

        p.add(textes, BorderLayout.WEST);
        p.add(btnActu, BorderLayout.EAST);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. KPI CARDS
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildKpiCards() {
        JPanel grille = new JPanel(new GridLayout(1, 4, 12, 0));
        grille.setOpaque(false);

        // Card 1 — Volume global prêté
        JPanel c1 = kpiCard("Trésorerie prêtée", "—", ACCENT, "Ar");
        lblTotalPrete = kpiValeurLabel(c1);
        grille.add(c1);

        // Card 2 — Bénéfice net
        JPanel c2 = kpiCard("Bénéfice net global", "—", GREEN, "Ar");
        lblBenefice = kpiValeurLabel(c2);
        grille.add(c2);

        // Card 3 — Taux de recouvrement
        JPanel c3 = kpiCard("Taux de recouvrement", "—", AMBER, "%");
        lblRecouvrement = kpiValeurLabel(c3);
        grille.add(c3);

        // Card 4 — Utilisateurs
        JPanel c4 = kpiCard("Clients & agents actifs", "—", new Color(168, 85, 247), "");
        lblUtilisateurs = kpiValeurLabel(c4);
        grille.add(c4);

        return grille;
    }

    private JPanel kpiCard(String libelle, String valeurInitiale, Color accent, String unite) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Barre colorée à gauche
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);

        // Point coloré + libellé
        JLabel lblLib = new JLabel("● " + libelle);
        lblLib.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLib.setForeground(TEXT_MUTED);
        lblLib.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblVal = new JLabel(valeurInitiale);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblVal.setForeground(NAVY);
        lblVal.setAlignmentX(LEFT_ALIGNMENT);
        lblVal.setName("VALEUR");          // marqueur pour retrouver ce label

        JLabel lblUnit = new JLabel(unite);
        lblUnit.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUnit.setForeground(accent);
        lblUnit.setAlignmentX(LEFT_ALIGNMENT);

        card.add(lblLib);
        card.add(Box.createVerticalStrut(8));
        card.add(lblVal);
        card.add(Box.createVerticalStrut(2));
        card.add(lblUnit);

        // Ombre légère via border
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            new EmptyBorder(14, 14, 14, 12)
        ));

        return card;
    }

    /** Récupère le JLabel marqué "VALEUR" dans une card. */
    private JLabel kpiValeurLabel(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel && "VALEUR".equals(c.getName())) {
                return (JLabel) c;
            }
        }
        return new JLabel(); // fallback
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. GRAPHIQUES
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildGraphiques() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setMinimumSize(new Dimension(0, 320));
        row.setPreferredSize(new Dimension(0, 320));

        // ── Line Chart ─────────────────────────────────────────────────────
        lineChart = new LineChartPanel();
        row.add(encadrer("Évolution des prêts & bénéfices", lineChart));

        // ── Donut ──────────────────────────────────────────────────────────
        donutChart = new DonutChartPanel();
        row.add(encadrer("Répartition des prêts par statut", donutChart));

        return row;
    }

    private JPanel encadrer(String titre, JPanel contenu) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setMinimumSize(new Dimension(260, 280));
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel t = new JLabel(titre);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(NAVY);
        wrapper.add(t, BorderLayout.NORTH);
        wrapper.add(contenu, BorderLayout.CENTER);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. TABLEAUX
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildTableaux() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setMinimumSize(new Dimension(0, 280));
        row.setPreferredSize(new Dimension(0, 280));

        // ── Top 5 prêts ────────────────────────────────────────────────────
        String[] colTop5 = {"Prêt", "Client", "Montant prêté (Ar)", "Taux", "À rendre (Ar)"};
        modelTop5 = new DefaultTableModel(colTop5, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        row.add(encadrerTableau("Top 5 — Plus gros prêts accordés", modelTop5));

        // ── Derniers remboursements ─────────────────────────────────────────
        String[] colRembours = {"Réf.", "Prêt", "Situation", "Montant payé (Ar)", "Date"};
        modelRemboursements = new DefaultTableModel(colRembours, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        row.add(encadrerTableau("Derniers remboursements reçus", modelRemboursements));

        return row;
    }

    private JPanel encadrerTableau(String titre, DefaultTableModel model) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setMinimumSize(new Dimension(260, 240));
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel t = new JLabel(titre);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(NAVY);

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_CLR);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(NAVY);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(NAVY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR));

        // Alignement centré pour les colonnes numériques
        DefaultTableCellRenderer centrer = new DefaultTableCellRenderer();
        centrer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 2; i < model.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centrer);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setMinimumSize(new Dimension(220, 180));
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        wrapper.add(t, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHARGEMENT DES DONNÉES
    // ─────────────────────────────────────────────────────────────────────────
    private void rafraichir() {
        chargerKpis();
        chargerLineChart();
        chargerDonutChart();
        chargerTop5();
        chargerDerniersRemboursements();
    }

    /** 1. KPI Cards */
    private void chargerKpis() {
        String sql = """
            SELECT
              (SELECT COALESCE(SUM(montant_prete), 0) FROM preter)             AS total_prete,
              (SELECT COALESCE(SUM(montant_prete * taux_interet / 100), 0)
                 FROM preter)                                                   AS benefice,
              (SELECT COUNT(*) FROM client)                                     AS nb_clients,
              (SELECT COUNT(*) FROM agent)                                      AS nb_agents,
              (SELECT COALESCE(SUM(montant_paye), 0) FROM rendre)              AS total_paye,
              (SELECT COALESCE(SUM(montant_a_rendre), 0) FROM preter)          AS total_a_rendre
            """;
        try (Connection cn = ConnexionBD.getConnexion();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                BigDecimal totalPrete   = rs.getBigDecimal("total_prete");
                BigDecimal benefice     = rs.getBigDecimal("benefice");
                long nbClients          = rs.getLong("nb_clients");
                long nbAgents           = rs.getLong("nb_agents");
                BigDecimal totalPaye    = rs.getBigDecimal("total_paye");
                BigDecimal totalARendre = rs.getBigDecimal("total_a_rendre");

                lblTotalPrete.setText(formaterMontant(totalPrete));
                lblBenefice.setText(formaterMontant(benefice));
                lblUtilisateurs.setText((nbClients + nbAgents) + " (" + nbClients + " clients, " + nbAgents + " agent(s))");

                if (totalARendre != null && totalARendre.compareTo(BigDecimal.ZERO) > 0) {
                    double taux = totalPaye.doubleValue() / totalARendre.doubleValue() * 100.0;
                    lblRecouvrement.setText(String.format("%.1f", taux));
                } else {
                    lblRecouvrement.setText("0.0");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur KPIs : " + e.getMessage());
        }
    }

    /** 2a. Données pour le Line Chart (prêts & bénéfices par mois) */
    private void chargerLineChart() {
        // On récupère les 6 derniers mois de décaissements
        String sql = """
            SELECT TO_CHAR(date_pret, 'Mon') AS mois,
                   EXTRACT(MONTH FROM date_pret) AS num_mois,
                   EXTRACT(YEAR  FROM date_pret) AS num_annee,
                   SUM(montant_prete)                               AS total_prete,
                   SUM(montant_prete * taux_interet / 100)         AS benefice
              FROM preter
             GROUP BY mois, num_mois, num_annee
             ORDER BY num_annee, num_mois
             LIMIT 6
            """;

        List<String> labels  = new ArrayList<>();
        List<Double> serieP  = new ArrayList<>();
        List<Double> serieB  = new ArrayList<>();

        try (Connection cn = ConnexionBD.getConnexion();
             Statement  st = cn.createStatement();
             ResultSet  rs = st.executeQuery(sql)) {
            while (rs.next()) {
                labels.add(rs.getString("mois"));
                serieP.add(rs.getBigDecimal("total_prete").doubleValue());
                serieB.add(rs.getBigDecimal("benefice").doubleValue());
            }
        } catch (Exception e) {
            System.err.println("Erreur Line Chart : " + e.getMessage());
        }

        // Si pas encore de données sur plusieurs mois, on génère des points fictifs
        // (données réelles du dump = tout en août 2026)
        if (labels.size() < 2) {
            labels  = Arrays.asList("Mar", "Avr", "Mai", "Juin", "Juil", "Août");
            serieP  = Arrays.asList(0.0,  0.0,  0.0,  0.0,   0.0,   totalPrete());
            serieB  = Arrays.asList(0.0,  0.0,  0.0,  0.0,   0.0,   beneficeTotal());
        }

        lineChart.setData(labels, serieP, serieB);
        lineChart.repaint();
    }

    /** 2b. Données pour le Donut (statuts des prêts) */
    private void chargerDonutChart() {
        String sql = """
            SELECT situation_actuelle, COUNT(*) AS nb
              FROM v_situation_prets
             GROUP BY situation_actuelle
            """;

        Map<String, Integer> data = new LinkedHashMap<>();
        try (Connection cn = ConnexionBD.getConnexion();
             Statement  st = cn.createStatement();
             ResultSet  rs = st.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("situation_actuelle"), rs.getInt("nb"));
            }
        } catch (Exception e) {
            System.err.println("Erreur Donut : " + e.getMessage());
        }

        donutChart.setData(data);
        donutChart.repaint();
    }

    /** 3a. Top 5 prêts */
    private void chargerTop5() {
        modelTop5.setRowCount(0);
        String sql = """
            SELECT p.num_pret,
                   c.nom || ' ' || COALESCE(c.prenoms, '') AS client,
                   p.montant_prete,
                   p.taux_interet,
                   p.montant_a_rendre
              FROM preter p
              JOIN client c ON c.num_compte = p.num_compte
             ORDER BY p.montant_prete DESC
             LIMIT 5
            """;
        try (Connection cn = ConnexionBD.getConnexion();
             Statement  st = cn.createStatement();
             ResultSet  rs = st.executeQuery(sql)) {
            while (rs.next()) {
                modelTop5.addRow(new Object[]{
                    rs.getString("num_pret"),
                    rs.getString("client").trim(),
                    formaterMontant(rs.getBigDecimal("montant_prete")),
                    rs.getDouble("taux_interet") + " %",
                    formaterMontant(rs.getBigDecimal("montant_a_rendre"))
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur Top 5 : " + e.getMessage());
        }
    }

    /** 3b. Derniers remboursements */
    private void chargerDerniersRemboursements() {
        modelRemboursements.setRowCount(0);
        String sql = """
            SELECT r.num_rendu, r.num_pret, r.situation,
                   r.montant_paye, r.date_rendu
              FROM rendre r
             ORDER BY r.date_rendu DESC, r.num_rendu DESC
             LIMIT 8
            """;
        try (Connection cn = ConnexionBD.getConnexion();
             Statement  st = cn.createStatement();
             ResultSet  rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String sit = rs.getString("situation");
                modelRemboursements.addRow(new Object[]{
                    rs.getString("num_rendu"),
                    rs.getString("num_pret"),
                    sit,
                    formaterMontant(rs.getBigDecimal("montant_paye")),
                    rs.getDate("date_rendu").toString()
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur Remboursements : " + e.getMessage());
        }
    }

    // ── Helpers pour valeurs scalaires (utilisés dans fallback) ──────────────
    private double totalPrete() {
        try (Connection cn = ConnexionBD.getConnexion();
             Statement  st = cn.createStatement();
             ResultSet  rs = st.executeQuery("SELECT COALESCE(SUM(montant_prete),0) AS v FROM preter")) {
            if (rs.next()) return rs.getDouble("v");
        } catch (Exception ignored) {}
        return 0;
    }
    private double beneficeTotal() {
        try (Connection cn = ConnexionBD.getConnexion();
             Statement  st = cn.createStatement();
             ResultSet  rs = st.executeQuery(
                 "SELECT COALESCE(SUM(montant_prete*taux_interet/100),0) AS v FROM preter")) {
            if (rs.next()) return rs.getDouble("v");
        } catch (Exception ignored) {}
        return 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITAIRES
    // ─────────────────────────────────────────────────────────────────────────
    private String formaterMontant(BigDecimal montant) {
        if (montant == null) return "0 Ar";
        DecimalFormatSymbols sym = new DecimalFormatSymbols();
        sym.setGroupingSeparator(' ');
        DecimalFormat df = new DecimalFormat("#,##0", sym);
        return df.format(montant) + " Ar";
    }

    // =========================================================================
    // COMPOSANT INTERNE — Line Chart
    // =========================================================================
    static class LineChartPanel extends JPanel {

        private List<String> labels  = Collections.emptyList();
        private List<Double> serieA  = Collections.emptyList(); // prêts
        private List<Double> serieB  = Collections.emptyList(); // bénéfices

        private static final Color C_PRET     = new Color(59, 130, 246);
        private static final Color C_BENEFICE = new Color(34, 197, 94);

        LineChartPanel() {
            setOpaque(false);
            setMinimumSize(new Dimension(220, 230));
            setPreferredSize(new Dimension(220, 230));
        }

        void setData(List<String> labels, List<Double> prets, List<Double> benefices) {
            this.labels = labels;
            this.serieA = prets;
            this.serieB = benefices;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (labels.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int W = getWidth(), H = getHeight();
            if (W < 120 || H < 100) {
                g2.dispose();
                return;
            }
            int padL = 64, padR = 20, padT = 20, padB = 40;
            int chartW = W - padL - padR;
            int chartH = H - padT - padB;

            // Max pour normaliser
            double maxVal = serieA.stream().mapToDouble(Double::doubleValue).max().orElse(1);
            maxVal = Math.max(maxVal, serieB.stream().mapToDouble(Double::doubleValue).max().orElse(1));
            if (maxVal == 0) maxVal = 1;

            int n = labels.size();
            double stepX = (double) chartW / Math.max(n - 1, 1);

            // Grille horizontale
            g2.setStroke(new BasicStroke(1f));
            for (int i = 0; i <= 4; i++) {
                int y = padT + (int)(chartH * (1 - (double) i / 4));
                g2.setColor(new Color(226, 232, 240));
                g2.drawLine(padL, y, padL + chartW, y);
                // Label axe Y
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(new Color(107, 114, 128));
                String yLabel = formatCompact(maxVal * i / 4);
                g2.drawString(yLabel, 2, y + 4);
            }

            // Aire remplie — Prêts
            int[] xPts = new int[n + 2];
            int[] yPts = new int[n + 2];
            for (int i = 0; i < n; i++) {
                xPts[i]   = padL + (int)(i * stepX);
                yPts[i]   = padT + (int)(chartH * (1 - serieA.get(i) / maxVal));
            }
            xPts[n]   = padL + chartW;
            yPts[n]   = padT + chartH;
            xPts[n+1] = padL;
            yPts[n+1] = padT + chartH;
            g2.setColor(new Color(59, 130, 246, 40));
            g2.fillPolygon(xPts, yPts, n + 2);

            // Courbe Prêts
            g2.setColor(C_PRET);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawCurve(g2, n, padL, padT, stepX, chartH, maxVal, serieA);

            // Courbe Bénéfices
            g2.setColor(C_BENEFICE);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    10f, new float[]{6f, 4f}, 0f));
            drawCurve(g2, n, padL, padT, stepX, chartH, maxVal, serieB);

            // Points et labels axe X
            for (int i = 0; i < n; i++) {
                int x = padL + (int)(i * stepX);

                // Point prêt
                int yA = padT + (int)(chartH * (1 - serieA.get(i) / maxVal));
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.fillOval(x - 4, yA - 4, 8, 8);
                g2.setColor(C_PRET);
                g2.drawOval(x - 4, yA - 4, 8, 8);

                // Point bénéfice
                int yB = padT + (int)(chartH * (1 - serieB.get(i) / maxVal));
                g2.setColor(Color.WHITE);
                g2.fillOval(x - 3, yB - 3, 6, 6);
                g2.setColor(C_BENEFICE);
                g2.drawOval(x - 3, yB - 3, 6, 6);

                // Label mois
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(new Color(107, 114, 128));
                FontMetrics fm = g2.getFontMetrics();
                int lw = fm.stringWidth(labels.get(i));
                g2.drawString(labels.get(i), x - lw / 2, H - 10);
            }

            // Légende
            drawLegende(g2, W - 190, padT);

            g2.dispose();
        }

        private void drawCurve(Graphics2D g2, int n, int padL, int padT,
                                double stepX, int chartH, double maxVal, List<Double> serie) {
            Path2D path = new Path2D.Double();
            for (int i = 0; i < n; i++) {
                int x = padL + (int)(i * stepX);
                int y = padT + (int)(chartH * (1 - serie.get(i) / maxVal));
                if (i == 0) path.moveTo(x, y);
                else        path.lineTo(x, y);
            }
            g2.draw(path);
        }

        private void drawLegende(Graphics2D g2, int x, int y) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            // Prêts
            g2.setColor(C_PRET);
            g2.fillRect(x, y, 14, 3);
            g2.setColor(new Color(55, 65, 81));
            g2.drawString("Prêts accordés", x + 18, y + 5);
            // Bénéfices
            g2.setColor(C_BENEFICE);
            g2.fillRect(x, y + 16, 14, 3);
            g2.setColor(new Color(55, 65, 81));
            g2.drawString("Bénéfices", x + 18, y + 21);
        }

        private String formatCompact(double v) {
            if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000);
            if (v >= 1_000)     return String.format("%.0fK", v / 1_000);
            return String.format("%.0f", v);
        }
    }

    // =========================================================================
    // COMPOSANT INTERNE — Donut Chart
    // =========================================================================
    static class DonutChartPanel extends JPanel {

        private Map<String, Integer> data = Collections.emptyMap();

        private static final Color[] COULEURS = {
            new Color(34, 197,  94),   // Tout payé  — vert
            new Color(59, 130, 246),   // Paye en partie — bleu
            new Color(239, 68,  68),   // Non remboursé — rouge
            new Color(251, 191, 36),   // autre — ambre
        };

        DonutChartPanel() {
            setOpaque(false);
            setMinimumSize(new Dimension(260, 230));
            setPreferredSize(new Dimension(260, 230));
        }

        void setData(Map<String, Integer> data) { this.data = data; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                g2.setColor(new Color(156, 163, 175));
                g2.drawString("Aucune donnée", getWidth() / 2 - 45, getHeight() / 2);
                g2.dispose();
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int total = data.values().stream().mapToInt(Integer::intValue).sum();
            if (total == 0) { g2.dispose(); return; }

            int W = getWidth(), H = getHeight();
            if (W < 180 || H < 100) {
                g2.dispose();
                return;
            }
            int size    = Math.min(W / 2, H) - 30;
            int cx      = size / 2 + 20;
            int cy      = H / 2;
            int outer   = size;
            int inner   = (int)(size * 0.55);

            // Arc
            double startAngle = -90;
            int idx = 0;
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(data.entrySet());
            for (Map.Entry<String, Integer> e : entries) {
                double sweep = 360.0 * e.getValue() / total;
                g2.setColor(COULEURS[idx % COULEURS.length]);
                g2.fill(new Arc2D.Double(cx - outer/2.0, cy - outer/2.0, outer, outer,
                        startAngle, sweep, Arc2D.PIE));
                startAngle += sweep;
                idx++;
            }

            // Trou central (donut)
            g2.setColor(CARD_BG);
            g2.fillOval(cx - inner/2, cy - inner/2, inner, inner);

            // Texte central
            g2.setColor(NAVY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            String totalStr = String.valueOf(total);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(totalStr, cx - fm.stringWidth(totalStr)/2, cy + 6);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(TEXT_MUTED);
            String sub = "prêts";
            g2.drawString(sub, cx - g2.getFontMetrics().stringWidth(sub)/2, cy + 20);

            // Légende à droite
            int legendX = cx + outer/2 + 20;
            int legendY = cy - (entries.size() * 22) / 2 + 10;
            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<String, Integer> e = entries.get(i);
                int pct = (int) Math.round(100.0 * e.getValue() / total);
                Color c = COULEURS[i % COULEURS.length];

                // Pastille
                g2.setColor(c);
                g2.fillRoundRect(legendX, legendY + i*26 - 10, 12, 12, 4, 4);

                // Texte
                g2.setColor(NAVY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString(pct + "%", legendX + 18, legendY + i*26);
                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(e.getKey(), legendX + 18, legendY + i*26 + 14);
            }

            g2.dispose();
        }
    }

    // ─── Couleur TEXT_MUTED accessible dans les inner classes ────────────────
    private static final Color TEXT_MUTED_REF = new Color(107, 114, 128);
}