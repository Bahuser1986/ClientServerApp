//package ru.latyshev;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import ru.latyshev.entities.DataStream;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.ServerSocket;
//
//public class Server1 implements ConnectionHandler {
//    public static final Logger log = LogManager.getLogger(Server.class);
//    @Override
//    public void run() {}
//    @Override
//    public void run(ServerSocket serverSocket) {
//        DataStream dataStream = new DataStream(serverSocket);
//        dataStream.writeLine("Connected to server...");
//        dataStream.writeLine("Enter your username 'login -u=username'");
//        String request = dataStream.readLine();
//
//    }
//    class InputHandler implements Runnable {
//        @Override
//        public void run() {
//            try {
//                BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
//                while (isActive) {
//                    String request = console.readLine();
//                    // TODO найти NullPointerExeption при выходе клиента
//                    if (request.equals("exit")) {
//                        writer.println(request);
//                        console.close();
//                        shutdown();
//                    } else {
//                        writer.println(request);
//                    }
//                }
//            } catch (IOException e) {
//                shutdown();
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//        NewClient2 client = new NewClient2();
//        client.run();
//    }
//
//
//
//    public static void main(String[] args) {
//        try (ServerSocket serverSocket = new ServerSocket(8080)) {
//            log.info("Server started...");
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
