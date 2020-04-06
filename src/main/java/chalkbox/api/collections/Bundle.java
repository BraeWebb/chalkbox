package chalkbox.api.collections;

import chalkbox.api.files.FileLoader;
import chalkbox.api.files.FileSourceFile;
import chalkbox.api.files.SourceFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

/**
 * Bundle of files and folders, abstracting away folders.
 */
public class Bundle {
    /* Name of files stored within this bundle */
    private List<String> files = new ArrayList<>();
    /* Root folder */
    private File folder;

    /**
     * Create a new temporary bundle.
     *
     * @throws IOException if a temporary bundle cannot be created
     */
    public Bundle() throws IOException {
        this.folder = Files.createTempDirectory("temp").toFile();
    }

    /**
     * Create a new bundle of an existing folder.
     *
     * @param folder an existing folder mocked by the bundle
     *
     * @throws NullPointerException if the Bundle folder does not exist
     */
    public Bundle(File folder) {
        if (!folder.exists()) {
            throw new NullPointerException();
        }
        this.folder = folder;
        this.files = FileLoader.loadFiles(folder.getPath());
    }

    /**
     * Return the list of files stored in this bundle.
     *
     * @return File names of files in this bundle
     */
    public List<String> getFileNames() {
        return new ArrayList<>(files);
    }

    /** Return the list of files matching a certain extension in this bundle.
     *
     * @param extension The file extension to search for
     * @return A list of file names relative to this bundle
     */
    public List<String> getFileNames(String extension) {
        List<String> filenames = new ArrayList<>();
        for (String filename : files) {
            if (filename.endsWith(extension)) {
                filenames.add(filename);
            }
        }
        return filenames;
    }

    /**
     * Create a bundle from a subdirectory in this bundle.
     *
     * @param path The path within this bundle to create a new bundle from
     * @return The new bundle
     */
    public Bundle getBundle(String path) {
        return new Bundle(new File(folder.getPath() + File.separator + path));
    }

    /**
     * Get the class names of the java source folders within this bundle.
     *
     * This works with the assumption that source files are either in the
     * root level, a src/ folder or a test/ folder.
     *
     * @param sourceRoot The root folder to search within.
     * @return A list of class names within this bundle.
     */
    public List<String> getClasses(String sourceRoot) {
        Bundle sources = getBundle(sourceRoot);
        List<String> classes = new ArrayList<>();
        for (String filename : sources.getFileNames(".java")) {
            classes.add(getClassName(filename));
        }
        return classes;
    }

    /**
     * Get the class name of a file path.
     *
     * <p>Removes the java extension and replaces paths with dots.
     *
     * <p>If it's within src/ or test/ those folders are removed.
     *
     * <p>Examples:
     * <pre>
     * src/package1/ClassOne.java -&gt; package1.ClassOne
     * package1/ClassOne.java -&gt; package1.ClassOne
     * test/package1/ClassOne.java -&gt; package1.ClassOne
     * src/package1/package2/ClassOne.java -&gt; package1.package2.ClassOne
     * src/ClassOne.java -&gt; ClassOne
     * </pre>
     * @param filePath File path of the class
     * @return The name of the class
     */
    public String getClassName(String filePath) {
        if (filePath.startsWith("/src/")) {
            filePath = filePath.replace("/src/", "");
        }
        if (filePath.startsWith("/test/")) {
            filePath = filePath.replace("/test/", "");
        }
        return filePath.replace(".java", "")
                .replace(File.separator, ".");
    }

    /**
     * Get the path of a class from it's class name.
     *
     * <p>Examples:
     * <pre>
     * package1.ClassOne -&gt; package1/ClassOne.java
     * ClassOne -&gt; ClassOne.java
     * </pre>
     *
     * @param className Name of the class
     * @return File path for a class
     */
    public String getPathName(String className) {
        return className.replace(".", File.separator) + ".java";
    }

    /**
     * Get all the source files in this bundle.
     *
     * @return The source files within this bundle.
     * @throws IOException If a source file cannot be loaded.
     */
    public SourceFile[] getFiles() throws IOException {
        return getFiles("");
    }

