package rmi;
import java.io.*;
import java.net.*;

import rmi.TestInterface;

public class TestClient{
    /*
        1. The client should be made aware of the service names and corresponding skeleton INetAddresses
        2. Client should attain the proxyObject somehow and call method on it
    */
    // TestInterface proxyObj = null;  //something;
    // String response = proxyObj.testMessage("Hello ");
    public static void main(String[] args) throws RMIException{
        InetSocketAddress addr;
        Socket skt;
        PrintStream os;
        TestInterface proxyTest;
        try {
            addr = new InetSocketAddress(11112);
            skt = new Socket("127.0.0.1", 11112);
            os = new PrintStream(skt.getOutputStream());
            proxyTest = Stub.create(TestInterface.class, addr);
            //String result = proxyTest.testMessage("Hello from proxy");
            System.out.println("done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
