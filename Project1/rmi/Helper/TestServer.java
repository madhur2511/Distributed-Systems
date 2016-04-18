package rmi;
import rmi.*;
import java.net.*;
import java.lang.*;
import java.util.logging.*;

public class TestServer implements TestInterface{
    private static int numCall = 0;
    private static InetSocketAddress addr = null;

    public String testMessage(String part) throws RMIException{
        numCall += 1;
        return "MK " + part + ", you are caller number " + numCall;
    }

    public Integer addNum(Integer a, Integer b) throws RMIException{
        return a + b;
    }

    public static void main(String[] args) throws RMIException{
        TestServer server = new TestServer();
        //try{
            addr = new InetSocketAddress(7000);
            Skeleton<TestInterface> skeleton = new Skeleton<TestInterface>(TestInterface.class, server, addr);
            skeleton.start();
            //System.out.println(probe());
        //}catch(UnknownHostException e){
        //    e.printStackTrace();
        //}
        //skeleton.stop();
    }

    private static boolean probe()
    {
        Socket socket = new Socket();

        try
        {
            socket.connect(addr);
        }
        catch(Exception e)
        {
            return false;
        }

        try
        {
            socket.close();
        }
        catch(Exception e) { }

        return true;
    }
}
