package xyz.sadiulhakim.employee.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    private final JobLauncher jobLauncher;
    private final Job salaryCalculatorJob;

    public EmployeeController(JobLauncher jobLauncher, Job salaryCalculatorJob) {
        this.jobLauncher = jobLauncher;
        this.salaryCalculatorJob = salaryCalculatorJob;
    }

    @GetMapping("/calculate/salary")
    ResponseEntity<?> calculateSalary() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters parameters = new JobParametersBuilder()
                .addString("id", UUID.randomUUID().toString())
                .toJobParameters();

        jobLauncher.run(salaryCalculatorJob, parameters);
        return ResponseEntity.ok("Done");
    }
}
