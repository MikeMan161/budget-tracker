import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 
import java.io.*;
import java.time.LocalDate;
import java.util.List;
 
import static org.junit.jupiter.api.Assertions.*;
 
/**
 * Unit tests for Personal Budget Tracker - Version 2
 * Tests cover: Transaction, BudgetManager, InputValidation, FileManager
 */
public class BudgetAppTest {
 
    private BudgetManager budgetManager;
    private Transaction incomeTransaction;
    private Transaction expenseTransaction;
 
    @BeforeEach
    void setUp() {
        budgetManager = new BudgetManager();
 
        incomeTransaction = new Transaction(
            LocalDate.of(2026, 3, 1), 1000.00, "Other", "Paycheck", "Income"
        );
        expenseTransaction = new Transaction(
            LocalDate.of(2026, 3, 2), 200.00, "Food", "Groceries", "Expense"
        );
    }
 
    // -----------------------------------------------------------------------
    // Transaction tests
    // -----------------------------------------------------------------------
 
    @Test
    void testTransactionGetters() {
        assertEquals(LocalDate.of(2026, 3, 1), incomeTransaction.getDate());
        assertEquals(1000.00, incomeTransaction.getAmount());
        assertEquals("Other", incomeTransaction.getCategory());
        assertEquals("Paycheck", incomeTransaction.getDescription());
        assertEquals("Income", incomeTransaction.getType());
    }
 
    @Test
    void testTransactionSetters() {
        incomeTransaction.setAmount(1500.00);
        incomeTransaction.setCategory("Rent");
        incomeTransaction.setDescription("Updated");
        incomeTransaction.setType("Expense");
 
        assertEquals(1500.00, incomeTransaction.getAmount());
        assertEquals("Rent", incomeTransaction.getCategory());
        assertEquals("Updated", incomeTransaction.getDescription());
        assertEquals("Expense", incomeTransaction.getType());
    }
 
    @Test
    void testTransactionToRow() {
        Object[] row = incomeTransaction.toRow();
        assertEquals(5, row.length);
        assertEquals("2026-03-01", row[0]);
        assertEquals(1000.00, row[1]);
        assertEquals("Other", row[2]);
        assertEquals("Paycheck", row[3]);
        assertEquals("Income", row[4]);
    }
 
    @Test
    void testTransactionToString() {
        String str = incomeTransaction.toString();
        assertTrue(str.contains("1000.0"));
        assertTrue(str.contains("Income"));
    }
 
    // -----------------------------------------------------------------------
    // BudgetManager tests
    // -----------------------------------------------------------------------
 
    @Test
    void testAddTransaction() {
        budgetManager.addTransaction(incomeTransaction);
        assertEquals(1, budgetManager.getTransactions().size());
    }
 
    @Test
    void testAddNullTransactionDoesNothing() {
        budgetManager.addTransaction(null);
        assertEquals(0, budgetManager.getTransactions().size());
    }
 
    @Test
    void testRemoveTransactionValidIndex() {
        budgetManager.addTransaction(incomeTransaction);
        boolean result = budgetManager.removeTransaction(0);
        assertTrue(result);
        assertEquals(0, budgetManager.getTransactions().size());
    }
 
    @Test
    void testRemoveTransactionInvalidIndex() {
        budgetManager.addTransaction(incomeTransaction);
        boolean result = budgetManager.removeTransaction(5);
        assertFalse(result);
        assertEquals(1, budgetManager.getTransactions().size());
    }
 
    @Test
    void testUpdateTransaction() {
        budgetManager.addTransaction(incomeTransaction);
        Transaction updated = new Transaction(
            LocalDate.now(), 500.00, "Utilities", "Electric bill", "Expense"
        );
        boolean result = budgetManager.updateTransaction(0, updated);
        assertTrue(result);
        assertEquals("Electric bill", budgetManager.getTransactions().get(0).getDescription());
    }
 
    @Test
    void testUpdateTransactionInvalidIndex() {
        boolean result = budgetManager.updateTransaction(99, incomeTransaction);
        assertFalse(result);
    }
 
    @Test
    void testGetTotalIncome() {
        budgetManager.addTransaction(incomeTransaction);  // $1000 Income
        budgetManager.addTransaction(expenseTransaction); // $200 Expense
        assertEquals(1000.00, budgetManager.getTotalIncome(), 0.001);
    }
 
    @Test
    void testGetTotalExpenses() {
        budgetManager.addTransaction(incomeTransaction);
        budgetManager.addTransaction(expenseTransaction);
        assertEquals(200.00, budgetManager.getTotalExpenses(), 0.001);
    }
 
    @Test
    void testGetBalance() {
        budgetManager.addTransaction(incomeTransaction);  // +1000
        budgetManager.addTransaction(expenseTransaction); // -200
        assertEquals(800.00, budgetManager.getBalance(), 0.001);
    }
 
    @Test
    void testGetBalanceEmptyIsZero() {
        assertEquals(0.00, budgetManager.getBalance(), 0.001);
    }
 
    @Test
    void testFilterByCategory() {
        budgetManager.addTransaction(incomeTransaction);  // category: Other
        budgetManager.addTransaction(expenseTransaction); // category: Food
 
        List<Transaction> filtered = budgetManager.filterTransactions("Food", "All");
        assertEquals(1, filtered.size());
        assertEquals("Food", filtered.get(0).getCategory());
    }
 
