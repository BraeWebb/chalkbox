package chalkbox.api.config;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class BoxConfigParserTest {

    private static final String SIMPLE_CONFIG = new StringBuilder()
            .append("key1=value1\n")
            .append("key2=value2\n")
            .append("key3=value3\n")
            .append("mykey=hello\n")
            .append("theirs=world\n")
            .toString();

    @Test(expected = ConfigParseException.class)
    public void testError() throws ConfigParseException {
        ConfigParser parser = ConfigParser.box();
        parser.read("invalid information");
    }

    @Test
    public void testStringReadBasicValue() throws ConfigParseException {
        ConfigParser parser = ConfigParser.box();
        ChalkboxConfig config = parser.read(SIMPLE_CONFIG);
        basicValue(config);
    }

    @Test
    public void testStringReadBasicIsSet() throws ConfigParseException {
        ConfigParser parser = ConfigParser.box();
        ChalkboxConfig config = parser.read(SIMPLE_CONFIG);
        basicIsSet(config);
    }

    @Test
    public void testStreamReadBasicValue() throws ConfigParseException {
        InputStream stream = new ByteArrayInputStream(SIMPLE_CONFIG.getBytes());

        ConfigParser parser = ConfigParser.box();
        ChalkboxConfig config = parser.read(stream);
        basicValue(config);
    }

    @Test
    public void testStreamReadBasicIsSet() throws ConfigParseException {
        InputStream stream = new ByteArrayInputStream(SIMPLE_CONFIG.getBytes());

        ConfigParser parser = ConfigParser.box();
        ChalkboxConfig config = parser.read(stream);
        basicIsSet(config);
    }

    @Test
    public void testPathReadBasicValue()
            throws ConfigParseException, IOException {
        File file = File.createTempFile("testPathReadBasicValue", ".box");
        OutputStream stream = new FileOutputStream(file);
        stream.write(SIMPLE_CONFIG.getBytes());
        stream.flush();
        file.deleteOnExit();

        ConfigParser parser = ConfigParser.box();
        ChalkboxConfig config = parser.read(file.toPath());
        basicValue(config);
    }

    @Test
    public void testPathReadBasicIsSet()
            throws ConfigParseException, IOException {
        File file = File.createTempFile("testPathReadBasicIsSet", ".box");
        OutputStream stream = new FileOutputStream(file);
        stream.write(SIMPLE_CONFIG.getBytes());
        stream.flush();
        file.deleteOnExit();

        ConfigParser parser = ConfigParser.box();
        ChalkboxConfig config = parser.read(file.toPath());
        basicValue(config);
    }

    private void basicValue(ChalkboxConfig config) {
        assertEquals("value1", config.value("key1"));
        assertEquals("value2", config.value("key2"));
        assertEquals("value3", config.value("key3"));
        assertEquals("hello", config.value("mykey"));
        assertEquals("world", config.value("theirs"));
    }

    private void basicIsSet(ChalkboxConfig config) {
        assertTrue(config.isSet("key1"));
        assertTrue(config.isSet("key2"));
        assertTrue(config.isSet("key3"));
        assertTrue(config.isSet("mykey"));
        assertTrue(config.isSet("theirs"));

        assertFalse(config.isSet("key0"));
        assertFalse(config.isSet("key4"));
        assertFalse(config.isSet("hello"));
        assertFalse(config.isSet("world"));
    }

    @Test
    public void testMapConversion() throws ConfigParseException {
        ConfigParser parser = ConfigParser.box();
        ChalkboxConfig config = parser.read(SIMPLE_CONFIG);

        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        expected.put("mykey", "hello");
        expected.put("theirs", "world");

        assertEquals(expected, config.toMap());
    }
}