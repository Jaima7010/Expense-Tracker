
const API_URL = "http://localhost:8080/expenses";

let expenses = [];
let editingExpenseId = null;
let categoryChart = null;


// ===============================
// LOAD EXPENSES
// ===============================

async function loadExpenses() {

    try {

        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Server error");
        }

        expenses = await response.json();

        updateDashboard();
        displayExpenses();
        updateCategoryChart();
        loadReport();

    } catch (error) {

        console.error(
            "Backend connection failed:",
            error
        );

        alert(
            "Java backend is not connected. " +
            "Please start ExpenseServer."
        );
    }
}


// ===============================
// ADD / UPDATE EXPENSE
// ===============================

const expenseForm =
    document.getElementById("expenseForm");


expenseForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();

        const description =
            document.getElementById("description")
                .value.trim();

        const amount =
            Number(
                document.getElementById("amount").value
            );

        const category =
            document.getElementById("category").value;

        const date =
            document.getElementById("date").value;


        if (
            !description ||
            amount <= 0 ||
            !category ||
            !date
        ) {

            alert("Please fill all the details.");

            return;
        }


        const expense = {

            description: description,

            amount: amount,

            category: category,

            date: date
        };


        try {

            let response;


            // ===============================
            // UPDATE
            // ===============================

            if (editingExpenseId !== null) {

                response =
                    await fetch(
                        API_URL +
                        "?id=" +
                        editingExpenseId,
                        {
                            method: "PUT",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify(expense)
                        }
                    );


                if (!response.ok) {

                    throw new Error(
                        "Failed to update expense"
                    );
                }


                alert(
                    "Expense updated successfully!"
                );


                editingExpenseId = null;

                expenseForm.reset();

                changeFormToAddMode();

                await loadExpenses();

                return;
            }


            // ===============================
            // ADD
            // ===============================

            response =
                await fetch(
                    API_URL,
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(expense)
                    }
                );


            if (!response.ok) {

                throw new Error(
                    "Failed to save expense"
                );
            }


            alert(
                "Expense added successfully!"
            );


            expenseForm.reset();

            await loadExpenses();


        } catch (error) {

            console.error(error);

            alert(
                "Could not save expense. " +
                "Make sure ExpenseServer is running."
            );
        }
    }
);


// ===============================
// EDIT EXPENSE
// ===============================

function editExpense(id) {

    const expense =
        expenses.find(
            item => item.id === id
        );


    if (!expense) {

        alert("Expense not found.");

        return;
    }


    editingExpenseId = id;


    document.getElementById(
        "description"
    ).value =
        expense.description;


    document.getElementById(
        "amount"
    ).value =
        expense.amount;


    document.getElementById(
        "category"
    ).value =
        expense.category;


    document.getElementById(
        "date"
    ).value =
        expense.date;


    changeFormToEditMode();

    showAddExpense();
}


// ===============================
// EDIT MODE
// ===============================

function changeFormToEditMode() {

    const button =
        expenseForm.querySelector(
            "button[type='submit']"
        );


    if (button) {

        button.textContent =
            "Update Expense";
    }


    const heading =
        document.querySelector(
            "#add-expense h2"
        );


    if (heading) {

        heading.textContent =
            "Edit Expense";
    }
}


// ===============================
// ADD MODE
// ===============================

function changeFormToAddMode() {

    const button =
        expenseForm.querySelector(
            "button[type='submit']"
        );


    if (button) {

        button.textContent =
            "Save Expense";
    }


    const heading =
        document.querySelector(
            "#add-expense h2"
        );


    if (heading) {

        heading.textContent =
            "Add New Expense";
    }
}


// ===============================
// DELETE EXPENSE
// ===============================

async function deleteExpense(id) {

    const confirmDelete =
        confirm(
            "Are you sure you want to delete this expense?"
        );


    if (!confirmDelete) {

        return;
    }


    try {

        const response =
            await fetch(
                API_URL +
                "?id=" +
                id,
                {
                    method: "DELETE"
                }
            );


        if (!response.ok) {

            throw new Error(
                "Failed to delete expense"
            );
        }


        alert(
            "Expense deleted successfully!"
        );


        await loadExpenses();


    } catch (error) {

        console.error(error);

        alert(
            "Could not delete expense."
        );
    }
}


// ===============================
// UPDATE DASHBOARD
// ===============================

function updateDashboard() {

    let total = 0;


    for (let expense of expenses) {

        total += Number(
            expense.amount
        );
    }


    document.getElementById(
        "totalAmount"
    ).textContent =
        "₹" + total.toFixed(2);


    document.getElementById(
        "expenseCount"
    ).textContent =
        expenses.length;


    const today = new Date();


    const currentYear =
        today.getFullYear();


    const currentMonth =
        today.getMonth() + 1;


    let monthlyTotal = 0;


    for (let expense of expenses) {

        const expenseDate =
            new Date(expense.date);


        if (
            expenseDate.getFullYear()
                === currentYear
            &&
            expenseDate.getMonth() + 1
                === currentMonth
        ) {

            monthlyTotal +=
                Number(expense.amount);
        }
    }


    document.getElementById(
        "monthlyAmount"
    ).textContent =
        "₹" +
        monthlyTotal.toFixed(2);


    document.getElementById(
        "summaryAmount"
    ).textContent =
        "₹" +
        monthlyTotal.toFixed(2);


    document.getElementById(
        "currentMonth"
    ).textContent =
        today.toLocaleString(
            "default",
            {
                month: "long",
                year: "numeric"
            }
        );
}


// ===============================
// DISPLAY EXPENSES
// ===============================

