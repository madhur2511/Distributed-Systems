package rmi;
import rmi.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.logging.*;

public class ClientProcessor<T> implements Runnable{
    private final Socket clientSocket;
    // private Logger logger = Logger.getLogger(this.getClass().getName());

    private T serverObject = null;
    private Class<T> classObject = null;
    private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;

    public ClientProcessor(Socket clientSocket, T serverObject, Class<T> classObject) {
        this.clientSocket = clientSocket;
        this.serverObject = serverObject;
        this.classObject = classObject;
    }

    public void run() {
        Object readObj = null;
        Object returnObj = null;
        Message msgObj = null;
        Method m = null;

        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.flush();

            ois = new ObjectInputStream(clientSocket.getInputStream());
            if(!this.clientSocket.isClosed()){
                readObj = ois.readObject();
            }

            if (readObj != null && readObj instanceof Message) {
                msgObj = (Message)readObj;

                synchronized(serverObject) {
                    m = serverObject.getClass().getMethod(msgObj.getMethodName(), msgObj.getArgTypes());
                    m.setAccessible(true);
                    returnObj = m.invoke(serverObject, msgObj.getArgs());
                }
                oos.writeObject(Utility.INVOKE_SUCCESS);
                oos.writeObject(returnObj);

                System.out.println("Skeleton responding with " + returnObj);
            }
        }
        catch (InvocationTargetException e) {
            try {
                oos.writeObject(Utility.INVOKE_FAILURE);
                oos.writeObject(e.getTargetException());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        catch (SocketException e) {}
        catch (Exception e) {
            e.printStackTrace();
            try {
                oos.writeObject(Utility.INVOKE_FAILURE);
                oos.writeObject(e);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
