package xyz.sadiulhakim.advanced_project.pojo;

import java.util.ArrayList;
import java.util.List;

public record Player(
        String name,
        List<Double> scores
) {
    public Player(String name) {
        this(name, new ArrayList<>());
    }
}
