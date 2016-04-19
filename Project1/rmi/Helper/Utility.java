package rmi.Helper;
import rmi.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.logging.*;

// Utility helper functions
public class Utility
{
    public static final boolean INVOKE_SUCCESS = true;
    public static final boolean INVOKE_FAILURE = false;
    public static final int MAX_WAIT = 30;        // total timeout = 30 * 2000 = 60secs
    public static final int SLEEPTIME = 2000;     // 2 secs

    public static boolean isValidRemoteMethod(Method meth) {
        boolean res = false;
        for (Class exceptionClass : meth.getExceptionTypes())
            if (exceptionClass == RMIException.class)
                res = true;

        return res;
    }

    public static boolean isValidRemoteInterface(Class c) {
        Method[] methods = c.getMethods();
        int count = 0;
        for (Method method : methods)
            if (isValidRemoteMethod(method))
                count += 1;

        return count == methods.length ? true : false;
    }
}
