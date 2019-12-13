package chalkbox.api.config;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of a configuration parser designed to work with
 * key-value pair text files, known as boxes.
 *
 * Example format:
 * <pre>
 * key1=value1
 * key2=value2
 * key3=value3
 * </pre>
 */
class BoxConfigParser implements ConfigParser {
    /**
     * Implementation of reading a box file.
     *
     * @param input The input stream to parse.
     * @return The configuration as a configuration object.
     * @throws ConfigParseException If the config file cannot be read.
     */
    @Override
    public ChalkboxConfig read(InputStream input) throws ConfigParseException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        Map<String, String> map = new HashMap<>();
        int lineNumber = 0;
        String line;
        while (true) {
            // Read the next line
            try {
                line = reader.readLine();
                lineNumber++;
            } catch (IOException e) {
                throw new ConfigParseException("Unable to read line "
                        + lineNumber, e);
            }

            // End of input
            if (line == null) {
                break;
            }

            // Skip comments
            if (line.startsWith("#") || line.length() == 0) {
                continue;
            }

            String[] parts = line.split("=", 2);

            // Error a malformed line
            if (parts.length < 2) {
                throw new ConfigParseException("Line "
                        + lineNumber + " doesn't have a key value assignment");
            }
            if (parts.length > 2) {
                throw new ConfigParseException("Line "
                        + lineNumber + " has multiple assignments and is ambiguous");
            }

            map.put(parts[0], parts[1]);
        }

        return new BoxChalkboxConfigImpl(map);
    }

    @Override
    public ChalkboxConfig read(Path path) throws ConfigParseException {
        try {
            return read(new FileInputStream(path.toFile()));
        } catch (FileNotFoundException e) {
            throw new ConfigParseException("Unable to find the config file", e);
        }
    }

    @Override
    public ChalkboxConfig read(String configuration) throws ConfigParseException {
        return read(new ByteArrayInputStream(configuration.getBytes()));
    }

    /**
     * Implementation of a ChalkboxConfig that stores String key to String value
     * mappings as a map.
     */
    private class BoxChalkboxConfigImpl implements ChalkboxConfig {
        private final Map<String, String> config;

        BoxChalkboxConfigImpl(Map<String, String> config) {
            this.config = config;
        }

        @Override
        public String value(String key) {
            return config.get(key);
        }

        @Override
        public boolean isSet(String key) {
            return config.containsKey(key);
        }

        @Override
        public Map<String, String> toMap() {
            return new HashMap<>(config);
        }
    }
}
