package rmi.Helper;
import rmi.*;
import java.io.*;
import java.net.*;
import rmi.Helper.*;
import java.lang.reflect.*;
import java.util.logging.*;

public class ClientProcessor<T> implements Runnable{
    private final Socket clientSocket;
    protected T serverObject = null;
    protected Class<T> classObject = null;
    protected ObjectInputStream ois = null;
    protected ObjectOutputStream oos = null;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public ClientProcessor(Socket clientSocket, T serverObject, Class<T> classObject) {
        this.clientSocket = clientSocket;
        this.serverObject = serverObject;
        this.classObject = classObject;
    }

    public void run() {
<<<<<<< Updated upstream
        logger.log(Level.INFO, "Got a client: " + clientSocket);
=======
        System.out.println("Got a client " + clientSocket);
>>>>>>> Stashed changes

        try{
            ois = new ObjectInputStream(clientSocket.getInputStream());
            Object readObj = ois.readObject();

            if(readObj instanceof Message){
                Message msgObj = (Message)readObj;
                Class[] argTypes = new Class[msgObj.getArgs().length];
                int i = 0;
                for (Object arg : msgObj.getArgs()) {
                    argTypes[i++] = arg.getClass();
                }
                Object returnObj = null;
                synchronized(serverObject){
                    Method m = serverObject.getClass().getMethod(msgObj.getMethodName(), argTypes);
                    returnObj = m.invoke(serverObject, msgObj.getArgs());
                    logger.log(Level.INFO, "Invoked method: " + m + " with args: " + msgObj.getArgs() +
                                            " result: " + returnObj);

                }
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
                if (returnObj != null)
                    oos.writeObject(returnObj);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch(NoSuchMethodException e){
            e.printStackTrace();
        }catch(IllegalAccessException e){
            e.printStackTrace();
        }catch(InvocationTargetException e){
            e.printStackTrace();
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
