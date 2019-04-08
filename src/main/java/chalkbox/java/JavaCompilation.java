package chalkbox.java;

import chalkbox.api.annotations.Asset;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.Compiler;
import chalkbox.api.files.SourceFile;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;

@Processor
public class JavaCompilation {
    private Bundle working;
    private Bundle source;
    private Data results;

    private String classPath;

    @Asset
    public void getClassPath(Map<String, String> config) {
        classPath = config.get("classPath");
    }

    @Pipe(stream = "submissions")
    public Collection compile(Collection submission) {
        working = submission.getWorking();
        source = submission.getSource();
        results = submission.getResults();

        // Create a new output directory
        if (!makeOutputDirectory()) {
            return submission;
        }

        StringWriter output = new StringWriter();
        classPath = source.getUnmaskedPath("") + ":" + classPath;

        boolean success = Compiler.compile(loadSourceFiles(), classPath,
                working.getUnmaskedPath("bin"), output);

        results.set("compilation.compiles", success);
        results.set("compilation.output", output.toString());
        working.refresh();
        return submission;
    }

    private boolean makeOutputDirectory() {
        if (!working.makeDir("bin")) {
            results.set("compilation.compiles", false);
            results.set("compilation.error", "Couldn't create output directory - see tutor");
            return false;
        }
        return true;
    }

    private Iterable<? extends JavaFileObject> loadSourceFiles() {
        SourceFile[] sourceFiles;
        try {
            sourceFiles = source.getFiles(".java");
        } catch (IOException e) {
            results.set("compilation.compiles", false);
            results.set("compilation.error", "Error loading source files - see tutor");
            return null;
        }
        return Arrays.asList(sourceFiles);
    }
}
