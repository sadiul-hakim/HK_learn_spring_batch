package xyz.sadiulhakim.expert_project.pojo;

public record SessionAction(
        long id,
        long userId,
        String actionType,
        double amount
) {
    public static final String SESSION_ACTION_TABLE_NAME = "session_action";
}
