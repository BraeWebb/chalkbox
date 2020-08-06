package chalkbox.java.junit;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Prior;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import chalkbox.api.common.java.JUnitRunner;
import chalkbox.api.files.FileLoader;
import chalkbox.api.files.SourceFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Processor
public class JUnit {
    private static final Logger LOGGER = Logger.getLogger(JUnit.class.getName());
    private static final String SOLUTIONS_ROOT = "junit.solutions";

    private Bundle solutionsOutput;
    private Bundle solutionOutput;

    private String solutionClassPath;
    private Map<String, String> classPaths = new HashMap<>();

    @ConfigItem(description = "JUnit classes to execute, separated by |")
    public String classes;

    @ConfigItem(description = "Class path for student tests to be compiled with")
    public String classPath;

    @ConfigItem(key = "solution",
            description = "Path to a directory containing the sample solution")
    public String solution;

    @ConfigItem(key = "junitSolutions",
            description = "Path to a directory containing various broken sample solutions")
    public String solutions;

    @Prior
    public void init() {
        createCompilationOutput();
        compileSolution();
        compileSolutions();
    }

    /**
     * Creates the compilation output directory.
     */
    public void createCompilationOutput() {
        /* Create a new temporary directory for compilation output */
        Bundle compilationOutput;
        try {
            compilationOutput = new Bundle();
        } catch (IOException e) {
            LOGGER.severe("Unable to create compilation output directory");
            return;
        }

        /* Create a subdirectory for broken solutions compilation */
        try {
            solutionsOutput = compilationOutput.makeBundle("solutions");
        } catch (IOException e) {
            LOGGER.severe("Unable to create solutions directory in compilation output directory");
            return;
        }

        /* Create a subdirectory for sample solution compilation */
        try {
            solutionOutput = compilationOutput.makeBundle("solution");
        } catch (IOException e) {
            LOGGER.severe("Unable to create solution directory in compilation output directory");
        }
    }

    private void compileSolution(Bundle source, String name, String output,
                                 StringWriter writer) {
        /* Collect all the source files to compile */
        SourceFile[] files;
        try {
            files = source.getFiles(".java");
        } catch (IOException e) {
            LOGGER.severe("Unable to load the source files within solution");
            return;
        }

        /* Compile the solution */
        boolean compiled = Compiler.compile(Arrays.asList(files),
                classPath, output, writer);
        if (!compiled) {
            LOGGER.severe("Unable to compile solution: " + name);
            LOGGER.severe(writer.toString());
        }
        LOGGER.finest("Solution " + name + " Compilation Output");
        LOGGER.finest(writer.toString());
    }

    /**
     * Compile all the broken solutions to test students junit tests on
     */
    public void compileSolutions() {
        /* Collect the list of broken solution folders */
        File solutionsFolder = new File(solutions);
        File[] solutions = solutionsFolder.listFiles();
        if (solutions == null) {
            LOGGER.severe("Unable to load the folder of broken solutions");
            return;
        }

        StringWriter writer;
        for (File solutionFolder : solutions) {
            String solutionName = FileLoader.truncatePath(solutionsFolder, solutionFolder);

            /* Get the folder for compilation output of this solution */
            Bundle solutionBundle = new Bundle(new File(solutionFolder.getPath()));
            String solutionOut = solutionsOutput.getAbsolutePath(solutionFolder.getName());

            writer = new StringWriter();
            compileSolution(solutionBundle, solutionName, solutionOut, writer);

            /* Add an entry for this solution to the class path mapping */
            classPaths.put(solutionName, classPath + ":" + solutionOut);
        }
    }

    /**
     * Compile the sample solution.
     */
    public void compileSolution() {
        Bundle solutionSource = new Bundle(new File(solution));

        /* Compile the sample solution */
        StringWriter writer = new StringWriter();
        compileSolution(solutionSource, "sample solution",
                solutionOutput.getUnmaskedPath(), writer);

        solutionClassPath = classPath + ":" + solutionOutput.getUnmaskedPath();
    }

    @Pipe
    public Collection compileTests(Collection submission) {
        String[] testClasses = classes.split("\\|");
        String student = submission.getResults().get("sid").toString();
        LOGGER.info(String.format("STUDENT(%s) Tests Compiling", student));

        Bundle tests = submission.getSource().getBundle("test");

        StringWriter output = new StringWriter();
        StringWriter error = new StringWriter();

        boolean success = false; // new code
        /*
            Used to compile all together - now compiles tests individually
            to switch back to original functionality - removed lines which are
            marked new code.
            uncomment commented out lines
         */
        //List<SourceFile> files = new ArrayList<>();
        for (String className : testClasses) {
            String fileName = className.replace(".", "/") + ".java";
            SourceFile file;
            try {
                file = tests.getFile(fileName);
                List<SourceFile> files = new ArrayList<>();
                files.add(file);
                //start of new code
                boolean fileSuccess = Compiler.compile(files,
                        solutionClassPath,
                        submission.getWorking().getUnmaskedPath(), output);
                if (fileSuccess) {
                    success = true;
                }
                //end of new code
                output.write("JUnit test file " + fileName + " found\n");
            } catch (FileNotFoundException e) {
                error.write("JUnit test file " + fileName + " not found\n");
            } catch (IOException e) {
                error.write("IO Compile Error - Please contact course staff\n");
            }

        }

        /*
        boolean success = Compiler.compile(files, solutionClassPath,
                submission.getWorking().getUnmaskedPath(), output);
         */
        submission.getResults().set("junit.compiles", success);
        submission.getResults().set("junit.output", output.toString());
        submission.getResults().set("junit.error", error.toString());
        return submission;
    }

    @Pipe
    public Collection runTests(Collection submission) {
        String student = submission.getResults().get("sid").toString();

        if (!submission.getResults().is("junit.compiles")) {
            LOGGER.finest("Skipping running JUnit tests for " + student);
            return submission;
        }

        String[] testClasses = classes.split("\\|");
        LOGGER.finest("Running student tests " + student);
        LOGGER.finest(Arrays.toString(testClasses));
        File working = new File(submission.getSource().getUnmaskedPath());

        Map<String, Integer> passes = new HashMap<>();
        for (String testClass : testClasses) {
            String classPath = solutionClassPath + ":" + submission.getWorking().getUnmaskedPath();
            Data results = JUnitRunner.runTest(testClass, classPath, working);
            if (results.get("passes") != null) {
                passes.put(testClass, Integer.parseInt(results.get("passes").toString()));
            }
        }

        for (String solution : classPaths.keySet()) {
            /* Class path for the particular solution */
            String classPath = classPaths.get(solution) + ":" + submission.getWorking().getUnmaskedPath();

            for (String testClass : testClasses) {
                String jsonRoot = SOLUTIONS_ROOT + "." + solution + "."
                        + testClass.replace(".", "\\.");

                /* Run the JUnit tests */
                Data results = JUnitRunner.runTest(testClass, classPath, working);
                results.set("correct", false);
                if (results.get("passes") != null) {
                    int passed = Integer.parseInt(results.get("passes").toString());
                    if (passed < passes.get(testClass)) {
                        results.set("correct", true);
                    }
                }
                submission.getResults().set(jsonRoot, results);
            }
        }

        return submission;
    }
}
