package rmi;

import rmi.*;
import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.logging.*;

public class PingServerFactory implements PingServerFactoryInterface
{
    // private static Logger logger = Logger.getLogger(Stub.class.getName());
    private int port = 50001;

    public PingServerFactory() throws RMIException {
        port = 50001;
    }

    public Object makePingServer() throws RMIException {

        InetSocketAddress addr1 = new InetSocketAddress("rmiserver", port);
        // InetSocketAddress addr1 = new InetSocketAddress("localhost", port);
        port += 1;
        PingPongServer pingserver = new PingPongServer(addr1);

        try {
            Skeleton<PingPongInterface> skeleton1 = new Skeleton<PingPongInterface>(PingPongInterface.class, pingserver, addr1);

            skeleton1.start();
            // logger.log(Level.INFO, "started ping server at: " + addr1 + ":" + addr1.getPort());

            PingPongInterface pingStub = Stub.create(PingPongInterface.class, skeleton1);

            // logger.log(Level.INFO, "created stub for ping server.");

            return pingStub;

        } catch(RMIException e) {
            // logger.log(Level.WARNING, "unable to create ping server, " + e);
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String args[])
    {
        try
        {
            PingServerFactory serverfactory = new PingServerFactory();
            InetSocketAddress addr = new InetSocketAddress(50002);

            Skeleton<PingServerFactoryInterface> skeleton =
                    new Skeleton<PingServerFactoryInterface>(PingServerFactoryInterface.class, serverfactory, addr);

            skeleton.start();
            // logger.log(Level.INFO, "PingServerFactory skeleton started");
        }
        catch (Exception e)
        {
            // logger.log(Level.WARNING, "failed to start PingServerFactory skeleton, err: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
