package xyz.sadiulhakim.expert_project.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class JobController {

    @Qualifier("asyncJobLauncher")
    private final JobLauncher jobLauncher;

    @Qualifier("singleThreadJob")
    private final Job singleThreadJob;

    public JobController(JobLauncher jobLauncher, Job singleThreadJob) {
        this.jobLauncher = jobLauncher;
        this.singleThreadJob = singleThreadJob;
    }

    @GetMapping("/single-thread-job")
    ResponseEntity<?> singleThreadJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters parameters = new JobParametersBuilder()
                .addString("id", UUID.randomUUID().toString())
                .toJobParameters();

        jobLauncher.run(singleThreadJob, parameters);
        return ResponseEntity.ok("Done");
    }
}
