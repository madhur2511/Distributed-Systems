package naming;
import storage.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class DfsObject {
    public Ftype ftype;
    public Ltype ltype;
    public ArrayList<Storage> servers = null;
    public ArrayList<String> ls = null;

    public DfsObject(Ftype type, Ltype ltype) {
        this.ftype = type;
        this.ltype = ltype;
        if (type == Ftype.FILE)
            this.servers = new ArrayList<Storage>();
        else
            this.ls = new ArrayList<String>();
    }

    public Ftype getType() {
        return this.ftype;
    }

    public boolean isFile() {
        return this.ftype == Ftype.FILE ? true : false;
    }

    public boolean isNotLocked() {
        return this.ltype == Ltype.NOT_LOCKED ? true : false;
    }

    public boolean isShareLocked() {
        return this.ltype == Ltype.SHARED_LOCK ? true : false;
    }

    public boolean isExclLocked() {
        return this.ltype == Ltype.EXCLUSIVE_LOCK ? true : false;
    }

    public void setLock(Ltype lockType) {
        this.ltype = lockType;
    }

    public boolean isDirectory() {
        return this.ftype == Ftype.DIRECTORY ? true : false;
    }

    public ArrayList<Storage> getStorage() {
        return this.servers;
    }

    public void addServer(Storage server) {
        this.servers.add(server);
    }

    public String[] getList() {
        return this.ls.toArray(new String[ls.size()]);
    }

    public boolean containsFile(String file) {
        return this.ls.contains(file) ? true : false;
    }

    public void addFile(String file) {
        this.ls.add(file);
    }

    public void print(){
        System.out.println("\n***** File Type *****");
        System.out.println(this.ftype);

        System.out.println("\n***** Lock Type *****");
        System.out.println(this.ltype);

        System.out.println("\n***** Directory -> Files *****");
        for(String file : ls)
            System.out.println(file);

        System.out.println("\n***** File -> Storage servers *****");
        for(Storage server : servers)
            System.out.println(server);

        System.out.println("\n");
    }
}
