import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * StatsPanel renders three live charts using pure Java2D:
 *   1. Spending by Category (horizontal bar chart)
 *   2. Income vs Expenses (grouped bar chart)
 *   3. Balance Over Time (line chart)
 *
 * Call refresh(transactions) whenever the transaction list changes.
 */
public class StatsPanel extends JPanel {

    private List<Transaction> transactions = new ArrayList<>();

    private static final Color[] CATEGORY_COLORS = {
        new Color(99, 132, 255),
        new Color(255, 99, 132),
        new Color(54, 205, 160),
        new Color(255, 206, 86),
        new Color(153, 102, 255),
        new Color(255, 159, 64)
    };

    private static final Color INCOME_COLOR  = new Color(54, 180, 100);
    private static final Color EXPENSE_COLOR = new Color(220, 60, 60);
    private static final Color LINE_COLOR    = new Color(70, 130, 200);
    private static final Color GRID_COLOR    = new Color(220, 220, 220);
    private static final Color BG_CARD       = Color.WHITE;
    private static final Font  TITLE_FONT    = new Font("SansSerif", Font.BOLD, 13);
    private static final Font  LABEL_FONT    = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font  SMALL_FONT    = new Font("SansSerif", Font.PLAIN, 10);

    public StatsPanel() {
        setLayout(new GridLayout(1, 3, 12, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(new Color(245, 246, 250));
    }

    /** Call this whenever transactions change to repaint all charts. */
    public void refresh(List<Transaction> updatedTransactions) {
        this.transactions = new ArrayList<>(updatedTransactions);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int gap = 12;
        int cardW = (w - gap * 4) / 3;
        int cardH = h - gap * 2;

        int x1 = gap;
        int x2 = gap * 2 + cardW;
        int x3 = gap * 3 + cardW * 2;
        int y  = gap;

        drawCard(g2, x1, y, cardW, cardH);
        drawSpendingByCategory(g2, x1, y, cardW, cardH);

        drawCard(g2, x2, y, cardW, cardH);
        drawIncomeVsExpenses(g2, x2, y, cardW, cardH);

        drawCard(g2, x3, y, cardW, cardH);
        drawBalanceOverTime(g2, x3, y, cardW, cardH);
    }

    // ─── Card background ────────────────────────────────────────────────────

    private void drawCard(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(BG_CARD);
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 12, 12));
        g2.setColor(new Color(210, 215, 225));
        g2.draw(new RoundRectangle2D.Float(x, y, w, h, 12, 12));
    }

    // ─── Chart 1: Spending by Category ──────────────────────────────────────

    private void drawSpendingByCategory(Graphics2D g2, int cx, int cy, int cw, int ch) {
        drawChartTitle(g2, "Spending by Category", cx, cy, cw);

        // Aggregate expense amounts per category
        Map<String, Double> totals = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if ("Expense".equalsIgnoreCase(t.getType())) {
                totals.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }

        int pad = 16;
        int titleH = 30;
        int chartX = cx + pad + 70; // leave room for labels on left
        int chartY = cy + titleH + pad;
        int chartW = cw - pad * 2 - 70;
        int chartH = ch - titleH - pad * 3;

        if (totals.isEmpty()) {
            drawNoData(g2, cx, cy, cw, ch);
            return;
        }

        double maxVal = totals.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
        String[] cats = totals.keySet().toArray(new String[0]);
        int barCount = cats.length;
        int barH = Math.min(30, (chartH - (barCount - 1) * 8) / barCount);
        int totalBarsH = barCount * barH + (barCount - 1) * 8;
        int startY = chartY + (chartH - totalBarsH) / 2;

        drawHorizontalGrid(g2, chartX, chartY, chartW, chartH, maxVal);

        for (int i = 0; i < barCount; i++) {
            String cat = cats[i];
            double val = totals.get(cat);
            int barW = (int) (val / maxVal * chartW);
            int by = startY + i * (barH + 8);

            Color c = CATEGORY_COLORS[i % CATEGORY_COLORS.length];
            g2.setColor(c);
            g2.fillRoundRect(chartX, by, Math.max(barW, 2), barH, 6, 6);

            // Category label on left
            g2.setFont(SMALL_FONT);
            g2.setColor(new Color(80, 80, 80));
            FontMetrics fm = g2.getFontMetrics();
            String label = cat.length() > 10 ? cat.substring(0, 9) + "." : cat;
            g2.drawString(label, chartX - fm.stringWidth(label) - 4, by + barH / 2 + fm.getAscent() / 2 - 2);

            // Value on right of bar
            g2.setColor(new Color(60, 60, 60));
            g2.drawString(String.format("$%.0f", val), chartX + barW + 4, by + barH / 2 + fm.getAscent() / 2 - 2);
        }
    }

