package rmi;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;

public class StubProxy implements InvocationHandler
{
    private final InetSocketAddress address;

    public StubProxy(InetSocketAddress address) {
        this.address = address;
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
    {
        Socket skt = null;
        InputStream is = null;
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        Object result = null;
        String str;
        try {
            // now, find the matching
            Class[] argTypes = new Class[args.length];
            int i = 0;
            for (Object arg : args) {
                argTypes[i++] = arg.getClass();
            }
            Method method = proxy.getClass().getMethod(m.getName(), argTypes);

            skt = new Socket(address.getHostName(), address.getPort());
            is = skt.getInputStream();
            ois = new ObjectInputStream(is);
            oos = new ObjectOutputStream(skt.getOutputStream());

            // result = m.invoke(proxy, args);
            oos.writeObject(m.getName());
            oos.writeObject(args);

            int total = 0;
            while(total < 120000 && is.available() == 0) {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                }
                total = total + 1000;
            }
            if(is.available() != 0) {
                result = ois.readObject();
            } else {
                throw new Exception("timed out after 120000ms");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            is.close();
            ois.close();
            oos.close();
            skt.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
        return result;
    }
}
