package xyz.sadiulhakim.logFiles;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;

@Configuration
@EnableBatchProcessing
public class LogFilesProcessor {

    private Resource[] loadLogFiles() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        return resolver.getResources("file:F:\\Batch\\data-importer\\**\\*.log");
    }
}
