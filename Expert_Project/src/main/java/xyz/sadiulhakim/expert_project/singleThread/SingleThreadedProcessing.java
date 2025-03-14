package xyz.sadiulhakim.expert_project.singleThread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.AbstractJob;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import xyz.sadiulhakim.expert_project.pojo.SessionAction;
import xyz.sadiulhakim.expert_project.pojo.SessionActionPartitioner;
import xyz.sadiulhakim.expert_project.pojo.UserScoreUpdate;
import xyz.sadiulhakim.expert_project.source.SourceDatabaseUtils;

import javax.sql.DataSource;

@Configuration
public class SingleThreadedProcessing {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleThreadedProcessing.class);

    @Bean
    @Qualifier("singleThreadJob")
    AbstractJob singleThreadJob(JobRepository jobRepository,
                                @Qualifier("singleThreadUserScoreStep") Step singleThreadUserScoreStep) {
        return (AbstractJob) new JobBuilder("singleThreadJob", jobRepository)
                .start(singleThreadUserScoreStep)
                .build();
    }

    @Bean
    @Qualifier("singleThreadUserScoreStep")
    Step singleThreadUserScoreStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                   @Qualifier("sessionActionReader") ItemReader<SessionAction> sessionActionReader,
                                   DataSource dataSource) {
        return new StepBuilder("singleThreadUserScoreStep", jobRepository)
                .<SessionAction, UserScoreUpdate>chunk(5, transactionManager)
                .reader(sessionActionReader)
                .processor(getSessionActionProcessor())
                .writer(new JdbcBatchItemWriterBuilder<UserScoreUpdate>()
                        .dataSource(dataSource)
                        .itemPreparedStatementSetter(SourceDatabaseUtils.UPDATE_USER_SCORE_PARAMETER_SETTER)
                        .sql(SourceDatabaseUtils.constructUpdateUserScoreQuery(UserScoreUpdate.USER_SCORE_TABLE_NAME))
                        .build()
                )
                .listener(beforeStepLoggerListener())
                .taskExecutor()
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
