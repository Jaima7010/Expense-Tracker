import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class ExpenseServer {

    private static final ExpenseManager manager = new ExpenseManager();

    public static void main(String[] args) throws IOException {

        // Load saved expenses
        ArrayList<Expense> savedExpenses = FileManager.loadExpenses();

        for (Expense expense : savedExpenses) {
            manager.addExpense(expense);
        }

        /*
         * Local laptop:
         * PORT is not available -> uses 8080
         *
         * Render:
         * PORT is provided automatically -> uses Render PORT
         */
        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8080")
        );

        // Start server
        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", port),
                0
        );

        // API routes
        server.createContext("/expenses", ExpenseServer::handleExpenses);
        server.createContext("/report", ExpenseServer::handleReport);
        server.createContext("/", ExpenseServer::handleHome);

        server.setExecutor(null);

        System.out.println("=================================");
        System.out.println("       EXPENSE TRACKER SERVER");
        System.out.println("=================================");
        System.out.println("Server started successfully!");
        System.out.println("Port: " + port);

        server.start();
    }


    // =========================================================
    // HOME
    // =========================================================

    private static void handleHome(HttpExchange exchange)
            throws IOException {

        sendResponse(
                exchange,
                200,
                "Expense Tracker Java Backend is running!",
                "text/plain"
        );
    }


    // =========================================================
    // EXPENSE API
    // =========================================================

    private static void handleExpenses(HttpExchange exchange)
            throws IOException {

        String method = exchange.getRequestMethod();


        // -----------------------------------------------------
        // OPTIONS
        // -----------------------------------------------------

        if (method.equalsIgnoreCase("OPTIONS")) {

            addCorsHeaders(exchange);

            exchange.sendResponseHeaders(204, -1);
            exchange.close();

            return;
        }


        // -----------------------------------------------------
        // GET - VIEW EXPENSES
        // -----------------------------------------------------

        if (method.equalsIgnoreCase("GET")) {

            String json = createJson();

            sendResponse(
                    exchange,
                    200,
                    json,
                    "application/json"
            );

            return;
        }


        // -----------------------------------------------------
        // POST - ADD EXPENSE
        // -----------------------------------------------------

        if (method.equalsIgnoreCase("POST")) {

            String data = readRequestBody(exchange);

            boolean success = addExpenseFromJson(data);

            if (success) {

                sendResponse(
                        exchange,
                        200,
                        "{\"message\":\"Expense added successfully!\"}",
                        "application/json"
                );

            } else {

                sendResponse(
                        exchange,
                        400,
                        "{\"message\":\"Invalid expense data\"}",
                        "application/json"
                );
            }

            return;
        }


        // -----------------------------------------------------
        // DELETE - DELETE EXPENSE
        // -----------------------------------------------------

        if (method.equalsIgnoreCase("DELETE")) {

            int id = getIdFromQuery(
                    exchange.getRequestURI().getQuery()
            );

            boolean deleted = deleteExpense(id);

            if (deleted) {

                sendResponse(
                        exchange,
                        200,
                        "{\"message\":\"Expense deleted successfully!\"}",
                        "application/json"
                );

            } else {

                sendResponse(
                        exchange,
                        404,
                        "{\"message\":\"Expense not found\"}",
                        "application/json"
                );
            }

            return;
        }


        // -----------------------------------------------------
        // PUT - EDIT EXPENSE
        // -----------------------------------------------------

        if (method.equalsIgnoreCase("PUT")) {

            int id = getIdFromQuery(
                    exchange.getRequestURI().getQuery()
            );

            String data = readRequestBody(exchange);

            boolean updated = updateExpense(id, data);

            if (updated) {

                sendResponse(
                        exchange,
                        200,
                        "{\"message\":\"Expense updated successfully!\"}",
                        "application/json"
                );

            } else {

                sendResponse(
                        exchange,
                        404,
                        "{\"message\":\"Expense not found\"}",
                        "application/json"
                );
            }

            return;
        }


        // -----------------------------------------------------
        // INVALID METHOD
        // -----------------------------------------------------

        sendResponse(
                exchange,
                405,
                "{\"message\":\"Method not allowed\"}",
                "application/json"
        );
    }


    // =========================================================
    // MONTHLY REPORT
    // =========================================================

    private static void handleReport(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"message\":\"Method not allowed\"}",
                    "application/json"
            );

            return;
        }


        double total = 0;
        double monthlyTotal = 0;

        LocalDate today = LocalDate.now();

        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        HashMap<String, Double> categoryTotals =
                new HashMap<>();


        // Calculate totals
        for (Expense expense : manager.getExpenses()) {

            double amount = expense.getAmount();

            total += amount;


            // Current month amount
            if (expense.getDate().getYear() == currentYear
                    && expense.getDate().getMonthValue() == currentMonth) {

                monthlyTotal += amount;
            }


            // Category-wise total
            String category =
                    expense.getCategory().getName();

            categoryTotals.put(
                    category,
                    categoryTotals.getOrDefault(category, 0.0)
                            + amount
            );
        }


        // Create category JSON
        StringBuilder categoryJson =
                new StringBuilder("{");

        int categoryIndex = 0;

        for (String category : categoryTotals.keySet()) {

            categoryJson
                    .append("\"")
                    .append(escapeJson(category))
                    .append("\":")
                    .append(categoryTotals.get(category));

            if (categoryIndex < categoryTotals.size() - 1) {
                categoryJson.append(",");
            }

            categoryIndex++;
        }

        categoryJson.append("}");


        // Create report JSON
        String report =
                "{"
                        + "\"totalExpenses\":"
                        + manager.getExpenses().size()
                        + ","

                        + "\"totalAmount\":"
                        + total
                        + ","

                        + "\"currentMonth\":\""
                        + today.getMonth()
                        .toString()
                        .substring(0, 1)
                        .toUpperCase()
                        + today.getMonth()
                        .toString()
                        .substring(1)
                        + " "
                        + currentYear
                        + "\","

                        + "\"monthlyAmount\":"
                        + monthlyTotal
                        + ","

                        + "\"categoryWise\":"
                        + categoryJson

                        + "}";


        sendResponse(
                exchange,
                200,
                report,
                "application/json"
        );
    }


    // =========================================================
    // READ REQUEST BODY
    // =========================================================

    private static String readRequestBody(
            HttpExchange exchange)
            throws IOException {

        return new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );
    }


    // =========================================================
    // ADD EXPENSE
    // =========================================================

    private static boolean addExpenseFromJson(
            String data) {

        try {

            String description =
                    getJsonValue(data, "description");

            String amountText =
                    getJsonValue(data, "amount");

            String categoryName =
                    getJsonValue(data, "category");

            String dateText =
                    getJsonValue(data, "date");


            // Validate empty values
            if (description.isEmpty()
                    || amountText.isEmpty()
                    || categoryName.isEmpty()
                    || dateText.isEmpty()) {

                return false;
            }


            double amount =
                    Double.parseDouble(amountText);

            LocalDate date =
                    LocalDate.parse(dateText);


            int id = getNextId();


            Category category =
                    new Category(
                            id,
                            categoryName
                    );


            Expense expense =
                    new Expense(
                            id,
                            description,
                            amount,
                            category,
                            date
                    );


            manager.addExpense(expense);


            // Save data
            FileManager.saveExpenses(
                    manager.getExpenses()
            );


            return true;

        } catch (Exception e) {

            System.out.println(
                    "Error adding expense: "
                            + e.getMessage()
            );

            return false;
        }
    }


    // =========================================================
    // UPDATE / EDIT EXPENSE
    // =========================================================

    private static boolean updateExpense(
            int id,
            String data) {

        try {

            String description =
                    getJsonValue(data, "description");

            String amountText =
                    getJsonValue(data, "amount");

            String categoryName =
                    getJsonValue(data, "category");

            String dateText =
                    getJsonValue(data, "date");


            // Validate values
            if (description.isEmpty()
                    || amountText.isEmpty()
                    || categoryName.isEmpty()
                    || dateText.isEmpty()) {

                return false;
            }


            double amount =
                    Double.parseDouble(amountText);

            LocalDate date =
                    LocalDate.parse(dateText);


            ArrayList<Expense> expenses =
                    manager.getExpenses();


            for (int i = 0;
                 i < expenses.size();
                 i++) {

                Expense oldExpense =
                        expenses.get(i);


                if (oldExpense.getId() == id) {

                    Category category =
                            new Category(
                                    id,
                                    categoryName
                            );


                    Expense updatedExpense =
                            new Expense(
                                    id,
                                    description,
                                    amount,
                                    category,
                                    date
                            );


                    expenses.set(
                            i,
                            updatedExpense
                    );


                    FileManager.saveExpenses(
                            expenses
                    );


                    return true;
                }
            }


            return false;

        } catch (Exception e) {

            System.out.println(
                    "Error updating expense: "
                            + e.getMessage()
            );

            return false;
        }
    }


    // =========================================================
    // DELETE EXPENSE
    // =========================================================

    private static boolean deleteExpense(int id) {

        ArrayList<Expense> expenses =
                manager.getExpenses();


        for (int i = 0;
             i < expenses.size();
             i++) {

            if (expenses.get(i).getId() == id) {

                expenses.remove(i);


                FileManager.saveExpenses(
                        expenses
                );


                return true;
            }
        }


        return false;
    }


    // =========================================================
    // GET NEXT ID
    // =========================================================

    private static int getNextId() {

        int maxId = 0;


        for (Expense expense :
                manager.getExpenses()) {

            if (expense.getId() > maxId) {

                maxId = expense.getId();
            }
        }


        return maxId + 1;
    }


    // =========================================================
    // GET ID FROM URL
    // =========================================================

    private static int getIdFromQuery(
            String query) {

        try {

            if (query == null) {
                return -1;
            }


            String[] parts =
                    query.split("=");


            if (parts.length < 2) {
                return -1;
            }


            return Integer.parseInt(
                    parts[1]
            );

        } catch (Exception e) {

            return -1;
        }
    }


    // =========================================================
    // CREATE EXPENSE JSON
    // =========================================================

    private static String createJson() {

        StringBuilder json =
                new StringBuilder();


        json.append("[");


        ArrayList<Expense> expenses =
                manager.getExpenses();


        for (int i = 0;
             i < expenses.size();
             i++) {

            Expense expense =
                    expenses.get(i);


            json.append("{");


            // ID
            json.append("\"id\":")
                    .append(expense.getId())
                    .append(",");


            // Description
            json.append("\"description\":\"")
                    .append(
                            escapeJson(
                                    expense.getDescription()
                            )
                    )
                    .append("\",");


            // Amount
            json.append("\"amount\":")
                    .append(expense.getAmount())
                    .append(",");


            // Category
            json.append("\"category\":\"")
                    .append(
                            escapeJson(
                                    expense
                                            .getCategory()
                                            .getName()
                            )
                    )
                    .append(",");


            // Date
            json.append("\"date\":\"")
                    .append(expense.getDate())
                    .append("\"");


            json.append("}");


            if (i < expenses.size() - 1) {
                json.append(",");
            }
        }


        json.append("]");


        return json.toString();
    }


    // =========================================================
    // GET JSON VALUE
    // =========================================================

    private static String getJsonValue(
            String json,
            String key) {

        String search =
                "\"" + key + "\":";


        int start =
                json.indexOf(search);


        if (start == -1) {
            return "";
        }


        start += search.length();


        // Remove spaces
        while (
                start < json.length()
                        &&
                Character.isWhitespace(
                        json.charAt(start)
                )
        ) {

            start++;
        }


        if (start >= json.length()) {
            return "";
        }


        // String value
        if (json.charAt(start) == '"') {

            start++;


            int end =
                    json.indexOf(
                            "\"",
                            start
                    );


            if (end == -1) {
                return "";
            }


            return json.substring(
                    start,
                    end
            );
        }


        // Number value
        int end =
                json.indexOf(
                        ",",
                        start
                );


        if (end == -1) {

            end =
                    json.indexOf(
                            "}",
                            start
                    );
        }


        if (end == -1) {
            return "";
        }


        return json.substring(
                start,
                end
        ).trim();
    }


    // =========================================================
    // CORS
    // =========================================================

    private static void addCorsHeaders(
            HttpExchange exchange) {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*"
        );


        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
        );


        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type"
        );
    }


    // =========================================================
    // SEND RESPONSE
    // =========================================================

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response,
            String contentType)
            throws IOException {

        addCorsHeaders(exchange);


        exchange.getResponseHeaders().set(
                "Content-Type",
                contentType
        );


        byte[] responseBytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );


        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );


        OutputStream output =
                exchange.getResponseBody();


        output.write(responseBytes);

        output.close();
    }


    // =========================================================
    // ESCAPE JSON
    // =========================================================

    private static String escapeJson(
            String text) {

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
