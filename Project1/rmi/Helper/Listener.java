package rmi.Helper;
import java.net.*;
import java.io.*;
import rmi.*;
import rmi.Helper.*;
import java.util.logging.*;

public class Listener<T> implements Runnable{

    private final int BACKLOG = 50;
    protected ServerSocket listener = null;
    protected InetSocketAddress address = null;
    protected Skeleton<T> skeletonReference = null;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public Listener(Skeleton<T> skeletonReference, InetSocketAddress address){
        this.address = address;
        this.skeletonReference = skeletonReference;
        logger.log(Level.INFO, "Binding skeleton-listener to " + address.getAddress() + ":" + address.getPort());
    }

    public void run() {
        try{
            ServerSocket listener = new ServerSocket(address.getPort(), BACKLOG, address.getAddress());
            while(!Thread.currentThread().isInterrupted()){
                Socket clientSocket = listener.accept();
                logger.log(Level.INFO, "Connected to client: " + clientSocket.getInetAddress());
                skeletonReference.newClient(clientSocket);
            }
        }
        catch(IOException e){
            System.out.println(e);
        }
    }
}
