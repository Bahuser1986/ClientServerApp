package ru.latyshev.entities;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

//creates I/O streams
public class DataStream implements Closeable{
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
//    private BufferedReader console;

    public DataStream(String ip, int port){
        try {
            this.socket = new Socket(ip, port);
            this.reader = createReader();
            this.writer = createWriter();
            //this.console = createConsole();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public DataStream(ServerSocket socket) {
        try {
            this.socket = socket.accept();
            this.reader = createReader();
            this.writer = createWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedReader createReader() throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    private BufferedWriter createWriter() throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }
    private BufferedReader createConsole() throws IOException {
        return new BufferedReader(new InputStreamReader(System.in));
    }
    public void writeLine(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String readLine(){
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//    public String getCommand(){
//        try {
//            return console.readLine();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    public void close() throws IOException {
        //console.close();
        writer.close();
        reader.close();
        socket.close();
    }
}
