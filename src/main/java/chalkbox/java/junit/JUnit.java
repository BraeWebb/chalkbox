package chalkbox.java.junit;

import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Prior;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import chalkbox.api.common.java.JUnitRunner;
import chalkbox.api.files.FileLoader;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Processor
public class JUnit {
    private static final String SOLUTIONS_ROOT = "junit.solutions";
    private static final String SAMPLE_SOLUTION = "solution";

    private String solutionClassPath;
    private Map<String, String> classPaths = new HashMap<>();

    @Prior
    public void compileSolutions(Map<String, String> config) {
        String classPath = config.get("classPath");
        File outputFolder = new File(config.get("results") + File.separator + "solutions");
        File solutionsFolder = new File(config.get("junitSolutions"));

        outputFolder.mkdirs();
        Bundle output = new Bundle(outputFolder);

        StringWriter writer = new StringWriter();
        File[] classes = solutionsFolder.listFiles();
        for (File clazz : classes) {
            Bundle solutionBundle = new Bundle(new File(clazz.getPath()));
            String solutionOut = output.getUnmaskedPath(clazz.getName());

            try {
                Compiler.compile(Arrays.asList(solutionBundle.getFiles()),
                        classPath, solutionOut, writer);
            } catch (IOException io) {
                System.err.println("Failed to compile solution: " + clazz);
            }

            classPaths.put(FileLoader.truncatePath(solutionsFolder, clazz),
                    classPath + ":" + solutionOut);
        }
    }

    @Prior
    public void compileSolution(Map<String, String> config) {
        String classPath = config.get("classPath");
        File outputFolder = new File(config.get("results") + File.separator + "fullSolution");
        File solutionFolder = new File(config.get("solution"));

        outputFolder.mkdirs();
        Bundle output = new Bundle(outputFolder);

        StringWriter writer = new StringWriter();
        Bundle solutionBundle = new Bundle(solutionFolder);
        try {
            Compiler.compile(Arrays.asList(solutionBundle.getFiles()),
                    classPath, output.getUnmaskedPath(), writer);
        } catch (IOException io) {
            System.err.println("Failed to compile full solution");
        }

        solutionClassPath = classPath + ":" + output.getUnmaskedPath();
    }

    @Pipe(stream = "submissions")
    public Collection runTests(Collection submission) {
        String[] testClasses = new String[]{"passengers.PassengerTest", "stops.StopTest"};
        Bundle junitBundle = submission.getSource().getBundle("test");

        StringWriter output = new StringWriter();
        boolean success;
        try {
            success = Compiler.compile(Arrays.asList(junitBundle.getFiles()),
                    solutionClassPath, submission.getWorking().getUnmaskedPath(), output);
        } catch (IOException io) {
            submission.getResults().set("junit.compiles", false);
            submission.getResults().set("junit.error", "IO Compile Error - See tutor");
            return submission;
        }

        submission.getResults().set("junit.compiles", success);
        submission.getResults().set("junit.output", output.toString());

        if (!success) {
            return submission;
        }

        for (String solution : classPaths.keySet()) {
            String classPath = classPaths.get(solution) + ":" + submission.getWorking().getUnmaskedPath();
            for (String testClass : testClasses) {
                Data results = JUnitRunner.runTest(testClass, classPath);
                String jsonRoot = SOLUTIONS_ROOT + "." + solution + "." + testClass.replace(".", "\\.");
                submission.getResults().set(jsonRoot, results);
            }
        }

        return submission;
    }

    /**
     * Gives a .correct flag to each broken solution.
     * .correct is true iff the solution passes fewer tests than the sample solution.
     */
    @Pipe(stream = "submissions")
    public Collection hack(Collection submission) {
        Data data = submission.getResults();
        /* Determine the baseline amount of tests that pass for the sample solution */
        Map<String, Integer> baseline = new HashMap<>();
        String solutionPath = SOLUTIONS_ROOT + "." + SAMPLE_SOLUTION;
        for (String clazz : data.keys(solutionPath)) {
            String solutionKey = solutionPath + "." + clazz.replace(".", "\\.");

            if (data.get(solutionKey + ".passes") == null) {
                continue;
            }

            int passes = Integer.parseInt(data.get(solutionKey + ".passes").toString());
            baseline.put(clazz, passes);
        }

        /* Flag all the solutions that have a better amount of failing tests */
        for (String solution : data.keys(SOLUTIONS_ROOT)) {
            if (solution.equals(SAMPLE_SOLUTION)) {
                continue;
            }
            String solutionKey = SOLUTIONS_ROOT + "." + solution;

            for (String clazz : data.keys(solutionKey)) {
                String testKey = solutionKey + "." + clazz.replace(".", "\\.");
                data.set(testKey + ".correct", false);

                if (data.get(testKey + ".passes") == null) {
                    continue;
                }

                int passes = Integer.parseInt(data.get(testKey + ".passes").toString());

                if (baseline.containsKey(clazz)) {
                    if (passes < baseline.get(clazz)) {
                        data.set(testKey + ".correct", true);
                    }
                }
            }
        }

        return submission;
    }
}
