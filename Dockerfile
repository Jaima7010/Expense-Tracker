FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY *.java ./
COPY expenses.json ./

RUN javac *.java

EXPOSE 8080

CMD ["java", "ExpenseServer"]