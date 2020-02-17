package chalkbox.java.compilation;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Prior;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import chalkbox.api.files.SourceFile;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Process to compile each file in a submission individually with the
 * files from a sample solution. Looks for files within src/ of the solution
 * and submissions compile.
 *
 * <p>The process of compiling the sample solution is detailed in
 * {@link IndividualJavaCompiler#compileSolution(Map)}
 *
 * <p>A compiling or non-compiling submission may produce the following output,
 * where ... stands for any output warnings or compilation errors when compiling:
 * <pre>
 * "compilation": {
 *      "package1.ClassOne": {
 *          "compiles": true,
 *          "output": "..."
 *      },
 *      "package2.ClassTwo": {
 *          "compiles": true,
 *          "output": "..."
 *      }
 * }
 * </pre>
 *
 * <p>If a particular file in the above output cannot be found in a submission,
 * it will have compiles set to false and output set to "File not found".
 *
 * <p>If issues occur reading source files or creating output folders, the
 * following output may be produced:
 * <pre>
 * "compilation": {
 *     "compiles": false,
 *     "error": "Couldn't create output directory - see tutor"
 * }
 * </pre>
 */
@Processor
public class IndividualJavaCompiler {
    @ConfigItem(description = "Class path to use to compile submissions")
    public String classPath;

    @ConfigItem(key = "solution",
            description = "Path to a sample solution to compile submitted files with")
    public String solutionPath;

    @ConfigItem(key = "temp", description = "A temporary output directory")
    public String outputFolder;

    /** A map of class names to the class path to compile that class with */
    private Map<String, String> classPaths = new HashMap<>();

    /**
     * A folder is created for each class in the sample solution.
     *
     * <p>A directory is created for each of the files in the sample solution.
     * e.g. For a sample solution with package1.ClassOne and package2.ClassTwo, the
     * following folders are created:
     * <pre>
     * package1.ClassOne/
     * package2.ClassTwo/
     * </pre>
     *
     * <p>The sample solution is compiled and a copy is placed within each of the
     * directories listed above.
     *
     * <p>For each of the directories, the class for the directory name is removed.
     */
    @Prior
    public void compileSolution(Map<String, String> config) {
        /* Load the sample solution files */
        File solutionFolder = new File(solutionPath);
        Bundle solution = new Bundle(solutionFolder);

        /* Create an output directory */
        File outputFolder = new File(this.outputFolder);
        if (!outputFolder.exists()) {
            if (!outputFolder.mkdirs()) {
                System.err.println("Unable to create temporary output directory");
                return;
            }
        }
        Bundle output = new Bundle(outputFolder);

        /* Load the solution source files to compile */
        List<SourceFile> files = Compiler.getSourceFiles(solution);
        if (files == null) {
            System.err.println("Unable to load source files");
            return;
        }

        StringWriter outStream = new StringWriter();
        for (String className : solution.getClasses("src")) {
            /* Create output folder for this class */
            String folder = "solution" + File.separator + className;
            File outFolder = new File(output.getUnmaskedPath(folder));
            if (!outFolder.exists() && !outFolder.mkdirs()) {
                System.err.println("Unable to create solution output directory: " + outFolder);
                continue;
            }

            // TODO: really Brae, there is no need to re-compile for each file, this is pure laziness
            Compiler.compile(files, classPath, outFolder.getPath(), outStream);
            output.refresh();

            /* Delete the file associated with the current class */
            String targetFile = folder + File.separator + output.getPathName(className);
            output.deleteFile(targetFile.replace(".java", ".class"));
            output.deleteFile(targetFile.replace(".java", "$1.class"));

            /* Map the class to it's associated classpath */
            classPaths.put(className, classPath + ":" + output.getUnmaskedPath(folder));
        }

        // TODO: this bad boy needs a debug logger
        System.out.println(outStream.toString());
    }

    @Pipe(stream = "submissions")
    public Collection compile(Collection submission) {
        Bundle working = submission.getWorking();
        Bundle source = submission.getSource();
        Data results = submission.getResults();

        /* Create a new output directory */
        if (!makeOutputDirectory(working, results)) {
            return submission;
        }

        /* Map each class name to its source file */
        Map<String, SourceFile> files = loadSourceFiles(source, results);
        if (files == null) {
            return submission;
        }

        StringWriter output;
        String classPath;
        for (String file : classPaths.keySet()) {
            /* Create JSON root, escaping any dots in the class name */
            String jsonRoot = "compilation." + file.replace(".", "\\.") + ".";
            if (!files.containsKey(file)) {
                results.set(jsonRoot + "compiles", false);
                results.set(jsonRoot + "output", "File not found");
                continue;
            }

            /* Reset output stream */
            output = new StringWriter();
            classPath = classPaths.get(file);

            /* Compile just the one file */
            List<SourceFile> toCompile = new ArrayList<>();
            toCompile.add(files.get(file));
            boolean success = Compiler.compile(toCompile, classPath,
                    working.getUnmaskedPath(file), output);

            results.set(jsonRoot + "compiles", success);
            results.set(jsonRoot + "output", output.toString());
        }

        working.refresh();
        return submission;
    }

    /** Attempt to create an output directory for a submission */
    private boolean makeOutputDirectory(Bundle working, Data results) {
        if (!working.makeDir("bin")) {
            results.set("compilation.compiles", false);
            results.set("compilation.error", "Couldn't create output directory - see tutor");
            return false;
        }
        return true;
    }

    /** Map each class name to its source file **/
    private Map<String, SourceFile> loadSourceFiles(Bundle source, Data results) {
        List<SourceFile> sourceFiles = Compiler.getSourceFiles(source);
        if (sourceFiles == null) {
            results.set("compilation.compiles", false);
            results.set("compilation.error", "Error loading source files - see tutor");
            return null;
        }

        Map<String, SourceFile> files = new HashMap<>();
        for (SourceFile file : sourceFiles) {
            files.put(source.getClassName(file.getName()), file);
        }
        return files;
    }
}
