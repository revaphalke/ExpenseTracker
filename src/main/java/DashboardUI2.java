import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

public class DashboardUI2 extends JFrame {

    private final LocalStore store;
    private final int userId;
    private String userName = "User", activeMonth;
    private boolean syncingMonth = false;

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private DefaultTableModel transactionModel, recentTableModel;

    private JLabel totalBalanceLabel, incomeLabel, expenseLabel, budgetLeftLabel, smartAdviceLabel;
    private JProgressBar monthlyProgressBar;
    private JLabel monthlyProgressPercentLabel, monthlyProgressAmountLabel;
    private JLabel incomeTodayLabel, expenseTodayLabel, netTodayLabel;
    private JLabel reportIncomeLabel, reportExpenseLabel, reportSavingsLabel, reportCountLabel;

    private JComboBox<String> addMonthBox, historyMonthBox, reportMonthBox;
    private JTable historyTable;
    private JTextField historyFromDate, historyToDate, historySearchField;
    private JComboBox<String> historyCategoryFilter, historyTypeFilter;

    private final DefaultCategoryDataset reportBarDataset = new DefaultCategoryDataset();
    private final DefaultCategoryDataset reportLineDataset = new DefaultCategoryDataset();
    private JFreeChart barChart, lineChart;

    private final Color COLOR_BG = new Color(13, 18, 28), COLOR_PANEL = new Color(20, 27, 41), COLOR_CARD_BG = new Color(22, 31, 48), COLOR_TOPBAR = new Color(11, 15, 23), COLOR_BORDER = new Color(34, 46, 68);
    private final Color COLOR_ACCENT = new Color(74, 108, 247), COLOR_GREEN = new Color(46, 204, 113), COLOR_RED = new Color(235, 77, 75), COLOR_ORANGE = new Color(243, 156, 18);
    private final Color COLOR_TEXT_PRIMARY = new Color(240, 243, 246), COLOR_TEXT_MUTED = new Color(130, 145, 170);

    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28), FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 24), FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);
    private final Font FONT_CARD_TITLE = new Font("Segoe UI", Font.BOLD, 15), FONT_LABEL_BOLD = new Font("Segoe UI", Font.BOLD, 14), FONT_BTN = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13), FONT_MUTED_SMALL = new Font("Segoe UI", Font.PLAIN, 12), FONT_QUOTE = new Font("Segoe UI", Font.ITALIC, 14);

    private static final String RUPEE = "\u20B9";

