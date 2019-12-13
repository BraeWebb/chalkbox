package chalkbox.api.config;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class BoxConfigParserTest {

    private static final String SIMPLE_CONFIG = new StringBuilder()
            .append("key1=value1")
            .append("key2=value2")
            .append("key3=value3")
            .append("mykey=hello")
            .append("theirs=world")
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
        file.deleteOnExit();

        ConfigParser parser = ConfigParser.box();
        ChalkboxConfig config = parser.read(file.getPath());
        basicValue(config);
    }

    @Test
    public void testPathReadBasicIsSet()
            throws ConfigParseException, IOException {
        File file = File.createTempFile("testPathReadBasicIsSet", ".box");
        file.deleteOnExit();

        ConfigParser parser = ConfigParser.box();
        ChalkboxConfig config = parser.read(file.getPath());
        basicValue(config);
    }

    private void basicValue(ChalkboxConfig config) {
        assertEquals("value1", config.value("key1"));
        assertEquals("value2", config.value("key2"));
        assertEquals("value3", config.value("key3"));
        assertEquals("hello", config.value("mykey"));
        assertEquals("world", config.value("theirs"));
    }

    private void basicIsSet(ChalkboxConfig config) throws ConfigParseException {
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
}