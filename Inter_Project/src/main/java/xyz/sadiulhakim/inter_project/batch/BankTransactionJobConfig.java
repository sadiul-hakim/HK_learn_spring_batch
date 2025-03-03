package xyz.sadiulhakim.inter_project.batch;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import xyz.sadiulhakim.inter_project.pojo.BalanceUpdate;
import xyz.sadiulhakim.inter_project.pojo.BankTransaction;
import xyz.sadiulhakim.inter_project.pojo.DailyBalance;
import xyz.sadiulhakim.inter_project.pojo.MerchantMonthBalance;
import xyz.sadiulhakim.inter_project.util.SourceManagementUtils;

import javax.sql.DataSource;

@Configuration
public class BankTransactionJobConfig extends DefaultBatchConfiguration {

    // Constants for exit statuses of the fill balance step
    public static final String POSITIVE = "POSITIVE";
    public static final String NEGATIVE = "NEGATIVE";

    @Bean
    @Qualifier("bankTransactionReader")
    JdbcCursorItemReader<BankTransaction> bankTransactionReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<BankTransaction>()
                .name("bankTransactionReader")
                .dataSource(dataSource)
                .sql(BankTransaction.SELECT_ALL_QUERY)
                .rowMapper(BankTransaction.ROW_MAPPER)
                .build();
    }

    @Bean
    @Qualifier("merchantMonthlyAmountReader")
    ItemReader<MerchantMonthBalance> merchantMonthlyAmountReader(DataSource dataSource) {
        return new JdbcPagingItemReaderBuilder<MerchantMonthBalance>()
                .name("merchantMonthlyAmountReader")
                .dataSource(dataSource)
                .queryProvider(MerchantMonthBalance.getQueryProvider())
                .rowMapper(MerchantMonthBalance.ROW_MAPPER)
                .pageSize(5)
                .build();
    }

    @Bean
    @Qualifier("dailyBalanceReader")
    ItemReader<DailyBalance> dailyBalanceReader(DataSource dataSource) {
        return new JdbcPagingItemReaderBuilder<DailyBalance>()
                .name("dailyBalanceReader")
                .dataSource(dataSource)
                .queryProvider(DailyBalance.getQueryProvider())
                .rowMapper(DailyBalance.ROW_MAPPER)
                .pageSize(5)
                .build();
    }

    @Bean
    @Qualifier("balanceWriter")
    ItemWriter<BalanceUpdate> balanceWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<BalanceUpdate>()
                .dataSource(dataSource)
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setBigDecimal(1, item.balance());
                    ps.setLong(2, item.id());
                })
                .sql("update bank_transaction_yearly set balance = ? where id = ?")
                .build();
    }

    @Bean
    @Qualifier("fillBalanceStep")
    Step fillBalanaceStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                          DataSource dataSource) {

        FillBalanceProcessor processor = new FillBalanceProcessor();
        return new StepBuilder("fillBalanceStep", jobRepository)
                .<BankTransaction, BalanceUpdate>chunk(10, transactionManager)
                .reader(bankTransactionReader(dataSource))
                .processor(processor)
                .writer(balanceWriter(dataSource))
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        SourceManagementUtils.addBalanceColumn(dataSource);
                        processor.setStepExecution(stepExecution);
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        double totalBalance = processor.getLatestTransactionBalance();
                        processor.setStepExecution(null);
                        return new ExitStatus(totalBalance >= 0 ? POSITIVE : NEGATIVE);
                    }
                })
                .build();
    }
}
