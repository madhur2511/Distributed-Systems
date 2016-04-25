package rmi;

import java.net.*;

public interface PingServerFactoryInterface {
    public Object makePingServer() throws RMIException;
}
