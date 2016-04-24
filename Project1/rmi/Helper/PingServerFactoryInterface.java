package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PingServerFactoryInterface extends Remote {
    public PingPongServer makePingServer() throws RemoteException, RMIException;
}
