package ru.latyshev.entities;

import java.util.*;
import java.util.stream.Collectors;

public class Vote {
    private String name;
    private String description;
    public static Map<String, List<Vote>> topics = new HashMap<>();

    static {
        topics.put("zero votes", new ArrayList<>());
        topics.put("one vote", new ArrayList<>(Collections.singletonList(
                new Vote("one", "info", new ArrayList<>()))));
        topics.put("three votes", new ArrayList<>(Arrays.asList(
                new Vote("one", "info", new ArrayList<>()),
                new Vote("two", "info", new ArrayList<>()),
                new Vote("three", "info", new ArrayList<>()))));
    }

    public Vote(String name, String description, List<String> answers) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static String createVotingName(DataStream dataStream, String topicName) {
        while (true) {
            dataStream.writeLine("Enter a unique voting name");
            String request = dataStream.readLine();
            boolean isNameExists = topics.get(topicName).stream().anyMatch(x -> x.getName().equals(request));
            if (request.isEmpty() || isNameExists) {
                continue;
            }
            return request;
        }
    }
    public static String createDescription(DataStream dataStream) {
        while (true) {
            dataStream.writeLine("Describe the voting theme");
            String request = dataStream.readLine();
            if (request.isEmpty()) {
                continue;
            }
            return request;
        }
    }
    private static int getNumberOfAnswers(DataStream dataStream) {
        while (true) {
            dataStream.writeLine("Enter number of possible answers");
            String request = dataStream.readLine();
            if (request.isEmpty() || !request.chars().allMatch(Character::isDigit)) {
                continue;
            }
            return Integer.parseInt(request);
        }
    }
    public static List<String> createListOfAnswers(DataStream dataStream) {
        int numberOfAnswers = getNumberOfAnswers(dataStream);
        List<String> answers = new ArrayList<>();
        for (int i = 1; i <= numberOfAnswers; i++) {
            String answer;
            while (true) {
                dataStream.writeLine("Enter " + i + " answer");
                answer = dataStream.readLine();
                if (answer.isEmpty()) {
                    continue;
                } else {
                    answers.add(i + ". " + answer);
                    break;
                }
            }
        }
        return answers;
    }
    public static List<String> getTopicVotesNames(String topicName){
        return topics.get(topicName)
                .stream()
                .map(x -> String.format("%s", x.getName()))
                .collect(Collectors.toList());
    }

    //get list of topics for "view" command
    public static List<String> getAllVotesCount(){
        return topics.entrySet()
                .stream()
                .map(x -> String.format("%s (votes in '%s'=%d)", x.getKey(), x.getKey(), x.getValue().size()))
                .collect(Collectors.toList());
    }
}
