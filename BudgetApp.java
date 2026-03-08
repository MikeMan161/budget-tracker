import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.Vector;

public class BudgetApp {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField amountField, descField;
    private JComboBox<String> categoryBox;
    private final String FILE_NAME = "finance_data.csv";

    public BudgetApp() {
        prepareGUI();
        loadData();
    }

    private void prepareGUI() {
        frame = new JFrame("Personal Budget Tracker - V1");
        frame.setSize(600, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // --- Input Panel ---
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        
        inputPanel.add(new JLabel("Amount:"));
        amountField = new JTextField();
        inputPanel.add(amountField);

        inputPanel.add(new JLabel("Category:"));
        String[] categories = {"Food", "Rent", "Entertainment", "Utilities", "Other"};
        categoryBox = new JComboBox<>(categories);
        inputPanel.add(categoryBox);

        inputPanel.add(new JLabel("Description:"));
        descField = new JTextField();
        inputPanel.add(descField);

        JButton addButton = new JButton("Add Transaction");
        addButton.addActionListener(e -> addTransaction());
        inputPanel.add(addButton);

        frame.add(inputPanel, BorderLayout.NORTH);

        // --- Table Panel ---
        String[] columnNames = {"Date", "Amount", "Category", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Delete Button ---
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(e -> deleteTransaction());
        frame.add(deleteButton, BorderLayout.SOUTH);

        // Auto-save on exit requirement
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveData();
            }
        });

        frame.setVisible(true);
    }

    private void addTransaction() {
        String amount = amountField.getText();
        String category = (String) categoryBox.getSelectedItem();
        String desc = descField.getText();
        String date = "2026-03-08"; // Simple date for V1

        if (!amount.isEmpty()) {
            tableModel.addRow(new Object[]{date, amount, category, desc});
            saveData(); // Immediate save to prevent data loss
            amountField.setText("");
            descField.setText("");
        } else {
            JOptionPane.showMessageDialog(frame, "Please enter an amount.");
        }
    }

    private void deleteTransaction() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
            saveData();
        }
    }

    private void saveData() {
        try (PrintWriter writer = new PrintWriter(new File(FILE_NAME))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Vector row = (Vector) tableModel.getDataVector().elementAt(i);
                writer.println(String.join(",", 
                    row.get(0).toString(), 
                    row.get(1).toString(), 
                    row.get(2).toString(), 
                    row.get(3).toString()));
            }
        } catch (FileNotFoundException e) {
            System.err.println("Could not save file: " + e.getMessage());
        }
    }

    private void loadData() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    tableModel.addRow(data);
                }
            } catch (IOException e) {
                System.err.println("Could not load file: " + e.getMessage());
            }
        }
    }

    // This is the "start" button for your Java application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BudgetApp());
    }
}