package xyz.sadiulhakim.advanced_project.config;

import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import xyz.sadiulhakim.advanced_project.pojo.Team;

public class MultiFileTeamReader implements ResourceAwareItemReaderItemStream<Team> {

    private final FlatFileItemReader<String> lineReader;

    public MultiFileTeamReader(FlatFileItemReader<String> lineReader) {
        this.lineReader = lineReader;
    }

    @Override
    public void setResource(Resource resource) {
        lineReader.setResource(resource);
    }

    @Override
    public Team read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        lineReader.open(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        lineReader.close();
    }
}