function displayExpenses() {

    const expenseList =
        document.getElementById(
            "expenseList"
        );


    if (expenses.length === 0) {

        expenseList.innerHTML =
            `
            <div class="empty-message">
                No expenses added yet.
            </div>
            `;

        return;
    }


    expenseList.innerHTML = "";


    const reversedExpenses =
        [...expenses].reverse();


    for (let expense of reversedExpenses) {

        const item =
            document.createElement("div");


        item.className =
            "expense-item";


        item.innerHTML =
            `
            <div class="expense-info">

                <h3>
                    ${escapeHTML(
                        expense.description
                    )}
                </h3>

                <p>
                    ${escapeHTML(
                        expense.category
                    )}
                    •
                    ${expense.date}
                </p>

            </div>


            <div class="expense-actions">

                <div class="expense-amount">

                    ₹${Number(
                        expense.amount
                    ).toFixed(2)}

                </div>


                <button
                    class="edit-btn"
                    onclick="editExpense(${expense.id})"
                >
                    Edit
                </button>


                <button
                    class="delete-btn"
                    onclick="deleteExpense(${expense.id})"
                >
                    Delete
                </button>

            </div>
            `;


        expenseList.appendChild(item);
    }
}


// ===============================
// CATEGORY-WISE SPENDING
// ===============================

function updateCategoryChart() {

    const chartCanvas =
        document.getElementById(
            "categoryChart"
        );


    if (
        !chartCanvas ||
        typeof Chart === "undefined"
    ) {

        return;
    }


    const categoryTotals = {};


    for (let expense of expenses) {

        const category =
            expense.category;


        const amount =
            Number(expense.amount);


        if (!categoryTotals[category]) {

            categoryTotals[category] = 0;
        }


        categoryTotals[category] +=
            amount;
    }


    const categories =
        Object.keys(categoryTotals);


    const amounts =
        Object.values(categoryTotals);


    if (categoryChart) {

        categoryChart.destroy();
    }


    if (categories.length === 0) {

        return;
    }


    categoryChart =
        new Chart(
            chartCanvas,
            {
                type: "doughnut",

                data: {

                    labels: categories,

                    datasets: [
                        {
                            label: "Spending",

                            data: amounts
                        }
                    ]
                },

                options: {

                    responsive: true,

                    plugins: {

                        legend: {

                            position: "bottom"
                        },

                        tooltip: {

                            callbacks: {

                                label:
                                    function (
                                        context
                                    ) {

                                        return (
                                            context.label +
                                            ": ₹" +
                                            context.parsed
                                                .toFixed(2)
                                        );
                                    }
                            }
                        }
                    }
                }
            }
        );


    displayCategoryDetails(
        categoryTotals
    );
}


// ===============================
// CATEGORY DETAILS
// ===============================

function displayCategoryDetails(
    categoryTotals
) {

    const container =
        document.getElementById(
            "categoryDetails"
        );


    if (!container) {

        return;
    }


    container.innerHTML = "";


    const categories =
        Object.keys(categoryTotals);


    categories.sort(
        function (a, b) {

            return (
                categoryTotals[b] -
                categoryTotals[a]
            );
        }
    );


    for (
        let category
        of categories
    ) {

        const amount =
            categoryTotals[category];


        const item =
            document.createElement("div");


        item.className =
            "category-detail";


        item.innerHTML =
            `
            <span>
                ${escapeHTML(category)}
            </span>

            <strong>
                ₹${amount.toFixed(2)}
            </strong>
            `;


        container.appendChild(item);
    }
}


// ===============================
// LOAD REPORT FROM JAVA
// ===============================

async function loadReport() {

    try {

        const response =
            await fetch(
                "http://localhost:8080/report"
            );


        if (!response.ok) {

            throw new Error(
                "Report server error"
            );
        }


        const report =
            await response.json();


        const totalExpenses =
            document.getElementById(
                "reportTotalExpenses"
            );


        const totalAmount =
            document.getElementById(
                "reportTotalAmount"
            );


        const currentMonth =
            document.getElementById(
                "reportCurrentMonth"
            );


        const monthlyAmount =
            document.getElementById(
                "reportMonthlyAmount"
            );


        const categoryList =
            document.getElementById(
                "reportCategoryList"
            );


        // If Report section is not added yet
        if (
            !totalExpenses ||
            !totalAmount ||
            !currentMonth ||
            !monthlyAmount ||
            !categoryList
        ) {

            return;
        }


        totalExpenses.textContent =
            report.totalExpenses;


        totalAmount.textContent =
            "₹" +
            Number(
                report.totalAmount
            ).toFixed(2);


        currentMonth.textContent =
            report.currentMonth;


        monthlyAmount.textContent =
            "₹" +
            Number(
                report.monthlyAmount
            ).toFixed(2);


        categoryList.innerHTML = "";


        const categories =
            Object.entries(
                report.categoryWise || {}
            );


        if (categories.length === 0) {

            categoryList.innerHTML =
                "<p>No category data available.</p>";

            return;
        }


        categories.forEach(
            function ([category, amount]) {

                const item =
                    document.createElement("div");


                item.className =
                    "report-category-item";


                item.innerHTML =
                    `
                    <span>
                        ${escapeHTML(category)}
                    </span>

                    <strong>
                        ₹${Number(
                            amount
                        ).toFixed(2)}
                    </strong>
                    `;


                categoryList.appendChild(item);
            }
        );


    } catch (error) {

        console.error(
            "Report loading failed:",
            error
        );
    }
}


// ===============================
// ESCAPE HTML
// ===============================

function escapeHTML(text) {

    const div =
        document.createElement("div");


    div.textContent = text;


    return div.innerHTML;
}


// ===============================
// ADD EXPENSE BUTTON
// ===============================

function showAddExpense() {

    document
        .getElementById("add-expense")
        .scrollIntoView({
            behavior: "smooth"
        });
}


// ===============================
// START
// ===============================

loadExpenses();