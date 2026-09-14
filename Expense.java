import java.time.LocalDate;

public class Expense {

    private int id;
    private String description;
    private double amount;
    private Category category;
    private LocalDate date;

    public Expense(int id, String description, double amount,
                   Category category, LocalDate date) {

        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void displayExpense() {
        System.out.println("--------------------------------");
        System.out.println("Expense ID   : " + id);
        System.out.println("Description  : " + description);
        System.out.println("Amount       : ₹" + amount);
        System.out.println("Category     : " + category.getName());
        System.out.println("Date         : " + date);
        System.out.println("--------------------------------");
    }
}