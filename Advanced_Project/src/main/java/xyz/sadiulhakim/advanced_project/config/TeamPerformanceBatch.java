package xyz.sadiulhakim.advanced_project.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import xyz.sadiulhakim.advanced_project.pojo.Team;

@Configuration
@EnableScheduling
@EnableBatchProcessing
public class TeamPerformanceBatch {

    @Value("file:d:\\Hakim_Code\\learn_batch\\staticFiles\\advanced\\input\\")
    private Resource inputFolderPath;

    @Value("file:d:\\Hakim_Code\\learn_batch\\staticFiles\\advanced\\output\\avg.txt")
    private WritableResource avgOutputFile;

    @Value("file:d:\\Hakim_Code\\learn_batch\\staticFiles\\advanced\\output\\min.txt")
    private WritableResource minOutputFile;

    @Value("file:d:\\Hakim_Code\\learn_batch\\staticFiles\\advanced\\output\\max.txt")
    private WritableResource maxOutputFile;

    @Bean
    @Qualifier("asyncJobLauncher")
    JobLauncher asyncJobLauncher(JobRepository jobRepository) {
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setVirtualThreads(true);

        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(simpleAsyncTaskExecutor);
        return jobLauncher;
    }

    @Bean
    @Qualifier("teamReader")
    ItemReader<Team> teamReader() {

        // Takes a file from FlatFileItemReader reads sequentially.
        // It keeps the current resource open until reading is done and it also remembers where it left off.
        FlatFileItemReader<String> lineReader = new FlatFileItemReaderBuilder<String>()
                .name("lineReader")
                .lineMapper((line, lineNumber) -> line) // Return the same line so that we can process in ResourceAwareItemReaderItemStream
                .build();

        // Takes file from MultiFileTeamReader one by one and passes it to FlatFileItemReader.
        MultiFileTeamReader reader = new MultiFileTeamReader(lineReader);

        // MultiResourceItemReaderBuilder takes multiple files as input then delegates files to MultiFileTeamReader
        // one by one.
        return new MultiResourceItemReaderBuilder<Team>()
                .name("teamReader")
                .resources(inputFolderPath)
                .delegate(reader)
                .build();
    }
}
