package ru.latyshev;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import ru.latyshev.entities.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Server {
    public static final Logger log = LogManager.getLogger(Server.class);
    private static Set<User> users = new HashSet<>();
    private static Map<String, List<Vote>> topicList = new HashMap<>();
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
                            if (topicList.containsKey(topicName)) {
                                dataStream.writeLine(topicName + " is already exists. Use different topic name.");
                            } else {
                                topicList.put(topicName, new ArrayList<>());
                                dataStream.writeLine("You created a new topic " + topicName);
                            }

                        // topics view
                        }
//                        else if (request.startsWith("view")) {
//                            if (request.equals("view")) {}
//                            else if (request.startsWith("view -t=")) {}
//                            else {request = dataStream.readLine();}
//                        }
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
}
