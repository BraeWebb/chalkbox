package chalkbox.output;

import chalkbox.api.annotations.Output;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class CompileErrors {
    @Output(stream = "submissions")
    public void output(PrintStream stream, List<Collection> collections) {
        List<String> compiling = new ArrayList<>();
        List<String> notCompiling = new ArrayList<>();

        for (Collection collection : collections) {
            Data results = collection.getResults();
            if (results.is("compilation.compiles")) {
                compiling.add((String) results.get("sid"));
            } else {
                stream.println(results.get("compilation.output"));
                notCompiling.add((String) results.get("sid"));
            }
        }

        stream.println("Compiling");
        for (String student : compiling) {
            stream.println(student);
        }

        stream.println("Not Compiling");
        for (String student : notCompiling) {
            stream.println(student);
        }
    }
}