    // ─── Chart 2: Income vs Expenses ────────────────────────────────────────

    private void drawIncomeVsExpenses(Graphics2D g2, int cx, int cy, int cw, int ch) {
        drawChartTitle(g2, "Income vs Expenses", cx, cy, cw);

        double totalIncome   = 0;
        double totalExpenses = 0;
        for (Transaction t : transactions) {
            if ("Income".equalsIgnoreCase(t.getType()))  totalIncome   += t.getAmount();
            if ("Expense".equalsIgnoreCase(t.getType())) totalExpenses += t.getAmount();
        }

        int pad    = 16;
        int titleH = 30;
        int chartX = cx + pad + 40;
        int chartY = cy + titleH + pad;
        int chartW = cw - pad * 2 - 40;
        int chartH = ch - titleH - pad * 3 - 30; // room for x-labels

        double maxVal = Math.max(Math.max(totalIncome, totalExpenses), 1);
        drawVerticalGrid(g2, chartX, chartY, chartW, chartH, maxVal);

        int barW = chartW / 5;
        int gap  = chartW / 10;

        // Income bar
        int incomeH = (int) (totalIncome  / maxVal * chartH);
        int expenseH= (int) (totalExpenses/ maxVal * chartH);

        int incomeX  = chartX + gap;
        int expenseX = chartX + gap * 2 + barW + gap;

        g2.setColor(INCOME_COLOR);
        g2.fillRoundRect(incomeX, chartY + chartH - incomeH, barW, incomeH, 6, 6);

        g2.setColor(EXPENSE_COLOR);
        g2.fillRoundRect(expenseX, chartY + chartH - expenseH, barW, expenseH, 6, 6);

        // X-axis labels
        g2.setFont(SMALL_FONT);
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(80, 80, 80));

        String incLabel = "Income";
        g2.drawString(incLabel, incomeX + (barW - fm.stringWidth(incLabel)) / 2,
                chartY + chartH + 14);

        String expLabel = "Expenses";
        g2.drawString(expLabel, expenseX + (barW - fm.stringWidth(expLabel)) / 2,
                chartY + chartH + 14);

        // Value labels above bars
        g2.setColor(new Color(50, 50, 50));
        String incVal = String.format("$%.0f", totalIncome);
        g2.drawString(incVal, incomeX + (barW - fm.stringWidth(incVal)) / 2,
                chartY + chartH - incomeH - 4);

        String expVal = String.format("$%.0f", totalExpenses);
        g2.drawString(expVal, expenseX + (barW - fm.stringWidth(expVal)) / 2,
                chartY + chartH - expenseH - 4);

