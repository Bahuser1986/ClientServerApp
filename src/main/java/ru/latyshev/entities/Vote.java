package ru.latyshev.entities;

import ru.latyshev.Server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Vote implements Serializable{
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

    public static String createVotingName(BufferedReader reader, PrintWriter writer, String topicName) throws IOException {
        while (true) {
            writer.println("Enter a unique voting name");
            String request = reader.readLine();
            boolean isNameExists = topics.get(topicName).stream().anyMatch(x -> x.getName().equals(request));
            if (request.isEmpty() || isNameExists) {
                continue;
            }
            return request;
        }
    }
    public static String createDescription(BufferedReader reader, PrintWriter writer) throws IOException {
        while (true) {
            writer.println("Describe the voting theme");
            String request = reader.readLine();
            if (request.isEmpty()) {
                continue;
            }
            return request;
        }
    }
    // TODO добавить ограничения на количество ответов
    private static int getNumberOfAnswers(BufferedReader reader, PrintWriter writer) throws IOException {
        while (true) {
            writer.println("Enter number of possible answers");
            String request = reader.readLine();
            if (request.isEmpty() || !request.chars().allMatch(Character::isDigit)) {
                continue;
            }
            return Integer.parseInt(request);
        }
    }
    public static TreeMap<String, Integer> createListOfAnswers(BufferedReader reader, PrintWriter writer) throws IOException {
        int numberOfAnswers = getNumberOfAnswers(reader, writer);
        TreeMap<String, Integer> answers = new TreeMap<>();
        for (int i = 1; i <= numberOfAnswers; i++) {
            String answer;
            while (true) {
                writer.println("Enter " + i + " answer");
                answer = reader.readLine();
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
    public static void printTopicDoesNtExist(String topicName, PrintWriter writer){
        writer.println("'" + topicName + "' doesn't exist");
    }
    public static boolean isVoteExists(String topicName, String voteName) {
        return getTopicVotesNames(topicName).contains(voteName);
    }
    public static void printVoteDoesNtExist(String voteName, PrintWriter writer){
        writer.println("'" + voteName + "' doesn't exist");
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
    public static void printVoteNameAndAnswers(String topicName, String voteName, PrintWriter writer) {
        Vote vote = getVote(topicName, voteName);
        assert vote != null;
        writer.println(vote.getName());
        StringBuilder dashes = new StringBuilder();
        for (int i = 0; i < voteName.length(); i++) {
            dashes.append("-");
        }
        writer.println(dashes.toString());
        for (Map.Entry<String, Integer> pair : vote.getVoting().entrySet()) {
            writer.println(pair.getKey() + " - " + pair.getValue() + " vote(s)");
        }
    }
    public static void voteForAnswer(String topicName, String voteName, BufferedReader reader, PrintWriter writer, User user) throws IOException {
        printVoteNameAndAnswers(topicName, voteName, writer);
        writer.println("");
        // TODO проверить на число и ограничения по кол-ву ответов
        writer.println("Enter number of your answer");
        int number = Integer.parseInt(reader.readLine());

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

    public static void deleteTheVote(String topicName, String voteName, BufferedReader reader, PrintWriter writer) {
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
            writer.println("The vote was successfully deleted");
        } else {
            writer.println("Something wrong happened. The vote wasn't deleted");
        }
    }
    public static void saveVotesToFile(String path){
        Path file = Paths.get(path);
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))){
            Map<String, List<Vote>> map = new HashMap<>(topics);
            out.writeObject(map);
        } catch (IOException e) {
            Server.log.error(e.getMessage());
        }
    }
    public static void loadVotesFromFile(String path){
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(Paths.get(path)))){
            HashMap<String, List<Vote>> map = (HashMap<String, List<Vote>>) in.readObject();
            topics = new HashMap<>(map);
        } catch (IOException | ClassNotFoundException e) {
            Server.log.error(e.getMessage());
        }
    }
}