public DashboardUI2(LocalStore store, int userId) {
    this.store=store; this.userId=userId; this.activeMonth=DBUtil.defaultMonth();
    loadUserName();

    setTitle("Personal Finance Manager"); setSize(1300, 800); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null); getContentPane().setBackground(COLOR_BG); setLayout(new BorderLayout());
    addWindowListener(new WindowAdapter(){ @Override public void windowClosing(WindowEvent e){} });

    addMonthBox=new JComboBox<>(DBUtil.MONTHS); historyMonthBox=new JComboBox<>(DBUtil.MONTHS); reportMonthBox=new JComboBox<>(DBUtil.MONTHS);
    addMonthBox.setSelectedItem(activeMonth); historyMonthBox.setSelectedItem(activeMonth); reportMonthBox.setSelectedItem(activeMonth);

    transactionModel=new DefaultTableModel(new String[]{"ID","Date","Type","Category","Description","Payment Method","Amount"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
    recentTableModel=new DefaultTableModel(new String[]{"Date","Category","Amount","Type"},0){@Override public boolean isCellEditable(int r,int c){return false;}};

    JPanel topPanel=new JPanel(new BorderLayout()); topPanel.setBackground(COLOR_TOPBAR); topPanel.setBorder(new EmptyBorder(12,20,12,20));
    JButton logout=createButton("Logout",COLOR_RED,Color.WHITE,FONT_BTN,new EmptyBorder(6,18,6,18)); logout.addActionListener(e->doLogout());
    topPanel.add(lbl("Welcome, "+userName,FONT_HEADER,COLOR_TEXT_PRIMARY),BorderLayout.WEST);
    topPanel.add(lbl("\"Do not save what is left after spending, but spend what is left after saving.\"",FONT_QUOTE,COLOR_ACCENT,SwingConstants.CENTER),BorderLayout.CENTER);
    topPanel.add(logout,BorderLayout.EAST); add(topPanel,BorderLayout.NORTH);

    JPanel sidebar=new JPanel(); sidebar.setBackground(COLOR_PANEL); sidebar.setPreferredSize(new Dimension(220,0)); sidebar.setLayout(new BoxLayout(sidebar,BoxLayout.Y_AXIS)); sidebar.setBorder(new EmptyBorder(20,10,20,10));
    String[] menu={"Dashboard","Add Transaction","Transaction History","Monthly Report","Settings"};
    for(int i=0;i<menu.length;i++){ JButton btn=createSidebarButton(menu[i],i==0); String cardName=menu[i]; btn.addActionListener(e->{
        if(cardName.equals("Dashboard"))loadDashboardData(); else if(cardName.equals("Transaction History"))loadTransactionHistoryData(); else if(cardName.equals("Monthly Report"))loadMonthlyReportData();
        cardLayout.show(contentPanel,cardName); }); sidebar.add(btn); sidebar.add(Box.createRigidArea(new Dimension(0,5))); }
    add(sidebar,BorderLayout.WEST);

    cardLayout=new CardLayout(); contentPanel=new JPanel(cardLayout); contentPanel.setBackground(COLOR_BG);
    contentPanel.add(createDashboardHomePanel(),"Dashboard"); contentPanel.add(createAddTransactionPanel(),"Add Transaction");
    contentPanel.add(createTransactionHistoryPanel(),"Transaction History"); contentPanel.add(createMonthlyReportPanel(),"Monthly Report"); contentPanel.add(createSettingsPanel(),"Settings"); add(contentPanel,BorderLayout.CENTER);

    ActionListener monthListener=e->{ if(e.getSource() instanceof JComboBox){Object sel=((JComboBox<?>)e.getSource()).getSelectedItem();if(sel!=null)setActiveMonth(sel.toString());} };
    addMonthBox.addActionListener(monthListener); historyMonthBox.addActionListener(monthListener); reportMonthBox.addActionListener(monthListener);
    refreshAll();
}


    private JPanel createDashboardHomePanel() {
        JPanel center = new JPanel(new BorderLayout(15, 15)); center.setBackground(COLOR_BG); center.setBorder(new EmptyBorder(15, 15, 15, 15));
        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 15)); cards.setOpaque(false);

        totalBalanceLabel = lbl(money(0), FONT_SUBTITLE, COLOR_ACCENT); incomeLabel = lbl(money(0), FONT_SUBTITLE, COLOR_GREEN);
        expenseLabel = lbl(money(0), FONT_SUBTITLE, COLOR_RED); budgetLeftLabel = lbl(money(0), FONT_SUBTITLE, COLOR_TEXT_PRIMARY);

        cards.add(createStatCard("Total Net Balance", totalBalanceLabel, "Cash + Bank Accounts", 15, 18, 15, 18));
        cards.add(createStatCard("Total Income", incomeLabel, "Total Inflow", 15, 18, 15, 18));
        cards.add(createStatCard("Total Expense", expenseLabel, "Total Outflow", 15, 18, 15, 18));
        cards.add(createStatCard("Monthly Budget Left", budgetLeftLabel, "Target Limit", 15, 18, 15, 18));
        center.add(cards, BorderLayout.NORTH);

        JPanel bodyGrid = new JPanel(new GridBagLayout()); bodyGrid.setOpaque(false); GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.BOTH;

        JPanel recentPanel = new JPanel(new BorderLayout(0, 12)); recentPanel.setBackground(COLOR_PANEL);
        recentPanel.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(15, 18, 15, 18)));
        JPanel recentHeader = new JPanel(new BorderLayout()); recentHeader.setOpaque(false);
        recentHeader.add(lbl("Recent Activity", FONT_HEADER, COLOR_TEXT_PRIMARY), BorderLayout.WEST);
        recentHeader.add(lbl("Latest Transactions", FONT_REGULAR, COLOR_TEXT_MUTED), BorderLayout.EAST);

        JTable recentTable = new JTable(recentTableModel); styleTable(recentTable);
        JScrollPane recentScroll = new JScrollPane(recentTable); recentScroll.getViewport().setBackground(COLOR_PANEL); recentScroll.setBorder(BorderFactory.createEmptyBorder());
        recentPanel.add(recentHeader, BorderLayout.NORTH); recentPanel.add(recentScroll, BorderLayout.CENTER);

        JPanel rightCol = new JPanel(); rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS)); rightCol.setOpaque(false);
        JPanel progressCard = new JPanel(new GridBagLayout());
        progressCard.setBackground(COLOR_PANEL); progressCard.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(14, 16, 14, 16)));
        GridBagConstraints pg = new GridBagConstraints(); pg.insets = new Insets(5, 5, 5, 5); pg.fill = GridBagConstraints.HORIZONTAL; pg.weightx = 1.0;
        pg.gridx = 0; pg.gridy = 0;
        JLabel progressTitle = lbl("Monthly Target Usage", FONT_CARD_TITLE, COLOR_TEXT_PRIMARY);
        progressCard.add(progressTitle, pg);
        monthlyProgressPercentLabel = lbl("0%", FONT_CARD_TITLE, COLOR_ORANGE, SwingConstants.RIGHT);
        pg.gridx = 1; pg.weightx = 0.0; progressCard.add(monthlyProgressPercentLabel, pg);
        monthlyProgressBar = new JProgressBar(0, 100); monthlyProgressBar.setStringPainted(false); monthlyProgressBar.setPreferredSize(new Dimension(220, 14));
        pg.gridx = 0; pg.gridy = 1; pg.gridwidth = 2; pg.weightx = 1.0; progressCard.add(monthlyProgressBar, pg);
        monthlyProgressAmountLabel = lbl(money(0) + " spent", FONT_MUTED_SMALL, COLOR_TEXT_MUTED, SwingConstants.CENTER);
        pg.gridx = 0; pg.gridy = 2; pg.gridwidth = 2; progressCard.add(monthlyProgressAmountLabel, pg);
        JButton setTargetButton = createButton("Set Target", COLOR_ACCENT, Color.WHITE, FONT_BTN, new EmptyBorder(9, 24, 9, 24));
        setTargetButton.addActionListener(e -> cardLayout.show(contentPanel, "Settings"));
        pg.gridx = 0; pg.gridy = 3; pg.gridwidth = 2; pg.weightx = 0.0; pg.fill = GridBagConstraints.NONE; progressCard.add(setTargetButton, pg);

        smartAdviceLabel = lbl("", FONT_MUTED_SMALL, COLOR_TEXT_MUTED);
        smartAdviceLabel.setVerticalAlignment(SwingConstants.TOP);
        smartAdviceLabel.setVisible(false);
        JPanel advicePanel = new JPanel(new BorderLayout()); advicePanel.setOpaque(false); advicePanel.setBorder(new EmptyBorder(10, 2, 2, 2)); advicePanel.add(smartAdviceLabel, BorderLayout.CENTER); recentPanel.add(advicePanel, BorderLayout.SOUTH);

        incomeTodayLabel = lbl(money(0), new Font("Segoe UI", Font.BOLD, 20), COLOR_GREEN);
        expenseTodayLabel = lbl(money(0), new Font("Segoe UI", Font.BOLD, 20), COLOR_RED);
        netTodayLabel = lbl(money(0), new Font("Segoe UI", Font.BOLD, 20), COLOR_TEXT_PRIMARY);

        rightCol.add(progressCard); rightCol.add(Box.createRigidArea(new Dimension(0, 15)));
        rightCol.add(createStatCard("Today's Inflow", incomeTodayLabel, null, 14, 18, 14, 18)); rightCol.add(Box.createRigidArea(new Dimension(0, 10)));
        rightCol.add(createStatCard("Today's Outflow", expenseTodayLabel, null, 14, 18, 14, 18)); rightCol.add(Box.createRigidArea(new Dimension(0, 10)));
        rightCol.add(createStatCard("Today's Net Flow", netTodayLabel, null, 14, 18, 14, 18));

        gbc.weightx = 0.68; gbc.weighty = 1.0; gbc.gridx = 0; bodyGrid.add(recentPanel, gbc);
        gbc.weightx = 0.32; gbc.gridx = 1; gbc.insets = new Insets(0, 15, 0, 0); bodyGrid.add(rightCol, gbc);

        center.add(bodyGrid, BorderLayout.CENTER); return center;
    }

    private JPanel createAddTransactionPanel() {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(COLOR_BG);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(13, 16, 13, 16); gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(lbl("Add Transaction", FONT_TITLE, COLOR_TEXT_PRIMARY, SwingConstants.CENTER), gbc); gbc.gridwidth = 1;

        JRadioButton income = new JRadioButton("Income"), expense = new JRadioButton("Expense", true);
        for (JRadioButton rb : new JRadioButton[]{income, expense}) { rb.setFont(FONT_LABEL_BOLD); rb.setBackground(COLOR_BG); rb.setForeground(COLOR_TEXT_PRIMARY); }
        ButtonGroup group = new ButtonGroup(); group.add(income); group.add(expense);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0)); radioPanel.setBackground(COLOR_BG); radioPanel.add(income); radioPanel.add(expense);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(lbl("Transaction Type:", FONT_LABEL_BOLD, COLOR_TEXT_PRIMARY), gbc); gbc.gridx = 1; panel.add(radioPanel, gbc);

        JTextField amountField = createTextField(20, FONT_LABEL_BOLD, 40);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(lbl("Amount:", FONT_LABEL_BOLD, COLOR_TEXT_PRIMARY), gbc); gbc.gridx = 1; panel.add(amountField, gbc);

        String[] expenseCategories = {"Food", "Transport", "Shopping", "Bills", "Entertainment"}, incomeCategories = {"Salary", "Business", "Investment", "Bonus"};
        JComboBox<String> categoryBox = createComboBox(expenseCategories, FONT_LABEL_BOLD, 40);

        income.addActionListener(e -> { categoryBox.removeAllItems(); for (String s : incomeCategories) categoryBox.addItem(s); });
        expense.addActionListener(e -> { categoryBox.removeAllItems(); for (String s : expenseCategories) categoryBox.addItem(s); });

        gbc.gridx = 0; gbc.gridy = 3; panel.add(lbl("Category:", FONT_LABEL_BOLD, COLOR_TEXT_PRIMARY), gbc); gbc.gridx = 1; panel.add(categoryBox, gbc);

        JTextField dateField = createTextField(DBUtil.today(), FONT_LABEL_BOLD, 40);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(lbl("Date:", FONT_LABEL_BOLD, COLOR_TEXT_PRIMARY), gbc); gbc.gridx = 1; panel.add(dateField, gbc);

        JTextArea description = new JTextArea(4, 20); description.setFont(FONT_LABEL_BOLD); description.setBackground(COLOR_TOPBAR);
        description.setForeground(COLOR_TEXT_PRIMARY); description.setCaretColor(COLOR_TEXT_PRIMARY); description.setLineWrap(true); description.setWrapStyleWord(true);
        description.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        JScrollPane scroll = new JScrollPane(description); scroll.setBorder(new LineBorder(COLOR_BORDER, 1));

        gbc.gridx = 0; gbc.gridy = 5; panel.add(lbl("Description:", FONT_LABEL_BOLD, COLOR_TEXT_PRIMARY), gbc); gbc.gridx = 1; panel.add(scroll, gbc);

        JComboBox<String> payment = createComboBox(new String[]{"Cash", "UPI", "Debit Card", "Credit Card", "Net Banking"}, FONT_LABEL_BOLD, 40);
        gbc.gridx = 0; gbc.gridy = 6; panel.add(lbl("Payment Method:", FONT_LABEL_BOLD, COLOR_TEXT_PRIMARY), gbc); gbc.gridx = 1; panel.add(payment, gbc);

        addMonthBox.setFont(FONT_LABEL_BOLD); addMonthBox.setBackground(COLOR_TOPBAR); addMonthBox.setForeground(COLOR_TEXT_PRIMARY);
        addMonthBox.setPreferredSize(new Dimension(addMonthBox.getPreferredSize().width, 40));

        gbc.gridx = 0; gbc.gridy = 7; panel.add(lbl("Save to Month:", FONT_LABEL_BOLD, COLOR_TEXT_PRIMARY), gbc); gbc.gridx = 1; panel.add(addMonthBox, gbc);

        JButton save = createButton("Save", COLOR_GREEN, Color.WHITE, FONT_LABEL_BOLD, new EmptyBorder(8, 22, 8, 22));
        JButton clear = createButton("Clear", COLOR_RED, Color.WHITE, FONT_LABEL_BOLD, new EmptyBorder(8, 22, 8, 22));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); btnPanel.setBackground(COLOR_BG); btnPanel.add(save); btnPanel.add(clear);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2; panel.add(btnPanel, gbc);

        clear.addActionListener(e -> {
            expense.setSelected(true); categoryBox.removeAllItems();
            for (String s : expenseCategories) categoryBox.addItem(s);
            amountField.setText(""); dateField.setText(DBUtil.today()); description.setText(""); payment.setSelectedIndex(0);
        });
        save.addActionListener(e -> saveTransaction(panel, income, amountField, categoryBox, dateField, description, payment));
        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel outer = new JPanel(new BorderLayout(15, 15)); outer.setBackground(COLOR_BG); outer.setBorder(new EmptyBorder(20, 20, 20, 20));
        outer.add(lbl("System & Profile Settings Center", new Font("Segoe UI", Font.BOLD, 22), COLOR_TEXT_PRIMARY), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 15, 15)); grid.setOpaque(false);

        JPanel card1 = createSettingsCard("Account & Profile Information");
        card1.add(createFormRow("Display Name:", createTextField(userName, FONT_REGULAR, -1)));
        card1.add(createFormRow("Email Address:", createTextField("", FONT_REGULAR, -1)));
        card1.add(createFormRow("Default Currency:", createComboBox(new String[]{"INR (\u20B9)", "USD ($)", "EUR (\u20AC)", "GBP (\u00A3)"}, FONT_REGULAR, -1)));

        JPanel card2 = createSettingsCard("Financial Targets & Limits");
        card2.add(createFormRow("Monthly Expense Limit (\u20B9):", createTextField("0.0", FONT_REGULAR, -1)));
        JTextField savingsGoalField = createTextField(String.valueOf(store.getMonthlyTarget(userId)), FONT_REGULAR, -1);
        card2.add(createFormRow("Monthly Savings Goal (\u20B9):", savingsGoalField));
        JButton saveTargetBtn = createButton("Save Target", COLOR_ACCENT, Color.WHITE, FONT_BTN, new EmptyBorder(6, 14, 6, 14));
        JPanel targetSaveRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); targetSaveRow.setOpaque(false); targetSaveRow.add(saveTargetBtn);
        card2.add(targetSaveRow);
        card2.add(createFormRow("Budget Calculation Cycle:", createComboBox(new String[]{"Monthly (1st-31st)", "Bi-Weekly", "Custom Cycle"}, FONT_REGULAR, -1)));
        saveTargetBtn.addActionListener(e -> saveMonthlyTarget(savingsGoalField));

        JPanel card3 = createSettingsCard("Notification & System Alerts");
        card3.add(createCheckBox("Receive bill payment reminders", true)); card3.add(Box.createRigidArea(new Dimension(0, 10)));
        card3.add(createCheckBox("Alert when exceeding 80% of budget limit", true)); card3.add(Box.createRigidArea(new Dimension(0, 10)));
        card3.add(createCheckBox("Weekly financial report summary email", false));

        JPanel card4 = createSettingsCard("Data Management & Storage");
        JButton resetAllBtn = createButton("Reset All Transactions", COLOR_RED, Color.WHITE, FONT_BTN, new EmptyBorder(6, 14, 6, 14));
        JButton savePrefBtn = createButton("Save All Preferences", COLOR_ACCENT, Color.WHITE, FONT_BTN, new EmptyBorder(6, 14, 6, 14));

        resetAllBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to permanently delete ALL your transactions across all months?\nThis action cannot be undone!", "Confirm Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                try { store.clearTransactions(userId); JOptionPane.showMessageDialog(this, "All transactions have been successfully reset to zero!"); refreshAll(); } catch (Exception ex) { showError("Failed to reset transactions", ex); }
            }
        });
        savePrefBtn.addActionListener(e -> saveMonthlyTarget(savingsGoalField));

        card4.add(resetAllBtn); card4.add(Box.createRigidArea(new Dimension(0, 12))); card4.add(savePrefBtn);
        grid.add(card1); grid.add(card2); grid.add(card3); grid.add(card4); outer.add(grid, BorderLayout.CENTER); return outer;
    }

    private void saveMonthlyTarget(JTextField savingsGoalField) {
        try {
            double target = Double.parseDouble(savingsGoalField.getText().trim());
            if(target < 0) throw new NumberFormatException();
            store.setMonthlyTarget(userId, target);
            JOptionPane.showMessageDialog(this, "Monthly target saved successfully!");
            loadDashboardData();
            cardLayout.show(contentPanel, "Dashboard");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Monthly Savings Goal must be a valid amount (0 or greater).", "Invalid Target", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JLabel lbl(String text, Font font, Color color) { return lbl(text, font, color, -1); }
    private JLabel lbl(String text, Font font, Color color, int alignment) {
        JLabel l = (alignment == -1) ? new JLabel(text) : new JLabel(text, alignment); l.setFont(font); l.setForeground(color); return l;
    }

    private JButton createButton(String text, Color bg, Color fg, Font font, EmptyBorder border) {
        JButton btn = new JButton(text); btn.setFont(font); btn.setBackground(bg); btn.setForeground(fg); btn.setFocusPainted(false); btn.setBorder(border); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
    }

    private JTextField createTextField(Object initialValue, Font font, int height) {
        JTextField tf = (initialValue instanceof Integer) ? new JTextField((Integer) initialValue) : new JTextField(initialValue.toString());
        tf.setFont(font); tf.setBackground(COLOR_TOPBAR); tf.setForeground(COLOR_TEXT_PRIMARY); tf.setCaretColor(COLOR_TEXT_PRIMARY);
        if (height > 0) tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, height));
        tf.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(5, 8, 5, 8))); return tf;
    }

    private JComboBox<String> createComboBox(String[] items, Font font, int height) {
        JComboBox<String> cb = new JComboBox<>(items); cb.setFont(font); cb.setBackground(COLOR_TOPBAR); cb.setForeground(COLOR_TEXT_PRIMARY);
        if (height > 0) cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, height)); return cb;
    }

    private JCheckBox createCheckBox(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected); cb.setOpaque(false); cb.setForeground(COLOR_TEXT_PRIMARY); cb.setFont(FONT_REGULAR); return cb;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String subtext, int top, int left, int bottom, int right) {
        JPanel card = new JPanel(new BorderLayout(5, 5)); card.setBackground(COLOR_PANEL); card.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(top, left, bottom, right)));
        card.add(lbl(title, FONT_REGULAR, COLOR_TEXT_MUTED), BorderLayout.NORTH); card.add(valueLabel, BorderLayout.CENTER);
        if (subtext != null) card.add(lbl(subtext, FONT_MUTED_SMALL, COLOR_TEXT_MUTED), BorderLayout.SOUTH); return card;
    }

    private JPanel createSettingsCard(String title) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(COLOR_PANEL); p.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(20, 20, 20, 20)));
        p.add(lbl(title, FONT_CARD_TITLE, COLOR_ACCENT)); p.add(Box.createRigidArea(new Dimension(0, 15))); return p;
    }

    private JPanel createFormRow(String labelText, JComponent field) {
        JPanel r = new JPanel(new BorderLayout(10, 0)); r.setOpaque(false); r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel l = lbl(labelText, FONT_REGULAR, COLOR_TEXT_PRIMARY); l.setPreferredSize(new Dimension(160, 30));
        r.add(l, BorderLayout.WEST); r.add(field, BorderLayout.CENTER); return r;
    }

