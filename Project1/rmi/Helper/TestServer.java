package rmi;
import rmi.*;
import java.net.*;
import java.util.logging.*;

public class TestServer implements TestInterface{
    private static int numCall = 0;

    public String testMessage(String part) throws RMIException{
        numCall += 1;
        return "MK " + part + ", you are caller number " + numCall;
    }

    public static void main(String[] args) throws RMIException{
        TestServer server = new TestServer();
        try{
            InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 11111);
            Skeleton<TestInterface> skeleton = new Skeleton<TestInterface>(TestInterface.class, server, addr);
            skeleton.start();

        }catch(UnknownHostException e){
            e.printStackTrace();
        }
        //skeleton.stop();
    }
}
