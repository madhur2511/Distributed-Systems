package rmi.Helper;
import rmi.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.logging.*;

public class ClientProcessor<T> implements Runnable{
    private final Socket clientSocket;
    // private Logger logger = Logger.getLogger(this.getClass().getName());

    protected T serverObject = null;
    protected Class<T> classObject = null;
    protected ObjectInputStream ois = null;
    protected ObjectOutputStream oos = null;

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

        // logger.log(Level.INFO, "Got a client: " + clientSocket);

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
                    returnObj = m.invoke(serverObject, msgObj.getArgs());
                }
                oos.writeObject(Utility.INVOKE_SUCCESS);
                oos.writeObject(returnObj);

                // logger.log(Level.INFO, "Invoked METHOD: " + m + " ARGS: " + msgObj.getArgs() +
                                        // " RESULT: " + returnObj);

                System.out.println("Skeleton responding with " + returnObj);
            }
        }

        catch (InvocationTargetException e) {
            // logger.log(Level.WARNING, "Invocation Exception, METHOD: " + m +
                                    //   " ARGS: " + msgObj.getArgs() +
                                    //   " EXCEPTION: " + e.getMessage());
            try {
                oos.writeObject(Utility.INVOKE_FAILURE);
                oos.writeObject(e.getTargetException());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        catch (SocketException e) {
            // logger.log(Level.WARNING, "Socket Connection Error: " + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            // logger.log(Level.WARNING, "Non-Invocation Exception, METHOD: " + m +
                                    //   " ARGS: " + msgObj.getArgs() +
                                    //   " EXCEPTION: " + e.getMessage());
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