private void setActiveMonth(String month) {
    if(syncingMonth||month==null||month.equals(activeMonth)||!DBUtil.isValidMonth(month))return;
    syncingMonth=true; try{activeMonth=month;addMonthBox.setSelectedItem(month);historyMonthBox.setSelectedItem(month);reportMonthBox.setSelectedItem(month);}finally{syncingMonth=false;} refreshAll();
}
private void refreshAll(){loadDashboardData();loadTransactionHistoryData();loadMonthlyReportData();}
private String money(double value){return RUPEE+String.format("%.2f",value);}
private void showError(String what,Exception ex){JOptionPane.showMessageDialog(this,what+":\n"+ex.getMessage(),"Storage Error",JOptionPane.ERROR_MESSAGE);}
private void loadUserName(){UserAccount u=store.getUser(userId);userName=u==null?"User":u.username;}
private void closeConnection(){}
private void doLogout(){if(JOptionPane.showConfirmDialog(this,"Log out of the application?","Confirm Logout",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;dispose();SwingUtilities.invokeLater(LoginSystem::new);}
private java.util.List<Transaction> monthTransactions(String month){java.util.List<Transaction> out=new java.util.ArrayList<>();for(Transaction t:store.allTransactions(userId))if(month.equals(DBUtil.monthNameOf(java.util.Date.from(t.date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()))))out.add(t);return out;}
private java.util.List<Transaction> allTransactions(){return store.allTransactions(userId);}

public void loadDashboardData(){
    try{
        java.util.List<Transaction> all=allTransactions();
        double totalIncome=all.stream().filter(t->"Income".equalsIgnoreCase(t.type)).mapToDouble(t->t.amount).sum();
        double totalExpense=all.stream().filter(t->"Expense".equalsIgnoreCase(t.type)).mapToDouble(t->t.amount).sum();
        totalBalanceLabel.setText(money(totalIncome-totalExpense));incomeLabel.setText(money(totalIncome));expenseLabel.setText(money(totalExpense));
        java.util.List<Transaction> current=monthTransactions(activeMonth);
        double monthIncome=current.stream().filter(t->"Income".equalsIgnoreCase(t.type)).mapToDouble(t->t.amount).sum();
        double monthExpense=current.stream().filter(t->"Expense".equalsIgnoreCase(t.type)).mapToDouble(t->t.amount).sum();
        double monthlySavings=monthIncome-monthExpense;
        double monthlyTarget=store.getMonthlyTarget(userId);
        double targetLeft=Math.max(0, monthlyTarget-monthlySavings);
        budgetLeftLabel.setText(money(targetLeft));

        int progress=monthlyTarget>0 ? (int)Math.round(Math.max(0, Math.min(100, (monthExpense/monthlyTarget)*100.0))) : 0;
        if(monthlyProgressBar!=null) monthlyProgressBar.setValue(progress);
        if(monthlyProgressPercentLabel!=null) monthlyProgressPercentLabel.setText(progress+"% spent");
        if(monthlyProgressAmountLabel!=null) monthlyProgressAmountLabel.setText(money(monthExpense) + " spent" + (monthlyTarget>0 ? " of " + money(monthlyTarget) : ""));

        if(smartAdviceLabel!=null){
            boolean hasExpenseTransaction=current.stream().anyMatch(t -> "Expense".equalsIgnoreCase(t.type));
            smartAdviceLabel.setVisible(hasExpenseTransaction);
            if(hasExpenseTransaction){
                String advice=buildSmartAdvice(current, monthIncome, monthExpense, monthlyTarget).replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
                smartAdviceLabel.setText("<html><div style='width:280px'>"+advice+"</div></html>");
            } else {
                smartAdviceLabel.setText("");
            }
        }
        java.time.LocalDate today=java.time.LocalDate.now();
        double incToday=all.stream().filter(t->today.equals(t.date)&&"Income".equalsIgnoreCase(t.type)).mapToDouble(t->t.amount).sum();
        double expToday=all.stream().filter(t->today.equals(t.date)&&"Expense".equalsIgnoreCase(t.type)).mapToDouble(t->t.amount).sum();
        incomeTodayLabel.setText(money(incToday));expenseTodayLabel.setText(money(expToday));netTodayLabel.setText(money(incToday-expToday));
        recentTableModel.setRowCount(0); all.sort(java.util.Comparator.comparing((Transaction t)->t.date,java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())).thenComparingInt(t->-t.id));
        for(Transaction t:all.stream().limit(15).toList())recentTableModel.addRow(new Object[]{DBUtil.formatDisplayDate(java.util.Date.from(t.date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())),t.category,money(t.amount),t.type});
    }catch(Exception ex){showError("Could not load the dashboard",ex);}
}

private String buildSmartAdvice(java.util.List<Transaction> rows,double income,double expense,double target){
    if(rows==null || rows.isEmpty()) return "";

    double savings=income-expense;
    if(target>0 && savings>=target){
        return String.format("Great! You have reached your monthly target of ₹%.2f. Keep this spending pattern and continue saving.",target);
    }

    double gap=target>0 ? Math.max(0,target-savings) : 0;
    java.util.Map<String,Double> focusSpend=new java.util.LinkedHashMap<>();
    java.util.Map<String,String> displayNames=new java.util.LinkedHashMap<>();
    String[] stop={"the","and","for","with","from","this","that","paid","buy","bought","spent","expense","payment","monthly","daily","on","at","to","a","an","of","in","my","is","was","via"};
    java.util.Set<String> stopWords=new java.util.HashSet<>(java.util.Arrays.asList(stop));
    for(Transaction t:rows){
        if(!"Expense".equalsIgnoreCase(t.type)) continue;
        String description=t.description==null?"":t.description.toLowerCase().replaceAll("[^a-z0-9 ]"," ").trim();
        for(String word:description.split("\\s+")){
            if(word.length()<3||stopWords.contains(word)||word.matches("\\d+")) continue;
            focusSpend.merge(word,t.amount,Double::sum);
            displayNames.putIfAbsent(word,word);
        }
        String category=t.category==null?"":t.category.trim();
        if(!category.isEmpty()) focusSpend.merge("category:"+category.toLowerCase(),t.amount,Double::sum);
        if(!category.isEmpty()) displayNames.putIfAbsent("category:"+category.toLowerCase(),category);
    }

    String focus=null; double focusAmount=0;
    for(java.util.Map.Entry<String,Double> e:focusSpend.entrySet()){
        if(e.getValue()>focusAmount){focus=e.getKey();focusAmount=e.getValue();}
    }

    if(focus!=null && focusAmount>0){
        double suggestedSave=focusAmount*0.25;
        String name=displayNames.getOrDefault(focus,focus);
        if(target>0){
            double remaining=Math.max(0,gap-suggestedSave);
            return String.format("You are ₹%.2f away from your target. You spent ₹%.2f on %s. Reducing this by 25%% could save about ₹%.2f this month%s.",gap,focusAmount,name,suggestedSave,remaining>0?String.format("; you would still need ₹%.2f",remaining):"");
        }
        return String.format("You spent ₹%.2f on %s. Reducing this by 25%% could save about ₹%.2f this month.",focusAmount,name,suggestedSave);
    }

    if(target>0){
        return String.format("You are ₹%.2f away from your monthly target. Review your largest expenses and move any amount you cut into savings.",gap);
    }
    return String.format("You have spent ₹%.2f this month. Review your recent expenses and identify one area where you can save.",expense);
}

private void saveTransaction(JPanel panel,JRadioButton income,JTextField amountField,JComboBox<String> categoryBox,JTextField dateField,JTextArea description,JComboBox<String> payment){
    String type=income.isSelected()?"Income":"Expense",amountStr=amountField.getText().trim();Object categorySel=categoryBox.getSelectedItem(),paymentSel=payment.getSelectedItem();
    if(amountStr.isEmpty()){JOptionPane.showMessageDialog(panel,"Please enter an amount!","Error",JOptionPane.ERROR_MESSAGE);return;}
    double amount;try{amount=Double.parseDouble(amountStr);}catch(NumberFormatException ex){JOptionPane.showMessageDialog(panel,"Amount must be a number.","Error",JOptionPane.ERROR_MESSAGE);return;}
    if(amount<=0){JOptionPane.showMessageDialog(panel,"Amount must be greater than zero.","Error",JOptionPane.ERROR_MESSAGE);return;} if(categorySel==null){JOptionPane.showMessageDialog(panel,"Please choose a category.","Error",JOptionPane.ERROR_MESSAGE);return;}
    java.util.Date parsedDate=DBUtil.tryParseDisplayDate(dateField.getText());if(parsedDate==null){JOptionPane.showMessageDialog(panel,"Date must be in dd/MM/yyyy format, for example "+DBUtil.today()+".","Error",JOptionPane.ERROR_MESSAGE);return;}
    String targetMonth=String.valueOf(addMonthBox.getSelectedItem()),dateMonth=DBUtil.monthNameOf(parsedDate);
    if(!dateMonth.equals(targetMonth)){int choice=JOptionPane.showConfirmDialog(panel,"The date you entered falls in "+dateMonth+", but \""+targetMonth+"\" is selected.\n\nSave it into the "+dateMonth+" month instead?","Month Mismatch",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);if(choice==JOptionPane.CANCEL_OPTION||choice==JOptionPane.CLOSED_OPTION)return;if(choice==JOptionPane.YES_OPTION)targetMonth=dateMonth;}
    java.time.LocalDate ld=java.time.LocalDate.ofInstant(parsedDate.toInstant(),java.time.ZoneId.systemDefault());
    try{store.add(userId,ld,type,categorySel.toString(),description.getText().trim(),paymentSel==null?"":paymentSel.toString(),amount);JOptionPane.showMessageDialog(panel,"Transaction Saved Successfully");amountField.setText("");description.setText("");if(!targetMonth.equals(activeMonth))setActiveMonth(targetMonth);else refreshAll();}catch(Exception ex){showError("Error saving transaction",ex);}
}

    private JPanel createTransactionHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10)); panel.setBackground(COLOR_BG); panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8)); filterBar.setBackground(COLOR_PANEL);

        historyFromDate = new JTextField(8); historyToDate = new JTextField(8);
        historyCategoryFilter = new JComboBox<>(new String[]{"All", "Food", "Transport", "Shopping", "Bills", "Entertainment", "Salary", "Business", "Investment", "Bonus"});
        historyTypeFilter = new JComboBox<>(new String[]{"All", "Income", "Expense"}); historySearchField = new JTextField(12);

        JButton searchBtn = createButton("Search", COLOR_ACCENT, Color.WHITE, FONT_BTN, new EmptyBorder(6, 14, 6, 14));
        searchBtn.addActionListener(e -> loadTransactionHistoryData()); historySearchField.addActionListener(e -> loadTransactionHistoryData());

        JButton resetBtn = createButton("Reset", COLOR_TEXT_MUTED, Color.WHITE, FONT_BTN, new EmptyBorder(6, 14, 6, 14));
        resetBtn.addActionListener(e -> { historyFromDate.setText(""); historyToDate.setText(""); historyCategoryFilter.setSelectedIndex(0); historyTypeFilter.setSelectedIndex(0); historySearchField.setText(""); loadTransactionHistoryData(); });

        filterBar.add(lbl("Month:", FONT_REGULAR, COLOR_TEXT_PRIMARY)); filterBar.add(historyMonthBox); filterBar.add(lbl("From:", FONT_REGULAR, COLOR_TEXT_PRIMARY)); filterBar.add(historyFromDate);
        filterBar.add(lbl("To:", FONT_REGULAR, COLOR_TEXT_PRIMARY)); filterBar.add(historyToDate); filterBar.add(lbl("Category:", FONT_REGULAR, COLOR_TEXT_PRIMARY)); filterBar.add(historyCategoryFilter);
        filterBar.add(lbl("Type:", FONT_REGULAR, COLOR_TEXT_PRIMARY)); filterBar.add(historyTypeFilter); filterBar.add(historySearchField); filterBar.add(searchBtn); filterBar.add(resetBtn);
        panel.add(filterBar, BorderLayout.NORTH);

        historyTable = new JTable(transactionModel); styleTable(historyTable); historyTable.removeColumn(historyTable.getColumnModel().getColumn(0));
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.setBackground(COLOR_BG);
        JButton edit = createButton("Edit", COLOR_ACCENT, Color.WHITE, FONT_BTN, new EmptyBorder(6, 14, 6, 14)), delete = createButton("Delete", COLOR_RED, Color.WHITE, FONT_BTN, new EmptyBorder(6, 14, 6, 14));
        edit.addActionListener(e -> editSelectedTransaction()); delete.addActionListener(e -> deleteSelectedTransaction());
        bottom.add(edit); bottom.add(delete); panel.add(bottom, BorderLayout.SOUTH); return panel;
    }

