package chalkbox.api.files;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

/**
 * An implementation of a source file based around abstracting an actual file
 */
public class FileSourceFile extends SourceFile {
    private final File file;

    /**
     * Create a source file from a base file
     *
     * @param uri The unique resource identifier of the file (analogous to path)
     * @param file The file with which to base the source file
     */
    public FileSourceFile(String uri, File file) {
        super(uri);
        this.file = file;
    }

    /**
     * Create a source file from a base file, the URI of the file is based on
     * the path of the file
     *
     * @param file The file with which to base the source file
     */
    public FileSourceFile(File file) {
        this(file.getPath(), file);
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return new FileReader(file);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }
}
