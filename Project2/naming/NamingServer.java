package naming;

import java.io.*;
import java.net.*;
import java.util.*;

import rmi.*;
import common.*;
import storage.*;

/** Naming server.

    <p>
    Each instance of the filesystem is centered on a single naming server. The
    naming server maintains the filesystem directory tree. It does not store any
    file data - this is done by separate storage servers. The primary purpose of
    the naming server is to map each file name (path) to the storage server
    which hosts the file's contents.

    <p>
    The naming server provides two interfaces, <code>Service</code> and
    <code>Registration</code>, which are accessible through RMI. Storage servers
    use the <code>Registration</code> interface to inform the naming server of
    their existence. Clients use the <code>Service</code> interface to perform
    most filesystem operations. The documentation accompanying these interfaces
    provides details on the methods supported.

    <p>
    Stubs for accessing the naming server must typically be created by directly
    specifying the remote network address. To make this possible, the client and
    registration interfaces are available at well-known ports defined in
    <code>NamingStubs</code>.
 */
public class NamingServer implements Service, Registration
{
    private ServiceSkeleton svcSkeleton;
    private RegistrationSkeleton regSkeleton;
    private volatile boolean svcStopped;
    private volatile boolean regStopped;
    private ArrayList<Storage> storageStubs;
    private ArrayList<Command> commandStubs;
    private HashMap<Path, ArrayList<Storage>> fileTree = null;
    private HashMap<Path, ArrayList<String>> directoryTree = null;


    // Override neccessary skeleton methods for service server.
    private class ServiceSkeleton extends Skeleton<Service> {
        public ServiceSkeleton(Service s) {
            super(Service.class, s, new InetSocketAddress(NamingStubs.SERVICE_PORT));
        }

        @Override
        protected void stopped(Throwable e) {
            synchronized (this) {
                NamingServer.this.svcStopped = true;
                if (NamingServer.this.regStopped = true) {
                    NamingServer.this.stopped(null);
                }
            }
        }
    }

    // Override neccessary skeleton methods for registration server.
    private class RegistrationSkeleton extends Skeleton<Registration> {
        public RegistrationSkeleton(Registration s) {
            super(Registration.class, s, new InetSocketAddress(NamingStubs.REGISTRATION_PORT));
        }
        @Override
        protected void stopped(Throwable e) {
            synchronized (this) {
                NamingServer.this.regStopped = true;
                if (NamingServer.this.svcStopped = true) {
                    NamingServer.this.stopped(null);
                }
            }
        }
    }

    /** Creates the naming server object.

        <p>
        The naming server is not started.
     */
    public NamingServer()
    {
        this.svcSkeleton = new ServiceSkeleton(this);
        this.regSkeleton = new RegistrationSkeleton(this);
        this.fileTree = new HashMap<Path, ArrayList<Storage>>();
        this.directoryTree = new HashMap<Path, ArrayList<String>>();
        this.storageStubs = new ArrayList<Storage>();
        this.commandStubs = new ArrayList<Command>();
    }

    /** Starts the naming server.

        <p>
        After this method is called, it is possible to access the client and
        registration interfaces of the naming server remotely.

        @throws RMIException If either of the two skeletons, for the client or
                             registration server interfaces, could not be
                             started. The user should not attempt to start the
                             server again if an exception occurs.
     */
    public synchronized void start() throws RMIException
    {
        try {
            this.svcSkeleton.start();
            this.regSkeleton.start();
        } catch (Exception e) {
            throw new RMIException(e);
        }
    }

    /** Stops the naming server.

        <p>
        This method commands both the client and registration interface
        skeletons to stop. It attempts to interrupt as many of the threads that
        are executing naming server code as possible. After this method is
        called, the naming server is no longer accessible remotely. The naming
        server should not be restarted.
     */
    public void stop()
    {
        svcSkeleton.stop();
        regSkeleton.stop();
    }

    /** Indicates that the server has completely shut down.

        <p>
        This method should be overridden for error reporting and application
        exit purposes. The default implementation does nothing.

        @param cause The cause for the shutdown, or <code>null</code> if the
                     shutdown was by explicit user request.
     */
    protected void stopped(Throwable cause)
    {
    }

    // The following public methods are documented in Service.java.
    @Override
    public void lock(Path path, boolean exclusive) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void unlock(Path path, boolean exclusive)
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isDirectory(Path path) throws FileNotFoundException
    {
        if(path == null)
            throw new NullPointerException("Path cannot be null");

        if(!this.fileTree.containsKey(path) && !this.directoryTree.containsKey(path))
            throw new FileNotFoundException("No such directory path exists");

        return this.directoryTree.containsKey(path) ? true : false;
    }

