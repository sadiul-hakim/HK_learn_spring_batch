package xyz.sadiulhakim.employee.config;

import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import xyz.sadiulhakim.employee.pojo.Employee;
import xyz.sadiulhakim.employee.pojo.HighPaidEmployee;
import xyz.sadiulhakim.employee.pojo.Salary;

import javax.sql.DataSource;

@Configuration
public class EmployeePayrollConfig {

    @Value("file:D:\\Hakim_Code\\learn_batch\\staticFiles\\employee\\Employee.csv")
    private Resource employee;

    @Value("file:D:\\Hakim_Code\\learn_batch\\staticFiles\\employee\\Salary.csv")
    private WritableResource salary;

    @Value("file:D:\\Hakim_Code\\learn_batch\\staticFiles\\employee\\High Paid Employee.json")
    private WritableResource highPaidEmployee;

    @Bean
    TaskExecutor employeeTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadFactory(Thread.ofVirtual().factory());
        return taskExecutor;
    }

    @Bean
    @Qualifier("employeeItemReader")
    ItemReader<Employee> employeeItemReader() {
        return new FlatFileItemReaderBuilder<Employee>()
                .name("employeeReader")
                .resource(employee)
                .delimited()
                .delimiter(",")
                .names("name", "department", "hoursWorked", "hourlyRate", "bonus", "deductions")
                .targetType(Employee.class)
                .linesToSkip(1)
                .build();
    }

    @Bean
    @Qualifier("calculatedSalaryReader")
    ItemReader<Salary> calculatedSalaryReader() {
        return new FlatFileItemReaderBuilder<Salary>()
                .name("calculatedSalaryReader")
                .resource(salary)
                .delimited()
                .delimiter(",")
                .names("name", "department", "salary", "totalSalarySoFar")
                .targetType(Salary.class)
                .build();
    }

    @Bean
    @Qualifier("calculatedSalaryWriter")
    ItemWriter<Salary> calculatedSalaryWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Salary>()
                .dataSource(dataSource)
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setString(1, item.name());
                    ps.setString(2, item.department());
                    ps.setDouble(3, item.salary());
                })
                .sql("insert into salary(name,department,salary) values(?,?,?)")
                .build();
    }

    @Bean
    ItemWriter<Salary> salaryItemWriter() {
        return new FlatFileItemWriterBuilder<Salary>()
                .name("salaryWriter")
                .resource(salary)
                .delimited()
                .delimiter(",")
                .names("name", "department", "salary", "totalSalarySoFar")
                .build();
    }

    @Bean
    ItemWriter<HighPaidEmployee> highPaidEmployeeWriter() {
        return new JsonFileItemWriterBuilder<HighPaidEmployee>()
                .name("highPaidEmployeeWriter")
                .resource(highPaidEmployee)
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .build();
    }

    @Bean
    @Qualifier("payrollCalculatorStep")
    Step payrollCalculatorStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        SalaryProcessor salaryProcessor = new SalaryProcessor();
        return new StepBuilder("payrollCalculatorStep", jobRepository)
                .<Employee, Salary>chunk(10_000, transactionManager)
                .reader(employeeItemReader())
                .processor(salaryProcessor)
                .writer(salaryItemWriter())
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        salaryProcessor.setStepExecution(stepExecution);
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        salaryProcessor.setStepExecution(null);
                        return StepExecutionListener.super.afterStep(stepExecution);
                    }
                })
                .allowStartIfComplete(true)
                .taskExecutor(employeeTaskExecutor())
                .build();
    }

    @Bean
    @Qualifier("highPaidEmployeeSeparator")
    Step highPaidEmployeeSeparator(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        HighPaidEmployeeProcessor processor = new HighPaidEmployeeProcessor();
        return new StepBuilder("payrollCalculatorStep", jobRepository)
                .<Employee, HighPaidEmployee>chunk(100, transactionManager)
                .reader(employeeItemReader())
                .processor(processor)
                .writer(highPaidEmployeeWriter())
                .allowStartIfComplete(true)
                .taskExecutor(employeeTaskExecutor())
                .build();
    }

    @Bean
    @Qualifier("salarySavingStep")
    Step salarySavingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                          DataSource dataSource) {
        return new StepBuilder("salarySavingStep", jobRepository)
                .<Salary, Salary>chunk(10_000, transactionManager)
                .reader(calculatedSalaryReader())
                .writer(calculatedSalaryWriter(dataSource))
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    @Qualifier("salaryCalculatorJob")
    Job salaryCalculatorJob(JobRepository jobRepository,
                            @Qualifier("payrollCalculatorStep") Step payrollCalculatorStep,
                            @Qualifier("highPaidEmployeeSeparator") Step highPaidEmployeeSeparator,
                            @Qualifier("salarySavingStep") Step salarySavingStep
    ) {
        return new JobBuilder("salaryCalculatorJob", jobRepository)
                .start(payrollCalculatorStep)
                .next(highPaidEmployeeSeparator)
                .next(salarySavingStep)
                .build();
    }

    private static class SalaryProcessor implements ItemProcessor<Employee, Salary> {

        private StepExecution stepExecution;

        @Override
        public Salary process(Employee item) throws Exception {
            if (stepExecution == null)
                throw new RuntimeException("StepExecution is not set in SalaryProcessor.");

            double totalSalarySoFar = stepExecution.getExecutionContext().getDouble("totalSalarySoFar", 0d);
            double salary = item.calculateSalary();

            stepExecution.getExecutionContext().putDouble("totalSalarySoFar", (salary + totalSalarySoFar));
            return new Salary(item.name(), item.department(), salary, (salary + totalSalarySoFar));
        }

        public void setStepExecution(StepExecution stepExecution) {
            this.stepExecution = stepExecution;
        }
    }

    private static class HighPaidEmployeeProcessor implements ItemProcessor<Employee, HighPaidEmployee> {

        @Override
        public HighPaidEmployee process(Employee item) throws Exception {
            if (item.calculateSalary() > 5_000) {
                return new HighPaidEmployee(item.name(), item.department(), item.calculateSalary());
            }

            return null;
        }

    }
}
