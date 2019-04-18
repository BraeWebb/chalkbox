package chalkbox.output;

import chalkbox.api.annotations.Output;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;

public class SaveJsonOutput {
    @Output(stream = "submissions")
    public void output(PrintStream stream, List<Collection> collections) {
        for (Collection collection : collections) {
            Data results = collection.getResults();
            File jsonFile = new File((String) results.get("json"));
            try {
                Files.write(jsonFile.toPath(), results.toString().getBytes());
            } catch (IOException e) {
                System.err.println("Unable to write json file for " + results.get("sid"));
            }
        }
    }
}
