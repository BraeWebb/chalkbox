package chalkbox.api.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading file paths relative to a root folder.
 */
public class FileLoader {
    private File root;
    private String prefix = "";
    private String suffix = "";
    private boolean recursive = true;
    private boolean removeSuffix = false;
    private boolean ignoreHidden = true;

    /**
     * Create a file loader with the root folder
     *
     * @param root folder that paths are relative to
     */
    public FileLoader(String root) {
        this.root = new File(root);
    }

    /**
     * Create a file loader with the root folder
     *
     * @param root folder that paths are relative to
     * @param recursive whether to recursively search for files
     */
    public FileLoader(String root, boolean recursive) {
        this(root);
        this.recursive = recursive;
    }

    /**
     * Create a file loader with the root folder
     *
     * @param root folder that paths are relative to
     * @param prefix only match files matching this prefix
     * @param suffix only match files matching this suffix
     */
    public FileLoader(String root, String prefix, String suffix) {
        this(root, true);
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * Create a file loader with the root folder
     *
     * @param root folder that paths are relative to
     * @param prefix only match files matching this prefix
     * @param suffix only match files matching this suffix
     * @param recursive whether to recursively search for files
     */
    public FileLoader(String root, String prefix,
                      String suffix, boolean recursive) {
        this(root, prefix, suffix);
        this.recursive = recursive;
    }

    public FileLoader ignoreHiddenFiles(boolean ignore) {
        this.ignoreHidden = ignore;
        return this;
    }

    /**
     * Turn on or off the removal of matching suffixes in resultant paths
     */
    public void setRemoveSuffix(boolean removeSuffix) {
        this.removeSuffix = removeSuffix;
    }

    /**
     * Search for files, returning the file paths relative to the loader root
     *
     * @param root the directory to search for files within
     * @return file paths relative to the loader root
     */
    public List<String> loadFiles(File root) {
        List<String> files = new ArrayList<>();
        File[] subfiles = root.listFiles();
        if (subfiles == null) {
            return files;
        }
        for (File file : subfiles) {
            // remove hidden files
            if (hiddenFilter(file)) {
                continue;
            }

            // Add nested directories
            if (file.isDirectory() && recursive) {
                files.addAll(loadFiles(file));
                continue;
            }

            if (file.getName().startsWith(prefix)
                    && file.getName().endsWith(suffix)) {
                files.add(truncatePath(file));
            }
        }
        return files;
    }

    /**
     * Utility function to truncate a path relative to root
     */
    public static String truncatePath(File root, File path) {
        String location = root.toURI().relativize(path.toURI()).getPath();
        if (path.isDirectory()) {
            location = location.substring(0, location.length() - 1);
        }
        return location;
    }

    /**
     * Recursively load the relative paths of all files in the path
     */
    public static List<String> loadFiles(String path) {
        FileLoader loader = new FileLoader(path);

        return loader.loadFiles(new File(path));
    }

    /**
     * Utility function to truncate a path relative to root
     */
    private String truncatePath(File path) {
        String relative = root.toURI().relativize(path.toURI()).getPath();
        if (removeSuffix) {
            relative = relative.replace(suffix, "");
        }
        return relative;
    }

    private boolean hiddenFilter(File file) {
        return this.ignoreHidden && file.getName().startsWith(".");
    }
}
