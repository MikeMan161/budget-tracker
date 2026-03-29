import java.time.LocalDate;

public class Transaction {
    private LocalDate date;
    private double amount;
    private String category;
    private String description;
    private String type;

    public Transaction(LocalDate date, double amount, String category, String description, String type) {
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "date=" + date +
                ", amount=" + amount +
                ", category=" + category + '\'' +
                ", description=" + description + '\'' +
                ", type='" + type + '\'' + '}';
    }

    public Object[] toRow() {
        return new Object[] {
                date.toString(),
                amount,
                category,
                description,
                type
        };
    }
}
