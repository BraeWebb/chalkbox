package chalkbox.java.test;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Prior;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.JUnitRunner;
import chalkbox.api.files.FileLoader;
import chalkbox.java.compilation.IndividualJavaCompiler;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A version of the {@link JavaTest} process for the individual compilation process.
 *
 * Uses the class paths for each class to execute the JUnit test.
 */
@Processor(depends = {IndividualJavaCompiler.class})
public class IndividualJavaTest extends JavaTest {
    @ConfigItem(key = "temp", description = "A temporary output directory")
    public String tempResults;

    @ConfigItem(key = "included", required = false,
            description = "Folder containing files to include in submission working directory when running tests")
    public File included = null;

    private Map<String, String> classPaths = new HashMap<>();

    private Bundle tests;

    @Prior
    public void buildClassPath(Map<String, String> config) {
        File solutionFolder = new File(tempResults + File.separator + "solution");

        File[] classes = solutionFolder.listFiles();
        for (File clazz : classes) {
            classPaths.put(FileLoader.truncatePath(solutionFolder, clazz), clazz.getPath());
        }

        tests = new Bundle(new File(testPath));
    }

    @Pipe(stream = "submissions")
    public Collection runTests(Collection submission) {
        if (hasErrors) {
            return submission;
        }

        try {
            if (included != null) {
                submission.getWorking().copyFolder(included);
            }
        } catch (IOException e) {
            submission.getResults().set("tests.error.errors",
                    "Unable to populate working directory");
        }

        File working = new File(submission.getWorking().getUnmaskedPath());

        for (String className : tests.getClasses("")) {
            String clazz = className.replace("Test", "");
            String rootJSON = "tests." + className
                    .replace(".", "\\.");

            if (!submission.getResults().is("compilation."
                    + clazz.replace(".", "\\.") + ".compiles")) {
                submission.getResults().set(rootJSON + ".errors",
                        "Class could not compile - tests not run");
                continue;
            }

            String classPath = this.classPath + ":" + classPaths.get(clazz)
                    + ":" + submission.getWorking().getUnmaskedPath(clazz);
            Data results = JUnitRunner.runTest(className, classPath, working);
            submission.getResults().set(rootJSON, results);
        }

        return submission;
    }
}
