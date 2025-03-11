package xyz.sadiulhakim.expert_project;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import xyz.sadiulhakim.expert_project.pojo.UserScoreUpdate;
import xyz.sadiulhakim.expert_project.source.GenerateSourceDatabase;
import xyz.sadiulhakim.expert_project.source.SourceDatabaseUtils;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private final GenerateSourceDatabase sourceDatabase;

    public Application(GenerateSourceDatabase sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        sourceDatabase.start();
    }
}
