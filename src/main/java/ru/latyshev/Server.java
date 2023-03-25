package ru.latyshev;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import ru.latyshev.entities.DataStream;
import ru.latyshev.entities.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    public static final Logger log = LogManager.getLogger(Server.class);

    public static void main(String[] args){
        try (ServerSocket server = new ServerSocket(8080)){
            log.info("Server started...");
//            throw new IOException();

            while (true) {
                try (DataStream dataStream = new DataStream(server)) {
                    String request = dataStream.readLine();

                    //login
                    while (true) {
                        if (request.equals("exit")) {
                            dataStream.writeLine("Good bye!");
                            dataStream.close();
                        }
                        if (User.userLogin(request)) {
                            dataStream.writeLine("Welcome to server!");
                            break;
                        }
                        request = dataStream.readLine();
                    }

                    //next actions
                    request = dataStream.readLine();
                    while (true) {
                        if (request.equals("exit")) {
                            dataStream.writeLine("Good bye!");
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
