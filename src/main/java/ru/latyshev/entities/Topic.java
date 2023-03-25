package ru.latyshev.entities;

import java.util.ArrayList;
import java.util.List;

public class Topic {
    private String name;
    private static List<Topic> topicList = new ArrayList<>();

    public Topic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<Topic> getTopicList() {
        return topicList;
    }
}
