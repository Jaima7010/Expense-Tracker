import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalDate;

public class FileManager {

    private static final String FILE_NAME = "expenses.json";

    // Save expenses to JSON
    public static void saveExpenses(ArrayList<Expense> expenses) {

        try {

            FileWriter writer = new FileWriter(FILE_NAME);

            writer.write("[\n");

            for (int i = 0; i < expenses.size(); i++) {

                Expense expense = expenses.get(i);

                writer.write("  {\n");

                writer.write("    \"id\": "
                        + expense.getId() + ",\n");

                writer.write("    \"description\": \""
                        + expense.getDescription() + "\",\n");

                writer.write("    \"amount\": "
                        + expense.getAmount() + ",\n");

                writer.write("    \"category\": \""
                        + expense.getCategory().getName() + "\",\n");

                writer.write("    \"date\": \""
                        + expense.getDate() + "\"\n");

                writer.write("  }");

                if (i < expenses.size() - 1) {
                    writer.write(",");
                }

                writer.write("\n");
            }

            writer.write("]");

            writer.close();

            System.out.println(
                    "Expenses saved successfully to JSON!"
            );

        } catch (IOException e) {

            System.out.println(
                    "Error while saving expenses."
            );
        }
    }


    // Load expenses from JSON
    public static ArrayList<Expense> loadExpenses() {

        ArrayList<Expense> expenses =
                new ArrayList<>();

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return expenses;
        }

        try {

            Scanner reader =
                    new Scanner(file);

            String id = "";
            String description = "";
            String amount = "";
            String category = "";
            String date = "";

            while (reader.hasNextLine()) {

                String line =
                        reader.nextLine().trim();

                if (line.startsWith("\"id\"")) {

                    id = getValue(line);

                } else if (line.startsWith("\"description\"")) {

                    description = getValue(line);

                } else if (line.startsWith("\"amount\"")) {

                    amount = getValue(line);

                } else if (line.startsWith("\"category\"")) {

                    category = getValue(line);

                } else if (line.startsWith("\"date\"")) {

                    date = getValue(line);

                    int categoryId =
                            expenses.size() + 1;

                    Category categoryObject =
                            new Category(
                                    categoryId,
                                    category
                            );

                    Expense expense =
                            new Expense(
                                    Integer.parseInt(id),
                                    description,
                                    Double.parseDouble(amount),
                                    categoryObject,
                                    LocalDate.parse(date)
                            );

                    expenses.add(expense);
                }
            }

            reader.close();

        } catch (Exception e) {

            System.out.println(
                    "Error while loading expenses."
            );
        }

        return expenses;
    }


    // Extract value from JSON line
    private static String getValue(String line) {

        int colonIndex =
                line.indexOf(":");

        String value =
                line.substring(colonIndex + 1)
                    .trim();

        value =
                value.replace(",", "");

        value =
                value.replace("\"", "");

        return value.trim();
    }
}