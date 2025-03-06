package xyz.sadiulhakim.advanced_project.config;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import xyz.sadiulhakim.advanced_project.pojo.Player;
import xyz.sadiulhakim.advanced_project.pojo.Team;

public class TeamAverageProcessor implements ItemProcessor<Team, AverageScore> {

    public static final String MAX_SCORE = "max.score";
    public static final String MAX_PLAYER = "max.player";
    public static final String MIN_SCORE = "min.score";
    public static final String MIN_PLAYER = "min.player";

    private StepExecution stepExecution;

    private final int scoreIndex;

    public TeamAverageProcessor(int scoreIndex) {
        this.scoreIndex = scoreIndex;
    }

    // We have to set MAX_SCORE, MAX_PLAYER, MIN_SCORE, MIN_PLAYER, and we need to return the average score.
    @Override
    public AverageScore process(Team team) throws Exception {

        if (stepExecution == null)
            throw new RuntimeException("StepExecution is not set!");

        ExecutionContext executionContext = stepExecution.getExecutionContext();
        Double lastMaxScore = executionContext.containsKey(MAX_SCORE) ? executionContext.getDouble(MAX_SCORE) : null;
        Double lastMinScore = executionContext.containsKey(MIN_SCORE) ? executionContext.getDouble(MIN_SCORE) : null;

        double sum = 0;
        int count = 0;
        for (Player player : team.players()) {
            Double score = player.scores().get(scoreIndex);

            if (lastMaxScore == null || score > lastMaxScore) {
                executionContext.put(MAX_SCORE, score);
                executionContext.put(MAX_PLAYER, player.name());
            }

            if (lastMinScore == null || score < lastMinScore) {
                executionContext.put(MIN_SCORE, score);
                executionContext.put(MIN_PLAYER, player.name());
            }

            sum += score;
            count++;
        }

        return new AverageScore(team.name(), (sum / count));
    }

    public StepExecution getStepExecution() {
        return stepExecution;
    }
}
