package chalkbox.api.common.java;

import chalkbox.api.collections.Data;
import chalkbox.api.common.Execution;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Utility class to execute a JUnit test.
 */
public class JUnitRunner {
    private static final String JUNIT_RUNNER = "org.junit.runner.JUnitCore";

    /**
     * Run a JUnit test with the name className and a given classPath.
     *
     * @param className Name of the JUnit class to execute.
     * @param classPath Class path for the JUnit execution
     *
     * @return The json output of executing a JUnit test
     */
    public static Data runTest(String className, String classPath) {
        Data results = new Data();

        /* Execute a JUnit process */
        Process process;
        try {
            process = Execution.runProcess(10000, "java", "-cp",
                    classPath, JUNIT_RUNNER, className);
        } catch (IOException e) {
            e.printStackTrace();
            results.set("errors", "Test running IO Error - see tutor");
            return results;
        } catch (TimeoutException e) {
            results.set("errors", "Timed out");
            return results;
        }

        /* Consume the std out and std error */
        String output = Execution.readStream(process.getInputStream());
        String errors = Execution.readStream(process.getErrorStream());

        results.set("output", output);
        results.set("errors", errors);

        /* Parse the JUnit output */
        JUnitParser jUnit;
        try {
            jUnit = JUnitParser.parse(output);
        } catch (IOException io) {
            io.printStackTrace();
            return results;
        } catch (JUnitParseException p) {
            System.err.println(output);
            p.printStackTrace();
            return results;
        }

        results.set("passes", jUnit.getPasses());
        results.set("fails", jUnit.getFails());
        results.set("total", jUnit.getTotal());
        results.set("output", jUnit.formatOutput());

        return results;
    }
}
