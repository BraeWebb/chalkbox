package chalkbox.api.collections;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A mapping of keys to values in a JSON format
 */
public class Data {
    private JSONObject json;

    /**
     * Construct a new empty data collection
     */
    public Data() {
        json = new JSONObject();
    }

    /**
     * Construct a new data collection from a JSON string
     */
    public Data(String json) {
        JSONParser parser = new JSONParser();
        try {
            this.json = (JSONObject) parser.parse(json);
        } catch (ParseException pe) {
            this.json = new JSONObject();
        }
    }

    /**
     * Construct a data collection that is a copy of the parameter
     *
     * @param data The data to copy
     */
    public Data(Data data) {
        this(data.json.toJSONString());
    }

    /**
     * Construct a data collection from a json file
     *
     * @param file The JSON file to import
     * @throws IOException If there is an error parsing the file
     */
    public Data(File file) throws IOException {
        FileReader reader = new FileReader(file);
        JSONParser parser = new JSONParser();
        JSONObject json;
        try {
            Object object = parser.parse(reader);
            if (object instanceof JSONObject) {
                json = (JSONObject) object;
            } else {
                throw new IOException();
            }
        } catch (IOException | ParseException pe) {
            throw new IOException("Unable to parse JSON file");
        } finally {
            reader.close();
        }

        this.json = json;
    }

    /**
     * @return The keys of a data object.
     */
    public Set<String> keys() {
        return json.keySet();
    }

    /**
     * @return The keys of a data object at a given key.
     */
    public Set<String> keys(String key) {
        Object json = get(key);
        if (json instanceof JSONObject) {
            return ((JSONObject) json).keySet();
        }
        if (json instanceof Data) {
            return ((Data) json).keys();
        }
        return new HashSet<>();
    }

    /**
     * Return a boolean value at key, if key doesn't hold a boolean returns false.
     *
     * @param key The key to search for
     * @return true if key holds a value true boolean value
     */
    public boolean is(String key) {
        Object value = get(key);
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        return false;
    }

    /**
     * Get a value stored at the given key
     *
     * @param key The key to lookup
     * @return The value stored in this data collection
     */
    public Object get(String key) {
        if (!key.contains(".")) {
            return json.get(key);
        }

        String[] keys = key.split("(?<!\\\\)\\.");
        JSONObject json = this.json;
        for (int i = 0; i < keys.length - 1; i++) {
            String keyValue = keys[i].replace("\\.", ".");
            if (!json.containsKey(keyValue)) {
                return null;
            } else {
                Object inner = json.get(keyValue);
                if (inner instanceof JSONObject) {
                    json = (JSONObject) inner;
                } else if (inner instanceof Data) {
                    json = ((Data) inner).json;
                }
            }
        }
        return json.get(keys[keys.length - 1].replace("\\.", "."));
    }

    /**
     * Store a value at the given key
     *
     * @param key The key to store value at
     * @param value The value to store
     */
    public void set(String key, Object value) {
        if (!key.contains(".")) {
            json.put(key, value);
            return;
        }

        if (value instanceof Data) {
            value = ((Data) value).json;
        }

        String[] keys = key.split("(?<!\\\\)\\.");
        JSONObject oldJson = this.json;
        JSONObject json = this.json;
        for (int i = 0; i < keys.length - 1; i++) {
            String keyValue = keys[i].replace("\\.", ".");
            if (!oldJson.containsKey(keyValue)) {
                json = new JSONObject();
            } else {
                json = (JSONObject) oldJson.get(keyValue);
            }
            oldJson.put(keyValue, json);
            oldJson = json;
        }
        json.put(keys[keys.length - 1].replace("\\.", "."), value);
    }

    @Override
    public String toString() {
        return this.json.toJSONString();
    }
}
