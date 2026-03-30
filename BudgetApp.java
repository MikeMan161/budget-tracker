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
 
    private final BudgetManager budgetManager;
    private final String FILE_NAME = "finance_data.csv";
 
    public BudgetApp() {
        budgetManager = new BudgetManager();
        prepareGUI();
        loadData();
    }
 
    private void prepareGUI() {
        frame = new JFrame("Personal Budget Tracker - V2");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));
 
        frame.add(buildInputPanel(), BorderLayout.NORTH);
        frame.add(buildTablePanel(), BorderLayout.CENTER);
        frame.add(buildSouthPanel(), BorderLayout.SOUTH);
 
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveData();
            }
        });
 
        frame.setVisible(true);
    }
 
    // --- Input Panel (top) ---
    private JPanel buildInputPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(5, 5));
 
        // Row 1: Amount, Category, Type, Description
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
 
        JButton addButton = new JButton("Add Transaction");
        addButton.addActionListener(e -> addTransaction());
        inputRow.add(addButton);
 
        // Row 2: Filter controls
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
 
        // Empty cell to fill grid
        inputRow.add(new JLabel(""));
 
        wrapper.add(inputRow, BorderLayout.CENTER);
        return wrapper;
    }
 
    // --- Table Panel (center) ---
    private JScrollPane buildTablePanel() {
        String[] columnNames = {"Date", "Amount", "Category", "Description", "Type"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is read-only; edits go through the form
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return new JScrollPane(table);
    }
 
    // --- South Panel: summary + delete button ---
    private JPanel buildSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
 
        // Summary labels
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        incomeLabel  = new JLabel("Income: $0.00");
        expenseLabel = new JLabel("Expenses: $0.00");
        balanceLabel = new JLabel("Balance: $0.00");
        incomeLabel.setForeground(new Color(0, 128, 0));
        expenseLabel.setForeground(Color.RED);
        summaryPanel.add(incomeLabel);
        summaryPanel.add(expenseLabel);
        summaryPanel.add(balanceLabel);
 
        // Delete button
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(e -> deleteTransaction());
 
        southPanel.add(summaryPanel, BorderLayout.CENTER);
        southPanel.add(deleteButton, BorderLayout.EAST);
        return southPanel;
    }
 
    // --- Core Actions ---
 
    private void addTransaction() {
        String amountText = amountField.getText();
        String category   = (String) categoryBox.getSelectedItem();
        String type       = (String) typeBox.getSelectedItem();
        String desc       = descField.getText();
 
        // Validate using InputValidation
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
        budgetManager.addTransaction(t);
        tableModel.addRow(t.toRow());
 
        saveData();
        updateSummary();
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
 
        // If a filter is active, find the real index in budgetManager
        String filterCat  = (String) filterCategoryBox.getSelectedItem();
        String filterType = (String) filterTypeBox.getSelectedItem();
        boolean isFiltered = !"All".equals(filterCat) || !"All".equals(filterType);
 
        if (isFiltered) {
            // Find the actual transaction object shown in that row
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