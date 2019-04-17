package chalkbox.java.test;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Prior;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import chalkbox.api.common.java.JUnitRunner;
import chalkbox.java.compilation.JavaCompilation;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Process to execute JUnit tests on each submission.
 *
 * <p>Requires {@link JavaCompilation} process to be executed first.
 * Checks if compilation.compiles is true.
 *
 * <p>An example output is shown below, where ... is the raw test output:
 * <pre>
 * "tests": {
 *     "package1.ClassOneTest": {
 *         "output": "...",
 *         "total": 12,
 *         "passes": 8,
 *         "fails": 4,
 *         "errors": ""
 *     }
 * }
 * </pre>
 *
 * <p>If a test class times out while being executed, the below will be given:
 * <pre>
 * "tests": {
 *     "package1.TimedOutTest": {
 *         "errors": "Timed out"
 *     }
 * }
 * </pre>
 */
@Processor(depends = {JavaCompilation.class})
public class JavaTest {
    private Bundle tests;
    private boolean hasErrors;

    @ConfigItem(key = "solution", description = "Sample solution to compile tests with")
    public String solutionPath;

    @ConfigItem(key = "tests", description = "Path of JUnit test files")
    public String testPath;

    @ConfigItem(description = "Class path for tests to be compiled with")
    public String classPath;

    /**
     * Compile the sample solution and then compile the tests with the sample
     * solution.
     */
    @Prior
    public void compileTests(Map<String, String> config) {
        tests = new Bundle(new File(testPath));
        Bundle solution = new Bundle(new File(solutionPath));

        /* Load output directories for the solution and the tests */
        Bundle solutionOutput;
        Bundle testOutput;
        try {
            solutionOutput = new Bundle();
            testOutput = new Bundle();
            /* Add the tests to the class path for execution */
            classPath = classPath + ":" + testOutput.getUnmaskedPath();
        } catch (IOException e) {
            hasErrors = true;
            e.printStackTrace();
            return;
        }

        StringWriter output = new StringWriter();

        /* Compile the sample solution */
        Compiler.compile(Compiler.getSourceFiles(solution), classPath,
                solutionOutput.getUnmaskedPath(), output);

        /* Compile the tests with the sample solution */
        Compiler.compile(Compiler.getSourceFiles(tests),
                classPath + ":" + solutionOutput.getUnmaskedPath(),
                testOutput.getUnmaskedPath(), output);
    }

    /**
     * Run the tests on a submission
     */
    @Pipe(stream = "submissions")
    public Collection runTests(Collection submission) {
        if (hasErrors) {
            return submission;
        }
        if (!submission.getResults().is("compilation.compiles")) {
            return submission;
        }

        String classPath = this.classPath + ":" + submission.getWorking().getUnmaskedPath("bin");
        for (String className : tests.getClasses("")) {
            Data results = JUnitRunner.runTest(className, classPath);
            submission.getResults().set("tests." + className.replace(".", "\\."), results);
        }

        return submission;
    }
}