        // Axis line
        g2.setColor(new Color(180, 180, 180));
        g2.drawLine(chartX, chartY + chartH, chartX + chartW, chartY + chartH);
    }

    // ─── Chart 3: Balance Over Time ──────────────────────────────────────────

    private void drawBalanceOverTime(Graphics2D g2, int cx, int cy, int cw, int ch) {
        drawChartTitle(g2, "Balance Over Time", cx, cy, cw);

        if (transactions.isEmpty()) {
            drawNoData(g2, cx, cy, cw, ch);
            return;
        }

        // Sort transactions by date, accumulate running balance
        List<Transaction> sorted = new ArrayList<>(transactions);
        sorted.sort(Comparator.comparing(Transaction::getDate));

        List<LocalDate> dates   = new ArrayList<>();
        List<Double>    balance = new ArrayList<>();
        double running = 0;

        for (Transaction t : sorted) {
            if ("Income".equalsIgnoreCase(t.getType()))       running += t.getAmount();
            else if ("Expense".equalsIgnoreCase(t.getType())) running -= t.getAmount();
            dates.add(t.getDate());
            balance.add(running);
        }

        int pad    = 16;
        int titleH = 30;
        int chartX = cx + pad + 50;
        int chartY = cy + titleH + pad;
        int chartW = cw - pad * 2 - 50;
        int chartH = ch - titleH - pad * 3 - 20;

        double minB = balance.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxB = balance.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        // Pad range slightly
        double range = Math.max(maxB - minB, 1);
        minB -= range * 0.1;
        maxB += range * 0.1;
        range = maxB - minB;

        drawVerticalGrid(g2, chartX, chartY, chartW, chartH, maxB - Math.min(minB, 0));

        // Draw zero line if balance goes negative
        if (minB < 0 && maxB > 0) {
            int zeroY = chartY + chartH - (int) ((0 - minB) / range * chartH);
            g2.setColor(new Color(200, 80, 80, 100));
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10, new float[]{4, 4}, 0));
            g2.drawLine(chartX, zeroY, chartX + chartW, zeroY);
            g2.setStroke(new BasicStroke(1));
        }

        // Convert to screen coordinates
        int n = balance.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int i = 0; i < n; i++) {
            xs[i] = chartX + (n == 1 ? chartW / 2 : i * chartW / (n - 1));
            ys[i] = chartY + chartH - (int) ((balance.get(i) - minB) / range * chartH);
        }

        // Fill area under line
        if (n > 1) {
            int zeroScreenY = chartY + chartH - (int) ((0 - minB) / range * chartH);
            zeroScreenY = Math.min(zeroScreenY, chartY + chartH);

            int[] fillX = new int[n + 2];
            int[] fillY = new int[n + 2];
            fillX[0] = xs[0]; fillY[0] = zeroScreenY;
            for (int i = 0; i < n; i++) { fillX[i+1] = xs[i]; fillY[i+1] = ys[i]; }
            fillX[n+1] = xs[n-1]; fillY[n+1] = zeroScreenY;

            g2.setColor(new Color(LINE_COLOR.getRed(), LINE_COLOR.getGreen(), LINE_COLOR.getBlue(), 40));
            g2.fillPolygon(fillX, fillY, n + 2);
        }

        // Draw line
        g2.setColor(LINE_COLOR);
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < n - 1; i++) {
            g2.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
        }
        g2.setStroke(new BasicStroke(1));

        // Dots at each point
        for (int i = 0; i < n; i++) {
            g2.setColor(Color.WHITE);
            g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
            g2.setColor(LINE_COLOR);
            g2.drawOval(xs[i] - 4, ys[i] - 4, 8, 8);
        }

        // X-axis date labels (first and last only to avoid clutter)
        g2.setFont(SMALL_FONT);
        g2.setColor(new Color(100, 100, 100));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        if (n >= 1) {
            g2.drawString(dates.get(0).format(fmt), xs[0] - 15, chartY + chartH + 14);
        }
        if (n >= 2) {
            String lastLabel = dates.get(n-1).format(fmt);
            g2.drawString(lastLabel, xs[n-1] - 15, chartY + chartH + 14);
        }

        // Y-axis: current balance label
        g2.setFont(SMALL_FONT);
        g2.setColor(new Color(80, 80, 80));
        String curLabel = String.format("$%.0f", balance.get(n - 1));
        g2.drawString(curLabel, xs[n-1] + 4, ys[n-1] + 4);
    }

    // ─── Shared helpers ──────────────────────────────────────────────────────

    private void drawChartTitle(Graphics2D g2, String title, int cx, int cy, int cw) {
        g2.setFont(TITLE_FONT);
        g2.setColor(new Color(50, 50, 70));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, cx + (cw - fm.stringWidth(title)) / 2, cy + 20);
    }

    private void drawNoData(Graphics2D g2, int cx, int cy, int cw, int ch) {
        g2.setFont(LABEL_FONT);
        g2.setColor(new Color(160, 160, 160));
        String msg = "No data yet";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, cx + (cw - fm.stringWidth(msg)) / 2, cy + ch / 2);
    }

    private void drawHorizontalGrid(Graphics2D g2, int chartX, int chartY, int chartW, int chartH, double maxVal) {
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(0.5f));
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            int x = chartX + i * chartW / gridLines;
            g2.drawLine(x, chartY, x, chartY + chartH);
        }
        g2.setStroke(new BasicStroke(1));
    }

    private void drawVerticalGrid(Graphics2D g2, int chartX, int chartY, int chartW, int chartH, double maxVal) {
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(0.5f));
        int gridLines = 4;
        g2.setFont(SMALL_FONT);
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i <= gridLines; i++) {
            int y = chartY + i * chartH / gridLines;
            g2.drawLine(chartX, y, chartX + chartW, y);

            // Y-axis value labels
            double val = maxVal * (gridLines - i) / gridLines;
            String label = val >= 1000 ? String.format("$%.0fk", val / 1000) : String.format("$%.0f", val);
            g2.setColor(new Color(150, 150, 150));
            g2.drawString(label, chartX - fm.stringWidth(label) - 4, y + fm.getAscent() / 2 - 2);
            g2.setColor(GRID_COLOR);
        }
        g2.setStroke(new BasicStroke(1));
    }
}
