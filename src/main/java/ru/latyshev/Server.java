package ru.latyshev;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.latyshev.entities.CommandParsing;
import ru.latyshev.entities.User;
import ru.latyshev.entities.Vote;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static ru.latyshev.entities.Vote.*;

public class Server implements Runnable{
    BufferedReader console;
    private ExecutorService pool;
    private ServerSocket server;
    private boolean isServerActive;
    private List<ConnectionHandler> connections;
    public static final Logger log = LogManager.getLogger(Server.class);

    public Server() {
        isServerActive = true;
        connections = new ArrayList<>();
        console = new BufferedReader(new InputStreamReader(System.in));
    }

    public void shutdown() {
        try {
            isServerActive = false;
            for (ConnectionHandler handler : connections) {
                handler.shutdown();
            }
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }

        } catch (IOException e) {
            //
        }
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(8080);
            log.info("Server started...");
            pool = Executors.newCachedThreadPool();

            new Thread(() -> {
                new InputHandler().run();
            }).start();

            while (isServerActive) {
                Socket client = server.accept();
                ConnectionHandler connectionHandler = new ConnectionHandler(client);
                connections.add(connectionHandler);
                pool.execute(connectionHandler);
            }
        }catch (SocketException e) {
            log.error("Server stopped");
            shutdown();
        } catch (Exception e) {
            log.error(e.getMessage());
            shutdown();
        }
    }
    class ConnectionHandler implements Runnable {
        Socket client;
        BufferedReader reader;
        PrintWriter writer;
        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                writer = new PrintWriter(client.getOutputStream(), true);

                User user = null;
                boolean isActive = true;

                writer.println("Connected to server...");
                writer.println("Enter your username 'login -u=username'");
                String request;

                // login
                boolean isLogged = false;
                while (!isLogged) {
                    request = reader.readLine();
                    if (request.equals("exit")) {
                        writer.println("Good bye!");
                        isActive = false;
                        shutdown();
                        break;
                    }
                    // command request checking  and creating user
                    else if (CommandParsing.checkLoginCommand(request)) {
                        String loginName = CommandParsing.getLoginNameFromCommand(request);

                        user = new User(loginName);
                        user.setLogged(true);

                        writer.println("Welcome to server, " + user.getLoginName() + "!");
                        isLogged = true;

                    } else {
                        writer.println("Please, enter your login 'login -u=username'");
                    }
                }

                // rest commands
                String topicName;
                String voteName;
                while (true) {
                    request = reader.readLine();
                    if (request.equals("exit")) {
                        writer.println("Good bye, " + user.getLoginName() + "!");
                        shutdown();
                        break;

                    // creating new topic
                    } else if (request.startsWith("create topic -n=")) {
                        topicName = CommandParsing.getTopicName(request);
                        if (isTopicExists(topicName)) {
                            writer.println("'" + topicName + "'" + " is already exists. Use different topic name.");
                        } else {
                            Vote.topics.put(topicName, new ArrayList<>());
                            writer.println("You created a new topic " + "'" + topicName + "'");
                        }

                    // topics view
                    } else if (request.startsWith("view")) {
                        if (request.equals("view")) {
                            if (topics.isEmpty()) {
                                writer.println("Nobody has created any topic yet");
                            } else {
                                for (String voteCount : getAllVotesCount()) {
                                    writer.println(voteCount);
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
                                    printTopicDoesNtExist(topicName, writer);
                                } else {
                                    if (!isVoteExists(topicName, voteName)) {
                                        printVoteDoesNtExist(voteName, writer);
                                    } else {
                                        printVoteNameAndAnswers(topicName, voteName, writer);
                                    }
                                }

                            // view -t=<topic>
                            } else if (request.split("=").length == 2) {
                                // TODO добавить в парсинг проверки, если строка зканчивается на "="
                                topicName = CommandParsing.getTopicName(request);
                                if (isTopicExists(topicName)) {
                                    if (topics.get(topicName).isEmpty()) {
                                        writer.println("Nobody has created any voting yet");
                                    } else {
                                        for (String vote : getTopicVotesNames(topicName))
                                            writer.println(vote);
                                    }
                                } else {
                                    writer.println("'" + topicName + "'" + " doesn't exist.");
                                }
                            }
                        }

                    // create vote
                    } else if (request.startsWith("create vote -t=")) {
                        // TODO добавить в парсинг проверки, если строка зканчивается на '='
                        topicName = CommandParsing.getTopicName(request);
                        if (isTopicExists(topicName)) {
                            String name = createVotingName(reader, writer, topicName);
                            String description = createDescription(reader, writer);
                            TreeMap<String, Integer> answers = createListOfAnswers(reader, writer);

                            Vote vote = new Vote(name, description, answers, user);
                            topics.get(topicName).add(vote);
                            writer.println("The voting was just created");

                        } else {
                            writer.println("'" + topicName + "'" + " doesn't exist. You can't create voting");
                        }

                    // vote -t=<topic> -v=<vote>
                    } else if (request.startsWith("vote -t=")) {
                        if (request.split("=").length == 3) {
                            // TODO добавить проверки на корректность команды
                            String[] topicAndVoteNames = CommandParsing.getTopicAndVoteNames(request);
                            topicName = topicAndVoteNames[0];
                            voteName = topicAndVoteNames[1];
                            if (!isTopicExists(topicName)) {
                                printTopicDoesNtExist(topicName, writer);
                            } else {
                                if (!isVoteExists(topicName, voteName)) {
                                    printVoteDoesNtExist(voteName, writer);
                                } else {
                                    // vote for answer
                                    // TODO добавить проверки что пользователь не голосовал
                                    voteForAnswer(topicName, voteName, reader, writer, user);
                                    writer.println("Thank you! Your vote is important for us!");
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
                                printTopicDoesNtExist(topicName, writer);
                            } else {
                                if (!isVoteExists(topicName, voteName)) {
                                    printVoteDoesNtExist(voteName, writer);
                                } else {
                                    // delete func
                                    if (isUserTheVoteOwner(topicName, voteName, user)) {
                                        writer.println("Are you sure you want to delete the vote? (y/n)");
                                        request = reader.readLine().toLowerCase();
                                        if (request.equals("y")) {
                                            deleteTheVote(topicName, voteName, reader, writer);
                                        }
                                    } else {
                                        writer.println("You can't delete the vote. You're not the owner of the vote");
                                    }
                                }
                            }
                        }
                    // help message
                    } else if (request.equals("help")) {
                        CommandParsing.sendHelpMessage(writer);

                    // default server response after client login
                    } else {
                        writer.println("Use command 'help' for more information");
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
        public void shutdown() {
            try {
                writer.close();
                reader.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                //;
            }
        }
    }
    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                while (isServerActive) {
                    String consoleCommand = console.readLine();
                    if (consoleCommand.equals("exit")) {
                        console.close();
                        shutdown();
                    } else if (consoleCommand.startsWith("save \"")) {
                        saveVotesToFile(CommandParsing.getFileName(consoleCommand));
                    } else if (consoleCommand.startsWith("load \"")) {
                        loadVotesFromFile(CommandParsing.getFileName(consoleCommand));
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
