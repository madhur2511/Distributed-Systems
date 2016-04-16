package rmi;
import java.net.*;
import rmi.RMIException;

public interface TestInterface{
    public String testMessage(String part1) throws RMIException;
}
