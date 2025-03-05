package xyz.sadiulhakim.employee.pojo;

public record Salary(
        String name,
        String department,
        double salary,
        double totalSalarySoFar
) {
}