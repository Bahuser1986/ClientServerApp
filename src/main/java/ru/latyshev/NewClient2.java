package ru.latyshev;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NewClient2 implements Runnable{
    private Socket client;
    private BufferedReader reader;
    private PrintWriter writer;
    boolean isActive = true;
    private static final String IP_ADDRESS = "127.0.0.1";
    @Override
    public void run() {
        try {
            client = new Socket(IP_ADDRESS, 8080);
            writer = new PrintWriter(client.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler handler = new InputHandler();
            Thread thread = new Thread(handler);
            thread.start();

            String response;
            while ((response = reader.readLine()) != null) {
                System.out.println(response);
            }

        } catch (IOException e) {
            shutdown();
        }
    }
    private void shutdown() {
        try {
            isActive = false;
            reader.close();
            writer.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            shutdown();
        }
    }
    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
                while (isActive) {
                    String request = console.readLine();
                    // TODO найти NullPointerExeption при выходе клиента
                    if (request.equals("exit")) {
                        writer.println(request);
                        console.close();
                        shutdown();
                    } else {
                        writer.println(request);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        NewClient2 client = new NewClient2();
        client.run();
    }

}
