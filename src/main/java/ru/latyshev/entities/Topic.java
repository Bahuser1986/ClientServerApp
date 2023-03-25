package ru.latyshev.entities;

import java.util.ArrayList;
import java.util.List;

public class Topic {
    private String name;
    private String author;

    public Topic(String name, String author) {
        this.name = name;
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }
}
