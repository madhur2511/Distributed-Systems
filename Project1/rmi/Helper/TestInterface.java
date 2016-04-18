package rmi;
import java.net.*;
import java.lang.*;
import rmi.RMIException;

public interface TestInterface{
    public String testMessage(String part1) throws RMIException;
    public Integer addNum(Integer a, Integer b) throws RMIException;
    public String exceptionThrower(String msg) throws RMIException;
}
