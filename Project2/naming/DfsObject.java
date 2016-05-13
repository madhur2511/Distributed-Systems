package naming;
import storage.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class DfsObject {
    public Ftype ftype;
    public Ltype ltype;
    private int currentReaders = 0;
    private int currentWriters = 0;
    private int writersPending = 0;
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
        if(ftype == Ftype.DIRECTORY)
            for(String file : ls)
                System.out.println(file);

        System.out.println("\n***** File -> Storage servers *****");
        if(ftype == Ftype.FILE)
            for(Storage server : servers)
                System.out.println(server);

        System.out.println("\n***** Locks *****");
        System.out.println("Current Readers: " + currentReaders);
        System.out.println("Current Writers: " + currentWriters);
        System.out.println("Pending Writers: " + writersPending);

        System.out.println("\n");
    }

    // LOCKING MECHANISM

    public synchronized void requestReadLock() throws InterruptedException {
        while (!canAllowRead())
            wait();
        currentReaders += 1;
    }

    private boolean canAllowRead() {
        if (hasCurrentWriters() || hasPendingWriters())
            return false;
        return true;
    }

    public synchronized void releaseReadLock() {
        currentReaders -= 1;
        notifyAll();
    }

    public synchronized void requestWriteLock() throws InterruptedException {
        writersPending += 1;
        while (!canAllowWrite()) {
            wait();
        }
        writersPending -= 1;
        currentWriters += 1;
    }

    public synchronized void releaseWriteLock() throws InterruptedException {
        currentWriters -= 1;
        notifyAll();
    }

    private boolean canAllowWrite() {
        if (hasCurrentReaders() || hasCurrentWriters())
            return false;
        return true;
    }

    private boolean hasCurrentReaders() {
        return this.currentReaders > 0;
    }

    private boolean hasCurrentWriters() {
        return this.currentWriters > 0;
    }

    private boolean hasPendingWriters() {
        return this.writersPending > 0;
    }

}
