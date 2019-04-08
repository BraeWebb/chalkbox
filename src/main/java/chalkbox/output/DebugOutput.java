package chalkbox.output;

import chalkbox.api.annotations.Output;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.PrintStream;
import java.util.List;

public class DebugOutput {
    @Output(stream = "submissions")
    public void output(PrintStream stream, List<Collection> collections) {
        for (Collection collection : collections) {
            Data results = collection.getResults();
            stream.println(results);
        }
    }
}
