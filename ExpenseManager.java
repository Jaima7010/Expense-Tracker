import java.util.ArrayList;
import java.time.LocalDate;
import java.time.YearMonth;

public class ExpenseManager {

    private ArrayList<Expense> expenses;

    public ExpenseManager() {
        expenses = new ArrayList<>();
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
        System.out.println("Expense added successfully!");
    }

    public void displayAllExpenses() {

        if (expenses.isEmpty()) {
            System.out.println("No expenses found.");
            return;
        }

        System.out.println("\n===== ALL EXPENSES =====");

        for (Expense expense : expenses) {
            expense.displayExpense();
        }
    }

    public double getTotalExpense() {

        double total = 0;

        for (Expense expense : expenses) {
            total += expense.getAmount();
        }

        return total;
    }

    public int getExpenseCount() {
        return expenses.size();
    }

    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    // Monthly Summary Report
    public void monthlySummary(int year, int month) {

        YearMonth selectedMonth =
                YearMonth.of(year, month);

        double total = 0;

        System.out.println("\n==============================");
        System.out.println("       MONTHLY SUMMARY");
        System.out.println("==============================");
        System.out.println("Month : " + selectedMonth);

        for (Expense expense : expenses) {

            LocalDate date = expense.getDate();

            YearMonth expenseMonth =
                    YearMonth.from(date);

            if (expenseMonth.equals(selectedMonth)) {

                System.out.println(
                        expense.getDescription()
                        + " - Rs."
                        + expense.getAmount()
                );

                total += expense.getAmount();
            }
        }

        System.out.println("------------------------------");
        System.out.println(
                "Total for " + selectedMonth
                + " : Rs." + total
        );
        System.out.println("==============================");
    }
}