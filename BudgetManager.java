import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BudgetManager {
    private final List<Transaction> transactions;

    public BudgetManager() {
        this.transactions = new ArrayList<>();
    }

    public void addTransaction(Transaction transaction) {
        if (transaction != null) {
            transactions.add(transaction);
        }
    }

    public boolean removeTransaction(int index) {
        if (index >= 0 && index < transactions.size()) {
            transactions.remove(index);
            return true;
        }

        return false;
    }

    public boolean updateTransaction(int index, Transaction updatedTransaction) {
        if (index >= 0 && index < transactions.size() && updatedTransaction != null) {
            transactions.set(index, updatedTransaction);
            return true;
        }

        return false;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void setTransactions(List<Transaction> newTransactions) {
        transactions.clear();
        if (newTransactions != null) {
            transactions.addAll(newTransactions);
        }
    }

    public double getTotalIncome() {
        double total = 0.0;
        for (Transaction transaction : transactions) {
            if ("Income".equalsIgnoreCase(transaction.getType())) {
                total += transaction.getAmount();
            }
        }

        return total;
    }

    public double getTotalExpenses() {
        double total = 0.0;
        for (Transaction transaction : transactions) {
            if("Expense".equalsIgnoreCase(transaction.getType())) {
                total += transaction.getAmount();
            }
        }

        return total;
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpenses();
    }

    public List<Transaction> filterTransactions(String category, String type) {
        List<Transaction> filtered = new ArrayList<>();

        for (Transaction transaction : transactions) {
            boolean matchCategory = (category == null || category.equalsIgnoreCase("All")
                    || transaction.getCategory().equalsIgnoreCase(category));

            boolean matchType = (type == null || type.equalsIgnoreCase("All"))
                    || transaction.getType().equalsIgnoreCase(type);

            if (matchCategory && matchType) {
                filtered.add(transaction);
            }
        }

        return filtered;
    }
}
