package chalkbox.output;

import chalkbox.api.annotations.Output;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.PrintStream;
import java.util.List;

public class ConformanceFilter {
    @Output(stream = "submissions")
    public void output(PrintStream stream, List<Collection> collections) {
        for (Collection collection : collections) {
            boolean filtered = false;
            Data results = collection.getResults();

            if (!results.is("compilation.compiles")) {
                filtered = true;
            }

//            for (String key : results.keys("conformance")) {
//                try {
//                    if (results.is("conformance."
//                            + key.replace(".", "\\.") + ".differs")) {
//                        filtered = true;
//                    }
//                } catch (ClassCastException e) {
//                    filtered = true;
//                }
//            }

            if (!filtered) {
                stream.println(results.get("sid"));
            }
        }
    }
}
