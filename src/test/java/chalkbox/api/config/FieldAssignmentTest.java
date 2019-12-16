package chalkbox.api.config;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test custom field type assignments using the ConfigItem annotation
 */
public class FieldAssignmentTest {
    // enum used to test fields of type Enum
    public enum TestEnum {
        IN_ENUM, A, B, C
    }

    // class used to assign field values for different types
    public class InstanceClass {
        public String stringField;
        public File fileField;
        public TestEnum enumField;
        public int intField;
        public boolean booleanField;
        public List<String> listField;
        public String[] arrayField;
        public ZipFile zipField;
    }

    public interface Callback {
        void accept();
    }

    /**
     * Mocks the error stream while running the callback method.
     *
     * @param callback Method to execute while mocking the error stream.
     * @return The text printed to the error stream.
     */
    private String mockErrorStream(Callback callback) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream mockedError = new PrintStream(outStream);
        PrintStream realError = System.err;

        System.setErr(mockedError);
        callback.accept();
        System.setErr(realError);

        return outStream.toString();
    }

    private InstanceClass instance;
    private FieldAssigner assigner;

    /**
     * Create an instance to use for testing with a field assigner instance.
     */
    @Before
    public void setup() {
        instance = new InstanceClass();
        assigner = FieldAssigner.getInstance(instance);
    }

    private boolean assign(String fieldName, String value) {
        try {
            Field field = instance.getClass().getField(fieldName);

            Method method = FieldAssigner.getMethod(assigner, field);
            if (method == null) {
                fail("No assign method found for the field");
            }

            return (boolean) method.invoke(assigner,
                    field.get(instance), field, value);
        } catch (NoSuchFieldException|IllegalAccessException
                |InvocationTargetException e) {
            e.printStackTrace();
            fail("Exception thrown during assignment");
            return false;
        }
    }

    @Test
    public void testStringAssignment() {
        boolean result = assign("stringField", "field value");

        assertTrue(result);
        assertEquals("field value", instance.stringField);
    }

    @Test
    public void testFileAssignment() {
        String file = "src/test/resources/csse1001/test.box";
        boolean result = assign("fileField", file);

        assertTrue(result);
        assertEquals(file, instance.fileField.getPath());
    }

    @Test
    public void testNoFileAssignment() {
        String file = "nosuchfile";

        String out = mockErrorStream(() -> {
            boolean result = assign("fileField", file);
            assertFalse(result);
        });

        assertEquals("Unable to find file: nosuchfile\n", out);
        assertNull(instance.fileField);
    }

    @Test
    public void testEnumAssignment() {
        boolean result = assign("enumField", "IN_ENUM");

        assertTrue(result);
        assertEquals(TestEnum.IN_ENUM, instance.enumField);
    }

    @Test
    public void testNoEnumAssignment() {
        String out = mockErrorStream(() -> {
            boolean result = assign("enumField", "NOT_IN_ENUM");
            assertFalse(result);
        });

        assertEquals("Invalid enum value for enum: class chalkbox.api.config.FieldAssignmentTest$TestEnum\n" +
                "Value enum values are: [IN_ENUM, A, B, C]\n" +
                "Found: NOT_IN_ENUM\n", out);
        assertNull(instance.enumField);
    }

    @Test
    public void testIntAssignment() {
        boolean result = assign("intField", "42");

        assertTrue(result);
        assertEquals(42, instance.intField);
    }

    @Test
    public void testNoIntAssignment() {
        String out = mockErrorStream(() -> {
            boolean result = assign("intField", "not an int");
            assertFalse(result);
        });

        assertEquals("Unable to cast not an int to integer\n", out);
        assertEquals(0, instance.intField);
    }

    @Test
    public void testBoolAssignment() {
        boolean result = assign("booleanField", "true");

        assertTrue(result);
        assertTrue(instance.booleanField);

        result = assign("booleanField", "false");

        assertTrue(result);
        assertFalse(instance.booleanField);
    }

    @Test
    public void testInvalidBoolAssignment() {
        String out = mockErrorStream(() -> {
            boolean result = assign("booleanField", "not a bool");
            assertFalse(result);
        });

        assertEquals("Unable to cast not a bool to boolean\n", out);
        assertFalse(instance.booleanField);
    }

    @Test
    public void testListAssignment() {
        boolean result = assign("listField", "first,second,second last,last");

        List<String> list = new ArrayList<>();
        list.add("first");
        list.add("second");
        list.add("second last");
        list.add("last");

        assertTrue(result);
        assertEquals(list, instance.listField);
    }

    @Test
    public void testOneListAssignment() {
        boolean result = assign("listField", "first");

        List<String> list = new ArrayList<>();
        list.add("first");

        assertTrue(result);
        assertEquals(list, instance.listField);
    }

    @Test
    public void testArrayAssignment() {
        boolean result = assign("arrayField", "first,second,second last,last");

        List<String> list = new ArrayList<>();
        list.add("first");
        list.add("second");
        list.add("second last");
        list.add("last");

        assertTrue(result);
        assertArrayEquals(list.toArray(new String[]{}), instance.arrayField);
    }

    @Test
    public void testOneArrayAssignment() {
        boolean result = assign("arrayField", "first");

        List<String> list = new ArrayList<>();
        list.add("first");

        assertTrue(result);
        assertArrayEquals(list.toArray(new String[]{}), instance.arrayField);
    }

    @Test
    public void testZipAssignment() {
        String file = "src/test/resources/csse1001/gradebook.zip";
        boolean result = assign("zipField", file);

        assertTrue(result);
        assertEquals(file, instance.zipField.getName());
    }

    @Test
    public void testNoZipAssignment() {
        String file = "src/test/resources/csse1001/nozip.zip";
        String out = mockErrorStream(() -> {
            boolean result = assign("zipField", file);
            assertFalse(result);
        });

        assertTrue(out.endsWith("IO Exception trying to read zip file\n"));
        assertNull(instance.zipField);
    }

    @Test
    public void testInvalidZipAssignment() {
        String file = "src/test/resources/csse1001/test.box";
        String out = mockErrorStream(() -> {
            boolean result = assign("zipField", file);
            assertFalse(result);
        });

        assertTrue(out.endsWith("Invalid zip file found\n"));
        assertNull(instance.zipField);
    }
}
