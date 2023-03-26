package ru.latyshev;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import ru.latyshev.entities.*;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;
import java.util.stream.Collectors;


public class Server {
    public static final Logger log = LogManager.getLogger(Server.class);
    private static Set<User> users = new HashSet<>();
    private static Map<String, List<Vote>> topics = new HashMap<>();

    static {
        topics.put("zero votes", new ArrayList<>());
        topics.put("one vote", new ArrayList<>(Collections.singletonList(
                new Vote("one", "info", new ArrayList<>()))));
        topics.put("three votes", new ArrayList<>(Arrays.asList(
                new Vote("one", "info", new ArrayList<>()),
                new Vote("two", "info", new ArrayList<>()),
                new Vote("three", "info", new ArrayList<>()))));
    }
    public static void main(String[] args){
        try (ServerSocket server = new ServerSocket(8080)){
            log.info("Server started...");
//            throw new IOException();

            while (true) {
                try (DataStream dataStream = new DataStream(server)) {

                    User user;

                    String request = dataStream.readLine();

                    // login
                    while (true) {
                        if (request.equals("exit")) {
                            dataStream.writeLine("Good bye!");
                            dataStream.close();
                        }
                        // command request checking  and creating user
                        if (CommandParsing.checkLoginCommand(request)) {
                            String loginName = CommandParsing.getLoginNameFromCommand(request);

                            user = new User(loginName);
                            user.setLogged(true);
                            users.add(user);

                            dataStream.writeLine("Welcome to server, " + user.getLoginName() + "!");
                            break;
                        } else {
                            dataStream.writeLine("Please, enter your login 'login -u=username'");
                        }
                        request = dataStream.readLine();
                    }

                    // rest commands
                    while (true) {
                        request = dataStream.readLine();
                        if (request.equals("exit")) {
                            dataStream.writeLine("Good bye, " + user.getLoginName() + "!");
                            dataStream.close();
                           
                          // creating new topic  
                        } else if (request.startsWith("create topic -n=")) {
                            String topicName = CommandParsing.getTopicName(request);
                            if (topics.containsKey(topicName)) {
                                dataStream.writeLine(topicName + " is already exists. Use different topic name.");
                            } else {
                                topics.put(topicName, new ArrayList<>());
                                dataStream.writeLine("You created a new topic " + topicName);
                            }

                        // topics view
                        } else if (request.startsWith("view")) {
                            if (request.equals("view")) {
                                dataStream.writeLine(getAllVotesCount());
                            }
                            else if (request.startsWith("view -t=")) {
                                String topicName = CommandParsing.getTopicName(request);
                                if (topics.containsKey(topicName)) {
                                    dataStream.writeLine(getTopicVotesNames(topicName));
                                } else {
                                    dataStream.writeLine("'" + topicName + "'" + " doesn't exist.");
                                }
                            }

                        // create vote
//                        } else if (request.startsWith("create vote -t=")) {
//                            String topicName = CommandParsing.getTopicName(request);
//                            if (topics.containsKey(topicName)) {
//                                Vote vote = new Vote(); /// !!! сначала нужно протестировать view -t=<topic>
//                                vote.createVote(); // !!! голосование не создается, уходит в бесконечный цикл
//                            } else {
//                                dataStream.writeLine("'" + topicName + "'" + " doesn't exist. You can't create voting");
//                            }

                        // default server response after client login
                        } else {
                            dataStream.writeLine("Use command 'help'(isn't ready) for more information");
                        }
                    }
                } catch (NullPointerException e) {}
            }
        } catch (IOException e) {
            log.error(e + "/" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            log.info("Server stopped");
        }
    }

    //get string for "view" command
    //!!!отбрасывается часть строки, если внутри строки есть перевод строки
    //???нужен визуальный список или вывод строки в виде List?
    private static String getAllVotesCount(){
        if (topics.isEmpty()) {
            return "Nobody has created any topic yet";
        }
        return topics.entrySet()
                .stream()
                .map(x -> String.format("%s (votes in %s=%d)", x.getKey(), x.getKey(), x.getValue().size()))
                .collect(Collectors.toList()).toString();

    }
    public static String getTopicVotesNames(String topicName){
        if (topics.get(topicName).isEmpty()) {
            return "Nobody has created any voting yet";
        }
        return topics.get(topicName)
                .stream()
                .map(x -> String.format("%s", x.getName()))
                .collect(Collectors.toList()).toString();

    }
}
