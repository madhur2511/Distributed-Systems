package rmi;
import java.io.*;
import java.net.*;
import java.util.logging.*;

import rmi.TestInterface;

public class TestClient{

    public static void main(String[] args) throws RMIException{
        Socket skt;
        PrintStream os;
        TestInterface proxyTest;
        try {
            String result;
            InetSocketAddress addr = new InetSocketAddress(50001);
            proxyTest = Stub.create(TestInterface.class, addr);

            result = proxyTest.testMessage("Hello");
            System.out.println("SUCCESS: Result : " + result);
            System.out.println("SUCCESS: Result : " + proxyTest.addNum(1, 3));
            proxyTest.boolMirror(true);
            System.out.println(proxyTest.toString());
            result = proxyTest.exceptionThrower("junk");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
