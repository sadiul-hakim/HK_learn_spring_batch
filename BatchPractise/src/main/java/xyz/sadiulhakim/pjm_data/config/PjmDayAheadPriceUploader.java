package xyz.sadiulhakim.pjm_data.config;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import xyz.sadiulhakim.pjm_data.pojo.DayAheadPrice;
import xyz.sadiulhakim.pjm_data.pojo.DayAheadPriceWrapper;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class PjmDayAheadPriceUploader {

    @Value("file:F:\\Batch\\data.json")
    private Resource dayAheadPrice;

    @Bean
    @Qualifier("pjmDayAheadPriceReader")
    ItemReader<DayAheadPriceWrapper> pjmDayAheadPriceReader() {

        return new JsonItemReaderBuilder<DayAheadPriceWrapper>()
                .name("pjmDayAheadPriceReader")
                .jsonObjectReader(new JacksonJsonObjectReader<>(DayAheadPriceWrapper.class))
                .resource(dayAheadPrice)
                .build();
    }

    @Bean
    @Qualifier("pjmDayAheadPriceProcessor")
    ItemProcessor<DayAheadPriceWrapper, DayAheadPriceWrapper> pjmDayAheadPriceProcessor() {
        return item -> {
            System.out.println("Item :: " + item);
            for (DayAheadPrice aheadPrice : item.items()) {
                aheadPrice.setTotal_da(
                        aheadPrice.getTotal_lmp_da() + aheadPrice.getCongestion_price_da()
                                + aheadPrice.getMarginal_loss_price_da() + aheadPrice.getSystem_energy_price_da()
                );
            }

            return item;
        };
    }

//    @Bean
//    @Qualifier("pjmDayAheadPriceWriter")
//    ItemWriter<DayAheadPriceWrapper> pjmDayAheadPriceWriter(DataSource dataSource){
//        return new JdbcBatchItemWriterBuilder<DayAheadPriceWrapper>()
//                .dataSource(dataSource)
//                .
//    }

    @Bean
    @Qualifier("pjmDayAheadPriceUploaderStep")
    Step pjmDayAheadPriceUploaderStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                      @Qualifier("pjmDayAheadPriceReader") ItemReader<DayAheadPriceWrapper> pjmDayAheadPriceReader,
                                      @Qualifier("pjmDayAheadPriceProcessor") ItemProcessor<DayAheadPriceWrapper, DayAheadPriceWrapper> pjmDayAheadPriceProcessor) {
        return new StepBuilder("pjmDayAheadPriceUploaderStep", jobRepository)
                .<DayAheadPriceWrapper, DayAheadPriceWrapper>chunk(1, transactionManager)
                .reader(pjmDayAheadPriceReader)
                .processor(pjmDayAheadPriceProcessor)
                .writer(null)
                .build();
    }
}
