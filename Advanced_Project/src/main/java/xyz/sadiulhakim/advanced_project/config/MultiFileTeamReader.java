package xyz.sadiulhakim.advanced_project.config;

import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import xyz.sadiulhakim.advanced_project.pojo.Player;
import xyz.sadiulhakim.advanced_project.pojo.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        Optional<Team> maybeTeam = Optional.empty();

        String line;

        // Instead of line by line, keep reading until the empty line
        // At the end FlatFileItemReader returns null
        while ((line = lineReader.read()) != null) {
            line = line.trim();
            if (line.isEmpty()) {

                // It is the end of team
                return maybeTeam.orElse(null);
            } else if (!line.contains(":")) {

                // It is a Team name
                maybeTeam = Optional.of(new Team(line));
            } else {

                // It is a player line
                String[] arr = line.split(":");
                String[] scores = arr[1].split(",");
                List<Double> scoresDouble = Arrays.stream(scores).map(Double::parseDouble).toList();
                Player player = new Player(arr[0], scoresDouble);
                maybeTeam.ifPresent(team -> team.players().add(player));
            }
        }

        // Here we read the current resource till an empty line. FlatFileItemReader would keep the resource open
        // until reading is done and it would remember where did it leave off.
        return maybeTeam.orElse(null);
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
