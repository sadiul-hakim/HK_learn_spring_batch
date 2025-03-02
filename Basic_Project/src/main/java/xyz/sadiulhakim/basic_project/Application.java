package xyz.sadiulhakim.basic_project;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    private final Job sensorDataJob;

    public Application(JobLauncher jobLauncher, Job sensorDataJob) {
        this.jobLauncher = jobLauncher;
        this.sensorDataJob = sensorDataJob;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            jobLauncher.run(sensorDataJob, new org.springframework.batch.core.JobParameters()); // Jobs with empty parameters
            // can be restarted.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