    @Override
    public String[] list(Path directory) throws FileNotFoundException
    {
        if(directory == null)
            throw new NullPointerException("Directory path cannot be null");

        if(!this.directoryTree.containsKey(directory))
            throw new FileNotFoundException("No such directory exists");

        ArrayList<String> ls = this.directoryTree.get(directory);
        return ls.toArray(new String[ls.size()]);
    }

    @Override
    public boolean createFile(Path file)
        throws RMIException, FileNotFoundException
    {
        if(file == null)
            throw new NullPointerException("File path cannot be null");
        if(file.isRoot())
            return false;
        this.isDirectory(file.parent());
        if(this.fileTree.containsKey(file.parent()))
            throw new FileNotFoundException("Cant add directory on a file");

        if(this.directoryTree.containsKey(file) || this.fileTree.containsKey(file))
            return false;

        this.fileTree.put(file, new ArrayList<Storage>());
        int randIndex = chooseRandomIndex(this.storageStubs);
        if(commandStubs.get(randIndex).create(file))
            this.fileTree.get(file).add(storageStubs.get(randIndex));
        else{
            if(this.fileTree.get(file) != null)
                this.fileTree.remove(file);
            return false;
        }
        return true;
    }

    private int chooseRandomIndex(ArrayList<Storage> list) throws IllegalStateException{
        Random rand = new Random();
        if(list.isEmpty())
            throw new IllegalStateException("No storage servers registered with naming server");
        return rand.nextInt(list.size());
    }

    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {
        if(directory == null)
            throw new NullPointerException("Directory path cannot be null");
        if(directory.isRoot())
            return false;
        this.isDirectory(directory.parent());
        if(this.fileTree.containsKey(directory.parent()))
            throw new FileNotFoundException("Cant add directory on a file");

        if(this.directoryTree.containsKey(directory) || this.fileTree.containsKey(directory))
            return false;
        else
            this.directoryTree.put(directory, new ArrayList<String>());
        return true;
    }

    private Storage getRandomServer(){
        Random rand = new Random();
        System.out.print(rand.nextInt(this.storageStubs.size()));
        return null;
    }

    @Override
    public boolean delete(Path path) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Storage getStorage(Path file) throws FileNotFoundException
    {
        if(file == null)
            throw new NullPointerException("file path cannot be null");
        if(!this.fileTree.containsKey(file))
            throw new FileNotFoundException("No such file exists");
        int randIndex = chooseRandomIndex(this.fileTree.get(file));
        return this.fileTree.get(file).get(randIndex);
    }

    // The method register is documented in Registration.java.
    @Override
    public Path[] register(Storage client_stub, Command command_stub,
                           Path[] files)
    {
        if (client_stub == null || command_stub == null || files == null)
            throw new NullPointerException("Missing stubs or files list");
        if (storageStubs.contains(client_stub))
            throw new IllegalStateException("Duplicate storage stub");

        storageStubs.add(client_stub);
        commandStubs.add(command_stub);

        ArrayList<Path> deletionPaths = new ArrayList<Path>();
        for(Path path : files){
            if(path.isRoot())
                continue;
            if(this.fileTree.containsKey(path) || this.directoryTree.containsKey(path))
                deletionPaths.add(path);
            else
                updateFSTrees(path, client_stub);
        }
        // printFileSystem();
        return deletionPaths.toArray(new Path[deletionPaths.size()]);
    }


    private void updateFSTrees(Path path, Storage client_stub){
        if (this.fileTree.get(path) == null)
            this.fileTree.put(path, new ArrayList<Storage>());
        this.fileTree.get(path).add(client_stub);
        String lastPath = "";

        Path temp = new Path(path.toString());
        while(!temp.isRoot()){
            lastPath = temp.last();
            temp = temp.parent();

            if (this.directoryTree.get(temp) == null)
                this.directoryTree.put(temp, new ArrayList<String>());

            ArrayList<String> currentList = this.directoryTree.get(temp);
            if(!currentList.contains(lastPath))
                this.directoryTree.get(temp).add(lastPath);
        }
    }

    private void printFileSystem(){
        System.out.println("******************************");

        System.out.println();
        for(Path k : this.fileTree.keySet()){
            System.out.println(this.directoryTree.containsKey(k));
            System.out.println(k.toString());
        }

        System.out.println();

        for(Path k : this.directoryTree.keySet()){
            System.out.println(this.directoryTree.containsKey(k));
            System.out.println(k.toString());
        }
        System.out.println("******************************");

        System.out.println();
        System.out.println();
        System.out.println();
    }

}
