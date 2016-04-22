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

        try{
            skeleton.start();
        }catch(RMIException e){
            System.out.println("unable to start skeleton");
        }

        //skeleton.stop();


        //}catch(UnknownHostException e){
        //    e.printStackTrace();
        //}
    }
}
