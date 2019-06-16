package chalkbox.integrity;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.GroupPipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.common.Execution;
import chalkbox.api.common.ProcessExecution;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Processor(threads = 1)
public class MossReport {
    @ConfigItem(description = "path to the moss bash script from stanford")
    public String mossnet;

    @ConfigItem(description = "path to the supplied code to ignore for the report")
    public String supplied;

    @ConfigItem(description = "language the execute the language with")
    public String language;
    @ConfigItem(description = "extension of the type of file to look for")
    public String extension;

    @GroupPipe(stream = "submissions")
    public void check(List<Collection> collections) {
        Bundle suppliedFiles = new Bundle(new File(supplied));

        List<String> arguments = new ArrayList<>();

        arguments.add("sh");
        arguments.add(mossnet);
        arguments.add("-l");
        arguments.add(language);
        arguments.add("-d");

        String[] filePaths = suppliedFiles.getFileNames(extension)
                .toArray(new String[]{});
        for (String path : filePaths) {
            arguments.add("-b");
            arguments.add(suppliedFiles.getUnmaskedPath(path));
        }

        for (Collection submission : collections) {
            List<String> paths = submission.getSource().getFileNames(extension);
            for (String path : paths) {
                arguments.add(submission.getSource().getUnmaskedPath(path));
            }
        }

        for (String part : arguments) {
            System.out.print(part + " ");
        }

        ProcessExecution execution;
        try {
            execution = Execution.runProcess(20000000,
                    arguments.toArray(new String[]{}));
        } catch (TimeoutException e) {
            e.printStackTrace();
            System.err.println("Timeout when running MoSS");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException runnig MoSS");
            return;
        }

        System.out.println(execution.getOutput());
        System.out.println(execution.getError());
    }
}
