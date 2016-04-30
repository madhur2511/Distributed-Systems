package rmi;

import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.*;
import java.lang.*;
import rmi.*;
import java.lang.reflect.*;
import java.util.logging.*;

public class StubProxy implements InvocationHandler, Serializable
{
    private InetSocketAddress address = null;
    // private final transient Logger logger = Logger.getLogger(this.getClass().getName());

    public StubProxy(InetSocketAddress address) {
        this.address = address;
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
    {
        try {
            if (Utility.isValidRemoteMethod(m))
                return remoteInvoke(proxy, m, args);
            else
                return localInvoke(proxy, m, args);
        } catch (Exception e) {
            throw e;
        }
    }

    private Object remoteInvoke(Object proxy, Method m, Object[] args) throws Throwable
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
            // Note that, we should not be matching up method signatures on the
            // client side. server is the right place to do this.

            socket = new Socket(address.getAddress(), address.getPort());
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            is = socket.getInputStream();
            ois = new ObjectInputStream(is);

            //logger.log(Level.INFO, "Connected to SERVER: "
            //                        + address.getAddress() + ":" + address.getPort());

            Message msg = new Message();
            msg.setMethodName(m.getName());
            msg.setArgs(args);
            msg.setArgTypes(argTypes);

            oos.writeObject(msg);

            // System.out.println("StubProxy asking for method" + m.getName());

            invoke_status = (boolean) ois.readObject();
            result = ois.readObject();

            // System.out.println("Received result at client stubproxy: " + result);


            //logger.log(Level.INFO, "Remote Invocation STATUS: " + invoke_status +
            //                       " METHOD: " + method + " RESULT: " + result);

            // } else {
            //     logger.log(Level.WARNING, "Remote Invocation timed out. METHOD: " + method + " SERVER: " +
            //                               address.getAddress() + ":" + address.getPort());
            //     throw new Exception("RMI timed out");
            // }

        } catch (Exception e) {
            // System.out.println("Exception: " + e);

            throw new RMIException(e);
        } finally {
            try {
                if((socket != null) && !socket.isClosed())
                    socket.close();
            }
            catch (IOException e) {
                System.out.println(e);
            }
        }
        if (invoke_status != Utility.INVOKE_SUCCESS) {
            System.out.println(result.toString());
            throw (Throwable)result;
        }
        return result;
    }

    private Object localInvoke(Object proxy, Method m, Object[] args) throws Throwable
    {
        StubProxy handler = (StubProxy) Proxy.getInvocationHandler(proxy);

        if (handler == null)
            throw new Exception("couldn't get StubProxy handler");

        if (m.getName().equals("hashCode"))
            return hashCode(proxy, handler);
        else if (m.getName().equals("equals") && (args.length == 1))
            return equals(proxy, args[0]);
        else if (m.getName().equals("toString"))
            return toString(proxy, handler);
        else
            return m.invoke(handler, args);
    }

    private int hashCode(Object proxy, StubProxy handler) {
        return proxy.getClass().hashCode() + handler.address.hashCode();
    }

    private String toString(Object proxy, StubProxy handler) {
        String s = "";
        for (Class c : proxy.getClass().getInterfaces())
            s = s.concat(c.toString());

        return s + " " + handler.address.toString();
    }

    private boolean equals(Object proxy, Object other) {
        if (other == null)
            return false;

        if (!Proxy.isProxyClass(other.getClass()) ||
            !proxy.getClass().equals(other.getClass()))
            return false;

        InvocationHandler handler = Proxy.getInvocationHandler(other);
        if (!(handler instanceof StubProxy))
            return false;

        if (!address.equals(((StubProxy) handler).address))
            return false;

        return true;
    }
}
