package xyz.sadiulhakim.advanced_project.controller;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ApplicationController {

    @Qualifier("asyncJobLauncher")
    private final JobLauncher asyncJobLauncher;

    public ApplicationController(JobLauncher asyncJobLauncher) {
        this.asyncJobLauncher = asyncJobLauncher;
    }

    @PostMapping("/start")
    String start(@RequestParam long scoreIndex) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        String id = UUID.randomUUID().toString();
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("id", id)
                .addLong("scoreIndex", scoreIndex)
                .toJobParameters();

        asyncJobLauncher.run(null, jobParameters);
        return "Job with id " + id + " was submitted";
    }
}