public void loadTransactionHistoryData(){
    if(transactionModel==null)return;transactionModel.setRowCount(0);
    java.util.Date from=DBUtil.tryParseDisplayDate(historyFromDate.getText()),to=DBUtil.tryParseDisplayDate(historyToDate.getText());
    java.time.LocalDate fromDate=from==null?null:java.time.LocalDate.ofInstant(from.toInstant(),java.time.ZoneId.systemDefault());
    java.time.LocalDate toDate=to==null?null:java.time.LocalDate.ofInstant(to.toInstant(),java.time.ZoneId.systemDefault());
    Object cat=historyCategoryFilter.getSelectedItem(),type=historyTypeFilter.getSelectedItem();String search=historySearchField.getText().trim().toLowerCase();
    java.util.List<Transaction> rows=monthTransactions(activeMonth);rows.removeIf(t->{if(fromDate!=null&&t.date.isBefore(fromDate))return true;if(toDate!=null&&t.date.isAfter(toDate))return true;if(cat!=null&&!"All".equals(cat.toString())&&!cat.toString().equals(t.category))return true;if(type!=null&&!"All".equals(type.toString())&&!type.toString().equals(t.type))return true;if(!search.isEmpty()){String h=(String.valueOf(t.description)+" "+String.valueOf(t.category)+" "+String.valueOf(t.paymentMethod)).toLowerCase();return !h.contains(search);}return false;});
    rows.sort(java.util.Comparator.comparing((Transaction t)->t.date,java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())).thenComparingInt(t->-t.id));
    for(Transaction t:rows)transactionModel.addRow(new Object[]{t.id,DBUtil.formatDisplayDate(java.util.Date.from(t.date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())),t.type,t.category,t.description,t.paymentMethod,money(t.amount)});
}


    private int selectedHistoryModelRow() {
        if (historyTable == null) return -1;
        int viewRow = historyTable.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Please select a transaction in the table first.", "No Selection", JOptionPane.INFORMATION_MESSAGE); return -1; }
        return historyTable.convertRowIndexToModel(viewRow);
    }

