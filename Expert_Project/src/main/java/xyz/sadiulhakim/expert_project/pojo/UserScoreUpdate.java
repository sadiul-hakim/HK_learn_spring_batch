package xyz.sadiulhakim.expert_project.pojo;

public record UserScoreUpdate(
        long userId,
        double add,
        double multiple
) {
    public static final String USER_SCORE_TABLE_NAME = "user_score";
}
