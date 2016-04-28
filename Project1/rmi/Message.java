package rmi;
import java.net.*;
import java.io.*;
import rmi.*;

public class Message implements Serializable{
    private String methodName = null;
    private Object[] args = null;
    private Class[] argTypes = null;

    public void setMethodName(String methodName){
        this.methodName = methodName;
    }
    public void setArgs(Object[] args){
        this.args = args;
    }
    public void setArgTypes(Class[] argTypes){
        this.argTypes = argTypes;
    }
    public String getMethodName(){
        return this.methodName;
    }
    public Object[] getArgs(){
        return this.args;
    }
    public Class[] getArgTypes(){
        return this.argTypes;
    }
}
