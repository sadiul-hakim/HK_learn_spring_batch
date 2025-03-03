package xyz.sadiulhakim.inter_project.batch;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
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

import javax.sql.DataSource;

@Configuration
public class BankTransactionJobConfig extends DefaultBatchConfiguration {

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
    @Qualifier("fillBalanceStep")
    Step fillBalanaceStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                          DataSource dataSource) {
        return new StepBuilder("fillBalanceStep", jobRepository)
                .<BankTransaction, BalanceUpdate>chunk(10, transactionManager)
                .reader(bankTransactionReader(dataSource))
                .processor(null)
                .writer(null)
                .build();
    }


}
