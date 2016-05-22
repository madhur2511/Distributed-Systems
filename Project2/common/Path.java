package common;

import java.io.*;
import java.util.*;

/** Distributed filesystem paths.

    <p>
    Objects of type <code>Path</code> are used by all filesystem interfaces.
    Path objects are immutable.

    <p>
    The string representation of paths is a forward-slash-delimeted sequence of
    path components. The root directory is represented as a single forward
    slash.

    <p>
    The colon (<code>:</code>) and forward slash (<code>/</code>) characters are
    not permitted within path components. The forward slash is the delimeter,
    and the colon is reserved as a delimeter for application use.
 */
public class Path implements Iterable<String>, Comparable<Path>, Serializable
{
    // local Variable to store absolute file path in Distributed File System
    private String pathString = "";

    /** Creates a new path which represents the root directory. */
    public Path()
    {
        this.pathString = new String("/");
    }

    /** Creates a new path by appending the given component to an existing path.

        @param path The existing path.
        @param component The new component.
        @throws IllegalArgumentException If <code>component</code> includes the
                                         separator, a colon, or
                                         <code>component</code> is the empty
                                         string.
    */
    public Path(Path path, String component)
    {
        // Basic checks
        if(component == null
        || component.isEmpty()
        || component.indexOf(':') != -1
        || component.indexOf('/') != -1){
            throw new IllegalArgumentException("Component has Illegal characters or is Empty");
        }
        if(path == null || !path.pathString.startsWith("/")){
            throw new IllegalArgumentException("Path doesnt start with forward-slash or is null");
        }

        if (path.isRoot())
            this.pathString = path.pathString + component;
        else
            this.pathString = path.pathString + "/" + component;
    }

    /** Creates a new path from a path string.

        <p>
        The string is a sequence of components delimited with forward slashes.
        Empty components are dropped. The string must begin with a forward
        slash.

        @param path The path string.
        @throws IllegalArgumentException If the path string does not begin with
                                         a forward slash, or if the path
                                         contains a colon character.
     */
    public Path(String path)
    {
        if(!path.startsWith("/") || path.indexOf(':') != -1){
            throw new IllegalArgumentException("Path doesnt start with forward-slash or has a colon");
        }
        path = path.replaceAll("/+", "/");
        if(path.length() > 1 && path.charAt(path.length() - 1) == '/')
            path = path.substring(0, path.length() - 1);

        this.pathString = new String(path);
    }

    /** Returns an iterator over the components of the path.

        <p>
        The iterator cannot be used to modify the path object - the
        <code>remove</code> method is not supported.

        @return The iterator.
     */
    @Override
    public Iterator<String> iterator()
    {
        String[] splits = this.pathString.split("/");

        //ArrayList<String> temp = new ArrayList<String>();
        String[] temp = new String[splits.length-1];

        int j = 0;
        for(int i = 0; i < splits.length; ++i){
            if(!splits[i].isEmpty())
                temp[j++] = splits[i];
        }
        return Arrays.asList(temp).iterator();
    }

    /** Lists the paths of all files in a directory tree on the local
        filesystem.

        @param directory The root directory of the directory tree.
        @return An array of relative paths, one for each file in the directory
                tree.
        @throws FileNotFoundException If the root directory does not exist.
        @throws IllegalArgumentException If <code>directory</code> exists but
                                         does not refer to a directory.
     */
    public static Path[] list(File directory) throws FileNotFoundException
    {
        if(directory == null || !directory.exists())
            throw new FileNotFoundException("File doesnt exist");
        if(directory == null || !directory.isDirectory())
            throw new IllegalArgumentException("Directory is either null or not a directory");

        ArrayList<String> pathStrings = getAllFilesFromDirectory(directory);
        Path[] returnPaths = new Path[pathStrings.size()];

        for(int i = 0; i < pathStrings.size(); ++i)
            returnPaths[i] = new Path(pathStrings.get(i));
        return returnPaths;
    }

    // Method to generate all list strings of file Path from given Directory
    public static ArrayList<String> getAllFilesFromDirectory(File directory){
        ArrayList<String> result = new ArrayList<String>();
        File[] files = directory.listFiles();
        for(int i = 0; i < files.length; ++i){
            if(files[i].isDirectory()){
                ArrayList<String> temp = getAllFilesFromDirectory(files[i]);
                for (int j = 0; j < temp.size(); ++j){
                    result.add("/" + files[i].getName() + temp.get(j));
                }
            }else
                result.add("/" + files[i].getName());
        }
        return result;
   }

