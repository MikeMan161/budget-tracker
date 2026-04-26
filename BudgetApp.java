import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
 
public class BudgetApp {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
 
    private JTextField amountField, descField;
    private JComboBox<String> categoryBox;
    private JComboBox<String> typeBox;
    private JComboBox<String> filterCategoryBox;
    private JComboBox<String> filterTypeBox;
 
    private JLabel balanceLabel, incomeLabel, expenseLabel;

    private StatsPanel statsPanel;
 
    private final BudgetManager budgetManager;
    private final String FILE_NAME = "finance_data.csv";

    private int editingIndex = -1;
    private JButton addButton;
 
    public BudgetApp() {
        budgetManager = new BudgetManager();
        prepareGUI();
        loadData();
    }
 
    private void prepareGUI() {
        frame = new JFrame("Personal Budget Tracker - V3");
        frame.setSize(900, 620);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));

        // Top input panel (always visible above tabs)
        frame.add(buildInputPanel(), BorderLayout.NORTH);

        // Tabbed center area
        JTabbedPane tabs = new JTabbedPane();

        // Tab 1: Transactions table + summary/buttons
        JPanel transactionsTab = new JPanel(new BorderLayout(5, 5));
        transactionsTab.add(buildTablePanel(), BorderLayout.CENTER);
        transactionsTab.add(buildSouthPanel(), BorderLayout.SOUTH);
        tabs.addTab("Transactions", transactionsTab);

        // Tab 2: Statistics charts
        statsPanel = new StatsPanel();
        tabs.addTab("Statistics", statsPanel);

        // Refresh charts when switching to Statistics tab
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                statsPanel.refresh(budgetManager.getTransactions());
            }
        });

        frame.add(tabs, BorderLayout.CENTER);
 
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveData();
            }
        });
 
        frame.setVisible(true);
    }
 
    // --- Input Panel (top, always visible) ---
    private JPanel buildInputPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(5, 5));
 
        JPanel inputRow = new JPanel(new GridLayout(2, 6, 5, 5));
 
        inputRow.add(new JLabel("Amount:"));
        amountField = new JTextField();
        inputRow.add(amountField);
 
        inputRow.add(new JLabel("Category:"));
        String[] categories = {"Food", "Rent", "Entertainment", "Utilities", "Other"};
        categoryBox = new JComboBox<>(categories);
        inputRow.add(categoryBox);
 
        inputRow.add(new JLabel("Type:"));
        typeBox = new JComboBox<>(new String[]{"Income", "Expense"});
        inputRow.add(typeBox);
 
        inputRow.add(new JLabel("Description:"));
        descField = new JTextField();
        inputRow.add(descField);
 
        addButton = new JButton("Add Transaction");
        addButton.addActionListener(e -> addTransaction());
        inputRow.add(addButton);
 
        inputRow.add(new JLabel("Filter Category:"));
        filterCategoryBox = new JComboBox<>(new String[]{"All", "Food", "Rent", "Entertainment", "Utilities", "Other"});
        inputRow.add(filterCategoryBox);
 
        inputRow.add(new JLabel("Filter Type:"));
        filterTypeBox = new JComboBox<>(new String[]{"All", "Income", "Expense"});
        inputRow.add(filterTypeBox);
 
        JButton filterButton = new JButton("Apply Filter");
        filterButton.addActionListener(e -> applyFilter());
        inputRow.add(filterButton);
 
        JButton clearFilterButton = new JButton("Clear Filter");
        clearFilterButton.addActionListener(e -> clearFilter());
        inputRow.add(clearFilterButton);
 
        inputRow.add(new JLabel(""));
 
        wrapper.add(inputRow, BorderLayout.CENTER);
        return wrapper;
    }
 
    // --- Table Panel ---
    private JScrollPane buildTablePanel() {
        String[] columnNames = {"Date", "Amount", "Category", "Description", "Type"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return new JScrollPane(table);
    }
 
    // --- South Panel: summary + delete/edit buttons ---
    private JPanel buildSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
 
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        incomeLabel  = new JLabel("Income: $0.00");
        expenseLabel = new JLabel("Expenses: $0.00");
        balanceLabel = new JLabel("Balance: $0.00");
        incomeLabel.setForeground(new Color(0, 128, 0));
        expenseLabel.setForeground(Color.RED);
        summaryPanel.add(incomeLabel);
        summaryPanel.add(expenseLabel);
        summaryPanel.add(balanceLabel);
 
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(e -> deleteTransaction());

        JButton editButton = new JButton("Edit Selected");
        editButton.addActionListener(e -> startEditTransaction());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
 
        southPanel.add(summaryPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.EAST);
        return southPanel;
    }
 
    // --- Core Actions ---
 
    private void addTransaction() {
        String amountText = amountField.getText();
        String category   = (String) categoryBox.getSelectedItem();
        String type       = (String) typeBox.getSelectedItem();
        String desc       = descField.getText();
 
        if (!InputValidation.isValidArgument(amountText)) {
            JOptionPane.showMessageDialog(frame,
                "Please enter a valid positive number for the amount.",
                "Invalid Amount", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!InputValidation.isValidDescription(desc)) {
            JOptionPane.showMessageDialog(frame,
                "Please enter a description.",
                "Invalid Description", JOptionPane.ERROR_MESSAGE);
            return;
        }
 
        double amount = InputValidation.parseAmount(amountText);
        LocalDate date = LocalDate.now();
        Transaction t = new Transaction(date, amount, category, desc, type);

        if (editingIndex != -1) {
            // Edit mode: update existing transaction in place
            budgetManager.updateTransaction(editingIndex, t);
            refreshTable(budgetManager.getTransactions());
            editingIndex = -1;
            addButton.setText("Add Transaction");
        } else {
            // Normal add
            budgetManager.addTransaction(t);
            tableModel.addRow(t.toRow());
        }
 
        saveData();
        updateSummary();
        statsPanel.refresh(budgetManager.getTransactions());
        amountField.setText("");
        descField.setText("");
    }
 
    private void deleteTransaction() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame,
                "Please select a row to delete.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        String filterCat  = (String) filterCategoryBox.getSelectedItem();
        String filterType = (String) filterTypeBox.getSelectedItem();
        boolean isFiltered = !"All".equals(filterCat) || !"All".equals(filterType);
 
        if (isFiltered) {
            List<Transaction> filtered = budgetManager.filterTransactions(filterCat, filterType);
            Transaction toDelete = filtered.get(selectedRow);
            int realIndex = budgetManager.getTransactions().indexOf(toDelete);
            budgetManager.removeTransaction(realIndex);
        } else {
            budgetManager.removeTransaction(selectedRow);
        }
 
        tableModel.removeRow(selectedRow);
        saveData();
        updateSummary();
        statsPanel.refresh(budgetManager.getTransactions());
    }

    private void startEditTransaction() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame,
                "Please select a row to edit.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Resolve real index accounting for active filters
        String filterCat  = (String) filterCategoryBox.getSelectedItem();
        String filterType = (String) filterTypeBox.getSelectedItem();
        boolean isFiltered = !"All".equals(filterCat) || !"All".equals(filterType);

        Transaction toEdit;
        if (isFiltered) {
            List<Transaction> filtered = budgetManager.filterTransactions(filterCat, filterType);
            toEdit = filtered.get(selectedRow);
            editingIndex = budgetManager.getTransactions().indexOf(toEdit);
        } else {
            editingIndex = selectedRow;
            toEdit = budgetManager.getTransactions().get(editingIndex);
        }

        // Populate input fields with the selected transaction's values
        amountField.setText(String.valueOf(toEdit.getAmount()));
        descField.setText(toEdit.getDescription());
        categoryBox.setSelectedItem(toEdit.getCategory());
        typeBox.setSelectedItem(toEdit.getType());
        addButton.setText("Save Changes");
    }
 
    private void applyFilter() {
        String filterCat  = (String) filterCategoryBox.getSelectedItem();
        String filterType = (String) filterTypeBox.getSelectedItem();
        List<Transaction> filtered = budgetManager.filterTransactions(filterCat, filterType);
        refreshTable(filtered);
    }
 
    private void clearFilter() {
        filterCategoryBox.setSelectedItem("All");
        filterTypeBox.setSelectedItem("All");
        refreshTable(budgetManager.getTransactions());
    }
 
    // --- Table / Data Helpers ---
 
    private void refreshTable(List<Transaction> transactions) {
        tableModel.setRowCount(0);
        for (Transaction t : transactions) {
            tableModel.addRow(t.toRow());
        }
    }
 
    private void updateSummary() {
        incomeLabel.setText(String.format("Income: $%.2f", budgetManager.getTotalIncome()));
        expenseLabel.setText(String.format("Expenses: $%.2f", budgetManager.getTotalExpenses()));
        balanceLabel.setText(String.format("Balance: $%.2f", budgetManager.getBalance()));
    }
 
    private void saveData() {
        FileManager.saveToCSV(budgetManager.getTransactions(), FILE_NAME);
    }
 
    private void loadData() {
        List<Transaction> loaded = FileManager.loadFromCSV(FILE_NAME);
        budgetManager.setTransactions(loaded);
        refreshTable(budgetManager.getTransactions());
        updateSummary();
    }
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(BudgetApp::new);
    }
}
