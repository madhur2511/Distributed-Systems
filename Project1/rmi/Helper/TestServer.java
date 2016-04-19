package rmi;
import java.io.*;
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

    public int addNum(int a, int b) throws RMIException{
        return a + b;
    }

    public String exceptionThrower(String msg) throws RMIException {
        throw new RMIException("Exception: " + msg);
    }

    public static void main(String[] args) throws RMIException{
        TestServer server = new TestServer();
        //try{
        addr = new InetSocketAddress(7000);
        Skeleton<TestInterface> skeleton = new Skeleton<TestInterface>(TestInterface.class, server, addr);

        if(probe())
            System.out.println("skeleton accepts connections before start");
        try{
            skeleton.start();
        }catch(RMIException e){
            System.out.println("unable to start skeleton");
        }

        if(!probe())
            System.out.println("skeleton refuses connections after start");

        skeleton.stop();

        if(probe())
           System.out.println("skeleton accepts connections after stop");




        //}catch(UnknownHostException e){
        //    e.printStackTrace();
        //}
    }

    private static boolean probe()
    {
        Socket socket = new Socket();
        try{
            socket.connect(new InetSocketAddress(7000));
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        try{
            socket.close();
        }catch(Exception e){
        }
        return true;
    }
}
