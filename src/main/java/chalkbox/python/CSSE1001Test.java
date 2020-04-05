package chalkbox.python;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.Execution;
import chalkbox.api.common.ProcessExecution;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

//todo(issue:#21) Refactor this to not be CSSE1001
@Processor(depends = RenameSubmissions.class)
public class CSSE1001Test {
    @ConfigItem(key = "python", required = false,
            description = "Command to execute python from terminal")
    public String PYTHON = "python3";

    @ConfigItem(key = "runner", description = "Name of the test runner")
    public File runner;

    @ConfigItem(key = "included", description = "Path to supplied assignment files")
    public File included;

    @Pipe
    public Collection run(Collection collection) {
        Data feedback = collection.getResults();
        ProcessExecution process;
        Map<String, String> environment = new HashMap<>();
        environment.put("PYTHONPATH", included.getPath());
        File working = new File(collection.getWorking().getUnmaskedPath());

        try {
            collection.getWorking().copyFolder(included);
        } catch (IOException e) {
            e.printStackTrace();
            feedback.set("test.error", "Unable to copy supplied directory");
            return collection;
        }

        try {
            process = Execution.runProcess(working, environment, 10000,
                    PYTHON, runner.getAbsolutePath(), "--json");
        } catch (IOException e) {
            System.err.println("Error occurred trying to spawn the test runner process (in json mode)");
            e.printStackTrace();
            feedback.set("test.error", "IOException occurred");
            return collection;
        } catch (TimeoutException e) {
            feedback.set("test.error", "Timed out executing tests");
            return collection;
        }

        String output = process.getOutput();
        feedback.set("test", new Data(output));

        System.err.println(process.getError());

        try {
            process = Execution.runProcess(working, environment, 10000,
                    PYTHON, runner.getPath());
        } catch (IOException e) {
            System.err.println("Error occurred trying to spawn the test runner process");
            e.printStackTrace();
            feedback.set("test.error", "IOException occurred");
            return collection;
        } catch (TimeoutException e) {
            feedback.set("test.error", "Timed out executing tests");
            return collection;
        }

        output = process.getOutput();
        feedback.set("output", output);

        return collection;
    }
}
