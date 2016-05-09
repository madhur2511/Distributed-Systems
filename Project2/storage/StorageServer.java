package storage;

import java.io.*;
import java.net.*;

import common.*;
import rmi.*;
import naming.*;

/** Storage server.

    <p>
    Storage servers respond to client file access requests. The files accessible
    through a storage server are those accessible under a given directory of the
    local filesystem.
 */
public class StorageServer implements Storage, Command
{
    private File localRoot;
    private int clientPort;
    private int commandPort;
    private Skeleton storageSkeleton;
    private Skeleton commandSkeleton;
    private Registration naming_server;
    private String hostname;

    /** Creates a storage server, given a directory on the local filesystem, and
        ports to use for the client and command interfaces.

        <p>
        The ports may have to be specified if the storage server is running
        behind a firewall, and specific ports are open.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @param client_port Port to use for the client interface, or zero if the
                           system should decide the port.
        @param command_port Port to use for the command interface, or zero if
                            the system should decide the port.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
    */
    public StorageServer(File root, int client_port, int command_port)
    {
        if (root == null)
            throw new NullPointerException("Root is null");

        if (client_port == 0)
            client_port = 12000;
        if (command_port == 0)
            command_port = 11000;

        this.localRoot = root;
        this.storageSkeleton = new Skeleton(Storage.class,this,new InetSocketAddress(client_port));
        this.commandSkeleton = new Skeleton(Command.class,this,new InetSocketAddress(command_port));
    }

    /** Creats a storage server, given a directory on the local filesystem.

        <p>
        This constructor is equivalent to
        <code>StorageServer(root, 0, 0)</code>. The system picks the ports on
        which the interfaces are made available.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
     */
    public StorageServer(File root)
    {
        if (root == null)
            throw new NullPointerException("Root is null");

        this.localRoot = root;
        storageSkeleton = new Skeleton(Storage.class,this,new InetSocketAddress(12000));
        commandSkeleton = new Skeleton(Command.class,this,new InetSocketAddress(11000));

    }

    private synchronized void deleteEmptyDirs(File dir) {
        if (dir.isDirectory() == false)
            return;

        if (dir.list().length > 0) {
            for (File s : dir.listFiles()) {
                deleteEmptyDirs(s);
            }
        }

        if (dir.list().length == 0)
            dir.delete();
    }

    /** Starts the storage server and registers it with the given naming
        server.

        @param hostname The externally-routable hostname of the local host on
                        which the storage server is running. This is used to
                        ensure that the stub which is provided to the naming
                        server by the <code>start</code> method carries the
                        externally visible hostname or address of this storage
                        server.
        @param naming_server Remote interface for the naming server with which
                             the storage server is to register.
        @throws UnknownHostException If a stub cannot be created for the storage
                                     server because a valid address has not been
                                     assigned.
        @throws FileNotFoundException If the directory with which the server was
                                      created does not exist or is in fact a
                                      file.
        @throws RMIException If the storage server cannot be started, or if it
                             cannot be registered.
     */
    public synchronized void start(String hostname, Registration naming_server)
        throws RMIException, UnknownHostException, FileNotFoundException
    {
        // TODO if (!hostname) throw UnknownHostException
        if (hostname == null)
            throw new UnknownHostException("Hostname given is null");

        if (localRoot.exists() == false || localRoot.isDirectory() == false)
            throw new FileNotFoundException("Either directory doesn't exists or is a file");

        this.hostname = hostname;
        this.naming_server = naming_server;

        try {
            commandSkeleton.start();
            storageSkeleton.start();

            Path[] delFiles = naming_server.register(
                    Stub.create(Storage.class, storageSkeleton, hostname),
                    Stub.create(Command.class, commandSkeleton, hostname),
                    Path.list(localRoot));

            for (int i = 0; i < delFiles.length; i++) {
                delete(delFiles[i]);
            }

            deleteEmptyDirs(localRoot);
        }
        catch (Exception e) {
            throw new RMIException(e);
        }
    }

    /** Stops the storage server.

        <p>
        The server should not be restarted.
     */
    public void stop()
    {
            commandSkeleton.stop();
            storageSkeleton.stop();
    }

    /** Called when the storage server has shut down.

        @param cause The cause for the shutdown, if any, or <code>null</code> if
                     the server was shut down by the user's request.
     */
    protected void stopped(Throwable cause)
    {
    }

    // The following methods are documented in Storage.java.
    @Override
    public synchronized long size(Path file) throws FileNotFoundException
    {
        if (file == null) {
            throw new NullPointerException("File path is null");
        }
        File f = file.toFile(localRoot);
        if (f.exists() == false || f.isDirectory() == true) {
            throw new FileNotFoundException("Either file does not exists or is a directory");
        }
        return f.length();
    }

    @Override
    public synchronized byte[] read(Path file, long offset, int length)
        throws FileNotFoundException, IOException
    {
        if (file == null) {
            throw new NullPointerException("File path is null");
        }

        File f = file.toFile(localRoot);
        if (f.exists() == false || f.isDirectory() == true) {
            throw new FileNotFoundException("Either file does not exists or s a directory");
        }

        if (offset < 0 || offset > Long.MAX_VALUE ||
            length < 0 || (offset + length) > f.length()) {
                throw new IndexOutOfBoundsException("Out of bound read");
        }
        if (length == 0) {
            return "".getBytes();
        }
        byte[] result = new byte[length];
        RandomAccessFile raf = new RandomAccessFile(f, "r");

        raf.seek(offset);
        raf.readFully(result);
        return result;
    }

    @Override
    public synchronized void write(Path file, long offset, byte[] data)
        throws FileNotFoundException, IOException
    {
        if (file == null) {
            throw new NullPointerException("File path is null");
        }

        File f = file.toFile(localRoot);
        if (f.exists() == false || f.isDirectory() == true) {
            throw new FileNotFoundException("Either file does not exists or is a directory");
        }

        if (data == null) {
            throw new NullPointerException("Null data");
        }
        if (offset < 0 || offset > Long.MAX_VALUE) {
                throw new IndexOutOfBoundsException("Out of bound write");
        }
        if (data.length == 0) {
            return;
        }

        RandomAccessFile raf = new RandomAccessFile(f, "rw");

        raf.seek(offset);
        raf.write(data);
    }

    // The following methods are documented in Command.java.
    @Override
    public synchronized boolean create(Path file)
    {
        if (file == null) {
            throw new NullPointerException("File path is null");
        }

        File f = file.toFile(localRoot);
        if (file.isRoot() || f.exists()) {
            return false;
        }

        if (!f.getParentFile().exists()) {
            if (!f.getParentFile().mkdirs()) {
                return false;
            }
        }
        try {
            return f.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public synchronized boolean delete(Path path)
    {
        if (path == null) {
            throw new NullPointerException("File path is null");
        }

        File temp = path.toFile(localRoot);
        if (path.isRoot() || !temp.exists()) {
            return false;
        }

        if (temp.isDirectory()) {
            for (String s : temp.list()) {
                if (delete(new Path(path, s)) == false)
                    return false;
            }
        }
        return temp.delete();
    }

    @Override
    public synchronized boolean copy(Path file, Storage server)
        throws RMIException, FileNotFoundException, IOException
    {
        throw new UnsupportedOperationException("not implemented");
    }
}
