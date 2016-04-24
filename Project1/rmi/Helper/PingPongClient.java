package rmi;
import java.io.*;
import java.net.*;
import java.util.logging.*;

import rmi.PingPongInterface;

public class PingPongClient{
    private static Logger logger = Logger.getLogger(Stub.class.getName());

    public static void main(String[] args) throws RMIException{
        int id = 0;
        int pass = 0;
        String result = "";
        PingPongInterface proxy = null;

        try {

            InetSocketAddress addr = new InetSocketAddress("rmiserver", 9999);
            proxy = Stub.create(PingPongInterface.class, addr);

            logger.log(Level.INFO, "Stub created");

            for (id = 0; id < 4; id++) {
                result = proxy.ping(id);
                if (result.equals("Pong, ID: " + id))
                    pass += 1;
            }

        } catch (Exception e) {
            logger.log(Level.INFO, "Exception: " + e);
            e.printStackTrace();
        } finally {
            System.out.println(pass + " Tests Completed, " + (4-pass) + " Tests Failed.");
            logger.log(Level.INFO, pass + " Tests Completed, " + (4-pass) + " Tests Failed.");
        }
    }
}
