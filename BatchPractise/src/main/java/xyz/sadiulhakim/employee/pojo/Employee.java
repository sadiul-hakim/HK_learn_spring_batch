package xyz.sadiulhakim.employee.pojo;

public record Employee(
        String name,
        String department,
        double hoursWorked,
        double hourlyRate,
        double bonus,
        double deductions
) {

    public double calculateSalary() {
        return (hoursWorked * hourlyRate) + bonus - deductions;
    }
}
