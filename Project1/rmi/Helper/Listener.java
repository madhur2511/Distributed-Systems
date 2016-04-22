package rmi.Helper;
import java.net.*;
import java.io.*;
import rmi.*;
import java.util.logging.*;

public class Listener<T> implements Runnable{

    protected ServerSocket listener = null;
    protected InetSocketAddress address = null;
    protected Skeleton<T> skeletonReference = null;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public Listener(Skeleton<T> skeletonReference, ServerSocket listener){
        this.listener = listener;
        this.skeletonReference = skeletonReference;
    }

    public void run() {
        try{
            while(true){
                Socket clientSocket = listener.accept();
                System.out.println("Connected to client: " + clientSocket.getInetAddress());

                logger.log(Level.INFO, "Connected to client: " + clientSocket.getInetAddress());
                skeletonReference.newClient(clientSocket);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(!listener.isClosed())
                    listener.close();
            }catch(Exception e){}
        }
    }
}
