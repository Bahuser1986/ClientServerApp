package ru.latyshev;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import ru.latyshev.entities.DataStream;
import ru.latyshev.entities.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;


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

                    //login
                    while (true) {
                        if (request.equals("exit")) {
                            dataStream.writeLine("Good bye!");
                            dataStream.close();
                        }
                        //check command request and creating user
                        if (User.checkLoginCommand(request)) {
                            String loginName = User.getLoginNameFromCommand(request);

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

                    //next actions
                    request = dataStream.readLine();
                    while (true) {
                        if (request.equals("exit")) {
                            dataStream.writeLine("Good bye, " + user.getLoginName() + "!");
                            dataStream.close();
                        }
                        dataStream.writeLine("some answer");
                        request = dataStream.readLine();
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
