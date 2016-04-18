package rmi.Helper;
import rmi.*;
import java.io.*;
import java.net.*;
import rmi.Helper.*;
import java.lang.reflect.*;
import java.util.logging.*;

public class ClientProcessor<T> implements Runnable{
    private final Socket clientSocket;
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private final boolean INVOKE_SUCCESS = true;
    private final boolean INVOKE_FAILURE = false;

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

        logger.log(Level.INFO, "Got a client: " + clientSocket);

        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(clientSocket.getInputStream());
            readObj = ois.readObject();

            if (readObj instanceof Message) {
                msgObj = (Message)readObj;
                Class[] argTypes = new Class[msgObj.getArgs().length];
                int i = 0;
                for (Object arg : msgObj.getArgs()) {
                    argTypes[i++] = arg.getClass();
                }

                synchronized(serverObject) {
                    m = serverObject.getClass().getMethod(msgObj.getMethodName(), argTypes);
                    returnObj = m.invoke(serverObject, msgObj.getArgs());
                    oos.writeObject(INVOKE_SUCCESS);
                    oos.writeObject(returnObj);
                    logger.log(Level.INFO, "Invoked METHOD: " + m + " ARGS: " + msgObj.getArgs() +
                                            " RESULT: " + returnObj);

                }
            }
        //} catch (IOException e) {
        //    e.printStackTrace();
        //} catch (ClassNotFoundException e) {
        //    e.printStackTrace();
        //} catch (NoSuchMethodException e) {
        //    e.printStackTrace();
        //} catch (IllegalAccessException e) {
        //    e.printStackTrace();
        } catch (InvocationTargetException e) {
            logger.log(Level.WARNING, "Invocation Exception, METHOD: " + m +
                                      " ARGS: " + msgObj.getArgs() +
                                      " EXCEPTION: " + e.getMessage());
            try {
                oos.writeObject(INVOKE_FAILURE);
                oos.writeObject(e);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Non-Invocation Exception, METHOD: " + m +
                                      " ARGS: " + msgObj.getArgs() +
                                      " EXCEPTION: " + e.getMessage());
            try {
                oos.writeObject(INVOKE_FAILURE);
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
