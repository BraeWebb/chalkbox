package chalkbox.api.collections;

import chalkbox.api.files.FileLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
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
}
