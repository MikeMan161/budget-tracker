# Personal Budget Tracker - Iteration 2

A Java-based personal finance application that helps users track income and expenses, manage budgets, and filter transactions through a simple graphical interface.

## Team Members
- Michael Rivera
- Zarah Maullon

---

## What's New in Version 2

- **Income/Expense Type Tracking** — each transaction is now labeled as either Income or Expense
- **Balance Summary Bar** — live display of total Income, total Expenses, and current Balance at the bottom of the window
- **Filter Transactions** — filter the table by category, type, or both simultaneously
- **Input Validation** — invalid amounts (non-numeric, negative, empty) and missing descriptions are caught with error dialogs
- **Refactored Architecture** — logic is now split across dedicated classes (`Transaction`, `BudgetManager`, `FileManager`, `InputValidation`) instead of one monolithic file

---

## Features (Version 2)

- Add transactions with amount, category (Food/Rent/Entertainment/Utilities/Other), type (Income/Expense), and description
- Delete selected transactions from the table
- Filter transactions by category and/or type; clear filter to restore full view
- Live balance summary showing total income, total expenses, and net balance
- Data automatically saved to `finance_data.csv` on exit and loaded on startup
- Input validation with user-friendly error messages

---

## Project Structure

| File | Description |
|------|-------------|
| `BudgetApp.java` | Main class — builds the GUI and wires everything together |
| `Transaction.java` | Data model representing a single transaction |
| `BudgetManager.java` | Manages the list of transactions; handles filtering, totals, and balance |
| `FileManager.java` | Handles reading and writing transactions to/from CSV |
| `InputValidation.java` | Validates user input (amount, description, category, type) |
| `BudgetAppTest.java` | JUnit 5 unit tests for all supporting classes |
| `finance_data.csv` | Local storage file for transaction data |

---

## Installation and Requirements

- **Language:** Java 21 or higher (JDK required)
- **Testing:** JUnit 5 (`junit-platform-console-standalone-1.10.0.jar` in `lib/`)
- **Environment:** Optimized for SoC lab computers and VS Code with the Java Extension Pack

---

## Setup Instructions

1. Clone the repository to your local machine
2. Ensure the Java Development Kit (JDK 21+) is installed and configured in your PATH
3. Open the project folder in VS Code
4. Run the application:
   ```
   javac *.java
   java BudgetApp
   ```
   Or use the VS Code Run button on `BudgetApp.java`

---

## Running Tests

Make sure `lib/junit-platform-console-standalone-1.10.0.jar` is in your project folder, then either:

- Click the beaker icon in VS Code and run all tests from the Testing panel, or
- Run from terminal:
  ```
  javac -cp lib/junit-platform-console-standalone-1.10.0.jar *.java
  java -jar lib/junit-platform-console-standalone-1.10.0.jar --class-path . --scan-class-path
  ```

---

## Non-Functional Requirements

- **Usability:** Clean, consistent interface with labeled inputs and real-time balance feedback
- **Performance:** Save and load operations complete in under 1 second
- **Reliability:** Auto-save on window close prevents data loss
- **Maintainability:** Separated into single-responsibility classes for easier testing and future extension
