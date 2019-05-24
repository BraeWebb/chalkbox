package chalkbox.output;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Output;
import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports each working directory for a submission into a folder.
 */
public class ExportWorking {
    @ConfigItem(description = "Directory to place a copy of the working directory")
    public String directory;

    @Output(stream = "submissions")
    public void output(PrintStream stream, List<Collection> collections)
            throws IOException {
        File out = new File(directory);
        if (!out.exists()) {
            out.mkdirs();
        }
        Bundle output = new Bundle(new File(directory));

        Files.write(buildData(collections.get(0).getResults()).toString(),
                new File(output.getUnmaskedPath("config.criteria")),
                Charset.defaultCharset());

        for (Collection collection : collections) {
            String sid = collection.getResults().get("sid").toString();
            if (!output.makeDir(sid)) {
                System.err.println("Unable to make output directory");
                continue;
            }
            Bundle working = output.getBundle(sid);
            try {
                working.copyFolder(new File(collection.getWorking().getUnmaskedPath()));
                Files.write(reformatStyle(collection.getResults()).toString(),
                        new File(working.getUnmaskedPath(sid + ".style")),
                        Charset.defaultCharset());
            } catch (IOException e) {
                System.err.println("Unable to copy directory");
            }
        }
    }

    public static Data reformatStyle(Data data) {
        Data result = new Data();
        for (String category : data.keys("style")) {
            result.set(category + "." + "grade", data.get("style." + category));
            result.set(category + "." + "possible", 200);
            result.set(category + "." + "comments", new ArrayList<>());
        }
        return result;
    }

    public static Data buildData(Data data) {
        Data criteria = new Data();
        for (String category : data.keys("style")) {
            criteria.set(category + "." + "possible", 200);
            criteria.set(category + "." + "violations", new Data());
        }
        Data result = new Data();
        result.set("criteria", criteria);
        return result;
    }
}
