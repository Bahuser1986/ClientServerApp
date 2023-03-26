package ru.latyshev.entities;

import ru.latyshev.Server;

import java.util.ArrayList;
import java.util.List;

public class Vote {
    private static DataStream dataStream;
    private String name;
    private String description;
    private List<String> answers = new ArrayList<>();

    public Vote() {
    }

    public Vote(String name, String description, List<String> answers) {
        this.name = name;
        this.description = description;
        this.answers = answers;
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

    public List<String> getAnswers() {
        return answers;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public Vote createVote() {
        String name = createVotingName();
        String description = createDescription();
        fillListofAnswers();
        return new Vote(name, description, getAnswers());
    }
    private String createVotingName() {
        while (true) {
            dataStream.writeLine("Enter a unique voting name");
            String request = dataStream.readLine();
            if (request.isEmpty() || Server
                    .getTopicVotesNames(CommandParsing
                            .getTopicName(request))
                    .contains(request)) {
                continue;
            }
            return request;
        }
    }
    private String createDescription() {
        while (true) {
            dataStream.writeLine("Describe the voting theme");
            String request = dataStream.readLine();
            if (request.isEmpty()) {
                continue;
            }
            return request;
        }
    }
    private int getNumberOfAnswers() {
        while (true) {
            dataStream.writeLine("Enter number of possible answers");
            String request = dataStream.readLine();
            if (request.isEmpty() || !request.chars().allMatch(Character::isDigit)) {
                continue;
            }
            return Integer.parseInt(request);
        }
    }
    private void fillListofAnswers() {
        int nunberOfAnswers = getNumberOfAnswers();
        for (int i = 1; i <= nunberOfAnswers; i++) {
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
    }
}
