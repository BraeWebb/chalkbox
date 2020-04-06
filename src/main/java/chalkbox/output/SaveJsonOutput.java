package chalkbox.output;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Output;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;

public class SaveJsonOutput {
    @ConfigItem(key = "json")
    public String json;

    @Output(stream = "submissions")
    public void output(PrintStream stream, List<Collection> collections) {
        for (Collection collection : collections) {
            Data results = collection.getResults();
            results.set("timestamp", System.currentTimeMillis() / 1000L);
            File jsonFile;
            if (results.get("json") != null) {
                jsonFile = new File((String) results.get("json"));
            } else {
                jsonFile = new File(json + File.separator + results.get("sid") + ".json");
            }

            try {
                Files.write(jsonFile.toPath(), results.toString().getBytes());
            } catch (IOException e) {
                System.err.println("Unable to write json file for " + results.get("sid"));
            }
        }
    }
}
