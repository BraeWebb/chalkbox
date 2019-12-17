package chalkbox.collectors;

import chalkbox.api.annotations.Collector;
import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.DataSet;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Collector
public class BlackboardCollector extends LoadSubmissionData {
    @ConfigItem(key = "gradebook", description = "Location of the blackboard gradebook")
    public File gradebook;

    @DataSet
    public List<Collection> collect(Map<String, String> config) throws IOException {
        ZipFile zip = new ZipFile(gradebook);

        Map<String, Collection> data = new HashMap<>();

        for (ZipEntry entry : zip.stream().collect(Collectors.toList())) {
            if (entry.getName().endsWith(".txt")) {
                if (entry.getName().split("_").length != 4) {
                    continue;
                }
                Scanner s = new Scanner(zip.getInputStream(entry)).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";

                Data submissionData = readSubmissionFile(result);
                String sid = submissionData.get("sid").toString();

                submissionData = loadData(sid, submissionData);

                submissionData.set("root", ".");

                Collection collection = data.getOrDefault(sid, new Collection(submissionData));
                data.put(sid, collection);

                for (String file : submissionData.keys("files")) {
                    String key = "files." + file.replace(".", "\\.");

                    InputStream fileStream = zip.getInputStream(
                            zip.getEntry(submissionData.get(key).toString()));
                    File outputFile = new File(collection.getWorking()
                            .getUnmaskedPath(file));
                    Files.copy(fileStream, outputFile.toPath());
                }
                collection.getWorking().refresh();
            }
        }

        return new ArrayList<>(data.values());
    }

    // Because unzipping a zip in java is equally stupid
    // source: https://stackoverflow.com/questions/11287486/read-a-zip-file-inside-zip-file
    private static ZipFile innerZip(ZipFile zipFile, String zipFileName) throws IOException {
        File tempFile = File.createTempFile("tempFile", "zip");
        tempFile.deleteOnExit();
        InputStream inStream = zipFile.getInputStream(new ZipEntry(zipFileName));
        Files.copy(inStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return new ZipFile(tempFile);
    }

    private static Data readSubmissionFile(String submissionFile) {
        String[] brokenFile = submissionFile.split("\n");
        String lastKey = null;
        String currentFile = "unnamed";
        Data data = new Data();

        Data files = new Data();

        for (String line : brokenFile) {
            if (line.isEmpty()) {
                continue;
            }

            // handle one-line key-pair value
            if (line.contains(": ")) {
                String[] map = line.split(": ", 2);
                if (map[0].equals("\tOriginal filename")) {
                    currentFile = map[1].replace(".", "\\.");
                } else if (map[0].equals("\tFilename")) {
                    files.set(currentFile, map[1]);
                } else {
                    if (!data.keys().contains(map[0])) {
                        data.set(map[0], map[1]);
                    }
                }
                // multi-line key
            } else if (line.contains(":")) {
                lastKey = line.split(":")[0];
                // multi-line value
            } else {
                if (data.keys().contains(lastKey)) {
                    data.set(lastKey, data.get(lastKey) + line);
                } else {
                    data.set(lastKey, line);
                }
            }
        }

        data.set("files", files);

        // Extract the student number
        Pattern pattern = Pattern.compile("\\((.*)\\)");
        Matcher matcher = pattern.matcher(data.get("Name").toString());
        if (matcher.find()) {
            data.set("sid", matcher.group(1));
        }

        return data;
    }
}
