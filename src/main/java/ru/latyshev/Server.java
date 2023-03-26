package ru.latyshev;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import ru.latyshev.entities.*;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;

import static ru.latyshev.entities.Vote.*;

public class Server {
    public static final Logger log = LogManager.getLogger(Server.class);
    private static Set<User> users = new HashSet<>();

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

                    String topicName;
                    while (true) {
                        request = dataStream.readLine(); //если будут проблемы - попробовать вывести строку из while
                        if (request.equals("exit")) {
                            dataStream.writeLine("Good bye, " + user.getLoginName() + "!");
                            dataStream.close();
                           
                          // creating new topic  
                        } else if (request.startsWith("create topic -n=")) {
                            topicName = CommandParsing.getTopicName(request);
                            if (Vote.topics.containsKey(topicName)) {
                                dataStream.writeLine("'" + topicName + "'" + " is already exists. Use different topic name.");
                            } else {
                                Vote.topics.put(topicName, new ArrayList<>());
                                dataStream.writeLine("You created a new topic " + "'" + topicName + "'");
                            }

                        // topics view
                        // ??? нужен визуальный список или вывод строки в виде List?
                        } else if (request.startsWith("view")) {
                            if (request.equals("view")) {
                                dataStream.writeLine(Vote.getAllVotesCount());
                            }
                            else if (request.startsWith("view -t=")) {
                                // добавить в парсинг проверки из-за пустой строки после "="
                                topicName = CommandParsing.getTopicName(request);
                                if (Vote.topics.containsKey(topicName)) {
                                    dataStream.writeLine(Vote.getTopicVotesNames(topicName));
                                } else {
                                    dataStream.writeLine("'" + topicName + "'" + " doesn't exist.");
                                }
                            }

                        // create vote
                        } else if (request.startsWith("create vote -t=")) {
                            topicName = CommandParsing.getTopicName(request);
                            if (Vote.topics.containsKey(topicName)) {
                                String name = createVotingName(dataStream, topicName);
                                String description = createDescription(dataStream);
                                List<String> answers = createListOfAnswers(dataStream);

                                Vote vote = new Vote(name, description, answers);
                                topics.get(topicName).add(vote);
                                dataStream.writeLine("The voting was just created");

                            } else {
                                dataStream.writeLine("'" + topicName + "'" + " doesn't exist. You can't create voting");
                            }
                        //request = dataStream.readLine();

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
}
