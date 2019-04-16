package chalkbox.api.common.java;

import chalkbox.api.collections.Bundle;
import chalkbox.api.files.SourceFile;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for compiling Java source code.
 */
public class Compiler {

    /**
     * Compile java source files into java byte code files.
     *
     * If the output path doesn't exist it will attempt to be created.
     *
     * @param files The source java files to compile.
     * @param classPath The classpath to compile with.
     * @param outputPath The path of the folder to output the java byte code files.
     * @param output A string writer for the output from compiling the source files.
     *
     * @return true iff the files were compiled successfully.
     */
    public static boolean compile(Iterable<? extends JavaFileObject> files,
                                  String classPath, String outputPath,
                                  StringWriter output) {
        /* Try to create the output path directory */
        File outFile = new File(outputPath);
        if (!outFile.exists()) {
            if (!outFile.mkdirs()) {
                output.write("Unable to create output directory - See tutor");
                return false;
            }
        }

        List<String> options = new ArrayList<>();
        options.add("-cp");
        options.add(classPath);
        options.add("-d");
        options.add(outputPath);

        return compile(files, output, options);
    }

    /**
     * Compile java source files into java byte code files.
     *
     * @param files The source java files to compile.
     * @param output A string writer for the output from compiling the source files.
     * @param options Command line options for the compilation process.
     *
     * @return true iff the files were compiled successfully.
     */
    public static boolean compile(Iterable<? extends JavaFileObject> files,
                                  StringWriter output, List<String> options) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        boolean success;
        try {
            success = compiler.getTask(output, null,
                    null, options, null, files).call();
        } catch (IllegalStateException e) {
            output.write("Empty submission");
            return false;
        }

        return success;
    }

    /**
     * Get all of the java source files in a bundle as JavaFileObjects.
     *
     * @param source The bundle to search through.
     * @return A generic iterable of JavaFileObjects.
     */
    public static List<SourceFile> getSourceFiles(Bundle source) {
        try {
            return Arrays.asList(source.getFiles(".java"));
        } catch (IOException e) {
            return null;
        }
    }
}
