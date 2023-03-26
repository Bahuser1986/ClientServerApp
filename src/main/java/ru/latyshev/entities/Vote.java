package ru.latyshev.entities;

import java.util.*;
import java.util.stream.Collectors;

public class Vote {
    private final User owner;
    private String name;
    private String description;
    private static DataStream dataStream;
    private List<User> votedUsers = new ArrayList<>();
    private Map<String, Integer> voting = new HashMap<>();
    public static Map<String, List<Vote>> topics = new HashMap<>();

    static {
        topics.put("zero votes", new ArrayList<>());
        topics.put("one vote", Collections.singletonList(
                new Vote("one", "info", new HashMap<>(), new User("username"))));
        topics.put("three votes", Arrays.asList(
                new Vote("one", "info", new HashMap<>(), new User("user")),
                new Vote("two", "info", new HashMap<>(), new User("login")),
                new Vote("three", "info", new HashMap<>(), new User("aleks"))));
        Map<String, Integer> map = new HashMap<>();
        map.put("first answer", 5);
        map.put("second answer", 0);
        topics.put("test", Collections.singletonList(
                new Vote("test", "info test", map, new User("user"))));
    }

    public Vote(String name, String description, Map<String, Integer> voting, User owner) {
        this.name = name;
        this.description = description;
        this.voting = voting;
        this.owner = owner;
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

    public Map<String, Integer> getVoting() {
        return voting;
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
    // TODO добавить ограничения на количество ответов
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
    public static Map<String, Integer> createListOfAnswers(DataStream dataStream) {
        int numberOfAnswers = getNumberOfAnswers(dataStream);
        Map<String, Integer> answers = new HashMap<>();
        for (int i = 1; i <= numberOfAnswers; i++) {
            String answer;
            while (true) {
                dataStream.writeLine("Enter " + i + " answer");
                answer = dataStream.readLine();
                if (answer.isEmpty()) {
                    continue;
                } else {
                    answers.put(i + ". " + answer, 0);
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
    public static boolean isTopicExists(String topicName) {
        return topics.containsKey(topicName);
    }
    public static void printTopicDoesNtExist(String topicName){
        dataStream.writeLine("'" + topicName + "' doesn't exist");
    }
    public static boolean isVoteExists(String topicName, String voteName) {
        return getTopicVotesNames(topicName).contains(voteName);
    }
    public static void printVoteDoesNtExist(String voteName){
        dataStream.writeLine("'" + voteName + "' doesn't exist");
    }
    private static Vote getVote(String topicName, String voteName) {
        List<Vote> votesList = topics.get(topicName);
        for (Vote vote : votesList) {
            if (vote.getName().equals(voteName)) {
                return vote;
            }
        }
        return null;
    }
    public static void printVoteNameAndAnswers(String topicName, String voteName, DataStream stream) {
        Vote vote = getVote(topicName, voteName);
        assert vote != null;
        stream.writeLine(vote.getName());
        StringBuilder dashes = new StringBuilder();
        for (int i = 0; i < voteName.length(); i++) {
            dashes.append("-");
        }
        stream.writeLine(dashes.toString());
        for (Map.Entry<String, Integer> pair : vote.getVoting().entrySet()) {
            stream.writeLine(pair.getKey() + " - " + pair.getValue() + " vote(s)");
        }
    }
}
