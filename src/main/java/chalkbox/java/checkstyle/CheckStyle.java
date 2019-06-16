package chalkbox.java.checkstyle;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.Execution;
import chalkbox.api.common.ProcessExecution;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

/**
 * Processor to execute the checkstyle tool on a each submission, adding
 * the results of checkstyle to the checkstyle key in the output JSON.
 *
 * <pre>
 * {
 *     "checkstyle": "[checkstyle output]"
 * }
 * </pre>
 */
@Processor
public class CheckStyle {
    @ConfigItem(description = "path to a checkstyle jar to execute")
    public String checkstyleJar;

    @ConfigItem(description = "path to the checkstyle configuration to use when running")
    public String checkstyleConfig;

    private static final String JSON_ITEM = "checkstyle";

    @Pipe(stream = "submissions")
    public Collection run(Collection collection) {
        Data feedback = collection.getResults();

        // execute the checkstyle jar on the src directory
        ProcessExecution process;
        try {
            process = Execution.runProcess(10000,
                    "java", "-jar", checkstyleJar, "-c", checkstyleConfig,
                    collection.getSource().getUnmaskedPath("src"));
        } catch (IOException e) {
            e.printStackTrace();
            feedback.set(JSON_ITEM, "IOError when running checkstyle");
            return collection;
        } catch (TimeoutException e) {
            e.printStackTrace();
            feedback.set(JSON_ITEM, "Timed out when running checkstyle");
            return collection;
        }

        // get the absolute base path of src
        String basePath = Paths.get(collection.getSource().getUnmaskedPath()).toAbsolutePath().toString();

        // replace the base path and set the output
        feedback.set(JSON_ITEM, process.getOutput().replace(basePath, ""));

        return collection;
    }
}
