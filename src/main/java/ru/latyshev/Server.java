package ru.latyshev;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    private static final Logger log = LogManager.getLogger(Server.class);
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(8080)){
            log.info("Server started...");
//            throw new IOException();

            while (true) {
                try (Socket socket = server.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream()))) {

                    log.info("Client connected");

                    String request = reader.readLine();
                    System.out.println("Request: " + request);

                    String response = "Hello from server: " + request.length();
                    System.out.println("Response: " + response);
                    writer.write(response);
                    writer.newLine();
                    writer.flush();

                } catch (NullPointerException e) {
                    log.error(e + "/" + e.getMessage());
                    e.printStackTrace();
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
