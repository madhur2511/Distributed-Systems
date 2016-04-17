package rmi.Helper;
import java.net.*;
import java.io.*;
import rmi.*;
import rmi.Helper.*;

public class ClientProcessor<T> implements Runnable{
    private final Socket clientSocket;
    protected T serverObject = null;
    protected Class<T> classObject = null;

    public ClientProcessor(Socket clientSocket, T serverObject, Class<T> classObject) {
        this.clientSocket = clientSocket;
        this.serverObject = serverObject;
        this.classObject = classObject;
    }

    public void run() {
        System.out.println("Got a client !" + clientSocket);

        // Do operations
        // Make sure the client operations on server object is synchronized on the server object

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
