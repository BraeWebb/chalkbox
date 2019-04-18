package chalkbox.java.test;

import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Prior;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.JUnitRunner;
import chalkbox.api.files.FileLoader;
import chalkbox.java.compilation.IndividualJavaCompiler;
import chalkbox.java.test.JavaTest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A version of the {@link JavaTest} process for the individual compilation process.
 *
 * Uses the class paths for each class to execute the JUnit test.
 */
@Processor(depends = {IndividualJavaCompiler.class})
public class IndividualJavaTest extends JavaTest {
    private Map<String, String> classPaths = new HashMap<>();

    @Prior
    public void buildClassPath(Map<String, String> config) {
        File solutionFolder = new File(config.get("results") + File.separator + "solution");

        File[] classes = solutionFolder.listFiles();
        for (File clazz : classes) {
            classPaths.put(FileLoader.truncatePath(solutionFolder, clazz), clazz.getPath());
        }
    }

    @Pipe(stream = "submissions")
    public Collection runTests(Collection submission) {
        if (hasErrors) {
            return submission;
        }

        for (String clazz : classPaths.keySet()) {
            if (!submission.getResults().is("compilation."
                    + clazz.replace(".", "\\.") + ".compiles")) {
                return submission;
            }

            String classPath = this.classPath + ":" + classPaths.get(clazz)
                    + ":" + submission.getWorking().getUnmaskedPath(clazz);
            String className = clazz + "Test";
            Data results = JUnitRunner.runTest(className, classPath);
            submission.getResults().set("tests." + className
                    .replace(".", "\\."), results);
        }

        return submission;
    }
}
