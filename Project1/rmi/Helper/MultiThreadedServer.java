package rmi.Helper;
import java.net.*;
import java.io.*;

public class MultiThreadedServer implements Runnable {

    private final int BACKLOG = 50;
    private ServerSocket listener = null;
    private Socket clientSocket = null;
    private InetSocketAddress address = null;

    public MultiThreadedServer(InetSocketAddress address) {
        this.address = address;
        System.out.println("Connecting to " + address.getAddress() + ":" + address.getPort());
    }

    public void run() {
        try{
            ServerSocket listener = new ServerSocket(address.getPort(), 50, address.getAddress());
            while(true){
                Socket clientSocket = listener.accept();
                System.out.println("Connected");
            }
        }
        catch(IOException e){
            System.out.println(e);
        }

        try {
            clientSocket.close();
            listener.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
