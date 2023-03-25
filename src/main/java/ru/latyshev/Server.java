package ru.latyshev;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import ru.latyshev.entities.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;


public class Server {
    public static final Logger log = LogManager.getLogger(Server.class);
    private static Set<User> users = new HashSet<>();
    private static Map<String, List<Vote>> topics = new HashMap<>();

    static {
        topics.put("zero votes", new ArrayList<>());
        topics.put("one vote", new ArrayList<>(Collections.singletonList(
                new Vote("one", "info"))));
        topics.put("three votes", new ArrayList<>(Arrays.asList(
                new Vote("one", "info"),
                new Vote("two", "info"),
                new Vote("three", "info"))));
    }
    public static void main(String[] args){
        try (ServerSocket server = new ServerSocket(8080)){
            log.info("Server started...");
//            throw new IOException();

            while (true) {
                try (DataStream dataStream = new DataStream(server)) {

                    User user;

                    String request = dataStream.readLine();

                    //login
                    while (true) {
                        if (request.equals("exit")) {
                            dataStream.writeLine("Good bye!");
                            dataStream.close();
                        }
                        //check command request and creating user
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

                    //rest commands

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
                            else if (request.startsWith("view -t=")) {}
                            else {continue;}
                        }
                        else {
                            dataStream.writeLine("Use command 'help' for more information");
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
    //!!!нужен визуальный список или вывод строки в виде List???
    private static String getAllVotesCount(){
        return topics.entrySet()
                .stream()
                .map(x -> String.format("%s (votes in %s=%d)", x.getKey(), x.getKey(), x.getValue().size()))
                .collect(Collectors.toList()).toString();

    }
}
