package xyz.sadiulhakim.expert_project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.AbstractJob;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamWriter;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import xyz.sadiulhakim.expert_project.pojo.SessionAction;
import xyz.sadiulhakim.expert_project.pojo.SessionActionPartitioner;
import xyz.sadiulhakim.expert_project.pojo.UserScoreUpdate;
import xyz.sadiulhakim.expert_project.source.SourceDatabaseUtils;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class UserScoreProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserScoreProcessor.class);

    @Bean
    @Qualifier("singleThreadJob")
    AbstractJob singleThreadJob(JobRepository jobRepository,
                                @Qualifier("partitionStep") Step partitionStep) {
        return (AbstractJob) new JobBuilder("singleThreadJob", jobRepository)
                .start(partitionStep)
                .build();
    }

    @Bean
    @Qualifier("asyncJobLauncher")
    JobLauncher asyncJobLauncher(JobRepository jobRepository) {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setVirtualThreads(true);

        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setTaskExecutor(taskExecutor);
        jobLauncher.setJobRepository(jobRepository);
        return jobLauncher;
    }

    @Bean
    @StepScope
    @Qualifier("sessionActionReader")
    ItemStreamReader<SessionAction> sessionActionItemReader(DataSource sourceDataSource,
                                                            @Value("#{stepExecutionContext['partitionCount']}") Integer partitionCount,
                                                            @Value("#{stepExecutionContext['partitionIndex']}") Integer partitionIndex) {
        // Select all in case no partition properties passed; select partition-specific records otherwise
        PagingQueryProvider queryProvider = (partitionCount == null || partitionIndex == null)
                ? SourceDatabaseUtils.selectAllSessionActionsProvider(SessionAction.SESSION_ACTION_TABLE_NAME)
                : SourceDatabaseUtils
                .selectPartitionOfSessionActionsProvider(SessionAction.SESSION_ACTION_TABLE_NAME, partitionCount, partitionIndex);
        return new JdbcPagingItemReaderBuilder<SessionAction>()
                .name("sessionActionReader")
                .dataSource(sourceDataSource)
                .queryProvider(queryProvider)
                .rowMapper(SourceDatabaseUtils.getSessionActionMapper())
                .pageSize(5)
                .build();
    }

    ItemWriter<UserScoreUpdate> userScoreUpdateItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<UserScoreUpdate>()
                .dataSource(dataSource)
                .itemPreparedStatementSetter(SourceDatabaseUtils.UPDATE_USER_SCORE_PARAMETER_SETTER)
                .sql(SourceDatabaseUtils.constructUpdateUserScoreQuery(UserScoreUpdate.USER_SCORE_TABLE_NAME))
                .build();
    }

    // TODO : 3
    @Bean
    @Qualifier("partitionStep")
    Step partitionStep(JobRepository jobRepository,
                       @Qualifier("singleThreadUserScoreStep") Step singleThreadUserScoreStep) {
        return new StepBuilder("partitionStep", jobRepository)
                .partitioner("partitionStepPartitioner", new SessionActionPartitioner())
                .step(singleThreadUserScoreStep) // Use singleThreadUserScoreStep
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .gridSize(3) // 3 means 3 threads and 3 partitions
                .build();
    }


    // TODO : 1
    @Bean
    @Qualifier("singleThreadUserScoreStep")
    Step singleThreadUserScoreStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                   @Qualifier("sessionActionReader") ItemReader<SessionAction> sessionActionReader,
                                   DataSource dataSource) {
        return new StepBuilder("singleThreadUserScoreStep", jobRepository)
                .<SessionAction, UserScoreUpdate>chunk(5, transactionManager)
                .reader(sessionActionReader)
                .processor(getSessionActionProcessor())
                .writer(userScoreUpdateItemWriter(dataSource))
                .listener(beforeStepLoggerListener())
                .build();
    }

    // TODO : 2
    @Bean
    @Qualifier("multiThreadUserScoreStep")
    Step multiThreadUserScoreStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                  @Qualifier("sessionActionReader") ItemStreamReader<SessionAction> sessionActionReader,
                                  DataSource dataSource) {

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(3);
        taskExecutor.setThreadFactory(Thread.ofPlatform().factory());
        taskExecutor.initialize();

        return new StepBuilder("multiThreadUserScoreStep", jobRepository)
                .<SessionAction, UserScoreUpdate>chunk(5, transactionManager)
                .reader(
                        new SynchronizedItemStreamReaderBuilder<SessionAction>()
                                .delegate(sessionActionReader)
                                .build()
                )
                .processor(getSessionActionProcessor())
//                .writer(new SynchronizedItemStreamWriterBuilder<UserScoreUpdate>()
//                        .delegate()
//                        .build()
//                )
                .writer(userScoreUpdateItemWriter(dataSource))
                .listener(beforeStepLoggerListener())
                .taskExecutor(taskExecutor)
                .build();
    }

    // Processor to process single session action item
    private static ItemProcessor<SessionAction, UserScoreUpdate> getSessionActionProcessor() {
        return sessionAction -> {
            if (SourceDatabaseUtils.PLUS_TYPE.equals(sessionAction.actionType())) {
                return new UserScoreUpdate(sessionAction.userId(), sessionAction.amount(), 1d);
            } else if (SourceDatabaseUtils.MULTI_TYPE.equals(sessionAction.actionType())) {
                return new UserScoreUpdate(sessionAction.userId(), 0d, sessionAction.amount());
            } else {
                throw new RuntimeException("Unknown session action record type: " + sessionAction.actionType());
            }
        };
    }

    // Step execution listener that logs information about step and environment (thread) right before the start of the execution
    private static StepExecutionListener beforeStepLoggerListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                int partitionCount = stepExecution.getExecutionContext().getInt(SessionActionPartitioner.PARTITION_COUNT, -1);
                int partitionIndex = stepExecution.getExecutionContext().getInt(SessionActionPartitioner.PARTITION_INDEX, -1);
                if (partitionIndex == -1 || partitionCount == -1) {
                    LOGGER.warn("Calculation step is about to start handling all session action records");
                } else {
                    String threadName = Thread.currentThread().getName();
                    LOGGER.warn("Calculation step is about to start handling partition " + partitionIndex
                            + " out of total " + partitionCount + " partitions in the thread -> " + threadName);
                }
            }
        };
    }
}
