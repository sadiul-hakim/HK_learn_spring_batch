package xyz.sadiulhakim.inter_project;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xyz.sadiulhakim.inter_project.util.GenerateSourceDatabase;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private final GenerateSourceDatabase generateSourceDatabase;
    private final JobLauncher jobLauncher;
    private final Job bankTransactionJob;

    public Application(GenerateSourceDatabase generateSourceDatabase, JobLauncher jobLauncher, Job bankTransactionJob) {
        this.generateSourceDatabase = generateSourceDatabase;
        this.jobLauncher = jobLauncher;
        this.bankTransactionJob = bankTransactionJob;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            generateSourceDatabase.generate();
            jobLauncher.run(bankTransactionJob, new JobParameters());
        } catch (Exception ignore) {
        }
    }
}
