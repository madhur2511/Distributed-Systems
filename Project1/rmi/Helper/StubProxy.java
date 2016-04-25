package rmi.Helper;
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
    private final InetSocketAddress address;
    //private final transient Logger logger = Logger.getLogger(this.getClass().getName());

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

            method = proxy.getClass().getMethod(m.getName(), argTypes);
            if (method == null) {
                //logger.log(Level.WARNING, "Could not find a matching method. METHOD: " + m + " ARGS: " + args);
                throw new Exception("remote interface: " + proxy.getClass().getName() + " does not implement the method: "
                                    + m.getName() + "(" + argTypes + ")");
            }

            socket = new Socket(address.getAddress(), address.getPort());
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            is = socket.getInputStream();
            ois = new ObjectInputStream(is);
            //logger.log(Level.INFO, "Connected to SERVER: "
            //                        + address.getAddress() + ":" + address.getPort());

            Message msg = new Message();
            msg.setMethodName(method.getName());
            msg.setArgs(args);
            msg.setArgTypes(argTypes);

            oos.writeObject(msg);

            // TODO: Is there a testcase for this time-out ? If not, lets skip this, because
            // is_available doesnt check remote socket state anyways.


            // int i = 0;
            // while(i < Utility.MAX_WAIT && is.available() == 0) {
            //     try {
            //         Thread.sleep(Utility.SLEEPTIME);
            //     } catch (InterruptedException ex) {
            //         Thread.currentThread().interrupt();
            //     }
            //     i += 1;
            // }

            //if (is.available() != 0) {

            invoke_status = (boolean) ois.readObject();
            result = ois.readObject();
            //logger.log(Level.INFO, "Remote Invocation STATUS: " + invoke_status +
            //                       " METHOD: " + method + " RESULT: " + result);

            // } else {
            //     logger.log(Level.WARNING, "Remote Invocation timed out. METHOD: " + method + " SERVER: " +
            //                               address.getAddress() + ":" + address.getPort());
            //     throw new Exception("RMI timed out");
            // }

        } catch (Exception e) {


            //TODO: NEED TO VERIFY THIS ONCE
            //TODO: NEED TO VERIFY THIS ONCE
            //TODO: NEED TO VERIFY THIS ONCE
            //TODO: NEED TO VERIFY THIS ONCE
            //TODO: NEED TO VERIFY THIS ONCE
            //TODO: NEED TO VERIFY THIS ONCE
            //TODO: NEED TO VERIFY THIS ONCE
            //TODO: NEED TO VERIFY THIS ONCE

            //logger.log(Level.WARNING, "remoteInvoke Failed, err: " + e);
            throw new RMIException(e);
        } finally {
            try {
                if(!socket.isClosed())
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
        return proxy.getClass().getName() + " " + handler.address.toString();
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
