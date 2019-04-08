package chalkbox.java;

import chalkbox.api.annotations.Asset;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.annotations.Stream;
import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.common.Compiler;
import chalkbox.api.common.Execution;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Processor(depends = {JavaCompilation.class})
public class JUnitTest {
    private static final String JUNIT_RUNNER = "org.junit.runner.JUnitCore";

    @Stream
    public PrintStream outStream;

    private Bundle tests;
    private boolean hasErrors;
    private String classPath;

    @Asset
    public void compileTests(Map<String, String> config) {
        tests = new Bundle(new File(config.get("tests")));
        Bundle solution = new Bundle(new File(config.get("solution")));

        Bundle solutionOutput;
        Bundle testOutput;
        try {
            solutionOutput = new Bundle();
            testOutput = new Bundle();
            classPath = config.get("classPath") + ":" + testOutput.getUnmaskedPath();
        } catch (IOException e) {
            hasErrors = true;
            e.printStackTrace();
            return;
        }

        StringWriter output = new StringWriter();
        Compiler.compile(Compiler.getSourceFiles(solution), classPath, solutionOutput.getUnmaskedPath(), output);
        Compiler.compile(Compiler.getSourceFiles(tests),
                classPath + ":" + solutionOutput.getUnmaskedPath(),
                testOutput.getUnmaskedPath(), output);
    }

    @Pipe(stream = "submissions")
    public Collection runTests(Collection submission) {
        if (hasErrors) {
            return submission;
        }
        if (!submission.getResults().is("compilation.compiles")) {
            return submission;
        }

        System.out.println(submission.getResults().get("sid"));

        String classPath = this.classPath + ":" + submission.getWorking().getUnmaskedPath("bin");
        for (String className : tests.getClasses("")) {
            runTest(submission, className, classPath);
        }

        return submission;
    }

    public void runTest(Collection submission, String className, String classPath) {
        Process process;
        try {
            process = Execution.runProcess(10000, "java", "-cp",
                    classPath, JUNIT_RUNNER, className);
        } catch (IOException e) {
            e.printStackTrace();
            submission.getResults().set("junit." + className + ".errors", "Test running IO Error - see tutor");
            return;
        } catch (TimeoutException e) {
            submission.getResults().set("junit." + className + ".errors", "Timed out");
            return;
        }

        String output = Execution.readStream(process.getInputStream());
        String errors = Execution.readStream(process.getErrorStream());

        submission.getResults().set("junit." + className + ".output", output);
        submission.getResults().set("junit." + className + ".errors", errors);
    }
}
