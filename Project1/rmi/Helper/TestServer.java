package rmi;
import rmi.*;

public class TestServer implements TestInterface{
    public String testMessage(String part1) throws RMIException{
        return "Hello " + part1;
    }

    public static void main(String[] args) throws RMIException{
        TestServer server = new TestServer();
        Skeleton<TestInterface> skeleton = new Skeleton<TestInterface>(TestInterface.class, server);
        skeleton.start();
    }
}
