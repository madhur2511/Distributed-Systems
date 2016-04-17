package rmi;
import rmi.*;

public class TestServer implements TestInterface{
    private static int numCall = 0;
    public String testMessage(String part) throws RMIException{
        numCall++;
        return "Hello"+part+", you are caller number "+numCall;
    }

    public static void main(String[] args) throws RMIException{
        TestServer server = new TestServer();
        Skeleton<TestInterface> skeleton = new Skeleton<TestInterface>(TestInterface.class, server);
        skeleton.start();
    }
}
