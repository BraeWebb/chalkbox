package chalkbox.collectors;

import chalkbox.api.annotations.Collector;
import chalkbox.api.annotations.DataSet;
import chalkbox.api.annotations.Parser;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Collector
public class FolderCollector {
    private File folder;
    private boolean parseError = false;

    @DataSet(stream = "submissions")
    public List<Collection> collect(Map<String, String> config) {
        if (parseError) {
            return null;
        }

        List<Collection> collections = new ArrayList<>();
        for (File submissionFolder : folder.listFiles()) {
            if (!submissionFolder.isDirectory()) {
                continue;
            }

            Data metadata = new Data();

            metadata.set("sid", submissionFolder.getName());
            metadata.set("root", submissionFolder.getPath());

            Collection collection = new Collection(metadata);
            collections.add(collection);
        }
        return collections;
    }

    @Parser
    public String parseArgs(Map<String, String> config) {
        if (!config.containsKey("root")) {
            parseError = true;
            return "Missing folder root configuration";
        }

        String folder = config.get("root");
        this.folder = new File(folder);

        if (!this.folder.exists() || !this.folder.isDirectory()) {
            parseError = true;
            return "Folder parameter does not exist or is not a folder";
        }

        return "";
    }
}
