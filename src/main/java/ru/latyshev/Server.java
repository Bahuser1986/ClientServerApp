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

    public static void main(String[] args){
        try (ServerSocket server = new ServerSocket(8080)){
            log.info("Server started...");
//            throw new IOException();

            User user = null;

            while (true) {
                boolean isActive = true;
                try (DataStream dataStream = new DataStream(server)) {
                    
                    dataStream.writeLine("Connected to server...");
                    dataStream.writeLine("Enter your username 'login -u=username'");
                    String request;

                    // login
                    boolean isLogged = false;
                    while (!isLogged) {
                        request = dataStream.readLine();
                        if (request.equals("exit")) {
                            dataStream.writeLine("Good bye!");
                            dataStream.close();
                            isActive = false;
                            break;
                        }
                        // command request checking  and creating user
                        else if (CommandParsing.checkLoginCommand(request)) {
                            String loginName = CommandParsing.getLoginNameFromCommand(request);

                            user = new User(loginName);
                            user.setLogged(true);

                            dataStream.writeLine("Welcome to server, " + user.getLoginName() + "!");
                            isLogged = true;

                        } else {
                            dataStream.writeLine("Please, enter your login 'login -u=username'");
                        }
                    }

                    // rest commands
                    String topicName;
                    String voteName;
                    while (isActive) {
                        request = dataStream.readLine();
                        if (request.equals("exit")) {
                            dataStream.writeLine("Good bye, " + user.getLoginName() + "!");
                            dataStream.close();
                            break;

                        // creating new topic
                        } else if (request.startsWith("create topic -n=")) {
                            topicName = CommandParsing.getTopicName(request);
                            if (isTopicExists(topicName)) {
                                dataStream.writeLine("'" + topicName + "'" + " is already exists. Use different topic name.");
                            } else {
                                Vote.topics.put(topicName, new ArrayList<>());
                                dataStream.writeLine("You created a new topic " + "'" + topicName + "'");
                            }

                        // topics view
                        } else if (request.startsWith("view")) {
                            if (request.equals("view")) {
                                if (topics.isEmpty()) {
                                    dataStream.writeLine("Nobody has created any topic yet");
                                } else {
                                    for (String voteCount : getAllVotesCount()) {
                                        dataStream.writeLine(voteCount);
                                    }
                                }
                            } else if (request.startsWith("view -t=")) {

                                // view -t=<topic> -v=<vote>
                                if (request.split("=").length == 3) {
                                    // TODO добавить проверки на корректность команды
                                    String[] topicAndVoteNames = CommandParsing.getTopicAndVoteNames(request);
                                    topicName = topicAndVoteNames[0];
                                    voteName = topicAndVoteNames[1];
                                    if (!isTopicExists(topicName)) {
                                        printTopicDoesNtExist(topicName, dataStream);
                                    } else {
                                        if (!isVoteExists(topicName, voteName)) {
                                            printVoteDoesNtExist(voteName, dataStream);
                                        } else {
                                            printVoteNameAndAnswers(topicName, voteName, dataStream);
                                        }
                                    }

                                // view -t=<topic>
                                } else if (request.split("=").length == 2) {
                                    // TODO добавить в парсинг проверки, если строка зканчивается на "="
                                    topicName = CommandParsing.getTopicName(request);
                                    if (isTopicExists(topicName)) {
                                        if (topics.get(topicName).isEmpty()) {
                                            dataStream.writeLine("Nobody has created any voting yet");
                                        } else {
                                            for (String vote : getTopicVotesNames(topicName))
                                                dataStream.writeLine(vote);
                                        }
                                    } else {
                                        dataStream.writeLine("'" + topicName + "'" + " doesn't exist.");
                                    }
                                }
                            }

                        // create vote
                        } else if (request.startsWith("create vote -t=")) {
                            // TODO добавить в парсинг проверки, если строка зканчивается на '='
                            topicName = CommandParsing.getTopicName(request);
                            if (isTopicExists(topicName)) {
                                String name = createVotingName(dataStream, topicName);
                                String description = createDescription(dataStream);
                                TreeMap<String, Integer>  answers = createListOfAnswers(dataStream);

                                Vote vote = new Vote(name, description, answers, user);
                                topics.get(topicName).add(vote);
                                dataStream.writeLine("The voting was just created");

                            } else {
                                dataStream.writeLine("The topic '" + topicName + "'" + " doesn't exist. You can't create voting");
                            }

                        // vote -t=<topic> -v=<vote>
                        } else if (request.startsWith("vote -t=")) {
                            if (request.split("=").length == 3) {
                                // TODO добавить проверки на корректность команды
                                String[] topicAndVoteNames = CommandParsing.getTopicAndVoteNames(request);
                                topicName = topicAndVoteNames[0];
                                voteName = topicAndVoteNames[1];
                                if (!isTopicExists(topicName)) {
                                    printTopicDoesNtExist(topicName, dataStream);
                                } else {
                                    if (!isVoteExists(topicName, voteName)) {
                                        printVoteDoesNtExist(voteName, dataStream);
                                    } else {
                                        // vote for answer
                                        // TODO добавить проверки что пользователь не голосовал
                                        voteForAnswer(topicName, voteName, dataStream, user);
                                        dataStream.writeLine("Thank you! Your vote is important for us!");
                                    }
                                }
                            }

                        // delete -t=topic -v=<vote>
                        } else if (request.startsWith("delete -t=")) {
                            if (request.split("=").length == 3) {
                                // TODO добавить проверки на корректность команды
                                String[] topicAndVoteNames = CommandParsing.getTopicAndVoteNames(request);
                                topicName = topicAndVoteNames[0];
                                voteName = topicAndVoteNames[1];
                                if (!isTopicExists(topicName)) {
                                    printTopicDoesNtExist(topicName, dataStream);
                                } else {
                                    if (!isVoteExists(topicName, voteName)) {
                                        printVoteDoesNtExist(voteName, dataStream);
                                    } else {
                                        // delete func
                                        if (isUserTheVoteOwner(topicName, voteName, user)) {
                                            dataStream.writeLine("Are you sure you want to delete the vote? (y/n)");
                                            request = dataStream.readLine().toLowerCase();
                                            if (request.equals("y")) {
                                                deleteTheVote(topicName, voteName, dataStream);
                                            }
                                        } else {
                                            dataStream.writeLine("You can't delete the vote. You're not the owner of the vote");
                                        }
                                    }
                                }
                            }


                        // default server response after client login
                        } else {
                            // TODO сделать команду help со списком всех комманд и описанием
                            dataStream.writeLine("Use command 'help'(isn't ready) for more information");
                        }

                    }
                } catch (NullPointerException e) {
                    log.error(e + "/" + e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error(e + "/" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            log.info("Server stopped");
        }
    }
}