    /** Determines whether the path represents the root directory.

        @return <code>true</code> if the path does represent the root directory,
                and <code>false</code> if it does not.
     */
    public boolean isRoot()
    {
        return this.pathString.equals("/");
    }

    /** Returns the path to the parent of this path.

        @throws IllegalArgumentException If the path represents the root
                                         directory, and therefore has no parent.
     */
    public Path parent()
    {
        if(this.isRoot())
           throw new IllegalArgumentException("Can't find parent of root directory");
        int lastSlashIndex = this.pathString.lastIndexOf("/");
        if(lastSlashIndex == 0)
          return new Path("/");
        else
          return new Path(this.pathString.substring(0, this.pathString.lastIndexOf("/")));
    }

    /** Returns the last component in the path.

        @throws IllegalArgumentException If the path represents the root
                                         directory, and therefore has no last
                                         component.
     */
    public String last()
    {
        if(this.isRoot())
           throw new IllegalArgumentException("Can't find last component of root directory");
        return this.pathString.substring(this.pathString.lastIndexOf("/") + 1, this.pathString.length());
    }

    /** Determines if the given path is a subpath of this path.

        <p>
        The other path is a subpath of this path if it is a prefix of this path.
        Note that by this definition, each path is a subpath of itself.

        @param other The path to be tested.
        @return <code>true</code> If and only if the other path is a subpath of
                this path.
     */
    public boolean isSubpath(Path other)
    {
        return this.pathString.indexOf(other.pathString) == 0;
    }

    /** Converts the path to <code>File</code> object.

        @param root The resulting <code>File</code> object is created relative
                    to this directory.
        @return The <code>File</code> object.
     */
    public File toFile(File root)
    {
        if (root != null)
            return new File(root, this.toString());
        else
            return new File(this.toString());
    }

    /** Compares this path to another.

        <p>
        An ordering upon <code>Path</code> objects is provided to prevent
        deadlocks between applications that need to lock multiple filesystem
        objects simultaneously. By convention, paths that need to be locked
        simultaneously are locked in increasing order.

        <p>
        Because locking a path requires locking every component along the path,
        the order is not arbitrary. For example, suppose the paths were ordered
        first by length, so that <code>/etc</code> precedes
        <code>/bin/cat</code>, which precedes <code>/etc/dfs/conf.txt</code>.

        <p>
        Now, suppose two users are running two applications, such as two
        instances of <code>cp</code>. One needs to work with <code>/etc</code>
        and <code>/bin/cat</code>, and the other with <code>/bin/cat</code> and
        <code>/etc/dfs/conf.txt</code>.

        <p>
        Then, if both applications follow the convention and lock paths in
        increasing order, the following situation can occur: the first
        application locks <code>/etc</code>. The second application locks
        <code>/bin/cat</code>. The first application tries to lock
        <code>/bin/cat</code> also, but gets blocked because the second
        application holds the lock. Now, the second application tries to lock
        <code>/etc/dfs/conf.txt</code>, and also gets blocked, because it would
        need to acquire the lock for <code>/etc</code> to do so. The two
        applications are now deadlocked.

        @param other The other path.
        @return Zero if the two paths are equal, a negative number if this path
                precedes the other path, or a positive number if this path
                follows the other path.
     */
    @Override
    public int compareTo(Path other)
    {
        throw new UnsupportedOperationException("not implemented");
    }

    /** Compares two paths for equality.

        <p>
        Two paths are equal if they share all the same components.

        @param other The other path.
        @return <code>true</code> if and only if the two paths are equal.
     */
    @Override
    public boolean equals(Object other)
    {
        return this.pathString.equals(((Path)other).pathString);
    }

    /** Returns the hash code of the path. */
    @Override
    public int hashCode()
    {
        return this.pathString.hashCode();
    }

    /** Converts the path to a string.

        <p>
        The string may later be used as an argument to the
        <code>Path(String)</code> constructor.

        @return The string representation of the path.
     */
    @Override
    public String toString()
    {
        return this.pathString;
    }
}
