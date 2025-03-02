package xyz.sadiulhakim.inter_project;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xyz.sadiulhakim.inter_project.util.GenerateSourceDatabase;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private final GenerateSourceDatabase generateSourceDatabase;

    public Application(GenerateSourceDatabase generateSourceDatabase) {
        this.generateSourceDatabase = generateSourceDatabase;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            generateSourceDatabase.generate();
        } catch (Exception ignore) {
        }
    }
}
