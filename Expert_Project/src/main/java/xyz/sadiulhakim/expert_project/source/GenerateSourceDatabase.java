package xyz.sadiulhakim.expert_project.source;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import xyz.sadiulhakim.expert_project.pojo.SessionAction;
import xyz.sadiulhakim.expert_project.pojo.UserScoreUpdate;

import java.util.Random;

@Component
public class GenerateSourceDatabase {

    private final JdbcTemplate jdbcTemplate;

    private static final int USER_COUNT = 100;
    private static final int RECORD_COUNT = 1000;
    private static final Random RANDOM = new Random();

    public GenerateSourceDatabase(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Drop the table, create the table and fill it out with the data
    public void start() {

        SourceDatabaseUtils.dropTableIfExists(jdbcTemplate, SessionAction.SESSION_ACTION_TABLE_NAME);
        SourceDatabaseUtils.createSessionActionTable(jdbcTemplate, SessionAction.SESSION_ACTION_TABLE_NAME);

        for (int i = 0; i < RECORD_COUNT; i++) {
            SourceDatabaseUtils.insertSessionAction(jdbcTemplate, generateRecord(i + 1), SessionAction.SESSION_ACTION_TABLE_NAME);
        }

        SourceDatabaseUtils.dropTableIfExists(jdbcTemplate, UserScoreUpdate.USER_SCORE_TABLE_NAME);
        SourceDatabaseUtils.createUserScoreTable(jdbcTemplate, UserScoreUpdate.USER_SCORE_TABLE_NAME);

        // Print to console the success message
        System.out.println("Input source table with " + RECORD_COUNT + " records is successfully initialized");
    }

    // Generate random session action record
    private SessionAction generateRecord(long id) {
        long userId = 1 + RANDOM.nextInt(USER_COUNT);
        return RANDOM.nextBoolean()
                ? new SessionAction(id, userId, SourceDatabaseUtils.PLUS_TYPE, 1 + RANDOM.nextInt(3))
                : new SessionAction(id, userId, SourceDatabaseUtils.MULTI_TYPE, ((double) (1 + RANDOM.nextInt(5))) / 10 + 1d);
    }

}
