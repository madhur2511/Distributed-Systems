package rmi;
import java.io.*;
import java.net.*;
import java.util.logging.*;

import rmi.PingPongInterface;

public class PingPongClient{
    // private static Logger logger = Logger.getLogger(Stub.class.getName());

    public static void main(String[] args) throws RMIException{
        int id = 0;
        int pass = 0;
        String result = "";
        PingServerFactoryInterface factory = null;
        PingPongInterface clientProxy = null;
        InetSocketAddress pingaddr = null;

        try {

            InetSocketAddress addr = new InetSocketAddress("rmiserver", 50002);
            // InetSocketAddress addr = new InetSocketAddress("localhost", 50002);

            factory = Stub.create(PingServerFactoryInterface.class, addr);
            // logger.log(Level.INFO, "factory Stub created");
            // logger.log(Level.INFO, "factory Stub created");

            clientProxy = (PingPongInterface) factory.makePingServer();

            if (clientProxy == null)
                throw new Exception("Something bad happened.");

            for (id = 0; id < 4; id++) {
                result = clientProxy.ping(id);
                System.out.println(result);
                if (result.equals("Pong, ID: " + id))
                    pass += 1;
            }

        } catch (Exception e) {
            // logger.log(Level.INFO, "Exception: " + e);
            // e.printStackTrace();
        } finally {
            System.out.println(pass + " Tests Completed, " + (4 - pass) + " Tests Failed.");
            // logger.log(Level.INFO, pass + " Tests Completed, " + (4-pass) + " Tests Failed.");
        }
    }
}
