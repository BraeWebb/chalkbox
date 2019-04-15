package chalkbox.java;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;

import javax.tools.JavaFileObject;
import java.io.StringWriter;

/**
 * Process to compile all of the .java source files in a submission.
 *
 * <p>A compiling submission may produce the following output, where ... stands
 * for any output warnings when compiling:
 * <pre>
 * "compilation": {
 *     "compiles": true,
 *     "output": "..."
 * }
 * </pre>
 *
 * <p>A non-compiling submission may produce the following output, where ... stands
 * for the compilation errors:
 * <pre>
 * "compilation": {
 *     "compiles": false,
 *     "output": "..."
 * }
 * </pre>
 *
 * <p>A submission without any .java files will have compilation.compiles set
 * to false and compilation.output set to "Empty submission"
 */
@Processor
public class JavaCompilation {
    @ConfigItem(description = "Class path to use to compile submissions")
    private String classPath;


    @Pipe(stream = "submissions")
    public Collection compile(Collection submission) {
        Bundle working = submission.getWorking();
        Data results = submission.getResults();

        results.set("compilation.compiles", false);

        /* Attempt to create a new output directory */
        if (!working.makeDir("bin")) {
            System.err.println("Couldn't create output directory");
            results.set("compilation.error", "Couldn't create output directory - see tutor");
            return submission;
        }

        Iterable<? extends JavaFileObject> sourceFiles = Compiler.getSourceFiles(submission.getSource());
        if (sourceFiles == null) {
            System.err.println("Couldn't load source files");
            results.set("compilation.error", "Error loading source files - see tutor");
            return submission;
        }

        StringWriter output = new StringWriter();
        String classPath = submission.getSource().getUnmaskedPath() + ":" + this.classPath;

        boolean success = Compiler.compile(sourceFiles, classPath,
                working.getUnmaskedPath("bin"), output);

        results.set("compilation.compiles", success);
        results.set("compilation.output", output.toString());
        working.refresh();
        return submission;
    }
}
