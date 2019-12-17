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
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Processor
public class Splat {
    @ConfigItem(key = "python", required = false,
            description = "Command to execute python from terminal")
    public String PYTHON = "python3";

    @ConfigItem(key = "splat", description = "Path to splat executable")
    public File splat;

    @Pipe
    public Collection run(Collection collection) {
        Data feedback = collection.getResults();
        Map<String, String> environment = new HashMap<>();
        environment.put("PYTHONPATH", splat.getPath());
        File working = Paths.get(splat.getPath() + "/splat_analysis").toFile();

        ProcessExecution process;
        try {
            process = Execution.runProcess(working, environment, 10000,
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

        String output = process.getOutput();
        output = output.replace("marking_static_criteria.json", "");
        output = output.replace("marking_rubric_convert.json", "");

        Data out = new Data(output);

        if (out.get("rubric") != null) {
            feedback.set("style", out.get("rubric"));
        }

        feedback.set("splat", out);

        return collection;
    }
}
