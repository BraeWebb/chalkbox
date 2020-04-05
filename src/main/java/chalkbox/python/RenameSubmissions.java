package chalkbox.python;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;
import chalkbox.java.junit.JUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

/**
 * Process to rename submissions containing only one file of with an extension
 * to the expected file name.
 *
 * The file-name parameter specifies what the expected file should be, e.g. a1.py
 *
 * The expected-extension parameter specifies what extension to look for.
 *
 * If only one file is found with the given extension then it is renamed to
 * the expected file name.
 */
@Processor
public class RenameSubmissions {
    private static final Logger LOGGER = Logger.getLogger(JUnit.class.getName());

    @ConfigItem(key = "file-name", required = false,
            description = "If set, rename all submissions with only one file to the given name")
    public String fileName;
    @ConfigItem(key = "expected-extension", required = false,
            description = "Rename only files matching the given extension")
    public String expectedExtension;

    @Pipe
    public Collection run(Collection collection) throws IOException {
        if (fileName == null) {
            return collection;
        }
        List<String> files = collection.getWorking().getFileNames(expectedExtension);
        String sid = collection.getResults().get("sid").toString();

        if (files.size() == 1 && !files.get(0).equals(fileName)) {
            String file = files.get(0);
            if (!file.equals(fileName)) {
                LOGGER.info("Renaming " + file + " from " + sid + " to " + fileName);
                Path fromPath = Paths.get(collection.getWorking().getUnmaskedPath(file));
                Path toPath = Paths.get(collection.getWorking().getUnmaskedPath(fileName));
                Files.move(fromPath, toPath);
            }
        }

        return collection;
    }
}
