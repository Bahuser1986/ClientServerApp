package ru.latyshev;

import java.net.ServerSocket;

public interface ConnectionHandler extends Runnable {
    void run(ServerSocket serverSocket);
}
