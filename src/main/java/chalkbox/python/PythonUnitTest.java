package chalkbox.python;

import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.Execution;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Processor
public class PythonUnitTest {

    @Pipe
    public Collection run(Collection collection) {
        File working = new File(collection.getSource().getUnmaskedPath());
        String output = collection.getWorking().getUnmaskedPath("results.json");

        try {
            Execution.runProcess(working, 10000, "py.test",
                    "--json=" + output, "-p", "no:cacheprovider");
            collection.getWorking().refresh();
            String report = collection.getWorking().getFile("results.json")
                    .getCharContent(true).toString();
            Object data = new Data(report).get("report");
            collection.getResults().set("pytest", data);
        } catch (IOException e) {
            collection.getResults().set("pytest", "IOException - see a tutor");
        } catch (TimeoutException e) {
            collection.getResults().set("pytest", "Timeout");
        }

        return collection;
    }
}
