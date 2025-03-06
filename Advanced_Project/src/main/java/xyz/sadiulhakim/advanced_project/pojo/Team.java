package xyz.sadiulhakim.advanced_project.pojo;

import java.util.ArrayList;
import java.util.List;

public record Team(
        String name,
        List<Player> players
) {

    public Team(String name) {
        this(name, new ArrayList<>());
    }
}
