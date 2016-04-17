package rmi.Helper;
import java.net.*;
import java.io.*;
import java.lang.reflect.*;

public class MultiThreadedServer implements Runnable {

    private final int BACKLOG = 50;
    private ServerSocket listener = null;
    private Socket clientSocket = null;
    private InetSocketAddress address = null;
    private Object server;

    public MultiThreadedServer(InetSocketAddress address, Object server) {
        this.address = address;
        this.server = server;
        System.out.println("Connecting to " + address.getAddress() + ":" + address.getPort());
    }

    public void run() {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        String result = null;
        String mstr;
        Object[] args;
        Class[] argTypes;
        Method meth;
        try{
            ServerSocket listener = new ServerSocket(address.getPort(), 50, address.getAddress());
            while(true){
                System.out.println("Waiting for client");
                Socket clientSocket = listener.accept();
                ois = new ObjectInputStream(clientSocket.getInputStream());

                // Read method name and args list
                mstr = (String) ois.readObject();
                args = (Object[]) ois.readObject();

                // Find the matching method to invoke
                argTypes = new Class[args.length];
                int i = 0;
                for (Object arg : args) {
                    argTypes[i++] = arg.getClass();
                }
                meth = server.getClass().getMethod(mstr, argTypes);
                result = (String) meth.invoke(server, args);

                // return results.
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
                oos.writeObject(result);
            }
        }
        catch(IOException e){
            System.out.println(e);
        } catch (Exception e) {
            e.printStackTrace();
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