private void deleteSelectedTransaction(){int row=selectedHistoryModelRow();if(row<0)return;int id=(Integer)transactionModel.getValueAt(row,0);if(JOptionPane.showConfirmDialog(this,"Delete this transaction permanently?","Confirm Delete",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION)return;try{store.delete(userId,id);refreshAll();}catch(Exception ex){showError("Could not delete transaction",ex);}}


private void editSelectedTransaction(){
    int row=selectedHistoryModelRow();if(row<0)return;int id=(Integer)transactionModel.getValueAt(row,0);
    JTextField dateField=new JTextField(String.valueOf(transactionModel.getValueAt(row,1)));JComboBox<String> typeBox=new JComboBox<>(new String[]{"Income","Expense"});typeBox.setSelectedItem(String.valueOf(transactionModel.getValueAt(row,2)));
    JTextField categoryField=new JTextField(String.valueOf(transactionModel.getValueAt(row,3))),descField=new JTextField(String.valueOf(transactionModel.getValueAt(row,4)));JComboBox<String> paymentBox=new JComboBox<>(new String[]{"Cash","UPI","Debit Card","Credit Card","Net Banking"});paymentBox.setSelectedItem(String.valueOf(transactionModel.getValueAt(row,5)));JTextField amountField=new JTextField(String.valueOf(transactionModel.getValueAt(row,6)).replace(RUPEE,""));
    JPanel form=new JPanel(new GridLayout(6,2,8,8));form.add(new JLabel("Date (dd/MM/yyyy):"));form.add(dateField);form.add(new JLabel("Type:"));form.add(typeBox);form.add(new JLabel("Category:"));form.add(categoryField);form.add(new JLabel("Description:"));form.add(descField);form.add(new JLabel("Payment Method:"));form.add(paymentBox);form.add(new JLabel("Amount:"));form.add(amountField);
    if(JOptionPane.showConfirmDialog(this,form,"Edit Transaction",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)return;
    java.util.Date nd=DBUtil.tryParseDisplayDate(dateField.getText());if(nd==null){JOptionPane.showMessageDialog(this,"Date must be in dd/MM/yyyy format.","Error",JOptionPane.ERROR_MESSAGE);return;}double amt;try{amt=Double.parseDouble(amountField.getText().trim());}catch(NumberFormatException ex){JOptionPane.showMessageDialog(this,"Amount must be a number.","Error",JOptionPane.ERROR_MESSAGE);return;}
    java.time.LocalDate ld=java.time.LocalDate.ofInstant(nd.toInstant(),java.time.ZoneId.systemDefault());try{store.update(userId,id,ld,String.valueOf(typeBox.getSelectedItem()),categoryField.getText().trim(),descField.getText().trim(),String.valueOf(paymentBox.getSelectedItem()),amt);String nm=DBUtil.monthNameOf(nd);if(!activeMonth.equals(nm))setActiveMonth(nm);else refreshAll();}catch(Exception ex){showError("Could not update transaction",ex);}
}


    private JPanel createMonthlyReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15)); panel.setBackground(COLOR_BG); panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); filterPanel.setOpaque(false);

        JButton generateBtn = createButton("Generate Report", COLOR_ACCENT, Color.WHITE, FONT_BTN, new EmptyBorder(6, 14, 6, 14));
        generateBtn.addActionListener(e -> loadMonthlyReportData());
        filterPanel.add(lbl("Month:", FONT_REGULAR, COLOR_TEXT_PRIMARY)); filterPanel.add(reportMonthBox); filterPanel.add(generateBtn);

        header.add(lbl("Monthly Financial Report", FONT_SUBTITLE, COLOR_TEXT_PRIMARY), BorderLayout.WEST); header.add(filterPanel, BorderLayout.EAST); panel.add(header, BorderLayout.NORTH);

        JPanel main = new JPanel(); main.setOpaque(false); main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 15)); cards.setOpaque(false); cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        reportIncomeLabel = lbl(money(0), FONT_SUBTITLE, COLOR_GREEN, SwingConstants.LEFT); reportExpenseLabel = lbl(money(0), FONT_SUBTITLE, COLOR_RED, SwingConstants.LEFT);
        reportSavingsLabel = lbl(money(0), FONT_SUBTITLE, COLOR_ACCENT, SwingConstants.LEFT); reportCountLabel = lbl("0", FONT_SUBTITLE, COLOR_TEXT_PRIMARY, SwingConstants.LEFT);

        cards.add(createStatCard("Monthly Income", reportIncomeLabel, "", 15, 18, 15, 18)); cards.add(createStatCard("Monthly Expense", reportExpenseLabel, "", 15, 18, 15, 18));
        cards.add(createStatCard("Net Savings", reportSavingsLabel, "", 15, 18, 15, 18)); cards.add(createStatCard("Total Transactions", reportCountLabel, "", 15, 18, 15, 18));

        main.add(cards); main.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel chartsContainer = new JPanel(new GridLayout(1, 2, 15, 15)); chartsContainer.setOpaque(false);
        barChart = ChartFactory.createBarChart("Expenses by Category", "Category", "Amount (" + RUPEE + ")", reportBarDataset); styleChart(barChart);
        lineChart = ChartFactory.createLineChart("Income vs Expense Trend", "Date", "Amount (" + RUPEE + ")", reportLineDataset); styleChart(lineChart);

        chartsContainer.add(createChartPanel(barChart)); chartsContainer.add(createChartPanel(lineChart)); main.add(chartsContainer);
        panel.add(main, BorderLayout.CENTER); return panel;
    }

    private JPanel createChartPanel(JFreeChart chart) {
        JPanel box = new JPanel(new BorderLayout()); box.setBackground(COLOR_PANEL); box.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(10, 10, 10, 10)));
        ChartPanel cp = new ChartPanel(chart); cp.setBackground(COLOR_PANEL); box.add(cp, BorderLayout.CENTER); return box;
    }

