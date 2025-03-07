package xyz.sadiulhakim.advanced_project.config;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import xyz.sadiulhakim.advanced_project.pojo.Team;
import xyz.sadiulhakim.advanced_project.pojo.TeamPerformance;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Configuration
@EnableScheduling
@EnableBatchProcessing
public class TeamPerformanceBatch {

    @Value("classpath:input/*.txt")
    private Resource[] inputFolderPath;

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
    @StepScope
    @Qualifier("teamAverageProcessor")
    TeamAverageProcessor teamAverageProcessor(@Value("#{jobParameters['scoreIndex']}") int scoreIndex) {
        return new TeamAverageProcessor(scoreIndex);
    }

    @Bean
    @Qualifier("teamReader")
    ItemReader<Team> teamReader() {

        // Takes a file from FlatFileItemReader reads sequentially.
        // It keeps the current resource open until reading is done, and it also remembers where it left off.
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

    @Bean
    @Qualifier("teamAverageWriter")
    ItemWriter<AverageScore> teamAverageWriter() {
        return new FlatFileItemWriterBuilder<AverageScore>()
                .name("teamAverageWriter")
                .resource(avgOutputFile)
                .delimited()
                .delimiter(",")
//                .names("name","averageScore")
                .fieldExtractor(item -> new Object[]{item.name(), item.averageScore()})
                .build();
    }

    @Bean
    @Qualifier("playerInfoPromoter")
    ExecutionContextPromotionListener playerInfoPromoter() {
        ExecutionContextPromotionListener promotionListener = new ExecutionContextPromotionListener();

        // Promote these keys/values to Job Execution Listener
        promotionListener.setKeys(new String[]{
                TeamAverageProcessor.MAX_PLAYER,
                TeamAverageProcessor.MIN_PLAYER,
                TeamAverageProcessor.MAX_SCORE,
                TeamAverageProcessor.MIN_SCORE,
        });
        return promotionListener;
    }

    @Bean
    @Qualifier("teamAverageStep")
    Step teamAverageStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                         @Qualifier("teamReader") ItemReader<Team> teamReader,
                         @Qualifier("teamAverageProcessor") TeamAverageProcessor teamAverageProcessor,
                         @Qualifier("teamAverageWriter") ItemWriter<AverageScore> teamAverageWriter,
                         @Qualifier("playerInfoPromoter") ExecutionContextPromotionListener promotionListener) {
        return new StepBuilder("teamAverageStep", jobRepository)
                .<Team, AverageScore>chunk(5, transactionManager)
                .reader(teamReader)
                .processor(teamAverageProcessor)
                .writer(teamAverageWriter)
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        teamAverageProcessor.setStepExecution(stepExecution);
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        teamAverageProcessor.setStepExecution(null);
                        System.out.println("Done teamAverageStep Step.");
                        return StepExecutionListener.super.afterStep(stepExecution);
                    }
                })
                .listener(promotionListener)
                .allowStartIfComplete(true)
                .build();
    }

    //+---------------------------------------------+--------------------------------------------+

    @Bean
    @Qualifier("averageScoreReader")
    ItemReader<AverageScore> averageScoreReader() {
        return new FlatFileItemReaderBuilder<AverageScore>()
                .name("averageScoreReader")
                .resource(avgOutputFile)
                .delimited()
                .delimiter(",")
                .names("name", "averageScore")
                .targetType(AverageScore.class)
                .build();
    }

    @Bean
    @StepScope
    @Qualifier("maxRatioPerformanceProcessor")
    ItemProcessor<AverageScore, TeamPerformance> maxRatioPerformanceProcessor(@Value("#{jobExecutionContext['max.score']}") double maxScore) {
        return averageScore -> process(averageScore, maxScore);
    }

    @Bean
    @StepScope
    @Qualifier("minRatioPerformanceProcessor")
    ItemProcessor<AverageScore, TeamPerformance> minRatioPerformanceProcessor(@Value("#{jobExecutionContext['min.score']}") double minScore) {
        return averageScore -> process(averageScore, minScore);
    }

    // Method which is processing average scored item into the team performance given the baseline score
    // Performance is represented as "X%" string, where X = score * 100 / baseline, with up to 2 precision
    private static TeamPerformance process(AverageScore team, double baselineScore) {
        BigDecimal performance = BigDecimal.valueOf(team.averageScore())
                .multiply(new BigDecimal(100))
                .divide(BigDecimal.valueOf(baselineScore), 2, RoundingMode.HALF_UP);
        return new TeamPerformance(team.name(), performance + "%");
    }

    @Bean
    @Qualifier("teamMaxPerformanceStep")
    Step teamMaxPerformanceStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                @Qualifier("maxRatioPerformanceProcessor") ItemProcessor<AverageScore, TeamPerformance> maxRatioPerformanceProcessor) {
        return new StepBuilder("teamMaxPerformanceStep", jobRepository)
                .<AverageScore, TeamPerformance>chunk(5, transactionManager)
                .reader(averageScoreReader())
                .processor(maxRatioPerformanceProcessor)
                .writer(new FlatFileItemWriterBuilder<TeamPerformance>()
                        .name("teamMaxPerformanceWriter")
                        .resource(maxOutputFile)
                        .delimited()
                        .delimiter(",")
                        .fieldExtractor(item -> new Object[]{item.name(), item.performance()})
                        .build()
                )
                .build();
    }

    @Bean
    @Qualifier("teamMinPerformanceStep")
    Step teamMinPerformanceStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                @Qualifier("minRatioPerformanceProcessor") ItemProcessor<AverageScore, TeamPerformance> minRatioPerformanceProcessor) {
        return new StepBuilder("teamMinPerformanceStep", jobRepository)
                .<AverageScore, TeamPerformance>chunk(5, transactionManager)
                .reader(averageScoreReader())
                .processor(minRatioPerformanceProcessor)
                .writer(new FlatFileItemWriterBuilder<TeamPerformance>()
                        .name("teamMinPerformanceWriter")
                        .resource(minOutputFile)
                        .delimited()
                        .delimiter(",")
                        .fieldExtractor(item -> new Object[]{item.name(), item.performance()})
                        .build()
                )
                .build();
    }

    @Bean
    @Qualifier("averageScoreCalculatorJob")
    Job averageScoreCalculatorJob(JobRepository jobRepository,
                                  @Qualifier("teamAverageStep") Step teamAverageStep) {
        return new JobBuilder("averageScoreCalculatorJob", jobRepository)
                .start(teamAverageStep)
                .build();
    }
}
