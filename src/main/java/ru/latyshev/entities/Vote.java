package ru.latyshev.entities;

import java.util.*;
import java.util.stream.Collectors;

public class Vote {
    private final User owner;
    private String name;
    private String description;
    private List<User> votedUsers = new ArrayList<>();
    private TreeMap<String, Integer> voting = new TreeMap<>();
    public static Map<String, List<Vote>> topics = new HashMap<>();

    static {
        topics.put("zero votes", new ArrayList<>());
        topics.put("one vote", Collections.singletonList(
                new Vote("one", "info", new TreeMap<>(), new User("username"))));
        topics.put("three votes", Arrays.asList(
                new Vote("one", "info", new TreeMap<>(), new User("user")),
                new Vote("two", "info", new TreeMap<>(), new User("login")),
                new Vote("three", "info", new TreeMap<>(), new User("aleks"))));
        TreeMap<String, Integer> map = new TreeMap<>();
        map.put("1. first answer", 5);
        map.put("2. second answer", 0);
        topics.put("test", Collections.singletonList(
                new Vote("test", "info test", map, new User("user"))));
    }

    public Vote(String name, String description, TreeMap<String, Integer> voting, User owner) {
        this.name = name;
        this.description = description;
        this.voting = voting;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public User getOwner() {
        return owner;
    }

    public TreeMap<String, Integer> getVoting() {
        return voting;
    }

    public List<User> getVotedUsers() {
        return votedUsers;
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
    public static TreeMap<String, Integer> createListOfAnswers(DataStream dataStream) {
        int numberOfAnswers = getNumberOfAnswers(dataStream);
        TreeMap<String, Integer> answers = new TreeMap<>();
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
    public static void printTopicDoesNtExist(String topicName, DataStream dataStream){
        dataStream.writeLine("'" + topicName + "' doesn't exist");
    }
    public static boolean isVoteExists(String topicName, String voteName) {
        return getTopicVotesNames(topicName).contains(voteName);
    }
    public static void printVoteDoesNtExist(String voteName, DataStream dataStream){
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
    public static void printVoteNameAndAnswers(String topicName, String voteName, DataStream dataStream) {
        Vote vote = getVote(topicName, voteName);
        assert vote != null;
        dataStream.writeLine(vote.getName());
        StringBuilder dashes = new StringBuilder();
        for (int i = 0; i < voteName.length(); i++) {
            dashes.append("-");
        }
        dataStream.writeLine(dashes.toString());
        for (Map.Entry<String, Integer> pair : vote.getVoting().entrySet()) {
            dataStream.writeLine(pair.getKey() + " - " + pair.getValue() + " vote(s)");
        }
    }
    public static void voteForAnswer(String topicName, String voteName, DataStream dataStream, User user) {
        printVoteNameAndAnswers(topicName, voteName, dataStream);
        dataStream.writeLine("");
        // TODO проверить на число и ограничения по кол-ву ответов
        dataStream.writeLine("Enter number of your answer");
        int number = Integer.parseInt(dataStream.readLine());

        Vote vote = getVote(topicName, voteName);
        assert vote != null;

        TreeMap<String, Integer> map = vote.getVoting();
        int i = 1;
        for (Map.Entry<String, Integer> pair : map.entrySet()) {
            if (number == i) {
                pair.setValue(pair.getValue() + 1);
                break;
            } else {
                i++;
            }
        }
        vote.getVotedUsers().add(user);
    }
    // TODO переопределить equals для User
    public static boolean isUserTheVoteOwner(String topicName, String voteName, User user) {
        return Objects.requireNonNull(getVote(topicName, voteName))
                .getOwner().getLoginName().equals(user.getLoginName());
    }

    public static void deleteTheVote(String topicName, String voteName, DataStream dataStream) {
        List<Vote> voteList = new ArrayList<>(topics.get(topicName));
        Iterator<Vote> iterator = voteList.iterator();
        while (iterator.hasNext()) {
            Vote nextVote = iterator.next();
            if (nextVote.getName().equals(voteName)) {
                iterator.remove();
                break;
            }
        }
        for (Map.Entry<String, List<Vote>> pair : topics.entrySet()) {
            if (pair.getKey().equals(topicName)) {
                pair.setValue(voteList);
            }
        }
        if (!isVoteExists(topicName, voteName)) {
            dataStream.writeLine("The vote was successfully deleted");
        } else {
            dataStream.writeLine("Something wrong happened. The vote wasn't deleted");
        }
    }
}
