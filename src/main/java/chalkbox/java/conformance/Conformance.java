package chalkbox.java.conformance;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Prior;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import chalkbox.java.compilation.JavaCompilation;
import chalkbox.java.conformance.comparator.ClassComparator;
import chalkbox.java.conformance.comparator.CodeComparator;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@Processor(depends = {JavaCompilation.class})
public class Conformance {
    @ConfigItem
    public String conformance;

    @ConfigItem
    public String classPath;

    private Map<String, Class> expectedClasses;

    /**
     * Loads the expected class files into the conformance checker
     */
    @Prior
    public void loadExpected(Map<String, String> config) throws IOException {
        Bundle expected = new Bundle(new File(conformance));
        StringWriter output = new StringWriter();

        /* Load output directories for the solution and the tests */
        Bundle out;
        try {
            out = new Bundle();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        /* Compile the sample solution */
        Compiler.compile(Compiler.getSourceFiles(expected), classPath,
                out.getUnmaskedPath(), output);

        SourceLoader expectedLoader = new SourceLoader(out.getUnmaskedPath());
        try {
            expectedClasses = expectedLoader.getClassMap();
        } catch (ClassNotFoundException cnf) {
            throw new RuntimeException("Failed to load expected class");
        }
    }

    @Pipe(stream = "submissions")
    public Collection compare(Collection submission) throws IOException {
        Data data = submission.getResults();
        SourceLoader submissionLoader = new SourceLoader(submission.getWorking()
                .getUnmaskedPath("bin"));
        Map<String, Class> submissionMap;
        try {
            submissionMap = submissionLoader.getClassMap();
        } catch (ClassNotFoundException cnf) {
            System.err.println("Failed to find class");
            cnf.printStackTrace();
            return submission;
        }

        for (String className : expectedClasses.keySet()) {
            String jsonKey = "conformance." + className.replace(".", "\\.") + ".";
            Class expectedClass = expectedClasses.get(className);
            Class actualClass = submissionMap.get(className);

            if (expectedClass == null || actualClass == null) {
                data.set(jsonKey + "differs", true);
                data.set(jsonKey + "output", "Unable to load class");
                continue;
            }

            CodeComparator<Class> comparator = new ClassComparator(expectedClass,
                    actualClass);
            data.set(jsonKey + "differs", comparator.hasDifference());
            data.set(jsonKey + "output", comparator.toString());
        }

        return submission;
    }
}
