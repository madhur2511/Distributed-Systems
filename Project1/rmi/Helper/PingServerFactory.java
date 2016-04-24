package rmi;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.logging.*;

public class PingServerFactory implements PingServerFactoryInterface
{
    private static Logger logger = Logger.getLogger(Stub.class.getName());

    private int port = 0;
    public PingServerFactory() throws RMIException {
        port = 9999;
    }

    public PingPongServer makePingServer() throws RMIException {
        // instantiate server and return skeleton obj
        InetSocketAddress addr = new InetSocketAddress(port);
        port += 1;
        PingPongServer pingserver = new PingPongServer(addr);

        try {
            Skeleton<PingPongInterface> skeleton = new Skeleton<PingPongInterface>(PingPongInterface.class, pingserver, addr);

            skeleton.start();
            logger.log(Level.INFO, "started ping server at: " + addr + ":" + addr.getPort());
            return pingserver;
        } catch(RMIException e) {
            logger.log(Level.WARNING, "unable to start skeleton, " + e);
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String args[])
    {
        PingPongServer pingserver = null;
        try
        {
            PingServerFactory obj = new PingServerFactory();
            logger.log(Level.INFO, "PingServerFactory started");

            //TODO: makePingServer() call should be made by client using rmi.registry
            pingserver = obj.makePingServer();

            if (pingserver != null)
                logger.log(Level.INFO, "started ping server");
            else
                logger.log(Level.WARNING, "failed to start ping server");

        }
        catch (Exception e)
        {
            logger.log(Level.WARNING, "PingServerFactory err: " + e.getMessage());
            e.printStackTrace();
        }
        return;
    }
}
