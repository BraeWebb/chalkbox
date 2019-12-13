package chalkbox.api.config;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Interface for loading configuration information.
 *
 * Example usage: Parsing a box file called mybox.box in the working directory.
 * {@code
 * ConfigParser parser = ConfigParser.box();
 * ChalkboxConfig config = parser.read(Paths.of("./mybox.box"));
 * }
 */
public interface ConfigParser {
    /**
     * @return An instance of config parser designed for parsing box files.
     */
    static ConfigParser box() {
        return new BoxConfigParser();
    }

    /**
     * Parse the config from an input stream.
     *
     * @param input The input stream to parse.
     * @return The loaded configuration.
     *
     * @throws ConfigParseException If the config can't be parsed for some reason.
     */
    ChalkboxConfig read(InputStream input) throws ConfigParseException;

    /**
     * Parse the config from a file at a given path.
     *
     * @param path The path of the file to parse.
     * @return The loaded configuration.
     *
     * @throws ConfigParseException If the config can't be parsed for some reason.
     */
    ChalkboxConfig read(Path path) throws ConfigParseException;

    /**
     * Parse the config from a string representation of the configuration.
     *
     * @param configuration The string of the configuration.
     * @return The loaded configuration.
     *
     * @throws ConfigParseException If the config can't be parsed for some reason.
     */
    ChalkboxConfig read(String configuration) throws ConfigParseException;
}
