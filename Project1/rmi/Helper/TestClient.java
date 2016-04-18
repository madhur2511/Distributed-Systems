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
        Socket skt;
        PrintStream os;
        TestInterface proxyTest;
        try {
            String result;
            InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 11111);
            proxyTest = Stub.create(TestInterface.class, addr);
            result = proxyTest.testMessage("Hello");
            System.out.println("SUCCESS: Result - " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
