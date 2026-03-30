import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public static void saveToCSV(List<Transaction> transactions, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Date,Amount,Category,Description,Type");
            writer.newLine();

            for (Transaction t : transactions) {
                writer.write(
                        t.getDate() + "," + t.getAmount() + "," + t.getCategory() + "," +
                                t.getDescription() + "," + t.getType()
                );
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving to CSV: " + e.getMessage());
        }
    }

    public static List<Transaction> loadFromCSV(String fileName) {
        List<Transaction> transactions = new ArrayList<>();

        File file = new File(fileName);

        if (!file.exists()) {
            return transactions;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length < 5) {
                    continue;
                }

                try {
                    LocalDate date = LocalDate.parse(parts[0]);
                    double amount = Double.parseDouble(parts[1]);
                    String category = parts[2];
                    String description = parts[3];
                    String type = parts[4];

                    Transaction transaction = new Transaction(
                            date, amount, category, description, type
                    );

                    transactions.add(transaction);
                } catch (Exception e) {
                    System.err.println("Skipping invalid row: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading CSV: " + e.getMessage());
        }

        return transactions;
    }
}
