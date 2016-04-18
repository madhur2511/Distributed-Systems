package rmi.Helper;
import java.net.*;
import java.io.*;
import rmi.*;
import rmi.Helper.*;

public class Message implements Serializable{
    private String methodName = null;
    private Object[] args = null;

    public void setMethodName(String methodName){
        this.methodName = methodName;
    }
    public void setArgs(Object[] args){
        this.args = args;
    }
    public String getMethodName(){
        return this.methodName;
    }
    public Object[] getArgs(){
        return this.args;
    }
}
