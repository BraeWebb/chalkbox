package system;

import chalkbox.api.ChalkBox;
import chalkbox.api.config.ConfigParseException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Automated system test of a CSSE1001 functionality test.
 *
 * These tests use the resources/csse1001 sample files to complete.
 */
public class Test1001Functionality {
    private static final String BASE_FOLDER = "./src/test/resources/csse1001/";

    private static final String BOX_PATH = BASE_FOLDER + "test.box";
    private static final String EXPECTED_PATH = BASE_FOLDER + "expected";
    private static final String OUT_PATH = BASE_FOLDER + "data";

    /**
     * Run the whole chalkbox tool in the on the functionality testing box
     * located in the test resource folder.
     */
    @BeforeClass
    public static void runTests() throws ConfigParseException {
        ChalkBox.main(new String[]{BOX_PATH});
    }

    @AfterClass
    public static void removeOutput() throws IOException {
        FileUtils.deleteDirectory(Paths.get(OUT_PATH).toFile());
    }

    @Test
    public void testOutputFolderCreated() {
        assertTrue(Files.exists(Paths.get(OUT_PATH)));
    }

    /**
     * Get the file of the actual data file from the expected file.
     */
    private static File getActualFile(File expected) {
        String actual = expected.getParentFile().getParent() + File.separator
                + "data" + File.separator + expected.getName();

        return Paths.get(actual).toFile();
    }

    /**
     * Test that the student output files were created.
     */
    @Test
    public void testDataFilesCreated() {
        File expected = Paths.get(EXPECTED_PATH).toFile();
        File[] files = expected.listFiles();

        if (files == null) {
            fail("Unable to read files in expected folder: " + EXPECTED_PATH);
        }

        for (File file : files) {
            assertTrue(file.getName() + " was not created",
                    Files.exists(getActualFile(file).toPath()));
        }
    }

    /**
     * Test that the student output files are correct.
     */
    @Test
    @Ignore(value = "Test has to be ignored because times will likely be different")
    public void testDataFilesEqual() throws IOException {
        File expected = Paths.get(EXPECTED_PATH).toFile();
        File[] files = expected.listFiles();

        if (files == null) {
            fail("Unable to read files in expected folder: " + EXPECTED_PATH);
        }

        for (File file : files) {
            File actual = getActualFile(file);
            assertEquals("Student data files are different",
                    FileUtils.readFileToString(file, "utf-8"),
                    FileUtils.readFileToString(actual, "utf-8"));
        }
    }
}
