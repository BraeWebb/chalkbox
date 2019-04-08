package chalkbox.api.common;

import chalkbox.api.collections.Bundle;
import chalkbox.api.files.SourceFile;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Compiler {
    public static boolean compile(Iterable<? extends JavaFileObject> files,
                                  String classPath, String outputPath,
                                  StringWriter output) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        List<String> options = new ArrayList<>();
        options.add("-cp");
        options.add(classPath);
        options.add("-d");
        options.add(outputPath);

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

    public static Iterable<? extends JavaFileObject> getSourceFiles(Bundle source) {
        SourceFile[] sourceFiles;
        try {
            sourceFiles = source.getFiles(".java");
        } catch (IOException e) {
            return null;
        }
        return Arrays.asList(sourceFiles);
    }
}