    @Test
    void testFilterByType() {
        budgetManager.addTransaction(incomeTransaction);
        budgetManager.addTransaction(expenseTransaction);
 
        List<Transaction> filtered = budgetManager.filterTransactions("All", "Expense");
        assertEquals(1, filtered.size());
        assertEquals("Expense", filtered.get(0).getType());
    }
 
    @Test
    void testFilterAllReturnsAll() {
        budgetManager.addTransaction(incomeTransaction);
        budgetManager.addTransaction(expenseTransaction);
 
        List<Transaction> filtered = budgetManager.filterTransactions("All", "All");
        assertEquals(2, filtered.size());
    }
 
    @Test
    void testSetTransactions() {
        budgetManager.addTransaction(incomeTransaction);
        List<Transaction> newList = List.of(expenseTransaction);
        budgetManager.setTransactions(newList);
        assertEquals(1, budgetManager.getTransactions().size());
        assertEquals("Groceries", budgetManager.getTransactions().get(0).getDescription());
    }
 
    @Test
    void testGetTransactionsIsUnmodifiable() {
        budgetManager.addTransaction(incomeTransaction);
        assertThrows(UnsupportedOperationException.class, () ->
            budgetManager.getTransactions().add(expenseTransaction)
        );
    }
 
    // -----------------------------------------------------------------------
    // InputValidation tests
    // -----------------------------------------------------------------------
 
    @Test
    void testValidAmount() {
        assertTrue(InputValidation.isValidArgument("150.00"));
        assertTrue(InputValidation.isValidArgument("1"));
        assertTrue(InputValidation.isValidArgument("  99.99  "));
    }
 
    @Test
    void testInvalidAmountNegative() {
        assertFalse(InputValidation.isValidArgument("-50"));
    }
 
    @Test
    void testInvalidAmountZero() {
        assertFalse(InputValidation.isValidArgument("0"));
    }
 
    @Test
    void testInvalidAmountEmpty() {
        assertFalse(InputValidation.isValidArgument(""));
        assertFalse(InputValidation.isValidArgument(null));
    }
 
    @Test
    void testInvalidAmountNonNumeric() {
        assertFalse(InputValidation.isValidArgument("abc"));
        assertFalse(InputValidation.isValidArgument("12abc"));
    }
 
    @Test
    void testParseAmount() {
        assertEquals(99.99, InputValidation.parseAmount("99.99"), 0.001);
        assertEquals(100.0, InputValidation.parseAmount("  100  "), 0.001);
    }
 
    @Test
    void testValidCategory() {
        assertTrue(InputValidation.isValidCategory("Food"));
        assertFalse(InputValidation.isValidCategory(null));
        assertFalse(InputValidation.isValidCategory(""));
    }
 
    @Test
    void testValidDescription() {
        assertTrue(InputValidation.isValidDescription("Groceries"));
        assertFalse(InputValidation.isValidDescription(null));
        assertFalse(InputValidation.isValidDescription(""));
    }
 
    @Test
    void testValidType() {
        assertTrue(InputValidation.isValidType("Income"));
        assertTrue(InputValidation.isValidType("Expense"));
        assertTrue(InputValidation.isValidType("income")); // case insensitive
        assertFalse(InputValidation.isValidType("Other"));
        assertFalse(InputValidation.isValidType(null));
    }
 
    // -----------------------------------------------------------------------
    // FileManager tests
    // -----------------------------------------------------------------------
 
    @Test
    void testSaveAndLoadCSV() throws IOException {
        File tempFile = File.createTempFile("budget_test", ".csv");
        tempFile.deleteOnExit();
 
        List<Transaction> original = List.of(incomeTransaction, expenseTransaction);
        FileManager.saveToCSV(original, tempFile.getAbsolutePath());
 
        List<Transaction> loaded = FileManager.loadFromCSV(tempFile.getAbsolutePath());
 
        assertEquals(2, loaded.size());
        assertEquals(1000.00, loaded.get(0).getAmount(), 0.001);
        assertEquals("Income", loaded.get(0).getType());
        assertEquals(200.00, loaded.get(1).getAmount(), 0.001);
        assertEquals("Expense", loaded.get(1).getType());
    }
 
    @Test
    void testLoadFromNonExistentFileReturnsEmpty() {
        List<Transaction> result = FileManager.loadFromCSV("nonexistent_file_xyz.csv");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
 
    @Test
    void testSaveCreatesCSVWithHeader() throws IOException {
        File tempFile = File.createTempFile("budget_header_test", ".csv");
        tempFile.deleteOnExit();
 
        FileManager.saveToCSV(List.of(incomeTransaction), tempFile.getAbsolutePath());
 
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String header = reader.readLine();
            assertEquals("Date,Amount,Category,Description,Type", header);
        }
    }
 
    @Test
    void testLoadSkipsInvalidRows() throws IOException {
        File tempFile = File.createTempFile("budget_invalid_test", ".csv");
        tempFile.deleteOnExit();
 
        try (PrintWriter writer = new PrintWriter(tempFile)) {
            writer.println("Date,Amount,Category,Description,Type");
            writer.println("not-a-date,abc,Food,Bad row,Expense"); // invalid
            writer.println("2026-03-01,100.00,Food,Valid row,Expense"); // valid
        }
 
        List<Transaction> loaded = FileManager.loadFromCSV(tempFile.getAbsolutePath());
        assertEquals(1, loaded.size());
        assertEquals("Valid row", loaded.get(0).getDescription());
    }
}