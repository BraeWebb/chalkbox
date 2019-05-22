package chalkbox.python;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.Execution;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Processor
public class Splat {
    @ConfigItem(key = "python", required = false,
            description = "Command to execute python from terminal")
    public String PYTHON = "python3";

    @ConfigItem(key = "splat", description = "Path to splat executable")
    public String splat;

    @Pipe(stream = "submissions")
    public Collection run(Collection collection) {
        Data feedback = collection.getResults();
        Map<String, String> environment = new HashMap<>();
        environment.put("PYTHONPATH", splat);

        Process process;
        try {
            process = Execution.runProcess(environment, 10000,
                    PYTHON, "-m", "splat_analysis.cmd",
                    collection.getWorking().getUnmaskedPath(), "-all");
        } catch (IOException e) {
            e.printStackTrace();
            feedback.set("splat.error", "IOError when running splat");
            return collection;
        } catch (TimeoutException e) {
            e.printStackTrace();
            feedback.set("splat.error", "Timed out when running splat");
            return collection;
        }

        System.out.println(Execution.readStream(process.getInputStream()));
        System.out.println(Execution.readStream(process.getErrorStream()));

        collection.getWorking().refresh();
        System.out.println(collection.getWorking().getFileNames());

        return collection;
    }
}
