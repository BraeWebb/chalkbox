package chalkbox.api.collections;

import chalkbox.api.files.FileLoader;
import chalkbox.api.files.FileSourceFile;
import chalkbox.api.files.SourceFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Bundle of files and folders, abstracting away folders
 */
public class Bundle {
    /* Name of files stored within this bundle */
    private List<String> files = new ArrayList<>();
    /* Root folder */
    private File folder;

    /**
     * Create a new temporary bundle
     *
     * @throws IOException if a temporary bundle cannot be created
     */
    public Bundle() throws IOException {
        this.folder = Files.createTempDirectory("temp").toFile();
    }

    /**
     * Create a new bundle of an existing folder
     *
     * @param folder an existing folder mocked by the bundle
     */
    public Bundle(File folder) {
        this.folder = folder;
        this.files = FileLoader.loadFiles(folder.getPath());
    }

    /**
     * Return the list of files stored in this bundle
     *
     * @return File names of files in this bundle
     */
    public List<String> getFileNames() {
        return new ArrayList<>(files);
    }

    public List<String> getFileNames(String extension) {
        List<String> filenames = new ArrayList<>();
        for (String filename : files) {
            if (filename.endsWith(extension)) {
                filenames.add(filename);
            }
        }
        return filenames;
    }

    public Bundle getBundle(String path) {
        return new Bundle(new File(folder.getPath() + File.separator + path));
    }

    public List<String> getClasses(String sourceRoot) {
        Bundle sources = getBundle(sourceRoot);
        List<String> classes = new ArrayList<>();
        for (String filename : sources.getFileNames(".java")) {
            classes.add(filename.replace(".java", "")
                    .replace(File.separator, "."));
        }
        return classes;
    }

    public SourceFile[] getFiles() throws IOException {
        return getFiles("");
    }

    public SourceFile[] getFiles(String extension) throws IOException {
        List<String> filenames = getFileNames(extension);

        SourceFile[] sources = new SourceFile[filenames.size()];

        for (int i = 0; i < filenames.size(); i++) {
            sources[i] = getFile(filenames.get(i));
        }

        return sources;
    }

    public SourceFile getFile(String uri) throws IOException {
        if (!files.contains(uri)) {
            throw new FileNotFoundException("Couldn't find file: " + uri + " in " + folder.getPath());
        }

        File file = new File(this.folder + File.separator + uri);
        return new FileSourceFile(uri, file);
    }

    public String getUnmaskedPath() {
        return this.folder.getPath();
    }

    public String getUnmaskedPath(String uri) {
        return this.folder + File.separator + uri;
    }

    public boolean makeDir(String uri) {
        if (files.contains(uri)) {
            return false;
        }

        File file = new File(this.folder + File.separator + uri);
        if (file.mkdir()) {
            return true;
        }

        return false;
    }

    public void refresh() {
        files = FileLoader.loadFiles(folder.getPath());
    }

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
        try {
            return hash() + ": " + files.toString();
        } catch (IOException e) {
            return files.toString();
        }
    }
}
