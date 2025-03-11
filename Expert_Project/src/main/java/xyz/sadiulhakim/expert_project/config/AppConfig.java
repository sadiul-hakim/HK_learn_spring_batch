package xyz.sadiulhakim.expert_project.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import xyz.sadiulhakim.expert_project.pojo.SessionAction;
import xyz.sadiulhakim.expert_project.source.SourceDatabaseUtils;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class AppConfig {

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
    ItemReader<SessionAction> sessionActionItemReader(DataSource sourceDataSource,
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
}
