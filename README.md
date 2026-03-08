Personal Budget Tracker - Iteration 1
This project is a Java-based personal finance application designed to help users track expenses and manage budgets through a simple graphical interface. This version focuses on core functional requirements and local data persistence.

Team Members
Zarah Maullon Michael Rivera 

Features (Version 1)
Transaction Management: Users can add income and expense entries including amount, category, and description.
Data Persistence: All transactions are automatically saved to a local finance_data.csv file upon exit and loaded on startup to ensure no data loss.
Entry Deletion: Users can select specific entries from the table view and remove them permanently.
Category Selection: A dropdown menu allows users to organize spending into specific groups for better tracking.

Installation and Requirements
Language: Java 21 or higher (JDK).
Environment: Optimized for SoC lab computers and VS Code.

Setup Instructions

Clone the repository to your local machine.

Ensure the Java Development Kit (JDK) is installed and configured in your PATH.

Open the project folder in VS Code.

Run BudgetApp.java using the Run button or by executing javac BudgetApp.java followed by java BudgetApp in the terminal.

Project Structure
BudgetApp.java: The main application class containing the GUI logic and File I/O operations.
finance_data.csv: The local storage file where transaction data is persisted.
.gitignore: Configured to manage project files while keeping the environment clean.

Non-Functional Performance
Usability: Designed with a simple, consistent interface for quick learning.
Performance: Data saving and loading operations are designed to complete in under 1 second.
Reliability: Includes auto-save triggers to prevent data loss during unexpected shutdowns.