    /**
     * Get all the source files in this bundle with a given extension.
     *
     * @param extension The extension to search for.
     * @return The source files within this bundle.
     * @throws IOException If a source file cannot be loaded.
     */
    public SourceFile[] getFiles(String extension) throws IOException {
        List<String> filenames = getFileNames(extension);

        SourceFile[] sources = new SourceFile[filenames.size()];

        for (int i = 0; i < filenames.size(); i++) {
            sources[i] = getFile(filenames.get(i));
        }

        return sources;
    }

    /**
     * Get a source file based on the path of the file.
     *
     * @param uri The resource identifier (path) of a file.
     * @return The SourceFile for the given path.
     * @throws IOException If a source file cannot be loaded.
     */
    public SourceFile getFile(String uri) throws IOException {
        if (!files.contains(uri)) {
            throw new FileNotFoundException("Couldn't find file: " + uri + " in " + folder.getPath());
        }

        File file = new File(this.folder + File.separator + uri);
        return new FileSourceFile(uri, file);
    }

    /**
     * @return The actual folder path for the bundle.
     */
    public String getUnmaskedPath() {
        return this.folder.getPath();
    }

    /**
     * @return The actual folder path for a file in the bundle.
     */
    public String getUnmaskedPath(String uri) {
        return this.folder + File.separator + uri;
    }

    /**
     * @return The absolute path of the bundle.
     */
    public String getAbsolutePath() {
        return this.folder.getAbsolutePath();
    }

    /**
     * @return The absolute path of the bundle.
     */
    public String getAbsolutePath(String uri) {
        return this.folder.getAbsolutePath() + File.separator + uri;
    }

    /**
     * Make a directory within this bundle with a given path.
     *
     * @param uri The path relative to the bundle to create.
     * @return false if the folder couldn't be created or already existed.
     */
    public boolean makeDir(String uri) {
        if (files.contains(uri)) {
            return false;
        }

        File file = new File(getUnmaskedPath(uri));
        return file.mkdir();
    }

    /**
     * Make a directory within this bundle and return the given bundle.
     *
     * @param uri The path relative to the bundle to create.
     * @return the of the subdirectory.
     *
     * @throws IOException if a subdirectory could not be make or if the bundle
     *                      could not be loaded.
     */
    public Bundle makeBundle(String uri) throws IOException {
        if (!makeDir(uri)) {
            throw new IOException("Unable to create the subdirectory");
        }

        Bundle bundle;
        try {
            bundle = new Bundle(new File(getUnmaskedPath(uri)));
        } catch (NullPointerException e) {
            throw new IOException("Unable to load a bundle for the subdirectory");
        }
        return bundle;
    }

    /**
     * Delete a file from the bundle based on the path.
     *
     * @param uri The path relative to the bundle to delete.
     * @return true iff the file was deleted successfully.
     */
    public boolean deleteFile(String uri) {
        File file = new File(getUnmaskedPath(uri));
        return file.delete();
    }

    /**
     * Copy a folder into the current bundle.
     *
     * @param src The source folder to copy into this bundle.
     * @throws IOException
     */
    public void copyFolder(File src) throws IOException {
        File dest = folder;

        FileUtils.copyDirectory(src, dest);
    }

    /**
     * Reload the files stored within this bundle.
     */
    public void refresh() {
        files = FileLoader.loadFiles(folder.getPath());
    }

    /**
     * Produce a hash for this bundle based on the combined hash of all files
     * within the bundle.
     *
     * @return The string representation of the SHA-256 hash.
     * @throws IOException If any of the source files couldn't be loaded.
     */
    public String hash() throws IOException {
        StringBuilder hashString = new StringBuilder();
        for (SourceFile file : getFiles()) {
            hashString.append(file.toHash());
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException algo) {
            return null; // will surely never occur
        }

        byte[] hash = digest.digest(hashString.toString().getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public String toString() {
        return folder.toString() + ": " + files.toString();
    }
}
