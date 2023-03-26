package ru.latyshev;

import ru.latyshev.entities.DataStream;

import java.io.*;

public class Client {
    private static final String IP_ADDRESS = "127.0.0.1";
    public static void main(String[] args) {
        try (DataStream dataStream = new DataStream(IP_ADDRESS, 8080)) {
            System.out.println("Connected to server\nEnter your login 'login -u=username'");

            while (true) {
                String request = dataStream.sendCommand();
                dataStream.writeLine(request);

                String response = dataStream.readLine();
                System.out.println(response);

                if (request.equals("exit")) {
                    break;
                }
            }
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}