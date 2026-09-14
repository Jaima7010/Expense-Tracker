import java.time.LocalDate;

public class MonthlyExpense extends Expense {

    private String month;

    public MonthlyExpense(int id, String description, double amount,
                           Category category, LocalDate date, String month) {

        super(id, description, amount, category, date);
        this.month = month;
    }

    public String getMonth() {
        return month;
    }

    public void displayMonthlyExpense() {
        displayExpense();
        System.out.println("Month        : " + month);
    }
}