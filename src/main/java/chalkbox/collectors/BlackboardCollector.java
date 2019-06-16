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
public class BlackboardCollector {
    @ConfigItem(key = "gradebook", description = "Location of the blackboard gradebook")
    public String gradebook;

    @ConfigItem(key = "json", required = false, description = "Location of json files")
    public String json = null;

    @DataSet(stream = "submissions")
    public List<Collection> collect(Map<String, String> config) throws IOException {
        ZipFile zip = new ZipFile(gradebook);

        Map<String, Collection> data = new HashMap<>();

        for (ZipEntry entry : zip.stream().collect(Collectors.toList())) {
            if (entry.getName().endsWith(".txt")) {
                Scanner s = new Scanner(zip.getInputStream(entry)).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";

                Data submissionData = readSubmissionFile(result);
                String sid = submissionData.get("sid").toString();

                Data loaded = loadData(sid);
                if (loaded != null) {
                    submissionData = loaded;
                }

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

    private Data loadData(String sid) {
        if (json == null) {
            return null;
        }

        String jsonPath = json + File.separator + sid + ".json";
        File file = new File(jsonPath);

        Data data = new Data();

        if (file.exists()) {
            try {
                data = new Data(new String(Files.readAllBytes(file.toPath())));
            } catch (IOException e) {
                System.err.println("Couldn't read json file: " + file);
                return null;
            }
        } else {
            return null;
        }

        return data;
    }

//        for (Map.Entry<Submission, String> entry : zips.entrySet()) {
//            if (entry.getValue() == null) {
//                continue;
//            }
//
//            BlackboardSubmission submission = (BlackboardSubmission) entry.getKey();
//            ZipFile studentZip;
//            try {
//                studentZip = innerZip(zip, entry.getValue());
//                submission.loadZip(studentZip);
//            } catch (IOException io) {
//                logger.log("Unable to open zip");
//                logger.log(entry.getValue());
//            }
//
//            submission.setSchema(new Schema(submission.getFileNames()));
//        }
//    }

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
                    data.set(map[0], map[1]);
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
