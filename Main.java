import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        ExpenseManager manager = new ExpenseManager();

        ArrayList<Category> categories = new ArrayList<>();

        categories.add(new Category(1, "Food"));
        categories.add(new Category(2, "Travel"));
        categories.add(new Category(3, "Education"));
        categories.add(new Category(4, "Shopping"));
        categories.add(new Category(5, "Bills"));
        categories.add(new Category(6, "Health"));
        categories.add(new Category(7, "Entertainment"));
        categories.add(new Category(8, "Other"));

        // Load saved expenses
        ArrayList<Expense> savedExpenses =
                FileManager.loadExpenses();

        for (Expense expense : savedExpenses) {
            manager.addExpense(expense);
        }

        while (true) {

            System.out.println("\n=================================");
            System.out.println("        EXPENSE TRACKER");
            System.out.println("       SDG 1 - NO POVERTY");
            System.out.println("=================================");

            System.out.println("1. Add Expense");
            System.out.println("2. View All Expenses");
            System.out.println("3. Monthly Summary");
            System.out.println("4. Exit");

            System.out.print("Enter choice: ");

            int menuChoice;

            try {
                menuChoice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
                continue;
            }

            // ADD EXPENSE
            if (menuChoice == 1) {

                System.out.print("\nEnter Expense Description: ");
                String description = sc.nextLine();

                double amount;

                while (true) {

                    try {

                        System.out.print("Enter Amount: ");
                        amount = Double.parseDouble(sc.nextLine());

                        if (amount <= 0) {
                            System.out.println(
                                    "Invalid amount! Enter a positive amount."
                            );
                            continue;
                        }

                        break;

                    } catch (NumberFormatException e) {

                        System.out.println(
                                "Invalid amount! Please enter a number."
                        );
                    }
                }

                System.out.println("\nSelect Category:");

                for (Category category : categories) {
                    System.out.println(
                            category.getId()
                            + ". "
                            + category.getName()
                    );
                }

                System.out.println("9. Add New Category");

                int choice;

                while (true) {

                    try {

                        System.out.print("Enter choice: ");
                        choice = Integer.parseInt(sc.nextLine());

                        if (choice >= 1 && choice <= 9) {
                            break;
                        }

                        System.out.println(
                                "Invalid choice! Select 1 to 9."
                        );

                    } catch (NumberFormatException e) {

                        System.out.println(
                                "Please enter a number."
                        );
                    }
                }

                Category selectedCategory;

                if (choice == 9) {

                    System.out.print(
                            "Enter New Category Name: "
                    );

                    String newCategoryName =
                            sc.nextLine();

                    int newId =
                            categories.size() + 1;

                    selectedCategory =
                            new Category(
                                    newId,
                                    newCategoryName
                            );

                    categories.add(selectedCategory);

                    System.out.println(
                            "New category added successfully!"
                    );

                } else {

                    selectedCategory =
                            categories.get(choice - 1);
                }

                LocalDate date;

                while (true) {

                    try {

                        System.out.print(
                                "Enter Date (YYYY-MM-DD): "
                        );

                        String dateInput =
                                sc.nextLine();

                        date = LocalDate.parse(dateInput);

                        break;

                    } catch (DateTimeParseException e) {

                        System.out.println(
                                "Invalid date! Use YYYY-MM-DD format."
                        );
                    }
                }

                int expenseId =
                        manager.getExpenseCount() + 1;

                Expense expense = new Expense(
                        expenseId,
                        description,
                        amount,
                        selectedCategory,
                        date
                );

                manager.addExpense(expense);

                FileManager.saveExpenses(
                        manager.getExpenses()
                );

                System.out.println(
                        "\n===== EXPENSE DETAILS ====="
                );

                expense.displayExpense();

                System.out.println(
                        "Total Expense: Rs."
                        + manager.getTotalExpense()
                );

                System.out.println(
                        "Number of Expenses: "
                        + manager.getExpenseCount()
                );
            }

            // VIEW ALL EXPENSES
            else if (menuChoice == 2) {

                manager.displayAllExpenses();

                System.out.println(
                        "\nTotal Expense: Rs."
                        + manager.getTotalExpense()
                );
            }

            // MONTHLY SUMMARY
            else if (menuChoice == 3) {

                int year;
                int month;

                try {

                    System.out.print(
                            "Enter Year: "
                    );

                    year = Integer.parseInt(
                            sc.nextLine()
                    );

                    System.out.print(
                            "Enter Month (1-12): "
                    );

                    month = Integer.parseInt(
                            sc.nextLine()
                    );

                    if (month < 1 || month > 12) {
                        System.out.println(
                                "Invalid month!"
                        );
                        continue;
                    }

                    manager.monthlySummary(
                            year,
                            month
                    );

                } catch (NumberFormatException e) {

                    System.out.println(
                            "Please enter valid numbers."
                    );
                }
            }

            // EXIT
            else if (menuChoice == 4) {

                System.out.println(
                        "Thank you for using Expense Tracker!"
                );

                break;
            }

            else {

                System.out.println(
                        "Invalid choice! Select 1 to 4."
                );
            }
        }

        sc.close();
    }
}