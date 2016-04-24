package rmi;
import java.net.*;
import java.lang.*;
import rmi.RMIException;

public interface PingPongInterface{
    public String ping(int id) throws RMIException;
}
