package rmi;
import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.logging.*;

public class PingPongServer implements PingPongInterface{
    public InetSocketAddress addr = null;

    public PingPongServer(InetSocketAddress addr) {
        this.addr = addr;
    }

    public String ping(int id) throws RMIException{
        return "Pong, ID: " + id;
    }
}
