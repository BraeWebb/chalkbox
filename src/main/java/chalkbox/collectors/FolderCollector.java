package chalkbox.collectors;

import chalkbox.api.annotations.Collector;
import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.DataSet;
import chalkbox.api.annotations.Prior;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Collector
public class FolderCollector {
    @ConfigItem(key = "root", description = "Root directory of folder submissions")
    public String folderPath;

    private File folder;
    private boolean parseError = false;

    @Prior
    public void loadFolder(Map<String, String> config) {
        this.folder = new File(folderPath);

        if (!this.folder.exists() || !this.folder.isDirectory()) {
            parseError = true;
            System.err.println("Folder parameter does not exist or is not a folder");
        }
    }

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

            String jsonPath = submissionFolder.getPath() + File.separator
                    + submissionFolder.getName() + ".json";
            File configFile = new File(jsonPath);

            Data metadata = new Data();

            if (!configFile.exists()) {
                try {
                    configFile.createNewFile();
                } catch (IOException e) {
                }
            } else {
                try {
                    metadata = new Data(new String(Files.readAllBytes(configFile.toPath())));
                } catch (IOException e) {
                }
            }

            metadata.set("sid", submissionFolder.getName());
            metadata.set("root", submissionFolder.getPath());
            metadata.set("json", configFile.getPath());


            Collection collection = new Collection(metadata);
            collections.add(collection);
        }
        return collections;
    }
}
