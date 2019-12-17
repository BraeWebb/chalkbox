package chalkbox.collectors;

import chalkbox.api.annotations.Collector;
import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.DataSet;
import chalkbox.api.annotations.Prior;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Collector
public class FolderCollector extends LoadSubmissionData {
    @ConfigItem(key = "root", description = "Root directory of folder submissions")
    public File folder;

    private boolean parseError = false;

    @Prior
    public void loadFolder(Map<String, String> config) {
        if (!this.folder.isDirectory()) {
            parseError = true;
            System.err.println("Folder parameter does not exist or is not a folder");
        }
    }

    @DataSet
    public List<Collection> collect(Map<String, String> config) {
        if (parseError) {
            return null;
        }

        List<Collection> collections = new ArrayList<>();
        for (File submissionFolder : folder.listFiles()) {
            if (!submissionFolder.isDirectory()) {
                continue;
            }

            Data metadata = loadData(submissionFolder.getName());

            metadata.set("root", submissionFolder.getPath());

            Collection collection = new Collection(metadata);
            collections.add(collection);
        }
        return collections;
    }
}
