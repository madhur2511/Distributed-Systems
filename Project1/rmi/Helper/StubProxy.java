package rmi;
import rmi.*;
import java.io.*;
import java.net.*;
import rmi.Helper.*;
import java.lang.reflect.*;

public class StubProxy implements InvocationHandler
{
    private final InetSocketAddress address;

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

        try {
            Class[] argTypes = new Class[args.length];
            int i = 0;
            for (Object arg : args) {
                argTypes[i++] = arg.getClass();
            }
            Method method = proxy.getClass().getMethod(m.getName(), argTypes);

            socket = new Socket(address.getAddress(), address.getPort());
            Message msg = new Message();
            msg.setMethodName(method.getName());
            msg.setArgs(args);

            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(msg);

            is = socket.getInputStream();
            ois = new ObjectInputStream(is);
            result = ois.readObject();
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
