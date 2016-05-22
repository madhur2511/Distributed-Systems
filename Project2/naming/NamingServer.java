package naming;

import rmi.*;
import java.io.*;
import java.net.*;
import java.util.*;
import common.*;
import naming.*;
import storage.*;
import java.util.concurrent.*;

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
    private HashMap<Path, DfsObject> dfsTree = null;


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
        this.storageStubs = new ArrayList<Storage>();
        this.commandStubs = new ArrayList<Command>();
        this.regSkeleton = new RegistrationSkeleton(this);

        this.dfsTree = new HashMap<Path, DfsObject>();
        DfsObject root = new DfsObject(new Path("/"), Ftype.DIRECTORY, Ltype.NOT_LOCKED);
        this.dfsTree.put(new Path("/"), root);
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

    private void setLockTypeRecursively(Path path, Ltype lockType) throws InterruptedException{
        Path temp = new Path(path.toString());
        if(!temp.isRoot())
            setLockTypeRecursively(path.parent(), lockType);
        try{
            if(lockType == Ltype.NOT_LOCKED)
                dfsTree.get(temp).releaseReadLock();
            else
                dfsTree.get(temp).requestReadLock();
        }catch(Exception e){}
    }

    // The following public methods are documented in Service.java.
    @Override
    public void lock(Path path, boolean exclusive) throws FileNotFoundException
    {
        if(path == null)
            throw new NullPointerException("Path cannot be null");
        if(!dfsTree.containsKey(path))
            throw new FileNotFoundException("File or directory doesn't exist!");
        try{
            if(!path.isRoot())
                setLockTypeRecursively(path.parent(), Ltype.SHARED_LOCK);
            if(!exclusive){
                dfsTree.get(path).requestReadLock();
                if(dfsTree.get(path).getTotalReadCount() % 20 == 0)
                    replicate(path);
            }
            else{
                dfsTree.get(path).requestWriteLock();
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    public void replicate(Path path){
        ArrayList<Command> diffCommandServers = new ArrayList<Command>(commandStubs);
        diffCommandServers.removeAll(dfsTree.get(path).getCommand());

        if(!diffCommandServers.isEmpty()){
            Command diffCommandServer = diffCommandServers.get(new Random().nextInt(diffCommandServers.size()));
            try{
                DfsObject dfsNode = dfsTree.get(path);
                Storage existingStorageServer = dfsNode.getStorage().get(0);
                Storage diffStorageServer = storageStubs.get(commandStubs.indexOf(diffCommandServer));

                if(diffCommandServer.copy(path, existingStorageServer)){
                    dfsNode.addCommand(diffCommandServer);
                    dfsNode.addServer(diffStorageServer);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unlock(Path path, boolean exclusive)
    {
        if(path == null)
            throw new NullPointerException("Path cannot be null");
        if (!dfsTree.containsKey(path))
            throw new IllegalArgumentException("File or directory doesn't exist!");
        try{
            if(!exclusive)
                dfsTree.get(path).releaseReadLock();
            else
                dfsTree.get(path).releaseWriteLock();
            if(!path.isRoot())
                setLockTypeRecursively(path.parent(), Ltype.NOT_LOCKED);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean isDirectory(Path path) throws FileNotFoundException
    {
        synchronized(dfsTree){
            if(path == null)
                throw new NullPointerException("Path cannot be null");

            if (!dfsTree.containsKey(path))
                throw new FileNotFoundException("File or directory doesn't exist!");

            // TODO: Take share_lock on directory before getting listing.
            return dfsTree.get(path).isDirectory();
        }
    }

    @Override
    public String[] list(Path directory) throws FileNotFoundException
    {
        synchronized(dfsTree){
            if(directory == null)
                throw new NullPointerException("Directory path cannot be null");

            if(!dfsTree.containsKey(directory) || (dfsTree.get(directory).isFile()))
                throw new FileNotFoundException("No such directory exists");

            // TODO: Take share_lock on directory before getting listing.
            return dfsTree.get(directory).getList();
        }
    }

    @Override
    public boolean createFile(Path file)
        throws RMIException, FileNotFoundException
    {
        // TODO: Take share_lock on directory before getting listing.
        synchronized(dfsTree){
            if(file == null)
                throw new NullPointerException("File path cannot be null");
            if(file.isRoot())
                return false;

            if (!this.isDirectory(file.parent())) {
                throw new FileNotFoundException("Parent is not a directory");
            }

            if (dfsTree.containsKey(file)) {
                return false;
            } else {
                DfsObject obj = new DfsObject(file, Ftype.FILE, Ltype.NOT_LOCKED);

                int randIndex = chooseRandomIndex(this.storageStubs);
                if(commandStubs.get(randIndex).create(file)) {
                    obj.addServer(storageStubs.get(randIndex));
                    obj.addCommand(commandStubs.get(randIndex));
                } else {
                    return false;
                }

                dfsTree.put(file, obj);
                // TODO: lock the parent before adding the file to its list?
                dfsTree.get(file.parent()).addFile(file.last());
            }
            return true;
        }
    }

    public int chooseRandomIndex(ArrayList<Storage> list) throws IllegalStateException{
        Random rand = new Random();
        if(list.isEmpty())
            throw new IllegalStateException("No storage servers registered with naming server");
        return rand.nextInt(list.size());
    }

    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {
        // TODO: Take share_lock on directory before getting listing.
        synchronized(dfsTree){
            if(directory == null)
                throw new NullPointerException("Directory path cannot be null");

            if(directory.isRoot())
                return false;

            if (!this.isDirectory(directory.parent())) {
                throw new FileNotFoundException("Parent is not a directory");
            }

            if (dfsTree.containsKey(directory)) {
                return false;
            } else {
                DfsObject obj = new DfsObject(directory, Ftype.DIRECTORY, Ltype.NOT_LOCKED);
                dfsTree.put(directory, obj);
                // TODO: lock the parent before adding the directory to its list?
                dfsTree.get(directory.parent()).addFile(directory.last());
            }

            return true;
        }
    }

    @Override
    public boolean delete(Path path) throws RMIException, FileNotFoundException
    {
        synchronized (dfsTree) {
            if (path == null)
                throw new NullPointerException("Path cannot be null");

            if (path.isRoot()) {
                return false;
            }

            if (!dfsTree.containsKey(path))
                throw new FileNotFoundException("No such file exists");

            // TODO: Do we need to lock this object for exclusive access??

            DfsObject obj = dfsTree.get(path);
            ArrayList<Command> commands = null;
            if (obj.isFile())
                commands = obj.getCommand();
            else
                commands = this.commandStubs;

            for (Command cmd : commands) {
                // some of the servers may not have directory and hence
                // could return false on delete. Ignore them.
                if (!cmd.delete(path) && !obj.isDirectory())
                    return false;
            }

            dfsTree.remove(path);
            return true;
        }
    }

    @Override
    public Storage getStorage(Path file) throws FileNotFoundException
    {
        synchronized(dfsTree){
            if(file == null)
                throw new NullPointerException("file path cannot be null");

            if(!dfsTree.containsKey(file))
                throw new FileNotFoundException("No such file exists");

            DfsObject obj = dfsTree.get(file);
            if (obj.isDirectory())
                throw new FileNotFoundException("Directort with same name exists");

            ArrayList<Storage> servers = obj.getStorage();
            int randIndex = this.chooseRandomIndex(servers);
            return servers.get(randIndex);
        }
    }

    // The method register is documented in Registration.java.
    @Override
    public Path[] register(Storage client_stub, Command command_stub,
                           Path[] files)
    {
        synchronized(dfsTree){
            if (client_stub == null || command_stub == null || files == null)
                throw new NullPointerException("Missing stubs or files list");
            if (storageStubs.contains(client_stub))
                throw new IllegalStateException("Duplicate storage stub");

            storageStubs.add(client_stub);
            commandStubs.add(command_stub);

            ArrayList<Path> deletionPaths = new ArrayList<Path>();
            DfsObject obj = null;
            Path temp  = null;

            for(Path path : files){
                if(path.isRoot())
                    continue;

                // first check if the file can be added successfully.
                if (dfsTree.containsKey(path)) {
                    deletionPaths.add(path);
                } else {
                    // check if one of the parent exists as a file
                    obj = null;
                    temp = new Path(path.toString());
                    while(!temp.isRoot()){
                        temp = temp.parent();
                        obj = dfsTree.get(temp);
                        if (obj != null && obj.isFile())
                            deletionPaths.add(path);
                    }
                }
                if (deletionPaths.contains(path))
                    continue;
                updateFSTrees(path, client_stub, command_stub);
            }
            return deletionPaths.toArray(new Path[deletionPaths.size()]);
        }
    }

    private void updateFSTrees(Path path, Storage client_stub, Command command_stub){
        synchronized(dfsTree){
            DfsObject obj = dfsTree.get(path);
            if (obj == null) {
                obj = new DfsObject(path, Ftype.FILE, Ltype.NOT_LOCKED);
                dfsTree.put(path, obj);
                obj.addServer(client_stub);
                obj.addCommand(command_stub);
            } else {
                // XXX: We should never land up here
                System.out.println("something fishy going on here!!");
            }

            String lastPath = "";
            Path temp = new Path(path.toString());
            obj = null;

            while(!temp.isRoot()){
                lastPath = temp.last();
                temp = temp.parent();
                obj = dfsTree.get(temp);

                if (obj == null) {
                    obj = new DfsObject(temp, Ftype.DIRECTORY, Ltype.NOT_LOCKED);
                    dfsTree.put(temp, obj);
                } else if (obj.isFile()) {
                    // XXX: We should never land up here
                    System.out.println("something fishy going on here!!");
                }

                // TODO: share lock/write lock the directory before reading its listings.
                if (!obj.containsFile(lastPath))
                    obj.addFile(lastPath);
            }
        }
    }

    private void printFileSystem(){
        synchronized(dfsTree){
            for(Path k : dfsTree.keySet()){
                System.out.println("\n***** File Path *****");
                System.out.println(k.toString());
                dfsTree.get(k).print();
            }
        }
    }
}
