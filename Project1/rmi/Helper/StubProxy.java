package rmi;
import rmi.*;
import java.io.*;
import java.net.*;
import rmi.Helper.*;
import java.lang.reflect.*;
import java.util.logging.*;

public class StubProxy implements InvocationHandler
{
    private final int MAX_WAIT = 30;        // total timeout = 30 * 2000 = 60secs
    private final int SLEEPTIME = 2000;     // 2 secs
    private final InetSocketAddress address;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public StubProxy(InetSocketAddress address) {
        this.address = address;
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
    {
        Socket socket = null;
        InputStream is = null;
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        Object result = null;
        String str;
        Method method = null;

        try {
            Class[] argTypes = new Class[args.length];
            int i = 0;
            for (Object arg : args) {
                argTypes[i++] = arg.getClass();
            }
            method = proxy.getClass().getMethod(m.getName(), argTypes);
            if (method == null)
                logger.log(Level.WARNING, "Could not find a matching mathod: " + m + " with args: " + args);

            socket = new Socket(address.getAddress(), address.getPort());
            logger.log(Level.INFO, "Connected to server running at: "
                                    + address.getAddress() + ":" + address.getPort());

            Message msg = new Message();
            msg.setMethodName(method.getName());
            msg.setArgs(args);

            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(msg);
            logger.log(Level.INFO, "Message sent to server: " + msg);

            is = socket.getInputStream();
            ois = new ObjectInputStream(is);

            i = 0;
            while(i < MAX_WAIT && is.available() == 0) {
                try {
                    Thread.sleep(SLEEPTIME);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                i += 1;
            }

            if (is.available() != 0) {
                result = ois.readObject();
                logger.log(Level.INFO, "Results of the method: " + method + " are" + result);
            } else {
                logger.log(Level.WARNING, "Call to method: " + method + " on server: " +
                                        address.getAddress() + ":" + address.getPort() +
                                        " timed out.");
                throw new Exception("timed out");
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
        return result;
    }
}
