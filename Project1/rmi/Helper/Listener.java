package rmi.Helper;
import java.net.*;
import java.io.*;
import rmi.*;
import rmi.Helper.*;

public class Listener<T> implements Runnable{
    private final int BACKLOG = 50;
    private ServerSocket listener = null;
    private InetSocketAddress address = null;
    private Skeleton<T> skeletonReference = null;

    public Listener(Skeleton<T> skeletonReference, InetSocketAddress address){
        this.address = address;
        this.skeletonReference = skeletonReference;
        System.out.println("Binding skeleton-listener to " + address.getAddress() + ":" + address.getPort());
    }

    public void run() {
        try{
            ServerSocket listener = new ServerSocket(address.getPort(), BACKLOG, address.getAddress());
            while(!Thread.currentThread().isInterrupted()){
                Socket clientSocket = listener.accept();
                System.out.println("Connected");
                skeletonReference.newClient(clientSocket);
            }
        }
        catch(IOException e){
            System.out.println(e);
        }
    }
}
