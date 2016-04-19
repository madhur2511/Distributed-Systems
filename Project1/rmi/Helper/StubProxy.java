package rmi.Helper;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import rmi.*;
import java.lang.reflect.*;
import java.util.logging.*;

public class StubProxy implements InvocationHandler
{
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
        boolean invoke_status = true;
        String str;
        Method method = null;

        try {
            Class[] argTypes = m.getParameterTypes();


            /* TODO:
                Make primitive types work by using the idea printed below
            */

            for (Class arg : argTypes)
                System.out.println(arg);

            method = proxy.getClass().getMethod(m.getName(), argTypes);
            if (method == null)
                logger.log(Level.WARNING, "Could not find a matching method. METHOD: " + m + " ARGS: " + args);

            socket = new Socket(address.getAddress(), address.getPort());
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            is = socket.getInputStream();
            ois = new ObjectInputStream(is);
            logger.log(Level.INFO, "Connected to SERVER: "
                                    + address.getAddress() + ":" + address.getPort());

            Message msg = new Message();
            msg.setMethodName(method.getName());
            msg.setArgs(args);
            oos.writeObject(msg);

            int i = 0;
            while(i < Utility.MAX_WAIT && is.available() == 0) {
                try {
                    Thread.sleep(Utility.SLEEPTIME);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                i += 1;
            }

            if (is.available() != 0) {
                invoke_status = (boolean) ois.readObject();
                result = ois.readObject();
                logger.log(Level.INFO, "Remote Invocation STATUS: " + invoke_status +
                                       " METHOD: " + method + " RESULT: " + result);
            } else {
                logger.log(Level.WARNING, "Remote Invocation timed out. METHOD: " + method + " SERVER: " +
                                          address.getAddress() + ":" + address.getPort());
                throw new Exception("RMI timed out");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            }
            catch (IOException e) {
                System.out.println(e);
            }
        }
        if (invoke_status != Utility.INVOKE_SUCCESS) {
            throw (Throwable)result;
        }
        return result;
    }
}