public void loadMonthlyReportData(){
    if(reportIncomeLabel==null)return;try{java.util.List<Transaction> rows=monthTransactions(activeMonth);double in=rows.stream().filter(t->"Income".equalsIgnoreCase(t.type)).mapToDouble(t->t.amount).sum(),ex=rows.stream().filter(t->"Expense".equalsIgnoreCase(t.type)).mapToDouble(t->t.amount).sum();reportIncomeLabel.setText(money(in));reportExpenseLabel.setText(money(ex));reportSavingsLabel.setText(money(in-ex));reportCountLabel.setText(String.valueOf(rows.size()));reportBarDataset.clear();java.util.Map<String,Double> byCat=new java.util.LinkedHashMap<>();for(Transaction t:rows)if("Expense".equalsIgnoreCase(t.type))byCat.merge(t.category==null?"Uncategorised":t.category,t.amount,Double::sum);byCat.forEach((k,v)->reportBarDataset.addValue(v,"Expense",k));reportLineDataset.clear();java.util.Map<java.time.LocalDate,java.util.Map<String,Double>> daily=new java.util.TreeMap<>();for(Transaction t:rows)daily.computeIfAbsent(t.date,k->new java.util.LinkedHashMap<>()).merge(t.type,t.amount,Double::sum);daily.forEach((d,m)->m.forEach((k,v)->reportLineDataset.addValue(v,k,DBUtil.formatShortDate(java.util.Date.from(d.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())))));if(barChart!=null){barChart.setTitle("Expenses by Category - "+activeMonth);styleChartTitle(barChart);}if(lineChart!=null){lineChart.setTitle("Income vs Expense Trend - "+activeMonth);styleChartTitle(lineChart);}}catch(Exception ex){showError("Could not build report",ex);}
}


    private JButton createSidebarButton(String text, boolean isActive) {
        JButton btn = new JButton(text); btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); btn.setFont(FONT_LABEL_BOLD); btn.setFocusPainted(false); btn.setHorizontalAlignment(SwingConstants.LEFT); btn.setBorder(new EmptyBorder(8, 15, 8, 15)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (isActive) { btn.setBackground(COLOR_ACCENT); btn.setForeground(Color.WHITE); } 
        else {
            btn.setBackground(COLOR_PANEL); btn.setForeground(COLOR_TEXT_MUTED);
            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { btn.setBackground(COLOR_CARD_BG); btn.setForeground(COLOR_TEXT_PRIMARY); }
                @Override public void mouseExited(MouseEvent e) { btn.setBackground(COLOR_PANEL); btn.setForeground(COLOR_TEXT_MUTED); }
            });
        }
        return btn;
    }

    private void styleTable(JTable table) {
        table.setBackground(COLOR_PANEL); table.setForeground(COLOR_TEXT_PRIMARY); table.setGridColor(COLOR_BORDER); table.setRowHeight(32); table.setFont(FONT_REGULAR); table.setSelectionBackground(COLOR_CARD_BG); table.setSelectionForeground(COLOR_TEXT_PRIMARY);
        JTableHeader header = table.getTableHeader(); header.setBackground(COLOR_TOPBAR); header.setForeground(COLOR_TEXT_PRIMARY); header.setFont(FONT_BTN); header.setPreferredSize(new Dimension(100, 32));
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(COLOR_PANEL); styleChartTitle(chart);
        if (chart.getLegend() != null) { chart.getLegend().setBackgroundPaint(COLOR_PANEL); chart.getLegend().setItemPaint(COLOR_TEXT_PRIMARY); }
        CategoryPlot plot = chart.getCategoryPlot(); plot.setBackgroundPaint(COLOR_PANEL); plot.setOutlinePaint(COLOR_BORDER); plot.setRangeGridlinePaint(COLOR_BORDER);
        CategoryAxis domain = plot.getDomainAxis(); domain.setLabelPaint(COLOR_TEXT_MUTED); domain.setTickLabelPaint(COLOR_TEXT_MUTED); domain.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        ValueAxis range = plot.getRangeAxis(); range.setLabelPaint(COLOR_TEXT_MUTED); range.setTickLabelPaint(COLOR_TEXT_MUTED);
    }

    private void styleChartTitle(JFreeChart chart) {
        if (chart.getTitle() != null) { chart.getTitle().setPaint(COLOR_TEXT_PRIMARY); chart.getTitle().setFont(FONT_CARD_TITLE); }
    }

    public static void main(String[] args) { LoginSystem.main(args); }
}