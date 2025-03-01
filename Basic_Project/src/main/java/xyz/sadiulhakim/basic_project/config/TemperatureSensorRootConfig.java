package xyz.sadiulhakim.basic_project.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.transaction.PlatformTransactionManager;
import xyz.sadiulhakim.basic_project.pojo.RawDailySensorData;
import xyz.sadiulhakim.basic_project.pojo.SensorData;

@Configuration
public class TemperatureSensorRootConfig extends DefaultBatchConfiguration {

    @Value("file:D:\\Hakim_Code\\learn_batch\\staticFiles\\basic\\HTE2NP.txt")
    Resource rawDailyInput;

    @Value("file:D:\\Hakim_Code\\learn_batch\\staticFiles\\basic\\output\\HTE2NP.xml")
    WritableResource rawDailyOutput;

    @Value("file:D:\\Hakim_Code\\learn_batch\\staticFiles\\basic\\output\\HTE2NP.csv")
    WritableResource rawDailyOutputInCsv;

    @Bean
    @Qualifier("rawDataReader")
    FlatFileItemReader<RawDailySensorData> rawDataReader() {
        return new FlatFileItemReaderBuilder<RawDailySensorData>()
                .name("Raw Data Reader")
                .resource(rawDailyInput)
                .lineMapper(new SensorDataTextMapper())
                .build();
    }

    @Bean
    @Qualifier("sensorDataReader")
    StaxEventItemReader<SensorData> sensorDataReader() {
        return new StaxEventItemReaderBuilder<SensorData>()
                .name("Sensor Data Reader")
                .unmarshaller(SensorData.getMarshaller())
                .resource(rawDailyOutput)
                .addFragmentRootElements(SensorData.ROOT_FRAGMENT_NAME)
                .build();
    }

    @Bean
    @Qualifier("sensorDataWriter")
    StaxEventItemWriter<SensorData> sensorDataWriter() {
        return new StaxEventItemWriterBuilder<SensorData>()
                .name("sensorDataWriter")
                .marshaller(SensorData.getMarshaller())
                .resource(rawDailyOutput)
                .rootTagName("data")
                .overwriteOutput(true)
                .build();
    }

    @Bean
    @Qualifier("makeXmlStep")
    Step makeXmlStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("makeXmlStep", jobRepository)
                .<RawDailySensorData, SensorData>chunk(1, platformTransactionManager)

                // Out FlatFileItemRead would read one line then
                // convert it into RawDailySensorData and pass it to ItemProcessor then
                // ItemProcessor would do some processing and convert that RawDailySensorData into SensorData then
                // StaxEventItemWriter would write it to destination.
                // So, here batch works with only one item at a time.
                .reader(rawDataReader())
                .processor(new RawToSensorDataItemProcessor())
                .writer(sensorDataWriter())
                .build();
    }

    @Bean
    @Qualifier("sensorDataJob")
    Job sensorDataJob(JobRepository jobRepository,
                      @Qualifier("makeXmlStep") Step makeXmlStep) {
        return new JobBuilder("sensorDataJob", jobRepository)
                .start(makeXmlStep)
                .build();
    }
}
